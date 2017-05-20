package ru.nsc.interval.thermocompensation.model;

import java.util.Arrays;
import ru.nsc.interval.thermocompensation.parse.Parse;

/**
 *
 */
public class ChipRefine implements ChipModel {

    private final ChipExpress base;
    private final ChipT inpMeas;
    private final double[] minF0 = new double[4096];
    private final double[] maxF0 = new double[4096];
    final int minAdcOut;
    final int maxAdcOut;

    public ChipRefine(ChipExpress base, ChipT inpMeas, PolyState.Inp inp) {
        this.base = base;
        this.inpMeas = inpMeas;
        Arrays.fill(minF0, Double.POSITIVE_INFINITY);
        Arrays.fill(maxF0, Double.NEGATIVE_INFINITY);
        Parse.InpF oldMf = null;
        double oldAdcOutD = Double.NaN;
        for (Parse.InpF mf : inpMeas.selectByInp(inp, false)) {
            assert inp.equals(inp);
            double adcOutD = inpMeas.getAdcOut(mf.time);
            int adcOut = Math.min(Math.max((int) Math.rint(adcOutD), 0), 4095);
            inp.T = adcOut;
            int dacInp = PolyModel.compute(inp);
            double f0 = mf.f - base.getDF(inp.CC, inp.CF, adcOut, dacInp);
            minF0[adcOut] = Math.min(minF0[adcOut], f0);
            maxF0[adcOut] = Math.max(maxF0[adcOut], f0);
            if (oldMf != null) {
                int n = 2 + (int) Math.abs(adcOutD - oldAdcOutD) * 3;
                for (int i = 1; i < n; i++) {
                    double aD = (oldAdcOutD * (n - i) + adcOutD * i) / n;
                    int a = Math.min(Math.max((int) Math.rint(aD), 0), 4095);
                    double f = (oldMf.f * (n - i) + mf.f * i) / n;
                    inp.T = a;
                    dacInp = PolyModel.compute(inp);
                    f0 = f - base.getDF(inp.CC, inp.CF, adcOut, dacInp);
                    minF0[a] = Math.min(minF0[a], f0);
                    maxF0[a] = Math.max(maxF0[a], f0);
                }
            }
            oldMf = mf;
            oldAdcOutD = adcOutD;
        }
        int adcOut = base.minAdcOut;
        while (minF0[adcOut] > maxF0[adcOut]) {
            adcOut++;
        }
        minAdcOut = adcOut;
        for (adcOut = base.minAdcOut; adcOut < minAdcOut; adcOut++) {
            inp.T = minAdcOut;
            int dacInp = PolyModel.compute(inp);
            double fl = getLowerModelFfromAdcOut(inp.CC, inp.CF, dacInp, minAdcOut);
            double fu = getUpperModelFfromAdcOut(inp.CC, inp.CF, dacInp, minAdcOut);
            inp.T = adcOut;
            dacInp = PolyModel.compute(inp);
            minF0[adcOut] = fl - base.getDF(inp.CC, inp.CF, adcOut, dacInp);
            maxF0[adcOut] = fu - base.getDF(inp.CC, inp.CF, adcOut, dacInp);
        }
        adcOut = base.maxAdcOut;
        while (minF0[adcOut] > maxF0[adcOut]) {
            adcOut--;
        }
        maxAdcOut = adcOut;
        for (adcOut = base.maxAdcOut; adcOut > maxAdcOut; adcOut--) {
            inp.T = maxAdcOut;
            int dacInp = PolyModel.compute(inp);
            double fl = getLowerModelFfromAdcOut(inp.CC, inp.CF, dacInp, maxAdcOut);
            double fu = getUpperModelFfromAdcOut(inp.CC, inp.CF, dacInp, maxAdcOut);
            inp.T = adcOut;
            dacInp = PolyModel.compute(inp);
            minF0[adcOut] = fl - base.getDF(inp.CC, inp.CF, adcOut, dacInp);
            maxF0[adcOut] = fu - base.getDF(inp.CC, inp.CF, adcOut, dacInp);
        }
    }

    @Override
    public double getLowerModelFfromAdcOut(double cc, double cf, double dacInp, int adcOut) {
        return minF0[adcOut] + base.getDF(cc, cf, adcOut, dacInp);
    }

    @Override
    public double getUpperModelFfromAdcOut(double cc, double cf, double dacInp, int adcOut) {
        return maxF0[adcOut] + base.getDF(cc, cf, adcOut, dacInp);
    }

    @Override
    public int[] getAdcOuts() {
        return null;
    }

    public boolean isMonotonic0() {
        return base.isMonotonic0();
    }

    public AdcRange newAdcRange() {
        return newAdcRange(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    public AdcRange newAdcRange(double minT, double maxT) {
        int minAdcOut = this.minAdcOut;
        if (minT > Double.NEGATIVE_INFINITY) {
            minAdcOut = Math.max(minAdcOut, inpMeas.getAdcOutFloor(maxT));
        }
        int maxAdcOut = this.maxAdcOut;
        if (maxT < Double.POSITIVE_INFINITY) {
            maxAdcOut = Math.min(maxAdcOut, inpMeas.getAdcOutCeil(minT));
        }
        return new AdcRange(minAdcOut, maxAdcOut);
    }
}
