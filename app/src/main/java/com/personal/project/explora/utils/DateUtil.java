package com.personal.project.explora.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateUtil {

    private static final String TAG = "DateUtil";

    public static LocalDate parse(@NonNull String date) {
        LocalDate localDate = null;
        try {
            localDate = LocalDate.parse(date, DateTimeFormatter.RFC_1123_DATE_TIME);
        } catch (DateTimeParseException e) {
            //Log.e(TAG, "parse: Unable to parse date " + date, e);
        }

        return localDate;
    }

    /*public static LocalDate parseMyDate(@NonNull String date) {
        LocalDate localDate = null;
        try {
            localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd.MM.yyyy."));
        } catch (DateTimeParseException e) {
            Log.e(TAG, "parse: Unable to parse date " + date, e);
        }

        return localDate;
    }*/

    public static String formatMyDate(@NonNull LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy.");
        return date.format(formatter);
    }

    public static int compareStringDateTimes(String A, String B) {
        LocalDateTime a = LocalDateTime.parse(A);
        LocalDateTime b = LocalDateTime.parse(B);
        if (a.isEqual(b))
            return 0;

        if (a.isBefore(b))
            return 1;

        return -1;
    }

}
