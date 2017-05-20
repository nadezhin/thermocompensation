package ru.nsc.interval.thermocompensation.intopt;

import net.java.jinterval.expression.CodeList;
import net.java.jinterval.expression.Expression;
import net.java.jinterval.interval.set.SetInterval;
import net.java.jinterval.interval.set.SetIntervalOps;
import net.java.jinterval.rational.Rational;
import ru.nsc.interval.thermocompensation.model.PolyModel;
import ru.nsc.interval.thermocompensation.model.PolyState;

/**
 * Интервальная модель вычислителя.
 */
public enum IntervalPolyModel {

    /**
     * Идеализированная модель вычислителя - игнорирует округленияю
     */
    IDEAL {
        @Override
        public Expression getObjective() {
            return FunctionsIdeal.getObjective();
        }

        @Override
        public Rational evalPoint(PolyState.Inp inp) {
            return (Rational) PolyModel.computeSpecification(inp);
        }
    },
    /**
     * Модель вычислителя как он был задуман.
     */
    SPECIFIED {
        @Override
        public Expression getObjective() {
            return FunctionsSpecified.getObjective();
        }

        @Override
        public Rational evalPoint(PolyState.Inp inp) {
            boolean check = false;
            boolean fixBugP = true;
            if (false) {
                // Debug model
                PolyModel polyModel = PolyModel.computePolyModel(inp, fixBugP);
                System.out.println("xd=" + polyModel.prinf.getResult());
                System.out.println("xs=" + (polyModel.prs.getResult() >> 5));
                System.out.println("pr2=" + polyModel.pr4.getResult());
                System.out.println("res3=" + (polyModel.pr3.getResult() >> 10));
                System.out.println("res4=" + (polyModel.pr2.getResult() >> 10));
                System.out.println("res5=" + (polyModel.pr1.getResult() >> 10));
                System.out.println("res6=" + (polyModel.pr0.getResult() >> 14));
                System.out.println("u=" + polyModel.resultOut);
                return Rational.valueOf(polyModel.resultOut);
            } else {
                return Rational.valueOf(PolyModel.compute(inp, check, fixBugP));
            }
        }
    },
    /**
     * Модель вычислителя как он изготовлен.
     */
    MANUFACTURED {
        @Override
        public Expression getObjective() {
            return FunctionsManufactured.getObjective();
        }

        @Override
        public Rational evalPoint(PolyState.Inp inp) {
            boolean check = false;
            boolean fixBugP = false;
            return Rational.valueOf(PolyModel.compute(inp, check, fixBugP));
        }
    };

    /**
     * Выражение описывающее функцию u - полином, обрезанный в интервал
     * [0,4095].
     *
     * @return выражение для функции u
     */
    public abstract Expression getObjective();

    /**
     * Точечное вычисление функции u.
     *
     * @param inp коэффициенты и температура
     * @return функция u
     */
    public abstract Rational evalPoint(PolyState.Inp inp);

    /**
     * Список операций модели.
     *
     * @return список операций модели
     */
    public CodeList getList() {
        return getObjective().getCodeList();
    }

    /**
     * Количество входов модели
     */
    public int getNumInputs() {
        return getList().getNumInps();
    }

    /**
     * Вход модели с заданным номером
     *
     * @param i номер входа
     * @return выражение, соответствующее входу
     */
    public Expression getInp(int i) {
        return getList().getInp(i);
    }

    private static final SetInterval INFBIT = SetIntervalOps.nums2(0, 63);
    private static final SetInterval SBIT = SetIntervalOps.nums2(0, 31);
    private static final SetInterval K1BIT = SetIntervalOps.nums2(1, 255);
    private static final SetInterval K2BIT = SetIntervalOps.nums2(0, 127);
    private static final SetInterval K3BIT = SetIntervalOps.nums2(0, 31);
    private static final SetInterval K4BIT = SetIntervalOps.nums2(0, 31);
    private static final SetInterval K5BIT = SetIntervalOps.nums2(0, 15);

    /**
     * Полный брус, содержащий все возможные значения коэффициентов.
     *
     * @return полный брус
     */
    public SetInterval[] getTopBox() {
        return new SetInterval[]{INFBIT, SBIT, K1BIT, K2BIT, K3BIT, K4BIT, K5BIT};
    }

    /**
     * Вычислить функцию u для всех температур из массива
     *
     * @param inp набор коэффициентов
     * @param temp массив температур
     * @return массив u
     */
    public Rational[] evalU(PolyState.Inp inp, int[] temp) {
        int saveT = inp.T;
        Rational[] result = new Rational[temp.length];
        for (int i = 0; i < result.length; i++) {
            inp.T = temp[i];
            result[i] = evalPoint(inp);
        }
        inp.T = saveT;
        return result;
    }

    public String getAbbrev() {
        return name().substring(0, 5);
    }
}
