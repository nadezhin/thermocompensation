package ru.nsc.interval.thermocompensation.intopt;

import net.java.jinterval.interval.set.SetInterval;
import net.java.jinterval.interval.set.SetIntervalContext;

import java.util.Comparator;
import java.util.PriorityQueue;

public class ThermOpt {

    PriorityQueue<ResultHolderElement> resultHolder = new PriorityQueue<>(new DifferenceComparator());
    private final SetInterval[] initialBox;
    private final SetIntervalContext ic;
    private final double eps;
    private final IntervalModel im;

    ThermOpt(IntervalModel im, double eps, SetIntervalContext ic) {
        this.im = im;
        initialBox = im.getTopBox();
        this.eps = eps;
        this.ic = ic;
        resultHolder.add(optimizationStep(initialBox));
    }

    private ResultHolderElement optimizationStep(SetInterval[] box) {
        SetInterval obj = im.eval(box);
        return new ResultHolderElement(box, obj.doubleInf(), obj.doubleSup());
    }

    public int[] startOptimization() {
        int[] result = new int[initialBox.length];
        int numbOfWidest;
        double maxWidth;
        SetInterval[] currentBox;
        double supOfGlobalOptimum = Double.POSITIVE_INFINITY;
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

class DifferenceComparator implements Comparator<ResultHolderElement> {

    public int compare(ResultHolderElement el1, ResultHolderElement el2) {
        return Double.valueOf(el1.getMinDifferenceBtwU()).compareTo(el2.getMinDifferenceBtwU());
    }
}

class ResultHolderElement {

    private SetInterval[] recordBox;
    private double minDifferenceBtwU;
    private double maxDifferenceBtwU;

    ResultHolderElement(SetInterval[] recordBox, double minDifferenceBtwU, double maxDifferenceBtwU) {
        this.recordBox = recordBox;
        this.minDifferenceBtwU = minDifferenceBtwU;
        this.maxDifferenceBtwU = maxDifferenceBtwU;
    }

    public void setRecordBox(SetInterval[] recordBox) {
        this.recordBox = recordBox;
    }

    public void setMinDifferenceBtwU(double minDifferenceBtwU) {
        this.minDifferenceBtwU = minDifferenceBtwU;
    }

    public double getMinDifferenceBtwU() {
        return minDifferenceBtwU;
    }

    public SetInterval[] getRecordBox() {
        return recordBox;
    }

    public double getMaxDifferenceBtwU() {
        return maxDifferenceBtwU;
    }

    public void setMaxDifferenceBtwU(double maxDifferenceBtwU) {
        this.maxDifferenceBtwU = maxDifferenceBtwU;
    }

    public int compareTo(ResultHolderElement el) {
        return Double.valueOf(this.minDifferenceBtwU).compareTo(Double.valueOf(el.minDifferenceBtwU));
    }
}
