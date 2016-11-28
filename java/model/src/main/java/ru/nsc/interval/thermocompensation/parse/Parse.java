package ru.nsc.interval.thermocompensation.parse;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import ru.nsc.interval.thermocompensation.model.PolyState;

/**
 *
 */
public class Parse {

//    private static DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.FULL, Locale.ROOT);
    double voltage;
    double Uc = 800;
    private String date;
    private String time0str;
    private Date time0;
    private boolean disableFilter = false;
    final List<Line> allLines = new ArrayList<>();
    final List<MeasTd> measTds = new ArrayList<>();
    final List<MeasVadc> measVadcs = new ArrayList<>();
    final List<MeasV> measVs = new ArrayList<>();
    final List<MeasVinp> measVinps = new ArrayList<>();

    public static class Line {

        public final Date time;

        private Line(Date time) {
            this.time = time;
        }

        protected void parseC(int cmd) {
            throw new UnsupportedOperationException();
        }

        protected boolean optC(int cmd) {
            return false;
        }

        protected void parseW() {
            throw new UnsupportedOperationException();
        }

        protected PolyState.Inp parseR() {
            throw new UnsupportedOperationException();
        }

        protected void parseA(int addr) {
            throw new UnsupportedOperationException();
        }

        protected void parseX(int val) {
            throw new UnsupportedOperationException();
        }

        protected void parseY(String var, int val) {
            throw new UnsupportedOperationException();
        }

        protected double parseV() {
            throw new UnsupportedOperationException();
        }

        protected double parseF() {
            throw new UnsupportedOperationException();
        }
    }

    public static class LineA extends Line {

        public final int addr;

        LineA(Date time, int addr) {
            super(time);
            this.addr = addr;
        }

        @Override
        public void parseA(int addr) {
            assert this.addr == addr;
        }
    }

    public static class LineC extends Line {

        public final int cmd;

        LineC(Date time, int cmd) {
            super(time);
            this.cmd = cmd;
        }

        @Override
        protected void parseC(int cmd) {
            assert this.cmd == cmd;
        }

        @Override
        protected boolean optC(int cmd) {
            return this.cmd == cmd;
        }
    }

    public static class LineW extends Line {

        public final PolyState.Inp inp;

        LineW(Date time, PolyState.Inp inp) {
            super(time);
            this.inp = inp;
        }

        @Override
        public void parseW() {
        }
    }

    public static class LineX extends Line {

        final int val;

        LineX(Date time, int val) {
            super(time);
            this.val = val;
        }

        @Override
        public void parseX(int val) {
            assert this.val == val;
        }
    }

    public static class LineY extends Line {

        final String var;
        final int val;

        LineY(Date time, String var, int val) {
            super(time);
            this.var = var;
            this.val = val;
        }

        @Override
        public void parseY(String var, int val) {
            assert this.var.equals(var) && this.val == val;
        }
    }

    public static class LineV extends Line {

        public final double v;

        public LineV(Date time, double v) {
            super(time);
            this.v = v;
        }

        @Override
        public double parseV() {
            return v;
        }
    }

    public static class LineF extends Line {

        public final double f;

        LineF(Date time, double f) {
            super(time);
            this.f = f;
        }

        @Override
        public double parseF() {
            return f;
        }
    }

    public static class LineR extends Line {

        public final PolyState.Inp inp;

        LineR(Date time, PolyState.Inp inp) {
            super(time);
            this.inp = inp;
        }

        @Override
        public PolyState.Inp parseR() {
            return inp;
        }
    }

    public static class LineEq extends Line {

        final String var1, var2, var3;
        final double k;
        final int result;

        LineEq(Date time, String var1, String var2, String var3, double k, int result) {
            super(time);
            this.var1 = var1;
            this.var2 = var2;
            this.var3 = var3;
            this.k = k;
            this.result = result;
        }
    }

    public static class LineT extends Line {

        public final int adcOut;

        LineT(Date time, int adcOut) {
            super(time);
            this.adcOut = adcOut;
        }
    }

    public static class LineS extends Line {

        public final int chipNo;

