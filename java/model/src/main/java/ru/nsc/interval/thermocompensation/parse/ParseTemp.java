package ru.nsc.interval.thermocompensation.parse;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 */
public class ParseTemp {

    public static class TimeTemp extends Parse.Meas {

        public final long time;
        public final double temp;
        public final double slope;

        public TimeTemp(long time, double temp) {
            this(time, temp, 0);
        }

        public TimeTemp(long time, double temp, double slope) {
            this.time = time;
            this.temp = temp;
            this.slope = slope;
        }

        private TimeTemp(TimeTemp tt) {
            this(tt.time, tt.temp, tt.slope);
        }

        @Override
        public long getTime() {
            return time;
        }

        @Override
        public double getValue() {
            return temp;
        }
    }
    public final List<TimeTemp> timeCmds = new ArrayList<>();
    public final List<TimeTemp> timeTemps = new ArrayList<>();
    public final long startTime;

    public ParseTemp(Path path) throws IOException, ParseException {
        Date oldTime = null;
        boolean lastRequestTemp = true;
        List<String> lines = Files.readAllLines(path, Charset.forName("windows-1251"));
        boolean foundPowerOff = false;
        for (String line : lines) {
            line = line.replace("\\r\\n", "");
            try {
                String tempS, dateS, timeS;
                double slope = 0;
                if (line.startsWith("*")) {
                    lastRequestTemp = line.equals("*TEMP?");
                    continue;
                }
                if (line.startsWith("NA:")) {
                    continue;
                } else if (line.startsWith("OK")) {
                    String[] ss = line.split(" +");
                    assert ss.length == 3;
                    if (ss[0].equals("OK:1,POWER,ON")) {
                        continue;
                    }
                    if (ss[0].equals("OK:1,POWER,OFF")) {
                        foundPowerOff = true;
                        continue;
                    }
                    if (ss[0].startsWith("OK:1,RUNPRGM,TEMP")) {
                        int gotempInd = ss[0].indexOf("GOTEMP");
                        int timeInd = ss[0].indexOf("TIME");
                        int colonInd = ss[0].lastIndexOf(':');
                        tempS = ss[0].substring("OK:1,RUNPRGM,TEMP".length(), gotempInd);
                        dateS = ss[1];
                        timeS = ss[2];
                        slope = (Double.valueOf(ss[0].substring(gotempInd + 6, timeInd))
                                - Double.valueOf(tempS))
                                / (Double.valueOf(ss[0].substring(timeInd + 4, colonInd)) * 60
                                + Double.valueOf(ss[0].substring(colonInd + 1)));
                    } else if (ss[0].startsWith("OK:1,TEMP,S")) {
                        tempS = ss[0].substring("OK:1,TEMP,S".length());
                        dateS = ss[1];
                        timeS = ss[2];
                    } else {
                        continue;
                    }
                } else {
                    String[] ss = line.split("[ ]+|[,]");
                    assert ss.length == 6;
                    tempS = ss[0];
                    dateS = ss[4];
                    timeS = ss[5];
                }
                assert dateS.length() == 10;
                assert timeS.length() == 12;
                Date time = Parse.parseDateAndTime(dateS, timeS);
                assert oldTime == null || oldTime.getTime() <= time.getTime();
                oldTime = time;
                double temp = Double.valueOf(tempS);
                if (line.startsWith("OK")) {
                    timeCmds.add(new TimeTemp(time.getTime(), temp, slope));
                } else if (lastRequestTemp) {
                    timeTemps.add(new TimeTemp(time.getTime(), temp));
                }
            } catch (AssertionError e) {
                System.out.println("Incomplete " + line);
                break;
            }
        }
        if (!foundPowerOff) {
            System.out.println("Incomplete - without POWER,OFF");
        }
        startTime = Math.min(timeCmds.get(0).time, timeTemps.get(0).time);
    }

    public ParseTemp(ParseTemp pt) {
        this.startTime = pt.startTime;
        for (TimeTemp tt : pt.timeCmds) {
            timeCmds.add(new TimeTemp(tt));
        }
        for (TimeTemp tt : pt.timeTemps) {
            timeTemps.add(new TimeTemp(tt));
        }

    }

    public Date getCmdDate(int k) {
        return new Date(timeCmds.get(k).time);
    }

    public double getCmdTemp(int k) {
        return timeCmds.get(k).temp;
    }

    public double getCmdTemp(Date time) {
        int index = ParseSeq.binarySearch(timeCmds, time);
        if (index < 0) {
            index = -index - 1;
        }
        ParseTemp.TimeTemp tt = timeCmds.get(Math.max(index - 1, 0));
        return tt.temp + tt.slope * (time.getTime() - tt.time) / 6e4;
    }

    public double getTemp(Date time) {
        return timeTemps.isEmpty() ? 0 : ParseSeq.getValue(timeTemps, time);
    }

    public double getMinTemp() {
        double result = Double.POSITIVE_INFINITY;
        for (ParseTemp.TimeTemp t : timeTemps) {
            result = Math.min(result, t.temp);
        }
        return result;
    }

    public double getMaxTemp() {
        double result = Double.NEGATIVE_INFINITY;
        for (ParseTemp.TimeTemp t : timeTemps) {
            result = Math.max(result, t.temp);
        }
        return result;
    }
}
