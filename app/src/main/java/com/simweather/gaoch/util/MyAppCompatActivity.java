package com.simweather.gaoch.util;

import android.app.Activity;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MyAppCompatActivity extends AppCompatActivity {
    public static Map<String,Activity>destroyMap = new HashMap<>();


    public static void addDestroyActivity(Activity activity, String activityName){
        destroyMap.put(activityName,activity);
    }
    public static void destroyActivity(String activityName){
        Set<String> keySet=destroyMap.keySet();
        if(keySet.size()>0){
            for(String key:keySet){
                if(activityName.equals(key)){
                    destroyMap.get(key).finish();
                }
            }
        }
    }
}
