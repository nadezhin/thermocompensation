package ru.nsc.interval.thermocompensation.model;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Parse a list of the thermocomensation device parameters. The format is
 * chipno: device-parameters # f = freq += df chipno - id of device in a group
 * device-parameters - device parameters as defined in DeviceParams freq -
 * target frequency df - maximal deviation from the target frequency
 */
public class ParseTestInps {

    public static class ExtendedInp {

        public final PolyState.Inp inp;
        public final double f;
        public final double df;

        public ExtendedInp(PolyState.Inp inp, double f, double df) {
            this.inp = inp;
            this.f = f;
            this.df = df;
        }
    }

    public static List<List<PolyState.Inp>> parseTestInps(Path path) throws IOException {
        List<List<PolyState.Inp>> result = new ArrayList<List<PolyState.Inp>>();
        for (List<ExtendedInp> l : parseTestExtendedInps(path)) {
            List<PolyState.Inp> r = new ArrayList<PolyState.Inp>();
            for (ExtendedInp ei : l) {
                r.add(ei.inp);
            }
            result.add(r);
        }
        return result;
    }

    public static List<List<ExtendedInp>> parseLogExtendedInps(Path path) throws IOException {
        return parseTestExtendedInps(path, true);
    }

    public static List<List<ExtendedInp>> parseTestExtendedInps(Path path) throws IOException {
        return parseTestExtendedInps(path, false);
    }

    public static List<List<ExtendedInp>> parseTestExtendedInps(Path path, boolean isLog) throws IOException {
        List<String> lines = Files.readAllLines(path, Charset.defaultCharset());
        List<List<ExtendedInp>> testInps = new ArrayList<>();
        for (int lineNo = 0; lineNo < lines.size(); lineNo++) {
            String originalLine = lines.get(lineNo);
            if (isLog) {
                int colonIndex = originalLine.indexOf(':');
                if (colonIndex < 0 || originalLine.startsWith("Warning:")) {
                    continue;
                }
            }
            int ind = originalLine.indexOf('#');
            String line = (ind >= 0 ? originalLine.substring(0, ind) : originalLine).trim();
            if (line.isEmpty()) {
                continue;
            }
            int colonIndex = line.indexOf(':');
            if (colonIndex < 0) {
                System.err.println("Error in line " + (lineNo + 1) + " of " + path);
                throw new IOException();
            }
            try {
                int chipNo = Integer.parseInt(line.substring(0, colonIndex)) - 1;
                PolyState.Inp inp = new PolyState.Inp(DeviceParams.fromNom(line.substring(colonIndex + 1)));
                if (chipNo < 0 || chipNo >= 64) {
                    System.err.println("Error in line " + (lineNo + 1) + " of " + path);
                    throw new IOException();
                }
                while (chipNo >= testInps.size()) {
                    testInps.add(new ArrayList<ExtendedInp>());
                }
                String comment = ind >= 0 ? originalLine.substring(ind + 1).trim() : "";
                double f = Double.NaN;
                double df = 0;
                int indPlusMinus = comment.indexOf("+-");
                if (comment.startsWith("f =") && indPlusMinus >= 0) {
                    f = Double.parseDouble(comment.substring("f =".length(), indPlusMinus));
                    df = Double.parseDouble(comment.substring(indPlusMinus + "+-".length()));
                }
                testInps.get(chipNo).add(new ExtendedInp(inp, f, df));
            } catch (NumberFormatException e) {
                System.err.println("Error in line " + (lineNo + 1) + " of " + path);
                throw new IOException();
            }
        }
        return testInps;
    }
}