        LineS(Date time, int chipNo) {
            super(time);
            this.chipNo = chipNo;
        }
    }

    public static class LineG extends LineF {

        LineG(Date time, double f) {
            super(time, f);
        }
    }

    public static class LineO extends Line {

        final int Uc;

        LineO(Date time, int Uc) {
            super(time);
            this.Uc = Uc;
        }
    }

    public static abstract class Meas {

        public abstract long getTime();

        public abstract double getValue();

        public Date getDate() {
            return new Date(getTime());
        }
    }

    public static class MeasTN extends Meas {

        public final LineT lineT;

        public MeasTN(LineT lineT) {
            this.lineT = lineT;
        }

        @Override
        public long getTime() {
            return lineT.time.getTime();
        }

        @Override
        public double getValue() {
            return lineT.adcOut;
        }
    }

    public static class MeasV extends Meas {

        public final int xt;
        public final LineV[] vals;

        public MeasV(int xt, LineV[] vals) {
            this.xt = xt;
            this.vals = vals;
        }

        @Override
        public long getTime() {
            long t0 = vals[0].time.getTime();
            long ts = 0;
            for (int i = 1; i < vals.length; i++) {
                LineV line = vals[i];
                ts += (line.time.getTime() - t0);
            }
            return t0 + (ts + vals.length / 2) / vals.length;
        }

        @Override
        public double getValue() {
            double s = 0;
            for (LineV line : vals) {
                s += line.v;
            }
            return s / vals.length;
        }
    }

    public static class MeasVadc extends Meas {

        public final LineV[] vals;

        public MeasVadc(LineV[] vals) {
            this.vals = vals;
        }

        @Override
        public long getTime() {
            long t0 = vals[0].time.getTime();
            long ts = 0;
            for (int i = 1; i < vals.length; i++) {
                LineV line = vals[i];
                ts += (line.time.getTime() - t0);
            }
            return t0 + (ts + vals.length / 2) / vals.length;
        }

        @Override
        public double getValue() {
            return getVadc();
        }

        public double getVadc() {
            double s = 0;
            for (LineV line : vals) {
                s += line.v;
            }
            return s / vals.length;
        }
    }

    public static class MeasVinp extends Meas {

        public final PolyState.Inp inp;
        public final LineV[] vals;
        public final boolean disturb;

        public MeasVinp(PolyState.Inp inp, LineV[] vals, boolean disturb) {
            this.inp = inp;
            this.vals = vals;
            this.disturb = disturb;
        }

        @Override
        public long getTime() {
            long t0 = vals[0].time.getTime();
            long ts = 0;
            for (int i = 1; i < vals.length; i++) {
                LineV line = vals[i];
                ts += (line.time.getTime() - t0);
            }
            return t0 + (ts + vals.length / 2) / vals.length;
        }

        @Override
        public double getValue() {
            double s = 0;
            for (LineV line : vals) {
                s += line.v;
            }
            return s / vals.length;
        }
    }

    public static class MeasTd extends Meas {

        public final LineV[] vals;

        public MeasTd(LineV[] vals) {
            this.vals = vals;
        }

        @Override
        public long getTime() {
            long t0 = vals[0].time.getTime();
            long ts = 0;
            for (int i = 1; i < vals.length; i++) {
                LineV line = vals[i];
                ts += (line.time.getTime() - t0);
            }
            return t0 + (ts + vals.length / 2) / vals.length;
        }

        @Override
        public double getValue() {
            double s = 0;
            for (LineV line : vals) {
                s += line.v;
            }
            return s / vals.length;
        }
    }

    public static class MeasF extends Meas {

        public final Date time;
        public final int xt;
        public final PolyState.Inp inp;
        public final int Uc;
        public final LineF[] fs;
        public final double f;

        public MeasF(Date time, int xt, PolyState.Inp inp, int Uc, LineF[] fs, double f) {
            this.time = time;
            this.xt = xt;
            this.inp = inp;
            this.Uc = Uc;
            this.fs = fs;
            this.f = f;
        }

        @Override
        public long getTime() {
            return time.getTime();
        }

