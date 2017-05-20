package ru.nsc.interval.thermocompensation.intopt;

import net.java.jinterval.expression.CodeList;
import net.java.jinterval.expression.Expression;

public class Functions {
    private static final String[] inputs = new String[]{"INFBIT", "SBIT", "K1BIT", "K2BIT", "K3BIT", "K4BIT", "K5BIT", "T"};
    private static final CodeList list = CodeList.create(inputs);
    private static final Expression[] expr;
    private static final Expression objectiveFunction;

    static {
        expr = new Expression[10];
        expr[0] = list.lit("1535").add(list.getInp(0).mul(list.lit("8")));
        expr[1] = (list.getInp(1).add(list.lit("16"))).div(list.lit("32"));
        expr[2] = list.lit("1032");
        expr[3] = (list.lit("-195")).sub(list.lit("2").mul(list.getInp(2)));
        expr[4] = (list.getInp(3).add(list.lit("20"))).div(list.lit("2"));
        expr[5] = (list.getInp(4).add(list.lit("8"))).div(list.lit("2"));
        expr[6] = (list.getInp(5).sub(list.lit("25"))).div(list.lit("8"));
        expr[7] = list.getInp(6).div(list.lit("16"));
        expr[8] = (list.getInp(7).sub(expr[0])).mul(expr[1]).mul(list.lit("2").pown(-8));
        objectiveFunction = expr[2].add(expr[3].mul(expr[8])).add(expr[4].mul(expr[8].sqr())).add(expr[5].mul(expr[8].pown(3)))
                .add(expr[6].mul(expr[8].pown(4))).add(expr[7].mul(expr[8].pown(5)));
    }

    public static final Expression getObjective() {
        return objectiveFunction;
    }

    public static final CodeList getList() {
        return list;
    }
}
