package ru.nsc.interval.thermocompensation.intopt;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import net.java.jinterval.interval.set.SetInterval;
import net.java.jinterval.interval.set.SetIntervalContext;
import net.java.jinterval.interval.set.SetIntervalContexts;
import ru.nsc.interval.thermocompensation.model.AdcRange;
import ru.nsc.interval.thermocompensation.model.ChipMin;
import ru.nsc.interval.thermocompensation.model.ChipModel;
import ru.nsc.interval.thermocompensation.model.ParseTestInps;
import ru.nsc.interval.thermocompensation.model.ParseTestInps.ExtendedInp;
import ru.nsc.interval.thermocompensation.model.PolyState;
import ru.nsc.interval.thermocompensation.optim.Optim;
import ru.nsc.interval.thermocompensation.optim.OptimMin;
import ru.nsc.interval.thermocompensation.show.ChipShow;

public class Application2 {

    static SetIntervalContext ic = null;
    static double eps = 1e-5;
    static boolean gnuplot = false;

    private static void doChip(String plotDirName, IntervalPolyModel ipm, int chipNo,
            Map<IntervalPolyModel, List<IntervalModel>> models, List<List<ExtendedInp>> results) throws IOException, InterruptedException {
        long startTime;
        final IntervalModel chip = models.get(ipm).get(chipNo);
        System.out.println("Chip " + (chipNo + 1) + " f0=" + chip.getF0() + " CC=" + chip.getCC() + " CF=" + chip.getCF());

        // Heuristic optimization
        startTime = System.currentTimeMillis();
        ByteArrayOutputStream ba = new ByteArrayOutputStream();
        PrintWriter out = new PrintWriter(ba);
        Optim.Record record = Optim.optimF(out, chip.getThermoFreqModel(),
                chip.getCC(), chip.getCF(), new AdcRange(0, 4095), chip.getF0());
        PolyState.Inp heuristicInp = record.inp;
        System.out.println(record.inp.toNom() + " +-" + chip.evalMaxPpm(heuristicInp));
        out.close();
        ba.close();
        System.out.println(((System.currentTimeMillis() - startTime + 999) / 1000) + " sec");

        // Combined interval optimizations
        startTime = System.currentTimeMillis();
        PolyState.Inp combinedInp = checkOptim(chip, record);
        System.out.println(((System.currentTimeMillis() - startTime + 999) / 1000) + " sec");

        for (IntervalPolyModel m : IntervalPolyModel.values()) {
            if (m == IntervalPolyModel.IDEAL) {
                continue;
            }
            System.out.println(m.getAbbrev() + " heuristic: " + models.get(m).get(chipNo).evalMaxPpm(heuristicInp));
        }
        for (IntervalPolyModel m : IntervalPolyModel.values()) {
            if (m == IntervalPolyModel.IDEAL) {
                continue;
            }
            System.out.println(m.getAbbrev() + " enhanced:  " + models.get(m).get(chipNo).evalMaxPpm(combinedInp));
        }
        // Plot and print
        if (gnuplot) {
            showChip(plotDirName, chipNo, models.get(ipm), heuristicInp, combinedInp);
        }

        // Interval optimization
//        startTime = System.currentTimeMillis();
//        ThermOpt program = new ThermOpt(chip, eps, ic);
//        int[] result = program.startOptimization();
//        PolyState.Inp intervalInp = chip.pointAsInp(result);
//        System.out.println(((System.currentTimeMillis() - startTime + 999) / 1000) + " sec");
//
//        for (IntervalPolyModel m : IntervalPolyModel.values()) {
//            System.out.println(m.getAbbrev() + " interval : " + models.get(m).get(chipNo).evalMaxPpm(intervalInp));
//        }
        System.out.println("-------");
        double df = models.get(ipm).get(chipNo).evalMaxAbsDF(combinedInp).doubleSup();
        ExtendedInp result = new ExtendedInp(combinedInp, chip.getF0(), df);
        while (results.size() <= chipNo) {
            results.add(null);
        }
        results.set(chipNo, Arrays.asList(result));
    }

