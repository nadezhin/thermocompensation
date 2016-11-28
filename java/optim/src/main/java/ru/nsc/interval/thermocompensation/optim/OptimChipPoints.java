package ru.nsc.interval.thermocompensation.optim;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import ru.nsc.interval.thermocompensation.model.AdcRange;
import ru.nsc.interval.thermocompensation.model.ChipModel;
import static ru.nsc.interval.thermocompensation.model.ChipPoints.readChipPoints;

/**
 *
 */
public class OptimChipPoints {


    public static void main(String[] args) throws IOException {
        int cc = 0;
        int cf = 0;
        AdcRange adcRange = new AdcRange(0, 4095);
        ChipModel[] chips = readChipPoints(new File(args[0]));
        Optim.Record[] records = new Optim.Record[chips.length];
        PrintWriter out = new PrintWriter(System.out, true);
        for (int chipNo = 0; chipNo < chips.length; chipNo++) {
            ChipModel chip = chips[chipNo];
            if (chip == null) {
                continue;
            }
            records[chipNo] = Optim.optimF(out, chip, cc, cf, adcRange, chip.getF0());
        }
        out.flush();
        for (int chipNo = 0; chipNo < chips.length; chipNo++) {
            if (records[chipNo] != null) {
                System.out.println((chipNo + 1) + ": " + records[chipNo]);
            }
        }
    }
}
