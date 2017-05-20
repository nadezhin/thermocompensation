package ru.nsc.interval.thermocompensation.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.QRDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import ru.nsc.interval.thermocompensation.parse.CapSettings;
import ru.nsc.interval.thermocompensation.parse.Parse;

/**
 *
 */
public class ChipAnalyze {

    private static final int adc0 = 2048;
    private static final double t0 = +25;

    public static void printCs0(ChipT... chips) {
        for (ChipT chip : chips) {
            if (chip == null) {
                continue;
            }
            chip.printReport();
        }
    }

    public static void printCs1(ChipT... chips) {
        for (ChipT chip : chips) {
            if (chip == null) {
                continue;
            }
            System.out.println(chip.name + " " + chip.runDate);
            Map<CapSettings, Integer> counts = new TreeMap<>(new Comparator<CapSettings>() {
                @Override
                public int compare(CapSettings o1, CapSettings o2) {
                    int cmp = Integer.compare(o1.xt, o2.xt);
                    if (cmp != 0) {
                        return cmp;
                    }
                    cmp = Integer.compare(o1.cc, o2.cc);
                    if (cmp != 0) {
                        return cmp;
                    }
                    cmp = Integer.compare(o1.cf, o2.cf);
                    return cmp;
                }
            });
            List<Parse.MeasF> measFs = chip.getMeasFs();
            if (measFs.isEmpty()) {
                System.out.println("NO MeasFs");
                continue;
            }
            for (Parse.MeasF mf : measFs) {
                CapSettings cs = new CapSettings(mf.inp.CC, mf.inp.CF, mf.xt);
                Integer oldCount = counts.get(cs);
                counts.put(cs, Integer.valueOf(oldCount != null ? oldCount + 1 : 1));
            }
            Map.Entry<CapSettings, Integer> bestCs = null;
            for (Map.Entry<CapSettings, Integer> e : counts.entrySet()) {
                System.out.println("  " + e.getKey() + " " + e.getValue());
                if (bestCs == null || bestCs.getValue() < e.getValue()) {
                    bestCs = e;
                }
            }
            if (!chip.cs0.equals(bestCs.getKey())) {
                System.out.println("Wrong CS0 " + chip.cs0 + " should be " + bestCs.getKey());
            }
        }
    }

    public static void checkAccur(String msg, ChipT... chips) {
        checkAccur(msg, Integer.MIN_VALUE, Integer.MAX_VALUE, chips);
    }

    public static void checkAccur(String msg, int minAdcOut, int maxAdcOut, ChipT... chips) {
        for (ChipT chip : chips) {
            if (chip == null) {
                continue;
            }
            System.out.println(chip.name + " " + chip.runDate);
            ChipExpress chipe = new ChipExpress(chip);
            double worst0 = Double.NEGATIVE_INFINITY;
            double rms0 = 0;
            int count0 = 0;
            double worst = Double.NEGATIVE_INFINITY;
            double rms = 0;
            int count = 0;
            int cc0 = chip.cs0.cc;
            int cf0 = chip.cs0.cf;
            for (Parse.MeasF mf : chip.getMeasFs()) {
                double adcOut = chip.getAdcOut(mf.time);
                if (adcOut < minAdcOut || adcOut > maxAdcOut) {
                    continue;
                }
                double modelDF = chipe.getDF(mf.inp.CC, mf.inp.CF, adcOut, mf.xt);
                double experDF = mf.f - chip.getF0(mf.time);
                if (Double.isNaN(experDF)) {
                    continue;
                }
                double diff = experDF - modelDF;
                if (mf.inp.CC == cc0 && mf.inp.CF == cf0) {
                    worst0 = Math.max(worst0, Math.abs(diff));
                    rms0 += diff * diff;
                    count0++;
                }
                worst = Math.max(worst, Math.abs(diff));
                rms += diff * diff;
                count++;
            }
            System.out.println(msg + " worst0=" + worst0 + " rms0=" + Math.sqrt(rms0 / count0) + " worst=" + worst + " rms=" + Math.sqrt(rms / count));
        }
    }

