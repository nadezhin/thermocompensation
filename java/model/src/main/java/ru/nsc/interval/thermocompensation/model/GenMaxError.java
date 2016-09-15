package ru.nsc.interval.thermocompensation.model;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
//import parse.RawFile;

/**
 *
 */
public class GenMaxError {

    private static PolyState.Inp fromS(String s) {
        return new PolyState.Inp(DeviceParams.fromNom(s));
    }

    private static Map<String, PolyState.Inp> genDocTests() {
        Map<String, PolyState.Inp> map = new LinkedHashMap<>();
//        map.put("01", fromS("32 16 130  40 24 18  2 0 0 0 7"));
//        map.put("02", fromS("32 16 255 127 31  0 15 0 0 0 7"));
//        map.put("03", fromS("32 16 250 127 31 31  0 0 0 0 7"));
//        map.put("04", fromS("32 16  30   0  0 25  5 0 0 0 7"));
//        map.put("05", fromS("32 16   0   0  0 26  5 0 0 0 7"));
//        map.put("06", fromS("32 16 200   0 31 19  9 0 0 0 7"));
//        map.put("07", fromS("32  6 255 127 31 31 15 0 0 0 7"));
//        map.put("08", fromS("32 31 190 127  6 12  2 0 0 0 7"));
//        map.put("09", fromS("10 16 200 100 15 12  6 0 0 0 7"));
//        map.put("10", fromS("63 16 255 127 31 31  6 0 0 0 7"));
        map.put("0", fromS("0 31 255 0 0 0 4 0 0 0 7"));
        return map;
    }

//    private static void showDocTests(String fileName) {
//        Map<String, PolyState.Inp> map = genDocTests();
//        int nVars = 1 + map.size() * 5;
//        RawFile rf = new RawFile(4096, nVars);
//        int iVar = 0;
//        rf.setVar(iVar++, "t");
//        for (String name : map.keySet()) {
//            rf.setVar(iVar++, "spec" + name);
//            rf.setVar(iVar++, "impl" + name);
//            rf.setVar(iVar++, "err" + name);
//            rf.setVar(iVar++, "implf" + name);
//            rf.setVar(iVar++, "errf" + name);
//        }
//        assert iVar == nVars;
//        for (int i = 0; i < 4096; i++) {
//            iVar = 0;
//            rf.set(i, iVar++, i);
//            for (PolyState.Inp inp : map.values()) {
//                inp.T = i;
//                rf.set(i, iVar++, PolyModel.computeSpecification(inp).doubleValue());
//                rf.set(i, iVar++, PolyModel.compute(inp, true, false));
//                rf.set(i, iVar++, PolyModel.computeError(inp, false).doubleValue());
//                rf.set(i, iVar++, PolyModel.compute(inp, true, true));
//                rf.set(i, iVar++, PolyModel.computeError(inp, true).doubleValue());
//            }
//            assert iVar == nVars;
//        }
//        rf.write(fileName);
//    }
    public static final int minDiff = -1535 - 63 * 8;
    public static final int maxDiff = 4095 - 1535;

    private static void iterateDiff() {
        for (int diff = minDiff; diff <= maxDiff; diff++) {
            PolyState.Inp lInp, uInp;
            if (diff >= 0) {
                lInp = GenDeriv.genRightNegDeriv();
                uInp = GenDeriv.genRightPosDeriv();
            } else {
                lInp = GenDeriv.genLeftPosDeriv();
                uInp = GenDeriv.genLeftNegDeriv();
            }
            assert lInp.INF == (diff >= 0 ? 0 : 63);
            lInp.T = (1535 + (lInp.INF << 3)) + diff;
            assert lInp.T >= 0 && lInp.T <= 4095;
            assert uInp.INF == (diff >= 0 ? 0 : 63);
            uInp.T = (1535 + (uInp.INF << 3)) + diff;
            assert uInp.T >= 0 && uInp.T <= 4095;
            int minLInp = Integer.MAX_VALUE;
            int maxUInp = Integer.MIN_VALUE;
            for (int sbit = 0; sbit < 32; sbit++) {
                lInp.SBIT = sbit;
                uInp.SBIT = sbit;
                int lDacInp = PolyModel.compute(lInp, true, false);
                int uDacInp = PolyModel.compute(uInp, true, false);
                assert lDacInp <= uDacInp;
//                System.out.println("  diff=" + diff + " sbit=" + sbit + " [" + lDacInp + "," + uDacInp + "]");
                minLInp = Math.min(minLInp, lDacInp);
                maxUInp = Math.max(maxUInp, uDacInp);
            }
            System.out.println(diff + " " + minLInp + " " + maxUInp);
        }
    }

    private static class DiffToInp {

        static final int[] lDacInp = new int[maxDiff - minDiff + 1];
        static final int[] uDacInp = new int[maxDiff - minDiff + 1];

