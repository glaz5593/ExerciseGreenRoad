package com.example.exercisegreenroad.geofencing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.widget.Toast;

import com.example.exercisegreenroad.device.AppBase;
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
        Logger_i(TAG, "onReciver");

        this.context = context;
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);
        if (event == null || event.hasError()) {
            Logger_i(TAG, "onReciver --> event is null");
            return;
        }

        if (event.hasError()) {
            onError(event.getErrorCode());
            Logger_i(TAG, "EROOR " + event.getErrorCode());
        } else {
            int transition = event.getGeofenceTransition();
            if (transition == Geofence.GEOFENCE_TRANSITION_ENTER || transition == Geofence.GEOFENCE_TRANSITION_DWELL || transition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                Location l= event.getTriggeringLocation();
                if (transition == Geofence.GEOFENCE_TRANSITION_ENTER || transition == Geofence.GEOFENCE_TRANSITION_DWELL) {
                   Logger_i(TAG,"on enter " +l);
                    AppManager.getInstance().addPoint(l.getLatitude(),l.getLongitude(), GLocation .TYPE_ENTER);
                } else if (transition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                    Logger_i(TAG,"on exit "+l);
                    AppManager.getInstance().addPoint(l.getLatitude(),l.getLongitude(), GLocation .TYPE_EXIT);
                }
            }
        }
    }

    private void Logger_i(String tag, String s) {
        Logger.i(tag,s);
        //Toast.makeText(AppBase.getContext(), tag+"\n"+s, Toast.LENGTH_SHORT).show();
    }


    protected void onError(int errorCode) {
        Logger.e(TAG, "Error: " + errorCode);
        Toast.makeText(context,"Geofance Error: " + errorCode,Toast.LENGTH_LONG).show();
    }
}