    public static void checkMonotonic(ChipT... chips) {
        for (ChipT chip : chips) {
            if (chip == null) {
                continue;
            }
            System.out.println(chip.name + " " + chip.runDate);
            ChipExpress chipe = new ChipExpress(chip);
            int cf = 0;
            int errCnt = 0;
            loopCC:
            for (int cc = 0; cc < 16; cc++) {
                System.out.print("#");
                for (int adcOut = chip.minAdcOut; adcOut <= chip.maxAdcOut; adcOut++) {
                    double oldDf = chipe.getDF(cc, cf, adcOut, 0);
                    for (int dacInp = 1; dacInp < 4096; dacInp++) {
                        double df = chipe.getDF(cc, cf, adcOut, dacInp);
                        if (!(df > oldDf)) {
                            if (errCnt >= 100) {
                                System.out.println("...");
                                break loopCC;
                            }
                            System.out.println("  adcOut=" + adcOut + " cc=" + cc + " dacInp=" + dacInp + " " + oldDf + " -> " + df);
                            errCnt++;
                        }
                        oldDf = df;
                    }
                }
            }
            System.out.println();
        }
    }

    public static void checkMonotonic0(ChipT... chips) {
        for (ChipT chip : chips) {
            if (chip == null) {
                continue;
            }
            System.out.println(chip.name + " " + chip.runDate);
            ChipExpress chipe = new ChipExpress(chip);
            int cc = 0;
            int cf = 0;
            int errCnt = 0;
            loopCC:
            for (int adcOut = chip.minAdcOut; adcOut <= chip.maxAdcOut; adcOut++) {
                double oldDf = chipe.getDF(cc, cf, adcOut, 0);
                for (int dacInp = 1; dacInp < 4096; dacInp++) {
                    double df = chipe.getDF(cc, cf, adcOut, dacInp);
                    if (!(df > oldDf)) {
                        if (errCnt >= 10) {
                            System.out.println("...");
                            break loopCC;
                        }
                        System.out.println("  adcOut=" + adcOut + " cc=" + cc + " dacInp=" + dacInp + " " + oldDf + " -> " + df);
                        errCnt++;
                    }
                    oldDf = df;
                }
            }
            System.out.println();
        }
    }

    public static void diffModels(ChipT[] chips1, ChipT[] chips2) {
        assert chips1.length == chips2.length;
        for (int chipNo = 0; chipNo < chips1.length; chipNo++) {
            ChipT chipt1 = chips1[chipNo];
            ChipT chipt2 = chips2[chipNo];
            if (chipt1 == null || chipt2 == null) {
                continue;
            }
            ChipExpress chipe1 = new ChipExpress(chipt1);
            ChipExpress chipe2 = new ChipExpress(chipt2);
            int minAdcOut = Math.max(chipe1.minAdcOut, chipe2.minAdcOut);
            int maxAdcOut = Math.min(chipe1.maxAdcOut, chipe2.maxAdcOut);
            System.out.println(chipt1.name + " " + chipt1.runDate + " " + chipt2.name + " " + chipt2.runDate);
            int cf = 0;
            double maxDf = 0;
            for (int cc = 0; cc < 16; cc++) {
                System.out.print("#");
                for (int adcOut = minAdcOut; adcOut <= maxAdcOut; adcOut++) {
                    for (int dacInp = 0; dacInp < 4096; dacInp++) {
                        double lf1 = chipe1.getLowerModelFfromAdcOut(cc, cf, dacInp, adcOut);
                        double uf1 = chipe1.getUpperModelFfromAdcOut(cc, cf, dacInp, adcOut);
                        double lf2 = chipe2.getLowerModelFfromAdcOut(cc, cf, dacInp, adcOut);
                        double uf2 = chipe2.getUpperModelFfromAdcOut(cc, cf, dacInp, adcOut);
                        double df = Math.max(Math.abs(lf2 - lf1), Math.abs(uf2 - uf1));
                        maxDf = Math.max(maxDf, df);
                    }
                }
            }
            System.out.println(maxDf);
        }
    }

    private static class BasisFun {

        private static final int N = 16;
        private static final int STEP = 4096 / N;
        private final CapSettings cs0;
        private final double dacInpMin, dacInpCenter, dacInpMax;
        private final int adcP;
        private final int ccP;

