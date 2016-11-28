package ru.nsc.interval.thermocompensation.spline;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.QRDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

/**
 *
 */
public class Polys {

    public static double[] approxPoly(double arg0, double[] args, double[] vals, int N) {
        assert args.length == vals.length;
        if (args.length <= N) {
            throw new IllegalArgumentException();
        }
        RealMatrix A = new Array2DRowRealMatrix(args.length, N + 1);
        RealVector b = new ArrayRealVector(args.length);
        for (int j = 0; j < args.length; j++) {
            double x = args[j] - arg0;
            double xk = 1;
            for (int k = 0; k <= N; k++) {
                A.setEntry(j, k, xk);
                xk *= x;
            }
            b.setEntry(j, vals[j]);
        }
        DecompositionSolver solver;
        if (args.length == N + 1) {
            solver = new LUDecomposition(A).getSolver();
        } else {
            solver = new QRDecomposition(A).getSolver();
        }
        RealVector x = solver.solve(b);
        assert x.getDimension() == N + 1;
        double[] result = new double[N + 1];
        for (int k = 0; k <= N; k++) {
            result[k] = x.getEntry(k);
        }
        return result;
    }

    public static double calcPoly(double arg0, double[] coeff, double arg) {
        double p = 0;
        double x = arg - arg0;
        for (int i = coeff.length - 1; i >= 0; i--) {
            p = p * x + coeff[i];
        }
        return p;
    }

    public static double calcDeriv(double arg0, double[] coeff, double arg) {
        double p = 0;
        double x = arg - arg0;
        for (int i = coeff.length - 1; i >= 1; i--) {
            p = p * x + i * coeff[i];
        }
        return p;
    }
}
