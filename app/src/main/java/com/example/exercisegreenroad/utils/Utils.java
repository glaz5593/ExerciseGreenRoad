package com.example.exercisegreenroad.utils;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import com.example.exercisegreenroad.device.AppBase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils {
    public static Date getDate(String date, String format) {
        if (date == null) {
            return null;
        }

        try {
            return new SimpleDateFormat(format).parse(date);
        } catch (Exception ex) {
        }

        return null;
    }

    public static boolean isNullOrEmpty(String value) {
        return value == null || value.length() == 0;
    }

    public static boolean equals(String str1, String str2) {
        String s1 = str1 == null ? "" : str1;
        String s2 = str2 == null ? "" : str2;
        return s2.equals(s1);
    }

    public static int getDP(int px) {
        return (int)(px / AppBase.getContext().getResources().getDisplayMetrics().density);
    }

    public  interface runOnUIListener{void run();}
    public static void runOnUI(runOnUIListener listener) {
        runOnUI(0,listener);
    }
    public static void runOnUI(int timeout,runOnUIListener listener) {
        try {
            if (timeout > 0) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        sleep(timeout);
                        runOnUI(0,listener);
                    }
                }).start();
            } else {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            listener.run();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sleep(long time){
        try{
            Thread.sleep(time);
        }catch (Exception e){

        }
    }

    public static int getColor(int resId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return AppBase.getContext().getResources().getColor(resId, AppBase.getContext().getTheme());
        } else {
            return AppBase.getContext().getResources().getColor(resId);
        }
    }

    public static double getDouble(String value) {
        return tryParseDouble(value, 0d);
    }

    public static double tryParseDouble(String value, Double defaultValue) {
        try {
            return Double.parseDouble(value);
        } catch (Exception Ex) {
        }
        return defaultValue;
    }

    public static SimpleDateFormat format_dd_MM_yyyy = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
    public static SimpleDateFormat format_dd_MM_HH_mm = new SimpleDateFormat("dd/MM HH:mm", Locale.ENGLISH);
    public static SimpleDateFormat format_HH_mm = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
    public static SimpleDateFormat format_HH_mm_ss = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
    public static String getTime(Date date) {
        if (date == null)
            return "";

        return format_HH_mm.format(date);
    }
    public static String getFullTime(Date date) {
        if (date == null)
            return "";

        return format_HH_mm_ss.format(date);
    }
    public static Date getCurrentDate() {
        return new Date();
    }public static String getCurrentTime() {
        return getTime(getCurrentDate());
    }
    public static String getTimeOrDateTime(Date date) {
        if (date == null) {
            return "";
        }

        String currentDate = getDate(getCurrentDate());
        String dDate = getDate(date);

        if (equals(currentDate, dDate)) {
            return format_HH_mm.format(date);
        }

        return format_dd_MM_HH_mm.format(date);
    }
    public static String getDate(Date date) {
        if (date == null)
            return "";

        return format_dd_MM_yyyy.format(date);
    }

    public static String getHTMLText_green(String text) {
        return "<font color=\"#009400\">" + text + "</font>";
    }

    public static String getHTMLText_red(String text) {
        return "<font color=\"#ba0c0c\">" + text + "</font>";
    }

    public static String getHTMLText_black(String text) {
        return "<font color=\"black\">" + text + "</font>";
    }

    public static String getHTMLText_blue(String text) {
        return "<font color=\"blue\">" + text + "</font>";
    }

    public static String getHTMLEnter() {
        return "<BR>";
    }

    public static String getHTMLText_bold(String text) {
        return "<B>" + text + "</B>";
    }

    public static String getHTMLText_underline(String text) {
        return "<U>" + text + "</U>";
    }

}
