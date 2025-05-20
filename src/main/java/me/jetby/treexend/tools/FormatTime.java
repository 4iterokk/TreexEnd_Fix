package me.jetby.treexend.tools;

import me.jetby.treexend.Main;
import me.jetby.treexend.configurations.Config;


public class FormatTime {
    private final Config config;
    public FormatTime(Main plugin) {
        this.config = plugin.getCfg();
    }
    public String stringFormat(int totalSeconds) {
        int weeks = totalSeconds / (7 * 24 * 3600);
        int days = (totalSeconds % (7 * 24 * 3600)) / (24 * 3600);
        int hours = (totalSeconds % (24 * 3600)) / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        StringBuilder formattedTime = new StringBuilder();

        if (weeks > 0) {
            formattedTime.append(formatUnit(weeks,
                    config.getFormattedTimeWeeks().get(0),
                    config.getFormattedTimeWeeks().get(1),
                    config.getFormattedTimeWeeks().get(2))).append(" ");
        }
        if (days > 0) {
            formattedTime.append(formatUnit(days,
                    config.getFormattedTimeDays().get(0),
                    config.getFormattedTimeDays().get(1),
                    config.getFormattedTimeDays().get(2))).append(" ");
        }
        if (hours > 0) {
            formattedTime.append(formatUnit(hours,
                    config.getFormattedTimeHours().get(0),
                    config.getFormattedTimeHours().get(1),
                    config.getFormattedTimeHours().get(2))).append(" ");
        }
        if (minutes > 0) {
            formattedTime.append(formatUnit(minutes,
                    config.getFormattedTimeMinutes().get(0),
                    config.getFormattedTimeMinutes().get(1),
                    config.getFormattedTimeMinutes().get(2))).append(" ");
        }
        if (seconds > 0) {
            formattedTime.append(formatUnit(seconds,
                    config.getFormattedTimeSeconds().get(0),
                    config.getFormattedTimeSeconds().get(1),
                    config.getFormattedTimeSeconds().get(2))).append(" ");
        }
        if (formattedTime.length() == 0) {
            formattedTime.append("0 сек");
        }
        return formattedTime.toString().trim();
    }


    private String formatUnit(int value, String form1, String form2, String form5) {
        value = Math.abs(value);
        int remainder10 = value % 10;
        int remainder100 = value % 100;

        if (remainder10 == 1 && remainder100 != 11) {
            return String.format("%d %s", value, form1);
        } else if (remainder10 >= 2 && remainder10 <= 4 && (remainder100 < 10 || remainder100 >= 20)) {
            return String.format("%d %s", value, form2);
        } else {
            return String.format("%d %s", value, form5);
        }
    }
}
