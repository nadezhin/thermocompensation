package ru.nsc.interval.thermocompensation.model;

public class AdcRange {

    public final int minAdcOut;
    public final int maxAdcOut;

    public AdcRange(int minAdcOut, int maxAdcOut) {
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
