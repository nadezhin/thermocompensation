package ru.nsc.interval.thermocompensation.intopt;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.java.jinterval.interval.set.SetInterval;
import net.java.jinterval.interval.set.SetIntervalContext;
import net.java.jinterval.interval.set.SetIntervalContexts;
import ru.nsc.interval.thermocompensation.model.AdcRange;
import ru.nsc.interval.thermocompensation.model.ParseTestInps;
import ru.nsc.interval.thermocompensation.model.ParseTestInps.ExtendedInp;
import ru.nsc.interval.thermocompensation.model.PolyState;
import ru.nsc.interval.thermocompensation.optim.Optim;
import ru.nsc.interval.thermocompensation.show.ChipShow;

public class Application {

    static SetIntervalContext ic = null;
    static double eps = 1e-5;
    static boolean gnuplot = false;

    private static void doChip(String plotDirName, IntervalPolyModel ipm, int chipNo,
            Map<IntervalPolyModel, List<IntervalModel>> models) throws IOException, InterruptedException {
        IntervalModel chip = models.get(ipm).get(chipNo);
        System.out.println("Chip " + (chipNo + 1));

        // Interval optimization
        ThermOpt program = new ThermOpt(chip, eps, ic);
        int[] result = program.startOptimization();
        PolyState.Inp intervalInp = chip.pointAsInp(result);

        // Heuristic optimization
        ByteArrayOutputStream ba = new ByteArrayOutputStream();
        PrintWriter out = new PrintWriter(ba);
        Optim.Record record = Optim.optimF(out, chip.thermoFreqModel,
                chip.getCC(), chip.getCF(), new AdcRange(0, 4095), chip.getF0());
        PolyState.Inp heuristicInp = record.inp;
        System.out.println(record.inp.toNom() + " +-" + record.bestDiff / record.targetF * 1e6);
        out.close();
        ba.close();

        // Plot and print
        if (gnuplot) {
            showChip(plotDirName, chipNo, models, heuristicInp, intervalInp);
        }
        for (IntervalPolyModel m : IntervalPolyModel.values()) {
            System.out.println(m.getAbbrev() + " interval : " + models.get(m).get(chipNo).evalMaxPpm(intervalInp));
        }
        for (IntervalPolyModel m : IntervalPolyModel.values()) {
            System.out.println(m.getAbbrev() + " heuristic: " + models.get(m).get(chipNo).evalMaxPpm(heuristicInp));
        }
        System.out.println("-------");
    }

    private static void showChip(String plotDirName, int chipNo,
            Map<IntervalPolyModel, List<IntervalModel>> models,
            PolyState.Inp heuristicInp, PolyState.Inp intervalInp) throws IOException, InterruptedException {
        String plotName = "plot";
        String chipName = "" + (chipNo + 1);

        File plotDir = new File(plotDirName);
        plotDir.mkdir();
        ChipShow chipShow = new ChipShow(plotName, plotDir, true);
        chipShow.startPdf(
                chipName + "ppm",
                "Отклонение компенсированной частоты " + chipName,
                "Датчик температуры",
                "Отклонение компенсированной частоты (ppm)",
                "%.1f");
        for (IntervalPolyModel ipm : IntervalPolyModel.values()) {
            if (ipm == IntervalPolyModel.SPECIFIED) {
                continue;
            }
            IntervalModel model = models.get(ipm).get(chipNo);
            showModelInpIntervalPpm(chipShow, ipm.getAbbrev() + " interval  " + intervalInp.toLongNom(), model, intervalInp);
        }
        for (IntervalPolyModel ipm : IntervalPolyModel.values()) {
            if (ipm == IntervalPolyModel.SPECIFIED) {
                continue;
            }
            IntervalModel model = models.get(ipm).get(chipNo);
            showModelInpIntervalPpm(chipShow, ipm.getAbbrev() + " heuristic " + heuristicInp.toLongNom(), model, heuristicInp);
        }
        chipShow.closePdf();
        chipShow.closeAndRunGnuplot();
    }

