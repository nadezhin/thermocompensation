package ru.nsc.interval.thermocompensation.intopt;

import net.java.jinterval.interval.set.SetInterval;
import net.java.jinterval.interval.set.SetIntervalContext;
import net.java.jinterval.interval.set.SetIntervalContexts;
import ru.nsc.interval.thermocompensation.model.AdcRange;
import ru.nsc.interval.thermocompensation.model.ChipModel;
import ru.nsc.interval.thermocompensation.model.ParseTestInps;
import ru.nsc.interval.thermocompensation.model.ParseTestInps.ExtendedInp;
import ru.nsc.interval.thermocompensation.model.PolyState;
import ru.nsc.interval.thermocompensation.optim.Optim;
import ru.nsc.interval.thermocompensation.show.ChipShow;

import java.io.PrintWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Application {

    static SetIntervalContext ic = null;
    static double eps = 1e-5;
    static boolean gnuplot = false;
    static int ADC_MAX = 4095;
    static int ADC_MIN = 0;

    private static void printChip(IntervalPolyModel ipm, int chipNo, Map<IntervalPolyModel, List<IntervalModel>> allModels)
            throws IOException, InterruptedException {
        IntervalModel chip = allModels.get(ipm).get(chipNo);
        int CC = chip.getCC();
        int CF = chip.getCF();
        double F0 = chip.getF0();
        ChipModel chipModel = chip.getThermoFreqModel();
        int[] DIG_TEMP = chipModel.getAdcOuts();
        double f_inf, f_sup, freqDifference;

        File outputData = new File("outputData");
        outputData.mkdir();

        Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(outputData.getName() + "/N_" + (chipNo + 1) + ".txt"), "utf-8"));

        for (int i = 0; i < DIG_TEMP.length; i++) {
            for (int dac = ADC_MIN; dac < ADC_MAX; dac++) {
                f_inf = chipModel.getLowerModelFfromAdcOut(CC, CF, dac, DIG_TEMP[i]);
                f_sup = chipModel.getUpperModelFfromAdcOut(CC, CF, dac, DIG_TEMP[i]);

                freqDifference = Math.max(Math.abs(f_sup - F0), Math.abs(f_inf - F0));
                writer.write(DIG_TEMP[i] + "; " + dac + "; " + freqDifference + ";\n");
            }
        }
        writer.close();
    }

    private static void doChip(String plotDirName, IntervalPolyModel ipm, int chipNo,
            Map<IntervalPolyModel, List<IntervalModel>> models) throws IOException, InterruptedException {
        long startTime;
        IntervalModel chip = models.get(ipm).get(chipNo);
        System.out.println("Chip " + (chipNo + 1) + " f0=" + chip.getF0() + " CC=" + chip.getCC() + " CF=" + chip.getCF());

        // Interval optimization
        startTime = System.currentTimeMillis();
        ThermOpt program = new ThermOpt(chip, eps, ic);
        int[] result = program.startOptimization();
        PolyState.Inp intervalInp = chip.pointAsInp(result);
        System.out.println(((System.currentTimeMillis() - startTime + 999) / 1000) + " sec");

        // Heuristic optimization
        startTime = System.currentTimeMillis();
        ByteArrayOutputStream ba = new ByteArrayOutputStream();
        PrintWriter out = new PrintWriter(ba);
        Optim.Record record = Optim.optimF(out, chip.getThermoFreqModel(),
                chip.getCC(), chip.getCF(), new AdcRange(ADC_MIN, ADC_MAX), chip.getF0());
        PolyState.Inp heuristicInp = record.inp;
        System.out.println(record.inp.toNom() + " +-" + record.bestDiff / record.targetF * 1e6);
        out.close();
        ba.close();
        System.out.println(((System.currentTimeMillis() - startTime + 999) / 1000) + " sec");

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
            if (ipm != IntervalPolyModel.MANUFACTURED) {
                continue;
            }
            IntervalModel model = models.get(ipm).get(chipNo);
            showModelInpIntervalPpm(chipShow, ipm.getAbbrev() + " interval  ", model, intervalInp);
        }
        for (IntervalPolyModel ipm : IntervalPolyModel.values()) {
            if (ipm != IntervalPolyModel.MANUFACTURED) {
                continue;
            }
            IntervalModel model = models.get(ipm).get(chipNo);
            showModelInpIntervalPpm(chipShow, ipm.getAbbrev() + " heuristic ", model, heuristicInp);
        }
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
        System.out.println("Usage: java -ea -Djna.library.path=../lib -jar intoptXXX.jar dir [-print] [-sN] [-ideal] [-eD] [-nN] [-g] [-p]");
        System.out.println("  -print - prints table of (T, u, max(|f_inf-f0|, |f_sup-f0|)) for all chips if no chip number specified");
        System.out.println("  -sN stage N   - N=1 or N=2");
        System.out.println("  -ideal  IDEAL model");
        System.out.println("  -spec   SPECIFIED model");
        System.out.println("  -manuf  MANUFACTURED model");
        System.out.println("  -eD value of eps");
        System.out.println("  -nN only chip N");
        System.out.println("  -g gnuplot graphs");
        System.out.println("  -p  use plain context");
        System.out.println("examples:");
        System.out.println(" P");
        System.out.println(" 150616V15 -s1 -g");
        System.out.println(" 150601V15 -s1 -e0.1 -p");
        System.exit(0);
    }

    public static void main(String[] args) throws Exception {
        IntervalPolyModel ipm = IntervalPolyModel.SPECIFIED;
        int stage = 0;
        int chipNo = -1;
        boolean printOnly = false;
        List<String> argsList = new ArrayList<String>();
        for (String arg : args) {
            System.out.print(" " + arg);
            if (arg.startsWith("-")) {
                if (arg.equals("-print")) {
                    printOnly = true;
                } else if (arg.equals("-ideal")) {
                    ipm = IntervalPolyModel.IDEAL;
                } else if (arg.equals("-spec")) {
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
        List<List<ExtendedInp>> inps, tasks;
        switch (stage) {
            case 0:
                plotDir = dir + "Plot/";
                allModels = IntervalModel.readCsvModels(dir + "P.csv", ic);
                inps = null;
                break;
            case 1:
                plotDir = dir + "Plot1/";
                inps = ParseTestInps.parseLogExtendedInps(Paths.get(dir + "nom_inps.txt"));
                tasks = ParseTestInps.parseLogExtendedInps(Paths.get(dir + "o1.txt"));
                allModels = IntervalModel.readTF0Models(dir + "m1", inps, tasks, ic);
                break;
            case 2:
                plotDir = dir + "Plot2/";
                inps = tasks = ParseTestInps.parseLogExtendedInps(Paths.get(dir + "o1.txt"));
                allModels = IntervalModel.readTF0Models(dir + "m2", inps, tasks, ic);
                break;
            default:
                throw new AssertionError();
        }
        List<IntervalModel> models = allModels.get(ipm);
        if (chipNo >= 0) {
            if (printOnly) {
                printChip(ipm, chipNo, allModels);
            } else {
                doChip(plotDir, ipm, chipNo, allModels);
            }
        } else {
            for (chipNo = 0; chipNo < models.size(); chipNo++) {
                if (models.get(chipNo) == null) {
                    continue;
                }
                if (printOnly) {
                    printChip(ipm, chipNo, allModels);
                } else {
                    doChip(plotDir, ipm, chipNo, allModels);
                }
            }
        }
    }
}
