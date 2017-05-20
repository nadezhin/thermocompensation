/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.nsc.interval.thermocompensation.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.java.jinterval.rational.Rational;
import net.java.jinterval.rational.RationalOps;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

/**
 *
 */
public class GenDeriv {

    public static PolyState.Inp genZeros() {
        PolyState.Inp lin = PolyState.Inp.genNom();
        lin.INF = 0;
        lin.SBIT = 0;
        lin.K1BIT = 0;
        lin.K2BIT = 0;
        lin.K3BIT = 0;
        lin.K4BIT = 0;
        lin.K5BIT = 0;
        return lin;
    }

    public static PolyState.Inp genRightPosDeriv() {
        PolyState.Inp lin = PolyState.Inp.genNom();
        lin.INF = 0;
        lin.SBIT = 31;
        lin.K1BIT = 1;
        lin.K2BIT = 127;
        lin.K3BIT = 31;
        lin.K4BIT = 31;
        lin.K5BIT = 15;
        return lin;
    }

    public static PolyState.Inp genRightPosDeriv1() {
        PolyState.Inp lin = PolyState.Inp.genNom();
        lin.INF = 0;
        lin.SBIT = 31;
        lin.K1BIT = 255;
        lin.K2BIT = 127;
        lin.K3BIT = 31;
        lin.K4BIT = 31;
        lin.K5BIT = 15;
        return lin;
    }

    public static PolyState.Inp genRightPosDeriv2() {
        PolyState.Inp lin = PolyState.Inp.genNom();
        lin.INF = 0;
        lin.SBIT = 31;
        lin.K1BIT = 255;
        lin.K2BIT = 0;
        lin.K3BIT = 31;
        lin.K4BIT = 31;
        lin.K5BIT = 15;
        return lin;
    }

    public static PolyState.Inp genRightPosDeriv3() {
        PolyState.Inp lin = PolyState.Inp.genNom();
        lin.INF = 0;
        lin.SBIT = 31;
        lin.K1BIT = 255;
        lin.K2BIT = 0;
        lin.K3BIT = 0;
        lin.K4BIT = 31;
        lin.K5BIT = 15;
        return lin;
    }

    public static PolyState.Inp genRightPosDeriv4() {
        PolyState.Inp lin = PolyState.Inp.genNom();
        lin.INF = 0;
        lin.SBIT = 31;
        lin.K1BIT = 255;
        lin.K2BIT = 0;
        lin.K3BIT = 0;
        lin.K4BIT = 0;
        lin.K5BIT = 15;
        return lin;
    }

    public static PolyState.Inp genRightPosDeriv4_14() {
        PolyState.Inp lin = PolyState.Inp.genNom();
        lin.INF = 0;
        lin.SBIT = 31;
        lin.K1BIT = 255;
        lin.K2BIT = 0;
        lin.K3BIT = 0;
        lin.K4BIT = 0;
        lin.K5BIT = 14;
        return lin;
    }

    public static PolyState.Inp genRightPosDeriv4_13() {
        PolyState.Inp lin = PolyState.Inp.genNom();
        lin.INF = 0;
        lin.SBIT = 31;
        lin.K1BIT = 255;
        lin.K2BIT = 0;
        lin.K3BIT = 0;
        lin.K4BIT = 0;
        lin.K5BIT = 13;
        return lin;
    }

    public static PolyState.Inp genRightPosDeriv4_8() {
        PolyState.Inp lin = PolyState.Inp.genNom();
        lin.INF = 0;
        lin.SBIT = 31;
        lin.K1BIT = 255;
        lin.K2BIT = 0;
        lin.K3BIT = 0;
        lin.K4BIT = 0;
        lin.K5BIT = 8;
        return lin;
    }

    public static PolyState.Inp genRightPosDeriv4_4() {
        PolyState.Inp lin = PolyState.Inp.genNom();
        lin.INF = 0;
        lin.SBIT = 31;
        lin.K1BIT = 255;
        lin.K2BIT = 0;
        lin.K3BIT = 0;
        lin.K4BIT = 0;
        lin.K5BIT = 4;
        return lin;
    }

