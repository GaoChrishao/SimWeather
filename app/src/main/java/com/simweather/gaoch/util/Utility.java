package com.simweather.gaoch.util;

import android.content.Context;
import android.util.TypedValue;

import com.google.gson.Gson;
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






}
