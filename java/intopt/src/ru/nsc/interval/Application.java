package ru.nsc.interval;

import net.java.jinterval.interval.set.SetInterval;
import net.java.jinterval.interval.set.SetIntervalContext;
import net.java.jinterval.interval.set.SetIntervalContexts;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Application {
    static SetIntervalContext ic = SetIntervalContexts.getPlain();

    public static void main(String[] args) throws IOException {
        BufferedReader fileReader = new BufferedReader(new FileReader("input.txt"));
        double[] temp = new double[13];
        double[] u = new double[13];
        for (int i = 0; i < 13; i++) {
            String[] tmp = fileReader.readLine().split(";");
            temp[i] = Double.parseDouble(tmp[0]);
            u[i] = Double.parseDouble(tmp[1]);
        }

        SetInterval INFBIT = ic.numsToInterval(0, 63);
        SetInterval SBIT = ic.numsToInterval(0, 31);
        SetInterval K1BIT = ic.numsToInterval(1, 255);
        SetInterval K2BIT = ic.numsToInterval(0, 127);
        SetInterval K3BIT = ic.numsToInterval(0, 31);
        SetInterval K4BIT = ic.numsToInterval(0, 31);
        SetInterval K5BIT = ic.numsToInterval(0, 15);

        SetInterval[] box = new SetInterval[]{INFBIT, SBIT, K1BIT, K2BIT, K3BIT, K4BIT, K5BIT};

        double eps = 1E-5;

        ThermOpt program = new ThermOpt(box, temp, u, eps, ic);
        program.startOptimization();
    }
}
