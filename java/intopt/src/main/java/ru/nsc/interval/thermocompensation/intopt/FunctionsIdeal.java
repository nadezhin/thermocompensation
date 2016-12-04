package ru.nsc.interval.thermocompensation.intopt;

import net.java.jinterval.expression.CodeList;
import net.java.jinterval.expression.Expression;

public class FunctionsIdeal {

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
    private static final Expression INF = list.lit("1535").add(INFBIT.mul(list.lit("8")));
    private static final Expression SCALE = (SBIT.add(list.lit("16"))).div(list.lit("32"));
    private static final Expression K0 = list.lit("1032");
    private static final Expression K1 = (list.lit("-195")).sub(list.lit("2").mul(K1BIT));
    private static final Expression K2 = (K2BIT.add(list.lit("20"))).div(list.lit("2"));
    private static final Expression K3 = (K3BIT.add(list.lit("8"))).div(list.lit("2"));
    private static final Expression K4 = (K4BIT.sub(list.lit("25"))).div(list.lit("8"));
    private static final Expression K5 = K5BIT.div(list.lit("16"));
    private static final Expression xs = (T.sub(INF)).mul(SCALE);
    private static final Expression x = xs.mul(list.lit("0x1p-8"));
    private static final Expression uArg
            = K0
            .add(K1.mul(x))
            .add(K2.mul(x.sqr()))
            .add(K3.mul(x.pown(3)))
            .add(K4.mul(x.pown(4)))
            .add(K5.mul(x.pown(5)));
    private static final Expression u = list.lit("0").max(list.lit("4095").min(uArg));

    public static final Expression getObjective() {
        return u;
    }

    public static final CodeList getList() {
        return list;
    }
}
