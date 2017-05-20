/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.nsc.interval.thermocompensation.model;

import java.util.Arrays;
import java.util.List;

/**
 * Estimate prod6 from PolyModel.computeNew when INFBIT and SBIT are known
 */
public class IntModel {

    // (prod6 << 30) - (k1*K1BIT + k2*K2BIT + k3*K3BIT + k4*K4BIT + k5*K5BIT) IN [l,u]
    public final int xs;
    public final boolean fixBugP;
    public final long k1, k2, k3, k4, k5;
    public long m, l, u;

    public IntModel(int xs, boolean fixBugP) {
        if (Math.abs(xs) >= 0x1000) {
            throw new IllegalArgumentException();
        }
        this.xs = xs;
        this.fixBugP = fixBugP;
        long x = xs;
        k1 = -x << 37;
        k2 = (x * x) << 27;
        k3 = (x * x * x) << 19;
        k4 = (x * x * x * x) << 9;
        k5 = x * x * x * x * x;
    }

    // ((prod6-m)<<30) IN [0,0]
    public void setMLU(int K1BIT, int K2BIT, int K3BIT, int K4BIT, int K5BIT) {
        if (xs == 0) {
            m = (1032L << 14) + (1L << 13) + (fixBugP ? 0 : 1);
            l = u = 0;
            return;
        }

        long uprod2 = ((K5BIT * xs) & ((1L << 35) - 1))
                + ((long) K4BIT << 9)
                + ((1L << 35) - (25L << 9))
                + 0;
        long prod2 = uprod2 << (64 - 35) >> (64 - 35);
        int p2 = fixBugP ? 0 : (int) (uprod2 >> 35);

        long uprod3 = ((prod2 * xs) & ((1L << 45) - 1))
                + (((long) K3BIT) << 19)
                + (1L << 9) + (1L << 22)
                + p2;
        long prod3 = uprod3 << (64 - 45) >> (64 - 45);
        int p3 = fixBugP ? 0 : (int) (uprod3 >> 45);
        long res3 = prod3 >> 10;

        long uprod4 = ((res3 * xs) & ((1L << 45) - 1))
                + (((long) K2BIT) << 17)
                + (1L << 9) + (5L << 19)
                + p3;
        long prod4 = uprod4 << (64 - 45) >> (64 - 45);
        int p4 = fixBugP ? 0 : (int) (uprod4 >> 45);
        assert 0 <= p4 && p4 <= 1;
        long res4 = prod4 >> 10;

        long uprod5 = ((res4 * xs) & ((1L << 45) - 1))
                + (((~(((long) K1BIT) << 17)) & ((1L << 45) - 1))
                + ((1L << 45) - (195L << 16)))
                + p4;
        long prod5 = uprod5 << (64 - 45) >> (64 - 45);
        int p5 = fixBugP ? 0 : (int) (uprod5 >> 45);
        long res5 = prod5 >> 10;

        long uprod6 = ((res5 * xs) & ((1L << 49) - 1))
                + (1L << 13) + (1032L << 14) + p5;
        long prod6 = uprod6 << (64 - 49) >> (64 - 49);
        m = prod6;
        l = u = 0;
    }