        private BasisFun(CapSettings cs0, int k0, int adcP) {
            this(cs0, k0 - 1, k0, k0 + 1, adcP, 0);
        }

        private BasisFun(CapSettings cs0, int kMin, int kCenter, int kMax, int adcP, int ccP) {
            this.cs0 = cs0;
            assert -1 <= kMin && kMin < kCenter && kCenter < kMax && kMax <= N + 1;
            dacInpMin = kMin < 0 ? Double.NEGATIVE_INFINITY : Math.min(kMin * STEP, 4095);
            dacInpCenter = Math.min(kCenter * STEP, 4095);
            dacInpMax = kMax > N ? Double.POSITIVE_INFINITY : Math.min(kMax * STEP, 4095);
            this.adcP = adcP;
            this.ccP = ccP;
        }

        private double apply(double cccf, double adcOut, double dacInp) {
            if (dacInp <= dacInpMin || dacInp >= dacInpMax) {
                return 0;
            }
            double v = 1;
            double dx = adcOut - adc0;
            for (int i = 0; i < adcP; i++) {
                v *= dx;
            }
            double dc = cccf - (cs0.cc + ChipExpress.CF2CC * cs0.cf);
            for (int i = 0; i < ccP; i++) {
                v *= dc;
            }
            if (dacInp < dacInpCenter) {
                assert dacInp > dacInpMin;
                return (1 - (dacInpCenter - dacInp) / (dacInpCenter - dacInpMin)) * v;
            } else if (dacInp > dacInpCenter) {
                assert dacInp < dacInpMax;
                return (1 - (dacInp - dacInpCenter) / (dacInpMax - dacInpCenter)) * v;
            } else {
                return v;
            }
        }

        @Override
        public String toString() {
            return "xt" + dacInpMin + "," + dacInpCenter + "," + dacInpMax + "t" + adcP;
        }
    }

    private static List<BasisFun> genBasis2D(CapSettings cs0) {
        List<BasisFun> result = new ArrayList<>();
        for (int k = 0; k <= 16; k++) {
            for (int l = 0; l <= 2; l++) {
                result.add(new BasisFun(cs0, k, l));
            }
        }
        return result;
    }

    private static List<BasisFun> genBasis3D1(CapSettings cs0) {
        List<BasisFun> result = new ArrayList<>();
        for (int k = 0; k <= 16; k++) {
            for (int l = 0; l <= 2; l++) {
                for (int m = 0; m <= 1; m++) {
                    result.add(new BasisFun(cs0, k - 1, k, k + 1, l, m));
                }
            }
        }
        return result;
    }

    private static List<BasisFun> genBasis3D2(CapSettings cs0) {
        List<BasisFun> result = new ArrayList<>();
        for (int k = 0; k <= 16; k++) {
            for (int l = 0; l <= 2; l++) {
                for (int m = 0; m <= 2; m++) {
                    result.add(new BasisFun(cs0, k - 1, k, k + 1, l, m));
                }
            }
        }
        return result;
    }

    private static List<BasisFun> genBasis3DF1(CapSettings cs0) {
        List<BasisFun> result = new ArrayList<>();
        for (int l = 0; l <= 2; l++) {
            for (int k = 0; k <= 16; k++) {
                result.add(new BasisFun(cs0, k - 1, k, k + 1, l, 0));
            }
            for (int m = 1; m <= 1; m++) {
                result.add(new BasisFun(cs0, -1, 0, 1, l, m));
                result.add(new BasisFun(cs0, 0, 1, 8, l, m));
                result.add(new BasisFun(cs0, 1, 8, 16, l, m));
                result.add(new BasisFun(cs0, 8, 16, 17, l, m));
            }
        }
        return result;
    }

    private static List<BasisFun> genBasis3DF2(CapSettings cs0) {
        List<BasisFun> result = new ArrayList<>();
        for (int l = 0; l <= 2; l++) {
            for (int k = 0; k <= 16; k++) {
                result.add(new BasisFun(cs0, k - 1, k, k + 1, l, 0));
            }
            for (int m = 1; m <= 2; m++) {
                result.add(new BasisFun(cs0, -1, 0, 1, l, m));
                result.add(new BasisFun(cs0, 0, 1, 8, l, m));
                result.add(new BasisFun(cs0, 1, 8, 16, l, m));
                result.add(new BasisFun(cs0, 8, 16, 17, l, m));
            }
        }
        return result;
    }

