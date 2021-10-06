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
import com.example.exercisegreenroad.utils.Utils;

public class LocationTask {
    android.location.LocationListener listener;
    android.location.LocationManager locationManager;
     Location location;

    int secondsTimeOut = 0;
    LocationTaskListener taskListener;

    int counter = 0;
    boolean running;

    public void start(int secondsTimeOut, LocationTaskListener locationTaskListener) {
        this.secondsTimeOut = secondsTimeOut;
        this.taskListener = locationTaskListener;

        addLog("start running");

        if (ActivityCompat.checkSelfPermission(AppBase.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(AppBase.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        running = true;

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                requestLocationUpdates();
            }
        });

        runStopTread();
    }

    private void requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(AppBase.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(AppBase.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            running = false;
            return;
        }

        listener = createListener();
        locationManager = (android.location.LocationManager) AppBase.getContext().getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(android.location.LocationManager.GPS_PROVIDER, 1, 1, listener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1, 1, listener);
    }

    private static void addLog(String line) {
        Logger.i("LocationTask", line);
    }
    private static void addErrorLog(String line) {
          Log.e("LocationTask", line);
    }

    private void runStopTread() {
        new Thread(() -> {
            while (location == null && counter < secondsTimeOut && running) {
                addLog("thread sleep 1000 ms");
                Utils.sleep(1000);
                counter++;
            }

            if (running || location == null) {
                addErrorLog("no location received");
            }

            if (running) {
                running=false;
                taskListener.onLocationTaskStopRunning();
            }
        }).start();
    }

    private LocationListener createListener() {
        return new android.location.LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                addLog("location received");
                LocationTask.this.location = location;
                if (taskListener.onLocationTaskReceived(location)) {
                    locationManager.removeUpdates(listener);
                    running=false;
                }
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
    }
}
