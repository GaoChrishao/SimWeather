package com.simweather.gaoch.gson_weather;

import com.google.gson.annotations.SerializedName;

/**
 * Created by GaoCh on 2018/7/23.
 */

public class Now {
    public String tmp;       //温度，默认单位：摄氏度
    public String cloud;        //云量
    public String cond_txt;     //实况天气状况代码

    public String cond_code;     //实况天气状况代码

    public String wind_deg;     //风向360度

    public String wind_dir; //风向

    public String wind_sc; //风力
    public String wind_spd; //风速
    public String vis;   //能见度，默认单位：公里
    public String hum;      //相对湿度
    public String pcpn;     //降水量
    public String fl;       //体感温度，默认单位：摄氏度
    public String pres;     //大气压强



}
