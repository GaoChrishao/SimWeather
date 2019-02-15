package com.simweather.gaoch.util;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.util.TypedValue;

import com.google.gson.Gson;
import com.simweather.gaoch.LocalDatabaseHelper;
import com.simweather.gaoch.WeatherActivity;
import com.simweather.gaoch.gson_weather.Weather;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by GaoCh on 2018/7/23.
 */

public class Utility {
    /**
     * 将返回的JSON数据解析成Weather实体类
     */
    public static Weather handleWeather6Response(String response){
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather6");
            String weatherContent = jsonArray.getJSONObject(0).toString();
            Weather weather= new Gson().fromJson(weatherContent,Weather.class);
            return weather;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int dp2px(Context context,float dpValue){
        float scale=context.getResources().getDisplayMetrics().density;
        return (int)(dpValue*scale+0.5f);
    }


    public static int sp2px(Context context,float spValue){
        float fontScale=context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue*fontScale+0.5f);
    }

    public static boolean saveWeatherToDB(SQLiteDatabase db,String responseText){
        final Weather weather = Utility.handleWeather6Response(responseText);
        if(weather==null||!weather.status.equals("ok")){
            Log.e("GGG","所获取到的responseText无效");
            return false;
        }
        Cursor cursor = db.query(LocalDatabaseHelper.tableName, null, LocalDatabaseHelper.citycode+"=?", new String[] { weather.basic.weatherId}, null, null, null);
        if(cursor.moveToNext()){
            //表示存在，只需要更新
            String sql = "UPDATE "+LocalDatabaseHelper.tableName+" set "+LocalDatabaseHelper.content+"='"+responseText+"' where "+LocalDatabaseHelper.citycode+"='"+weather.basic.weatherId+"';";

            db.execSQL(sql);
            Log.e("GGG","数据库更新");
        }else{
            //不存在，需要添加
            ContentValues value = new ContentValues();
            value.put(LocalDatabaseHelper.citycode,weather.basic.weatherId);
            value.put(LocalDatabaseHelper.content,responseText);
            db.insert(LocalDatabaseHelper.tableName,null,value);
            Log.e("GGG","数据库添加");
        }
        db.close();
        return true;
    }

    public static boolean isDbEmpty(SQLiteDatabase db){
        Cursor cursor = db.query(LocalDatabaseHelper.tableName, null, null, null, null, null, null);
        if(cursor.moveToNext()){
            db.close();
            return false;
        }
        db.close();
        return true;
    }






}
