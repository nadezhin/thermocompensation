package ru.nsc.interval.thermocompensation.optim;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import net.java.jinterval.ils.IntervalMatrix;
import net.java.jinterval.ils.IntervalVector;
import net.java.jinterval.interval.set.SetInterval;
import net.java.jinterval.interval.set.SetIntervalContext;
import net.java.jinterval.interval.set.SetIntervalContexts;
import net.java.jinterval.rational.Rational;
import net.java.jinterval.rational.RationalOps;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularMatrixException;
import org.apache.commons.math3.util.Precision;
import ru.nsc.interval.thermocompensation.model.IntModel;
import ru.nsc.interval.thermocompensation.model.PolyModel;

/**
 *
 */
public class OptimMin {

    private static final int k1bit = 0;
    private static final int k2bit = 1;
    private static final int k3bit = 2;
    private static final int k4bit = 3;
    private static final int k5bit = 4;
    private static final int opt = 5;
    private static final int numVars = opt - k1bit + 1;

    private static final double SCALE = 0x1p-44;
    private static final int SCALE_P = -44;

    private static final int[] kl = {1, 0, 0, 0, 0};
    private static final int[] ku = {255, 127, 31, 31, 15};
    //
    public final boolean fixBugP;
    public final int infbit;
    public final int sbit;
    public final int numAdcOuts;
    public final SetIntervalContext ic = SetIntervalContexts.getDefault();
    private final int[] adcOuts;
    private final int[] intModelByAdcOut;
    private final List<IntModel> intModels = new ArrayList<>();
    private final int[] lm;
    private final int[] um;
    private RealMatrix A0;
    private RealVector rhs;
    private IntervalMatrix A0i;
    private IntervalVector rhsi;
    private String[] constrNames;

    private Basis curBasis;
    private BitSet signs;
    private RealMatrix A;
    private RealVector b;
    private RealMatrix Ainv;
    private RealVector x;

    public OptimMin(int[] adcOuts, boolean fixBugP, int infbit, int sbit) {
        this.adcOuts = adcOuts;
        this.fixBugP = fixBugP;
        this.infbit = infbit;
        this.sbit = sbit;
        numAdcOuts = adcOuts.length;
        intModelByAdcOut = new int[numAdcOuts];
        for (int i = 0; i < numAdcOuts; i++) {
            int xs = PolyModel.calcXS(infbit, sbit, adcOuts[i]);
            if (intModels.isEmpty() || intModels.get(intModels.size() - 1).xs != xs) {
                intModelByAdcOut[i] = intModels.size();
                intModels.add(new IntModel(xs, fixBugP));
            } else {
                intModelByAdcOut[i] = intModels.size() - 1;
            }
        }
        lm = new int[intModels.size()];
        um = new int[intModels.size()];
    }

    public double[] optim(int[] l, int[] u) {
        Arrays.fill(lm, 0);
        Arrays.fill(um, 0xFFF);
        for (int i = 0; i < numAdcOuts; i++) {
            int j = intModelByAdcOut[i];
            lm[j] = Math.max(lm[j], l[i]);
            um[j] = Math.min(um[j], u[i]);
        }
        fillRows();
        double[] sol = checkSol();
        return sol;
    }

