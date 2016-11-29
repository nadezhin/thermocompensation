package ru.nsc.interval.thermocompensation.show;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import ru.nsc.interval.thermocompensation.model.AdcRange;
import ru.nsc.interval.thermocompensation.model.ChipAnalyze;
import ru.nsc.interval.thermocompensation.model.ChipModel;
import ru.nsc.interval.thermocompensation.model.ChipT;
import ru.nsc.interval.thermocompensation.model.PolyModel;
import ru.nsc.interval.thermocompensation.parse.CapSettings;
import ru.nsc.interval.thermocompensation.parse.Parse;
import ru.nsc.interval.thermocompensation.model.PolyState;
import ru.nsc.interval.thermocompensation.spline.Polys;

/**
 *
 */
public class ChipShow {

    private static final boolean IS_PDF = true;
    private static final String DRIVER = IS_PDF ? "pdf" : "postscript";
    private static final String EXTENSION = IS_PDF ? ".pdf" : ".ps";
    String prefix;
    File gnuplotFile;
    PrintWriter gnuplot;
    boolean found = false;
    File tempDir;
    boolean deleteOnExit;

    public ChipShow(String name) throws IOException {
        this(name, new File("."), false);
    }

    public ChipShow(String name, File tempDir, boolean deleteOnExit) throws IOException {
        tempDir = tempDir.getAbsoluteFile();
        this.prefix = name;
        this.deleteOnExit = deleteOnExit;
        gnuplotFile = new File(tempDir, name + ".gnuplot");
        if (deleteOnExit) {
            gnuplotFile.deleteOnExit();
        }
        gnuplot = new PrintWriter(gnuplotFile);
        gnuplot.println("set terminal " + DRIVER + " font \"Helvetica,4\"");
        gnuplot.println("set grid");
        this.tempDir = tempDir;
    }

    public void startPdf(String pdfFileName, String title, String xLabel, String yLabel, String yFormat) {
        gnuplot.println("set output \"" + pdfFileName + EXTENSION + "\"");
        gnuplot.println("set title \"" + title + "\"");
        gnuplot.println("set xlabel \"" + xLabel + "\"");
        gnuplot.println("set ylabel \"" + yLabel + "\"");
        gnuplot.println("set format y \"" + yFormat + "\"");
        gnuplot.print("plot");
        found = false;
    }

    private void withLines(String fileName, int colX, int colY, String title) {
        if (found) {
            gnuplot.print(",");
        }
        found = true;
        gnuplot.println(" \\");
        gnuplot.print("  '" + fileName + "' using " + colX + ":" + colY + " title '" + title + "' with lines");
    }

    private void withLinesPoints(String fileName, int colX, int colY, String title) {
        if (found) {
            gnuplot.print(",");
        }
        found = true;
        gnuplot.println(" \\");
        gnuplot.print("  '" + fileName + "' using " + colX + ":" + colY + " title '" + title + "' with linespoints");
    }

    public void withLines(double[] x, double[] y, String title) throws IOException {
        if (x.length != y.length) {
            throw new IllegalArgumentException();
        }
        if (x.length == 0) {
            return;
        }
        File csv = File.createTempFile(prefix, ".csv", tempDir);
        if (deleteOnExit) {
            csv.deleteOnExit();
        }
        try (PrintWriter out = new PrintWriter(csv)) {
            out.println("# x y " + title);
            for (int i = 0; i < x.length; i++) {
                out.println(x[i] + " " + y[i]);
            }
        }
        String csvPath = csv.getPath().replace('\\', '/');
        withLines(csvPath, 1, 2, title);
    }

    public void withLinesPoints(double[] x, double[] y, String title) throws IOException {
        if (x.length != y.length) {
            throw new IllegalArgumentException();
        }
        if (x.length == 0) {
            return;
        }
        File csv = File.createTempFile(prefix, ".csv", tempDir);
        if (deleteOnExit) {
            csv.deleteOnExit();
        }
        try (PrintWriter out = new PrintWriter(csv)) {
            out.println("# x y " + title);
            for (int i = 0; i < x.length; i++) {
                out.println(x[i] + " " + y[i]);
            }
        }
        String csvPath = csv.getPath().replace('\\', '/');
        withLinesPoints(csvPath, 1, 2, title);
    }

    public void closePdf() {
        gnuplot.println();
        gnuplot.println();
    }

