package com.quicktaxi;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Utils {

    @SuppressLint("SimpleDateFormat")
    public static String nowTime() {
        return new SimpleDateFormat("HH:mm").format(new Date());
    }

    @SuppressLint("SimpleDateFormat")
    public static String addSecondsToJavaUtilDate(int seconds) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.SECOND, seconds);
        return new SimpleDateFormat("HH:mm").format(calendar.getTime());
    }
}