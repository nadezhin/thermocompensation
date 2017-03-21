package ru.nsc.interval.thermocompensation.intopt;

import java.util.Arrays;
import java.util.PriorityQueue;
import net.java.jinterval.interval.set.SetInterval;
import net.java.jinterval.interval.set.SetIntervalContext;
import net.java.jinterval.interval.set.SetIntervalEvaluator;
import net.java.jinterval.interval.set.SetIntervalOps;
import ru.nsc.interval.thermocompensation.model.PolyState;

public class ThermOpt2 {

    /**
     * Check that point model is within interval model
     */
    private static final boolean CHECK_POINT_WITHIN_INTERVAL = true;

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
        double pObjective = im.evalMaxPpm(inp);
        if (CHECK_POINT_WITHIN_INTERVAL) {
            checkPointWithinInterval(inp, pObjective);
        }

        if (pObjective < supOfGlobalOptimum) {
            supOfGlobalOptimum = pObjective;
            for (int i = 0; i < result.length; i++) {
                result[0] = inp.INF;
                result[1] = inp.SBIT;
                result[2] = inp.K1BIT;
                result[3] = inp.K2BIT;
                result[4] = inp.K3BIT;
                result[5] = inp.K4BIT;
                result[6] = inp.K5BIT;
            }
        }
    }

    private void checkPointWithinInterval(PolyState.Inp inp, double pObjective) {
        SetInterval[] midBox = initialBox.clone();
        midBox[0] = ic.numsToInterval(inp.INF, inp.INF);
        midBox[1] = ic.numsToInterval(inp.SBIT, inp.SBIT);
        midBox[2] = ic.numsToInterval(inp.K1BIT, inp.K1BIT);
        midBox[3] = ic.numsToInterval(inp.K2BIT, inp.K2BIT);
        midBox[4] = ic.numsToInterval(inp.K3BIT, inp.K3BIT);
        midBox[5] = ic.numsToInterval(inp.K4BIT, inp.K4BIT);
        midBox[6] = ic.numsToInterval(inp.K5BIT, inp.K5BIT);
        SetInterval iObjective = im.eval(midBox);
        if (iObjective.isMember(pObjective)) {
            return;
        }

        System.out.println("Inp=" + inp.toNom() + " "
                + pObjective + " not in " + IntervalModel.print(iObjective));
        SetIntervalEvaluator ev;
        switch (im.getPolyModel()) {
            case SPECIFIED:
                ev = SetIntervalEvaluator.create(ic,
                        FunctionsSpecified.getList(),
                        FunctionsSpecified.xd,
                        FunctionsSpecified.xs,
                        FunctionsSpecified.pr2,
                        FunctionsSpecified.res3,
                        FunctionsSpecified.res4,
                        FunctionsSpecified.res5,
                        FunctionsSpecified.res6,
                        FunctionsSpecified.u);
                break;
            case MANUFACTURED:
                ev = SetIntervalEvaluator.create(ic,
                        FunctionsManufactured.getList(),
                        FunctionsManufactured.xd,
                        FunctionsManufactured.xs,
                        FunctionsManufactured.pr2,
                        FunctionsManufactured.res3,
                        FunctionsManufactured.res4,
                        FunctionsManufactured.res5,
                        FunctionsManufactured.res6,
                        FunctionsManufactured.u);
                break;
            default:
                ev = null;
        }
        SetInterval[] boxAndTemp = Arrays.copyOf(midBox, midBox.length + 1);
        SetInterval[] vals = ev.evaluate(boxAndTemp);
        System.out.println("xd=" + IntervalModel.print(vals[0]));
        System.out.println("xs=" + IntervalModel.print(vals[1]));
        System.out.println("pr2=" + IntervalModel.print(vals[2]));
        System.out.println("res3=" + IntervalModel.print(vals[3]));
        System.out.println("res4=" + IntervalModel.print(vals[4]));
        System.out.println("res5=" + IntervalModel.print(vals[5]));
        System.out.println("res6=" + IntervalModel.print(vals[6]));
        System.out.println("u=" + IntervalModel.print(vals[7]));
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
            int[] midPoint = new int[currentBox.length];
            for (int i = 0; i < midPoint.length; i++) {
                midPoint[i] = (int) Math.floor(currentBox[i].doubleMid());
            }
            updateRecord(im.pointAsInp(midPoint));
        }
        for (int i = 0; i < result.length; i++) {
            System.out.print(result[i] + " ");
        }
        System.out.print("min " + resultHolder.peek().getMinDifferenceBtwU());
        System.out.println(" max " + supOfGlobalOptimum);
        return result;
    }
}
