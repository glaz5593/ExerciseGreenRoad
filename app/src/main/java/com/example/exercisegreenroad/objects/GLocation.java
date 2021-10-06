package com.example.exercisegreenroad.objects;

import com.example.exercisegreenroad.utils.Utils;
import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

public class GLocation {
    public static final int TYPE_MAIN = 0;
    public static final int TYPE_ENTER = 1;
    public static final int TYPE_EXIT = 2;
    public static final int TYPE_TRACKING = 3;
    public double lat;
    public double lon;
    public Date date;
    public String description;
    public int type;

    public String getDescription() {
        if(Utils.isNullOrEmpty(description)){
            return lat+","+  lon;
        }

        return description;
    }

    public LatLng getPoint() {
        return new LatLng(lat, lon);
    }
}