    private static PolyState.Inp checkOptim(IntervalModel im, Optim.Record record) {
        boolean fixBugP = im.getPolyModel() != IntervalPolyModel.MANUFACTURED;
        ChipModel chip = im.getThermoFreqModel();
        double targetF = im.getF0();
        int cc = im.getCC();
        int cf = im.getCF();
        ChipMin chipMin = new ChipMin(chip, targetF, cc, cf);
        int numAdcOuts = chipMin.numAdcOuts;
        int[] l = new int[numAdcOuts];
        int[] u = new int[numAdcOuts];
        PolyState.Inp recordInp = record.inp;
        double bestDiff = im.evalMaxAbsDF(recordInp).doubleSup();
        if (bestDiff <= chipMin.possibleDF) {
            return recordInp;
        }
        chipMin.getBoundsStrong(bestDiff, l, u);
//        System.out.println("ppm=" + im.evalMaxPpm(recordInp));
        for (int infbit = 0; infbit <= 63; infbit++) {
            for (int sbit = 0; sbit <= 31; sbit++) {
//                System.out.println("====================================== [" + infbit + "," + sbit + "] ");
                OptimMin optimMin = new OptimMin(chip.getAdcOuts(), fixBugP, infbit, sbit);
                double[] coeff = optimMin.optim(l, u);
                if (coeff != null) {
                    BitSet bs = new BitSet(5);
                    bs.set(0, 5);
//                    System.out.print("[" + infbit + "," + sbit + "] ");
                    for (;;) {
                        PolyState.Inp inp = explore(optimMin, l, u, bs, im, bestDiff);
                        if (inp == null) {
                            break;
                        }
                        double newDiff = im.evalMaxAbsDF(inp).doubleSup();
                        if (newDiff < bestDiff) {
                            recordInp = inp;
                            bestDiff = newDiff;
                            System.out.println(inp.toNom() + " +-" + im.evalMaxPpm(inp));
//                            System.out.println("ppm=" + im.evalMaxPpm(recordInp));
                            if (bestDiff <= chipMin.possibleDF) {
                                return recordInp;
                            }
                            chipMin.getBoundsStrong(bestDiff, l, u);
                        }
                    }
//                    ThermOpt2 program = new ThermOpt2(im, infbit, sbit, eps, ic);
//                    program.updateRecord(recordInp);
//                    int[] result = program.startOptimization();
//                    PolyState.Inp inp = im.pointAsInp(result);
//                    double newDiff = im.evalMaxAbsDF(inp).doubleSup();
//                    if (newDiff < bestDiff) {
//                        recordInp = inp;
//                        bestDiff = newDiff;
//                        System.out.println("ppm=" + im.evalMaxPpm(recordInp));
//                        if (bestDiff <= chipMin.possibleDF) {
//                            return recordInp;
//                        }
//                        chipMin.getBoundsStrong(bestDiff, l, u);
//                    }
                }
            }
        }
        return recordInp;
    }

