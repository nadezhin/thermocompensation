package ru.nsc.interval.thermocompensation.parse;

/**
 *
 */
public class AbstractMeasureItem {

    public static final Inp inpI = new Inp();
    public static final Inp inpID = new Inp();

    public static class Inp extends AbstractMeasureItem {

        private Inp() {
        }
    }
}
