package ru.nsc.interval.thermocompensation.model;

import java.math.BigInteger;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import net.java.jinterval.interval.set.SetInterval;
import net.java.jinterval.interval.set.SetIntervalOps;
import net.java.jinterval.rational.Rational;
import net.java.jinterval.rational.RationalOps;

/**
 * High-level model of the polynomial evaluator. It tracks evaluation of the
 * polynomial on certain input. There two ways of evaluation - detailed and
 * fast.
 *
 * The detailed way is to create an instance of PolyModel class by its
 * constructor. The constructor performs detailed simulation of the finite state
 * machine for many clocks until it returns the result.
 *
 * The fast way is by static method compute. It is a behavior model of the
 * polynomial evaluator. It is faster but its equivalence to the detailed model
 * has not be proved yet.
 *
 */
public class PolyModel {

    /**
     * An array with ranges of valid KBIT parameters. Notice that KBITranges[1]
     * doesn't include zero.
     */
    public static final SetInterval[] KBITranges = {
        null,
        SetIntervalOps.nums2(1, 255),
        SetIntervalOps.nums2(0, 127),
        SetIntervalOps.nums2(0, 31),
        SetIntervalOps.nums2(0, 31),
        SetIntervalOps.nums2(0, 15)
    };
    public static int DEBUG = 0;
    private final PolyState s0 = new PolyState();
    private final PolyState s1 = new PolyState();
    /**
     * The input of the evaluation
     */
    private final PolyState.Inp inp;
    /**
     * true when model behaves as if the bug in the evaluator is fixed
     */
    private final boolean fixBugP;
    /**
     * The result of evaluation of scaled temperature XS. This is an input of
     * the polynomial
     */
    public final int xs;
    /**
     * Biased temperature
     */
    public final ProductResult prinf;
    /**
     * Scaled temperature
     */
    public final ProductResult prs;
    /**
     * Value of k4 + xs*k5
     */
    public final ProductResult pr4;
    /**
     * Value of k3 + xs*(k4 + xs*k5))
     */
    public final ProductResult pr3;
    /**
     * Value of k2 + xs*(k3 + xs*(k4 + xs*k5)))
     */
    public final ProductResult pr2;
    /**
     * Value of k1 + xs*(k2 + xs*(k3 + xs*(k4 + xs*k5)))
     */
    public final ProductResult pr1;
    /**
     * Value of 1032 + xs*(k1 + xs*(k2 + xs*(k3 + xs*(k4 + xs*k5))))
     */
    public final ProductResult pr0;
    /**
     * Output result
     */
    public final int resultOut;

    /**
     * Result of a Horner step. It contains also carry bits P1 and P2. The
     * polynomial evaluator erroneously uses these bits in evaluation of the
     * next Horner step.
     */
    public static class ProductResult {

        /**
         * 35-bit result of a Horner step
         */
        final long result;
        /**
         * Carry bit
         */
        final int p1;
        /**
         * Carry bit
         */
        final int p2;

        ProductResult(long result, int p1, int p2) {
            this.result = result;
            this.p1 = p1;
            this.p2 = p2;
        }

        public long getResult() {
            return result;
        }

        int getP() {
            return p1 + p2;
        }
    }