    public static PolyState.Inp genRightPosDeriv3_8_3() {
        PolyState.Inp lin = PolyState.Inp.genNom();
        lin.INF = 0;
        lin.SBIT = 31;
        lin.K1BIT = 255;
        lin.K2BIT = 0;
        lin.K3BIT = 0;
        lin.K4BIT = 8;
        lin.K5BIT = 3;
        return lin;
    }

    public static PolyState.Inp genRightPosDeriv3_4_3() {
        PolyState.Inp lin = PolyState.Inp.genNom();
        lin.INF = 0;
        lin.SBIT = 31;
        lin.K1BIT = 255;
        lin.K2BIT = 0;
        lin.K3BIT = 0;
        lin.K4BIT = 4;
        lin.K5BIT = 3;
        return lin;
    }

    public static PolyState.Inp genRightPosDeriv3_3_3() {
        PolyState.Inp lin = PolyState.Inp.genNom();
        lin.INF = 0;
        lin.SBIT = 31;
        lin.K1BIT = 255;
        lin.K2BIT = 0;
        lin.K3BIT = 0;
        lin.K4BIT = 3;
        lin.K5BIT = 3;
        return lin;
    }

    public static PolyState.Inp genRightPosDeriv2_3_2_3() {
        PolyState.Inp lin = PolyState.Inp.genNom();
        lin.INF = 0;
        lin.SBIT = 31;
        lin.K1BIT = 255;
        lin.K2BIT = 0;
        lin.K3BIT = 3;
        lin.K4BIT = 2;
        lin.K5BIT = 3;
        return lin;
    }

    public static PolyState.Inp genRightPosDeriv2_2_2_3() {
        PolyState.Inp lin = PolyState.Inp.genNom();
        lin.INF = 0;
        lin.SBIT = 31;
        lin.K1BIT = 255;
        lin.K2BIT = 0;
        lin.K3BIT = 2;
        lin.K4BIT = 2;
        lin.K5BIT = 3;
        return lin;
    }

    public static PolyState.Inp genRightPosDeriv2_1_2_3() {
        PolyState.Inp lin = PolyState.Inp.genNom();
        lin.INF = 0;
        lin.SBIT = 31;
        lin.K1BIT = 255;
        lin.K2BIT = 0;
        lin.K3BIT = 1;
        lin.K4BIT = 2;
        lin.K5BIT = 3;
        return lin;
    }

    public static PolyState.Inp genRightPosDeriv2_0_2_3() {
        PolyState.Inp lin = PolyState.Inp.genNom();
        lin.INF = 0;
        lin.SBIT = 31;
        lin.K1BIT = 255;
        lin.K2BIT = 0;
        lin.K3BIT = 0;
        lin.K4BIT = 2;
        lin.K5BIT = 3;
        return lin;
    }

    private static Map<String, PolyState.Inp> genRightPosDerivs() {
        Map<String, PolyState.Inp> result = new LinkedHashMap<>();
        result.put("rightPos", genRightPosDeriv());
        result.put("rightPos1", genRightPosDeriv1());
        result.put("rightPos2", genRightPosDeriv2());
        result.put("rightPos3", genRightPosDeriv3());
        result.put("rightPos4", genRightPosDeriv4());
        result.put("rightPos4_14", genRightPosDeriv4_14());
        result.put("rightPos4_13", genRightPosDeriv4_13());
        result.put("rightPos4_8", genRightPosDeriv4_8());
        result.put("rightPos4_4", genRightPosDeriv4_4());
        result.put("rightPos3_8_3", genRightPosDeriv3_8_3());
        result.put("rightPos3_4_3", genRightPosDeriv3_4_3());
        result.put("rightPos3_3_3", genRightPosDeriv3_3_3());
        result.put("rightPos2_3_2_3", genRightPosDeriv2_3_2_3());
        result.put("rightPos2_2_2_3", genRightPosDeriv2_2_2_3());
        result.put("rightPos2_1_2_3", genRightPosDeriv2_1_2_3());
        result.put("rightPos2_0_2_3", genRightPosDeriv2_0_2_3());
        return result;
    }

    public static PolyState.Inp genRightNegDeriv() {
        PolyState.Inp lin = PolyState.Inp.genNom();
        lin.INF = 0;
        lin.SBIT = 31;
        lin.K1BIT = 255;
        lin.K2BIT = 0;
        lin.K3BIT = 0;
        lin.K4BIT = 0;
        lin.K5BIT = 0;
        return lin;
    }

