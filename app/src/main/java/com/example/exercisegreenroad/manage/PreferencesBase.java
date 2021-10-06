package com.example.exercisegreenroad.manage;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.exercisegreenroad.device.AppBase;

public abstract class PreferencesBase {
    private String prefKey;
    private Context context;

    protected PreferencesBase(Context context, String prefKey) {
        this.context = context;
        this.prefKey = prefKey;
    }

    public SharedPreferences sharedPreferences = null;
    public SharedPreferences.Editor editor = null;

    private SharedPreferences getSharedPreferences() {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(prefKey, 0);
        }

        return sharedPreferences;
    }
    public SharedPreferences.Editor getEditor() {
        if (editor == null) {
            editor = getSharedPreferences().edit();
        }
        return editor;
    }
    public void clear() {
        getEditor().clear();
        getEditor().commit();
    }
    public String get(String key, String defaultValue) {
        String res = getSharedPreferences().getString(key, defaultValue);
        return res;
    }
    public void put(String key, String value) {
        getEditor().putString(key, value);
        getEditor().commit();
    }
}
