package com.simweather.gaoch.gson_weather;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by GaoCh on 2018/7/23.
 */

public class Weather {
    public String status;
    public Basic basic;
    public Now now;
    public List<Suggestion> lifestyle;
    @SerializedName("daily_forecast")
    public List<Forecast> forecastList;

    public List<HourFor>hourly;
    public Update update;
}