    /**
     * Simulate polynom evaluator on certain input
     *
     * @param inp Input
     * @param fixBugP true when model behaves as if the bug in the evaluator is
     * fixed
     */
    PolyModel(PolyState.Inp inp, boolean fixBugP) {
        this.inp = new PolyState.Inp(inp);
        this.fixBugP = fixBugP;
        s0.reset();
        s1.reset();

        s0.posedge(inp, s1, fixBugP);
        s1.negedge(inp, s0);

        s0.posedge(inp, s1, fixBugP);
        s1.negedge(inp, s0);
//        System.out.print("neg:");
//        s1.show();

        prinf = product(0, 34, 0, -1, 0, 0, inp.T, inp.INF << 3, 0, -1535);
        long diff = inp.T - (1535 + (inp.INF << 3));
        assert diff >= -1535 - 63 * 8 && diff <= 4095 - 1535;
        assert diff >= -2039 && diff <= 2560;
        assert prinf.result == diff;
        assert prinf.p1 == 0;
        assert prinf.p2 == (fixBugP ? 0 : inp.T < (inp.INF << 3) || inp.T >= 1535 + (inp.INF << 3) ? 1 : 0);
        int scale = inp.SBIT + 0x10;
        assert 16 <= scale && scale <= 47;
        prs = product(1, 35, 0, diff, prinf.p1, prinf.p2, 0, scale, 16, 0);
        long newProdA = diff * scale;
        assert -95833 <= newProdA && newProdA <= 120320;
        long resA = newProdA + 16 + prinf.p2;
        assert -95817 <= resA && resA <= 120337;
        int p1A = (((newProdA & 0x7FFFFFFFFL) + 16) & ~0x7FFFFFFFFL) != 0 ? 1 : 0;
        int p2A = ((((newProdA + 16) & 0x7FFFFFFFFL) + prinf.p2) & ~0x7FFFFFFFFL) != 0 ? 1 : 0;
        int multA = diff < 0 && (resA & 1) == 0 ? +1 : diff >= 0 && (resA & 1) != 0 ? -1 : 0;
        long oldAccA = newProdA >> 35;
        assert oldAccA == 0 || oldAccA == -1;
        long newAccA = oldAccA + multA * scale;
        assert prs.result == resA;
        assert prs.p1 == (fixBugP ? 0 : (((newAccA & 1) + p1A) & ~1) != 0 ? 1 : 0);
        assert prs.p2 == (fixBugP ? 0 : ((((newAccA + p1A) & 1) + p2A) & ~1) != 0 ? 1 : 0);
        xs = (int) (prs.result >> 5);
        assert xs >= -0x1000 && xs < 0x1000;
        assert xs >= -2995 && xs <= 3760;
        pr4 = product(2, 34, 0, inp.K5BIT, prs.p1, prs.p2, 0, xs,
                (((long) inp.K4BIT) << 9), -25L << 9);
        assert pr4.result == inp.K5BIT * xs + (((long) inp.K4BIT - 25) << 9) + prs.p1 + prs.p2;
        assert pr4.p1 == (fixBugP ? 0 : ((((inp.K5BIT * xs) & 0x7FFFFFFFFL) + (((long) inp.K4BIT) << 9) + prs.p1) & ~0x7FFFFFFFFL) != 0 ? 1 : 0);
        assert pr4.p2 == (fixBugP ? 0 : ((((inp.K5BIT * xs + (((long) inp.K4BIT) << 9) + prs.p1) & 0x7FFFFFFFFL) + ((-25L << 9) & 0x7FFFFFFFFL) + prs.p2) & ~0x7FFFFFFFFL) != 0 ? 1 : 0);
        assert pr4.result >= -2995 * 15 - (25L << 9) && pr4.result <= 3760 * 15 + ((31 - 25) << 9) /*
                 * + 2
                 */;
        if (xs >= 0) {
            assert pr4.result >= -(25L << 9) && pr4.result <= 3760 * 15 + ((31 - 25) << 9) /*
                     * + 2
                     */;
            assert pr4.result >= -12800 && pr4.result <= 59472;
        }
        if (xs <= 0) {
            assert pr4.result >= (-2995) * 15 - (25L << 9) && pr4.result <= ((31 - 25) << 9) + 2;
            assert pr4.result >= -57725 && pr4.result <= 3074;
        }
        assert pr4.result >= -57725 && pr4.result <= 59472;
        pr3 = product(3, 44, 10, pr4.result, pr4.p1, pr4.p2, 0, xs,
                (((long) inp.K3BIT) << 19) + (1L << 9), 1L << 22);
        assert pr3.result == pr4.result * xs + (((long) inp.K3BIT) << 19) + (1L << 9) + (1L << 22) + pr4.p1 + pr4.p2;
        assert pr3.p1 == (fixBugP ? 0 : ((((pr4.result * xs) & 0x7FFFFFFFFL) + (((long) inp.K3BIT) << 19) + (1L << 9) + pr4.p1) & ~0x7FFFFFFFFL) != 0 ? 1 : 0);
        assert pr3.p2 == (fixBugP ? 0 : ((((pr4.result * xs + (((long) inp.K3BIT) << 19) + (1L << 9) + pr4.p1) & 0x7FFFFFFFFL) + ((1L << 22) & 0x7FFFFFFFFL) + pr4.p2) & ~0x7FFFFFFFFL) != 0 ? 1 : 0);
        long res3 = pr3.result >> 10;
        if (xs >= 0) {
            assert pr3.result >= 3760 * (-12800L) + (1L << 9) + (1L << 22) && pr3.result <= 3760 * 59472L + (31L << 19) + (1L << 9) + (1L << 22) + 1/*
                     * 2
                     */;
            assert pr3.result >= -43933184L && pr3.result <= 244062465L;
            assert res3 >= -42904L && res3 <= 238342L;
        }
        if (xs <= 0) {
            assert pr3.result >= (-2995) * 3074 + (1L << 9) + (1L << 22) && pr3.result <= (-2995) * (-57725) + (31L << 19) + (1L << 9) + (1L << 22) + 2;
            assert pr3.result >= -5011814L && pr3.result <= 193334121L;
            assert res3 >= -4895L && res3 <= 188802L;
        }
        assert pr3.result >= -43933184L && pr3.result <= 244062465L;
        assert res3 >= -42904L && res3 <= 238342L;
        pr2 = product(4, 44, 10, res3, pr3.p1, pr3.p2, 0, xs,
                (((long) inp.K2BIT) << 17) + (1L << 9), 5L << 19);
        assert pr2.result == res3 * xs + (((long) inp.K2BIT) << 17) + (1L << 9) + (5L << 19) + pr3.p1 + pr3.p2;
        assert pr2.p1 == (fixBugP ? 0 : ((((res3 * xs) & 0x7FFFFFFFFL) + (((long) inp.K2BIT) << 17) + (1L << 9) + pr3.p1) & ~0x7FFFFFFFFL) != 0 ? 1 : 0);
        assert pr2.p2 == (fixBugP ? 0 : ((((res3 * xs + (((long) inp.K2BIT) << 17) + (1L << 9) + pr3.p1) & 0x7FFFFFFFFL) + ((5L << 19) & 0x7FFFFFFFFL) + pr3.p2) & ~0x7FFFFFFFFL) != 0 ? 1 : 0);
        long res4 = pr2.result >> 10;
        if (xs >= 0) {
            assert pr2.result >= 3760 * (-42904L) + (1L << 9) + (5L << 19) && pr2.result <= 3760 * 238342L + (127L << 17) + (1L << 9) + (5L << 19) /*
                     * + 2
                     */;
            assert pr2.result >= -158697088L && pr2.result <= 915434016L;
            assert res4 >= -154978L && res4 <= 893978L;
        }
        if (xs <= 0) {
            assert pr2.result >= (-2995) * 188802L + (1L << 9) + (5L << 19) && pr2.result <= (-2995) * (-4895L) + (127L << 17) + (1L << 9) + (5L << 19) + 2;
            assert pr2.result >= -562840038L && pr2.result <= 33928623L;
            assert res4 >= -549649L && res4 <= 33133L;
        }
        assert pr2.result >= -562840038L && pr2.result <= 915434016L;
        assert res4 >= -549649L && res4 <= 893978L;
        pr1 = product(5, 44, 10, res4, pr2.p1, pr2.p2, 0, xs,
                (-((long) inp.K1BIT) << 17) - 1, -195L << 16);
        assert pr1.result == res4 * xs + (-((long) inp.K1BIT) << 17) - 1 - (195L << 16) + pr2.p1 + pr2.p2;
        assert pr1.p1 == (fixBugP ? 0 : ((((res4 * xs) & 0x7FFFFFFFFL) + (((-((long) inp.K1BIT) << 17) - 1) & 0x7FFFFFFFFL) + pr2.p1) & ~0x7FFFFFFFFL) != 0 ? 1 : 0);
        assert pr1.p2 == (fixBugP ? 0 : ((((res4 * xs + (-((long) inp.K1BIT) << 17) - 1 + pr2.p1) & 0x7FFFFFFFFL) + ((-195L << 16) & 0x7FFFFFFFFL) + pr2.p2) & ~0x7FFFFFFFFL) != 0 ? 1 : 0);
        long res5 = pr1.result >> 10;
        if (xs >= 0) {
            assert pr1.result >= 3760 * (-154978L) + (-255L << 17) - 1 + (-195L << 16) && pr1.result <= 3760 * 893978L - 1 + (-195L << 16) /*
                     * + 2
                     */;
            assert pr1.result >= -628920161L && pr1.result <= 3348577759L;
            assert res5 >= -614180L && res5 <= 3270095L;
        }
        if (xs <= 0) {
            assert pr1.result >= (-2995) * 33133L + (-255L << 17) - 1 + (-195L << 16) && pr1.result <= (-2995) * (-549649L) - 1 + (-195L << 16) + 2;
            assert pr1.result >= -145436216L && pr1.result <= 1633419236L;
            assert res5 >= -142028L && res5 <= 1595135L;

        }
        assert pr1.result >= -628920161L && pr1.result <= 3348577759L;
        assert res5 >= -614180L && res5 <= 3270095L;
        pr0 = product(6, 48, 14, res5, pr1.p1, pr1.p2, 0, xs,
                1L << 13, 1032L << 14);
        assert pr0.result == res5 * xs + (1L << 13) + (1032L << 14) + pr1.p1 + pr1.p2;
        assert pr0.p1 == (fixBugP ? 0 : ((((res5 * xs) & 0x7FFFFFFFFL) + (1L << 13) + pr1.p1) & ~0x7FFFFFFFFL) != 0 ? 1 : 0);
        assert pr0.p2 == (fixBugP ? 0 : ((((res5 * xs + (1L << 13) + pr1.p1) & 0x7FFFFFFFFL) + (1032L << 14) + pr1.p2) & ~0x7FFFFFFFFL) != 0 ? 1 : 0);
        long res6 = pr0.result >> 14;
        if (xs >= 0) {
            assert pr0.result >= 3760 * (-614180L) + (1L << 13) + (1032L << 14) && pr0.result <= 3760 * 3270095L + (1L << 13) + (1032L << 14) + 2;
            assert pr0.result >= -2292400320L && pr0.result <= 12312473682L;
            assert res6 >= -139918L && res6 <= 751493L;
        }
        if (xs <= 0) {
            assert pr0.result >= (-2995) * 1595135L + (1L << 13) + (1032L << 14) && pr0.result <= (-2995) * (-142028L) + (1L << 13) + (1032L << 14) + 2;
            assert pr0.result >= -4760512845L && pr0.result <= 442290342L;
            assert res6 >= -290559L && res6 <= 26995L;
        }
        assert pr0.result >= -4760512845L && pr0.result <= 12312473682L;
        assert res6 >= -290559L && res6 <= 751493L;
//        s1.show();
        assert s1.ENwork_trig0 == 1 && s1.ENwork_trig1 == 1;
        assert s1.ENshift_trig0 == 0 && s1.ENshift_trig1 == 0 && s1.ENshift_trig2 == 0 && s1.ENshift_trig3 == 0 && s1.ENshift_trig4 == 0;
        assert s1.EN_trig == 1;
        assert s1.CNTS == 12;
        assert s1.CNTM == 0;
        assert s1.CNTD == 0;
        assert s1.CNTP == 7;
        assert s1.DONE == 1;
//            if (k == 0) {
//                assert s1.WORK == (acc & 0x1FFF);
//            } else {
//                int mask1 = (1 << k) - 1;
//                int sumK1 = (acc & mask1) + (xsCom & mask1) + carry;
//                int oldMask1 = (1 << (13 - k)) - 1;
//                assert (sumK1 & mask1) == (accNew & mask1);
//                assert s1.P == ((sumK1 & ~mask1) != 0 ? 1 : 0);
//                assert s1.WORK == (((accNew & mask1) << (13 - k)) | (acc >> k) & oldMask1);
//            }
        assert s1.XS == (xs & 0x1FFF);
        assert s1.P1 == pr0.p1;
        assert s1.P2 == pr0.p2;
        assert s1.RESULT == (res6 & 0x7FFFFFFFFL);
        assert s1.RESULTout == 0;
//            assert s1.RESULTn_1 == oldRes1 && s1.RESULTn_2 == oldRes2;
        s0.posedge(inp, s1, fixBugP);
        resultOut = (res6 < 0 ? 0 : res6 >= (1L << 12) ? 4095 : (int) res6);
        assert s0.RESULTout == resultOut;
    }

