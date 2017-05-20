package ru.nsc.interval.thermocompensation.model;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import ru.nsc.interval.thermocompensation.parse.CapSettings;
import ru.nsc.interval.thermocompensation.parse.Parse;
import ru.nsc.interval.thermocompensation.parse.ParseSeq;
import ru.nsc.interval.thermocompensation.parse.ParseTemp;
import ru.nsc.interval.thermocompensation.spline.Polys;

/**
 *
 */
public class ChipT {

    public final String name;
    public final String runDate;
    public final double voltage;
    public final boolean badFreq;
    private final ParseTemp parseTemp;
    private final long startTime;
    final List<Parse.MeasTN> measTNs;
    final List<Parse.MeasF> measFs;
    public final CapSettings cs0;
    final List<Parse.MeasF> measF0s = new ArrayList<>();
    public final List<Parse.InpF> inpFs;
    public final int minAdcOut;
    public final int maxAdcOut;
    private double[] polyTempAdc;
    private static final double TNOM = +25;
    private double[] polyAdcTemp;
    private static final double AdcNOM = 2048;

    public ChipT(String name, String runDate, ParseTemp parseTemp, ParseSeq parseSeq, CapSettings cs0) {
        this.name = name;
        this.runDate = runDate;
        this.voltage = parseSeq.voltage;
        this.parseTemp = parseTemp;
        startTime = parseTemp != null ? parseTemp.startTime : parseSeq.getStartDate().getTime();
        measTNs = parseSeq.getMeasTN();
        polyTempAdc = approxTempAdc();
        polyAdcTemp = approxAdcTemp();
        measFs = parseSeq.getMeasF();
        inpFs = parseSeq.getInpF();
        double minAdc = Double.POSITIVE_INFINITY;
        double maxAdc = Double.NEGATIVE_INFINITY;
        this.cs0 = cs0;
        for (Parse.MeasTN mt : measTNs) {
            minAdc = Math.min(minAdc, mt.getValue());
            maxAdc = Math.max(maxAdc, mt.getValue());
        }
        minAdcOut = (int) Math.floor(minAdc);
        maxAdcOut = (int) Math.ceil(maxAdc);
        boolean badF = parseSeq.freqFailure();
        for (Parse.MeasF mf : measFs) {
            if (Double.isNaN(mf.f)) {
                badF = true;
                break;
            }
            if (mf.inp.CC == cs0.cc && mf.inp.CF == cs0.cf && mf.xt == cs0.xt) {
                measF0s.add(mf);
            }
            double adcOut = getAdcOut(mf.time);
            assert adcOut >= minAdcOut && adcOut <= maxAdcOut;
        }
        badFreq = badF;
    }

    public void printReport() {
        int countC = 0;
        for (Parse.MeasF mf : measFs) {
            if (mf.inp.CC == cs0.cc && mf.inp.CF == cs0.cf) {
                countC++;
            }
        }
        System.out.println("Chip " + name + " " + runDate + " " + voltage + "V " + cs0
                + " adc=[" + minAdcOut + "," + maxAdcOut + "] "
                + measTNs.size() + "ts "
                + measF0s.size() + "f0 "
                + countC + "fc "
                + measFs.size() + "f");
    }

    public Date getStartDate() {
        return new Date(startTime);
    }

    public boolean hasTemp() {
        return parseTemp != null && !parseTemp.timeCmds.isEmpty();
    }

    public Date getCmdDate(int k) {
        return parseTemp.getCmdDate(k);
    }

    public double getCmdTemp(int k) {
        return parseTemp.getCmdTemp(k);
    }

    public double getCmdTemp(Date time) {
        return parseTemp.getCmdTemp(time);
    }

    public double getTemp(Date time) {
        return parseTemp != null ? parseTemp.getTemp(time) : 0;
    }

    public double getMinTemp() {
        return parseTemp != null ? parseTemp.getMinTemp() : Double.POSITIVE_INFINITY;
    }

    public double getMaxTemp() {
        return parseTemp != null ? parseTemp.getMaxTemp() : Double.NEGATIVE_INFINITY;
    }

