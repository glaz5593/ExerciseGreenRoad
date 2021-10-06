package com.example.exercisegreenroad.location;

import android.location.Location;

public interface LocationTaskListener {
    boolean onLocationTaskReceived(Location location);
    void onLocationTaskStopRunning();
}
