package com.simweather.gaoch.gson_weather;

import com.google.gson.annotations.SerializedName;

/**
 * Created by GaoCh on 2018/7/23.
 */

public class Basic {
    @SerializedName("location")
    public String cityName;

    @SerializedName("cid")
    public String weatherId;


}
