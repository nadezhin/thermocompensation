package ru.nsc.interval.thermocompensation.optim;

public class AdcRange {

    public int minAdcOut;
    public int maxAdcOut;

    AdcRange(int minAdcOut, int maxAdcOut) {
        if (!(0 <= minAdcOut && minAdcOut <= maxAdcOut && maxAdcOut <= 4095)) {
            throw new IllegalArgumentException();
        }
        this.minAdcOut = minAdcOut;
        this.maxAdcOut = maxAdcOut;
    }

    public boolean isIn(double adcOut) {
        return minAdcOut <= adcOut && adcOut <= maxAdcOut;
    }
}
