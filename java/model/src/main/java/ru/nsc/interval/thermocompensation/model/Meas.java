package ru.nsc.interval.thermocompensation.model;

/**
 * A single measurement
 */
public class Meas {

    /**
     * Chip number starting with 0
     */
    public final int chipNo;
    /**
     * Output of the temperature sensor. Input of the Pol) [0..2047]
     */
    public final int adcOut;
    /**
     * Required input of the digital-analog convertor. Required output of the
     * Poly [0..2047]
     */
    public final int dacInp;
    public final double f;
    public final double df_du;

    Meas(int chipNo, int adcOut, int dacInp, double f, double df_du) {
        this.chipNo = chipNo;
        this.adcOut = adcOut;
        this.dacInp = dacInp;
        this.f = f;
        this.df_du = 1;
    }
}
