package ru.nsc.interval.thermocompensation.intopt;

import net.java.jinterval.interval.set.SetInterval;
import net.java.jinterval.interval.set.SetIntervalContext;
import net.java.jinterval.interval.set.SetIntervalEvaluator;

import java.util.Comparator;
import java.util.PriorityQueue;

public class ThermOpt {
    PriorityQueue<ResultHolderElement> resultHolder = new PriorityQueue<>(new DifferenceComparator());
    private SetInterval[] initialBox;
    private SetIntervalContext ic;
    private double[] temp;
    private double[] u;
    private double eps;
    private SetIntervalEvaluator setEv;

    ThermOpt(SetInterval[] box, double[] temp, double[] u, double eps, SetIntervalContext ic) {
        initialBox = box;
        this.temp = temp;
        this.u = u;
        this.eps = eps;
        this.ic = ic;
        setEv = SetIntervalEvaluator.create(ic, Functions.getList(), Functions.getObjective());
        resultHolder.add(optimizationStep(initialBox));
    }

    private final ResultHolderElement optimizationStep(SetInterval[] box) {
        double differenceBtwU;
        double maxDifferenceMag = 0;
        double maxDifferenceMig = 0;
        SetInterval uIntval;
        for (int i = 0; i < temp.length; i++) {
            SetInterval[] boxAndTmp = new SetInterval[box.length + 1];
            System.arraycopy(box, 0, boxAndTmp, 0, box.length);
            boxAndTmp[box.length] = ic.numsToInterval(temp[i], temp[i]);
            uIntval = setEv.evaluate(boxAndTmp)[0];
            /*
            if (uIntval.doubleInf() > 4095) {
                uIntval = ic.numsToInterval(4095, 4095);
            } else {
                if (uIntval.doubleSup() > 4095) {
                    uIntval = ic.numsToInterval(uIntval.doubleInf(), 4095);
                }
            }
            if (uIntval.doubleSup() < 0) {
                uIntval = ic.numsToInterval(0, 0);
            } else {
                if (uIntval.doubleInf() < 0) {
                    uIntval = ic.numsToInterval(0, uIntval.doubleSup());
                }
            }
            */
            uIntval = ic.sub(uIntval, ic.numsToInterval(u[i], u[i]));
            if (ic.intersection(uIntval, ic.numsToInterval(0, 0)).isEmpty()) {
                differenceBtwU = Math.min(Math.abs(uIntval.doubleInf()), Math.abs(uIntval.doubleSup()));
            } else {
                differenceBtwU = 0;
            }
            if (maxDifferenceMig <= differenceBtwU) {
                maxDifferenceMig = differenceBtwU;
                maxDifferenceMag = Math.max(Math.abs(uIntval.doubleInf()), Math.abs(uIntval.doubleSup()));
            }
            maxDifferenceMig = Math.max(differenceBtwU, maxDifferenceMig);
        }
        return new ResultHolderElement(box, maxDifferenceMig, maxDifferenceMag);
    }

    public int[] startOptimization() {
        int[] result = new int[initialBox.length];
        int numbOfWidest;
        double maxWidth;
        SetInterval[] currentBox;
        double supOfGlobalOptimum = Double.POSITIVE_INFINITY;
        while ((supOfGlobalOptimum - resultHolder.peek().getMinDifferenceBtwU()) > eps) {
//            System.out.println(resultHolder.peek().getMinDifferenceBtwU());
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
            double currentSupOfGlobalOptimum = optimizationStep(midBox).getMinDifferenceBtwU();
            if (currentSupOfGlobalOptimum < supOfGlobalOptimum) {
                supOfGlobalOptimum = currentSupOfGlobalOptimum;
                for (int i = 0; i < result.length; i++) {
                    result[i] = (int) midBox[i].doubleInf();
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
