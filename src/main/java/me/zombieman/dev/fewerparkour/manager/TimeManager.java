package me.zombieman.dev.fewerparkour.manager;

import me.zombieman.dev.fewerparkour.FewerParkour;
import me.zombieman.dev.fewerparkour.enums.Times;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeManager {

    private FewerParkour plugin;

    public TimeManager(FewerParkour plugin) {
        this.plugin = plugin;
    }

    public static String convert(long time, Times times) {

        switch (times) {
            case SECONDS -> {
                return String.valueOf(time);
            }
            case MINUTES -> {
                return String.valueOf(time / 60);
            }
            case HOURS -> {
                return String.valueOf(time / 60 / 60);
            }
            case DAYS -> {
                return String.valueOf(time / 60 / 60 / 24);
            }
            case WEEKS -> {
                return String.valueOf(time / 60 / 60 / 24 / 7);
            }
            case MONTHS -> {
                return String.valueOf(time / 60 / 60 / 24 / 30);
            }
            case YEARS -> {
                return String.valueOf(time / 60/ 60 / 24 / 365);
            }
            case FORMATTED -> {
                return compileTime(time);
            }

        }

        return "n/a";
    }


    private static final String[] TIME_UNITS = {"Millennium", "Century", "Decade", "Year", "Month", "Week", "Day", "Hour", "Minute", "Second"};
    private static final long[] UNIT_SECONDS = {
            3600 * 24 * 365L * 1000,   // Millennium
            3600 * 24 * 365L * 100,    // Century
            3600 * 24 * 365L * 10,     // Decade
            3600 * 24 * 365L,          // Year
            3600 * 24 * 30L,           // Month
            3600 * 24 * 7L,            // Week
            3600 * 24L,                // Day
            3600L,                     // Hour
            60L,                       // Minute
            1L                         // Second
    };


    public static long decompileTime(String timeString) {
        if (timeString == null || timeString.isEmpty()) {
            return 0;
        }
        long totalSeconds = 0;
        Matcher matcher = Pattern.compile("(\\d+)\\s*(Millennium|Century|Decade|Year|Month|Week|Day|Hour|Minute|Second)s?").matcher(timeString);
        while (matcher.find()) {
            long value = Long.parseLong(matcher.group(1));
            String unit = matcher.group(2);
            for (int i = 0; i < TIME_UNITS.length; i++) {
                if (TIME_UNITS[i].equals(unit)) {
                    totalSeconds += value * UNIT_SECONDS[i];
                    break;
                }
            }
        }
        return totalSeconds;
    }

    public static String compileTime(long totalSeconds) {
        if (totalSeconds < 0) {
            return "n/a";
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < TIME_UNITS.length; i++) {
            long value = totalSeconds / UNIT_SECONDS[i];
            if (value > 0) {
                result.append(value).append(" ").append(TIME_UNITS[i]).append(value > 1 ? "s " : " ");
                totalSeconds %= UNIT_SECONDS[i];
            }
        }
        return result.length() > 0 ? result.toString().trim() : "n/a";
    }
}
