package ru.nsc.interval.thermocompensation.intopt;

import net.java.jinterval.expression.CodeList;
import net.java.jinterval.expression.Expression;

/**
 *
 */
public class FunctionsSpecified {

    private static final String[] inputs = new String[]{"INFBIT", "SBIT", "K1BIT", "K2BIT", "K3BIT", "K4BIT", "K5BIT", "T"};
    private static final CodeList list = CodeList.create(inputs);

    private static final Expression INFBIT = list.getInp(0);
    private static final Expression SBIT = list.getInp(1);
    private static final Expression K1BIT = list.getInp(2);
    private static final Expression K2BIT = list.getInp(3);
    private static final Expression K3BIT = list.getInp(4);
    private static final Expression K4BIT = list.getInp(5);
    private static final Expression K5BIT = list.getInp(6);
    private static final Expression T = list.getInp(7);

    private static final Expression HALF = l("0.5");
    private static final Expression INF = l("1535").add(INFBIT.mul(l("8")));
    private static final Expression SCALE = SBIT.add(l("16"));
    private static final Expression K0 = l("1032");
    private static final Expression K1 = K1BIT.mul(l("-2")).sub(l("195")).mul(l("0x1p6"));
    private static final Expression K2 = K2BIT.add(l("20")).mul(l("0x1p7"));
    private static final Expression K3 = K3BIT.add(l("8")).mul(l("0x1p9"));
    private static final Expression K4 = K4BIT.sub(l("25")).mul(l("0x1p9"));
    private static final Expression K5 = K5BIT;

    static final Expression xd = T.sub(INF);
    static final Expression xs = round(xd.mul(SCALE), 5);
    static final Expression pr2 = K4.add(K5.mul(xs));
    static final Expression res3 = K3.add(round(pr2.mul(xs), 10));
    static final Expression res4 = K2.add(round(res3.mul(xs), 10));
    static final Expression res5 = K1.add(round(res4.mul(xs), 10));
    static final Expression res6 = K0.add(round(res5.mul(xs), 14));
    static final Expression u = l("0").max(l("4095").min(res6));

    /**
     * Return constant Expression
     *
     * @param s string literal
     * @return constant Expression
     */
    private static Expression l(String s) {
        return list.lit(s);
    }

    /**
     * Round fixed-point expression x assuming that binary point is at position
     * dotPos
     *
     * @param x amount to round
     * @param dotPos binary point
     * @return rounded expression
     */
    private static Expression round(Expression x, int dotPos) {
        String scale = "0x1p" + (-dotPos);
        return (dotPos != 0 ? x.mul(l(scale)) : x).add(HALF).floor();
    }

    /**
     * Round fixed-point expression x assuming that binary point is at position
     * dotPos
     *
     * @param x amount to round
     * @param dotPos binary point
     * @return rounded expression
     */
    private static Expression roundBuggy(Expression x, int dotPos) {
        String scale = "0x1p" + (-dotPos);
        return x.sub(l("1")).mul(l(scale)).floor();
    }

    public static final Expression getObjective() {
        return u;
    }

    public static final CodeList getList() {
        return list;
    }
}