        @Override
        public double getValue() {
            return f;
        }

        public int getCC() {
            return inp.CC;
        }

        public int getCF() {
            return inp.CF;
        }
    }

    public static class InpF extends Meas {

        public final Date time;
        public final PolyState.Inp inp;
        public final LineF[] fs;
        public final double f;
        public final boolean disturb;

        public InpF(Date time, PolyState.Inp inp, LineF[] fs, double f, boolean disturb) {
            this.time = time;
            assert inp != null;
            this.inp = inp;
            this.fs = fs;
            this.f = f;
            this.disturb = disturb;
        }

        @Override
        public long getTime() {
            return time.getTime();
        }

        @Override
        public double getValue() {
            return f;
        }
    }

    private static Date parseTime(String s) {
        assert s.length() == 12 && s.charAt(2) == ':' && s.charAt(5) == ':' && s.charAt(8) == '.';
        Calendar cal = Calendar.getInstance(Locale.ROOT);
        cal.set(Calendar.HOUR_OF_DAY, Integer.valueOf(s.substring(0, 2)));
        cal.set(Calendar.MINUTE, Integer.valueOf(s.substring(3, 5)));
        cal.set(Calendar.SECOND, Integer.valueOf(s.substring(6, 8)));
        cal.set(Calendar.MILLISECOND, Integer.valueOf(s.substring(9, 12)));
        return cal.getTime();
    }

    public static Date parseDateAndTime(String date, String time) {
        assert date.length() == 5 && date.charAt(2) == '-' || date.length() == 10 && date.charAt(2) == '-' && date.charAt(5) == '-';
        assert time.length() == 12 && time.charAt(2) == ':' && time.charAt(5) == ':' && time.charAt(8) == '.';
        Calendar cal = Calendar.getInstance(Locale.ROOT);
        if (date.length() == 10) {
            cal.set(Calendar.YEAR, Integer.valueOf(date.substring(6)));
        }
        cal.set(Calendar.MONTH, Integer.valueOf(date.substring(3, 5)) - 1);
        cal.set(Calendar.DAY_OF_MONTH, Integer.valueOf(date.substring(0, 2)));
        cal.set(Calendar.HOUR_OF_DAY, Integer.valueOf(time.substring(0, 2)));
        cal.set(Calendar.MINUTE, Integer.valueOf(time.substring(3, 5)));
        cal.set(Calendar.SECOND, Integer.valueOf(time.substring(6, 8)));
        cal.set(Calendar.MILLISECOND, Integer.valueOf(time.substring(9, 12)));
        return cal.getTime();
    }