    // ((prod6-m)<<30 - k1*K1BIT) IN  [l,u]
    public void setMLU(int K2BIT, int K3BIT, int K4BIT, int K5BIT) {
        if (xs == 0) {
            m = (1032L << 14) + (1L << 13) + (fixBugP ? 0 : 1);
            l = u = 0;
            return;
        }

        long uprod2 = ((K5BIT * xs) & ((1L << 35) - 1))
                + ((long) K4BIT << 9)
                + ((1L << 35) - (25L << 9))
                + 0;
        long prod2 = uprod2 << (64 - 35) >> (64 - 35);
        int p2 = fixBugP ? 0 : (int) (uprod2 >> 35);

        long uprod3 = ((prod2 * xs) & ((1L << 45) - 1))
                + (((long) K3BIT) << 19)
                + (1L << 9) + (1L << 22)
                + p2;
        long prod3 = uprod3 << (64 - 45) >> (64 - 45);
        int p3 = fixBugP ? 0 : (int) (uprod3 >> 45);
        long res3 = prod3 >> 10;

        long uprod4 = ((res3 * xs) & ((1L << 45) - 1))
                + (((long) K2BIT) << 17)
                + (1L << 9) + (5L << 19)
                + p3;
        long prod4 = uprod4 << (64 - 45) >> (64 - 45);
        int p4 = fixBugP ? 0 : (int) (uprod4 >> 45);
        assert 0 <= p4 && p4 <= 1;
        long res4 = prod4 >> 10;

        m = res4 * xs - 1 - (195L << 16) + p4;
        // ((prod5-m)<<20 - (k1*K1BIT)/xs) IN [0,0]

        long r4 = m & 0x3FF;
        m = m >> 10;
        u = r4 << 20;
        l = (r4 - 0x3FF) << 20;
        // ((res5-m)<<30 - (k1*K1BIT)/xs) IN [l,u]

        m *= xs;
        if (xs > 0) {
            l *= xs;
            u *= xs;
        } else {
            long tl = l;
            l = u * xs;
            u = tl * xs;
        }
        // ((res5*xs-m)<<30 - (k1*K1BIT)) IN [l,u]

        m += (1032L << 14) + (1L << 13);
        if (!fixBugP) { // p5
            l += 1L << 30;
            u += 2L << 30;
        }
        // (((prod6-m)<<30) - (k1*K1BIT)) IN [l,u]
    }

    // ((prod6-m)<<30 - k1*K1BIT - k2*K2BIT) IN  [l,u]
    public void setMLU(int K3BIT, int K4BIT, int K5BIT) {
        if (xs == 0) {
            m = (1032L << 14) + (1L << 13) + (fixBugP ? 0 : 1);
            l = u = 0;
            return;
        }

        long uprod2 = ((K5BIT * xs) & ((1L << 35) - 1))
                + ((long) K4BIT << 9)
                + ((1L << 35) - (25L << 9))
                + 0;
        long prod2 = uprod2 << (64 - 35) >> (64 - 35);
        int p2 = fixBugP ? 0 : (int) (uprod2 >> 35);

        long uprod3 = ((prod2 * xs) & ((1L << 45) - 1))
                + (((long) K3BIT) << 19)
                + (1L << 9) + (1L << 22)
                + p2;
        long prod3 = uprod3 << (64 - 45) >> (64 - 45);
        int p3 = fixBugP ? 0 : (int) (uprod3 >> 45);
        long res3 = prod3 >> 10;

        m = res3 * xs + (5L << 19) + (1L << 9) + p3;
        // ((prod4-m)<<10) - (k2*K2BIT)/xs/xs) IN [0,0]

        long r3 = m & 0x3FF;
        m = m >> 10;
        u = r3 << 10;
        l = (r3 - 0x3FF) << 10;
        // ((res4-m)<<20 - (k2*K2BIT)/xs/xs) IN [l,u]

        m *= xs;
        if (xs > 0) {
            l *= xs;
            u *= xs;
        } else {
            long tl = l;
            l = u * xs;
            u = tl * xs;
        }
        // ((res4*xs-m)<<20 - (k2*K2BIT)/xs) IN [l,u]

        m += -(195L << 16) - 1;
        if (!fixBugP) {
            u += 1L << 20;
        }
        // ((prod5-m)<<20) - (k1*K1BIT+k2*K2BIT)/xs) IN [0,0]

        long r4 = m & 0x3FF;
        m = m >> 10;
        u += r4 << 20;
        l += (r4 - 0x3FF) << 20;
        // ((res5-m)<<30 - (k1*K1BIT+k2*K2BIT)/xs) IN [l,u]

        m *= xs;
        if (xs > 0) {
            l *= xs;
            u *= xs;
        } else {
            long tl = l;
            l = u * xs;
            u = tl * xs;
        }
        // ((res5*xs-m)<<30 - (k1*K1BIT+k2*K2BIT)) IN [l,u]

        m += (1032L << 14) + (1L << 13);
        if (!fixBugP) { // p5
            l += 1L << 30;
            u += 2L << 30;
        }
        // (((prod6-m)<<30) - (k1*K1BIT)) IN [l,u]
    }