    public static PolyState.Inp genLeftPosDeriv() {
        PolyState.Inp lin = PolyState.Inp.genNom();
        lin.INF = 63;
        lin.SBIT = 31;
        lin.K1BIT = 1;
        lin.K2BIT = 0;
        lin.K3BIT = 31;
        lin.K4BIT = 0;
        lin.K5BIT = 15;
        return lin;
    }

    public static PolyState.Inp genLeftNegDeriv() {
        PolyState.Inp lin = PolyState.Inp.genNom();
        lin.INF = 63;
        lin.SBIT = 31;
        lin.K1BIT = 255;
        lin.K2BIT = 127;
        lin.K3BIT = 0;
        lin.K4BIT = 31;
        lin.K5BIT = 0;
        return lin;
    }

    private static PolyState.Inp genRump_0(int inf) {
        PolyState.Inp lin = PolyState.Inp.genNom();
        lin.INF = inf;
        lin.SBIT = 31;
        lin.K1BIT = 255;
        lin.K2BIT = 127;
        lin.K3BIT = 2;
        lin.K4BIT = 30;
        lin.K5BIT = 4;
        return lin;
    }

    private static PolyState.Inp genRump_1(int inf) {
        PolyState.Inp lin = PolyState.Inp.genNom();
        lin.INF = inf;
        lin.SBIT = 31;
        lin.K1BIT = 255;
        lin.K2BIT = 127;
        lin.K3BIT = 0;
        lin.K4BIT = 31;
        lin.K5BIT = 15;
        return lin;
    }

    private static PolyState.Inp genRump_2(int inf) {
        PolyState.Inp lin = PolyState.Inp.genNom();
        lin.INF = inf;
        lin.SBIT = 31;
        lin.K1BIT = 255;
        lin.K2BIT = 127;
        lin.K3BIT = 0;
        lin.K4BIT = 31;
        lin.K5BIT = 0;
        return lin;
    }

    private static PolyState.Inp genRump_3(int inf) {
        PolyState.Inp lin = PolyState.Inp.genNom();
        lin.INF = inf;
        lin.SBIT = 31;
        lin.K1BIT = 1;
        lin.K2BIT = 127;
        lin.K3BIT = 31;
        lin.K4BIT = 31;
        lin.K5BIT = 15;
        return lin;
    }

    private static PolyState.Inp genRump_10(int inf) {
        PolyState.Inp lin = PolyState.Inp.genNom();
        lin.INF = inf;
        lin.SBIT = 31;
        lin.K1BIT = 255;
        lin.K2BIT = 127;
        lin.K3BIT = 31;
        lin.K4BIT = 31;
        lin.K5BIT = 15;
        return lin;
    }

    private static PolyState.Inp genRump_11(int inf) {
        PolyState.Inp lin = PolyState.Inp.genNom();
        lin.INF = inf;
        lin.SBIT = 31;
        lin.K1BIT = 255;
        lin.K2BIT = 0;
        lin.K3BIT = 0;
        lin.K4BIT = 12;
        lin.K5BIT = 15;
        return lin;
    }

    private static PolyState.Inp genRump_12(int inf) {
        PolyState.Inp lin = PolyState.Inp.genNom();
        lin.INF = inf;
        lin.SBIT = 31;
        lin.K1BIT = 255;
        lin.K2BIT = 0;
        lin.K3BIT = 0;
        lin.K4BIT = 0;
        lin.K5BIT = 7;
        return lin;
    }

    private static PolyState.Inp genRump_13(int inf) {
        PolyState.Inp lin = PolyState.Inp.genNom();
        lin.INF = inf;
        lin.SBIT = 31;
        lin.K1BIT = 255;
        lin.K2BIT = 0;
        lin.K3BIT = 1;
        lin.K4BIT = 2;
        lin.K5BIT = 4;
        return lin;
    }

