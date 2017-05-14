package ru.nsc.interval.thermocompensation.intopt;

import net.java.jinterval.interval.set.SetInterval;
import net.java.jinterval.interval.set.SetIntervalContext;

import java.util.LinkedList;
import java.util.Queue;

public class Verifier {
    private final int dac;
    private final int digitalTemperature;
    private final double eps;
    private final SetIntervalContext ic;
    private final IntervalModel im;
    private Queue<SetInterval[]> workingBoxes;

    Verifier(IntervalModel im, Queue<SetInterval[]> workingBoxes, int digitalTemperature, int dac, double eps, SetIntervalContext ic) {
        this.im = im;
        this.workingBoxes = new LinkedList<>(workingBoxes);
        this.digitalTemperature = digitalTemperature;
        this.dac = dac;
        this.eps = eps;
        this.ic = ic;
    }

    public Queue<SetInterval[]> startVerification() {
        Queue<SetInterval[]> probablySolution = new LinkedList<>();

        while (!workingBoxes.isEmpty()) {
            SetInterval[] workingBox = workingBoxes.poll();

            SetInterval u = im.evalU(workingBox, digitalTemperature);

            if (u.isMember(dac)) {
                if (multiDimWidth(workingBox) < eps) {
                    probablySolution.add(workingBox);
                } else {
                    int numbOfWidest = 0;
                    double maxWidth = workingBox[0].doubleWid();
                    for (int i = 1; i < workingBox.length; i++) {
                        if (maxWidth < workingBox[i].doubleWid()) {
                            maxWidth = workingBox[i].doubleWid();
                            numbOfWidest = i;
                        }
                    }

                    SetInterval[] firstBox = workingBox.clone();
                    int intvalInf = (int) Math.ceil(firstBox[numbOfWidest].doubleInf());
                    int intvalSup = (int) Math.floor(firstBox[numbOfWidest].doubleMid());

                    if (intvalInf <= intvalSup) {
                        firstBox[numbOfWidest] = ic.numsToInterval(intvalInf, intvalSup);
                        workingBoxes.add(firstBox);
                    }

                    SetInterval[] secondBox = workingBox.clone();
                    intvalInf = (int) Math.ceil(secondBox[numbOfWidest].doubleMid());
                    intvalSup = (int) Math.floor(secondBox[numbOfWidest].doubleSup());

                    if (intvalInf <= intvalSup) {
                        secondBox[numbOfWidest] = ic.numsToInterval(intvalInf, intvalSup);
                        workingBoxes.add(secondBox);
                    }
                }
            }
        }

        return probablySolution;
    }

    private double multiDimWidth(SetInterval[] box) {
        double maxWidth = box[0].doubleWid();

        for (int i = 1; i < box.length; i++) {
            if (box[i].doubleWid() > maxWidth) {
                maxWidth = box[i].doubleWid();
            }
        }

        return maxWidth;
    }
}