    // ((prod6-m)<<30 - k1*K1BIT - k2*K2BIT - k3*K3BIT) IN  [l,u]
    public void setMLU(int K4BIT, int K5BIT) {
        if (xs == 0) {
            m = (1032L << 14) + (1L << 13) + (fixBugP ? 0 : 1);
            l = u = 0;
            return;
        }

        long uprod2 = ((K5BIT * xs) & ((1L << 35) - 1))
                + ((long) K4BIT << 9)
                + ((1L << 35) - (25L << 9))
                + 0;
        long prod2 = uprod2 << (64 - 35) >> (64 - 35);
        int p2 = fixBugP ? 0 : (int) (uprod2 >> 35);

        m = prod2 * xs + (1L << 22) + (1L << 9) + p2;
        // ((prod3-m) - (k3*K3BIT)/xs/xs/xs) IN [0,0]

        long r2 = m & 0x3FF;
        m = m >> 10;
        u = r2;
        l = r2 - 0x3FF;
        // ((res3-m)<<10 - (k3*K3BIT)/xs/xs/xs) IN [l,u]

        m *= xs;
        if (xs > 0) {
            l *= xs;
            u *= xs;
        } else {
            long tl = l;
            l = u * xs;
            u = tl * xs;
        }
        // ((res3*xs-m)<<10 - (k3*K3BIT)/xs/xs) IN [l,u]

        m += (5L << 19) + (1L << 9);
        if (!fixBugP) { // p3
            u += 1L << 10;
        }
        // ((prod4-m)<<10) - (k2*K2BIT+k3*K3BIT)/xs/xs) IN [0,0]

        long r3 = m & 0x3FF;
        m = m >> 10;
        u += r3 << 10;
        l += (r3 - 0x3FF) << 10;
        // ((res4-m)<<20 - (k2*K2BIT+k3*K3BIT)/xs/xs) IN [l,u]

        m *= xs;
        if (xs > 0) {
            l *= xs;
            u *= xs;
        } else {
            long tl = l;
            l = u * xs;
            u = tl * xs;
        }
        // ((res4*xs-m)<<20 - (k2*K2BIT+k3*K3BIT)/xs) IN [l,u]

        m += -(195L << 16) - 1;
        if (!fixBugP) { // p4
            u += 1L << 20;
        }
        // ((prod5-m)<<20) - (k1*K1BIT+k2*K2BIT+k3*K3BIT)/xs) IN [0,0]

        long r4 = m & 0x3FF;
        m = m >> 10;
        u += r4 << 20;
        l += (r4 - 0x3FF) << 20;
        // ((res5-m)<<30 - (k1*K1BIT+k2*K2BIT+k3*K3BIT)/xs) IN [l,u]

        m *= xs;
        if (xs > 0) {
            l *= xs;
            u *= xs;
        } else {
            long tl = l;
            l = u * xs;
            u = tl * xs;
        }
        // ((res5*xs-m)<<30 - (k1*K1BIT+k2*K2BIT+k3*K3BIT)) IN [l,u]

        m += (1032L << 14) + (1L << 13);
        if (!fixBugP) { // p5
            l += 1L << 30;
            u += 2L << 30;
        }
        // (((prod6-m)<<30) - (k1*K1BIT+k2*K2BIT+k3*K3BIT)) IN [l,u]
    }