    /**
     * Behavior model of the polynomial evaluator.
     *
     * @param inp input with coefficients and temperature
     * @param fixBugP fix the bug
     * @return result of computation
     *
     */
    public static PolyModel computePolyModel(PolyState.Inp inp, boolean fixBugP) {
        inp.ENwork = 1;
        return new PolyModel(inp, fixBugP);
    }

    /**
     * Return P0 garbage carryout.
     *
     * @return P0
     */
    public int getP0() {
        return fixBugP ? 0 : prinf.getP();
    }

    /**
     * Return P1 garbage carryout.
     *
     * @return P1
     */
    public int getP1() {
        return fixBugP ? 0 : prs.getP();
    }

    /**
     * Return P2 garbage carryout.
     *
     * @return P2
     */
    public int getP2() {
        return fixBugP ? 0 : pr4.getP();
    }

    /**
     * Return P3 garbage carryout.
     *
     * @return P3
     */
    public int getP3() {
        return fixBugP ? 0 : pr3.getP();
    }

    /**
     * Return P4 garbage carryout.
     *
     * @return P4
     */
    public int getP4() {
        return fixBugP ? 0 : pr2.getP();
    }

    /**
     * Return P5 garbage carryout.
     *
     * @return P5
     */
    public int getP5() {
        return fixBugP ? 0 : pr1.getP();
    }

    /**
     * Simulate hardware evaluation of a partial sum of a product
     *
     * @param entrig component of state which starts evaluation
     * @param cntp component of state which counts Horner steps
     * @param cntm component of state which counts multiplications steps
     * @param cntd component of state which says number of truncated bits
     * @param acc accumulator
     * @param oldXs value of XS
     * @param mult Booth multiplier
     * @param oldRes old value of RESULT
     * @param newRes new value of RESULT
     * @param oldRes1 old value of RESULTn_1
     * @param newRes1 new value of RESULTn_1
     * @param oldRes2 old value of RESULTn_2
     * @param newRes2 new value of RESULTn_2
     * @param oldP1 old value of P1
     * @param newP1 new value of P1
     * @param oldP2 old value of P2
     * @param newP2 new value of P2
     */
    private void partialSum(int entrig, int cntp, int cntm, int cntd, int acc, int oldXs, int mult,
            long oldRes, long newRes,
            int oldRes1, int newRes1,
            int oldRes2, int newRes2,
            int oldP1, int newP1,
            int oldP2, int newP2) {
        int accNew = acc + mult * oldXs;
        int xsCom = mult == 1 ? oldXs : mult == -1 ? ~oldXs : 0;
        int carry = mult == -1 ? 1 : 0;
        if (DEBUG >= 2) {
            System.out.println("acc=" + Integer.toString(acc, 2));
            System.out.println("acc1=" + Integer.toString(accNew, 2));
        }
        for (int k = 0; k <= 12; k++) {
            int mask = (1 << (k + 1)) - 1;
            int sumK = (acc & mask) + (xsCom & mask) + carry;
            assert (sumK & mask) == (accNew & mask);

            assert s1.ENwork_trig0 == 1 && s1.ENwork_trig1 == 1;
            assert s1.ENshift_trig0 == 0 && s1.ENshift_trig1 == 0 && s1.ENshift_trig2 == 0 && s1.ENshift_trig3 == 0 && s1.ENshift_trig4 == 0;
            assert s1.EN_trig == (k == 0 ? entrig : 1);
            assert s1.CNTS == 12 - k;
            assert s1.CNTM == cntm;
            assert s1.CNTD == cntd;
            assert s1.CNTP == cntp;
            assert s1.DONE == 0;
            if (k == 0) {
                assert s1.WORK == (acc & 0x1FFF);
            } else {
                int mask1 = (1 << k) - 1;
                int sumK1 = (acc & mask1) + (xsCom & mask1) + carry;
                int oldMask1 = (1 << (13 - k)) - 1;
                assert (sumK1 & mask1) == (accNew & mask1);
                assert s1.P == ((sumK1 & ~mask1) != 0 ? 1 : 0);
                assert s1.WORK == (((accNew & mask1) << (13 - k)) | (acc >> k) & oldMask1);
            }
            assert s1.XS == ((oldXs << (13 - k)) & 0x1FFF | ((oldXs & 0x1FFF) >> k));
            assert s1.P1 == oldP1;
            assert s1.P2 == oldP2;
            assert s1.RESULT == (oldRes & 0x7FFFFFFFFL) && s1.RESULTn_1 == oldRes1 && s1.RESULTn_2 == oldRes2;
            s0.posedge(inp, s1, fixBugP);
            if (DEBUG >= 3) {
                System.out.print("pos:");
                s0.show();
            }
            assert s0.ENwork_trig0 == 1 && s0.ENwork_trig1 == 1;
            assert s0.ENshift_trig0 == 0 && s0.ENshift_trig1 == 0 && s0.ENshift_trig2 == 0 && s0.ENshift_trig3 == 0 && s0.ENshift_trig4 == 0;
            assert s0.EN_trig == 1;
            assert s0.CNTS == 12 - k;
            assert s0.CNTM == cntm;
            assert s0.CNTD == cntd;
            assert s0.CNTP == cntp;
            assert s0.DONE == 0;
            assert s0.P == ((sumK & ~mask) != 0 ? 1 : 0);
            int oldMask = (1 << (12 - k)) - 1;
            assert (s0.WORK & oldMask) == ((acc >> (k + 1)) & oldMask);
            if (k == 12) {
                assert s0.WORK == (cntm == 0 ? 0 : (accNew >> 1) & 0x1FFF);
                assert s0.P1 == newP1;
                assert s0.P2 == newP2;
                if (cntm != 0) {
                    assert s0.RESULT == newRes;
                }
                assert s0.RESULTn_1 == newRes1;
                assert s0.RESULTn_2 == newRes2;
//                assert s0.XS == newXs;
            } else {
                assert ((s0.WORK >> (12 - k)) & mask) == (accNew & mask);
                assert s0.P1 == oldP1;
                assert s0.P2 == oldP2;
                assert s0.RESULT == (oldRes & 0x7FFFFFFFFL) && s0.RESULTn_1 == oldRes1 && s0.RESULTn_2 == oldRes2;
                assert s0.XS == ((oldXs << (12 - k)) & 0x1FFF | ((oldXs & 0x1FFF) >> (k + 1)));
            }
            s1.negedge(inp, s0);
            if (DEBUG >= 3) {
                System.out.print("neg:");
                s1.show();
            }
            assert s1.ENwork_trig0 == 1 && s1.ENwork_trig1 == 1;
            assert s1.ENshift_trig0 == 0 && s1.ENshift_trig1 == 0 && s1.ENshift_trig2 == 0 && s1.ENshift_trig3 == 0 && s1.ENshift_trig4 == 0;
            assert s1.EN_trig == 1;
            assert s1.CNTS == (k == 12 ? 12 : 11 - k);
            if (k != 12) {
                assert s1.CNTM == cntm && s1.CNTD == cntd;
                assert s1.CNTP == cntp;
                assert s1.DONE == 0;
            } else if (cntm != 0) {
                assert s1.CNTM == cntm - 1 && s1.CNTD == cntd;
                assert s1.CNTP == cntp;
                assert s1.DONE == 0;
            } else {
                assert s1.CNTP == cntp + 1;
                assert s1.DONE == (cntp == 6 ? 1 : 0);
            }
            assert s1.P == ((sumK & ~mask) != 0 ? 1 : 0);
            assert (s1.WORK & oldMask) == ((acc >> (k + 1)) & oldMask);
            if (k == 12) {
                assert s1.WORK == (cntm == 0 ? 0 : (accNew >> 1) & 0x1FFF);
                assert s1.P1 == newP1;
                assert s1.P2 == newP2;
                if (cntm != 0) {
                    assert s1.RESULT == newRes;
                }
                assert s1.RESULTn_1 == newRes1 && s1.RESULTn_2 == newRes2;
//                assert s1.XS == newXs;
            } else {
                assert ((s1.WORK >> (12 - k)) & mask) == (accNew & mask);
                assert s1.P1 == oldP1;
                assert s1.P2 == oldP2;
                assert s1.RESULT == (oldRes & 0x7FFFFFFFFL) && s1.RESULTn_1 == oldRes1 && s1.RESULTn_2 == oldRes2;
                assert s1.XS == ((oldXs << (12 - k)) & 0x1FFF | ((oldXs & 0x1FFF) >> (k + 1)));
            }
        }
        if (DEBUG == 2) {
            s1.show();
        }
    }

