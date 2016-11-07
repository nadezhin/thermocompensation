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
        optimizationStep(initialBox);
    }

    private final void optimizationStep(SetInterval[] box) {
        double differenceBtwU;
        double maxDifferenceMag = 0;
        double maxDifferenceMig = 0;
        SetInterval uIntval;
        for (int i = 0; i < temp.length; i++) {
            SetInterval[] boxAndTmp = new SetInterval[box.length + 1];
            System.arraycopy(box, 0, boxAndTmp, 0, box.length);
            boxAndTmp[box.length] = ic.numsToInterval(temp[i], temp[i]);
            uIntval = setEv.evaluate(boxAndTmp)[0];
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
        resultHolder.add(new ResultHolderElement(box, maxDifferenceMig, maxDifferenceMag));
    }

    private SetInterval intervalToPointFlooring(SetInterval intval) {
        if (Math.floor(intval.doubleInf()) == Math.floor(intval.doubleSup())) {
            if (intval.doubleInf() != Math.floor(intval.doubleInf())) {
                return null;
            } else {
                return ic.numsToInterval(intval.inf(), intval.inf());
            }
        } else {
            return ic.numsToInterval(Math.floor(intval.doubleSup()), Math.floor(intval.doubleSup()));
        }
    }

    public int[] startOptimization() {
        int[] result = null;
        int numbOfWidest;
        double maxWidth;
        SetInterval[] currentBox;
        while ((resultHolder.peek().getMaxDifferenceBtwU() - resultHolder.peek().getMinDifferenceBtwU()) > eps) {
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
            SetInterval[] secondBox = currentBox.clone();
            firstBox[numbOfWidest] = ic.numsToInterval(firstBox[numbOfWidest].inf(), firstBox[numbOfWidest].mid());
            secondBox[numbOfWidest] = ic.numsToInterval(secondBox[numbOfWidest].mid(), secondBox[numbOfWidest].sup());
            if (maxWidth < 2) {
                firstBox[numbOfWidest] = intervalToPointFlooring(firstBox[numbOfWidest]);
                secondBox[numbOfWidest] = intervalToPointFlooring(secondBox[numbOfWidest]);
                if (firstBox[numbOfWidest] != null) {
                    optimizationStep(firstBox);
                }
                if (secondBox[numbOfWidest] != null) {
                    optimizationStep(secondBox);
                }
                continue;
            }
            optimizationStep(firstBox);
            optimizationStep(secondBox);
        }
        result = new int[resultHolder.peek().getRecordBox().length];
        for (int i = 0; i < result.length; i++) {
            result[i] = (int) resultHolder.peek().getRecordBox()[i].doubleInf();
            System.out.print(result[i] + " ");
        }
        System.out.print("min " + resultHolder.peek().getMinDifferenceBtwU());
        System.out.println(" max " + resultHolder.poll().getMaxDifferenceBtwU());
        for (int i = 0; i < result.length; i++) {
            System.out.print("[" + resultHolder.peek().getRecordBox()[i].doubleInf() + ", " + resultHolder.peek().getRecordBox()[i].doubleSup() + "] ");
        }
        System.out.print("min " + resultHolder.peek().getMinDifferenceBtwU());
        System.out.println(" max " + resultHolder.poll().getMaxDifferenceBtwU());
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