    // ((prod6-m)<<30 - k1*K1BIT - k2*K2BIT - k3*K3BIT - k4*K4BIT) IN  [l,u]
    public void setMLU(int K5BIT) {
        if (xs == 0) {
            m = (1032L << 14) + (1L << 13) + (fixBugP ? 0 : 1);
            l = u = 0;
            return;
        }

        m = K5BIT * xs - (25L << 9);
        // ((prod2-m) - (k4*K4BIT)/xs/xs/xs/xs) IN [0,0]

        m *= xs;
        // ((prod2*xs-m)<<10 - (k4*K4BIT)/xs/xs/xs) IN [0,0]

        m += (1L << 22) + (1L << 9);
        l = u = 0;
        if (!fixBugP) { // o2
            u += 2L << 10;
        }
        // ((prod3-m) - (k3*K3BIT+k4*K4BIT)/xs/xs/xs) IN [0,0]

        long r2 = m & 0x3FF;
        m = m >> 10;
        u += r2;
        l += r2 - 0x3FF;
        // ((res3-m)<<10 - (k3*K3BIT+k4*K4BIT)/xs/xs/xs) IN [l,u]

        m *= xs;
        if (xs > 0) {
            l *= xs;
            u *= xs;
        } else {
            long tl = l;
            l = u * xs;
            u = tl * xs;
        }
        // ((res3*xs-m)<<10 - (k3*K3BIT+k4*K4BIT)/xs/xs) IN [l,u]

        m += (5L << 19) + (1L << 9);
        if (!fixBugP) { // p3
            u += 1L << 10;
        }
        // ((prod4-m)<<10) - (k2*K2BIT+k3*K3BIT+k4*K4BIT)/xs/xs) IN [0,0]

        long r3 = m & 0x3FF;
        m = m >> 10;
        u += r3 << 10;
        l += (r3 - 0x3FF) << 10;
        // ((res4-m)<<20 - (k2*K2BIT+k3*K3BIT+k4*K4BIT)/xs/xs) IN [l,u]

        m *= xs;
        if (xs > 0) {
            l *= xs;
            u *= xs;
        } else {
            long tl = l;
            l = u * xs;
            u = tl * xs;
        }
        // ((res4*xs-m)<<20 - (k2*K2BIT+k3*K3BIT+k4*K4BIT)/xs) IN [l,u]

        m += -(195L << 16) - 1;
        if (!fixBugP) { // p4
            u += 1L << 20;
        }
        // ((prod5-m)<<20) - (k1*K1BIT+k2*K2BIT+k3*K3BIT+k4*K4BIT)/xs) IN [0,0]

        long r4 = m & 0x3FF;
        m = m >> 10;
        u += r4 << 20;
        l += (r4 - 0x3FF) << 20;
        // ((res5-m)<<30 - (k1*K1BIT+k2*K2BIT+k3*K3BIT+k4*K4BIT)/xs) IN [l,u]

        m *= xs;
        if (xs > 0) {
            l *= xs;
            u *= xs;
        } else {
            long tl = l;
            l = u * xs;
            u = tl * xs;
        }
        // ((res5*xs-m)<<30 - (k1*K1BIT+k2*K2BIT+k3*K3BIT+k4*K4BIT)) IN [l,u]

        m += (1032L << 14) + (1L << 13);
        if (!fixBugP) { // p5
            l += 1L << 30;
            u += 2L << 30;
        }
        // (((prod6-m)<<30) - (k1*K1BIT+k2*K2BIT+k3*K3BIT+k4*K4BIT)) IN [l,u]
    }

