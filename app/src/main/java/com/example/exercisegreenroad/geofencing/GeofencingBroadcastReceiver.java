package com.example.exercisegreenroad.geofencing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.exercisegreenroad.manage.AppManager;
import com.example.exercisegreenroad.objects.GLocation;
import com.example.exercisegreenroad.utils.Logger;
import com.example.exercisegreenroad.utils.Utils;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.Arrays;

public class GeofencingBroadcastReceiver extends BroadcastReceiver {
    public final static String TAG = "GeofencingReceiver";
    final static String ACTION_PROCESS_UPDATES = "ACTION_PROCESS_UPDATES";
    Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.i(TAG, "GeofencingBroadcastReceiver onReciver");

        this.context = context;
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);
        if (event == null || event.hasError()) {
            return;
        }
        Logger.i(TAG, "onHandleIntent event");

        if (event.hasError()) {
            onError(event.getErrorCode());
            Logger.i(TAG, "EROOR " + event.getErrorCode());
        } else {
            int transition = event.getGeofenceTransition();
            if (transition == Geofence.GEOFENCE_TRANSITION_ENTER || transition == Geofence.GEOFENCE_TRANSITION_DWELL || transition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                String[] geofenceIds = new String[event.getTriggeringGeofences().size()];
                for (int index = 0; index < event.getTriggeringGeofences().size(); index++) {
                    geofenceIds[index] = event.getTriggeringGeofences().get(index).getRequestId();
                }

                if (transition == Geofence.GEOFENCE_TRANSITION_ENTER || transition == Geofence.GEOFENCE_TRANSITION_DWELL) {
                    onEnteredGeofences(geofenceIds);
                } else if (transition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                    onExitedGeofences(geofenceIds);
                }
            }
        }
    }

    protected void onEnteredGeofences(String[] geofenceIds) {
        for (String geofenceId : geofenceIds) {
            Logger.i(TAG, "onEnter " + geofenceId);
            addPoint(geofenceId,true);
        }
     }

    private void addPoint(String geofenceId, boolean isEnter) {
        String[] arr=geofenceId.split(",");
        double lat= Utils.getDouble (arr[0].trim());
        double lon= Utils.getDouble (arr[1].trim());
        AppManager.getInstance().addPoint(lat,lon,isEnter ? GLocation .TYPE_ENTER: GLocation .TYPE_EXIT);
    }

    protected void onExitedGeofences(String[] geofenceIds) {
        for (String geofenceId : geofenceIds) {
            Logger.i(TAG, "onExit " + geofenceId);
            addPoint(geofenceId,false);
        }
    }

    protected void onError(int errorCode) {
        Logger.e(TAG, "Error: " + errorCode);
        Toast.makeText(context,"Geofance Error: " + errorCode,Toast.LENGTH_LONG).show();
    }
}