    private static Map<String, PolyState.Inp> genRumpsMinMax() {
        Map<String, PolyState.Inp> result = new LinkedHashMap<>();
        result.put("rightPos", genRightPosDeriv());
        result.put("rightNeg", genRightNegDeriv());
        result.put("leftPos", genLeftPosDeriv());
        result.put("leftNeg", genLeftNegDeriv());
        result.put("rump0min", genRump_0(0));
        result.put("rump0max", genRump_0(63));
        result.put("rump1min", genRump_1(0));
        result.put("rump1max", genRump_1(63));
        result.put("rump2min", genRump_2(0));
        result.put("rump2max", genRump_2(63));
        result.put("rump3min", genRump_3(0));
        result.put("rump3max", genRump_3(63));
        result.put("rump10min", genRump_10(0));
        result.put("rump10max", genRump_10(63));
        result.put("rump11min", genRump_11(0));
        result.put("rump11max", genRump_11(63));
        result.put("rump12min", genRump_12(0));
        result.put("rump12max", genRump_12(63));
        result.put("rump13min", genRump_13(0));
        result.put("rump13max", genRump_13(63));
        return result;
    }

    private static Map<String, PolyState.Inp> genRumpsGrid() {
        Map<String, PolyState.Inp> result = new LinkedHashMap<>();
        for (int i = 0; i < 64; i++) {
            result.put("00_" + i, genRump_0(i));
        }
        for (int i = 0; i < 64; i++) {
            result.put("01_" + i, genRump_1(i));
        }
        for (int i = 0; i < 64; i++) {
            result.put("02_" + i, genRump_2(i));
        }
        for (int i = 0; i < 64; i++) {
            result.put("03_" + i, genRump_3(i));
        }
        for (int i = 0; i < 64; i++) {
            result.put("10_" + i, genRump_10(i));
        }
        for (int i = 0; i < 64; i++) {
            result.put("11_" + i, genRump_11(i));
        }
        for (int i = 0; i < 64; i++) {
            result.put("12_" + i, genRump_12(i));
        }
        for (int i = 0; i < 64; i++) {
            result.put("13_" + i, genRump_13(i));
        }
        return result;
    }

    private static int monotonic(PolyState.Inp inp, int t, int dv) {
        int sign = 0;
        inp.T = t;
        int prevMinV = PolyModel.compute(inp);
        int prevMaxV = prevMinV;
        int dt = 0;
        for (;;) {
            if (t + dt + 1 <= 4095) {
                inp.T = t + dt + 1;
                int maxV = PolyModel.compute(inp);
                if (maxV == prevMaxV) {
                    return dt;
                }
                if (sign == 0) {
                    sign = maxV > prevMaxV ? 1 : -1;
                } else if (sign > 0) {
                    if (maxV < prevMaxV + dv) {
                        return dt;
                    }
                } else if (sign < 0) {
                    if (maxV > prevMaxV - dv) {
                        return dt;
                    }
                }
                prevMaxV = maxV;
            }
            if (t - dt - 1 >= 0) {
                inp.T = t - dt - 1;
                int minV = PolyModel.compute(inp);
                if (minV == prevMinV) {
                    return dt;
                }
                if (sign == 0) {
                    sign = minV > prevMinV ? -1 : 1;
                } else if (sign > 0) {
                    if (minV > prevMinV - dv) {
                        return dt;
                    }
                } else if (sign < 0) {
                    if (minV < prevMinV + dv) {
                        return dt;
                    }
                }
                prevMinV = minV;
            }
            dt++;
        }
    }

    private static void bestMonotonic(Map<String, PolyState.Inp> inps, int t) {
        Map.Entry<String, PolyState.Inp> best = null;
        int bestM = 0;
        for (Map.Entry<String, PolyState.Inp> e : inps.entrySet()) {
            String name = e.getKey();
            PolyState.Inp inp = e.getValue();
            int m = monotonic(inp, t, 3);
            if (best == null || m > bestM) {
                best = e;
                bestM = m;
            }
        }
        System.out.println(t + ": " + best.getKey() + " " + bestM + " " + monotonic(best.getValue(), t, 4));
    }

//    private static void bestRump(Map<String,PolyState.Inp> inps, int t) {
//        Map.Entry<String,PolyState.Inp> best = null;
//        int bestMon = -1;
//        for (Map.Entry<String,PolyState.Inp> e: inps.entrySet()) {
//            String name = e.getKey();
//            PolyState.Inp inp = e.getValue();
//            inp.T = tmin;
//            int min = PolyModel.compute(inp);
//            inp.T = tmax;
//            int max = PolyModel.compute(inp);
//            int absDiff = Math.abs(max - min);
//            if (best == null || absDiff > bestAbsDiff) {
//                best = e;
//                bestAbsDiff = absDiff;
//            }
//        }
//        System.out.println(t +": "+ best.getKey() + " " + bestAbsDiff/(tmax-tmin));
//    }
//
//    private static void showBestRumps() {
//        Map<String,PolyState.Inp> inps = genRumps();
//        for (int i = 0; i < 4096; i++) {
//            bestRump(inps, i);
//        }
//    }
    private static void showBestMonotonic() {
        Map<String, PolyState.Inp> inps = genRumpsGrid();
        for (int i = 0; i < 4096; i++) {
            bestMonotonic(inps, i);
        }
    }

