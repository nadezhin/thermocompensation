package ru.nsc.interval.thermocompensation.intopt;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import ru.nsc.interval.thermocompensation.model.ParseTestInps;
import ru.nsc.interval.thermocompensation.model.ParseTestInps.ExtendedInp;
import ru.nsc.interval.thermocompensation.model.PolyModel;
import ru.nsc.interval.thermocompensation.model.PolyState;
import ru.nsc.interval.thermocompensation.model.ChipPoints;

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
            double f0 = chipPoints.getF0();
            int[] adcOuts = chipPoints.getAdcOuts();
            List<ExtendedInp> einps = results.get(chipNo);
            System.out.println("Chip " + (chipNo + 1) + " f0=" + f0);
            for (ExtendedInp einp : einps) {
                PolyState.Inp inp = einp.inp;
                double df = einp.df;
                System.out.println(inp.toLongNom() + "   # df=" + df);
                int cc = inp.CC;
                int cf = inp.CF;
                double fInfAll = Double.MAX_VALUE;
                double fSupAll = -Double.MAX_VALUE;
                for (int adcOut: adcOuts) {
                    inp.T = adcOut;
                    int dacInp = PolyModel.compute(inp);
                    double fInf = chipPoints.getLowerModelFfromAdcOut(cc, cf, dacInp, adcOut);
                    double fSup = chipPoints.getUpperModelFfromAdcOut(cc, cf, dacInp, adcOut);
                    fInfAll = Math.min(fInfAll, fInf);
                    fSupAll = Math.max(fSupAll, fSup);
                    System.out.println("  adcOut=" + adcOut
                            + "\tdacInp=" + dacInp
                            + "\tf=[" + fInf + "," + fSup + "]"
                            + "\tdf=[" + (fInf - f0) + "," + (fSup - f0) + "]");
                }
                System.out.println("df=[" + (fInfAll - f0) + "," + (fSupAll - f0) + "]");
            }
        }
    }
}
