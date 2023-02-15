package com.personal.project.explora.utils;

public class StringUtils {

    public static boolean isEmpty(String obj) {
        return obj == null || obj.isEmpty() || obj.equals("null");
    }

    public static String toTimestamp(int minutes, int seconds) {
        String minutesStr = String.valueOf(minutes);
        String secondsStr = String.valueOf(seconds);
        if (secondsStr.length() == 1) secondsStr = "0" + secondsStr;
        return minutesStr + ":" + secondsStr;
    }

    public static String timestampToMSS(long position) {
        int totalSeconds = (int) Math.floor((int)(position / 1000));
        int minutes = totalSeconds / 60;
        int remainingSeconds = totalSeconds - (minutes * 60);

        return (position < 0) ? "--:--" : toTimestamp(minutes, remainingSeconds);
    }

}