    public double getAdcOut(Date time) {
        return measTNs.isEmpty() ? 0 : ParseSeq.getValue(measTNs, time);
    }

    public double getAdcOutByCmdTemp(double temp) {
        int cnt = 0;
        double sum = 0;
        for (int k = 0; k < parseTemp.timeCmds.size() - 1; k++) {
            if (getCmdTemp(k) != temp) {
                continue;
            }
            Date time = getCmdDate(k + 1);
            sum += getAdcOut(time);
            cnt++;
        }
        return sum / cnt;
    }

    private double[] approxTempAdc() {
        double[] temp = new double[measTNs.size()];
        double[] adcOut = new double[measTNs.size()];
        for (int i = 0; i < temp.length; i++) {
            Parse.MeasTN tn = measTNs.get(i);
            Date t = tn.getDate();
            temp[i] = getTemp(t);
            adcOut[i] = getAdcOut(t);
        }
        return Polys.approxPoly(TNOM, temp, adcOut, 2);
    }

    public double getAdcOutByPoly2(double temp) {
        return Polys.calcPoly(TNOM, polyTempAdc, temp);
    }

    public int getAdcOutFloor(double temp) {
        return (int) Math.floor(getAdcOutByPoly2(temp));
    }

    public int getAdcOutCeil(double temp) {
        return (int) Math.ceil(getAdcOutByPoly2(temp));
    }

    private double[] approxAdcTemp() {
        double[] temp = new double[measTNs.size()];
        double[] adcOut = new double[measTNs.size()];
        for (int i = 0; i < temp.length; i++) {
            Parse.MeasTN tn = measTNs.get(i);
            Date t = tn.getDate();
            temp[i] = getTemp(t);
            adcOut[i] = getAdcOut(t);
        }
        return Polys.approxPoly(AdcNOM, adcOut, temp, 2);
    }

    public double getTempFromAdcOutNewByPoly2(double adc) {
        return Polys.calcPoly(AdcNOM, polyAdcTemp, adc);
    }