    // ((prod6-m)<<30 - k1*K1BIT - k2*K2BIT - k3*K3BIT - k4*K4BIT - k5*K5BIT) IN  [l,u]
    public void setMLU() {
        if (xs == 0) {
            m = (1032L << 14) + (1L << 13) + (fixBugP ? 0 : 1);
            l = u = 0;
            return;
        }

        m = -(25L << 9);
        // ((prod2-m) - (k4*K4BIT+k5*K5BIT)/xs/xs/xs/xs) IN [0,0]

        m *= xs;
        // ((prod2*xs-m)<<10 - (k4*K4BIT+k5*K5BIT)/xs/xs/xs) IN [0,0]

        m += (1L << 22) + (1L << 9);
        l = u = 0;
        if (!fixBugP) { // o2
            u += 2L << 10;
        }
        // ((prod3-m) - (k3*K3BIT+k4*K4BIT+k5*K5BIT)/xs/xs/xs) IN [0,0]

        long r2 = m & 0x3FF;
        m = m >> 10;
        u += r2;
        l += r2 - 0x3FF;
        // ((res3-m)<<10 - (k3*K3BIT+k4*K4BIT+k5*K5BIT)/xs/xs/xs) IN [l,u]

        m *= xs;
        if (xs > 0) {
            l *= xs;
            u *= xs;
        } else {
            long tl = l;
            l = u * xs;
            u = tl * xs;
        }
        // ((res3*xs-m)<<10 - (k3*K3BIT+k4*K4BIT+k5*K5BIT)/xs/xs) IN [l,u]

        m += (5L << 19) + (1L << 9);
        if (!fixBugP) { // p3
            u += 1L << 10;
        }
        // ((prod4-m)<<10) - (k2*K2BIT+k3*K3BIT+k4*K4BIT+k5*K5BIT)/xs/xs) IN [0,0]

        long r3 = m & 0x3FF;
        m = m >> 10;
        u += r3 << 10;
        l += (r3 - 0x3FF) << 10;
        // ((res4-m)<<20 - (k2*K2BIT+k3*K3BIT+k4*K4BIT+k5*K5BIT)/xs/xs) IN [l,u]

        m *= xs;
        if (xs > 0) {
            l *= xs;
            u *= xs;
        } else {
            long tl = l;
            l = u * xs;
            u = tl * xs;
        }
        // ((res4*xs-m)<<20 - (k2*K2BIT+k3*K3BIT+k4*K4BIT+k5*K5BIT)/xs) IN [l,u]

        m += -(195L << 16) - 1;
        if (!fixBugP) { // p4
            u += 1L << 20;
        }
        // ((prod5-m)<<20) - (k1*K1BIT+k2*K2BIT+k3*K3BIT+k4*K4BIT+k5*K5BIT)/xs) IN [0,0]

        long r4 = m & 0x3FF;
        m = m >> 10;
        u += r4 << 20;
        l += (r4 - 0x3FF) << 20;
        // ((res5-m)<<30 - (k1*K1BIT+k2*K2BIT+k3*K3BIT+k4*K4BIT+k5*K5BIT)/xs) IN [l,u]

        m *= xs;
        if (xs > 0) {
            l *= xs;
            u *= xs;
        } else {
            long tl = l;
            l = u * xs;
            u = tl * xs;
        }
        // ((res5*xs-m)<<30 - (k1*K1BIT+k2*K2BIT+k3*K3BIT+k4*K4BIT+k5*K5BIT)) IN [l,u]

        m += (1032L << 14) + (1L << 13);
        if (!fixBugP) { // p5
            l += 1L << 30;
            u += 2L << 30;
        }
        // (((prod6-m)<<30) - (k1*K1BIT+k2*K2BIT+k3*K3BIT+k4*K4BIT+k5*K5BIT)) IN [l,u]
    }

