package com.example.exercisegreenroad.manage;

import android.content.Context;

import com.example.exercisegreenroad.device.AppBase;
import com.example.exercisegreenroad.objects.GLocation;
import com.example.exercisegreenroad.objects.LocationHistory;
import com.example.exercisegreenroad.utils.Json;

public class PreferencesManager extends PreferencesBase{
    private static PreferencesManager instance;
    public static PreferencesManager getInstance() {
        if (instance == null) {
            instance = new PreferencesManager(AppBase.getContext(), "Pref13");
        }

        return instance;
    }
    private PreferencesManager(Context context, String prefKey) {
        super(context, prefKey);
    }

    public LocationHistory getLocationHistory(){
        String j= get("LocationHistory","");
        if(j.length()==0){
            return null;
        }

        return Json.toObject(j,LocationHistory.class);
    }

    public void saveLocationHistory(LocationHistory locationHistory){
        put("LocationHistory",Json.toString(locationHistory));
    }


    public GLocation getMainPoint(){
        String j= get("MainPoint","");
        if(j.length()==0){
            return null;
        }

        return Json.toObject(j,GLocation.class);
    }

    public void saveMainPoint(GLocation point){
        put("MainPoint",Json.toString(point));
    }
}
