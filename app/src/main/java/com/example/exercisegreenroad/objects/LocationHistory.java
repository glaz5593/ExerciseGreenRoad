package com.example.exercisegreenroad.objects;

import com.example.exercisegreenroad.utils.Utils;

import java.util.ArrayList;

public class LocationHistory {
    public LocationHistory(){
        locations=new ArrayList<>();
    }
    public ArrayList<GLocation> locations;

    public void add(GLocation location) {
        locations.add(0,location);
    }
}
