package com.example.exercisegreenroad.geofencing;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.exercisegreenroad.R;
import com.example.exercisegreenroad.device.AppBase;
import com.example.exercisegreenroad.manage.AppManager;
import com.example.exercisegreenroad.objects.GLocation;
import com.example.exercisegreenroad.ui.MapsActivity;
import com.example.exercisegreenroad.utils.Logger;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


public class GeofenceService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    public final static String TAG = "GeofenceService";
    public final static int NOTIFICATION_STATUS_ID = 12348;
    public final static int RADIUS_IN_METERS = 150;
    BroadcastReceiver updateMainPointReceiver;
    boolean mainPointRegistered;

    private GoogleApiClient mGoogleApiClient;
    private ArrayList<Geofence> geofencesToAdd;
    private GeofencingClient mGeofencingClient;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isRunning = true;

        startForeground(NOTIFICATION_STATUS_ID, buildNotification(getApplicationContext()));

        Logger_i(TAG, "onCreate()");

        updateMainPointReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                registerMainPoint();
            }
        };
        registerReceiver(updateMainPointReceiver, new IntentFilter(AppManager.ACTION_MAIN_POINT_UPDATE));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger_i(TAG, "onStartCommand()");

        if (!mainPointRegistered) {
            mainPointRegistered=true;
            registerMainPoint();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger_i(TAG, "onDestroy");
        isRunning = false;

        unregisterReceiver(updateMainPointReceiver);
    }


    private void registerMainPoint() {
        if (!AppManager.getInstance().hasMainPoint()) {
            Logger_i(TAG, "registerMainPoint() --> has no MainPoint");
            return;
        }

        Logger_i(TAG, "registerMainPoint()");

        if(mGeofencingClient!=null) {
            mGeofencingClient.removeGeofences(createRequestPendingIntent());
        }
        addGeofence();
    }

    private void addGeofence() {
        Logger_i(TAG, "addGeofence()");

        GLocation location=AppManager.getInstance().getMainPoint();
        String id = location.lat + " , " + location.lon;
        Geofence geofence = new Geofence.Builder()
                .setRequestId(id)
                .setCircularRegion(location.lat , location.lon, RADIUS_IN_METERS)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER
                        | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();

        geofencesToAdd = new ArrayList();
        geofencesToAdd.add(geofence);

        Logger_i(TAG,"register");
        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
        mGeofencingClient = LocationServices.getGeofencingClient(getApplicationContext());
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Logger_i(TAG,"onConnected");

        createRequestPendingIntent();
        addGeofence(0);
    }

    public void addGeofence(int numTry) {
        Logger_i(TAG,"addGeofence numTry("+numTry+")");

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        if (geofencesToAdd == null || geofencesToAdd.isEmpty()) {
            Logger_i(TAG,"addGeofence 'geofencesToAdd' is empty");
            return;
        }

        mGeofencingClient.addGeofences(getGeofencingRequest(), createRequestPendingIntent())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if (geofencesToAdd != null) {
                            Logger_i(TAG, "Registerer: " + geofencesToAdd.toString());
                        }else{
                            Logger_i(TAG, "Registerer onSuccess");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if(numTry < 10) {

                            Logger_i(TAG, "Registerer onFailure();");
                            new Handler().postDelayed(() -> {
                                addGeofence(numTry + 1);
                            }, TimeUnit.SECONDS.toMillis(5));
                        }

                    }
                });
    }

    private void Logger_i(String tag, String s) {
        Logger.i(tag,s);
        //Toast.makeText(AppBase.getContext(), tag+"\n"+s, Toast.LENGTH_SHORT).show();
    }

    private PendingIntent createRequestPendingIntent() {
        Intent intent1 = new Intent(getApplicationContext(), GeofencingBroadcastReceiver.class);
        intent1.setAction(GeofencingBroadcastReceiver.ACTION_PROCESS_UPDATES);
        return PendingIntent.getBroadcast(getApplicationContext(), 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_EXIT);
        builder.addGeofences(geofencesToAdd);
        return builder.build();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }



    public static Notification buildNotification(Context context) {
        int titleResId = R.string.app_name;
        int iconID = R.drawable.ic_baseline_location_on_24;

        NotificationChannel chan = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            chan = new NotificationChannel(AppBase.CHANNEL_ID, "MAPS", NotificationManager.IMPORTANCE_MIN);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            chan.setSound(null, null);
            chan.setShowBadge(false);
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(chan);
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, AppBase.CHANNEL_ID)
                        .setSmallIcon(iconID)
                        .setContentTitle(context.getResources().getString(titleResId))
                        .setShowWhen(false)
                        .setPriority(Notification.PRIORITY_MAX)
                        .setContentText("")
                        .setOngoing(true);

        Intent targetIntent = new Intent(context, MapsActivity.class);
        targetIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 999, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        return builder.build();
    }

    static boolean isRunning;

    public static void start() {
        if (isRunning) {
            return;
        }

        if (!AppManager.getInstance().hasMainPoint()) {
            return;
        }

        ContextCompat.startForegroundService(AppBase.getContext(), new Intent(AppBase.getContext(), GeofenceService.class));
    }

    public static void stop() {
        if (!isRunning) {
            return;
        }

        AppBase.getContext().stopService(new Intent(AppBase.getContext(), GeofenceService.class));
    }
}
