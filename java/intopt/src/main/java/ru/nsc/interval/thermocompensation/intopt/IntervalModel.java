package ru.nsc.interval.thermocompensation.intopt;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.java.jinterval.expression.Expression;
import net.java.jinterval.interval.set.SetInterval;
import net.java.jinterval.interval.set.SetIntervalContext;
import net.java.jinterval.interval.set.SetIntervalEvaluator;
import net.java.jinterval.rational.Rational;
import ru.nsc.interval.thermocompensation.model.ChipModel;
import ru.nsc.interval.thermocompensation.model.ChipPoints;
import ru.nsc.interval.thermocompensation.model.ChipRefineF0;
import ru.nsc.interval.thermocompensation.model.ParseTestInps.ExtendedInp;
import ru.nsc.interval.thermocompensation.model.PolyState;

/**
 * Интервальная модель микросборки. Содержит модель вычислителя и
 * температурно-частотную модель.
 */
public class IntervalModel {

    /**
     * Модель вычислителя.
     */
    private final IntervalPolyModel polyModel;
    /**
     * Температурно-частотная модель.
     */
    private final ChipModel thermoFreqModel;
    /**
     * Интервальный контекст для промежуточных вычислений.
     */
    public final SetIntervalContext ic;

    private final SetIntervalEvaluator setEv;
    private final SetInterval f0;
    private final int CC;
    private final int CF;
    private final SetInterval scale;
    private final int[] temp;

    /**
     * Построить интервальные модели для набора микросборок из CSV файла.
     * Возвращается отображение, в котором для каждой модели вычислителя помещён
     * список моделей микросборок в порядке возрастания номеров.
     *
     * @param fileName имя CSV файла
     * @param ic интервальный контекст для промежуточных вычислениях в моделях
     * @return отображение из модели вычислителя в масси моделей микросборок
     * @throws IOException
     */
    public static Map<IntervalPolyModel, List<IntervalModel>> readCsvModels(String fileName, SetIntervalContext ic) throws IOException {
        final int CC = 0;
        final int CF = 0;
        ChipPoints[] thermoFreqModels = ChipPoints.readChipPoints(new File(fileName));
        Map<IntervalPolyModel, List<IntervalModel>> result = new LinkedHashMap<>();
        for (IntervalPolyModel ipm : IntervalPolyModel.values()) {
            List<IntervalModel> models = new ArrayList<>();
            for (int chipNo = 0; chipNo < thermoFreqModels.length; chipNo++) {
                ChipPoints tfm = thermoFreqModels[chipNo];
                IntervalModel model = null;
                if (tfm != null) {
                    double f0 = tfm.getF0();
                    model = new IntervalModel(ipm, tfm, f0, CC, CF, ic);
                }
                models.add(model);
            }
            result.put(ipm, models);
        }
        return result;
    }

    /**
     * Построить интервальные модели для набора микросборок из t/f0 файлов.
     * Возвращается отображение, в котором для каждой модели вычислителя помещён
     * список моделей микросборок в порядке возрастания номеров.
     *
     * @param prefix префикс имени t/f0 файлов
     * @param inpsLists наборы, вокруг которых проводились измерения
     * @param taskLists наборы, из которых ьерутся f0, CC, CF для задачи
     * оптимизации
     * @param ic интервальный контекст для промежуточных вычислениях в моделях
     * @return отображение из модели вычислителя в масси моделей микросборок
     * @throws IOException
     */
    public static Map<IntervalPolyModel, List<IntervalModel>> readTF0Models(
            String prefix,
            List<List<ExtendedInp>> inpsLists,
            List<List<ExtendedInp>> taskLists,
            SetIntervalContext ic) throws IOException, ParseException {
        ChipModel[] thermoFreqModels = ChipRefineF0.readChips(prefix, inpsLists);
        Map<IntervalPolyModel, List<IntervalModel>> result = new LinkedHashMap<>();
        for (IntervalPolyModel ipm : IntervalPolyModel.values()) {
            List<IntervalModel> models = new ArrayList<>();
            for (int chipNo = 0; chipNo < thermoFreqModels.length && chipNo < taskLists.size(); chipNo++) {
                ChipModel tfm = thermoFreqModels[chipNo];
                IntervalModel model = null;
                if (tfm != null && !taskLists.get(chipNo).isEmpty()) {
                    ExtendedInp einp = taskLists.get(chipNo).get(0);
                    PolyState.Inp inp = einp.inp;
                    if (!Double.isNaN(einp.f)) {
                        model = new IntervalModel(ipm, tfm, einp.f, inp.CC, inp.CF, ic);
                    }
                }
                models.add(model);
            }
            result.put(ipm, models);
        }
        return result;
    }

