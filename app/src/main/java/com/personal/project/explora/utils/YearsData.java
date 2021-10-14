package com.personal.project.explora.utils;

import com.personal.project.explora.R;

import java.util.HashMap;
import java.util.Map;

public class YearsData {

    public static final int[] YEARS = { 2021, 2020, 2019, 2018, 2017 };

    public static final int[] YEAR_IMAGE_RES = {
            R.drawable.pic_2021,
            R.drawable.pic_2020,
            R.drawable.pic_2019,
            R.drawable.pic_2018,
            R.drawable.pic_2017
    };

    private static Map<Integer, Integer> yearRes = null;

    public static int getYearImageRes(int year) {

        if (yearRes == null) {

            yearRes = new HashMap<>();
            for (int i = 0; i < YEARS.length; i++) {
                yearRes.put(YEARS[i], YEAR_IMAGE_RES[i]);
            }
        }

        return yearRes.get(year);
    }

}