        static {
            try {
                Path path = Paths.get(ClassLoader.getSystemResource("polynommoskva/diff2inp.txt").toURI());
                int i = 0;
                for (String s : Files.readAllLines(path, Charset.forName("UTF-8"))) {
                    if (s.isEmpty()) {
                        continue;
                    }
                    String[] ss = s.split(" ");
                    assert ss.length == 3;
                    int diff = Integer.parseInt(ss[0]);
                    assert diff == minDiff + i++;
                    lDacInp[diff - minDiff] = Integer.parseInt(ss[1]);
                    uDacInp[diff - minDiff] = Integer.parseInt(ss[2]);
                }
            } catch (URISyntaxException | IOException e) {
                throw new AssertionError(e);
            }
        }
    }

    public static int getMinDacInpByDiff(int diff) {
        return DiffToInp.lDacInp[diff - minDiff];
    }

    public static int getMaxDacInpByDiff(int diff) {
        return DiffToInp.uDacInp[diff - minDiff];
    }

//    private static void showInpByDiff() {
//        int numVar = 3;
//        RawFile rf = new RawFile(maxDiff - minDiff + 1, numVar);
//        int iVar = 0;
//        rf.setVar(iVar++, "diff");
//        rf.setVar(iVar++, "l");
//        rf.setVar(iVar++, "u");
//        assert iVar == numVar;
//        for (int diff = minDiff; diff <= maxDiff; diff++) {
//            iVar = 0;
//            rf.set(diff - minDiff, iVar++, diff);
//            rf.set(diff - minDiff, iVar++, getMinDacInpByDiff(diff));
//            rf.set(diff - minDiff, iVar++, getMaxDacInpByDiff(diff));
//            assert iVar == numVar;
//        }
//        rf.write("inpbydiff.raw");
//    }
    private static void iterateXs() {
        int xsMin = Integer.MAX_VALUE;
        int xsMax = Integer.MIN_VALUE;
        double[] xsSpecMin = new double[2 * 4096];
        Arrays.fill(xsSpecMin, Double.POSITIVE_INFINITY);
        double[] xsSpecMax = new double[2 * 4096];
        Arrays.fill(xsSpecMax, Double.NEGATIVE_INFINITY);
        List<Set<Integer>> sets = new ArrayList<>();
        for (int i = 0; i < 2 * 4096; i++) {
            sets.add(new TreeSet<Integer>());
        }
        boolean fixBugP = false;
        PolyState.Inp inp = PolyState.Inp.genNom();
        int c00 = 0, c01 = 0, c10 = 0, c11 = 0;
        for (int sbit = 0; sbit < 32; sbit++) {
            inp.SBIT = sbit;
            System.out.println("sbit=" + sbit);
            for (int infbit = 0; infbit < 64; infbit++) {
                inp.INF = infbit;
                for (int adcOut = 0; adcOut < 4096; adcOut++) {
                    inp.T = adcOut;
//                    int dacOut = PolyModel.compute(inp, true, fixBugP);
                    PolyModel.ProductResult prs = PolyModel.computePrs(infbit, sbit, adcOut, fixBugP);
                    if (prs.p1 != 0) {
                        System.out.println("infbit=" + infbit + " adcOut=" + adcOut + " prs.result=" + prs.result + " prs.p1=" + prs.p1 + " prs.p2 =" + prs.p2);
                        if (prs.p2 != 0) {
                            c11++;
                        } else {
                            c10++;
                        }
                    } else if (prs.p2 != 0) {
                        c01++;
                    } else {
                        c00++;
                    }
                    double xsSpec = PolyModel.calcXSspecification(infbit, sbit, adcOut);
                    int xs = (int) (prs.result >> 5);
                    xsMin = Math.min(xsMin, xs);
                    xsMax = Math.max(xsMax, xs);
                    xsSpecMin[4096 + xs] = Math.min(xsSpecMin[4096 + xs], xsSpec);
                    xsSpecMax[4096 + xs] = Math.max(xsSpecMax[4096 + xs], xsSpec);
                    sets.get(4096 + xs).add((int) ((xsSpec - xs) * 32));
                }
            }
        }
        System.out.println("c00=" + c00 + " c01=" + c01 + " c10=" + c10 + " c11=" + c11);
        System.out.println("xsMin=" + xsMin + " xsMax=" + xsMax);
        int totalSets = 0;
        for (int i = 0; i < xsSpecMin.length; i++) {
            int xs = i - 4096;
            System.out.println("xs=" + xs + " " + (xsSpecMin[i] - xs) * 32 + " " + (xsSpecMax[i] - xs) * 32 + " " + sets.get(i));
            totalSets += sets.get(i).size();
        }
        System.out.println("totalSets=" + totalSets);
    }

    private static void showQ() {
        double C0 = 2.663175e-12;
        double L1 = 30.12186e-3;
        double R = 44.1453;
        double C1 = 5.84229e-15;
        double f = 11.99742e6;
        double omega = 2 * Math.PI * f;
        System.out.println("L=" + omega * L1 + " C0=" + 1 / omega / C0 + " C1=" + 1 / omega / C1);
    }

//    public static void main(String[] args) {
//        showQ();
//        iterateDiff();
//        showInpByDiff();
//        iterateXs();
//        showDocTests("doc.raw");
//    }
}