    private void fillRows() {
        int rowNum = 0;
        for (int i = 0; i < intModels.size(); i++) {
            if (lm[i] > 0) {
                rowNum++;
            }
            if (um[i] < 0xFFF) {
                rowNum++;
            }
        }
        A0 = new Array2DRowRealMatrix(rowNum, numVars);
        rhs = new ArrayRealVector(rowNum);
        A0i = new IntervalMatrix(rowNum, numVars);
        rhsi = new IntervalVector(rowNum);
        constrNames = new String[rowNum];
        rowNum = 0;
        for (int i = 0; i < intModels.size(); i++) {
            IntModel intModel = intModels.get(i);
            intModel.setMLU();

            if (lm[i] > 0) {
                setA0Scaled(rowNum, k1bit, -intModel.k1);
                setA0Scaled(rowNum, k2bit, -intModel.k2);
                setA0Scaled(rowNum, k3bit, -intModel.k3);
                setA0Scaled(rowNum, k4bit, -intModel.k4);
                setA0Scaled(rowNum, k5bit, -intModel.k5);
                setA0(rowNum, opt, -1.0);
                long lbound = (((long) lm[i]) << 14) - (1L << 13);
                setRhsScaled(rowNum, intModel.m - lbound, intModel.l);
                constrNames[rowNum] = "lxs" + intModel.xs;
                rowNum++;
            }
            if (um[i] < 0xFFF) {
                setA0Scaled(rowNum, k1bit, intModel.k1);
                setA0Scaled(rowNum, k2bit, intModel.k2);
                setA0Scaled(rowNum, k3bit, intModel.k3);
                setA0Scaled(rowNum, k4bit, intModel.k4);
                setA0Scaled(rowNum, k5bit, intModel.k5);
                setA0(rowNum, opt, -1.0);
                long ubound = (((long) um[i]) << 14) + (1L << 13) - 1;
                setRhsScaled(rowNum, ubound - intModel.m, -intModel.u);
                constrNames[rowNum] = "hxs" + intModel.xs;
                rowNum++;
            }
        }
        assert rowNum == A0.getRowDimension();
    }

    private void setA0(int i, int j, double v) {
        A0.setEntry(i, j, v);
        A0i.setEntry(i, j, ic.numsToInterval(v, v));
    }

    private void setA0Scaled(int i, int j, long k) {
        A0.setEntry(i, j, k * SCALE);
        Rational v = Rational.valueOf(BigInteger.valueOf(k), SCALE_P);
        A0i.setEntry(i, j, ic.numsToInterval(v, v));
    }

    private void setRhsScaled(int i, long hi, long lo) {
        rhs.setEntry(i, (hi * 0x1p30 + lo) * SCALE);
        Rational v = RationalOps.add(
                Rational.valueOf(BigInteger.valueOf(hi), SCALE_P + 30),
                Rational.valueOf(BigInteger.valueOf(lo), SCALE_P));
        rhsi.setEntry(i, ic.numsToInterval(v, v));
    }

    private String varName(Basis basis, BitSet signs, int i) {
        int v = basis.index[i];
        if (v >= 0) {
            return constrNames[v];
        }
        v = -v - 1;
        return "k" + (v + 1) + (signs.get(i) ? "l" : "u");
    }

    private void initBasis() {
        BitSet basisV = new BitSet();
        basisV.set(0, numVars - 1);
        TreeSet<Integer> basisC = new TreeSet<>();
        basisC.add(rhs.getMinIndex());
        setBasis(new Basis(basisV, basisC));
    }

    private void setBasis(Basis basis) throws SingularMatrixException {
        RealMatrix A_ = new Array2DRowRealMatrix(numVars, numVars);
        RealVector b_ = new ArrayRealVector(numVars);
        for (int k = 0; k < numVars; k++) {
            int v = basis.index[k];
            if (v >= 0) {
                A_.setRowVector(k, A0.getRowVector(v));
                b_.setEntry(k, rhs.getEntry(v));
            } else {
                A_.setEntry(k, -v - 1, 1);
            }
        }
        DecompositionSolver solver = new LUDecomposition(A_).getSolver();
        RealMatrix Ainv_ = solver.getInverse();
        BitSet signs_ = new BitSet();
        for (int i = 0; i < basis.Vcard; i++) {
            int v = -basis.index[i] - 1;
            if (Ainv_.getEntry(numVars - 1, i) >= 0) {
                signs_.set(i);
                for (int j = 0; j < numVars; j++) {
                    if (j != v) {
                        assert A_.getEntry(i, j) == 0;
                    } else {
                        assert A_.getEntry(i, v) == 1;
                        A_.setEntry(i, v, -1);
                    }
                }
                b_.setEntry(i, kl[v]);
            } else {
                b_.setEntry(i, ku[v]);
            }
        }
        solver = new LUDecomposition(A_).getSolver();
        Ainv_ = solver.getInverse();
        RealVector x_ = Ainv_.operate(b_);

        // Commit if no exceptions
        curBasis = basis;
        A = A_;
        b = b_;
        Ainv = Ainv_;
        signs = signs_;
        x = x_;
    }

