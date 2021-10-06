package com.example.exercisegreenroad.device;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import com.example.exercisegreenroad.geofencing.GeofenceService;

public class AppBase extends Application {
    public static final String CHANNEL_ID = "backgroundServiceChannel";
    private static AppBase mInstance;
    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        createNotificationChannel();
    }

    public static synchronized AppBase getInstance() {
        return mInstance;
    }

    public static synchronized Context getContext() {
        return mInstance.getApplicationContext();
    }

    private void createNotificationChannel(){

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel  serviceChannel = new NotificationChannel(CHANNEL_ID,
                    "Background Service Channel",
                    NotificationManager.IMPORTANCE_MIN);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
}
