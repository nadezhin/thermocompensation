package ru.nsc.interval.thermocompensation.intopt;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.java.jinterval.interval.set.SetIntervalContext;
import net.java.jinterval.interval.set.SetIntervalContexts;
import ru.nsc.interval.thermocompensation.model.ParseTestInps;
import ru.nsc.interval.thermocompensation.model.PolyState;

/**
 *
 */
public class Csv {

    static SetIntervalContext ic = SetIntervalContexts.getDefault();

    private static void help() {
        System.out.println("Usage: java -ea -jar intoptXXX.jar dir [-sN]");
        System.out.println("  -sN stage N   - N=1 or N=2");
        System.out.println("examples:");
        System.out.println(" P");
        System.out.println(" 150616V15 -s1");
        System.out.println(" 150601V15 -s1");
        System.exit(0);
    }

    public static void main(String[] args) throws Exception {
        int stage = 0;
        List<String> argsList = new ArrayList<>();
        for (String arg : args) {
            System.out.print(" " + arg);
            if (arg.startsWith("-")) {
                if (arg.startsWith("-s")) {
                    stage = Integer.parseInt(arg.substring(2));
                    if (stage < 1 || stage > 2) {
                        help();
                    }
                } else {
                    help();
                }
            } else {
                argsList.add(arg);
            }
        }
        System.out.println();
        String dir = argsList.get(0);
        if (!dir.isEmpty() && !dir.endsWith("/")) {
            dir = dir + "/";
        }
        String resultFile;
        Map<IntervalPolyModel, List<IntervalModel>> allModels;
        List<List<ParseTestInps.ExtendedInp>> inps, tasks;
        List<List<PolyState.Inp>> olpSpecInps, olpManufInps, ointSpecInps, ointManufInps;
        switch (stage) {
            case 0:
                resultFile = dir + "cmp.csv";
                inps = tasks = null;
                allModels = IntervalModel.readCsvModels(dir + "P.csv", ic);
                olpSpecInps = readInps(dir + "olps.txt");
                olpManufInps = readInps(dir + "olpm.txt");
                ointSpecInps = readInps(dir + "oints.txt");
                ointManufInps = readInps(dir + "ointm.txt");
                break;
            case 1:
                resultFile = dir + "cmp1.csv";
                inps = ParseTestInps.parseLogExtendedInps(Paths.get(dir + "nom_inps.txt"));
                tasks = ParseTestInps.parseLogExtendedInps(Paths.get(dir + "o1.txt"));
                allModels = IntervalModel.readTF0Models(dir + "m1", inps, tasks, ic);
                olpSpecInps = readInps(dir + "olps1.txt");
                olpManufInps = readInps(dir + "olpm1.txt");
                ointSpecInps = readInps(dir + "oints1.txt");
                ointManufInps = readInps(dir + "ointm1.txt");
                break;
            case 2:
                resultFile = dir + "cmp2.csv";
                inps = tasks = ParseTestInps.parseLogExtendedInps(Paths.get(dir + "o1.txt"));
                allModels = IntervalModel.readTF0Models(dir + "m2", inps, tasks, ic);
                olpSpecInps = readInps(dir + "olps2.txt");
                olpManufInps = readInps(dir + "olpm2.txt");
                ointSpecInps = readInps(dir + "oints2.txt");
                ointManufInps = readInps(dir + "ointm2.txt");
                break;
            default:
                throw new AssertionError();
        }
        List<IntervalModel> specModels = allModels.get(IntervalPolyModel.SPECIFIED);
        List<IntervalModel> manufModels = allModels.get(IntervalPolyModel.MANUFACTURED);
        try (PrintWriter out = new PrintWriter(resultFile)) {
            out.println("номер образца,выигрыш ppm,"
                    + "ppm. без корр. LP,ppm. с корр. LP,ppm. без корр. INT,ppm. с корр. INT,"
                    + "коэфф. без корр. LP,коэфф. с корр. LP,коэфф. без корр. INT,коэфф. с корр. INT");
            for (int chipNo = 0; chipNo < 64; chipNo++) {
                IntervalModel specModel = chipNo < specModels.size() ? specModels.get(chipNo) : null;
                IntervalModel manufModel = chipNo < manufModels.size() ? manufModels.get(chipNo) : null;
                if (specModel != null && manufModel != null) {
                    PolyState.Inp olpSpecInp = getInp(chipNo, olpSpecInps);
                    PolyState.Inp olpManufInp = getInp(chipNo, olpManufInps);
                    PolyState.Inp ointSpecInp = getInp(chipNo, ointSpecInps);
                    PolyState.Inp ointManufInp = getInp(chipNo, ointManufInps);
                    double olpSpecPpm = calcPpm(specModel, olpSpecInp);
                    double olpManufPpm = calcPpm(manufModel, olpManufInp);
                    double ointSpecPpm = calcPpm(specModel, ointSpecInp);
                    double ointManufPpm = calcPpm(manufModel, ointManufInp);
                    out.println((chipNo + 1)
                            + "," + (olpManufPpm - olpSpecPpm)
                            + "," + olpManufPpm
                            + "," + olpSpecPpm
                            + "," + (ointManufInp != null ? ointManufPpm : "")
                            + "," + (ointSpecInp != null ? ointSpecPpm : "")
                            + "," + olpManufInp.toNom()
                            + "," + olpSpecInp.toNom()
                            + "," + (ointManufInp != null ? ointManufInp.toNom() : "")
                            + "," + (ointSpecInp != null ? ointSpecInp.toNom() : ""));
                    if (ointSpecPpm < olpSpecPpm || ointManufPpm < olpManufPpm) {
                        System.out.println("Great !!! Interval optimization on chip "
                                + (chipNo + 1) + " finds better result than linear programming");
                    }
                }

            }
        }
    }

    private static List<List<PolyState.Inp>> readInps(String file) throws IOException {
        List<List<PolyState.Inp>> result = Collections.emptyList();
        Path path = Paths.get(file);
        if (path.toFile().canRead()) {
            result = ParseTestInps.parseTestInps(path);
        }
        return result;
    }

    private static PolyState.Inp getInp(int chipNo, List<List<PolyState.Inp>> inps) {
        if (chipNo >= 0 && chipNo < inps.size()) {
            List<PolyState.Inp> chipInps = inps.get(chipNo);
            if (!chipInps.isEmpty()) {
                return chipInps.get(0);
            }
        }
        return null;
    }

    private static double calcPpm(IntervalModel im, PolyState.Inp inp) {
        if (im != null && inp != null) {
            return im.evalMaxPpm(inp);
        }
        return Double.NaN;
    }

}
