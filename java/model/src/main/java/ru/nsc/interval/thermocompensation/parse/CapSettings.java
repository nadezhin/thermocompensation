package ru.nsc.interval.thermocompensation.parse;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class CapSettings extends AbstractMeasureItem {

    public static final CapSettings OLD_F0 = new CapSettings(0, 0, 4095);
    public static final CapSettings NEW_F0 = new CapSettings(8, 0, 0);
    public static final CapSettings STD_F0 = new CapSettings(0, 0, 0);
    public final int cc;
    public final int cf;
    public final int xt;

    public CapSettings(int cc, int cf, int xt) {
        this.cc = cc;
        this.cf = cf;
        this.xt = xt;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof CapSettings) {
            CapSettings that = (CapSettings) o;
            return this.cc == that.cc && this.cf == that.cf && this.xt == that.xt;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + this.cc;
        hash = 83 * hash + this.cf;
        hash = 83 * hash + this.xt;
        return hash;
    }

    @Override
    public String toString() {
        return "cc=" + cc + " cf=" + cf + " p=" + xt;
    }

    public static List<List<AbstractMeasureItem>> parseCapSchedule(String fileName) throws IOException {
        Path path = Paths.get(fileName);
        List<String> lines = Files.readAllLines(path, Charset.defaultCharset());
        List<List<AbstractMeasureItem>> capSchedule = new ArrayList<>();
        List<AbstractMeasureItem> capSeq = new ArrayList<>();
        for (int lineNo = 0; lineNo < lines.size(); lineNo++) {
            String originalLine = lines.get(lineNo);
            int ind = originalLine.indexOf('#');
            String line = (ind >= 0 ? originalLine.substring(0, ind) : originalLine).trim();
            if (line.isEmpty()) {
                if (!capSeq.isEmpty()) {
                    capSchedule.add(capSeq);
                    capSeq = new ArrayList<>();
                }
                continue;
            }
            String[] ss = line.split("[ \t]+");
            if (ss.length == 1) {
                if (ss[0].equals("I")) {
                    capSeq.add(AbstractMeasureItem.inpI);
                } else if (ss[0].equals("ID")) {
                    capSeq.add(AbstractMeasureItem.inpID);
                } else {
                    System.err.println("Bad input <" + ss[0] + "> in line " + (lineNo + 1) + " of " + fileName);
                    throw new IOException();
                }
                continue;
            }
            if (ss.length != 3) {
                System.err.println("Error in line " + (lineNo + 1) + " of " + fileName);
                throw new IOException();
            }
            try {
                int cc = Integer.parseInt(ss[0]);
                int cf = Integer.parseInt(ss[1]);
                int xt = Integer.parseInt(ss[2]);
                if (cc < 0 || cc >= 16 || cf < 0 || cf >= 64 || xt < 0 || xt >= 4096) {
                    System.err.println("Error in line " + (lineNo + 1) + " of " + fileName);
                    throw new IOException();
                }
                capSeq.add(new CapSettings(cc, cf, xt));
            } catch (NumberFormatException e) {
                System.err.println("Error in line " + (lineNo + 1) + " of " + fileName);
                throw new IOException();
            }
        }
        if (!capSeq.isEmpty()) {
            capSchedule.add(capSeq);
        }
        return capSchedule;
    }
}
