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

    public ChipRefineF0(ChipT chipT, PolyState.Inp inp) {
        super(new ChipExpress(chipT), chipT, inp);
    }

    @Override
    public int[] getAdcOuts() {
        int[] result = new int[maxAdcOut - minAdcOut + 1];
        for (int i = 0; i < result.length; i++) {
            result[i] = minAdcOut + i;
        }
        return result;
    }

    public static ChipModel[] readChips(String prefix, List<List<ExtendedInp>> inpsLists) throws IOException, ParseException {
        String runTime = "";
        Path pathT = Paths.get(prefix + "_t.txt");
        Path pathF = Paths.get(prefix + "_f0.txt");
        ParseTemp parseTemp = new ParseTemp(pathT);
        Parse parse = new Parse(pathF.toFile());

        ChipModel[] chipModels = new ChipModel[64];
        for (int chipNo = 0; chipNo < chipModels.length; chipNo++) {
            if (!parse.getMeasTN(chipNo).isEmpty()) {
                String chipNoStr = String.format("%1$02d", chipNo + 1);
                ChipRefineF0 chipModel;
                try {
                    ChipT chip = new ChipT(chipNoStr, runTime, parseTemp, new ParseSeq(parse, chipNo), CapSettings.STD_F0);
                    if (chip.badFreq) {
                        System.out.print('?');
                        continue;
                    }
                    chipModel = new ChipRefineF0(chip, inpsLists.get(chipNo).get(0).inp);
                    if (!chipModel.isMonotonic0()) {
                        System.out.print('X');
                        continue;
                    }
                } catch (Exception e) {
                    System.out.print('!');
                    continue;
                }
                chipModels[chipNo] = chipModel;
                System.out.print('+');
            } else {
                System.out.print('.');
            }
        }
        System.out.println();
        return chipModels;
    }
}
