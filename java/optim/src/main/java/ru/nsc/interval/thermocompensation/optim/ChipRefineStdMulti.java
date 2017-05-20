package ru.nsc.interval.thermocompensation.optim;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import ru.nsc.interval.thermocompensation.model.AdcRange;
import ru.nsc.interval.thermocompensation.model.ChipExpress;
import ru.nsc.interval.thermocompensation.model.ChipRefine;
import ru.nsc.interval.thermocompensation.model.ChipT;
import ru.nsc.interval.thermocompensation.parse.CapSettings;
import ru.nsc.interval.thermocompensation.parse.Parse;
import ru.nsc.interval.thermocompensation.parse.ParseSeq;
import ru.nsc.interval.thermocompensation.parse.ParseTemp;
import ru.nsc.interval.thermocompensation.model.ParseTestInps;
import ru.nsc.interval.thermocompensation.model.PolyState;

/**
 *
 */
public class ChipRefineStdMulti {

    public static class OptimResult {

        int chipNo;
        Reader listing;
        List<Optim.Record> optimResults;

        OptimResult(int chipNo, ByteArrayOutputStream outStream, List<Optim.Record> optimResults) {
            this.chipNo = chipNo;
            listing = new InputStreamReader(new ByteArrayInputStream(outStream.toByteArray()));
            this.optimResults = optimResults;
        }
    }

    public static class OptimTask implements Callable<OptimResult> {

        public final int chipNo;
        public final ChipT chipT;
        public final ChipExpress baseChipE;
        public final List<ParseTestInps.ExtendedInp> inpsList;
        public final ChipRefine chipr;
        public final AdcRange adcRange;
        double minT;
        double maxT;
        double freq;
        int cc;
        double[] relDiff;

        OptimTask(int chipNo, ChipT chipT, List<ParseTestInps.ExtendedInp> inpsList,
                double minT, double maxT,
                double freq, int cc, double[] relDiff) {
            this.chipNo = chipNo;
            this.chipT = chipT;
            baseChipE = new ChipExpress(chipT);
            this.inpsList = inpsList;
            this.minT = minT;
            this.maxT = maxT;
            this.freq = freq;
            if (cc < 0 || cc >= 16) {
                throw new IllegalArgumentException();
            }
            this.cc = cc;
            this.relDiff = relDiff;
            assert inpsList.size() == 1;
            ParseTestInps.ExtendedInp einp = inpsList.get(0);
            PolyState.Inp inp = einp.inp;
            chipr = new ChipRefine(baseChipE, chipT, inp);
            adcRange = chipr.newAdcRange(minT, maxT);
        }

        @Override
        public OptimResult call() throws Exception {
            ByteArrayOutputStream ba = new ByteArrayOutputStream();
            List<Optim.Record> log;
            try (PrintWriter out = new PrintWriter(ba)) {
                out.println("============= Chip " + (chipNo + 1));
                out.println("Started " + new Date() + " in thread " + Thread.currentThread().getName());
                if (!Double.isNaN(freq)) {
                    log = Arrays.asList(Optim.optimCC(out, chipr, adcRange, freq));
                } else {
                    log = Optim.optimFromStd(out, chipr, cc, adcRange, relDiff);
                }
                Optim.Record best = null;
                for (Optim.Record rec : log) {
                    if (best == null || rec.bestDiff < best.bestDiff) {
                        best = rec;
                    }
                }
                for (Optim.Record rec : log) {
                    String cmt = rec == best ? "" : "#";
                    out.println(cmt + (chipNo + 1) + ": " + rec);
                }
                out.println("Finished " + new Date() + " in thread " + Thread.currentThread().getName());
            }
            return new OptimResult(chipNo, ba, log);
        }
    }

    public static List<OptimTask> prepareTasks(ChipT[] chips, List<List<ParseTestInps.ExtendedInp>> inpsLists,
            double minT, double maxT, double freq, int cc, double[] relDiff) {
        List<OptimTask> tasks = new ArrayList<>();
        for (int chipNo = 0; chipNo < 64; chipNo++) {
            ChipT chipT = chips[chipNo];
            List<ParseTestInps.ExtendedInp> inpsList = Collections.emptyList();
            if (chipNo < inpsLists.size()) {
                inpsList = inpsLists.get(chipNo);
            }
            if (chipT == null || inpsList.isEmpty()) {
                System.out.print('.');
            } else if (chipT.badFreq) {
                System.out.print('?');
            } else {
                OptimTask task;
                try {
                    task = new OptimTask(chipNo, chipT, inpsList, minT, maxT, freq, cc, relDiff);
                } catch (Exception e) {
                    System.out.printf("!");
                    continue;
                }
                if (task.baseChipE.isMonotonic0()) {
                    tasks.add(task);
                    System.out.print('+');
                } else {
                    System.out.print('X');
                }
            }
        }
        System.out.println();
        return tasks;
    }

