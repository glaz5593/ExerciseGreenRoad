package com.example.exercisegreenroad.manage;

import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.example.exercisegreenroad.device.AppBase;
import com.example.exercisegreenroad.utils.Logger;

import java.util.List;
import java.util.Locale;

public class GoogleAPIManager {

    public static String getAddressString(double lat, double lon) {

        try {
            Geocoder geocoder = new Geocoder(AppBase.getContext(), Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
            if (addresses != null) {
                Address address = addresses.get(0);
                StringBuilder builder = new StringBuilder();

                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    builder.append(address.getAddressLine(i));
                    if(i<address.getMaxAddressLineIndex()){
                        builder.append("\n");
                    }
                }

                 Logger.i("getAddress", builder.toString());
                return builder.toString();
            }
        } catch (Exception e) {

        }
        return "";
    }
}