    public static void main(String[] args) {
        boolean fixBugP = false;
        List<List<PolyState.Inp>> inps = Arrays.asList(Arrays.asList(PolyState.Inp.genNom()));
        for (int chipNo = 0; chipNo < inps.size(); chipNo++) {
            List<PolyState.Inp> inps1 = inps.get(chipNo);
            for (PolyState.Inp inp : inps1) {
                System.out.println((chipNo + 1) + ":" + inp.toNom());
                for (int adcOut = 0; adcOut < 4096; adcOut++) {
                    inp.T = adcOut;
                    PolyModel pm = PolyModel.computePolyModel(inp, fixBugP);

                    IntModel im = new IntModel(pm.xs, fixBugP);
                    im.setMLU(inp.K1BIT, inp.K2BIT, inp.K3BIT, inp.K4BIT, inp.K5BIT);
                    assert im.l == 0 && im.u == 0;
                    assert im.m == pm.pr0.result;

                    im.setMLU(inp.K2BIT, inp.K3BIT, inp.K4BIT, inp.K5BIT);
                    long lin = im.k1 * inp.K1BIT;
                    assert im.m + ((im.l + lin) >> 30) <= pm.pr0.result;
                    assert im.m + ((im.u + lin) >> 30) >= pm.pr0.result;
                    long l = im.m + ((im.l + lin) >> 30) >> 14;
                    long u = im.m + ((im.u + lin) >> 30) >> 14;
                    if (l != u) {
                        System.out.println("2:adcOut=" + adcOut + " xs=" + im.xs + " result=" + Long.toHexString(pm.pr0.result)
                                + " result>>=" + Long.toHexString(pm.pr0.result >> 14)
                                + " m+l=" + Long.toHexString(l)
                                + " m+u=" + Long.toHexString(u));
                    }

                    im.setMLU(inp.K3BIT, inp.K4BIT, inp.K5BIT);
                    lin = im.k1 * inp.K1BIT + im.k2 * inp.K2BIT;
                    assert im.m + ((im.l + lin) >> 30) <= pm.pr0.result;
                    assert im.m + ((im.u + lin) >> 30) >= pm.pr0.result;
                    l = im.m + ((im.l + lin) >> 30) >> 14;
                    u = im.m + ((im.u + lin) >> 30) >> 14;
                    if (l != u) {
                        System.out.println("3:adcOut=" + adcOut + " xs=" + im.xs + " result=" + Long.toHexString(pm.pr0.result)
                                + " result>>=" + Long.toHexString(pm.pr0.result >> 14)
                                + " m+l=" + Long.toHexString(l)
                                + " m+u=" + Long.toHexString(u));
                    }

                    im.setMLU(inp.K4BIT, inp.K5BIT);
                    lin = im.k1 * inp.K1BIT + im.k2 * inp.K2BIT + im.k3 * inp.K3BIT;
                    assert im.m + ((im.l + lin) >> 30) <= pm.pr0.result;
                    assert im.m + ((im.u + lin) >> 30) >= pm.pr0.result;
                    l = im.m + ((im.l + lin) >> 30) >> 14;
                    u = im.m + ((im.u + lin) >> 30) >> 14;
                    if (l != u) {
                        System.out.println("4:adcOut=" + adcOut + " xs=" + im.xs + " result=" + Long.toHexString(pm.pr0.result)
                                + " result>>=" + Long.toHexString(pm.pr0.result >> 14)
                                + " m+l=" + Long.toHexString(l)
                                + " m+u=" + Long.toHexString(u));
                    }

                    im.setMLU(inp.K5BIT);
                    lin = im.k1 * inp.K1BIT + im.k2 * inp.K2BIT + im.k3 * inp.K3BIT + im.k4 * inp.K4BIT;
                    assert im.m + ((im.l + lin) >> 30) <= pm.pr0.result;
                    assert im.m + ((im.u + lin) >> 30) >= pm.pr0.result;
                    l = im.m + ((im.l + lin) >> 30) >> 14;
                    u = im.m + ((im.u + lin) >> 30) >> 14;
                    if (l != u) {
                        System.out.println("5:adcOut=" + adcOut + " xs=" + im.xs + " result=" + Long.toHexString(pm.pr0.result)
                                + " result>>=" + Long.toHexString(pm.pr0.result >> 14)
                                + " m+l=" + Long.toHexString(l)
                                + " m+u=" + Long.toHexString(u));
                    }

                    im.setMLU();
                    lin = im.k1 * inp.K1BIT + im.k2 * inp.K2BIT + im.k3 * inp.K3BIT + im.k4 * inp.K4BIT + im.k5 * inp.K5BIT;
                    assert im.m + ((im.l + lin) >> 30) <= pm.pr0.result;
                    assert im.m + ((im.u + lin) >> 30) >= pm.pr0.result;
                    l = im.m + ((im.l + lin) >> 30) >> 14;
                    u = im.m + ((im.u + lin) >> 30) >> 14;
                    if (l != u) {
                        System.out.println("6:adcOut=" + adcOut + " xs=" + im.xs + " result=" + Long.toHexString(pm.pr0.result)
                                + " result>>=" + Long.toHexString(pm.pr0.result >> 14)
                                + " m+l=" + Long.toHexString(l)
                                + " m+u=" + Long.toHexString(u));
                    }
                }
            }
        }
    }
}
