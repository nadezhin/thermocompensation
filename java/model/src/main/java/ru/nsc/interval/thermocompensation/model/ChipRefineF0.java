package ru.nsc.interval.thermocompensation.model;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.List;
import ru.nsc.interval.thermocompensation.model.ParseTestInps.ExtendedInp;
import ru.nsc.interval.thermocompensation.parse.CapSettings;
import ru.nsc.interval.thermocompensation.parse.Parse;
import ru.nsc.interval.thermocompensation.parse.ParseSeq;
import ru.nsc.interval.thermocompensation.parse.ParseTemp;

/**
 *
 */
public class ChipRefineF0 extends ChipRefine {

    private final double f0;

    public ChipRefineF0(ChipT chipT, PolyState.Inp inp, double f0) {
        super(new ChipExpress(chipT), chipT, inp);
        this.f0 = f0;
    }

    @Override
    public int[] getAdcOuts() {
        int[] result = new int[maxAdcOut - minAdcOut + 1];
        for (int i = 0; i < result.length; i++) {
            result[i] = minAdcOut + i;
        }
        return result;
    }

    @Override
    public double getF0() {
        return f0;
    }

    public static ChipModel[] readChips(String prefix, List<List<ExtendedInp>> inpsLists, double f0) throws IOException, ParseException {
        String runTime = "";
        Path pathT = Paths.get(prefix, "_t.txt");
        Path pathF = Paths.get(prefix, "_f0.txt");
        ParseTemp parseTemp = new ParseTemp(pathT);
        Parse parse = new Parse(pathF.toFile());

        ChipModel[] chipModels = new ChipModel[64];
        for (int chipNo = 0; chipNo < chipModels.length; chipNo++) {
            if (!parse.getMeasTN(chipNo).isEmpty()) {
                String chipNoStr = String.format("%1$02d", chipNo + 1);
                ChipT chip = new ChipT(chipNoStr, runTime, parseTemp, new ParseSeq(parse, chipNo), CapSettings.STD_F0);
                System.out.print(chip.badFreq ? '?' : '+');
                if (!chip.badFreq) {
                    chipModels[chipNo] = new ChipRefineF0(chip, inpsLists.get(chipNo).get(0).inp, f0);
                }
            } else {
                System.out.print('.');
            }
        }
        System.out.println();
        return chipModels;
    }

}
