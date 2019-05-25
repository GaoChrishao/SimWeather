package com.simweather.gaoch.gson_weather;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

/**
 * Created by GaoCh on 2018/7/23.
 */

@Keep
public class Basic {
    @SerializedName("location")
    public String cityName;

    @SerializedName("cid")
    public String weatherId;


}