    String basisRep() {
        String s = "BASIS";
        for (int i = 0; i < numVars; i++) {
            s += " " + varName(curBasis, signs, i);
        }
        return s;
    }

    void printBasis() {
        System.out.println(basisRep());
        for (int v = 0; v < numVars; v++) {
            System.out.print(varName(curBasis, signs, v) + " = " + b.getEntry(v));
            for (int i = 0; i < numVars; i++) {
                System.out.print(" " + -A.getEntry(v, i)
                        + "*" + (i == numVars - 1 ? "opt" : "b" + (i + 1)));
            }
            System.out.println();
        }
        System.out.println();
        for (int i = 0; i < numVars; i++) {
            System.out.print((i == numVars - 1 ? "opt" : "b" + (i + 1)) + " = " + x.getEntry(i));
            for (int j = 0; j < numVars; j++) {
                System.out.print(" " + -Ainv.getEntry(i, j) + "*" + varName(curBasis, signs, j));
            }
            System.out.println();
        }
        System.out.println();
    }

    private double pointBound() {
        return x.getEntry(numVars - 1);
    }

    private static String p(SetInterval x) {
        return "[" + x.doubleInf() + "," + x.doubleSup() + "]";
    }

    private SetInterval intervalBound() {
        SetInterval[] p = new SetInterval[numVars];
        SetInterval[] sums = new SetInterval[numVars];
        SetInterval sumr = ic.numsToInterval(0, 0);
        Arrays.fill(sums, ic.numsToInterval(0, 0));
        for (int i = 0; i < numVars; i++) {
            double pi = Math.max(-Ainv.getEntry(numVars - 1, i), 0);
//            System.out.println("p" + i + " = " + pi);
            p[i] = ic.numsToInterval(pi, pi);
            int v = curBasis.index[i];
            if (v >= 0) {
                for (int j = 0; j < numVars; j++) {
                    sums[j] = ic.add(sums[j], ic.mul(p[i], A0i.getEntry(v, j)));
                }
                sumr = ic.add(sumr, ic.mul(p[i], rhsi.getEntry(v)));
            } else {
                v = -v - 1;
                if (signs.get(i)) {
                    sums[v] = ic.sub(sums[v], p[i]);
                    sumr = ic.sub(sumr, ic.mul(p[i], ic.numsToInterval(kl[v], kl[v])));
                } else {
                    sums[v] = ic.add(sums[v], p[i]);
                    sumr = ic.add(sumr, ic.mul(p[i], ic.numsToInterval(ku[v], ku[v])));
                }
            }
        }
        SetInterval optBound = ic.numsToInterval(0, 0);
        for (int v = 0; v < numVars - 1; v++) {
            optBound = ic.add(optBound, ic.mul(sums[v], ic.numsToInterval(kl[v], ku[v])));
        }
        optBound = ic.sub(sumr, optBound);
        optBound = ic.div(optBound, sums[opt]);
//        for (int i = 0; i < numVars; i++) {
//            System.out.print(" " + p(sums[i]));
//        }
//        System.out.println();
//        System.out.println("rhs " + p(sumr));
        return optBound;
    }

