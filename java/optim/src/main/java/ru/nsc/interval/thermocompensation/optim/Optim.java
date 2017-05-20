package ru.nsc.interval.thermocompensation.optim;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import org.gnu.glpk.jna.GLPK;
import ru.nsc.interval.thermocompensation.model.PolyState;
import ru.nsc.interval.thermocompensation.model.PolyModel;

/**
 *
 */
public class Optim {

    private static final int k1bit = 1;
    private static final int k2bit = 2;
    private static final int k3bit = 3;
    private static final int k4bit = 4;
    private static final int k5bit = 5;
    private static final int opt = 6;
    //
    private final PrintWriter out;
    private final ChipModel chip;
    private final int cc;
    private final int cf;
    private GLPK.glp_prob lp = GLPK.glp_create_prob();
    private final GLPK.glp_smcp smcp = new GLPK.glp_smcp();
    private final GLPK.glp_iocp iocp = new GLPK.glp_iocp();
    private final int[] inds = new int[]{0, k1bit, k2bit, k3bit, k4bit, k5bit, opt};
    private final double[] matRow = new double[7];
    private final int minAdcOut;
    private final int maxAdcOut;
    private final int numPts;
    private final double minF;
    private final double maxF;
    private final double widF;
    private double targetF;
    private final int[] minDacInp = new int[4096];
    private final int[] maxDacInp = new int[4096];

    public static class Constraint {

        public final int adcOut;
        public final boolean upper;
        public final int dacInp;

        Constraint(int adcOut, boolean upper, int dacInp) {
            this.adcOut = adcOut;
            this.upper = upper;
            this.dacInp = dacInp;

        }
    }

    public static List<Record> optimFreqStep(PrintWriter out, final ChipModel chip, int cc, int cf, AdcRange adcRange, double freqStep, int extend) {
        out.println("minAdcOut=" + adcRange.minAdcOut + " maxAdcOut=" + adcRange.maxAdcOut);
        int[] adcOuts = getAdcOuts(chip, adcRange);
        Optim optim = new Optim(out, chip, cc, cf, adcRange);
        int minK, maxK;
        if (optim.minF >= optim.maxF) {
            minK = (int) Math.round(optim.maxF / freqStep) - extend;
            maxK = (int) Math.round(optim.minF / freqStep) + extend;
        } else {
            double targetF0 = (optim.minF + optim.maxF) * 0.5;
            minK = (int) Math.floor(targetF0 / freqStep) - extend;
            maxK = (int) Math.ceil(targetF0 / freqStep) + extend;
        }
        out.println("minOptF=" + minK * freqStep + " maxOptF=" + maxK * freqStep);

        List<Record> log = new ArrayList<>();
        for (int k = minK; k <= maxK; k++) {
            double targetF = freqStep * k;
            out.println("targetF=" + targetF);
            double deltaF = Math.max(optim.widF / 2, Math.max(targetF - optim.minF, optim.maxF - targetF));
            for (int i = 0; i < 10; i++) {
                double d = optim0optim(false, chip, cc, cf, adcOuts, targetF, deltaF);
                out.println("  " + deltaF + " -> " + d);
                deltaF += 0.5 * (d - deltaF);
            }
            Record rec = optim.optim(targetF, deltaF, null);
            if (rec != null) {
                log.add(rec);
            }
        }
//        optim0.free();
        optim.free();
        return log;
    }

    public static Record optimF(PrintWriter out, final ChipModel chip, int cc, int cf, AdcRange adcRange, double targetF) {
        int[] adcOuts = getAdcOuts(chip, adcRange);
        Optim optim = new Optim(out, chip, cc, cf, adcRange);

        out.println("cc=" + cc + " cf=" + cf + " targetF=" + targetF);
        double deltaF = Math.max(optim.widF / 2, Math.max(targetF - optim.minF, optim.maxF - targetF));
        for (int i = 0; i < 10; i++) {
            double d = optim0optim(false, chip, cc, cf, adcOuts, targetF, deltaF);
            out.println("  " + deltaF + " -> " + d);
            deltaF += 0.5 * (d - deltaF);
        }
//        optim0.free();
        Record rec = optim.optim(targetF, deltaF, null);
        optim.free();
        return rec;
    }