    public static Collection<OptimResult> optimMultithreaded(List<OptimTask> tasks) {
        int numProcessors = Runtime.getRuntime().availableProcessors();
        ExecutorService executorService = Executors.newFixedThreadPool(numProcessors);
        CompletionService<OptimResult> ecs = new ExecutorCompletionService<>(executorService);
        System.out.println(tasks.size() + " optimization tasks on " + numProcessors + " threads");
        for (OptimTask task : tasks) {
            ecs.submit(task);
        }

        Collection<OptimResult> results = new ArrayList<>();
        for (int i = 0; i < tasks.size(); i++) {
            try {
                OptimResult r = ecs.take().get();
                if (r != null) {
                    int c;
                    while ((c = r.listing.read()) >= 0) {
                        System.out.print((char) c);
                    }
                    r.listing = null;
                    results.add(r);
                }
            } catch (InterruptedException | ExecutionException | IOException e) {
                e.printStackTrace();
            }
        }
        executorService.shutdown();
        return results;
    }

    private static void help() {
        System.out.println("Usage: [-tMINT,MAXT] <Base_T.txt> <Base_F.txt> <inps.txt> [-ccN] [-f<freq>] [-d<relDiff1>,<relDiffN>]");
        System.exit(0);
    }

    public static void main(String[] args) {
        System.out.println(System.getProperty("user.dir"));
        for (String arg : args) {
            System.out.println(arg);
        }

        int cc = 0;
        double minT = Double.NEGATIVE_INFINITY;
        double maxT = Double.POSITIVE_INFINITY;
        double freq = Double.NaN;
        double[] relDiff = {-2.5e-6, -2e-6, -1.5e-6, -1e-6, -0.5e-6, 0};
        List<String> argsList = new ArrayList<>();
        for (String arg : args) {
            if (arg.startsWith("-")) {
                if (arg.startsWith("-cc")) {
                    if (cc >= 0) {
                        help();
                    }
                    cc = Integer.parseInt(arg.substring(3));
                    if (cc < 0 || cc >= 16) {
                        help();
                    }
                } else if (arg.startsWith("-t")) {
                    String range = arg.substring(2);
                    int commaPos = range.indexOf(',');
                    if (commaPos < 0) {
                        help();
                    }
                    minT = Double.parseDouble(range.substring(0, commaPos));
                    maxT = Double.parseDouble(range.substring(commaPos + 1));
                } else if (arg.startsWith("-f")) {
                    freq = Double.parseDouble(arg.substring(2));
                } else if (arg.startsWith("-d")) {
                    String[] relDiffStr = arg.substring(2).split(",");
                    relDiff = new double[relDiffStr.length];
                    for (int i = 0; i < relDiff.length; i++) {
                        relDiff[i] = Double.parseDouble(relDiffStr[i]);
                    }
                } else {
                    help();
                }
            } else {
                argsList.add(arg);
            }
        }
        if (argsList.size() != 3) {
            help();
        }
        try {
            String runTime = "";
            Path pathT = Paths.get(argsList.get(0));
            Path pathF = Paths.get(argsList.get(1));
            ParseTemp parseTemp = new ParseTemp(pathT);
            Parse parse = new Parse(pathF.toFile());
            List<List<ParseTestInps.ExtendedInp>> inpsLists = ParseTestInps.parseTestExtendedInps(Paths.get(argsList.get(2)));

            ChipT[] chips = new ChipT[64];
            for (int chipNo = 0; chipNo < 64; chipNo++) {
                if (!parse.getMeasTN(chipNo).isEmpty()) {
                    String chipNoStr = String.format("%1$02d", chipNo + 1);
                    ChipT chip = new ChipT(chipNoStr, runTime, parseTemp, new ParseSeq(parse, chipNo), CapSettings.STD_F0);
                    System.out.print(chip.badFreq ? '?' : '+');
                    chips[chipNo] = chip;
                } else {
                    System.out.print('.');
                }
            }
            System.out.println();
            Collection<OptimResult> optimResults = optimMultithreaded(prepareTasks(chips, inpsLists, minT, maxT, freq, cc, relDiff));
            System.out.println("-------------");
            for (OptimResult or : optimResults) {
                System.out.println((or.chipNo + 1) + ": " + or.optimResults.get(0));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