    private static class Approximate {

        private final ChipT chip;
        private final List<Parse.MeasF> measFc = new ArrayList<>();
        private final List<BasisFun> basis;

        private Approximate(ChipT chip, List<BasisFun> basis, boolean withCC) {
            this.chip = chip;
            this.basis = basis;
            for (Parse.MeasF mf : chip.getMeasFs()) {
                if (withCC || mf.inp.CC == chip.cs0.cc && mf.inp.CF == chip.cs0.cf) {
                    measFc.add(mf);
                }
            }
        }

        private double[] approxMat() {
            assert basis.size() <= measFc.size();
            RealMatrix A = new Array2DRowRealMatrix(measFc.size(), basis.size());
            RealVector b = new ArrayRealVector(measFc.size());
            for (int j = 0; j < measFc.size(); j++) {
                Parse.MeasF mf = measFc.get(j);
                if (Double.isNaN(mf.f - chip.getF0(mf.time))) {
                    continue;
                }
                double adcOut = chip.getAdcOut(mf.time);
                double cccf = mf.inp.CC + ChipExpress.CF2CC * mf.inp.CF;;
                for (int k = 0; k < basis.size(); k++) {
                    A.setEntry(j, k, basis.get(k).apply(cccf, adcOut, mf.xt));
                }
                b.setEntry(j, mf.f - chip.getF0(mf.time));
            }
            DecompositionSolver solver;
            if (basis.size() == measFc.size()) {
                solver = new LUDecomposition(A).getSolver();
            } else {
                solver = new QRDecomposition(A).getSolver();
            }
            RealVector x = solver.solve(b);
            assert x.getDimension() == basis.size();
            double[] result = new double[basis.size()];
            for (int k = 0; k < basis.size(); k++) {
                result[k] = x.getEntry(k);
            }
            return result;
        }

        private double apply(double[] coeff, double cccf, double adcOut, double dacInp) {
            double v = 0;
            for (int k = 0; k < basis.size(); k++) {
                v += coeff[k] * basis.get(k).apply(cccf, adcOut, dacInp);
            }
            return v;
        }

        private void checkResult(String msg, double[] coeff) {
            double worst0 = Double.NEGATIVE_INFINITY;
            double rms0 = 0;
            int count0 = 0;
            double worst = Double.NEGATIVE_INFINITY;
            double rms = 0;
            int count = 0;
            int cc0 = chip.cs0.cc;
            int cf0 = chip.cs0.cf;
            for (Parse.MeasF mf : chip.getMeasFs()) {
                double adcOut = chip.getAdcOut(mf.time);
                double cccf = mf.inp.CC + ChipExpress.CF2CC * mf.inp.CF;
                double modelDF = apply(coeff, cccf, adcOut, mf.xt);
                double experDF = mf.f - chip.getF0(mf.time);
                if (Double.isNaN(experDF)) {
                    continue;
                }
                double diff = experDF - modelDF;
                if (mf.inp.CC == cc0 && mf.inp.CF == cf0) {
                    worst0 = Math.max(worst0, Math.abs(diff));
                    rms0 += diff * diff;
                    count0++;
                }
                worst = Math.max(worst, Math.abs(diff));
                rms += diff * diff;
                count++;
            }
            System.out.println(msg + " worst0=" + worst0 + " rms=" + Math.sqrt(rms0 / count) + " worst=" + worst + " rms=" + Math.sqrt(rms / count));
        }
    }