    public static Record optimTnom(PrintWriter out, final ChipModel chip, AdcRange adcRange, int nomAdcOut, double targetF) {
        int infbit = (int) Math.rint((nomAdcOut - 1535) / 8);
        infbit = Math.min(Math.max(infbit, 0), 63);
        int nomAdcOutCorrected = 1535 + 8 * infbit;
        double ccMin = 0;
        double ccMax = 15 + 63 * ChipModel.CF2CC;
        double fMin = getMeanModelF0(chip, ccMin, nomAdcOutCorrected);
        double fMax = getMeanModelF0(chip, ccMax, nomAdcOutCorrected);
        assert fMin > fMax;
        if (targetF >= fMin) {
            ccMax = ccMin;
        } else if (targetF <= fMax) {
            ccMin = ccMax;
        } else {
            for (int i = 0; i < 20; i++) {
                double cc = 0.5 * (ccMin + ccMax);
                double f = getMeanModelF0(chip, cc, nomAdcOutCorrected);
                if (targetF == f) {
                    ccMin = ccMax = cc;
                    break;
                }
                if (targetF > f) {
                    ccMax = cc;
                } else {
                    ccMin = cc;
                }
            }
        }
        int cc = (int) Math.rint(0.5 * (ccMin + ccMax));
        cc = Math.min(Math.max(cc, 0), 15);
        int cf = (int) Math.rint((0.5 * (ccMin + ccMax) - cc) / ChipModel.CF2CC);
        cf = Math.min(Math.max(cf, 0), 63);
        int[] adcOuts = getAdcOuts(chip, adcRange);
        Optim optim = new Optim(out, chip, cc, cf, adcRange);

        out.println("inf= " + infbit + " cc=" + cc + " cf=" + cf + " targetF=" + targetF);
        double deltaF = Math.max(optim.widF / 2, Math.max(targetF - optim.minF, optim.maxF - targetF));
        for (int i = 0; i < 10; i++) {
            double d = optim0optim(true, chip, cc, cf, adcOuts, targetF, deltaF);
            out.println("  " + deltaF + " -> " + d);
            deltaF += 0.5 * (d - deltaF);
        }
//        optim0.free();
        Record rec = optim.optim(targetF, deltaF, new Integer[]{infbit}, null);
        optim.free();
        return rec;
    }

    private static double getMeanModelF0(ChipModel chip, double cc, int nomAdcOut) {
        double l = chip.getLowerModelFfromAdcOut(cc, 0, 1032, nomAdcOut);
        double u = chip.getUpperModelFfromAdcOut(cc, 0, 1032, nomAdcOut);
        return 0.5 * (l + u);
    }

    public static List<Record> optimFreqStep(PrintWriter out, final ChipModel chip, AdcRange adcRange, double freqStep, int extend) {
        int[] adcOuts = getAdcOuts(chip, adcRange);
        double opt0minF = getMinF(chip, 0, 0, adcOuts);
        double opt0maxF = getMaxF(chip, 0, 0, adcOuts);
        double opt15minF = getMinF(chip, 0, 0, adcOuts);
        double opt15maxF = getMaxF(chip, 0, 0, adcOuts);
        out.println("opt00.minF=" + opt0minF);
        out.println("opt15.minF=" + opt15minF);
        out.println("opt00.maxF=" + opt0maxF);
        out.println("opt15.maxF=" + opt15maxF);
        int minK, maxK;
        double slack = 20;
        if (opt15minF >= opt15maxF) {
            minK = (int) Math.round(opt15maxF / freqStep) - extend;
        } else {
            minK = (int) Math.floor((opt15minF + opt15maxF) * 0.5 / freqStep) - extend;
        }
        if (opt0minF >= opt0maxF) {
            maxK = (int) Math.round(opt0minF / freqStep) + extend;
        } else {
            maxK = (int) Math.ceil((opt0minF + opt0maxF) * 0.5 / freqStep) + extend;
        }
        out.println("minOptF=" + minK * freqStep + " maxOptF=" + maxK * freqStep);
//        opt0.free();
//        opt15.free();

        List<Record> log = new ArrayList<>();
        for (int k = minK; k <= maxK; k++) {
            double targetF = freqStep * k;
            out.println("targetF=" + targetF);
            Record rec = optimCC(out, chip, adcRange, targetF);
            if (rec != null) {
                log.add(rec);
            }
        }
        return log;
    }

    public static List<Record> optimFromStd(PrintWriter out, final ChipModel chip, AdcRange adcRange, double[] relDiff) {
        int[] adcOuts = getAdcOuts(chip, adcRange);
        double opt0minF = getMinF(chip, 0, 0, adcOuts);
        double opt0maxF = getMaxF(chip, 0, 0, adcOuts);
        double opt15minF = getMinF(chip, 0, 0, adcOuts);
        double opt15maxF = getMaxF(chip, 0, 0, adcOuts);
        out.println("opt00.minF=" + opt0minF);
        out.println("opt15.minF=" + opt15minF);
        out.println("opt00.maxF=" + opt0maxF);
        out.println("opt15.maxF=" + opt15maxF);
        double bestFreq = opt0maxF;
        out.println("bestFreq=" + bestFreq);
//        opt0.free();
//        opt15.free();

        List<Record> log = new ArrayList<>();
        for (int k = 0; k < relDiff.length; k++) {
            double targetF = bestFreq + bestFreq * relDiff[k];
            out.println("targetF=" + targetF);
            Record rec = optimCC(out, chip, adcRange, targetF);
            if (rec != null) {
                log.add(rec);
            }
        }
        return log;
    }