    public AdcRange newAdcRange() {
        return newAdcRange(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    public AdcRange newAdcRange(double minT, double maxT) {
        int minAdcOut = this.minAdcOut;
        if (minT > Double.NEGATIVE_INFINITY) {
            minAdcOut = Math.max(minAdcOut, getAdcOutFloor(maxT));
        }
        int maxAdcOut = this.maxAdcOut;
        if (maxT < Double.POSITIVE_INFINITY) {
            maxAdcOut = Math.min(maxAdcOut, getAdcOutCeil(minT));
        }
        return new AdcRange(minAdcOut, maxAdcOut);
    }

    public double getF0(Date time) {
        return measF0s.isEmpty() ? 0 : ParseSeq.getValue(measF0s, time);
    }

    public List<Parse.MeasTN> getMeasTNs() {
        return measTNs;
    }

    public List<Parse.MeasF> getMeasFs() {
        return measFs;
    }

    public List<Parse.MeasF> getMeasF0s() {
        return measF0s;
    }

    public List<Parse.MeasF> selectF(CapSettings cs) {
        List<Parse.MeasF> list = new ArrayList<>();
        for (Parse.MeasF mf : measFs) {
            if (!Double.isNaN(mf.f) && mf.inp.CC == cs.cc && mf.inp.CF == cs.cf && mf.xt == cs.xt) {
                list.add(mf);
            }
        }
        return list;
    }

    public void printInp(PolyState.Inp inp, boolean disturb, String fileName) throws IOException {
        PrintWriter out = new PrintWriter(fileName);
        out.println("# temp adcOut f0 f");
        for (Parse.InpF mf : selectByInp(inp, disturb)) {
            Date time = mf.getDate();
            double f0 = getF0(time);
            out.println(getTemp(time) + " " + getAdcOut(time) + " " + f0 + " " + mf.f);
        }
        out.close();
    }

    public List<Parse.InpF> selectByInp(PolyState.Inp inp, boolean disturb) {
        List<Parse.InpF> list = new ArrayList<Parse.InpF>();
        for (int i = 0; i < inpFs.size(); i++) {
            Parse.InpF mf = inpFs.get(i);
            if (inp.equals(mf.inp) && mf.disturb == disturb) {
                list.add(mf);
            }
        }
        return list;
    }

    public void printPpm(PolyState.Inp inp, boolean disturb, double nominalF) {
        printPpm(inp, disturb, nominalF, newAdcRange());
    }

    public void printPpmByTemp(PolyState.Inp inp, boolean disturb, double nominalF, double minT, double maxT) {
        printPpm(inp, disturb, nominalF, newAdcRange(minT, maxT));
    }

    public void printPpm(PolyState.Inp inp, boolean disturb, double nominalF, AdcRange adcRange) {
        double minF = Double.POSITIVE_INFINITY;
        double maxF = Double.NEGATIVE_INFINITY;
        Parse.InpF prevMF = null;
        double prevAdcOut = Double.NaN;
        boolean prevIn = false;
        for (Parse.InpF mf : selectByInp(inp, disturb)) {
            Date t = mf.getDate();
            double adcOut = getAdcOut(t);
            boolean in = adcRange.isIn(adcOut);
            if (in) {
                minF = Math.min(minF, mf.f);
                maxF = Math.max(maxF, mf.f);
            }
            if (prevMF != null && prevIn != in) {
                // Clip the graph
                double f;
                if (Math.min(adcOut, prevAdcOut) < adcRange.minAdcOut) {
                    assert Math.max(adcOut, prevAdcOut) >= adcRange.minAdcOut;
                    f = (adcOut - adcRange.minAdcOut) / (adcOut - prevAdcOut) * prevMF.f
                            + (adcRange.minAdcOut - prevAdcOut) / (adcOut - prevAdcOut) * mf.f;
                } else if (Math.max(adcOut, prevAdcOut) > adcRange.maxAdcOut) {
                    assert Math.min(adcOut, prevAdcOut) <= adcRange.maxAdcOut;
                    f = (adcOut - adcRange.maxAdcOut) / (adcOut - prevAdcOut) * prevMF.f
                            + (adcRange.maxAdcOut - prevAdcOut) / (adcOut - prevAdcOut) * mf.f;

                } else {
                    throw new AssertionError();
                }
                minF = Math.min(minF, f);
                maxF = Math.max(maxF, f);
            }
            prevMF = mf;
            prevIn = in;
            prevAdcOut = adcOut;
        }
        double pmin = (minF / nominalF - 1) * 1e6;
        double pmax = (maxF / nominalF - 1) * 1e6;
        System.out.println("ppm="
                + Math.ceil(Math.max(-pmin, pmax) * 100) / 100 + " ["
                + Math.floor(pmin * 100) / 100 + ","
                + Math.ceil(pmax * 100) / 100 + "] wid="
                + Math.ceil((pmax - pmin) * 100) / 100);
    }

    public void printInp2(PolyState.Inp inp, double nominalF, String fileName) throws IOException {
        List<Parse.InpF> listF = new ArrayList<>();
        List<Parse.InpF> listT = new ArrayList<>();
        for (int i = 0; i < inpFs.size(); i++) {
            Parse.InpF mf = inpFs.get(i);
            if (!inp.equals(mf.inp)) {
                continue;
            }
            (mf.disturb ? listT : listF).add(mf);
        }
        PrintWriter out = new PrintWriter(fileName);
        out.println("# temp adcOut dacInp f0 f fd df ppm ppmd");
        for (int i = 0; i < listF.size(); i++) {
            Parse.InpF mf = listF.get(i);
            Date time = mf.getDate();
            double f0 = getF0(time);
            double f = mf.f;
            double fd = !listT.isEmpty() ? ParseSeq.getValue(listT, time) : 0;
            int adcOutNew = Math.min(Math.max((int) Math.rint(getAdcOut(time)), 0), 4095);
            mf.inp.T = adcOutNew;
            int dacInp = PolyModel.compute(mf.inp);
            out.println(getTemp(time) + " "
                    + getAdcOut(time) + " "
                    + dacInp + " "
                    + f0 + " "
                    + f + " "
                    + fd + " "
                    + (fd - f) + " "
                    + (f / nominalF - 1) * 1e6 + " "
                    + (fd / nominalF - 1) * 1e6);
        }
        out.close();
    }
}