    public Parse(File file) throws IOException, ParseException {
        LineNumberReader inp = new LineNumberReader(new FileReader(file));
        String[] ss;
        String s = inp.readLine();
        if (s.startsWith("Programmator found.")) {
            s = inp.readLine();
        }
        if (s.startsWith("# version ")) {
            s = inp.readLine();
        }
        assert s.startsWith("# Power on");
        ss = s.split(" +");
        assert ss.length == 8;
        assert ss[0].equals("#") && ss[1].equals("Power") && ss[2].equals("on");
        voltage = 3.3;
        if (ss.length == 8) {
            assert ss[3].equals("V=3.3") || ss[3].equals("V=5");
            if (ss[3].equals("V=5")) {
                voltage = 5.0;
            };
            assert ss[4].equals("Uc=");
            Uc = Double.parseDouble(ss[5]);
            date = ss[6];
            time0str = ss[7];
        }

        time0 = parseDateAndTime(date, time0str);
//        time0 = timeFormat.parse(time0str);
        s = inp.readLine();
        if (s.startsWith("# devName=")) {
            s = inp.readLine();
        }
        if (s.equals("# Disable filter")) {
            disableFilter = true;
            s = inp.readLine();
        }
        Date oldTime = time0;
        try {
            while (s != null && !s.equals("# Power off")) {
                if (s.startsWith("#  ")) {
//                    System.out.println(s);
                    s = inp.readLine();
                    continue;
                }
                ss = s.split(" +");

                Date time = parseDateAndTime(ss[0], ss[1]);
                assert oldTime.getTime() <= time.getTime();
                oldTime = time;
                assert ss[2].length() == 1;
                Line line = null;
                switch (ss[2].charAt(0)) {
                    case 'A':
                        assert ss.length == 5 && ss[4].equals("1");
                        line = new LineA(time, Integer.parseInt(ss[3]));
                        break;
                    case 'C':
                        assert ss.length == 5 && ss[4].equals("1");
                        line = new LineC(time, Integer.parseInt(ss[3]));
                        break;
                    case 'W':
                        if (ss.length == 5) {
                            assert ss.length == 5 && ss[3].equals("0") && ss[4].equals("1");
                            line = new LineW(time, null);
                        } else {
                            assert ss.length == 15 && ss[3].equals("0");
                            PolyState.Inp coeff = PolyState.Inp.genNom();
                            coeff.ENwork = 1;
                            coeff.INF = Integer.parseInt(ss[4], 2);
                            coeff.SBIT = Integer.parseInt(ss[5], 2);
                            coeff.K1BIT = Integer.parseInt(ss[6], 2);
                            coeff.K2BIT = Integer.parseInt(ss[7], 2);
                            coeff.K3BIT = Integer.parseInt(ss[8], 2);
                            coeff.K4BIT = Integer.parseInt(ss[9], 2);
                            coeff.K5BIT = Integer.parseInt(ss[10], 2);
                            coeff.CF = Integer.parseInt(ss[11], 2);
                            coeff.CC = Integer.parseInt(ss[12], 2);
                            coeff.DIV = Integer.parseInt(ss[13], 2);
                            coeff.SENSE = Integer.parseInt(ss[14], 2);
                            line = new LineW(time, coeff);
                        }
                        break;
                    case 'D':
                        line = new Line(time);
                        break;
                    case 'R':
                        PolyState.Inp coeff = PolyState.Inp.genNom();
                        coeff.ENwork = 1;
                        coeff.INF = Integer.parseInt(ss[4], 2);
                        coeff.SBIT = Integer.parseInt(ss[5], 2);
                        coeff.K1BIT = Integer.parseInt(ss[6], 2);
                        coeff.K2BIT = Integer.parseInt(ss[7], 2);
                        coeff.K3BIT = Integer.parseInt(ss[8], 2);
                        coeff.K4BIT = Integer.parseInt(ss[9], 2);
                        coeff.K5BIT = Integer.parseInt(ss[10], 2);
                        coeff.CF = Integer.parseInt(ss[11], 2);
                        coeff.CC = Integer.parseInt(ss[12], 2);
                        coeff.DIV = Integer.parseInt(ss[13], 2);
                        coeff.SENSE = Integer.parseInt(ss[14], 2);
                        line = new LineR(time, coeff);
                        break;
                    case 'V':
                        assert ss.length == 6;
                        assert ss[3].equals("0");
                        line = new LineV(time, Double.parseDouble(ss[5]));
                        break;
                    case 'F':
                        assert ss.length == 6;
                        assert ss[3].equals("0");
                        line = new LineF(time, Double.parseDouble(ss[5]));
                        break;
                    case 'X':
                        assert ss.length == 5 && ss[4].equals("1");
                        line = new LineX(time, Integer.parseInt(ss[3]));
                        break;
                    case 'Y':
                        assert ss.length == 7 && ss[3].equals("0") && ss[6].equals("1");
                        line = new LineY(time, ss[4], Integer.parseInt(ss[5]));
                        break;
                    case '=':
                        assert ss.length == 9 && ss[3].equals("0");
                        line = new LineEq(time, ss[4], ss[5], ss[6], Double.parseDouble(ss[7]), Integer.parseInt(ss[8]));
                        break;
                    case 'T':
                        assert ss.length == 5 && ss[3].equals("0");
                        line = new LineT(time, Integer.parseInt(ss[4]));
                        break;
                    case 'S':
                        assert ss.length == 4;
                        line = new LineS(time, Integer.parseInt(ss[3]));
                        break;
                    case 'G':
                        assert ss.length == 4;
                        double f;
                        if (ss[3].equals("null")) {
//                            System.out.println("Failure of chip");
                            f = Double.NaN;
                        } else if (ss[3].endsWith("MHz")) {
                            f = Double.parseDouble(ss[3].substring(0, ss[3].length() - 3)) * 1e6;
                        } else if (ss[3].endsWith("kHz")) {
                            f = Double.parseDouble(ss[3].substring(0, ss[3].length() - 3)) * 1e3;
                        } else {
                            assert ss[3].endsWith("Hz");
                            f = Double.parseDouble(ss[3].substring(0, ss[3].length() - 2));
                        }
                        line = new LineG(time, f);
                        break;
                    case 'O':
                        assert ss.length == 4;
                        line = new LineO(time, Integer.parseInt(ss[3]));
                        break;
                    default:
                        throw new AssertionError();
                }
                allLines.add(line);
                s = inp.readLine();
            }
        } catch (NumberFormatException e) {
            System.out.println(s);
            e.printStackTrace();
            s = null;
        } catch (AssertionError e) {
            s = null;
        }
        if (s == null) {
            System.out.println("INCOMPLETE " + file);
        }
        inp.close();

        List<LineV> vals = new ArrayList<>();
        int curA = -1;
        int curX = -1;
        PolyState.Inp curInp = null;
        for (Line line : allLines) {
            if (line instanceof LineA) {
                if (!vals.isEmpty()) {
                    LineV[] lineVs = vals.toArray(new LineV[vals.size()]);
                    switch (curA) {
                        case 1:
                            measTds.add(new MeasTd(lineVs));
                            break;
                        case 3:
                            measVs.add(new MeasV(curX, lineVs));
                            break;
                        case 0:
                        case 7:
                            measVinps.add(new MeasVinp(curInp, lineVs, curA != 0));
                            break;
                        case 14:
                            measVadcs.add(new MeasVadc(lineVs));
                            break;
                    }
                    vals.clear();
                }
                curA = ((LineA) line).addr;
            } else if (line instanceof LineX) {
                if (!vals.isEmpty() && curA == 3) {
                    measVs.add(new MeasV(curX, vals.toArray(new LineV[vals.size()])));
                    vals.clear();
                }
                curX = ((LineX) line).val;
            } else if (line instanceof LineY) {
                if (!vals.isEmpty() && curA == 3) {
                    measVs.add(new MeasV(curX, vals.toArray(new LineV[vals.size()])));
                    vals.clear();
                }
                curX = ((LineY) line).val;
            } else if (line instanceof LineW) {
                if (!vals.isEmpty() && (curA == 0 || curA == 7)) {
                    measVinps.add(new MeasVinp(curInp, vals.toArray(new LineV[vals.size()]), curA != 0));
                    vals.clear();
                }
                curInp = ((LineW) line).inp;
            } else if (line instanceof LineR) {
                if (!vals.isEmpty() && (curA == 0 || curA == 7)) {
                    measVinps.add(new MeasVinp(curInp, vals.toArray(new LineV[vals.size()]), curA != 0));
                    vals.clear();
                }
                curInp = ((LineR) line).inp;
            } else if (line instanceof LineV) {
                vals.add((LineV) line);
            }
        }
        if (!vals.isEmpty()) {
            LineV[] lineVs = vals.toArray(new LineV[vals.size()]);
            switch (curA) {
                case 1:
                    measTds.add(new MeasTd(lineVs));
                    break;
                case 3:
                    measVs.add(new MeasV(curX, lineVs));
                    break;
                case 0:
                case 7:
                    measVinps.add(new MeasVinp(curInp, lineVs, curA != 0));
                    break;
                case 14:
                    measVadcs.add(new MeasVadc(lineVs));
                    break;
            }
            vals.clear();
        }
        for (int i = 1; i < measVs.size(); i++) {
            MeasV prev = measVs.get(i - 1);
            MeasV next = measVs.get(i);
            assert next.vals[0].time.getTime() >= prev.vals[prev.vals.length - 1].time.getTime();
        }
        for (int i = 1; i < measVadcs.size(); i++) {
            MeasVadc prev = measVadcs.get(i - 1);
            MeasVadc next = measVadcs.get(i);
            assert next.vals[0].time.getTime() >= prev.vals[prev.vals.length - 1].time.getTime();
        }
        for (int i = 1; i < measVinps.size(); i++) {
            MeasVinp prev = measVinps.get(i - 1);
            MeasVinp next = measVinps.get(i);
            assert next.vals[0].time.getTime() >= prev.vals[prev.vals.length - 1].time.getTime();
        }
    }