    public static List<Record> optimFromStd(PrintWriter out, final ChipModel chip, int cc, AdcRange adcRange, double[] relDiff) {
        int cf = 0;
        int[] adcOuts = getAdcOuts(chip, adcRange);
        double minF = getMinF(chip, cc, cf, adcOuts);
        double maxF = getMaxF(chip, cc, cf, adcOuts);

        out.println("opt" + cc + ".minF=" + minF);
        out.println("opt" + cc + ".maxF=" + maxF);

        List<Record> log = new ArrayList<>();
        if (minF < maxF) {
            double targetF = 0.5 * (minF + maxF);
            out.println("middleF=" + targetF);
            Record rec = optimF(out, chip, cc, cf, adcRange, targetF);
            if (rec != null) {
                log.add(rec);
            }
        }
        double bestFreq = maxF;
        out.println("bestFreq=" + bestFreq);
        for (int k = 0; k < relDiff.length; k++) {
            double targetF = bestFreq + bestFreq * relDiff[k];
            out.println("targetF=" + targetF);
            Record rec = optimF(out, chip, cc, cf, adcRange, targetF);
            if (rec != null) {
                log.add(rec);
            }
        }
        return log;
    }

    public static Record optimCC(PrintWriter out, final ChipModel chip, AdcRange adcRange, double targetF) {
        int cf = 0;
        final double[] deltas = new double[16];
        int[] adcOuts = getAdcOuts(chip, adcRange);
        for (int cc = 0; cc < 16; cc++) {
            double minF = getMinF(chip, cf, cf, adcOuts);
            double maxF = getMaxF(chip, cf, cf, adcOuts);
            double widF = getWidF(chip, cf, cf, adcOuts);
            double deltaF = Math.max(widF / 2, Math.max(targetF - minF, maxF - targetF));
            for (int i = 0; i < 10; i++) {
                double d = optim0optim(false, chip, cc, cf, adcOuts, targetF, deltaF);
//                out.println("  " + deltaF + " -> " + d);
                deltaF += 0.5 * (d - deltaF);
            }
//            opt0.free();
            deltas[cc] = deltaF;
            out.println("cc=" + cc + " deltaF=" + deltaF);
        }
        Integer[] sort = new Integer[16];
        for (int i = 0; i < 16; i++) {
            sort[i] = Integer.valueOf(i);
        }
        Arrays.sort(sort, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return Double.compare(deltas[o1.intValue()], deltas[o2.intValue()]);
            }
        });

        Record rec = null;
        for (Integer cc : sort) {
            if (rec != null && deltas[cc.intValue()] > rec.bestDiff) {
                continue;
            }
            out.println("cc=" + cc);
            Optim opt = new Optim(out, chip, cc, cf, adcRange);
            rec = opt.optim(targetF, deltas[cc], rec);
            opt.free();
        }
        return rec;
    }

    static int[] getAdcOuts(ChipModel chip, AdcRange adcRange) {
        int[] adcOuts = chip.getAdcOuts();
        if (adcOuts == null) {
            adcOuts = new int[adcRange.maxAdcOut - adcRange.minAdcOut + 1];
            for (int i = 0; i < adcOuts.length; i++) {
                adcOuts[i] = adcRange.minAdcOut + i;
            }
        }
        return adcOuts;
    }

    private static double getMinF(ChipModel chip, int cc, int cf, int[] adcOuts) {
        double minF = Double.POSITIVE_INFINITY;
        for (int adcOut : adcOuts) {
            double l4095 = chip.getLowerModelFfromAdcOut(cc, cf, 4095, adcOut);
            minF = Math.min(minF, l4095);
        }
        return minF;
    }

    private static double getMaxF(ChipModel chip, int cc, int cf, int[] adcOuts) {
        double maxF = Double.NEGATIVE_INFINITY;
        for (int adcOut : adcOuts) {
            double u0 = chip.getUpperModelFfromAdcOut(cc, cf, 0, adcOut);
            maxF = Math.max(maxF, u0);
        }
        return maxF;
    }

    private static double getWidF(ChipModel chip, int cc, int cf, int[] adcOuts) {
        double widF = Double.NEGATIVE_INFINITY;
        for (int adcOut : adcOuts) {
            double l0 = chip.getLowerModelFfromAdcOut(cc, cf, 0, adcOut);
            double u0 = chip.getUpperModelFfromAdcOut(cc, cf, 0, adcOut);
            double l4095 = chip.getLowerModelFfromAdcOut(cc, cf, 4095, adcOut);
            double u4095 = chip.getUpperModelFfromAdcOut(cc, cf, 4095, adcOut);
            widF = Math.max(widF, u0 - l0);
            widF = Math.max(widF, u4095 - l4095);
        }
        return widF;
    }

    static double optim0optim(boolean fixK0, ChipModel chip, int cc, int cf, int[] adcOuts, double targetF, double deltaF) {
        List<Constraint> constraints = new ArrayList<>();
        fillDacInp(constraints, chip, cc, cf, adcOuts, targetF, deltaF);
        int sbit = 16;
        int infbit = 32;
        Optim0 optim0 = new Optim0(constraints, fixK0);
        double[] freeCoeff = optim0.optim();
        return metric(fixK0, chip, cc, cf, adcOuts, infbit, sbit, targetF, freeCoeff);
    }

    static void fillDacInp(List<Constraint> constraints, ChipModel chip, int cc, int cf, int[] adcOuts, double targetF, double deltaF) {
        constraints.clear();
        double arg0 = 1535;
        double[] args = new double[adcOuts.length];
        double[] vals = new double[adcOuts.length];
        int k = 0;
        for (int adcOut : adcOuts) {
            int dacInpL = Optim.getLowerDacInpForF(chip, cc, cf, adcOut, targetF - deltaF);
            int dacInpH = Optim.getUpperDacInpForF(chip, cc, cf, adcOut, targetF + deltaF);
//            System.out.println("    adcOut=" + args[k] + " dacInp=[" + dacInpL + "," + dacInpH + "]");
            if (dacInpL > 0) {
                constraints.add(new Constraint(adcOut, false, dacInpL));
            }
            if (dacInpH < 4095) {
                constraints.add(new Constraint(adcOut, true, dacInpH));
            }
            args[k] = adcOut;
            vals[k] = 0.5 * (dacInpL + dacInpH);
            k++;
        }
//        double[] coeff = Polys.approxPoly(arg0, args, vals, 5);
//        for (k = 0; k < args.length; k++) {
//            System.out.println("    adcOut=" + args[k] + " dacInp?=" + Polys.calcPoly(arg0, coeff, args[k]));
//        }
    }

    static double metric(boolean fixK0, ChipModel chip, int cc, int cf, int[] adcOuts, int infbit, int sbit, double targetF, double[] freeCoeff) {
        double[] t = new double[6];
        double minFl = Double.POSITIVE_INFINITY;
        double maxFu = Double.NEGATIVE_INFINITY;
        for (int adcOut : adcOuts) {
            int xs = PolyModel.computeXS(infbit, sbit, adcOut);
            PolyModel.fillCoef(t, xs);
            double dacInp = t[0];
            if (fixK0) {
                for (int i = 1; i <= 5; i++) {
                    dacInp += t[i] * freeCoeff[i - 1];
                }
            } else {
                dacInp += freeCoeff[0];
                for (int i = 1; i <= 5; i++) {
                    dacInp += t[i] * freeCoeff[i];
                }
            }
//            assert freeCoeff.length == t.length;
//            for (int i = 0; i < t.length; i++) {
//                dacInp += freeCoeff[i] * t[i];
//            }
            dacInp = Math.min(Math.max(dacInp, 0), 4095);
            double fl = chip.getLowerModelFfromAdcOut(cc, cf, dacInp, adcOut);
            double fu = chip.getUpperModelFfromAdcOut(cc, cf, dacInp, adcOut);
//            System.out.println("      adcOut=" + adcOut + " dacInp=" + dacInp + " fl=" + fl + " fu=" + fu);
            minFl = Math.min(minFl, fl);
            maxFu = Math.max(maxFu, fu);
        }
//        System.out.println(" minFl=" + minFl + " maxFu=" + maxFu);
        return Math.max(targetF - minFl, maxFu - targetF);
    }

    private Optim(PrintWriter out, ChipModel chip, int cc, int cf, AdcRange adcRange) {
        this.out = out;
        this.chip = chip;
        this.cc = cc;
        this.cf = cf;
        minAdcOut = adcRange.minAdcOut;
        maxAdcOut = adcRange.maxAdcOut;
        numPts = getAdcOuts().length;
        {
            double minF = Double.POSITIVE_INFINITY;
            double maxF = Double.NEGATIVE_INFINITY;
            double widF = Double.NEGATIVE_INFINITY;
            for (int adcOut : getAdcOuts()) {
                double l0 = chip.getLowerModelFfromAdcOut(cc, cf, 0, adcOut);
                double u0 = chip.getUpperModelFfromAdcOut(cc, cf, 0, adcOut);
                double l4095 = chip.getLowerModelFfromAdcOut(cc, cf, 4095, adcOut);
                double u4095 = chip.getUpperModelFfromAdcOut(cc, cf, 4095, adcOut);
                minF = Math.min(minF, l4095);
                maxF = Math.max(maxF, u0);
                widF = Math.max(widF, u0 - l0);
                widF = Math.max(widF, u4095 - l4095);
            }
            this.minF = minF;
            this.maxF = maxF;
            this.widF = widF;
        }
        out.println("minF=" + minF + " maxF=" + maxF + " widF=" + widF);
        GLPK.glp_init_smcp(smcp);
        smcp.msg_lev = GLPK.GLP_MSG_OFF;
        GLPK.glp_init_iocp(iocp);
        iocp.msg_lev = GLPK.GLP_MSG_OFF;
        GLPK.glp_set_prob_name(lp, "Polynom");
        GLPK.glp_add_cols(lp, 6);
        GLPK.glp_set_col_name(lp, k1bit, "k1bit");
        GLPK.glp_set_col_name(lp, k2bit, "k2bit");
        GLPK.glp_set_col_name(lp, k3bit, "k3bit");
        GLPK.glp_set_col_name(lp, k4bit, "k4bit");
        GLPK.glp_set_col_name(lp, k5bit, "k5bit");
        GLPK.glp_set_col_name(lp, opt, "opt");
        GLPK.glp_set_col_bnds(lp, opt, GLPK.GLP_FR, 0, 0);
        initObj();
        GLPK.glp_add_rows(lp, 2 * numPts);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        free();
    }

    public static class Record {

        public final double targetF;
        public final PolyState.Inp inp;
        public final double bestDiff;

        Record(double targetF, PolyState.Inp inp, double bestDiff) {
            this.targetF = targetF;
            this.inp = inp;
            this.bestDiff = bestDiff;
        }

        @Override
        public String toString() {
            return inp.toNom() + " # f = " + (int) Math.rint(targetF) + " +- " + (int) Math.ceil(bestDiff);
        }
    }

    private int[] getAdcOuts() {
        int[] adcOuts = chip.getAdcOuts();
        if (adcOuts == null) {
            adcOuts = new int[maxAdcOut - minAdcOut + 1];
            for (int i = 0; i < adcOuts.length; i++) {
                adcOuts[i] = minAdcOut + i;
            }
        }
        return adcOuts;
    }

    private Record optim(final double tarF, double deltaF, Record rec) {
        Integer[] infbits = new Integer[64];
        for (int i = 0; i < infbits.length; i++) {
            infbits[i] = Integer.valueOf(i);
        }
        Arrays.sort(infbits, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                int adcOut1 = 1535 + 8 * o1.intValue();
                double f1 = Math.max(
                        tarF - chip.getLowerModelFfromAdcOut(cc, cf, 1032, adcOut1),
                        chip.getUpperModelFfromAdcOut(cc, cf, 1032, adcOut1) - tarF);
                int adcOut2 = 1535 + 8 * o2.intValue();
                double f2 = Math.max(
                        tarF - chip.getLowerModelFfromAdcOut(cc, cf, 1032, adcOut2),
                        chip.getUpperModelFfromAdcOut(cc, cf, 1032, adcOut2) - tarF);
                return Double.compare(f1, f2);
            }
        });
        return optim(tarF, deltaF, infbits, rec);
    }

    private Record optim(double tarF, double deltaF, Integer[] infbits, Record rec) {
        targetF = tarF;
        fillDacInp(deltaF);
        out.println("targetF=" + targetF + " deltaF=" + deltaF);
        PolyState.Inp bestInp = null;
        double bestOpt = Double.POSITIVE_INFINITY;
        for (int infbit : infbits) {
            int nomAdcOut = 1535 + 8 * infbit;
            int[] adcOuts = getAdcOuts();
            if (adcOuts[0] <= nomAdcOut && nomAdcOut <= adcOuts[adcOuts.length - 1]) {
                double fl = chip.getLowerModelFfromAdcOut(cc, cf, 1032, nomAdcOut);
                double fu = chip.getUpperModelFfromAdcOut(cc, cf, 1032, nomAdcOut);
                if (rec != null && Math.max(targetF - Math.min(fl, minF), Math.max(fu, maxF) - targetF) >= rec.bestDiff) {
                    continue;
                }
                out.println("INFBIT=" + infbit + " min=" + (fl - targetF) + " max=" + (fu - targetF) + " rad=" + Math.max(targetF - fl, fu - targetF));
            } else {
                out.println("INFBIT=" + infbit);
            }
            int sbit = 31;
            fillRows(sbit, infbit);
            double[] freeCoeff = optimFree();
            double[] t = new double[6];
            double minFl = Double.POSITIVE_INFINITY;
            double maxFu = Double.NEGATIVE_INFINITY;
            for (int adcOut : getAdcOuts()) {
                int xs = PolyModel.computeXS(infbit, sbit, adcOut);
                PolyModel.fillCoef(t, xs);
                double dacInp = 0;
                assert freeCoeff.length == t.length;
                for (int i = 0; i < t.length; i++) {
                    dacInp += freeCoeff[i] * t[i];
                }
                dacInp = Math.min(Math.max(dacInp, 0), 4095);
                double fl = chip.getLowerModelFfromAdcOut(cc, cf, dacInp, adcOut);
                double fu = chip.getUpperModelFfromAdcOut(cc, cf, dacInp, adcOut);
                minFl = Math.min(minFl, fl);
                maxFu = Math.max(maxFu, fu);
            }

            if (rec != null && Math.max(targetF - minFl, maxFu - targetF) >= rec.bestDiff) {
                continue;
            }
            out.println("infbit=" + infbit + " min=" + (minFl - targetF) + " max=" + (maxFu - targetF) + " rad=" + Math.max(targetF - minFl, maxFu - targetF));
            for (sbit = 31; sbit >= 0; sbit--) {
                fillRows(sbit, infbit);
                double[] boundedCoeff = optimBounded();
//                GLPK.glp_write_mps(lp, GLPK.GLP_MPS_FILE, Pointer.NULL, "b" + (int)tarF + "_" + sbit + "_" + inf + ".mps");
                if (boundedCoeff == null) {
                    continue;
                }
                if (GLPK.glp_get_obj_val(lp) >= bestOpt) {
                    continue;
                }
                double[] coeff = optimInt();
//                GLPK.glp_write_mps(lp, GLPK.GLP_MPS_FILE, Pointer.NULL, "i" + (int)tarF + "_" + sbit + "_" + inf + ".mps");
                PolyState.Inp inp = PolyState.Inp.genNom();
                inp.INF = infbit;
                inp.SBIT = sbit;
                inp.K1BIT = (int) Math.min(Math.max(Math.round(coeff[1]), 1), 255);
                inp.K2BIT = (int) Math.min(Math.max(Math.round(coeff[2]), 0), 127);
                inp.K3BIT = (int) Math.min(Math.max(Math.round(coeff[3]), 0), 31);
                inp.K4BIT = (int) Math.min(Math.max(Math.round(coeff[4]), 0), 31);
                inp.K5BIT = (int) Math.min(Math.max(Math.round(coeff[5]), 0), 15);
                inp.CC = cc;
                inp.CF = cf;
                double diffF = Double.NEGATIVE_INFINITY;
                for (int adcOut : getAdcOuts()) {
                    inp.T = adcOut;
                    int dacInp = PolyModel.compute(inp);
                    double fl = chip.getLowerModelFfromAdcOut(cc, cf, dacInp, adcOut);
                    double fu = chip.getUpperModelFfromAdcOut(cc, cf, dacInp, adcOut);
                    diffF = Math.max(diffF, Math.max(Math.abs(fl - targetF), Math.abs(fu - targetF)));
                }
//                out.println("diffF=" + diffF);
                if (rec == null || diffF < rec.bestDiff) {
                    rec = new Record(tarF, inp, diffF);
                }

                double optVal = GLPK.glp_mip_col_val(lp, opt);
                if (optVal < bestOpt) {
                    bestInp = inp;
                    bestOpt = optVal;
                }

            }
        }
        out.println("targetF=" + targetF);
        if (bestInp != null) {
            out.println("bestInp=" + bestInp.toNom() + " bestOpt=" + bestOpt);
        }
        if (rec != null) {
            out.println("bestInp=" + rec.inp.toNom() + " bestDiff=" + rec.bestDiff);
        }
        return rec;
    }

    private void free() {
        if (lp != null) {
            GLPK.glp_delete_prob(lp);
            lp = null;
        }
    }

    private double[] optimFree() {
        kbitFree();
        int status = GLPK.glp_simplex(lp, smcp);
        if (status != 0) {
            throw new ArithmeticException("Glpk failure " + status);
        }
        status = GLPK.glp_get_status(lp);
        if (status != GLPK.GLP_OPT) {
            throw new ArithmeticException("Glpk status " + status);
        }
//        GLPK.glp_print_sol(lp, "free_simplex.txt");
//        out.println("k1bit " + GLPK.glp_get_col_prim(lp, k1bit) + " " + GLPK.glp_get_col_dual(lp, k1bit));
//        out.println("k2bit " + GLPK.glp_get_col_prim(lp, k2bit) + " " + GLPK.glp_get_col_dual(lp, k2bit));
//        out.println("k3bit " + GLPK.glp_get_col_prim(lp, k3bit) + " " + GLPK.glp_get_col_dual(lp, k3bit));
//        out.println("k4bit " + GLPK.glp_get_col_prim(lp, k4bit) + " " + GLPK.glp_get_col_dual(lp, k4bit));
//        out.println("k5bit " + GLPK.glp_get_col_prim(lp, k5bit) + " " + GLPK.glp_get_col_dual(lp, k5bit));
//        out.println("opt   " + GLPK.glp_get_col_prim(lp, opt) + " " + GLPK.glp_get_col_dual(lp, opt));
//        out.println();

        return new double[]{
            1,
            GLPK.glp_get_col_prim(lp, k1bit),
            GLPK.glp_get_col_prim(lp, k2bit),
            GLPK.glp_get_col_prim(lp, k3bit),
            GLPK.glp_get_col_prim(lp, k4bit),
            GLPK.glp_get_col_prim(lp, k5bit)
        };
    }

    private double[] optimBounded() {
        kbitBounds();
        int status = GLPK.glp_simplex(lp, smcp);
        if (status == GLPK.GLP_ESING) {
            out.println("RESET BASIS");
            GLPK.glp_std_basis(lp);
            status = GLPK.glp_simplex(lp, smcp);
        }
        if (status != 0) {
            throw new ArithmeticException("Glpk failure " + status);
        }
        status = GLPK.glp_get_status(lp);
        if (status == GLPK.GLP_NOFEAS) {
            return null;
        }
        if (status != GLPK.GLP_OPT) {
            throw new ArithmeticException("Glpk status " + status);
        }
//        GLPK.glp_print_sol(lp, "bounds_simplex.txt");
//        out.println("k1bit " + GLPK.glp_get_col_prim(lp, k1bit) + " " + GLPK.glp_get_col_dual(lp, k1bit));
//        out.println("k2bit " + GLPK.glp_get_col_prim(lp, k2bit) + " " + GLPK.glp_get_col_dual(lp, k2bit));
//        out.println("k3bit " + GLPK.glp_get_col_prim(lp, k3bit) + " " + GLPK.glp_get_col_dual(lp, k3bit));
//        out.println("k4bit " + GLPK.glp_get_col_prim(lp, k4bit) + " " + GLPK.glp_get_col_dual(lp, k4bit));
//        out.println("k5bit " + GLPK.glp_get_col_prim(lp, k5bit) + " " + GLPK.glp_get_col_dual(lp, k5bit));
//        out.println("opt   " + GLPK.glp_get_col_prim(lp, opt) + " " + GLPK.glp_get_col_dual(lp, opt));
//        out.println();
        return new double[]{
            1,
            GLPK.glp_get_col_prim(lp, k1bit),
            GLPK.glp_get_col_prim(lp, k2bit),
            GLPK.glp_get_col_prim(lp, k3bit),
            GLPK.glp_get_col_prim(lp, k4bit),
            GLPK.glp_get_col_prim(lp, k5bit)
        };
    }

    private double[] optimInt() {
        GLPK.glp_set_col_kind(lp, k1bit, GLPK.GLP_IV);
        GLPK.glp_set_col_kind(lp, k2bit, GLPK.GLP_IV);
        GLPK.glp_set_col_kind(lp, k3bit, GLPK.GLP_IV);
        GLPK.glp_set_col_kind(lp, k4bit, GLPK.GLP_IV);
        GLPK.glp_set_col_kind(lp, k5bit, GLPK.GLP_IV);
        GLPK.glp_set_col_kind(lp, opt, GLPK.GLP_CV);
        int status = GLPK.glp_intopt(lp, iocp);
        if (status != 0) {
            throw new ArithmeticException("Glpk failure " + status);
        }
        status = GLPK.glp_mip_status(lp);
        if (status != GLPK.GLP_OPT) {
            throw new ArithmeticException("Glpk status " + status);
        }
//        GLPK.glp_print_sol(lp, "intopt.txt");
//        out.println("k1bit " + GLPK.glp_mip_col_val(lp, k1bit));
//        out.println("k2bit " + GLPK.glp_mip_col_val(lp, k2bit));
//        out.println("k3bit " + GLPK.glp_mip_col_val(lp, k3bit));
//        out.println("k4bit " + GLPK.glp_mip_col_val(lp, k4bit));
//        out.println("k5bit " + GLPK.glp_mip_col_val(lp, k5bit));
//        out.println("opt   " + GLPK.glp_mip_col_val(lp, opt));
        return new double[]{
            1,
            GLPK.glp_mip_col_val(lp, k1bit),
            GLPK.glp_mip_col_val(lp, k2bit),
            GLPK.glp_mip_col_val(lp, k3bit),
            GLPK.glp_mip_col_val(lp, k4bit),
            GLPK.glp_mip_col_val(lp, k5bit)
        };
    }

    private void initObj() {
        GLPK.glp_set_obj_dir(lp, GLPK.GLP_MIN);
        GLPK.glp_set_obj_coef(lp, k1bit, 0);
        GLPK.glp_set_obj_coef(lp, k2bit, 0);
        GLPK.glp_set_obj_coef(lp, k3bit, 0);
        GLPK.glp_set_obj_coef(lp, k4bit, 0);
        GLPK.glp_set_obj_coef(lp, k5bit, 0);
        GLPK.glp_set_obj_coef(lp, opt, 1);
    }

    private void fillDacInp(double deltaF) {
        Arrays.fill(minDacInp, Integer.MAX_VALUE);
        Arrays.fill(maxDacInp, Integer.MIN_VALUE);
        for (int adcOut : getAdcOuts()) {
            minDacInp[adcOut] = getLowerDacInpForF(chip, cc, cf, adcOut, targetF - deltaF);
            maxDacInp[adcOut] = getUpperDacInpForF(chip, cc, cf, adcOut, targetF + deltaF);
        }
    }

    private void fillRows(int sbit, int infbit) {
        assert GLPK.glp_get_num_rows(lp) == 2 * numPts;
        int rowNum = 1;
        for (int adcOut : getAdcOuts()) {
            int xs = PolyModel.computeXS(infbit, sbit, adcOut);
            PolyModel.fillCoef(matRow, xs);

            int lt = rowNum++;
            int ht = rowNum++;
            int dacInpL = minDacInp[adcOut];
            double bRowL = dacInpL > 0 ? matRow[0] - dacInpL : matRow[0] - 0 + 10000;
            GLPK.glp_set_row_name(lp, lt, "lt" + xs);
            matRow[opt] = 1.0;
            GLPK.glp_set_mat_row(lp, lt, opt, inds, matRow);
            GLPK.glp_set_row_bnds(lp, lt, GLPK.GLP_LO, -bRowL, 0);
            int dacInpH = maxDacInp[adcOut];
            double bRowH = dacInpH < 4095 ? matRow[0] - dacInpH : matRow[0] - 4095 - 10000;
            GLPK.glp_set_row_name(lp, ht, "ht" + xs);
            matRow[opt] = -1.0;
            GLPK.glp_set_mat_row(lp, ht, opt, inds, matRow);
            GLPK.glp_set_row_bnds(lp, ht, GLPK.GLP_UP, 0, -bRowH);
        }
        assert rowNum == 2 * numPts + 1;
    }

    private void kbitBounds() {
        GLPK.glp_set_col_bnds(lp, k1bit, GLPK.GLP_DB, 1, 255);
        GLPK.glp_set_col_bnds(lp, k2bit, GLPK.GLP_DB, 0, 127);
        GLPK.glp_set_col_bnds(lp, k3bit, GLPK.GLP_DB, 0, 31);
        GLPK.glp_set_col_bnds(lp, k4bit, GLPK.GLP_DB, 0, 31);
        GLPK.glp_set_col_bnds(lp, k5bit, GLPK.GLP_DB, 0, 15);
    }

    private void kbitFree() {
        GLPK.glp_set_col_bnds(lp, k1bit, GLPK.GLP_FR, 0, 0);
        GLPK.glp_set_col_bnds(lp, k2bit, GLPK.GLP_FR, 0, 0);
        GLPK.glp_set_col_bnds(lp, k3bit, GLPK.GLP_FR, 0, 0);
        GLPK.glp_set_col_bnds(lp, k4bit, GLPK.GLP_FR, 0, 0);
        GLPK.glp_set_col_bnds(lp, k5bit, GLPK.GLP_FR, 0, 0);
    }

    public static int getLowerDacInpForF(ChipModel chip, int cc, int cf, int adcOut, double f) {
        int l = 0;
        int r = 4095;
        double fl = chip.getLowerModelFfromAdcOut(cc, cf, l, adcOut);
        double fr = chip.getLowerModelFfromAdcOut(cc, cf, r, adcOut);
        if (fl >= f) {
            return l;
        }
        if (fr <= f) {
            return r;
        }
        while (l < r - 1) {
            int m = (l + r) / 2;
            double fm = chip.getLowerModelFfromAdcOut(cc, cf, m, adcOut);
            if (f >= fm) {
                l = m;
            } else {
                r = m;
            }
        }
        return (l + r) / 2;
    }

    public static int getUpperDacInpForF(ChipModel chip, int cc, int cf, int adcOut, double f) {
        int l = 0;
        int r = 4095;
        double fl = chip.getUpperModelFfromAdcOut(cc, cf, l, adcOut);
        double fr = chip.getUpperModelFfromAdcOut(cc, cf, r, adcOut);
        if (fl >= f) {
            return l;
        }
        if (fr <= f) {
            return r;
        }
        while (l < r - 1) {
            int m = (l + r) / 2;
            double fm = chip.getUpperModelFfromAdcOut(cc, cf, m, adcOut);
            if (f >= fm) {
                l = m;
            } else {
                r = m;
            }
        }
        return (l + r) / 2;
    }
}
