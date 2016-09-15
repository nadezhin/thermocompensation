package ru.nsc.interval.thermocompensation.intopt;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import ru.nsc.interval.thermocompensation.model.ParseTestInps;
import ru.nsc.interval.thermocompensation.model.ParseTestInps.ExtendedInp;
import ru.nsc.interval.thermocompensation.model.PolyModel;
import ru.nsc.interval.thermocompensation.model.PolyState;
import ru.nsc.interval.thermocompensation.optim.ChipPoints;

/**
 *
 */
public class ShowResults {

    public static void main(String[] args) throws IOException {
        String name = "P";
        ChipPoints[] data = ChipPoints.readChipPoints(new File(name + ".csv"));
        List<List<ExtendedInp>> results = ParseTestInps.parseLogExtendedInps(Paths.get(name + ".opt"));
        for (int chipNo = 0; chipNo < data.length; chipNo++) {
            if (chipNo >= data.length || data[chipNo] == null || chipNo >= results.size()) {
                continue;
            }
            ChipPoints chipPoints = data[chipNo];
            ChipPoints.Meas[] measures = chipPoints.getMeasures();
            List<ExtendedInp> einps = results.get(chipNo);
            System.out.println("Chip " + (chipNo + 1));
            for (ExtendedInp einp : einps) {
                PolyState.Inp inp = einp.inp;
                double df = einp.df;
                System.out.println(inp.toLongNom() + "   # df=" + df);
                for (ChipPoints.Meas meas : measures) {
                    int temp = meas.adcOut;
                    double required = meas.dacInp;
                    inp.T = temp;
                    int result = PolyModel.compute(inp);
                    System.out.println("  temp=" + temp
                            + "\tcomputed=" + result
                            + "\trequired=" + required
                            + "\tdiff=" + (result - required));
                }
            }
        }
    }
}