    /**
     * Полный брус, содержащий все возможные значения коэффициентов.
     *
     * @return полный брус
     */
    public SetInterval[] getTopBox() {
        return polyModel.getTopBox();
    }

    public IntervalPolyModel getPolyModel() {
        return polyModel;
    }

    public ChipModel getThermoFreqModel() {
        return thermoFreqModel;
    }

    /**
     * Требуемая частота
     *
     * @return требуемая частота
     */
    public double getF0() {
        return f0.doubleInf();
    }

    /**
     * Заданный CC
     *
     * @return заданный CC
     */
    public int getCC() {
        return CC;
    }

    /**
     * Заданный CF
     *
     * @return заданный CF
     */
    public int getCF() {
        return CF;
    }

    /**
     * Convert coefficient vector to Inp
     *
     * @param point coefficient vector
     * @return Inp
     */
    public PolyState.Inp pointAsInp(int[] point) {
        PolyState.Inp intervalInp = PolyState.Inp.genNom();
        intervalInp.INF = point[0];
        intervalInp.SBIT = point[1];
        intervalInp.K1BIT = point[2];
        intervalInp.K2BIT = point[3];
        intervalInp.K3BIT = point[4];
        intervalInp.K4BIT = point[5];
        intervalInp.K5BIT = point[6];
        intervalInp.CC = CC;
        intervalInp.CF = CF;
        return intervalInp;
    }

    /**
     * Инетервальная оценка критерия оптимизации на заданном брусе
     *
     * @param box брус
     * @return интервальная оценка критерия
     */
    public SetInterval eval(SetInterval[] box) {
        return eval(box, getTemps());
    }

    /**
     * Интервальная оценка критерия оптимизации на заданном брусе и на заданном
     * массиве температур.
     *
     * @param box брус
     * @param temps массив температур
     * @return интервальная оценка критерия
     */
    public SetInterval eval(SetInterval[] box, int[] temps) {
        SetInterval[] boxAndTemp = Arrays.copyOf(box, box.length + 1);
        SetInterval maxAbsDiffFreq = ic.numsToInterval(0, 0);
        for (int i = 0; i < temps.length; i++) {
            int adcOut = temps[i];
            boxAndTemp[box.length] = ic.numsToInterval(adcOut, adcOut);
            SetInterval u = setEv.evaluate(boxAndTemp)[0];
            SetInterval fInf = ic.numsToInterval(
                    thermoFreqModel.getLowerModelFfromAdcOut(CC, CF, u.doubleInf(), adcOut),
                    thermoFreqModel.getLowerModelFfromAdcOut(CC, CF, u.doubleSup(), adcOut));
            SetInterval fSup = ic.numsToInterval(
                    thermoFreqModel.getUpperModelFfromAdcOut(CC, CF, u.doubleInf(), adcOut),
                    thermoFreqModel.getUpperModelFfromAdcOut(CC, CF, u.doubleSup(), adcOut));
            SetInterval dfInf = ic.abs(ic.sub(fInf, f0));
            SetInterval dfSup = ic.abs(ic.sub(fSup, f0));
            maxAbsDiffFreq = ic.max(maxAbsDiffFreq, ic.max(dfInf, dfSup));
        }
        return ic.mul(maxAbsDiffFreq, scale);
    }

    /**
     * Интервальная оценка цифроаналового преобразователя на заданном брусе и
     * на заданной температуре.
     *
     * @param box брус
     * @param adcOut значение температуры
     * @return интервальная оценка цифроаналового преобразователя
     */
    public SetInterval evalU(SetInterval[] box, int adcOut) {
        SetInterval[] boxAndTemp = Arrays.copyOf(box, box.length + 1);
        boxAndTemp[box.length] = ic.numsToInterval(adcOut, adcOut);
        SetInterval u = setEv.evaluate(boxAndTemp)[0];
        return u;
    }

    /**
     * Return array of interesting to optimization digital temperatures
     *
     * @return
     */
    public int[] getTemps() {
        return temp.clone();
    }

