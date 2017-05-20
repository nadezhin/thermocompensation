package ru.nsc.interval.thermocompensation.optim;

/**
 *
 */
public interface ChipModel {

    public double getLowerModelFfromAdcOut(double cc, double cf, double dacInp, int adcOut);

    public double getUpperModelFfromAdcOut(double cc, double cf, double dacInp, int adcOut);

    public int[] getAdcOuts();

    public static final double CF2CC = 0.034;
}
