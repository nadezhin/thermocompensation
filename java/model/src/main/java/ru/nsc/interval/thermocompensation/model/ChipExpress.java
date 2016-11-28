package ru.nsc.interval.thermocompensation.model;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import ru.nsc.interval.thermocompensation.parse.CapSettings;
import ru.nsc.interval.thermocompensation.parse.Parse;
import ru.nsc.interval.thermocompensation.spline.Polys;

/**
 *
 */
public class ChipExpress implements ChipModel {

    public static final double CF2CC = 0.034;
    private static final int adc0 = 2048;
    private final ChipT[] chips;
    private final CapSettings cs0;
    private final double[][][] coeff = new double[17][3][];
    private final double[] minF0 = new double[4096];
    private final double[] maxF0 = new double[4096];
    public final int minAdcOut;
    public final int maxAdcOut;

    public ChipExpress(ChipT... chips) {
        this(Integer.MIN_VALUE, Integer.MAX_VALUE, chips);
    }

    public ChipExpress(int minAdcOut0, int maxAdcOut0, ChipT... chips) {
        this.chips = chips;
        cs0 = chips[0].cs0;
        assert cs0.xt % 256 == 0 || cs0.xt == 4095;
        assert cs0.cc == 0 || cs0.cc == 8 || cs0.cc == 15;
        assert cs0.cf == 0;
        Arrays.fill(minF0, Double.POSITIVE_INFINITY);
        Arrays.fill(maxF0, Double.NEGATIVE_INFINITY);
        for (ChipT chip : chips) {
            Parse.MeasF oldMf = null;
            double oldAdcOutD = Double.NaN;
            for (Parse.MeasF mf : chip.measF0s) {
                assert mf.inp.CC == cs0.cc && mf.inp.CF == cs0.cf && mf.xt == cs0.xt;
                double adcOutD = chip.getAdcOut(mf.time);
                int adcOut = Math.min(Math.max((int) Math.rint(adcOutD), 0), 4095);
                minF0[adcOut] = Math.min(minF0[adcOut], mf.f);
                maxF0[adcOut] = Math.max(maxF0[adcOut], mf.f);
                if (oldMf != null) {
                    int n = 2 + (int) Math.abs(adcOutD - oldAdcOutD) * 3;
                    for (int i = 1; i < n; i++) {
                        double aD = (oldAdcOutD * (n - i) + adcOutD * i) / n;
                        int a = Math.min(Math.max((int) Math.rint(aD), 0), 4095);
                        double f = (oldMf.f * (n - i) + mf.f * i) / n;
                        minF0[a] = Math.min(minF0[a], f);
                        maxF0[a] = Math.max(maxF0[a], f);
                    }
                }
                oldMf = mf;
                oldAdcOutD = adcOutD;
            }
        }
        int adcOut = 0;
        while (minF0[adcOut] > maxF0[adcOut] || adcOut < minAdcOut0) {
            adcOut++;
        }
        minAdcOut = adcOut;
        for (adcOut = 0; adcOut < minAdcOut; adcOut++) {
            minF0[adcOut] = minF0[minAdcOut];
            maxF0[adcOut] = maxF0[minAdcOut];
        }
        adcOut = 4095;
        while (minF0[adcOut] > maxF0[adcOut] || adcOut > maxAdcOut0) {
            adcOut--;
        }
        maxAdcOut = adcOut;
        for (adcOut = 4095; adcOut > maxAdcOut; adcOut--) {
            minF0[adcOut] = minF0[maxAdcOut];
            maxF0[adcOut] = maxF0[maxAdcOut];
        }
        for (adcOut = 0; adcOut < 4096; adcOut++) {
            assert minF0[adcOut] <= maxF0[adcOut];
        }
        int l0;
        switch (cs0.cc) {
            case 0:
                l0 = 0;
                break;
            case 8:
                l0 = 1;
                break;
            case 15:
                l0 = 2;
                break;
            default:
                throw new AssertionError();
        }
        for (int k = 0; k <= 16; k++) {
            int xt = Math.min(k * 256, 4095);
            for (int l = 0; l <= 2; l++) {
                int cc = Math.min(l * 8, 15);

                List<List<Parse.MeasF>> lists = new ArrayList<>();
                int totalSize = 0;
                for (ChipT chip : chips) {
                    List<Parse.MeasF> list = new ArrayList<>();
                    for (Parse.MeasF mf : chip.measFs) {
                        if (mf.inp.CC != cc || mf.inp.CF != cs0.cf || mf.xt != xt || Double.isNaN(mf.f)) {
                            continue;
                        }
                        Date date = mf.getDate();
                        double adcOutR = chip.getAdcOut(date);
                        if (adcOutR < minAdcOut || adcOutR > maxAdcOut) {
                            continue;
                        }
                        list.add(mf);
                    }
                    lists.add(list);
                    totalSize += list.size();
                }
                if (totalSize == 0) {
                    continue;
                }
//                System.out.println("  xt=" + xt + " cc=" + cc + " " + totalSize + " points");
                if (xt == cs0.xt && cc == cs0.cc) {
                    coeff[k][l] = new double[0];
                    continue;
                }
                double[] args = new double[totalSize];
                double[] vals = new double[totalSize];
                int ind = 0;
                for (int i = 0; i < chips.length; i++) {
                    ChipT chip = chips[i];
                    List<Parse.MeasF> list = lists.get(i);
                    for (int j = 0; j < list.size(); j++) {
                        Parse.MeasF mf = list.get(j);
                        Date date = mf.getDate();
                        double adcOutR = chip.getAdcOut(date);
                        double f0 = chip.getF0(date);
                        double df = mf.f - f0;
                        args[ind] = adcOutR;
                        vals[ind] = df;
                        ind++;
                    }
                }
                assert ind == totalSize;
                coeff[k][l] = Polys.approxPoly(adc0, args, vals, Math.min(2, totalSize - 1));
            }
            assert coeff[k][l0] != null;
        }
        if (coeff[0][0] == null || coeff[0][1] == null || coeff[0][2] == null) {
            for (int k = 0; k <= 16; k++) {
                for (int l = 0; l <= 2; l++) {
                    if (l != l0) {
//                        assert coeff[k][l] == null;
                        coeff[k][l] = coeff[k][l0];
                    }
                }
            }
        } else {
            assert coeff[0][0] != null && coeff[0][1] != null && coeff[0][2] != null;
            assert coeff[16][0] != null && coeff[16][1] != null && coeff[16][2] != null;
            for (int l = 0; l <= 2; l++) {
                if (l == l0) {
                    continue;
                }
                int oldK = 0;
                while (oldK < 16) {
                    int newK = oldK + 1;
                    while (coeff[newK][l] == null) {
                        newK++;
                    }
                    if (newK != oldK + 1) {
                        double[] oldDiff = sub(coeff[oldK][l], coeff[oldK][l0]);
                        double[] newDiff = sub(coeff[newK][l], coeff[newK][l0]);
                        for (int k = oldK + 1; k < newK; k++) {
                            double[] diff = interp(oldDiff, newDiff, (k - oldK) / (double) (newK - oldK));
                            assert coeff[k][l] == null;
                            coeff[k][l] = add(coeff[k][l0], diff);
                        }
                    }
                    oldK = newK;
                }
            }
        }
    }

