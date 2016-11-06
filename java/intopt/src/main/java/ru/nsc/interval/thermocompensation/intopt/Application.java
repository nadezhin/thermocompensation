package ru.nsc.interval.thermocompensation.intopt;

import net.java.jinterval.interval.set.SetInterval;
import net.java.jinterval.interval.set.SetIntervalContext;
import net.java.jinterval.interval.set.SetIntervalContexts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import ru.nsc.interval.thermocompensation.model.ParseTestInps;
import ru.nsc.interval.thermocompensation.model.ParseTestInps.ExtendedInp;
import ru.nsc.interval.thermocompensation.model.PolyModel;
import ru.nsc.interval.thermocompensation.model.PolyState;
import ru.nsc.interval.thermocompensation.optim.ChipPoints;
import ru.nsc.interval.thermocompensation.optim.Meas;

public class Application {

    static SetIntervalContext ic = SetIntervalContexts.getPlain();

    private static void checkChip(ChipPoints chipPoints) throws IOException {
        int chipNo = 0;
        Meas[] measures = chipPoints.getMeasures();
        try (BufferedReader fileReader = new BufferedReader(new FileReader("../java/intopt/input.txt"))) {
            double[] temp = new double[13];
            double[] u = new double[13];
            for (int i = 0; i < 13; i++) {
                String[] tmp = fileReader.readLine().split(";");
                temp[i] = Double.parseDouble(tmp[0]);
                u[i] = Double.parseDouble(tmp[1]);
                Meas meas = measures[i];
                assert meas.chipNo == chipNo;
                assert meas.adcOut == temp[i];
                assert meas.dacInp == u[i];
            }
        }
    }

    private static void doChip(int chipNo, ChipPoints[] data, List<List<ExtendedInp>> results) {
        ChipPoints chipPoints = data[chipNo];
        Meas[] measures = chipPoints.getMeasures();
        PolyState.Inp heuristicInp = results.get(chipNo).get(0).inp;
        double[] temp = new double[13];
        double[] u = new double[13];
        for (int i = 0; i < 13; i++) {
            Meas meas = measures[i];
            assert meas.chipNo == chipNo;
            temp[i] = meas.adcOut;
            u[i] = meas.dacInp;
        }

        SetInterval INFBIT = ic.numsToInterval(0, 63);
        SetInterval SBIT = ic.numsToInterval(0, 31);
        SetInterval K1BIT = ic.numsToInterval(1, 255);
        SetInterval K2BIT = ic.numsToInterval(0, 127);
        SetInterval K3BIT = ic.numsToInterval(0, 31);
        SetInterval K4BIT = ic.numsToInterval(0, 31);
        SetInterval K5BIT = ic.numsToInterval(0, 15);

        SetInterval[] box = new SetInterval[]{INFBIT, SBIT, K1BIT, K2BIT, K3BIT, K4BIT, K5BIT};

        double eps = 1E-5;

        ThermOpt program = new ThermOpt(box, temp, u, eps, ic);
        int[] result = program.startOptimization();
        PolyState.Inp intervalInp = PolyState.Inp.genNom();
        intervalInp.INF = result[0];
        intervalInp.SBIT = result[1];
        intervalInp.K1BIT = result[2];
        intervalInp.K2BIT = result[3];
        intervalInp.K3BIT = result[4];
        intervalInp.K4BIT = result[5];
        intervalInp.K5BIT = result[6];

        int maxHeuristic = 0;
        int maxInterval = 0;
        for (Meas meas : measures) {
            int adcOut = meas.adcOut;
            int required = meas.dacInp;
            heuristicInp.T = intervalInp.T = adcOut;
            int heuristicResult = PolyModel.compute(heuristicInp);
            int intervalResult = PolyModel.compute(intervalInp);
            int heuristicErr = heuristicResult - required;
            int intervalErr = intervalResult - required;
            maxHeuristic = Math.max(maxHeuristic, Math.abs(heuristicErr));
            maxInterval = Math.max(maxInterval, Math.abs(intervalErr));
//            System.out.println("  temp=" + adcOut
//                    + "\trequired=" + required
//                    + "\theuristic=" + heuristicResult + "(" + heuristicErr + ")"
//                    + "\tinterval=" + intervalResult + "(" + intervalErr + ")");
        }
        System.out.println("Chip " + (chipNo + 1));
        System.out.println("heuristic: " + heuristicInp.toLongNom() + "\t" + maxHeuristic);
        System.out.println("interval:  " + intervalInp.toLongNom() + "\t" + maxInterval);

    }

    public static void main(String[] args) throws IOException {
        String name = "P";
        ChipPoints[] data = ChipPoints.readChipPoints(new File(name + ".csv"));
        List<List<ExtendedInp>> results = ParseTestInps.parseLogExtendedInps(Paths.get(name + ".opt"));
        checkChip(data[0]);
        for (int chipNo = 0; chipNo < data.length; chipNo++) {
            if (chipNo >= data.length || data[chipNo] == null || chipNo >= results.size()) {
                continue;
            }
            doChip(chipNo, data, results);
            // break; uncomment to optimize chip 0 only
        }
    }
}