    /**
     * Simulate hardware evaluation of a product
     *
     * @param cntp component of state which counts Horner steps
     * @param cntm0 number of multiplications steps
     * @param cntd component of state which says number of truncated bits
     * @param r multiplier
     * @param prevP1 previous value of P1
     * @param prevP2 previous value of P2
     * @param acc0 initial value of accumulator
     * @param xs value of XS
     * @param a1
     * @param a1
     */
    private ProductResult product(int cntp, int cntm0, int cntd, long r, int prevP1, int prevP2, int acc0, int xs, long a1, long a2) {
        long r2 = r << 2;
        assert r >= -0x400000000L && r < 0x400000000L;
        byte[] mults = new byte[35];
        for (int i = 0; i < mults.length; i++) {
            int b0 = PolyState.bit(r2, i + 2);
            int b1 = PolyState.bit(r2, i + 1);
            mults[i] = (byte) (b0 == b1 ? 0 : b0 != 0 ? -1 : +1);
        }

        long rr = 0;
        for (int i = 0; i < mults.length; i++) {
            rr += ((long) mults[i]) << i;
        }
        if (r != rr) {
            System.out.println("r=" + Long.toHexString(r));
            System.out.println("rr=" + Long.toHexString(rr));
        }
        long newProd = acc0 + r * xs;
        long newS1 = newProd + a1 + prevP1;
        long newR = newS1 + a2 + prevP2;
        /*
         * inp.T + r*xs0
         */
        long mult = 0;
        int oldP1 = prevP1;
        int oldP2 = prevP2;
        assert cntm0 - cntd >= 34;
        for (int i = 0; i <= 34; i++) {
            int cntm = cntm0 - i;
            long oldPartial = acc0 + mult * xs;
            long oldAcc = oldPartial >> i;
            long rest = r - mult;
            assert rest == (i == 0 ? r : (r + (1L << (i - 1)) >> i << i));
            mult += ((long) mults[i]) << i;
            if (DEBUG >= 2) {
                System.out.println("oldAcc=" + Long.toString(oldAcc, 16));
            }
            assert oldAcc >= -0x1000 && oldAcc < 0x1000;
            int oldXs = xs;
//            int newXs = i == 34 ? nextXs : xs;
            long oldRes = (newR << (35 - i)) & 0x7FFFFFFFFL | ((r & 0x7FFFFFFFFL) >> i);
            long newRes = (newR << (34 - i)) & 0x7FFFFFFFFL | ((r & 0x7FFFFFFFFL) >> (i + 1)); // ???
            int oldRes1 = (int) ((r2 >> (i + 1)) & 1);
            int newRes1 = cntm == 0 ? 0 : (int) ((r2 >> (i + 2)) & 1);
            int oldRes2 = (int) ((r2 >> (i + 0)) & 1);
            int newRes2 = cntm == 0 ? 0 : (int) ((r2 >> (i + 1)) & 1);
            long mask = (1L << (i + 1)) - 1;
            int newP1 = fixBugP && cntm == 0 ? 0 : (((newProd & mask) + (a1 & mask) + prevP1) & ~mask) != 0 ? 1 : 0;
            int newP2 = fixBugP && cntm == 0 ? 0 : (((newS1 & mask) + (a2 & mask) + prevP2) & ~mask) != 0 ? 1 : 0;
            int entrig = cntp == 0 && cntm == 34 ? 0 : 1;
            assert newR == (s1.RESULT >> (35 - i))
                    + (((long) (s1.P1 + s1.P2)) << i)
                    + (((long) a1) >> i << i)
                    + (((long) a2) >> i << i)
                    + (((long) s1.WORK) << 51 >> (51 - i))
                    + rest * xs;
            partialSum(entrig, cntp, cntm, cntd, (int) oldAcc,
                    oldXs, mults[i],
                    oldRes, newRes,
                    oldRes1, newRes1,
                    oldRes2, newRes2,
                    oldP1, newP1,
                    oldP2, newP2);
            cntm--;
            oldP1 = newP1;
            oldP2 = newP2;
        }
        if (cntp == 1) {
            assert cntm0 == 35 && cntd == 0;
            long oldAcc = newProd >> 35;
            assert oldAcc == 0 || oldAcc == -1;
            long oldRes = newR;
            int mult1 = r < 0 && (newR & 1) == 0 ? +1 : r >= 0 && (newR & 1) != 0 ? -1 : 0;
            long newAcc = oldAcc + mult1 * xs;
            long newRes = ((newAcc + oldP1 + oldP2) << 34) & 0x7FFFFFFFFL | ((newR & 0x7FFFFFFFFL) >> 1);
            int oldRes1 = (int) ((r >> 34) & 1);
            int newRes1 = 0;
            int oldRes2 = (int) ((r >> 33) & 1);
            int newRes2 = 0;
            int newP1 = fixBugP ? 0 : (((newAcc & 1) + oldP1) & ~1) != 0 ? 1 : 0;
            int newP2 = fixBugP ? 0 : ((((newAcc + oldP1) & 1) + oldP2) & ~1) != 0 ? 1 : 0;
            assert (newR == s1.RESULT + (((long) s1.WORK) << 51 >> (51 - 35)));
            partialSum(1, cntp, 0, 0, (int) oldAcc,
                    xs, mult1,
                    oldRes, newRes,
                    oldRes1, newRes1,
                    oldRes2, newRes2,
                    oldP1, newP1,
                    oldP2, newP2);
            oldP1 = newP1;
            oldP2 = newP2;
        } else {
            assert cntm0 == cntd + 34;
            for (int i = 35; i <= cntm0; i++) {
                int cntm = cntm0 - i;
                long oldPartial = acc0 + mult * xs;
                long oldAcc = oldPartial >> i;
//                mult += ((long) mults[i]) << i;
                if (DEBUG >= 2) {
                    System.out.println("oldAcc=" + Long.toString(oldAcc, 16));
                }
                assert oldAcc >= -0x1000 && oldAcc < 0x1000;
                int oldXs = xs;
//            int newXs = i == 34 ? nextXs : xs;
                long oldRes = i > 35 ? (newR >> (i - 35)) & 0x7FFFFFFFFL : (newR << (35 - i)) & 0x7FFFFFFFFL | ((r & 0x7FFFFFFFFL) >> i);
                long newRes = i > 34 ? (newR >> (i - 34)) & 0x7FFFFFFFFL : (newR << (34 - i)) & 0x7FFFFFFFFL | ((r & 0x7FFFFFFFFL) >> (i + 1)); // ???
                int oldRes1 = cntm >= cntd ? (int) ((r2 >> (i + 1)) & 1) : (int) ((r2 >> (cntm0 - cntd + 2)) & 1);
                int newRes1 = cntm == 0 ? 0 : cntm >= cntd ? (int) ((r2 >> (i + 2)) & 1) : (int) ((r2 >> (cntm0 - cntd + 2)) & 1);
                int oldRes2 = cntm >= cntd ? (int) ((r2 >> (i + 0)) & 1) : (int) ((r2 >> (cntm0 - cntd + 1)) & 1);
                int newRes2 = cntm == 0 ? 0 : cntm >= cntd ? (int) ((r2 >> (i + 1)) & 1) : (int) ((r2 >> (cntm0 - cntd + 1)) & 1);
                long mask = (1L << (i + 1)) - 1;
                int newP1 = fixBugP && cntm == 0 ? 0 : (((newProd & mask) + (a1 & mask) + prevP1) & ~mask) != 0 ? 1 : 0;
                int newP2 = fixBugP && cntm == 0 ? 0 : (((newS1 & mask) + (a2 & mask) + prevP2) & ~mask) != 0 ? 1 : 0;
                int entrig = cntp == 0 && cntm == 34 ? 0 : 1;
                assert (((newR >> (i - 35)) & 0x7FFFFFFFFL)
                        == ((s1.RESULT + (((long) s1.WORK) << 51 >> (51 - 35))) & 0x7FFFFFFFFL));

                partialSum(entrig, cntp, cntm, cntd, (int) oldAcc,
                        oldXs, 0/*
                         * mults[i]
                         */,
                        oldRes, newRes,
                        oldRes1, newRes1,
                        oldRes2, newRes2,
                        oldP1, newP1,
                        oldP2, newP2);
                cntm--;
                oldP1 = newP1;
                oldP2 = newP2;
            }
        }
        if (DEBUG >= 1) {
            System.out.println("product=" + Long.toString(newR, 16) + "(" + (newR & 0x7FFFFFFFFL) + ")"
                    + " p1=" + oldP1 + " p2=" + oldP2);
        }
        if (cntp == 1) {
            assert s1.XS == ((((long) newR) >> 5) & 0x1FFF);
        } else {
            assert ((newR >> cntd) & 0x7FFFFFFFFL) == s1.RESULT;
        }

        return new ProductResult(newR, oldP1, oldP2);
    }