    private double[] checkSol() {
        double[] result = new double[numVars];
        initBasis();
        Set<Basis> visited = new HashSet<>();
        visited.add(curBasis);

        for (;;) {
            for (int i = 0; i < numVars; i++) {
                result[i] = x.getEntry(i);
            }
//            printBasis();
            SetInterval intBound = intervalBound();
//            System.out.println(basisRep() + " >= " + pointBound() + " " + p(intBound));
            if (intBound.doubleInf() > 0) {
                return null;
            }
            double[] c = new double[numVars];
//            double est = 0.0;
            for (int i = 0; i < numVars; i++) {
                c[i] = -Ainv.getEntry(numVars - 1, i);
//                System.out.println("c[" + i + "]=" + c[i]);
//                assert c[i] >= 0;
//                est += c[i] * b.getEntry(i);
            }
//            System.out.println("opt>=" + (-est));

            int nextIndex = curBasis.index[0];
            int nextK = 1;
            while (nextIndex < -numVars) {
                nextIndex = nextK < curBasis.index.length
                        ? curBasis.index[nextK++]
                        : Integer.MAX_VALUE;
            }
            int minI = Integer.MIN_VALUE;
            double minV = Double.POSITIVE_INFINITY;
            boolean signI = false;
            for (int i = numVars - 2; i >= 0; i--) {
                int v = -i - 1;
                if (v == nextIndex) {
                    nextIndex = nextK < curBasis.index.length
                            ? curBasis.index[nextK++]
                            : Integer.MAX_VALUE;
                    continue;
                }
                double bv = x.getEntry(i);
                double dl = bv - kl[i];
                double du = ku[i] - bv;
//                System.out.println("b" + (i+1) + "l " + dl + " = " + bv + " - " + kl[i]);
//                System.out.println("b" + (i+1) + "u " + du + " = " + ku[i] + " - " + bv);

                if (dl < minV || du < minV) {
                    minI = -i - 1;
                    minV = Math.min(dl, du);
                    signI = dl < du;
                }
            }

            RealVector A0x = A0.operate(x);
            RealVector defect = new ArrayRealVector(rhs).subtract(A0x);
            for (int i = 0; i < constrNames.length; i++) {
                if (i == nextIndex) {
                    nextIndex = nextK < curBasis.index.length
                            ? curBasis.index[nextK++]
                            : Integer.MAX_VALUE;
                    continue;
                }
                double dv = rhs.getEntry(i) - A0x.getEntry(i);
//                System.out.println(constrNames[i] + " "
//                        + dv + " = " + rhs.getEntry(i) + " - " + A0x.getEntry(i));
                if (dv < minV) {
                    minI = i;
                    minV = dv;
                }
            }
            assert nextIndex == Integer.MAX_VALUE;

//            System.out.println("minI=" + minI + " minV=" + minV);
            if (minV > 0) {
                break;
            }

            RealVector direction;
            if (minI >= 0) {
                direction = A0.getRowVector(minI);
            } else {
                int v = -minI - 1;
                direction = new ArrayRealVector(numVars);
                direction.setEntry(v, signI ? -1 : 1);
            }

            RealMatrix Ainvt = Ainv.transpose();
            RealVector deriv = Ainvt.operate(direction);

//            for (int i = 0; i < numVars; i++) {
//                System.out.println("deriv[" + i + "]=" + deriv.getEntry(i));
//            }
            int maxUlps = 10;
            // create a list of all the rows that tie for the lowest score in the minimum ratio test
            List<Integer> minRatioPositions = new ArrayList<Integer>();
            double minRatio = Double.MAX_VALUE;
            for (int i = 0; i < numVars; i++) {
//                if (curBasis.basisC.contains(i)) {
//                    continue;
//                }
                final double rhs = c[i];
                final double entry = deriv.getEntry(i);

                if (Precision.compareTo(entry, 0d, maxUlps) > 0) {
                    final double ratio = rhs / entry;
                    // check if the entry is strictly equal to the current min ratio
                    // do not use a ulp/epsilon check
                    final int cmp = Double.compare(ratio, minRatio);
                    if (cmp == 0) {
                        minRatioPositions.add(i);
                    } else if (cmp < 0) {
                        minRatio = ratio;
                        minRatioPositions = new ArrayList<Integer>();
                        minRatioPositions.add(i);
                    }
                }
            }

            Basis newBasis = null;
            for (Integer row : minRatioPositions) {
                int excludeInd = minI;//basisIndex[maxI];
                Basis basis = new Basis(curBasis, curBasis.index[row], excludeInd);
//                Basis basis = new Basis(curBasis, excludeInd, row);
                if (!visited.contains(basis)) {
                    newBasis = basis;
                    break;
                }
            }
            if (newBasis == null) {
                break;
            }
            try {
                setBasis(newBasis);
            } catch (SingularMatrixException e) {
                break;
            }
            visited.add(curBasis);
//            System.out.println(curBasis);
        }
        return result;
    }
}