    /**
     * Вычислить оценку частоты f для всех температур из массива
     *
     * @param inp набор коэффициентов
     * @param temps массив цифровых температур
     * @return массив оценок частоты
     */
    public SetInterval[] evalF(PolyState.Inp inp, int[] temps) {
        Rational[] u = polyModel.evalU(inp, temps);
        SetInterval[] result = new SetInterval[u.length];
        for (int i = 0; i < result.length; i++) {
            int adcOut = temps[i];
            SetInterval ui = ic.numsToInterval(u[i], u[i]);
            double inf = thermoFreqModel.getLowerModelFfromAdcOut(CC, CF, ui.doubleInf(), adcOut);
            double sup = thermoFreqModel.getUpperModelFfromAdcOut(CC, CF, ui.doubleSup(), adcOut);
            result[i] = ic.numsToInterval(inf, sup);
        }
        return result;
    }

    /**
     * Вычислить оценку погрешности частоты f для всех температур из массива
     *
     * @param inp набор коэффициентов
     * @param temps массив цифровых температур
     * @return массив оценок погрешности частоты
     */
    public SetInterval[] evalDF(PolyState.Inp inp, int[] temps) {
        SetInterval[] result = evalF(inp, temps);
        for (int i = 0; i < result.length; i++) {
            result[i] = ic.sub(result[i], f0);
        }
        return result;
    }

    /**
     * Вычислить оценку погрешности частоты f для всех температур из массива
     *
     * @param inp набор коэффициентов
     * @param temps массив цифровых температур
     * @return массив оценок погрешности частоты
     */
    public SetInterval evalMaxAbsDF(PolyState.Inp inp) {
        SetInterval[] df = evalDF(inp, getTemps());
        SetInterval max = ic.numsToInterval(0, 0);
        for (SetInterval v : df) {
            max = ic.max(max, ic.abs(v));
        }
        return max;
    }

    /**
     * Вычислить оценку погрешности частоты f в миллионных для всех температур
     * из массива
     *
     * @param inp набор коэффициентов
     * @param temps массив цифровых температур
     * @return массив оценок погрешности частоты
     */
    public SetInterval[] evalPpm(PolyState.Inp inp, int[] temps) {
        SetInterval[] result = evalDF(inp, temps);
        for (int i = 0; i < result.length; i++) {
            result[i] = ic.mul(result[i], scale);
        }
        return result;
    }

    /**
     * Вычислить худшую оценку погрешности частоты в миллионных
     *
     * @param inp набор коэффициентов
     * @return худшая оценка
     */
    public double evalMaxPpm(PolyState.Inp inp) {
        return evalMaxPpm(inp, getTemps()).doubleMag();
    }

    /**
     * Вычислить худшую оценку погрешности частоты в миллионных
     *
     * @param inp набор коэффициентов
     * @param temps массив цифровых температур
     * @return худшая оценка
     */
    public SetInterval evalMaxPpm(PolyState.Inp inp, int[] temps) {
        SetInterval[] ppm = evalPpm(inp, temps);
        SetInterval max = ic.numsToInterval(0, 0);
        for (SetInterval v : ppm) {
            max = ic.max(max, ic.abs(v));
        }
        return max;
    }

    private IntervalModel(IntervalPolyModel polyModel, ChipModel thermoFreqModel, double f0, int cc, int cf, SetIntervalContext ic) {
        this.polyModel = polyModel;
        this.thermoFreqModel = thermoFreqModel;
        this.f0 = ic.numsToInterval(f0, f0);
        CC = cc;
        CF = cf;
        this.ic = ic;
        Expression objective = polyModel.getObjective();
        setEv = objective != null
                ? SetIntervalEvaluator.create(ic, objective.getCodeList(), objective)
                : null;
        scale = ic.div(ic.numsToInterval(1e6, 1e6), this.f0);
        temp = thermoFreqModel.getAdcOuts();
        Arrays.sort(temp);
//        exploreINF();
    }

    private void exploreINF() {
        if (setEv == null) {
            return;
        }
        SetInterval[] top = getTopBox();
        int k = 0;
        for (int inf = 0; inf < 64; inf++) {
            int t0 = 1535 + 8 * inf;
            while (k < temp.length && temp[k] < t0) {
                k++;
            }
            int[] tmp;
            if (k == temp.length) {
                tmp = new int[]{temp[k - 1]};
            } else if (temp[k] == t0 || k == 0) {
                tmp = new int[]{t0};
            } else {
                tmp = new int[]{temp[k - 1], temp[k]};
            }
            top[0] = ic.numsToInterval(inf, inf);
            SetInterval ppm = eval(top, tmp);
            System.out.println("inf=" + inf + " t0=" + t0 + "[" + tmp[0] + (tmp.length > 1 ? "," + tmp[1] : "") + "] "
                    + print(ppm));
        }
    }

    static String print(SetInterval x) {
        return "[" + x.doubleInf() + "," + x.doubleSup() + "]";
    }

}