    /**
     * Compute Xs exactly the same as hardware does
     *
     * @param infbit INFBIT 6-bit parameter
     * @param sbit SBIT 5-bit parameter
     * @param adcOut ADC output
     * @return Xs exactly as in hardware
     */
    public static int computeXS(int infbit, int sbit, int adcOut) {
        return computeXs(infbit, sbit, adcOut, false);
    }

    /**
     * Compute xs exactly the same as hardware does
     *
     * @param infbit INFBIT 6-bit parameter
     * @param sbit SBIT 5-bit parameter
     * @param adcOut ADC output
     * @param fixBugP true to fix carry bug in current chip
     * @return xs exactly as in hardware
     */
    public static int computeXs(int infbit, int sbit, int adcOut, boolean fixBugP) {
        if (adcOut < 0 || adcOut >= 4096) {
            throw new IllegalArgumentException();
        }
        if (infbit < 0 || infbit >= 64) {
            throw new IllegalArgumentException();
        }
        if (sbit < 0 || sbit >= 32) {
            throw new IllegalArgumentException();
        }
        return (int) (computePrs(infbit, sbit, adcOut, fixBugP).result >> 5);
    }

    /**
     * Compute partial results prs (scaled temperature) exactly the same as
     * hardware does
     *
     * @param infbit INFBIT 6-bit parameter
     * @param sbit SBIT 5-bit parameter
     * @param adcOut ADC output
     * @param fixBugP true to fix carry bug in current chip
     * @return Prs exactly as in hardware
     */
    public static ProductResult computePrs(int infbit, int sbit, int adcOut, boolean fixBugP) {
        long diff = adcOut - (1535 + (infbit << 3));
        assert diff >= -1535 - 63 * 8 && diff <= 4095 - 1535;
        assert diff >= -2039 && diff <= 2560;
        ProductResult prinf = new ProductResult(diff,
                0,
                fixBugP ? 0 : (adcOut < (infbit << 3) || adcOut >= 1535 + (infbit << 3) ? 1 : 0));
        int scale = sbit + 0x10;
        assert 16 <= scale && scale <= 47;
        long newProdA = diff * scale;
        assert -95833 <= newProdA && newProdA <= 120320;
        long resA = newProdA + 16 + prinf.p2;
        assert -95817 <= resA && resA <= 120337;
        int p1A = fixBugP ? 0 : (((newProdA & 0x7FFFFFFFFL) + 16) & ~0x7FFFFFFFFL) != 0 ? 1 : 0;
        int p2A = fixBugP ? 0 : ((((newProdA + 16) & 0x7FFFFFFFFL) + prinf.p2) & ~0x7FFFFFFFFL) != 0 ? 1 : 0;
        int multA = diff < 0 && (resA & 1) == 0 ? +1 : diff >= 0 && (resA & 1) != 0 ? -1 : 0;
        long oldAccA = newProdA >> 35;
        assert oldAccA == 0 || oldAccA == -1;
        long newAccA = oldAccA + multA * scale;
        ProductResult prs = new ProductResult(resA,
                fixBugP ? 0 : ((((newAccA & 1) + p1A) & ~1) != 0 ? 1 : 0),
                fixBugP ? 0 : (((((newAccA + p1A) & 1) + p2A) & ~1) != 0 ? 1 : 0));
        return prs;
    }

    /**
     * Behavior model of the polynomial evaluator. No check. No bug fix.
     *
     * @param inp input with coefficients and temperature
     * @return result of computation
     */
    public static int compute(PolyState.Inp inp) {
        return compute(inp, false);
    }

