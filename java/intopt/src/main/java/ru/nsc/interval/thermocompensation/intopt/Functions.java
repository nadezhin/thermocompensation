package ru.nsc.interval.thermocompensation.intopt;

import net.java.jinterval.expression.CodeList;
import net.java.jinterval.expression.Expression;

public class Functions {

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
    private static final Expression xd = T.sub(list.lit("1535").add(list.lit("8").mul(INFBIT)));
    private static final Expression xs = (((xd.mul(list.lit("16").add(SBIT))).div(list.lit("32"))).add(list.lit("0.5"))).floor();
    private static final Expression pr2 = ((K4BIT.sub(list.lit("25"))).mul(list.lit("512"))).add(K5BIT.mul(xs));
    private static final Expression res3 = ((K3BIT.mul(list.lit("512"))).add(list.lit("4096")))
            .add(((pr2.mul(xs).div(list.lit("1024"))).add(list.lit("0.5"))).floor());
    private static final Expression res4 = ((K2BIT.mul(list.lit("128"))).add(list.lit("2560")))
            .add(((res3.mul(xs).div(list.lit("1024"))).add(list.lit("0.5"))).floor());
    private static final Expression res5 = ((K1BIT.neg().mul(list.lit("128"))).sub(list.lit("12480")))
            .add((((res4.mul(xs).sub(list.lit("1"))).div(list.lit("1024"))).add(list.lit("0.5"))).floor());
    private static final Expression res6 = (list.lit("1032").add((res5.mul(xs).div(list.lit("16384")))).add(list.lit("0.5")));
    private static final Expression u = list.lit("0").max(list.lit("4095").min(res6));

    public static final Expression getObjective() {
        return u;
    }

    public static final CodeList getList() {
        return list;
    }
}
