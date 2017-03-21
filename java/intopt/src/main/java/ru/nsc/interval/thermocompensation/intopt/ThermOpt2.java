package ru.nsc.interval.thermocompensation.intopt;

import java.util.PriorityQueue;
import net.java.jinterval.interval.set.SetInterval;
import net.java.jinterval.interval.set.SetIntervalContext;
import net.java.jinterval.interval.set.SetIntervalOps;
import ru.nsc.interval.thermocompensation.model.PolyState;

public class ThermOpt2 {

    PriorityQueue<ResultHolderElement> resultHolder = new PriorityQueue<>(new DifferenceComparator());
    private final SetInterval[] initialBox;
    private final SetIntervalContext ic;
    private final double eps;
    private final IntervalModel im;
    double supOfGlobalOptimum = Double.POSITIVE_INFINITY;
    int[] result;

    ThermOpt2(IntervalModel im, int infbit, int sbit, double eps, SetIntervalContext ic) {
        this.im = im;
        initialBox = im.getTopBox();
        initialBox[0] = SetIntervalOps.nums2(infbit, infbit);
        initialBox[1] = SetIntervalOps.nums2(sbit, sbit);
        this.eps = eps;
        this.ic = ic;
        resultHolder.add(optimizationStep(initialBox));
        result = new int[initialBox.length];
    }

    void updateRecord(PolyState.Inp inp) {
        SetInterval[] midBox = initialBox.clone();
        midBox[0] = ic.numsToInterval(inp.INF, inp.INF);
        midBox[1] = ic.numsToInterval(inp.SBIT, inp.SBIT);
        midBox[2] = ic.numsToInterval(inp.K1BIT, inp.K1BIT);
        midBox[3] = ic.numsToInterval(inp.K2BIT, inp.K2BIT);
        midBox[4] = ic.numsToInterval(inp.K3BIT, inp.K3BIT);
        midBox[5] = ic.numsToInterval(inp.K4BIT, inp.K4BIT);
        midBox[6] = ic.numsToInterval(inp.K5BIT, inp.K5BIT);
        for (int i = 0; i < midBox.length; i++) {
            midBox[i] = ic.numsToInterval(Math.floor(midBox[i].doubleMid()), Math.floor(midBox[i].doubleMid()));
        }
        double currentSupOfGlobalOptimum = optimizationStep(midBox).getMaxDifferenceBtwU();
        if (currentSupOfGlobalOptimum < supOfGlobalOptimum) {
            supOfGlobalOptimum = currentSupOfGlobalOptimum;
            for (int i = 0; i < result.length; i++) {
                result[i] = (int) midBox[i].doubleSup();
            }
        }

    }

    private ResultHolderElement optimizationStep(SetInterval[] box) {
        SetInterval obj = im.eval(box);
        return new ResultHolderElement(box, obj.doubleInf(), obj.doubleSup());
    }

    public int[] startOptimization() {
        int numbOfWidest;
        double maxWidth;
        SetInterval[] currentBox;
        int cnt = 0;
        while ((supOfGlobalOptimum - resultHolder.peek().getMinDifferenceBtwU()) > eps) {
            if (++cnt % 100000 == 0) {
                System.out.println(cnt + ":[" + resultHolder.peek().getMinDifferenceBtwU() + "," + supOfGlobalOptimum + "]");
            }
            currentBox = resultHolder.poll().getRecordBox();
            numbOfWidest = 0;
            maxWidth = currentBox[0].doubleWid();
            for (int i = 1; i < currentBox.length; i++) {
                if (maxWidth < currentBox[i].doubleWid()) {
                    maxWidth = currentBox[i].doubleWid();
                    numbOfWidest = i;
                }
            }
            SetInterval[] firstBox = currentBox.clone();
            int intvalInf = (int) Math.ceil(firstBox[numbOfWidest].doubleInf());
            int intvalSup = (int) Math.floor(firstBox[numbOfWidest].doubleMid());
            if (intvalInf <= intvalSup) {
                firstBox[numbOfWidest] = ic.numsToInterval(intvalInf, intvalSup);
                resultHolder.add(optimizationStep(firstBox));
            }
            SetInterval[] secondBox = currentBox.clone();
            intvalInf = (int) Math.ceil(secondBox[numbOfWidest].doubleMid());
            intvalSup = (int) Math.floor(secondBox[numbOfWidest].doubleSup());
            if (intvalInf <= intvalSup) {
                secondBox[numbOfWidest] = ic.numsToInterval(intvalInf, intvalSup);
                resultHolder.add(optimizationStep(secondBox));
            }
            SetInterval[] midBox = currentBox.clone();
            for (int i = 0; i < midBox.length; i++) {
                midBox[i] = ic.numsToInterval(Math.floor(midBox[i].doubleMid()), Math.floor(midBox[i].doubleMid()));
            }
            double currentSupOfGlobalOptimum = optimizationStep(midBox).getMaxDifferenceBtwU();
            if (currentSupOfGlobalOptimum < supOfGlobalOptimum) {
                supOfGlobalOptimum = currentSupOfGlobalOptimum;
                for (int i = 0; i < result.length; i++) {
                    result[i] = (int) midBox[i].doubleSup();
                }
            }
        }
        for (int i = 0; i < result.length; i++) {
            System.out.print(result[i] + " ");
        }
        System.out.print("min " + resultHolder.peek().getMinDifferenceBtwU());
        System.out.println(" max " + supOfGlobalOptimum);
        /*for (int i = 0; i < result.length; i++) {
            System.out.print("[" + resultHolder.peek().getRecordBox()[i].doubleInf() + ", " + resultHolder.peek().getRecordBox()[i].doubleSup() + "] ");
        }
        System.out.print("min " + resultHolder.peek().getMinDifferenceBtwU());
        System.out.println(" max " + resultHolder.poll().getMaxDifferenceBtwU());*/
        return result;
    }
}