    /**
     * Behavior model of the polynomial evaluator. No No bug fix.
     *
     * @param inp input with coefficients and temperature
     * @param check self-check
     * @return result of computation
     *
     */
    public static int compute(PolyState.Inp inp, boolean check) {
        return compute(inp, check, false);
    }

    /**
     * Behavior model of the polynomial evaluator.
     *
     * @param inp input with coefficients and temperature
     * @param check self-check
     * @param fixBugP fix the bug
     * @return result of computation
     *
     */
    public static int compute(PolyState.Inp inp, boolean check, boolean fixBugP) {
        long diff = inp.T - (1535 + (inp.INF << 3));
        assert diff >= -1535 - 63 * 8 && diff <= 4095 - 1535;
        assert diff >= -2039 && diff <= 2560;
        ProductResult prinf = new ProductResult(diff,
                0,
                fixBugP ? 0 : (inp.T < (inp.INF << 3) || inp.T >= 1535 + (inp.INF << 3) ? 1 : 0));
        int scale = inp.SBIT + 0x10;
        assert 16 <= scale && scale <= 47;
        long newProdA = diff * scale;
        assert -95833 <= newProdA && newProdA <= 120320;
        long resA = newProdA + 16 + prinf.p2;
        assert -95817 <= resA && resA <= 120337;
        int p1A = fixBugP ? 0 : (((newProdA & 0x7FFFFFFFFL) + 16) & ~0x7FFFFFFFFL) != 0 ? 1 : 0;
        int p2A = fixBugP ? 0 : ((((newProdA + 16) & 0x7FFFFFFFFL) + prinf.p2) & ~0x7FFFFFFFFL) != 0 ? 1 : 0;
        int multA = diff < 0 && (resA & 1) == 0 ? +1 : diff >= 0 && (resA & 1) != 0 ? -1 : 0;
        long oldAccA = newProdA >> 35;
        assert oldAccA == 0 || oldAccA == -1;
        long newAccA = oldAccA + multA * scale;
        ProductResult prs = new ProductResult(resA,
                fixBugP ? 0 : ((((newAccA & 1) + p1A) & ~1) != 0 ? 1 : 0),
                fixBugP ? 0 : (((((newAccA + p1A) & 1) + p2A) & ~1) != 0 ? 1 : 0));
        int xs = (int) (prs.result >> 5);
        assert xs == computeXs(inp.INF, inp.SBIT, inp.T, fixBugP);
        assert xs >= -0x1000 && xs < 0x1000;
        assert xs >= -2995 && xs <= 3760;
        ProductResult pr2 = new ProductResult(
                inp.K5BIT * xs + (((long) inp.K4BIT - 25) << 9) + prs.p1 + prs.p2,
                fixBugP ? 0 : ((((inp.K5BIT * xs) & 0x7FFFFFFFFL) + (((long) inp.K4BIT) << 9) + prs.p1) & ~0x7FFFFFFFFL) != 0 ? 1 : 0,
                fixBugP ? 0 : (((((inp.K5BIT * xs + (((long) inp.K4BIT) << 9) + prs.p1) & 0x7FFFFFFFFL) + ((-25L << 9) & 0x7FFFFFFFFL) + prs.p2) & ~0x7FFFFFFFFL) != 0 ? 1 : 0));
        assert pr2.result >= -2995 * 15 - (25L << 9) && pr2.result <= 3760 * 15 + ((31 - 25) << 9) /*
                 * + 2
                 */;
        if (xs >= 0) {
            assert pr2.result >= -(25L << 9) && pr2.result <= 3760 * 15 + ((31 - 25) << 9) /*
                     * + 2
                     */;
            assert pr2.result >= -12800 && pr2.result <= 59472;
        }
        if (xs <= 0) {
            assert pr2.result >= (-2995) * 15 - (25L << 9) && pr2.result <= ((31 - 25) << 9) + 2;
            assert pr2.result >= -57725 && pr2.result <= 3074;
        }
        assert pr2.result >= -57725 && pr2.result <= 59472;
        ProductResult pr3 = new ProductResult(
                pr2.result * xs + (((long) inp.K3BIT) << 19) + (1L << 9) + (1L << 22) + pr2.p1 + pr2.p2,
                fixBugP ? 0 : (((((pr2.result * xs) & 0x7FFFFFFFFL) + (((long) inp.K3BIT) << 19) + (1L << 9) + pr2.p1) & ~0x7FFFFFFFFL) != 0 ? 1 : 0),
                fixBugP ? 0 : (((((pr2.result * xs + (((long) inp.K3BIT) << 19) + (1L << 9) + pr2.p1) & 0x7FFFFFFFFL) + ((1L << 22) & 0x7FFFFFFFFL) + pr2.p2) & ~0x7FFFFFFFFL) != 0 ? 1 : 0));
        long res3 = pr3.result >> 10;
        if (xs >= 0) {
            assert pr3.result >= 3760 * (-12800L) + (1L << 9) + (1L << 22) && pr3.result <= 3760 * 59472L + (31L << 19) + (1L << 9) + (1L << 22) + 1/*
                     * 2
                     */;
            assert pr3.result >= -43933184L && pr3.result <= 244062465L;
            assert res3 >= -42904L && res3 <= 238342L;
        }
        if (xs <= 0) {
            assert pr3.result >= (-2995) * 3074 + (1L << 9) + (1L << 22) && pr3.result <= (-2995) * (-57725) + (31L << 19) + (1L << 9) + (1L << 22) + 2;
            assert pr3.result >= -5011814L && pr3.result <= 193334121L;
            assert res3 >= -4895L && res3 <= 188802L;
        }
        assert pr3.result >= -43933184L && pr3.result <= 244062465L;
        assert res3 >= -42904L && res3 <= 238342L;
        ProductResult pr4 = new ProductResult(
                res3 * xs + (((long) inp.K2BIT) << 17) + (1L << 9) + (5L << 19) + pr3.p1 + pr3.p2,
                fixBugP ? 0 : (((((res3 * xs) & 0x7FFFFFFFFL) + (((long) inp.K2BIT) << 17) + (1L << 9) + pr3.p1) & ~0x7FFFFFFFFL) != 0 ? 1 : 0),
                fixBugP ? 0 : (((((res3 * xs + (((long) inp.K2BIT) << 17) + (1L << 9) + pr3.p1) & 0x7FFFFFFFFL) + ((5L << 19) & 0x7FFFFFFFFL) + pr3.p2) & ~0x7FFFFFFFFL) != 0 ? 1 : 0));
        long res4 = pr4.result >> 10;
        if (xs >= 0) {
            assert pr4.result >= 3760 * (-42904L) + (1L << 9) + (5L << 19) && pr4.result <= 3760 * 238342L + (127L << 17) + (1L << 9) + (5L << 19) /*
                     * + 2
                     */;
            assert pr4.result >= -158697088L && pr4.result <= 915434016L;
            assert res4 >= -154978L && res4 <= 893978L;
        }
        if (xs <= 0) {
            assert pr4.result >= (-2995) * 188802L + (1L << 9) + (5L << 19) && pr4.result <= (-2995) * (-4895L) + (127L << 17) + (1L << 9) + (5L << 19) + 2;
            assert pr4.result >= -562840038L && pr4.result <= 33928623L;
            assert res4 >= -549649L && res4 <= 33133L;
        }
        assert pr4.result >= -562840038L && pr4.result <= 915434016L;
        assert res4 >= -549649L && res4 <= 893978L;
        ProductResult pr5 = new ProductResult(
                res4 * xs + (-((long) inp.K1BIT) << 17) - 1 - (195L << 16) + pr4.p1 + pr4.p2,
                fixBugP ? 0 : (((((res4 * xs) & 0x7FFFFFFFFL) + (((-((long) inp.K1BIT) << 17) - 1) & 0x7FFFFFFFFL) + pr4.p1) & ~0x7FFFFFFFFL) != 0 ? 1 : 0),
                fixBugP ? 0 : (((((res4 * xs + (-((long) inp.K1BIT) << 17) - 1 + pr4.p1) & 0x7FFFFFFFFL) + ((-195L << 16) & 0x7FFFFFFFFL) + pr4.p2) & ~0x7FFFFFFFFL) != 0 ? 1 : 0));
        long res5 = pr5.result >> 10;
        if (xs >= 0) {
            assert pr5.result >= 3760 * (-154978L) + (-255L << 17) - 1 + (-195L << 16) && pr5.result <= 3760 * 893978L - 1 + (-195L << 16) /*
                     * + 2
                     */;
            assert pr5.result >= -628920161L && pr5.result <= 3348577759L;
            assert res5 >= -614180L && res5 <= 3270095L;
        }
        if (xs <= 0) {
            assert pr5.result >= (-2995) * 33133L + (-255L << 17) - 1 + (-195L << 16) && pr5.result <= (-2995) * (-549649L) - 1 + (-195L << 16) + 2;
            assert pr5.result >= -145436216L && pr5.result <= 1633419236L;
            assert res5 >= -142028L && res5 <= 1595135L;

        }
        assert pr5.result >= -628920161L && pr5.result <= 3348577759L;
        assert res5 >= -614180L && res5 <= 3270095L;
        ProductResult pr6 = new ProductResult(
                res5 * xs + (1L << 13) + (1032L << 14) + pr5.p1 + pr5.p2,
                fixBugP ? 0 : (((((res5 * xs) & 0x7FFFFFFFFL) + (1L << 13) + pr5.p1) & ~0x7FFFFFFFFL) != 0 ? 1 : 0),
                fixBugP ? 0 : (((((res5 * xs + (1L << 13) + pr5.p1) & 0x7FFFFFFFFL) + (1032L << 14) + pr5.p2) & ~0x7FFFFFFFFL) != 0 ? 1 : 0));
        long res6 = pr6.result >> 14;
        if (xs >= 0) {
            assert pr6.result >= 3760 * (-614180L) + (1L << 13) + (1032L << 14) && pr6.result <= 3760 * 3270095L + (1L << 13) + (1032L << 14) + 2;
            assert pr6.result >= -2292400320L && pr6.result <= 12312473682L;
            assert res6 >= -139918L && res6 <= 751493L;
        }
        if (xs <= 0) {
            assert pr6.result >= (-2995) * 1595135L + (1L << 13) + (1032L << 14) && pr6.result <= (-2995) * (-142028L) + (1L << 13) + (1032L << 14) + 2;
            assert pr6.result >= -4760512845L && pr6.result <= 442290342L;
            assert res6 >= -290559L && res6 <= 26995L;
        }
        assert pr6.result >= -4760512845L && pr6.result <= 12312473682L;
        assert res6 >= -290559L && res6 <= 751493L;
        int resultOut = (res6 < 0 ? 0 : res6 >= (1L << 12) ? 4095 : (int) res6);
        if (check) {
            PolyModel pm = computePolyModel(inp, fixBugP);
            assert pm.prs.result == prs.result;
            assert pm.prs.p1 == prs.p1;
            assert pm.prs.p2 == prs.p2;
            assert pm.resultOut == resultOut;
        }
        return resultOut;
    }