    public void close() {
        gnuplot.close();
    }

    public void closeAndRunGnuplot() throws IOException, InterruptedException {
        close();
        ProcessBuilder pb = new ProcessBuilder().
                directory(tempDir).
                command("gnuplot", gnuplotFile.getAbsolutePath()).
                redirectErrorStream(true).
                redirectOutput(ProcessBuilder.Redirect.INHERIT);
        try {
            Process process = pb.start();
            int status = process.waitFor();
            System.out.println("status = " + status);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void showUncompFtemp(ChipT chip, CapSettings cs, int polyN) throws IOException {
        showUncompF(chip, cs, true, polyN);
    }

    public void showUncompFadc(ChipT chip, CapSettings cs, int polyN) throws IOException {
        showUncompF(chip, cs, false, polyN);
    }

    private void showUncompF(ChipT chip, CapSettings cs, boolean byTemp, int polyN) throws IOException {
        double x0 = byTemp ? +25 : 2048;
        double[] poly;
        if (polyN >= 0) {
            List<Parse.MeasF> list = chip.selectF(cs);
            double[] x = new double[list.size()];
            double[] y = new double[list.size()];
            for (int i = 0; i < list.size(); i++) {
                Parse.MeasF mf = list.get(i);
                Date t = mf.getDate();
                x[i] = byTemp ? chip.getTemp(t) : chip.getAdcOut(t);
                y[i] = mf.f;
            }
            poly = Polys.approxPoly(x0, x, y, polyN);
        } else {
            poly = new double[0];
        }
        showUncompF(chip, cs, byTemp, x0, poly);
    }

    public void showUncompF(ChipT chip, CapSettings cs, boolean byTemp, double x0, double[] poly) throws IOException {
        List<Parse.MeasF> list = chip.selectF(cs);
        double[] x = new double[list.size()];
        double[] y = new double[list.size()];
        for (int i = 0; i < list.size(); i++) {
            Parse.MeasF mf = list.get(i);
            Date t = mf.getDate();
            x[i] = byTemp ? chip.getTemp(t) : chip.getAdcOut(t);
            y[i] = mf.f - Polys.calcPoly(x0, poly, x[i]);
        }
        String title = "Некомпенсированная частота " + chip.runDate + " " + cs;
        if (cs.equals(chip.cs0)) {
            withLines(x, y, title);
        } else {
            withLinesPoints(x, y, title);
        }
    }

    public void showInpAdc(ChipT chipt, PolyState.Inp inp, String title) throws IOException {
        showInp(chipt, inp, false, false, null, title);
    }

    public void showInpPpmAdc(ChipT chipt, PolyState.Inp inp, double targetF, String title) throws IOException {
        showInp(chipt, inp, false, false, targetF, title);
    }

    public void showInpTemp(ChipT chipt, PolyState.Inp inp, String title) throws IOException {
        showInp(chipt, inp, false, true, null, title);
    }

    public void showInpPpmTemp(ChipT chipt, PolyState.Inp inp, double targetF, String title) throws IOException {
        showInp(chipt, inp, false, true, targetF, title);
    }

    public void showCheckInp(ChipT compChip, PolyState.Inp inp, double freq, boolean asPpm) throws IOException {
        List<Parse.InpF> list = compChip.selectByInp(inp, false);
        double ppm = ChipAnalyze.getPpm(list, freq);
        String title = "Компенсированная " + inp.toLongNom() + " f=" + (int) freq + " +-" + Math.ceil(ppm * 100) / 100 + "ppm";
        showInp(compChip, inp, false, true, asPpm ? freq : null, title);
    }

    private void showInp(ChipT chipt, PolyState.Inp inp, boolean disturb, boolean byTemp, Double targetF, String title) throws IOException {
        List<Parse.InpF> selInpF = chipt.selectByInp(inp, disturb);
        if (selInpF.isEmpty()) {
            return;
        }
        int numTemps = selInpF.size();
        double[] x = new double[numTemps];
        double[] y = new double[numTemps];
        for (int k = 0; k < numTemps; k++) {
            Parse.InpF mf = selInpF.get(k);
            Date time = mf.getDate();
            x[k] = byTemp ? chipt.getTemp(time) : chipt.getAdcOut(time);
            double freq = mf.f;
            y[k] = targetF != null ? (freq / targetF - 1) * 1e6 : freq;
        }
        withLines(x, y, title + " " + chipt.runDate);
    }

    public void showModelInpIntervalAdc(String modelName, ChipModel chipModel, PolyState.Inp inp, AdcRange adcRange) throws IOException {
        showModelInpInterval(modelName, chipModel, inp, null, adcRange.minAdcOut, adcRange.maxAdcOut, null);
    }

    public void showModelInpMidAdc(String modelName, ChipModel chipModel, PolyState.Inp inp, AdcRange adcRange) throws IOException {
        showModelInpMid(modelName, chipModel, inp, null, adcRange.minAdcOut, adcRange.maxAdcOut, null);
    }

    public void showModelInpIntervalPpmAdc(String modelName, ChipModel chipModel, PolyState.Inp inp, int minAdcOut, int maxAdcOut, double targetF) throws IOException {
        showModelInpInterval(modelName, chipModel, inp, null, minAdcOut, maxAdcOut, targetF);
    }

    public void showModelInpIntervalTemp(String modelName, ChipModel chipModel, PolyState.Inp inp, ChipT chipTemp, int minTemp, int maxTemp) throws IOException {
        if (chipTemp == null) {
            throw new NullPointerException();
        }
        showModelInpInterval(modelName, chipModel, inp, chipTemp, minTemp, maxTemp, null);
    }

    public void showModelInpIntervalPpmTemp(String modelName, ChipModel chipModel, PolyState.Inp inp, ChipT chipTemp, int minTemp, int maxTemp, double targetF) throws IOException {
        if (chipTemp == null) {
            throw new NullPointerException();
        }
        showModelInpInterval(modelName, chipModel, inp, chipTemp, minTemp, maxTemp, targetF);
    }

    public void showModelInpInterval(String modelName, ChipModel chipModel, PolyState.Inp inp, ChipT chipTemp, int minT, int maxT, Double targetF) throws IOException {
        int numPoints = maxT - minT + 1;
        double[] x = new double[numPoints];
        double[] lowerY = new double[numPoints];
        double[] upperY = new double[numPoints];
        for (int t = minT; t <= maxT; t++) {
            int adcOut = chipTemp != null ? (int) Math.rint(chipTemp.getAdcOutByPoly2(t)) : t;
            adcOut = Math.min(Math.max(adcOut, 0), 4095);
//            int adcOut = chipTemp != null ? (int) Math.rint(chipTemp.getAdcOutNewByCmdTemp(t)) : t;
            inp.T = adcOut;
            int dacInp = PolyModel.compute(inp);
            int k = t - minT;
            x[k] = chipTemp != null ? t : adcOut;
            double lF = chipModel.getLowerModelFfromAdcOut(inp.CC, inp.CF, dacInp, adcOut);
            double uF = chipModel.getUpperModelFfromAdcOut(inp.CC, inp.CF, dacInp, adcOut);
            lowerY[k] = targetF != null ? (lF / targetF - 1) * 1e6 : lF;
            upperY[k] = targetF != null ? (uF / targetF - 1) * 1e6 : uF;
        }
        withLines(x, lowerY, modelName + " снизу");
        withLines(x, upperY, modelName + " сверху");
    }

    public void showModelInpMid(String modelName, ChipModel chipModel, PolyState.Inp inp, ChipT chipTemp, int minT, int maxT, Double targetF) throws IOException {
        int numPoints = maxT - minT + 1;
        double[] x = new double[numPoints];
        double[] midY = new double[numPoints];
        for (int t = minT; t <= maxT; t++) {
            int adcOut = chipTemp != null ? (int) Math.rint(chipTemp.getAdcOutByCmdTemp(t)) : t;
            inp.T = adcOut;
            int dacInp = PolyModel.compute(inp);
            int k = t - minT;
            x[k] = chipTemp != null ? t : adcOut;
            double lF = chipModel.getLowerModelFfromAdcOut(inp.CC, inp.CF, dacInp, adcOut);
            double uF = chipModel.getUpperModelFfromAdcOut(inp.CC, inp.CF, dacInp, adcOut);
            double lowerY = targetF != null ? (lF / targetF - 1) * 1e6 : lF;
            double upperY = targetF != null ? (uF / targetF - 1) * 1e6 : uF;
            midY[k] = 0.5 * (lowerY + upperY);
        }
        withLines(x, midY, modelName);
    }
}