    public static void printInpPpm(double freqStep, ChipT... chips) {
        for (ChipT chip : chips) {
            if (chip == null) {
                continue;
            }
            chip.printReport();
            LinkedHashMap<PolyState.Inp, List<Parse.InpF>> allInps = new LinkedHashMap<>();
            for (Parse.InpF mi : chip.inpFs) {
                List<Parse.InpF> list = allInps.get(mi.inp);
                if (list == null) {
                    list = new ArrayList<>();
                    allInps.put(mi.inp, list);
                }
                list.add(mi);
            }
            for (Map.Entry<PolyState.Inp, List<Parse.InpF>> e : allInps.entrySet()) {
                PolyState.Inp inp = e.getKey();
                List<Parse.InpF> list = e.getValue();
                double minFreq = Double.POSITIVE_INFINITY;
                double maxFreq = Double.NEGATIVE_INFINITY;
                double minFreqD = Double.POSITIVE_INFINITY;
                double maxFreqD = Double.NEGATIVE_INFINITY;
                for (Parse.InpF mi : list) {
                    if (mi.disturb) {
                        minFreqD = Math.min(minFreqD, mi.f);
                        maxFreqD = Math.max(maxFreqD, mi.f);
                    } else {
                        minFreq = Math.min(minFreq, mi.f);
                        maxFreq = Math.max(maxFreq, mi.f);
                    }
                }
                double freq = Math.rint((maxFreq + minFreq) / 2 / freqStep) * freqStep;
                double ppmMin = (minFreq / freq - 1) * 1e6;
                double ppmMax = (maxFreq / freq - 1) * 1e6;
                double ppm = Math.max(-ppmMin, ppmMax);
                double dfreq = Math.max(freq - minFreq, maxFreq - freq);
                System.out.println(inp.toNom() + " " + (int) freq + " +-" + Math.ceil(ppm * 100) / 100 + "ppm (" + (int) Math.ceil(dfreq) + "Hz)"
                        + " [" + Math.floor(ppmMin * 100) / 100 + "," + Math.ceil(ppmMax * 100) / 100 + "]ppm"
                        + " [" + minFreq + "," + maxFreq + "] "
                        + " [" + minFreqD + "," + maxFreqD + "] " + list.size());
            }
        }
    }

    public static void printInpPpm(List<List<ParseTestInps.ExtendedInp>> inps, ChipT[] chips) {
        for (int chipNo = 0; chipNo < chips.length; chipNo++) {
            ChipT chip = chips[chipNo];
            List<ParseTestInps.ExtendedInp> inpl = chipNo < inps.size() ? inps.get(chipNo) : null;
            if (chip == null || inpl == null || inpl.isEmpty()) {
                continue;
            }
            chip.printReport();
            LinkedHashMap<PolyState.Inp, List<Parse.InpF>> allInps = new LinkedHashMap<>();
            for (Parse.InpF mi : chip.inpFs) {
                List<Parse.InpF> list = allInps.get(mi.inp);
                if (list == null) {
                    list = new ArrayList<>();
                    allInps.put(mi.inp, list);
                }
                list.add(mi);
            }
            for (ParseTestInps.ExtendedInp einp : inpl) {
                PolyState.Inp inp = einp.inp;
                List<Parse.InpF> list = allInps.get(inp);
                double minFreq = Double.POSITIVE_INFINITY;
                double maxFreq = Double.NEGATIVE_INFINITY;
                double minFreqD = Double.POSITIVE_INFINITY;
                double maxFreqD = Double.NEGATIVE_INFINITY;
                for (Parse.InpF mi : list) {
                    if (mi.disturb) {
                        minFreqD = Math.min(minFreqD, mi.f);
                        maxFreqD = Math.max(maxFreqD, mi.f);
                    } else {
                        minFreq = Math.min(minFreq, mi.f);
                        maxFreq = Math.max(maxFreq, mi.f);
                    }
                }
                double freq = einp.f;
                double ppmMin = (minFreq / freq - 1) * 1e6;
                double ppmMax = (maxFreq / freq - 1) * 1e6;
                double ppm = Math.max(-ppmMin, ppmMax);
                double dfreq = Math.max(freq - minFreq, maxFreq - freq);
                System.out.println(inp.toNom() + " " + (int) freq + " +-" + Math.ceil(ppm * 100) / 100 + "ppm "
                        + " [" + Math.floor(ppmMin * 100) / 100 + "," + Math.ceil(ppmMax * 100) / 100 + "]ppm"
                        + " +- " + (int) Math.ceil(dfreq) + "Hz (expected " + (int) Math.ceil(einp.df) + "Hz)");
            }
        }
    }