    /**
     * Compute polynomial on all possible temperatures
     *
     * @param inp polynomial coefficients
     * @return
     */
    public static int[] computeAll(PolyState.Inp inp) {
        int[] result = new int[4096];
        for (int adcOut = 0; adcOut < result.length; adcOut++) {
            inp.T = adcOut;
            result[adcOut] = compute(inp);
        }
        return result;
    }

    /**
     * Calculate XS exactly as double
     *
     * @param infbit INFBIT coefficient
     * @param sbit SBIT coefficient
     * @param adcOut temperature
     * @return exact double specification XS
     */
    public static final double calcXSspecification(int infbit, int sbit, int adcOut) {
        return (adcOut - (1535 + 8 * infbit)) * (16 + sbit) / 32.;
    }

    /**
     * Calculate integer approximation of XS exactly May be a little different
     * from hardware
     *
     * @param infbit INFBIT coefficient
     * @param sbit SBIT coefficient
     * @param adcOut temperature
     * @return approximate integer XS
     */
    public static final int calcXS(int infbit, int sbit, int adcOut) {
        return (int) Math.rint((adcOut - (1535 + 8 * infbit)) * (16 + sbit) / 32.);
    }

// Here are polynomial coefficients expressed by the bit encoding
// K0 = 1032
// K1 = -195 -2*K1BIT<8>  in [-705,-195]
// K2 = (20 + K2BIT<7>)/2 in [10,73.5]
// K3 = (8 + K3BIT<5>)/2 in [4,19.5]
// K4 = (K4BIT<5> - 25)/8 in [-3.125,0.75]
// K5 = K5BIT<4>/16 in [0,0.9375]
// SCALE = (SBIT<5> + 16)/32 * 2^{-8} in [0.5,1.46875]
// INF = 1535 + 8*INFBIT<6> in [1535,2039]
    /**
     * For a given XS value polynomial is linear function of its coefficient
     * encodings if we ignore rounding errors. This method computes this linear
     * function t[0] is constant term t[i] is coefficient before K-i-BIT
     *
     * @param t array to return linear function
     * @param xs given XS value
     */
    public static void fillCoefSpecification(Rational[] t, Rational xs) {
        Rational xs1 = xs;
        Rational xs2 = RationalOps.mul(xs1, xs);
        Rational xs3 = RationalOps.mul(xs2, xs);
        Rational xs4 = RationalOps.mul(xs3, xs);
        Rational xs5 = RationalOps.mul(xs4, xs);

        Rational s = Rational.valueOf(1032);
        t[5] = RationalOps.mul(xs5, Rational.valueOf(BigInteger.ONE, -40 - 4));
        t[4] = RationalOps.mul(xs4, Rational.valueOf(BigInteger.ONE, -32 - 3));
        s = RationalOps.fma(Rational.valueOf(BigInteger.valueOf(-25), -32 - 3), xs4, s);
        t[3] = RationalOps.mul(xs3, Rational.valueOf(BigInteger.ONE, -24 - 1));
        s = RationalOps.fma(Rational.valueOf(BigInteger.valueOf(8), -24 - 1), xs3, s);
        t[2] = RationalOps.mul(xs2, Rational.valueOf(BigInteger.ONE, -16 - 1));
        s = RationalOps.fma(Rational.valueOf(BigInteger.valueOf(20), -16 - 1), xs2, s);
        t[1] = RationalOps.mul(xs1, Rational.valueOf(BigInteger.ONE.negate(), -8 + 1));
        s = RationalOps.fma(Rational.valueOf(BigInteger.valueOf(-195), -8), xs1, s);
        t[0] = s;
    }