    private static String rev(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = s.length() - 1; i >= 0; i--) {
            sb.append(s.charAt(i));
        }
        return sb.toString();
    }

    public String getDateString() {
        return date;
    }

    public Date getTime0() {
        return time0;
    }

    public String getTime0String() {
        return time0str;
    }

    public boolean isDisableFilter() {
        return disableFilter;
    }

    public List<Line> getLines() {
        return allLines;
    }

    public List<MeasV> getMeasV() {
        return measVs;
    }

    public List<MeasTd> getMeasTds() {
        return measTds;
    }

    public List<MeasVadc> getMeasVadc() {
        return measVadcs;
    }

    public List<MeasVinp> getMeasVinp() {
        return measVinps;
    }

    public List<MeasTN> getMeasTN(int chipNo) {
        List<MeasTN> list = new ArrayList<>();
        int curChipNo = 0;
        int lastCmd = -1;
        for (Line line : allLines) {
            if (line instanceof LineS) {
                curChipNo = ((LineS) line).chipNo;
            } else if (line instanceof LineC) {
                lastCmd = ((LineC) line).cmd;
            } else if (line instanceof LineT) {
                if (lastCmd == 27 && curChipNo == chipNo) {
                    list.add(new MeasTN((LineT) line));
                }
            }
        }
        return list;
    }

    public List<MeasF> getMeasF(int chipNo) {
        List<MeasF> list = new ArrayList<>();
        List<LineF> vals = new ArrayList<>();
        boolean vppOn = false;
        int curChipNo = 0;
        int curA = -1;
        int curX = -1;
        PolyState.Inp[] curInp = new PolyState.Inp[64];
        int curUc = (int) Uc;
        for (Line line : allLines) {
            if (line instanceof Parse.LineC) {
                int cmd = ((Parse.LineC) line).cmd;
                switch (cmd) {
                    case 0x08:
                        assert !vppOn;
                        vppOn = true;
                        break;
                    case 0x09:
                        assert vppOn;
                        vppOn = false;
                        break;
                }
            } else if (line instanceof LineS) {
                if (!vals.isEmpty()) {
                    LineF last = vals.get(vals.size() - 1);
                    list.add(new MeasF(last.time, curX, curInp[curChipNo], curUc, vals.toArray(new LineF[vals.size()]), last.f));
                    vals.clear();
                }
                curChipNo = ((LineS) line).chipNo;
                curA = -1;
                curX = -1;
                if (curChipNo == -1) {
                    System.out.println("Can't handle chip -1");
                    break;
                }
            } else if (line instanceof LineW && vppOn) {
                if (!vals.isEmpty()) {
                    LineF last = vals.get(vals.size() - 1);
                    list.add(new MeasF(last.time, curX, curInp[curChipNo], curUc, vals.toArray(new LineF[vals.size()]), last.f));
                    vals.clear();
                }
                curInp[curChipNo] = ((LineW) line).inp;
            } else if (line instanceof LineR) {
                if (!vals.isEmpty()) {
                    LineF last = vals.get(vals.size() - 1);
                    list.add(new MeasF(last.time, curX, curInp[curChipNo], curUc, vals.toArray(new LineF[vals.size()]), last.f));
                    vals.clear();
                }
                curInp[curChipNo] = ((LineR) line).inp;
            } else if (line instanceof LineA) {
                if (!vals.isEmpty()) {
                    LineF last = vals.get(vals.size() - 1);
                    list.add(new MeasF(last.time, curX, curInp[curChipNo], curUc, vals.toArray(new LineF[vals.size()]), last.f));
                    vals.clear();
                }
                curA = ((LineA) line).addr;
            } else if (line instanceof LineX) {
                if (!vals.isEmpty()) {
                    LineF last = vals.get(vals.size() - 1);
                    list.add(new MeasF(last.time, curX, curInp[curChipNo], curUc, vals.toArray(new LineF[vals.size()]), last.f));
                    vals.clear();
                }
                curX = ((LineX) line).val;
            } else if (line instanceof LineY) {
                if (!vals.isEmpty()) {
                    LineF last = vals.get(vals.size() - 1);
                    list.add(new MeasF(last.time, curX, curInp[curChipNo], curUc, vals.toArray(new LineF[vals.size()]), last.f));
                    vals.clear();
                }
                curX = ((LineY) line).val;
            } else if (line instanceof LineO) {
                if (!vals.isEmpty()) {
                    LineF last = vals.get(vals.size() - 1);
                    list.add(new MeasF(last.time, curX, curInp[curChipNo], curUc, vals.toArray(new LineF[vals.size()]), last.f));
                    vals.clear();
                }
                curUc = ((LineO) line).Uc;
            } else if (line instanceof LineF && curA == 3 && curChipNo == chipNo) {
                vals.add((LineF) line);
            }
        }
        if (!vals.isEmpty()) {
            LineF last = vals.get(vals.size() - 1);
            list.add(new MeasF(last.time, curX, curInp[curChipNo], curUc, vals.toArray(new LineF[vals.size()]), last.f));
            vals.clear();
        }
        for (int i = 1; i < list.size(); i++) {
            MeasF prev = list.get(i - 1);
            MeasF next = list.get(i);
            assert next.fs[0].time.getTime() >= prev.fs[prev.fs.length - 1].time.getTime();
        }
        return list;
    }

    public List<InpF> getInpF(int chipNo) {
        List<InpF> list = new ArrayList<>();
        List<LineF> vals = new ArrayList<>();
        int curChipNo = 0;
        int curA = -1;
        PolyState.Inp curInp = null;
        for (Line line : allLines) {
            if (line instanceof LineS) {
                if (!vals.isEmpty()) {
                    LineF last = vals.get(vals.size() - 1);
                    list.add(new InpF(last.time, curInp, vals.toArray(new LineF[vals.size()]), last.f, curA != 0));
                    vals.clear();
                }
                curChipNo = ((LineS) line).chipNo;
                curA = -1;
                curInp = null;
            } else if (line instanceof LineA) {
                if (!vals.isEmpty()) {
                    LineF last = vals.get(vals.size() - 1);
                    list.add(new InpF(last.time, curInp, vals.toArray(new LineF[vals.size()]), last.f, curA != 0));
                    vals.clear();
                }
                curA = ((LineA) line).addr;
            } else if (line instanceof LineW) {
                if (!vals.isEmpty()) {
                    LineF last = vals.get(vals.size() - 1);
                    list.add(new InpF(last.time, curInp, vals.toArray(new LineF[vals.size()]), last.f, curA != 0));
                    vals.clear();
                }
                curInp = ((LineW) line).inp;
            } else if (line instanceof LineR) {
                if (!vals.isEmpty()) {
                    LineF last = vals.get(vals.size() - 1);
                    list.add(new InpF(last.time, curInp, vals.toArray(new LineF[vals.size()]), last.f, curA != 0));
                    vals.clear();
                }
                curInp = ((LineR) line).inp;
            } else if (line instanceof LineF && (curA == 0 || curA == 7) && curChipNo == chipNo) {
                vals.add((LineF) line);
            }
        }
        if (!vals.isEmpty()) {
            LineF last = vals.get(vals.size() - 1);
            list.add(new InpF(last.time, curInp, vals.toArray(new LineF[vals.size()]), last.f, curA != 0));
            vals.clear();
        }
        for (int i = 1; i < list.size(); i++) {
            InpF prev = list.get(i - 1);
            InpF next = list.get(i);
            assert next.fs[0].time.getTime() >= prev.fs[prev.fs.length - 1].time.getTime();
        }
        return list;
    }
}