    public static double getMinF(List<Parse.InpF> inpFs) {
        double result = Double.POSITIVE_INFINITY;
        for (Parse.InpF mf : inpFs) {
            result = Math.min(result, mf.f);
        }
        return result;
    }

    public static double getMaxF(List<Parse.InpF> inpFs) {
        double result = Double.NEGATIVE_INFINITY;
        for (Parse.InpF mf : inpFs) {
            result = Math.max(result, mf.f);
        }
        return result;
    }

    public static double getDiff(List<Parse.InpF> inpFs, double freq) {
        double minF = getMinF(inpFs);
        double maxF = getMaxF(inpFs);
        return Math.max(maxF - freq, freq - minF);
    }

    public static double getPpm(List<Parse.InpF> inpFs, double freq) {
        return getDiff(inpFs, freq) / freq * 1e6;
    }

    public static double getMidF(List<Parse.InpF> inpFs, double freqStep) {
        double minF = getMinF(inpFs);
        double maxF = getMaxF(inpFs);
        return Math.rint((minF + maxF) / 2 / freqStep) * freqStep;
    }

    public static double getMinF(ChipModel chipModel, PolyState.Inp inp, AdcRange adcRange) {
        double result = Double.POSITIVE_INFINITY;
        for (int adcOut = adcRange.minAdcOut; adcOut <= adcRange.maxAdcOut; adcOut++) {
            inp.T = adcOut;
            int dacInp = PolyModel.compute(inp);
            double f = chipModel.getLowerModelFfromAdcOut(inp.CC, inp.CF, dacInp, adcOut);
            result = Math.min(result, f);
        }
        return result;
    }

    public static double getMaxF(ChipModel chipModel, PolyState.Inp inp, AdcRange adcRange) {
        double result = Double.NEGATIVE_INFINITY;
        for (int adcOut = adcRange.minAdcOut; adcOut <= adcRange.maxAdcOut; adcOut++) {
            inp.T = adcOut;
            int dacInp = PolyModel.compute(inp);
            double f = chipModel.getUpperModelFfromAdcOut(inp.CC, inp.CF, dacInp, adcOut);
            result = Math.max(result, f);
        }
        return result;
    }

    public static double getMidF(ChipModel chipModel, PolyState.Inp inp, AdcRange adcRange, double freqStep) {
        double minF = getMinF(chipModel, inp, adcRange);
        double maxF = getMaxF(chipModel, inp, adcRange);
        return Math.rint((minF + maxF) / 2 / freqStep) * freqStep;
    }

    public static double getPpm(ChipModel chipModel, PolyState.Inp inp, AdcRange adcRange, double freq) {
        double minF = getMinF(chipModel, inp, adcRange);
        double maxF = getMaxF(chipModel, inp, adcRange);
        return Math.max(maxF - freq, freq - minF) / freq * 1e6;
    }

    /**
     * Вычислить предполагамое максимальное отклонение частоты от заданный на
     * заданном наборе параметров по модели микросборки в заданном диапазоне
     * цифровых температур
     *
     * @param chip модель микросборки
     * @param minAdcOut минимальная цифровая темература диапазона
     * @param maxAdcOut максимальная цифровая темература диапазона
     * @param inp набор параметров
     * @param targetF заданная частота
     * @return отклонение в ppm
     */
    public static double calcModelPpm(ChipModel chip, int minAdcOut, int maxAdcOut, PolyState.Inp inp, double targetF) {
        double minF = Double.POSITIVE_INFINITY;
        double maxF = Double.NEGATIVE_INFINITY;
        for (int adcOut = minAdcOut; adcOut <= maxAdcOut; adcOut++) {
            inp.T = adcOut;
            int dacInp = PolyModel.compute(inp);
            double lowerF = chip.getLowerModelFfromAdcOut(inp.CC, inp.CF, dacInp, adcOut);
            double upperF = chip.getUpperModelFfromAdcOut(inp.CC, inp.CF, dacInp, adcOut);
            minF = Math.min(minF, lowerF);
            maxF = Math.max(maxF, upperF);
        }
        return Math.max(maxF - targetF, targetF - minF) / targetF * 1e6;
    }
}