    /**
     * For a given XS derivative polynomial by XS is linear function of its
     * coefficient encodings if we ignore rounding errors. This method computes
     * this linear function t[0] is constant term t[i] is coefficient before
     * K-i-BIT
     *
     * @param t array to return linear function
     * @param xs given XS value
     */
    public static void fillCoefDerivSpecification(Rational[] t, Rational xs) {
        Rational xs1 = Rational.one();
        Rational xs2 = RationalOps.mul(xs1, xs);
        Rational xs3 = RationalOps.mul(xs2, xs);
        Rational xs4 = RationalOps.mul(xs3, xs);
        Rational xs5 = RationalOps.mul(xs4, xs);
        xs2 = RationalOps.mul(xs2, Rational.valueOf(2));
        xs3 = RationalOps.mul(xs3, Rational.valueOf(3));
        xs4 = RationalOps.mul(xs4, Rational.valueOf(4));
        xs5 = RationalOps.mul(xs5, Rational.valueOf(5));

        Rational s = Rational.zero();
        t[5] = RationalOps.mul(xs5, Rational.valueOf(BigInteger.ONE, -40 - 4));
        t[4] = RationalOps.mul(xs4, Rational.valueOf(BigInteger.ONE, -32 - 3));
        s = RationalOps.fma(Rational.valueOf(BigInteger.valueOf(-25), -32 - 3), xs4, s);
        t[3] = RationalOps.mul(xs3, Rational.valueOf(BigInteger.ONE, -24 - 1));
        s = RationalOps.fma(Rational.valueOf(BigInteger.valueOf(8), -24 - 1), xs3, s);
        t[2] = RationalOps.mul(xs2, Rational.valueOf(BigInteger.ONE, -16 - 1));
        s = RationalOps.fma(Rational.valueOf(BigInteger.valueOf(20), -16 - 1), xs2, s);
        t[1] = RationalOps.mul(xs1, Rational.valueOf(BigInteger.ONE.negate(), -8 + 1));
        s = RationalOps.fma(Rational.valueOf(BigInteger.valueOf(-195), -8), xs1, s);
        t[0] = s;
    }

    /**
     * A double approximation to fillCoeffSpecification
     *
     * @param t array to return linear function
     * @param xs given XS value
     */
    public static void fillCoef(double[] t, double xs) {
        double xs1 = 1 * xs;
        double xs2 = xs * xs;
        double xs3 = xs2 * xs;
        double xs4 = xs3 * xs;
        double xs5 = xs4 * xs;

        t[5] = xs5 * 0x1p-44;
        t[4] = xs4 * 0x1p-35;
        t[3] = xs3 * 0x1p-25;
        t[2] = xs2 * 0x1p-17;
        t[1] = -xs1 * 0x1p-7;

        t[0] = -25 * 0x1p-35 * xs4
                + 0x1p-22 * xs3
                + 5 * 0x1p-15 * xs2
                + (-195 * 0x1p-8 - 0x1p-24 - 0x1p-15) * xs1
                + 1032;
    }

    /**
     * An old double approximation to fillCoeffSpecification
     *
     * @param t array to return linear function
     * @param xs given XS value
     */
    private static void fillCoefOld(double[] t, double xs) {
        double xs1 = 1 * xs;
        double xs2 = xs * xs;
        double xs3 = xs2 * xs;
        double xs4 = xs3 * xs;
        double xs5 = xs4 * xs;

        t[5] = xs5 * 0x1p-44;
        t[4] = xs4 * 0x1p-35;
        t[3] = xs3 * 0x1p-25;
        t[2] = xs2 * 0x1p-17;
        t[1] = -xs1 * 0x1p-7;

        t[0] = -25 * 0x1p-35 * xs4
                + (0x1p-22 + 0x1p-35) * xs3
                + (5 * 0x1p-15 + 0x1p-25) * xs2
                + (-195 * 0x1p-8 - 0x1p-24) * xs1
                + (1032 + 0x1p-1);
    }

    /**
     * Compute specification (without rounding errors) of polynomial evaluator
     *
     * @param inp input coefficients and temperature
     * @return Rational specification value
     */
    public static Rational computeSpecification(PolyState.Inp inp) {
        Rational xs = Rational.valueOf(calcXSspecification(inp.INF, inp.SBIT, inp.T));
        Rational[] t = new Rational[6];
        fillCoefSpecification(t, xs);
        Rational v = t[0];
        v = RationalOps.fma(Rational.valueOf(inp.K1BIT), t[1], v);
        v = RationalOps.fma(Rational.valueOf(inp.K2BIT), t[2], v);
        v = RationalOps.fma(Rational.valueOf(inp.K3BIT), t[3], v);
        v = RationalOps.fma(Rational.valueOf(inp.K4BIT), t[4], v);
        v = RationalOps.fma(Rational.valueOf(inp.K5BIT), t[5], v);
        return RationalOps.min(RationalOps.max(v, Rational.valueOf(0)), Rational.valueOf(4095));
    }

    /**
     * Compute error between specification of polynomial evaluator and actual
     * hardware
     *
     * @param inp input coefficients and temperature
     * @param fixBugP true to fix carry bug in current chip
     * @return Rational error value
     */
    public static Rational computeError(PolyState.Inp inp, boolean fixBugP) {
        int impl = compute(inp, true, fixBugP);
        Rational spec = computeSpecification(inp);
        return RationalOps.sub(Rational.valueOf(impl), spec);
    }

    /**
     * Test poly with some coefficients with self-check
     */
    private static void testPoly() {
        boolean fixBugP = true;
        PolyState.Inp inp = new PolyState.Inp();
        inp.ENwork = 1;
        inp.SBIT = 31;

        inp.T = 0;
        inp.INF = 63;
        inp.K5BIT = 0;
        inp.K4BIT = 31;
        inp.K3BIT = 0;
        inp.K2BIT = 127;
        inp.K1BIT = 255;
        PolyModel.compute(inp, true, fixBugP);

        inp.T = 0;
        inp.INF = 63;
        inp.K5BIT = 15;
        inp.K4BIT = 0;
        inp.K3BIT = 31;
        inp.K2BIT = 0;
        inp.K1BIT = 0;
        PolyModel.compute(inp, true, fixBugP);

        inp.T = 4095;
        inp.INF = 0;
        inp.K5BIT = 15;
        inp.K4BIT = 31;
        inp.K3BIT = 31;
        inp.K2BIT = 127;
        inp.K1BIT = 0;
        PolyModel.compute(inp, true, fixBugP);

        inp.T = 4095;
        inp.INF = 0;
        inp.K5BIT = 0;
        inp.K4BIT = 0;
        inp.K3BIT = 0;
        inp.K2BIT = 0;
        inp.K1BIT = 255;
        PolyModel.compute(inp, true, fixBugP);
    }

    /**
     * Read input parameters from a file specified by the command-line argument
     * and print its evaluation on all temperatures exactly as the hardware.
     *
     * An example of file contents: 1: 31 15 127 63 15 15 7 0 0 0 7
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        testPoly();
        List<List<PolyState.Inp>> inps;
        if (args.length > 0) {
            inps = ParseTestInps.parseTestInps(Paths.get(args[0]));
        } else {
            inps = Arrays.asList(Arrays.asList(PolyState.Inp.genNom()));
        }
        DEBUG = 0;
        for (int chipNo = 0; chipNo < inps.size(); chipNo++) {
            List<PolyState.Inp> inps1 = inps.get(chipNo);
            for (PolyState.Inp inp : inps1) {
                System.out.println((chipNo + 1) + ":" + inp.toNom());
                for (int adcOut = 0; adcOut < 4096; adcOut++) {
                    inp.T = adcOut;
                    int dacInp = compute(inp, true, false);
                    System.out.println("\t" + adcOut + "\t" + dacInp);
                }
            }
        }
    }
}
