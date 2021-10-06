package com.example.exercisegreenroad.utils;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Json {
    static String format_Json_Date = "yyyy-MM-dd HH:mm:ss";


    public static String toString(Object obj) {
        if (obj == null) {
            return "";
        }

        try {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Date.class, new DateAdapter())
                    .create();

            return gson.toJson(obj);
        } catch (Exception ex) {
            Log.e("JsonMAPPER", "failed to parse to json string" + obj, ex);
            return "";
        }
    }

    public static <T> T toObject(String responseString, Class<T> valueType) {
        if (responseString == null || responseString.length() == 0) {
            return null;
        }

        try {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Date.class, new DateDeAdapter())
                    .create();

            return gson.fromJson(responseString, valueType);
        } catch (Exception e) {
            Log.e("JsonMAPPER", "failed to parse to :" + valueType.getClass().getName() + "/n" + responseString + "/n", e);
        }

        return null;
    }

    public static class DateAdapter implements JsonSerializer<Date> {

        @Override
        public JsonElement serialize(Date src, Type typeOfSrc,
                                     JsonSerializationContext context) {

            String sDate = new SimpleDateFormat(format_Json_Date).format(src);
            return new JsonPrimitive(sDate);
        }
    }

    public static class DateDeAdapter implements JsonDeserializer<Date> {


        @Override
        public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            String jVal = json.getAsString();
            jVal = jVal.replace("T", " ");
            Date d = Utils.getDate(jVal, format_Json_Date);
            return d;
        }
    }
}
