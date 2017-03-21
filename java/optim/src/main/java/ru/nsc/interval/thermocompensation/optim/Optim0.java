package ru.nsc.interval.thermocompensation.optim;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularMatrixException;
import org.apache.commons.math3.util.Precision;
import org.gnu.glpk.jna.GLPK;
import ru.nsc.interval.thermocompensation.model.PolyModel;

/**
 *
 */
class Optim0 {

    private static final int k0 = 1;
    private static final int k1bit = 2;
    private static final int k2bit = 3;
    private static final int k3bit = 4;
    private static final int k4bit = 5;
    private static final int k5bit = 6;
    private static final int opt = 7;
    //
    private final double[] matRow = new double[8];
    private final boolean fixK0;
    private final List<Optim.Constraint> constraints = new ArrayList<>();
    private final int numVars;
    private RealMatrix A0;
    private RealVector rhs;

    public Optim0(List<Optim.Constraint> constraints, boolean fixK0) {
        this.constraints.addAll(constraints);
        this.fixK0 = fixK0;
        numVars = opt - (fixK0 ? k1bit : k0) + 1;
    }

    public double[] optim() {
        int sbit = 16;
        int infbit = 32;
//        if (!fixK0) {
//            tryGlpk();
//        }
        fillRows(sbit, infbit);
        double[] freeCoeff = checkSol();
//        if (!fixK0) {
//            for (Optim.Constraint c : constraints) {
//                int xs = PolyModel.computeXS(infbit, sbit, c.adcOut);
//                PolyModel.fillCoef(matRow, xs);
//                double dacInp = 0;
//                for (int i = 1; i <= 5; i++) {
//                    dacInp += matRow[i] * freeCoeff[i];
//                }
//                dacInp += matRow[0] + freeCoeff[0];
//                System.out.println("Constr adcOut=" + c.adcOut + " " + dacInp + (c.upper ? " <= " : " >= ") + c.dacInp);
//            }
//        }
        return freeCoeff;
    }

    private void tryGlpk() {
        int sbit = 16;
        int infbit = 32;
        assert !fixK0;
        final GLPK.glp_prob lp = GLPK.glp_create_prob();
        final GLPK.glp_smcp smcp = new GLPK.glp_smcp();
        final GLPK.glp_iocp iocp = new GLPK.glp_iocp();
        final int[] inds = new int[]{0, k0, k1bit, k2bit, k3bit, k4bit, k5bit, opt};
        GLPK.glp_init_smcp(smcp);
//        smcp.msg_lev = GLPK.GLP_MSG_OFF;
        GLPK.glp_init_iocp(iocp);
//        iocp.msg_lev = GLPK.GLP_MSG_OFF;
        GLPK.glp_set_prob_name(lp, "Polynom");
        GLPK.glp_add_cols(lp, 7);
        GLPK.glp_set_col_name(lp, k0, "k0");
        GLPK.glp_set_col_name(lp, k1bit, "k1bit");
        GLPK.glp_set_col_name(lp, k2bit, "k2bit");
        GLPK.glp_set_col_name(lp, k3bit, "k3bit");
        GLPK.glp_set_col_name(lp, k4bit, "k4bit");
        GLPK.glp_set_col_name(lp, k5bit, "k5bit");
        GLPK.glp_set_col_name(lp, opt, "opt");
        GLPK.glp_set_col_bnds(lp, k0, GLPK.GLP_FR, 0, 0);
        GLPK.glp_set_col_bnds(lp, k1bit, GLPK.GLP_FR, 0, 0);
        GLPK.glp_set_col_bnds(lp, k2bit, GLPK.GLP_FR, 0, 0);
        GLPK.glp_set_col_bnds(lp, k3bit, GLPK.GLP_FR, 0, 0);
        GLPK.glp_set_col_bnds(lp, k4bit, GLPK.GLP_FR, 0, 0);
        GLPK.glp_set_col_bnds(lp, k5bit, GLPK.GLP_FR, 0, 0);
        GLPK.glp_set_col_bnds(lp, opt, GLPK.GLP_FR, 0, 0);
        GLPK.glp_set_obj_dir(lp, GLPK.GLP_MIN);
        GLPK.glp_set_obj_coef(lp, opt, 1);
        GLPK.glp_add_rows(lp, constraints.size());
        for (int rowNum = 0; rowNum < constraints.size(); rowNum++) {
            Optim.Constraint c = constraints.get(rowNum);
            int xs = PolyModel.computeXS(infbit, sbit, c.adcOut);
            PolyModel.fillCoef(matRow, xs);
            for (int i = 5; i >= 1; i--) {
                matRow[i + 1] = c.upper ? matRow[i] : -matRow[i];
            }
            matRow[1] = c.upper ? 1.0 : -1.0;
            matRow[7] = -1.0;
            GLPK.glp_set_row_name(lp, rowNum + 1, "constr" + xs);
            GLPK.glp_set_row_bnds(lp, rowNum + 1, GLPK.GLP_UP, 0, c.upper ? c.dacInp - matRow[0] : matRow[0] - c.dacInp);
            matRow[0] = 0;
            GLPK.glp_set_mat_row(lp, rowNum + 1, 7, inds, matRow);
        }
        int status = GLPK.glp_simplex(lp, smcp);
        if (status != 0) {
            throw new ArithmeticException("Glpk failure " + status);
        }
        status = GLPK.glp_get_status(lp);
        if (status != GLPK.GLP_OPT) {
            throw new ArithmeticException("Glpk status " + status);
        }
        System.out.println("k0=" + GLPK.glp_get_col_prim(lp, k0));
        System.out.println("k1bit=" + GLPK.glp_get_col_prim(lp, k1bit));
        System.out.println("k2bit=" + GLPK.glp_get_col_prim(lp, k2bit));
        System.out.println("k3bit=" + GLPK.glp_get_col_prim(lp, k3bit));
        System.out.println("k4bit=" + GLPK.glp_get_col_prim(lp, k4bit));
        System.out.println("k5bit=" + GLPK.glp_get_col_prim(lp, k5bit));
        System.out.println("opt=" + GLPK.glp_get_col_prim(lp, opt));
        GLPK.glp_delete_prob(lp);
    }

