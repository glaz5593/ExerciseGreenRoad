package com.example.exercisegreenroad.location;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.example.exercisegreenroad.device.AppBase;
import com.example.exercisegreenroad.utils.Logger;

public class TrackingLocationTask {
    LocationListener listener;
    LocationManager locationManager;
    Location location;

    LocationTaskListener taskListener;

    boolean running;

    public void start(LocationTaskListener locationTaskListener) {
        this.taskListener = locationTaskListener;

        addLog("start running");

        running = true;

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                requestLocationUpdates();
            }
        });
    }

    private void requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(AppBase.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(AppBase.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            running = false;
            return;
        }

        listener = createListener();
        locationManager = (LocationManager) AppBase.getContext().getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 10, listener);
       }

    private static void addLog(String line) {
        Logger.i("TrackingLocationTask", line);
    }
    private static void addErrorLog(String line) {
          Log.e("LocationTask", line);
    }

    private LocationListener createListener() {
        return new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                addLog("location received");
                TrackingLocationTask.this.location = location;
                taskListener.onLocationTaskReceived(location);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
                addLog( "onStatusChanged");
            }

            @Override
            public void onProviderEnabled(String provider) {
                addLog( "onProviderEnabled");
            }

            @Override
            public void onProviderDisabled(String provider) {
                addLog( "onProviderDisabled");
            }
        };
    }
    
    public boolean isRunning() {
        return running;
    }

    public void stopRunning() {
        running=false;
        try {
            locationManager.removeUpdates(listener);
        } catch (Exception e) {

        }
    }
}
