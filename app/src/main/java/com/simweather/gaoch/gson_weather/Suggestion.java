package com.simweather.gaoch.gson_weather;

import androidx.annotation.Keep;

/**
 * Created by GaoCh on 2018/7/23.
 */

@Keep
public class Suggestion {
   public String type;
   public String brf;
   public String txt;
   public Suggestion(){
      brf="无";
      txt="无";
      type="无";
   }
}
