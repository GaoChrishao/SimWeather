package com.simweather.gaoch.gson_weather;
import com.google.gson.annotations.SerializedName;

/**
 * Created by GaoCh on 2018/7/23.
 */

public class Forecast {
    public String date;    //预报日期
    public String cond_code_d;      //白天天气状况代码
    public String cond_code_n;      //晚间天气状况代码
    public String cond_txt_d;       //白天天气状况描述
    public String cond_txt_n;  //晚间天气状况描述
    public String tmp_max;
    public String tmp_min;

    public String hum;      //相对湿度
    public String mr;
    public String ms;
    public String pcpn;     //降水量
    public String sr;
    public String ss;
    public String uv_index;     //紫外线强度指数
    public String vis;
    public String wind_deg;     //风向360角度
    public String wind_dir;
    public String wind_sc;
    public String wind_spd;


}
