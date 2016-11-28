package ru.nsc.interval.thermocompensation.intopt;

import net.java.jinterval.interval.set.SetInterval;
import net.java.jinterval.interval.set.SetIntervalContext;
import net.java.jinterval.interval.set.SetIntervalContexts;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import net.java.jinterval.expression.Expression;
import net.java.jinterval.interval.set.SetIntervalOps;
import net.java.jinterval.rational.ExtendedRational;
import net.java.jinterval.rational.ExtendedRationalContexts;
import net.java.jinterval.rational.ExtendedRationalOps;
import net.java.jinterval.rational.Rational;
import ru.nsc.interval.thermocompensation.model.ParseTestInps;
import ru.nsc.interval.thermocompensation.model.ParseTestInps.ExtendedInp;
import ru.nsc.interval.thermocompensation.model.PolyModel;
import ru.nsc.interval.thermocompensation.model.PolyState;
import ru.nsc.interval.thermocompensation.model.ChipPoints;

public class Application2 {

    static SetIntervalContext ic = SetIntervalContexts.getPlain();

    private static void doChip(int chipNo, ChipPoints[] data, List<List<ExtendedInp>> results, boolean print) {
        ChipPoints chipPoints = data[chipNo];
        double f0 = chipPoints.getF0();
        int cc = 0;
        int cf = 0;
        int[] adcOuts = chipPoints.getAdcOuts();
        PolyState.Inp heuristicInp = results.get(chipNo).get(0).inp;
        double[] temp = new double[adcOuts.length];
        double[] u = new double[adcOuts.length];
        for (int i = 0; i < adcOuts.length; i++) {
            int adcOut = adcOuts[i];
            temp[i] = adcOut;
            for (int dacInp = 0; dacInp < 4096; dacInp++) {
                double inf = chipPoints.getLowerModelFfromAdcOut(cc, cf, dacInp, adcOut);
                double sup = chipPoints.getUpperModelFfromAdcOut(cc, cf, dacInp, adcOut);
                assert inf == sup;
                if (inf == f0) {
                    u[i] = dacInp;
                }
                if (sup == f0) {
                    u[i] = dacInp;
                }
            }
        }

        SetInterval INFBIT = ic.numsToInterval(0, 63);
        SetInterval SBIT = ic.numsToInterval(0, 31);
        SetInterval K1BIT = ic.numsToInterval(1, 255);
        SetInterval K2BIT = ic.numsToInterval(0, 127);
        SetInterval K3BIT = ic.numsToInterval(0, 31);
        SetInterval K4BIT = ic.numsToInterval(0, 31);
        SetInterval K5BIT = ic.numsToInterval(0, 15);

        SetInterval[] box = new SetInterval[]{INFBIT, SBIT, K1BIT, K2BIT, K3BIT, K4BIT, K5BIT};

        double eps = 1E-5;

        ThermOpt program = new ThermOpt(box, temp, u, eps, ic);
        int[] result = program.startOptimization();
        PolyState.Inp intervalInp = PolyState.Inp.genNom();
        intervalInp.INF = result[0];
        intervalInp.SBIT = result[1];
        intervalInp.K1BIT = result[2];
        intervalInp.K2BIT = result[3];
        intervalInp.K3BIT = result[4];
        intervalInp.K4BIT = result[5];
        intervalInp.K5BIT = result[6];
        ExtendedRational[] args = new ExtendedRational[result.length + 1];
        for (int i = 0; i < result.length; i++) {
            args[i] = Rational.valueOf(result[i]);
        }

        Expression expr = Functions.getObjective();

        double maxHeuristic = 0;
        double maxInterval = 0;
        double maxIdeal = 0;
        SetInterval diffBounds = SetIntervalOps.empty();
        for (int adcOut: adcOuts) {
//            int required = meas.dacInp;
            heuristicInp.T = intervalInp.T = adcOut;
            args[result.length] = Rational.valueOf(adcOut);
            int heuristicDacOut = PolyModel.compute(heuristicInp);
            double heuristicFInf = chipPoints.getLowerModelFfromAdcOut(cc, cf, heuristicDacOut, adcOut);
            double heuristicFSup = chipPoints.getUpperModelFfromAdcOut(cc, cf, heuristicDacOut, adcOut);
            int intervalDacOut = PolyModel.compute(intervalInp);
            double intervalFInf = chipPoints.getLowerModelFfromAdcOut(cc, cf, intervalDacOut, adcOut);
            double intervalFSup = chipPoints.getUpperModelFfromAdcOut(cc, cf, intervalDacOut, adcOut);
            ExtendedRational idealDacOut = ExtendedRationalContexts.evaluateRational(
                    ExtendedRationalContexts.exact(),
                    expr.getCodeList(),
                    args,
                    expr)[0];
            double idealFInf = chipPoints.getLowerModelFfromAdcOut(cc, cf, idealDacOut.doubleValue(), adcOut);
            double idealFSup = chipPoints.getUpperModelFfromAdcOut(cc, cf, idealDacOut.doubleValue(), adcOut);
            double heuristicErr = Math.max(Math.abs(heuristicFInf - f0), Math.abs(heuristicFSup - f0));
            double intervalErr = Math.max(Math.abs(intervalFInf - f0), Math.abs(intervalFSup - f0));
            double idealErr = Math.max(Math.abs(idealFInf - f0), Math.abs(idealFSup - f0));
            ExtendedRational diff = ExtendedRationalOps.sub(idealDacOut, Rational.valueOf(intervalDacOut));
            maxHeuristic = Math.max(maxHeuristic, Math.abs(heuristicErr));
            maxInterval = Math.max(maxInterval, Math.abs(intervalErr));
            maxIdeal = Math.max(maxIdeal, Math.abs(idealErr));
            diffBounds = SetIntervalOps.convexHull(diffBounds, SetIntervalOps.nums2(diff, diff));
            if (print) {
                System.out.println("  temp=" + adcOut
                        + "\theuristic=[" + (heuristicFInf - f0) + "," + (heuristicFSup - f0) + "]"
                        + " \tinterval=[" + (intervalFInf - f0) + "," + (intervalFSup - f0) + "]"
                        + "\tideal=[" + (idealFInf - f0) + "," + (idealFSup - f0) + ")"
                        + "\tdiff=" + diff.doubleValue());
            }
        }
        System.out.println("Chip " + (chipNo + 1) + " f0=" + f0);
        System.out.println("heuristic: " + heuristicInp.toLongNom() + "\t" + maxHeuristic);
        System.out.println("interval:  " + intervalInp.toLongNom() + "\t" + maxInterval);
        System.out.println("ideal:     " + intervalInp.toLongNom() + "\t" + maxIdeal);
        System.out.println("diffBounds: [" + diffBounds.doubleInf() + "," + diffBounds.doubleSup() + "]");
        System.out.println("-------");
    }

    public static void main(String[] args) throws IOException {
        String name = "P";
        ChipPoints[] data = ChipPoints.readChipPoints(new File(name + ".csv"));
        List<List<ExtendedInp>> results = ParseTestInps.parseLogExtendedInps(Paths.get(name + ".opt"));
        if (false) {
            doChip(3, data, results, true);
        } else {
            for (int chipNo = 0; chipNo < data.length; chipNo++) {
                if (chipNo >= data.length || data[chipNo] == null || chipNo >= results.size()) {
                    continue;
                }
                doChip(chipNo, data, results, true);
            }
        }
    }
}
