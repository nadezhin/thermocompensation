package ru.nsc.interval.thermocompensation.parse;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 */
public class ParseSeq {

    private final long startTime;
    public final double voltage;
    private final List<Parse.MeasF> measFs = new ArrayList<>();
    private final List<Parse.InpF> inpFs = new ArrayList<>();
    private final List<Parse.MeasTN> measTNs = new ArrayList<>();

    public ParseSeq(Parse... seq) {
        voltage = seq[0].voltage;
        startTime = seq[0].getTime0().getTime();
        for (int i = 1; i < seq.length; i++) {
            assert seq[i - 1].getTime0().compareTo(seq[i].getTime0()) < 0;
        }
        for (Parse parse : seq) {
            assert parse.voltage == voltage;
            measFs.addAll(parse.getMeasF(0));
            inpFs.addAll(parse.getInpF(0));
            measTNs.addAll(parse.getMeasTN(0));
        }
        for (int i = 1; i < measFs.size(); i++) {
            assert measFs.get(i - 1).getTime() < measFs.get(i).getTime();
        }
        for (int i = 1; i < inpFs.size(); i++) {
            assert inpFs.get(i - 1).getTime() < inpFs.get(i).getTime();
        }
    }

    public ParseSeq(Parse parse, int chipNo) {
        voltage = parse.voltage;
        startTime = parse.getTime0().getTime();
        measFs.addAll(parse.getMeasF(chipNo));
        inpFs.addAll(parse.getInpF(chipNo));
        measTNs.addAll(parse.getMeasTN(chipNo));
        for (int i = 1; i < measFs.size(); i++) {
            assert measFs.get(i - 1).getTime() < measFs.get(i).getTime();
        }
        for (int i = 1; i < inpFs.size(); i++) {
            assert inpFs.get(i - 1).getTime() < inpFs.get(i).getTime();
        }
    }

    public static <T extends Parse.Meas> double getValue(List<T> l, Date date) {
        int index = binarySearch(l, date);
        if (index >= 0) {
            return l.get(index).getValue();
        } else {
            index = -index - 1;
            if (index == 0) {
                return l.get(index).getValue();
            } else if (index == l.size()) {
                return l.get(index - 1).getValue();
            } else {
                T prev = l.get(index - 1);
                T next = l.get(index);
                long prevTime = prev.getTime();
                long nextTime = next.getTime();
                long time = date.getTime();
                long prevDT = time - prev.getTime();
                long nextDT = next.getTime() - time;
                assert prevDT > 0 && nextDT > 0;
                double prevValue = prev.getValue();
                double nextValue = next.getValue();
                double deriv = (nextValue - prevValue) / (prevDT + nextDT);
                return nextDT > prevDT ? prevValue + prevDT * deriv : nextValue - nextDT * deriv;
            }
        }
    }

    public static <T extends Parse.Meas> int binarySearch(List<T> l, Date date) {
        long time = date.getTime();
        int low = 0;
        int high = l.size() - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            T midVal = l.get(mid);
            long midTime = midVal.getTime();

            if (midTime < time) {
                low = mid + 1;
            } else if (midTime > time) {
                high = mid - 1;
            } else {
                return mid; // key found
            }
        }
        return -(low + 1);  // key not found
    }

    public Date getStartDate() {
        return new Date(startTime);
    }

    public List<Parse.MeasF> getMeasF() {
        return measFs;
    }

    public boolean freqFailure() {
        for (Parse.MeasF mf : measFs) {
            if (Double.isNaN(mf.f)) {
                return true;
            }
        }
        for (Parse.InpF mf : inpFs) {
            if (Double.isNaN(mf.f)) {
                return true;
            }
        }
        return false;
    }

    public List<Parse.InpF> getInpF() {
        return inpFs;
    }

    public List<Parse.MeasTN> getMeasTN() {
        return measTNs;
    }
}