    private static void showModelInpIntervalPpm(ChipShow chipShow, String modelName, IntervalModel chipModel, PolyState.Inp inp) throws IOException {
        int[] temp = chipModel.getTemps();
        SetInterval[] ppm = chipModel.evalPpm(inp);
        double[] x = new double[temp.length];
        double[] lowerY = new double[temp.length];
        double[] upperY = new double[temp.length];
        for (int i = 0; i < temp.length; i++) {
            x[i] = temp[i];
            lowerY[i] = ppm[i].doubleInf();
            upperY[i] = ppm[i].doubleSup();
        }
        chipShow.withLines(x, lowerY, modelName + " снизу");
        chipShow.withLines(x, upperY, modelName + " сверху");
    }

    private static void help() {
        System.out.println("Usage: java -ea -Djna.library.path=../lib -jar intoptXXX.jar dir [-sN] [-eD] [-nN] [-g] [-p]");
        System.out.println("  -sN stage N   - N=1 or N=2");
        System.out.println("  -eD value of eps");
        System.out.println("  -nN only chip N");
        System.out.println("  -g gnuplot graphs");
        System.out.println("  -p  use plain context");
        System.out.println("examples:");
        System.out.println(" P");
        System.out.println(" 150616V15 -s1 -g0222222222222222222222222222222");
        System.out.println(" 150601V15 -s1 -e0.1 -p");
        System.exit(0);
    }

    public static void main(String[] args) throws Exception {
        int stage = 0;
        int chipNo = -1;
        List<String> argsList = new ArrayList<String>();
        for (String arg : args) {
            System.out.print(" " + arg);
            if (arg.startsWith("-")) {
                if (arg.startsWith("-s")) {
                    stage = Integer.parseInt(arg.substring(2));
                    if (stage < 1 || stage > 2) {
                        help();
                    }
                } else if (arg.startsWith("-e")) {
                    eps = Double.parseDouble(arg.substring(2));
                } else if (arg.startsWith("-n")) {
                    chipNo = Integer.parseInt(arg.substring(2)) - 1;
                    if (chipNo < 0 || chipNo >= 64) {
                        help();
                    }
                } else if (arg.startsWith("-g")) {
                    gnuplot = true;
                } else if (arg.startsWith("-p")) {
                    ic = SetIntervalContexts.getPlain();
                } else {
                    help();
                }
            } else {
                argsList.add(arg);
            }
        }
        System.out.println();
        if (argsList.size() != 1) {
            help();
        }
        if (ic == null) {
            ic = SetIntervalContexts.getDefault();
        }

        String dir = argsList.get(0);
        if (!dir.isEmpty() && !dir.endsWith("/")) {
            dir = dir + "/";
        }
        String plotDir;
        Map<IntervalPolyModel, List<IntervalModel>> allModels;
        List<List<ExtendedInp>> inps;
        switch (stage) {
            case 0:
                plotDir = dir + "Plot/";
                allModels = IntervalModel.readCsvModels(dir + "P.csv", ic);
                inps = null;
                break;
            case 1:
                plotDir = dir + "Plot1/";
                inps = ParseTestInps.parseLogExtendedInps(Paths.get(dir + "nom_inps.txt"));
                allModels = IntervalModel.readTF0Models(dir + "m1", inps, 12000000, ic);
                break;
            case 2:
                plotDir = dir + "Plot1/";
                inps = ParseTestInps.parseLogExtendedInps(Paths.get(dir + "o1.txt"));
                allModels = IntervalModel.readTF0Models(dir + "m2", inps, 12000000, ic);
                break;
            default:
                throw new AssertionError();
        }
        IntervalPolyModel ipm = IntervalPolyModel.IDEAL;
        List<IntervalModel> models = allModels.get(ipm);
        if (chipNo >= 0) {
            doChip(plotDir, ipm, chipNo, allModels);
        } else {
            for (chipNo = 0; chipNo < models.size(); chipNo++) {
                if (models.get(chipNo) == null) {
                    continue;
                }
                doChip(plotDir, ipm, chipNo, allModels);
            }
        }
    }
}
