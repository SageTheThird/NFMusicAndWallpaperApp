package com.obcomdeveloper.realmusic.Utils;

import android.content.Context;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.obcomdeveloper.realmusic.Models.Song;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import needle.Needle;

public class SharedPreferences {

    private static final String TAG = "SharedPreferences";

    protected Context context;
    private android.content.SharedPreferences prefs;

    public SharedPreferences(Context context) {

        this.context=context;
         prefs= PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void saveString(final String key, final String name){

        Needle.onBackgroundThread().execute(new Runnable() {
            @Override
            public void run() {
                android.content.SharedPreferences.Editor editor = prefs.edit();
                editor.putString(key, name);
                editor.apply();
            }
        });

    }

    public String getString(String key,String defaultValue){
        return prefs.getString(key,defaultValue);
    }

    public void saveInt(final String key,final int index){
        Needle.onBackgroundThread().execute(new Runnable() {
            @Override
            public void run() {
                android.content.SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(key, index);
                editor.apply();
            }
        });

    }

    public void saveLong(String key,long index){

        android.content.SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(key, index);
        editor.apply();
    }

    public long getLong(String key){
        return  prefs.getLong(key, -1);
    }
    public int getInt(String key,int defaultValue){
        return  prefs.getInt(key, defaultValue);
    }
    public void saveBoolean(final String key,final boolean value){
        Needle.onBackgroundThread().execute(new Runnable() {
            @Override
            public void run() {
                android.content.SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(key, value);
                editor.apply();
            }
        });

    }
    public boolean getBoolean(String key, boolean defaultValue){
        return prefs.getBoolean(key, defaultValue);
    }
    public void saveList(final List<String> list,final String key){
        Needle.onBackgroundThread().execute(new Runnable() {
            @Override
            public void run() {
                android.content.SharedPreferences.Editor editor = prefs.edit();
                Gson gson = new Gson();
                String json = gson.toJson(list);
                editor.putString(key, json);
                editor.apply();     // This line is IMPORTANT !!!
            }
        });

    }


    public List<String> getList(String key){
        Gson gson = new Gson();
        String json = prefs.getString(key, null);
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        return gson.fromJson(json, type);
    }

    public  void saveObjectsList(final List<Song> list,final String key){
        Needle.onBackgroundThread().execute(new Runnable() {
            @Override
            public void run() {
                android.content.SharedPreferences.Editor editor = prefs.edit();
                Gson gson = new Gson();
                String json = gson.toJson(list);
                editor.putString(key, json);
                editor.apply();     // This line is IMPORTANT !!!
            }
        });

    }


    public List<Song> getObjectsList(String key){
        Gson gson = new Gson();
        String json = prefs.getString(key, null);
        Type type = new TypeToken<ArrayList<Song>>() {}.getType();
        return gson.fromJson(json, type);
    }
}
