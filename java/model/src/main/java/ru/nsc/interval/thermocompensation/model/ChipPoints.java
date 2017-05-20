package ru.nsc.interval.thermocompensation.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 *
 */
public class ChipPoints implements ChipModel {

    final int chipNo;
    final Meas[] meas;
    final double f0;

    private ChipPoints(int chipNo, Meas[] meas) {
        this.chipNo = chipNo;
        this.meas = meas.clone();
        double sf = 0;
        for (Meas m : meas) {
            sf += m.f;
        }
        f0 = Math.rint(sf / meas.length);
    }

    private double getModelFfromAdcOut(double dacInp, int adcOut) {
        Meas mMin = null;
        Meas mMax = null;
        for (int i = 0; i < meas.length; i++) {
            Meas m = meas[i];
            if (m.adcOut == adcOut) {
                return f0 + (dacInp - m.dacInp);
            }
            if (m.adcOut < adcOut && (mMin == null || mMin.adcOut < m.adcOut)) {
                mMin = m;
            }
            if (m.adcOut > adcOut && (mMax == null || mMax.adcOut > m.adcOut)) {
                mMax = m;
            }
        }
        if (mMax == null) {
            return f0 + (dacInp - mMin.dacInp);
        }
        if (mMin == null) {
            return f0 + (dacInp - mMax.dacInp);
        }
        return f0 + (dacInp - (mMin.dacInp * (mMax.adcOut - adcOut) + mMax.dacInp * (adcOut - mMin.adcOut)) / (double) (mMax.adcOut - mMin.adcOut));
    }

    public double getF0() {
        return f0;
    }

    @Override
    public double getLowerModelFfromAdcOut(double cc, double cf, double dacInp, int adcOut) {
        return getModelFfromAdcOut(dacInp, adcOut);
    }

    @Override
    public double getUpperModelFfromAdcOut(double cc, double cf, double dacInp, int adcOut) {
        return getModelFfromAdcOut(dacInp, adcOut);
    }

    @Override
    public int[] getAdcOuts() {
        int[] result = new int[meas.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = meas[i].adcOut;
        }
        return result;
    }

    @Deprecated
    public Meas[] getMeasures() {
        return meas.clone();
    }

    public static ChipPoints[] readChipPoints(File file) throws IOException {
        BitSet chips = new BitSet();
        List<Meas> allMeas = new ArrayList<>();
        final double DF_DU = 1;
        try (LineNumberReader in = new LineNumberReader(new InputStreamReader(new FileInputStream(file)))) {
            String line;
            while ((line = in.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                String[] pieces = line.split(";");
                assert pieces.length == 4;
                int chipNo = Integer.parseInt(pieces[0]);
                int adcOut = Integer.parseInt(pieces[1]);
                int dacInp = Integer.parseInt(pieces[2]);
                double f = Double.parseDouble(pieces[3].replace(',', '.'));
//                assert pieces[4].isEmpty();
                chips.set(chipNo);
                allMeas.add(new Meas(chipNo, adcOut, dacInp, f, DF_DU));
            }
        }
        ChipPoints[] result = new ChipPoints[64];
        for (int chipNo = 0; chipNo < result.length; chipNo++) {
            if (!chips.get(chipNo)) {
                continue;
            }
            List<Meas> myMeas = new ArrayList<>();
            for (Meas meas : allMeas) {
                if (meas.chipNo == chipNo) {
                    myMeas.add(meas);
                }
            }
            result[chipNo] = new ChipPoints(chipNo, myMeas.toArray(new Meas[myMeas.size()]));
        }
        return result;
    }
}