    private static PolyState.Inp genRightPosDeriv(int i, int lev) {
        PolyState.Inp inp = genRightPosDeriv();
        inp.T = i;
        while (inp.K1BIT < 255 && PolyModel.compute(inp) > lev) {
            inp.K1BIT++;
        }
        while (inp.K2BIT > 0 && PolyModel.compute(inp) > lev) {
            inp.K2BIT--;
        }
        while (inp.K3BIT > 0 && PolyModel.compute(inp) > lev) {
            inp.K3BIT--;
        }
        while (inp.K4BIT > 0 && PolyModel.compute(inp) > lev) {
            inp.K4BIT--;
        }
        while (inp.K5BIT > 0 && PolyModel.compute(inp) > lev) {
            inp.K5BIT--;
        }
        return inp;
    }

    private static void testPosDerivs() {
//        int lev = 2048;
        int lev = 4094;
        for (int i = 1535; i < 4096; i++) {
            PolyState.Inp inp = genRightPosDeriv(i, lev);
            int v = PolyModel.compute(inp);
            System.out.print(i + ": " + inp.toNom() + " " + v);
            inp.T--;
            System.out.println(" " + (v - PolyModel.compute(inp)));
        }
    }

    private static Rational calcDerivBound(Rational xs, int p) {
        if (xs.signum() <= 0) {
            return Rational.zero();
        }
        Rational[] v = new Rational[6];
        PolyModel.fillCoefSpecification(v, xs);
        Rational vlim = Rational.valueOf(4095);
        v[0] = RationalOps.sub(v[0], vlim);
        Rational[] d = new Rational[6];
        PolyModel.fillCoefDerivSpecification(d, xs);
        Rational[] md = new Rational[6];
        Rational kd = RationalOps.div(d[p], v[p]);
        assert kd.signum() > 0;
        for (int i = 0; i < 6; i++) {
            md[i] = RationalOps.mul(v[i], kd);
        }
        for (int i = 1; i <= 5; i++) {
            Rational di = RationalOps.sub(d[i], md[i]);
            if (di.signum() > 0) {
                md[i] = RationalOps.add(di, md[i]);
                md[0] = RationalOps.sub(md[0], RationalOps.mul(di, (Rational) PolyModel.KBITranges[i].sup()));
            } else if (di.signum() < 0) {
                md[i] = RationalOps.add(di, md[i]);
                md[0] = RationalOps.sub(md[0], RationalOps.mul(di, (Rational) PolyModel.KBITranges[i].inf()));
            }
        }
        return RationalOps.sub(d[0], md[0]);
    }

//    private static void showDeriv(String fileName, Map<String, PolyState.Inp> inpMap) {
//        String[] names = inpMap.keySet().toArray(new String[inpMap.size()]);
//        PolyState.Inp[] inps = inpMap.values().toArray(new PolyState.Inp[inpMap.size()]);
//        int k = 0;
//        for (Map.Entry<String, PolyState.Inp> e : inpMap.entrySet()) {
//            names[k] = e.getKey();
//            inps[k] = e.getValue();
//            k++;
//        }
//        int nVars = inpMap.size() * 3 + 1 + 5;
//        RawFile rf = new RawFile(4096, nVars);
//        int iVar = 0;
//        rf.setVar(iVar++, "inp");
//        for (k = 0; k < names.length; k++) {
//            rf.setVar(iVar++, "o" + names[k]);
//            rf.setVar(iVar++, "r" + names[k]);
//            rf.setVar(iVar++, "d" + names[k]);
//        }
//        for (int p = 1; p <= 5; p++) {
//            rf.setVar(iVar++, "b" + p);
//        }
//        assert iVar == nVars;
//        for (int i = 0; i < 4096; i++) {
//            iVar = 0;
//            rf.set(i, iVar++, i);
//            for (k = 0; k < names.length; k++) {
//                PolyState.Inp inp = inps[k];
//                inp.T = i;
//                int impl = PolyModel.compute(inp);
//                rf.set(i, iVar++, impl);
//                Rational xs = Rational.valueOf(PolyModel.computeXS(inp.INF, inp.SBIT, inp.T));
//                Rational[] t = new Rational[6];
//                PolyModel.fillCoefSpecification(t, xs);
//                Rational v = t[0];
//                v = RationalOps.fma(Rational.valueOf(inp.K1BIT), t[1], v);
//                v = RationalOps.fma(Rational.valueOf(inp.K2BIT), t[2], v);
//                v = RationalOps.fma(Rational.valueOf(inp.K3BIT), t[3], v);
//                v = RationalOps.fma(Rational.valueOf(inp.K4BIT), t[4], v);
//                v = RationalOps.fma(Rational.valueOf(inp.K5BIT), t[5], v);
//                boolean saturate = v.signum() <= 0 || v.ge(Rational.valueOf(4095));
//                v = RationalOps.min(RationalOps.max(v, Rational.valueOf(0)), Rational.valueOf(4095));
//                rf.set(i, iVar++, v.doubleValue());
//                PolyModel.fillCoefDerivSpecification(t, xs);
//                v = t[0];
//                v = RationalOps.fma(Rational.valueOf(inp.K1BIT), t[1], v);
//                v = RationalOps.fma(Rational.valueOf(inp.K2BIT), t[2], v);
//                v = RationalOps.fma(Rational.valueOf(inp.K3BIT), t[3], v);
//                v = RationalOps.fma(Rational.valueOf(inp.K4BIT), t[4], v);
//                v = RationalOps.fma(Rational.valueOf(inp.K5BIT), t[5], v);
//                rf.set(i, iVar++, saturate ? 0 : v.doubleValue());
//            }
//            for (int p = 1; p <= 5; p++) {
//                PolyState.Inp inp = inps[0];
//                Rational xs = Rational.valueOf(PolyModel.computeXS(inp.INF, inp.SBIT, inp.T));
//                rf.set(i, iVar++, calcDerivBound(xs, p).doubleValue());
//            }
//            assert iVar == nVars;
//        }
//        rf.write(fileName);
//    }
    private static void testMatrix() {
        double dt = 2056;
//        double dt = 500;
        RealMatrix A = new Array2DRowRealMatrix(3, 3);
        A.setEntry(0, 0, dt / (1 << 8));
        A.setEntry(0, 1, dt * dt / (1 << 16));
        A.setEntry(0, 2, dt * dt * dt / (1 << 24));
        A.setEntry(1, 0, 1. / (1 << 8));
        A.setEntry(1, 1, dt * 2 / (1 << 16));
        A.setEntry(1, 2, dt * dt * 3 / (1 << 24));
        A.setEntry(2, 0, 0);
        A.setEntry(2, 1, 2. / (1 << 16));
        A.setEntry(2, 2, 6 * dt / (1 << 24));
        RealVector b = new ArrayRealVector(3);
        b.setEntry(0, 2048 - 1032 - 3. / 8 * dt * dt * dt * dt / (1L << 32) - 7. / 16 * dt * dt * dt * dt * dt / (1L << 40));
        b.setEntry(1, 32 - 3. / 8 * 4 * dt * dt * dt / (1L << 32) - 7. / 16 * 5 * dt * dt * dt * dt / (1L << 40));
        b.setEntry(2, 0);
        RealVector x = new LUDecomposition(A).getSolver().solve(b);
        System.out.println(x.getEntry(0) + " " + x.getEntry(1) + " " + x.getEntry(2));
    }

//    public static void main(String[] args) {
//        showDeriv("deriv.raw", genRightPosDerivs());
//        testPosDerivs();
//        testMatrix();
//        testPosDerivs();
//        showBestMonotonic();
//    }
}