    public void printReport() {
        for (ChipT chip : chips) {
            chip.printReport();
        }
    }

    private static double getV(double[] a, int i) {
        return i < a.length ? a[i] : 0;
    }

    private static double[] add(double[] a, double[] b) {
        double[] r = new double[Math.max(a.length, b.length)];
        for (int i = 0; i < r.length; i++) {
            r[i] = getV(a, i) + getV(b, i);
        }
        return r;
    }

    private static double[] sub(double[] a, double[] b) {
        double[] r = new double[Math.max(a.length, b.length)];
        for (int i = 0; i < r.length; i++) {
            r[i] = getV(a, i) - getV(b, i);
        }
        return r;
    }

    private static double[] interp(double[] a, double[] b, double k) {
        double[] r = new double[Math.max(a.length, b.length)];
        for (int i = 0; i < r.length; i++) {
            r[i] = (1 - k) * getV(a, i) + k * getV(b, i);
        }
        return r;
    }

    public double getLowerModelF0fromAdcOut(int adcOut) {
        return minF0[adcOut];
    }

    public double getUpperModelF0fromAdcOut(int adcOut) {
        return maxF0[adcOut];
    }

    public double getLowerDF(double cc, double cf, double adcOut, double dacInp) {
        return getDF(cc, cf, adcOut, dacInp);
    }