    private static PolyState.Inp explore(OptimMin optimMin, int[] l, int[] u, BitSet iterateVars,
            IntervalModel im, double bestDiff) {
        double[] coeff = optimMin.optim(l, u);
        if (coeff == null) {
            return null;
        }
//        for (int i = iterateVars.cardinality(); i < 5; i++) {
//            System.out.print(' ');
//        }
//        for (int i = 0; i < coeff.length; i++) {
//            System.out.print(" " + coeff[i]);
//        }
//        System.out.println();
        if (iterateVars.isEmpty()) {
            PolyState.Inp inp = PolyState.Inp.genNom();
            assert optimMin.kl[0] == optimMin.ku[0];
            assert optimMin.kl[1] == optimMin.ku[1];
            assert optimMin.kl[2] == optimMin.ku[2];
            assert optimMin.kl[3] == optimMin.ku[3];
            assert optimMin.kl[4] == optimMin.ku[4];
            inp.INF = optimMin.infbit;
            inp.SBIT = optimMin.sbit;
            inp.K1BIT = optimMin.kl[0];
            inp.K2BIT = optimMin.kl[1];
            inp.K3BIT = optimMin.kl[2];
            inp.K4BIT = optimMin.kl[3];
            inp.K5BIT = optimMin.kl[4];
            if (im.evalMaxAbsDF(inp).doubleSup() < bestDiff) {
//                System.out.println("------- HERE ----------");
                return inp;
            } else {
                return null;
            }
        }
        int[] minK = new int[5];
        int[] maxK = new int[5];
        for (int i = 0; i < 5; i++) {
            if (!iterateVars.get(i)) {
                assert optimMin.kl[i] == optimMin.ku[i];
                minK[i] = optimMin.kl[i];
                maxK[i] = optimMin.ku[i];
                continue;
            }
            minK[i] = Integer.MAX_VALUE;
            maxK[i] = Integer.MIN_VALUE;

            int fi = Math.max(optimMin.kl[i], (int) Math.floor(coeff[i]));
            int saveU = optimMin.ku[i];
            for (int k = fi; k >= optimMin.kl[i]; k--) {
                optimMin.ku[i] = k;
                double[] coeff1 = optimMin.optim(l, u);
                if (coeff1 == null) {
                    break;
                }
                minK[i] = Math.min(minK[i], k);
                maxK[i] = Math.max(maxK[i], k);
//                System.out.print("k" + (i + 1) + "bit<=" + k);
//                for (int j = 0; j < coeff1.length; j++) {
//                    System.out.print(" " + coeff1[j]);
//                }
//                System.out.println();
            }
            optimMin.ku[i] = saveU;

            int ci = Math.min(optimMin.ku[i], (int) Math.ceil(coeff[i]));
            int saveL = optimMin.kl[i];
            for (int k = ci; k <= optimMin.ku[i]; k++) {
                optimMin.kl[i] = k;
                double[] coeff1 = optimMin.optim(l, u);
                if (coeff1 == null) {
                    break;
                }
                minK[i] = Math.min(minK[i], k);
                maxK[i] = Math.max(maxK[i], k);
//                System.out.print("k" + (i + 1) + "bit>=" + k);
//                for (int j = 0; j < coeff1.length; j++) {
//                    System.out.print(" " + coeff1[j]);
//                }
//                System.out.println();
            }
            optimMin.kl[i] = saveL;
        }
        boolean isEmpty = false;
        int minI = -1;
        int minW = Integer.MAX_VALUE;
//        for (int i = iterateVars.cardinality(); i < 5; i++) {
//            System.out.print(' ');
//        }
        for (int i = 0; i < 5; i++) {
            if (minK[i] <= maxK[i]) {
//                System.out.print(minK[i] <= maxK[i] ? " [" + minK[i] + "," + maxK[i] + "]" : "[]");
                if (iterateVars.get(i) && maxK[i] - minK[i] <= minW) {
                    minI = i;
                    minW = maxK[i] - minK[i];
                }
            } else {
//                System.out.print(" []");
                isEmpty = true;
            }
        }
//        System.out.println();
        if (isEmpty) {
            return null;
        }
        assert iterateVars.get(minI);
        iterateVars.clear(minI);
        int saveL = optimMin.kl[minI];
        int saveU = optimMin.ku[minI];
        for (int k = minK[minI]; k <= maxK[minI]; k++) {
            optimMin.kl[minI] = k;
            optimMin.ku[minI] = k;
            PolyState.Inp inp = explore(optimMin, l, u, iterateVars, im, bestDiff);
            if (inp != null) {
                iterateVars.set(minI);
                optimMin.kl[minI] = saveL;
                optimMin.ku[minI] = saveU;
                return inp;
            }
        }
        iterateVars.set(minI);
        optimMin.kl[minI] = saveL;
        optimMin.ku[minI] = saveU;
        return null;
    }

    private static void showChip(String plotDirName, int chipNo,
            List<IntervalModel> models,
            PolyState.Inp heuristicInp, PolyState.Inp combinedInp) throws IOException, InterruptedException {
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
        IntervalModel model = models.get(chipNo);
        String abbrev = model.getPolyModel().getAbbrev();
        showModelInpIntervalPpm(chipShow, abbrev + " enhanced  ", model, combinedInp);
        showModelInpIntervalPpm(chipShow, abbrev + " heuristic ", model, heuristicInp);
        chipShow.closePdf();
        chipShow.closeAndRunGnuplot();
    }

