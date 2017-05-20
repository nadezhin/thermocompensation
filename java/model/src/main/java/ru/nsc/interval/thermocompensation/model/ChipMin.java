package ru.nsc.interval.thermocompensation.model;

/**
 *
 */
public class ChipMin {

    public final ChipModel chipModel;
    public final double f0;
    public final int cc;
    public final int cf;
    public double possibleDF;
    public int numAdcOuts;

    private final int[] adcOuts;
    private final int[] minU;
    private final double[] minDF;

    public ChipMin(ChipModel chipModel, double f0, int cc, int cf) {
        this.chipModel = chipModel;
        this.f0 = f0;
        this.cc = cc;
        this.cf = cf;
        adcOuts = chipModel.getAdcOuts();
        numAdcOuts = adcOuts.length;
        minU = new int[numAdcOuts];
        minDF = new double[numAdcOuts];
        double pdf = 0;
        for (int i = 0; i < numAdcOuts; i++) {
            int adcOut = adcOuts[i];
            int mu = -1;
            double mdf = Double.POSITIVE_INFINITY;
            for (int dacIn = 0; dacIn < 0x1000; dacIn++) {
                double df = getDF(dacIn, adcOut);
                if (df < mdf) {
                    mu = dacIn;
                    mdf = df;
                }
            }
            minU[i] = mu;
            minDF[i] = mdf;
            pdf = Math.max(pdf, mdf);
            for (int dacIn = mu - 1; dacIn >= 0; dacIn--) {
                if (!(getDF(dacIn, adcOut) >= getDF(dacIn + 1, adcOut))) {
                    throw new IllegalArgumentException();
                }
            }
            for (int dacIn = mu + 1; dacIn < 0x1000; dacIn++) {
                if (!(getDF(dacIn, adcOut) >= getDF(dacIn - 1, adcOut))) {
                    throw new IllegalArgumentException();
                }
            }
        }
        possibleDF = pdf;
    }

    public int[] getAdcOus() {
        return adcOuts.clone();
    }

    public double getDF(int dacIn, int adcOut) {
        double lf = chipModel.getLowerModelFfromAdcOut(cc, cf, dacIn, adcOut);
        double uf = chipModel.getUpperModelFfromAdcOut(cc, cf, dacIn, adcOut);
        return Math.max(Math.abs(lf - f0), Math.abs(uf - f0));
    }

    public void getBoundsWeak(double df, int[] l, int[] u) {
        if (l.length != minU.length || u.length != minU.length) {
            throw new IllegalArgumentException();
        }
        if (!(df >= possibleDF)) {
            throw new IllegalArgumentException();
        }
        for (int i = 0; i < numAdcOuts; i++) {
            int adcOut = adcOuts[i];
            int dacIn = minU[i];
            while (dacIn > 0 && getDF(dacIn - 1, adcOut) <= df) {
                dacIn--;
            }
            l[i] = dacIn;
            dacIn = minU[i];
            while (dacIn < 0xFFF && getDF(dacIn + 1, adcOut) <= df) {
                dacIn++;
            }
            u[i] = dacIn;
        }
    }

    public void getBoundsStrong(double df, int[] l, int[] u) {
        if (l.length != minU.length || u.length != minU.length) {
            throw new IllegalArgumentException();
        }
        if (!(df > possibleDF)) {
            throw new IllegalArgumentException();
        }
        for (int i = 0; i < numAdcOuts; i++) {
            int adcOut = adcOuts[i];
            int dacIn = minU[i];
            while (dacIn > 0 && getDF(dacIn - 1, adcOut) < df) {
                dacIn--;
            }
            l[i] = dacIn;
            dacIn = minU[i];
            while (dacIn < 0xFFF && getDF(dacIn + 1, adcOut) < df) {
                dacIn++;
            }
            u[i] = dacIn;
        }
    }

}
