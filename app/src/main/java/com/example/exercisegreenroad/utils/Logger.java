package com.example.exercisegreenroad.utils;

import android.content.Intent;
import android.util.Log;

import com.example.exercisegreenroad.device.AppBase;

import java.util.ArrayList;
import java.util.Date;

public class Logger {
    public static final String ACTION_LOG = "com.example.exercisegreenroad.utils.log";

    static {
        log = new ArrayList<>();
    }

    public static ArrayList<Line> log;
    public static int i(String tag, String msg) {
        addLine(tag,msg,false);
        return Log.i("Logger" + tag, msg);
    }

    public static int e(String tag, String msg){
       return e(tag,msg,null);
    }
    public static int e(String tag, String msg, Throwable tr) {
        addLine(tag, msg + ((tr == null) ? "" : (" " + tr.getMessage())), true);
        return Log.e("Logger" + tag, msg, tr);
    }

    private static void addLine(String tag, String msg, boolean isError) {
        Line l = new Line();
        l.date = new Date() ;
        l.text = msg;
        l.tag = tag;
        l.isError = isError;
        log.add(0, l);
        AppBase.getContext().sendBroadcast(new Intent(ACTION_LOG));
    }

    public static class Line {
        public Date date;
        public String text;
        public String tag;
        public boolean isError;
    }
}
