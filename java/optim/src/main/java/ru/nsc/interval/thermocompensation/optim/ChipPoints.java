package ru.nsc.interval.thermocompensation.optim;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 *
 */
public class ChipPoints implements ChipModel {

    public static class Meas {

        public final int chipNo;
        public final int adcOut;
        public final int dacInp;
        public final double f;

        Meas(int chipNo, int adcOut, int dacInp, double f) {
            this.chipNo = chipNo;
            this.adcOut = adcOut;
            this.dacInp = dacInp;
            this.f = f;
        }
    }

    final int chipNo;
    final Meas[] meas;
    final double f;

    private ChipPoints(int chipNo, Meas[] meas) {
        this.chipNo = chipNo;
        this.meas = meas.clone();
        double sf = 0;
        for (Meas m : meas) {
            sf += m.f;
        }
        f = Math.rint(sf / meas.length);
    }

    private double getModelFfromAdcOut(double dacInp, int adcOut) {
        Meas mMin = null;
        Meas mMax = null;
        for (int i = 0; i < meas.length; i++) {
            Meas m = meas[i];
            if (m.adcOut == adcOut) {
                return f + (dacInp - m.dacInp);
            }
            if (m.adcOut < adcOut && (mMin == null || mMin.adcOut < m.adcOut)) {
                mMin = m;
            }
            if (m.adcOut > adcOut && (mMax == null || mMax.adcOut > m.adcOut)) {
                mMax = m;
            }
        }
        if (mMax == null) {
            return f + (dacInp - mMin.dacInp);
        }
        if (mMin == null) {
            return f + (dacInp - mMax.dacInp);
        }
        return f + (dacInp - (mMin.dacInp * (mMax.adcOut - adcOut) + mMax.dacInp * (adcOut - mMin.adcOut)) / (double) (mMax.adcOut - mMin.adcOut));
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

    public Meas[] getMeasures() {
        return meas.clone();
    }

    public static ChipPoints[] readChipPoints(File file) throws IOException {
        BitSet chips = new BitSet();
        List<Meas> allMeas = new ArrayList<>();
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
                allMeas.add(new Meas(chipNo, adcOut, dacInp, f));
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

    public static void main(String[] args) throws IOException {
        int cc = 0;
        int cf = 0;
        AdcRange adcRange = new AdcRange(0, 4095);
        ChipPoints[] chips = readChipPoints(new File(args[0]));
        Optim.Record[] records = new Optim.Record[chips.length];
        PrintWriter out = new PrintWriter(System.out, true);
        for (int chipNo = 0; chipNo < chips.length; chipNo++) {
            ChipPoints chip = chips[chipNo];
            if (chip == null) {
                continue;
            }
            records[chipNo] = Optim.optimF(out, chip, cc, cf, adcRange, chip.f);
        }
        out.flush();
        for (int chipNo = 0; chipNo < chips.length; chipNo++) {
            if (records[chipNo] != null) {
                System.out.println((chipNo + 1) + ": " + records[chipNo]);
            }
        }
    }
}
