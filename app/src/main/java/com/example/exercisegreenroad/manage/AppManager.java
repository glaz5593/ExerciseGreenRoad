package com.example.exercisegreenroad.manage;

import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;

import com.example.exercisegreenroad.device.AppBase;
import com.example.exercisegreenroad.geofencing.GeofenceService;
import com.example.exercisegreenroad.location.*;
import com.example.exercisegreenroad.objects.GLocation;
import com.example.exercisegreenroad.objects.LocationHistory;
import com.example.exercisegreenroad.utils.Utils;

import java.util.Date;

public class AppManager {
    public static final String ACTION_MAIN_POINT_UPDATE = "com.example.exercisegreenroad.manage.ACTION_MAIN_POINT_UPDATE";
    private static AppManager instance;
    public static String ACTION_updateUI = "com.example.exercisegreenroad.manage.updateUI";
    LocationTask task;

    boolean allowMainPointByNetwork;
    Object historyLockObject = "historyLockObject";
    LocationHistory history;
    GLocation mainPoint;

    public static AppManager getInstance() {
        if (instance == null) {
            instance = new AppManager();
        }

        return instance;
    }

    AppManager() {
        history = PreferencesManager.getInstance().getLocationHistory();
        mainPoint = PreferencesManager.getInstance().getMainPoint();

        if (history == null) {
            history = new LocationHistory();
        }

        if (mainPoint == null) {
            askMainPoint();
        }
    }

    private void askMainPoint() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mainPoint == null) {
                    if (task == null || !task.isRunning()) {
                        runLocationTask();
                    }

                    while (task.isRunning() && mainPoint == null) {
                        Utils.sleep(1000);
                    }

                    Utils.sleep(5000);
                }
            }
        }).start();
    }

    private void runLocationTask() {
        task = new LocationTask();
        task.start(20, new LocationTaskListener() {

            @Override
            public boolean onLocationTaskReceived(Location location) {
                if (allowMainPointByNetwork || location.getProvider().equals(android.location.LocationManager.GPS_PROVIDER)) {
                    setMainPoint(location.getLatitude(), location.getLongitude());
                    task.stopRunning();
                    return true;
                }

                return false;
            }

            @Override
            public void onLocationTaskStopRunning() {

            }
        });
    }

    private void setMainPoint(double lat, double lon) {
        AddPointTask task = new AddPointTask(lat, lon, GLocation.TYPE_MAIN);
        task.execute();

        Utils.runOnUI(1000, () -> {
            GeofenceService.start();
        });
    }

    public void addPoint(double lat, double lon,int type) {
        AddPointTask task = new AddPointTask(lat, lon,type);
        task.execute();
    }

    public LocationHistory getHistory() {
        return history;
    }

    public GLocation getMainPoint() {
        return mainPoint;
    }
    public void setMainPoint(GLocation location) {
         mainPoint =location;
         AppBase.getContext().sendBroadcast(new Intent(ACTION_MAIN_POINT_UPDATE));
    }
    public boolean hasMainPoint() {
        return getMainPoint() != null;
    }

    GLocation  lastLocation;
    public void setLastLocation(GLocation location) {
        lastLocation=location;
    }
    public GLocation getLastLocation() {
       return lastLocation;
    }
    public boolean hasLastLocation() {
       return lastLocation!=null;
    }
    private class AddPointTask extends AsyncTask<Void, Void, GLocation> {
        double lat, lon;
        int type;

        private AddPointTask(double lat, double lon, int type) {
            this.lat = lat;
            this.lon = lon;
            this.type = type;
        }

        @Override
        protected GLocation doInBackground(Void... params) {
            try {
                GLocation location = new GLocation();

                location.date = new Date();
                location.lat = lat;
                location.lon = lon;
                location.type = type;
                location.description = GoogleAPIManager.getAddressString(lat, lon);

                if (type==GLocation.TYPE_MAIN) {
                   setMainPoint(location);
                    PreferencesManager.getInstance().saveMainPoint(location);
                } else {
                    synchronized (historyLockObject) {
                        history.add(location);
                        PreferencesManager.getInstance().saveLocationHistory(history);
                    }
                }

                return location;
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(GLocation result) {
            updateUI();
        }
    }

    public void allowMainPointByNetwork() {
        this.allowMainPointByNetwork = true;
    }

    public boolean isAllowMainPointByNetwork() {
        return this.allowMainPointByNetwork;
    }

    private void updateUI() {
        try {
            AppBase.getContext().sendBroadcast(new Intent(ACTION_updateUI));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