    public double getUpperDF(double cc, double cf, double adcOut, double dacInp) {
        return getDF(cc, cf, adcOut, dacInp);
    }

    public double getDF(double cc, double cf, double adcOut, double dacInp) {
        double cccf = cc + cf * CF2CC;
        int k = Math.min(Math.max((int) (dacInp / 256), 0), 15);
        int dacInp0 = 256 * k;
        int dacInp1 = Math.min(256 * (k + 1), 4095);
        if (dacInp == dacInp0) {
            return getDF(cccf, adcOut, k);
        } else if (dacInp == dacInp0) {
            return getDF(cccf, adcOut, k + 1);
        } else {
            double kd0 = (dacInp1 - dacInp) / (dacInp1 - dacInp0);
            double kd1 = (dacInp - dacInp0) / (dacInp1 - dacInp0);
            double v0 = getDF(cccf, adcOut, k);
            double v1 = getDF(cccf, adcOut, k + 1);
            return kd0 * v0 + kd1 * v1;
        }
    }

    public double getDF(double cccf, double adcOut, int k) {
        double[][] coeffK = coeff[k];
        if (cccf == 0) {
            return computePoly(adcOut, coeffK[0]);
        } else if (cccf == 8) {
            return computePoly(adcOut, coeffK[1]);
        } else if (cccf == 15) {
            return computePoly(adcOut, coeffK[2]);
        } else if (coeffK[0] == coeffK[1] && coeffK[0] == coeffK[2]) {
            return computePoly(adcOut, coeffK[0]);
        } else {
            double x1 = 0;
            double x2 = 8;
            double x3 = 15;
            double y1 = computePoly(adcOut, coeffK[0]);
            double y2 = computePoly(adcOut, coeffK[1]);
            double y3 = computePoly(adcOut, coeffK[2]);
            double det = (x2 - x1) * (x3 - x1) * (x2 - x3);
            double a = (y1 * (x2 - x3) + y2 * (x3 - x1) + y3 * (x1 - x2)) / det;
            double b = (y1 * (x3 - x2) * (x3 + x2 - 2 * x1) - y2 * (x3 - x1) * (x3 - x1) + y3 * (x2 - x1) * (x2 - x1)) / det;
            double dx = cccf /* - x1*/;
            return y1 + dx * (b + dx * a);
        }
    }

    private double computePoly(double adcOut, double[] coeff) {
        double x = adcOut - adc0;
        double r = 0;
        for (int j = coeff.length - 1; j >= 0; j--) {
            r = r * x + coeff[j];
        }
        return r;
    }

    @Override
    public double getLowerModelFfromAdcOut(double cc, double cf, double dacInp, int adcOut) {
        return getLowerModelF0fromAdcOut(adcOut) + getLowerDF(cc, cf, adcOut, dacInp);
    }

    @Override
    public double getUpperModelFfromAdcOut(double cc, double cf, double dacInp, int adcOut) {
        return getUpperModelF0fromAdcOut(adcOut) + getUpperDF(cc, cf, adcOut, dacInp);
    }

    @Override
    public int[] getAdcOuts() {
        return null;
    }

    @Override
    public double getF0() {
        throw new UnsupportedOperationException();
    }

    public void showF_Interval(int cc, int cf, int dacInp, String fileName) throws IOException {
        PrintWriter out = new PrintWriter(fileName);
        out.println("# adcOut lowerF upperF widF");
        for (int adcOut = minAdcOut; adcOut <= maxAdcOut; adcOut++) {
            double lowerF = getLowerModelFfromAdcOut(cc, cf, dacInp, adcOut);
            double upperF = getUpperModelFfromAdcOut(cc, cf, dacInp, adcOut);
            out.println(adcOut + " " + lowerF + " " + upperF + " " + (upperF - lowerF));
        }
        out.close();
    }

    public boolean isMonotonic0() {
        int cc = 0;
        int cf = 0;
        for (int adcOut = minAdcOut; adcOut <= maxAdcOut; adcOut++) {
            double oldDf = getDF(cc, cf, adcOut, 0);
            for (int dacInp = 1; dacInp < 4096; dacInp++) {
                double df = getDF(cc, cf, adcOut, dacInp);
                if (!(df > oldDf)) {
                    return false;
                }
                oldDf = df;
            }
        }
        return true;
    }
}