    private static void showModelInpIntervalPpm(ChipShow chipShow, String modelName, IntervalModel chipModel, PolyState.Inp inp) throws IOException {
        int[] temp = chipModel.getTemps();
        SetInterval[] ppm = chipModel.evalPpm(inp, temp);
        double[] x = new double[temp.length];
        double[] lowerY = new double[temp.length];
        double[] upperY = new double[temp.length];
        for (int i = 0; i < temp.length; i++) {
            x[i] = temp[i];
            lowerY[i] = ppm[i].doubleInf();
            upperY[i] = ppm[i].doubleSup();
        }
        modelName += " " + inp.toNom();
        modelName += " " + Math.ceil(chipModel.evalMaxPpm(inp) * 100) / 100 + "ppm";
        chipShow.withLines(x, lowerY, modelName + " снизу");
        chipShow.withLines(x, upperY, modelName + " сверху");
    }

    private static void help() {
        System.out.println("Usage: java -ea -Djna.library.path=../lib -jar intoptXXX.jar dir [-sN] [-ideal] [-eD] [-nN] [-g] [-p]");
        System.out.println("  -sN stage N   - N=1 or N=2");
//        System.out.println("  -ideal  IDEAL model");
        System.out.println("  -spec   SPECIFIED model");
        System.out.println("  -manuf  MANUFACTURED model");
        System.out.println("  -eD value of eps");
        System.out.println("  -nN only chip N");
        System.out.println("  -g gnuplot graphs");
        System.out.println("  -p use plain context");
        System.out.println("examples:");
        System.out.println(" P");
        System.out.println(" 150616V15 -s1 -g");
        System.out.println(" 150601V15 -s1 -e0.1 -p");
        System.exit(0);
    }

    public static void main(String[] args) throws Exception {
        IntervalPolyModel ipm = null;
        int stage = 0;
        int chipNo = -1;
        List<String> argsList = new ArrayList<>();
        for (String arg : args) {
            System.out.print(" " + arg);
            if (arg.startsWith("-")) {
//                if (arg.equals("-ideal")) {
//                    ipm = IntervalPolyModel.IDEAL;
                if (arg.equals("-spec")) {
                    ipm = IntervalPolyModel.SPECIFIED;
                } else if (arg.equals("-manuf")) {
                    ipm = IntervalPolyModel.MANUFACTURED;
                } else if (arg.startsWith("-s")) {
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
        if (ipm == null || argsList.size() != 1) {
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
        String resultFile = "olp" + ipm.getAbbrev().toLowerCase().charAt(0);
        // "olps" - optimization by linear programming with SPECIFIED model
        // "olpm" - optimization by linear programming with MANUFACTURED model
        Map<IntervalPolyModel, List<IntervalModel>> allModels;
        List<List<ExtendedInp>> inps, tasks;
        switch (stage) {
            case 0:
                plotDir = dir + "Plot/";
                resultFile = dir + resultFile + ".txt";
                allModels = IntervalModel.readCsvModels(dir + "P.csv", ic);
                inps = null;
                break;
            case 1:
                plotDir = dir + "Plot1/";
                resultFile = dir + resultFile + "1.txt";
                inps = ParseTestInps.parseLogExtendedInps(Paths.get(dir + "nom_inps.txt"));
                tasks = ParseTestInps.parseLogExtendedInps(Paths.get(dir + "o1.txt"));
                allModels = IntervalModel.readTF0Models(dir + "m1", inps, tasks, ic);
                break;
            case 2:
                plotDir = dir + "Plot2/";
                resultFile = dir + resultFile + "2.txt";
                inps = tasks = ParseTestInps.parseLogExtendedInps(Paths.get(dir + "o1.txt"));
                allModels = IntervalModel.readTF0Models(dir + "m2", inps, tasks, ic);
                break;
            default:
                throw new AssertionError();
        }
        List<IntervalModel> models = allModels.get(ipm);
        List<List<ExtendedInp>> results = new ArrayList<>();
        if (chipNo >= 0) {
            doChip(plotDir, ipm, chipNo, allModels, results);
        } else {
            for (chipNo = 0; chipNo < models.size(); chipNo++) {
                if (models.get(chipNo) == null) {
                    continue;
                }
                doChip(plotDir, ipm, chipNo, allModels, results);
            }
        }
        ParseTestInps.writeTestInps(new File(resultFile), results);
    }
}