    private void fillRows(int sbit, int infbit) {
        rhs = new ArrayRealVector(constraints.size());
        A0 = new Array2DRowRealMatrix(constraints.size(), numVars);
        for (int rowNum = 0; rowNum < constraints.size(); rowNum++) {
            Optim.Constraint c = constraints.get(rowNum);
            int xs = PolyModel.computeXS(infbit, sbit, c.adcOut);
            PolyModel.fillCoef(matRow, xs);

            if (fixK0) {
                for (int j = 1; j <= 5; j++) {
                    double v = matRow[j];
                    A0.setEntry(rowNum, j - 1, c.upper ? v : -v);
                }
            } else {
                A0.setEntry(rowNum, 0, c.upper ? 1.0 : -1.0);
                for (int j = 1; j <= 5; j++) {
                    double v = matRow[j];
                    A0.setEntry(rowNum, j, c.upper ? v : -v);
                }
            }
//            if (!c.upper) {
//                for (int j = 0; j < numVars; j++) {
//                    A0.setEntry(rowNum, j, -matRow[(fixK0 ? k1bit : k0) + j]);
//                }
//
//            } else {
//                for (int j = 0; j < numVars; j++) {
//                    A0.setEntry(rowNum, j, matRow[(fixK0 ? k1bit : k0) + j]);
//                }
//            }
            A0.setEntry(rowNum, numVars - 1, -1.0); // opt
            rhs.setEntry(rowNum, c.upper ? c.dacInp - matRow[0] : matRow[0] - c.dacInp);
        }
    }

    private double[] checkSol() {
        double[] result = new double[numVars - 1];
        Basis curBasis;
        {
            BitSet basisV = new BitSet();
            basisV.set(0, numVars - 1);
            TreeSet<Integer> basisC = new TreeSet<>();
            basisC.add(rhs.getMinIndex());
            curBasis = new Basis(basisV, basisC);
        }
        Set<Basis> visited = new HashSet<>();
        visited.add(curBasis);

        for (;;) {
            RealMatrix A = new Array2DRowRealMatrix(numVars, numVars);
            RealVector b = new ArrayRealVector(numVars);
            for (int k = 0; k < numVars; k++) {
                int v = curBasis.index[k];
                if (v >= 0) {
                    A.setRowVector(k, A0.getRowVector(v));
                    b.setEntry(k, rhs.getEntry(v));
                } else {
                    A.setEntry(k, -v - 1, 1);
                }
            }
            DecompositionSolver solver = new LUDecomposition(A).getSolver();
            RealMatrix Ainv;
            try {
                Ainv = solver.getInverse();
            } catch (SingularMatrixException e) {
                break;
            }
            RealVector x = solver.solve(b);
            for (int i = 0; i < numVars - 1; i++) {
                result[i] = x.getEntry(i);
//                result[(fixK0 ? k1bit : k0) + i] = x.getEntry(i);
//                System.out.print(" " + result[i]);
            }
//            System.out.println();
//            System.out.println("opt=" + x.getEntry(numVars - 1));
//            System.out.println(Ainv);
            int maxI = -1;
            double maxV = Double.NaN;
            for (int i = 0; i < numVars; i++) {
                double v = Ainv.getEntry(numVars - 1, i);
                if (i < curBasis.Vcard) {
                    v = Math.abs(v);
                }
                if (maxI < 0 || v > maxV) {
                    maxI = i;
                    maxV = v;
                }
            }
//            System.out.println("maxI=" + maxI + " maxV=" + maxV);
            if (maxV < 0) {
//                System.out.println("opt=" + x.getEntry(numVars - 1));
                break;
            }
            RealVector excludeColumn = Ainv.getColumnVector(maxI);
            if (Ainv.getEntry(numVars - 1, maxI) > 0) {
                excludeColumn.mapMultiplyToSelf(-1);
            }
//            System.out.println(excludeColumn);
            RealVector deriv = A0.operate(excludeColumn);
            RealVector A0x = A0.operate(x);
            RealVector defect = new ArrayRealVector(rhs).subtract(A0x);

            int maxUlps = 10;
            // create a list of all the rows that tie for the lowest score in the minimum ratio test
            List<Integer> minRatioPositions = new ArrayList<>();
            double minRatio = Double.MAX_VALUE;
            int nextIndex = curBasis.index[0];
            int nextK = 1;
            while (nextIndex < 0) {
                nextIndex = nextK < curBasis.index.length
                        ? curBasis.index[nextK++]
                        : Integer.MAX_VALUE;
            }
            for (int i = 0; i < rhs.getDimension(); i++) {
                if (i == nextIndex) {
                    nextIndex = nextK < curBasis.index.length
                            ? curBasis.index[nextK++]
                            : Integer.MAX_VALUE;
                    continue;
                }
                final double rhs = defect.getEntry(i);
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
            assert nextIndex == Integer.MAX_VALUE;

            Basis newBasis = null;
            for (Integer row : minRatioPositions) {
                int excludeInd = curBasis.index[maxI];
                Basis basis = new Basis(curBasis, excludeInd, row);
                if (!visited.contains(basis)) {
                    newBasis = basis;
                    break;
                }
            }
            if (newBasis == null) {
                break;
            }
            curBasis = newBasis;
            visited.add(curBasis);
//            System.out.println(curBasis);
        }
        return result;
    }
}
