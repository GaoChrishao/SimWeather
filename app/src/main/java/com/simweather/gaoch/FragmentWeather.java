package com.simweather.gaoch;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Display;
import android.view.DisplayCutout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.simweather.gaoch.MyView.LineHourlyView;
import com.simweather.gaoch.MyView.LineView;
import com.simweather.gaoch.gson_weather.Forecast;
import com.simweather.gaoch.gson_weather.Weather;
import com.simweather.gaoch.util.Blur;
import com.simweather.gaoch.util.ConstValue;
import com.simweather.gaoch.util.Utility;

import java.util.ArrayList;

/**
 * Created by GaoCh on 2018/7/24.
 */

public class FragmentWeather extends Fragment {
    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInforText;
    private LinearLayout forecastLayout,forecastLayout_des;
    private TextView cloudText,windText,humText,flText;  //相关信息
    private TextView comfortText,carWashText,drsgText,uvText,travText,fluText,airText,sportText;      //lifestyle
    public SwipeRefreshLayout swipeRefresh;
    private Button navButton;
    private int hasBlured_top1=0,hasBlured_top2=0,hasBlured_top3=0,hasBlured_top4=0;
    private LineView lineView_max;
    private LineHourlyView mylineHourlyView;;

    private  LinearLayout layout_suggestion, layout_forecast, layout_aqi,layout_forecast_paint,layout_hourly;
//    private ScrollView layoutBG;



    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weather,container,false);
        weatherLayout = view.findViewById(R.id.weather_layout);
        titleCity = view.findViewById(R.id.title_city);
        titleUpdateTime = view.findViewById(R.id.title_update_time);
        degreeText = view.findViewById(R.id.degree_text);
        weatherInforText = view.findViewById(R.id.weather_infor_text);
        forecastLayout = view.findViewById(R.id.forecast_layout);
        forecastLayout_des=view.findViewById(R.id.forecast_layout_des);
        mylineHourlyView =view.findViewById(R.id.forecast_paint_hourly);
        layout_hourly=view.findViewById(R.id.hourly_forecast);

        //相关信息
        flText = view.findViewById(R.id.fl_text);
        cloudText = view.findViewById(R.id.cloud_text);
        humText=view.findViewById(R.id.hum_text);
        windText=view.findViewById(R.id.wind_text);

        //lifestyle
        comfortText = view.findViewById(R.id.lifestyle_comf);
        carWashText = view.findViewById(R.id.lifestyle_cw);
        drsgText = view.findViewById(R.id.lifestyle_drsg);
        uvText=view.findViewById(R.id.lifestyle_uv);
        travText=view.findViewById(R.id.lifestyle_trav);
        fluText=view.findViewById(R.id.lifestyle_flu);
        airText=view.findViewById(R.id.lifestyle_air);
        sportText=view.findViewById(R.id.lifestyle_sport);


        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeColors(getResources().getColor(R.color.colorAccent));
        navButton = view.findViewById(R.id.nav_button);

        lineView_max=view.findViewById(R.id.forecast_paint_week);
        layout_suggestion = view.findViewById(R.id.layout_sugesstion);
        layout_forecast = view.findViewById(R.id.forecast);
        layout_aqi =view.findViewById(R.id.aqi_ll);
        layout_forecast_paint=view.findViewById(R.id.forecast_paint);


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("FragmentWeather","OnResume()");
        showWeatherInfo();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WeatherActivity activity = (WeatherActivity) getActivity();
                activity.showDrawer();
            }
        });
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                WeatherActivity weatherActivity = (WeatherActivity) getActivity();
                String location=weatherActivity.getLocation();
                if(!location.equals("")){
                    weatherActivity.getRequestWeather(location);
                }else{
                    weatherActivity.getRequestWeather(Utility.handleWeather6Response(weatherActivity.getResponseText()).basic.weatherId);
                }

                Log.d("FragmentWeather","Refresh");
            }
        });
        Log.d("FragmentWeather","OnCreate()");


        /**
         * 毛玻璃效果
         */
        setBlur();



    }


    /**
     * 处理并展示weather实体类中的数据
     */

    public void showWeatherInfo(){
        String responseText = ((WeatherActivity)getActivity()).getResponseText();
        System.out.println(responseText);
        if(responseText!=""){

            Weather weather = Utility.handleWeather6Response(responseText);
            String cityName = weather.basic.cityName;
            String updateTime = weather.update.loc.split(" ")[1];
            String degree = weather.now.tmp +"°C";
            String weatherInfor =weather.now.cond_txt;


            titleCity.setText(cityName);
            titleUpdateTime.setText(updateTime);
            degreeText.setText(degree);
            weatherInforText.setText(weatherInfor);
            forecastLayout.removeAllViews();
            ArrayList<Integer> list_max = new ArrayList<Integer>();
            for(Forecast forecast:weather.forecastList){
                View view = LayoutInflater.from(getContext()).inflate(R.layout.forecast_item,forecastLayout,false);
                TextView dateText = view.findViewById(R.id.date_text);
                TextView inforText = view.findViewById(R.id.infor_text);
                TextView maxText = view.findViewById(R.id.max_text);
                TextView minText = view.findViewById(R.id.min_text);
                String date[] = forecast.date.split("-");
                String date_month = date[date.length-2];
                String date_day = date[date.length-1];
                date_month = date_month.replaceFirst("^0*", "");
                date_day = date_day.replaceFirst("^0*","");
                dateText.setText(date_month+"月"+date_day+"日");
                inforText.setText(forecast.cond_txt_d);
                Log.e("FragmentWeahet",forecast.cond_txt_d);
                maxText.setText(forecast.tmp_max);
                minText.setText(forecast.tmp_min);
                forecastLayout.addView(view);
            }
            if(weather.forecastList.size()>3){
                lineView_max.addDots(weather.forecastList);
                lineView_max.invalidate();
                forecastLayout_des.setVisibility(View.GONE);
                forecastLayout.setVisibility(View.GONE);
            }else{
                lineView_max.setVisibility(View.GONE);
                forecastLayout.setVisibility(View.VISIBLE);
            }

            if(((WeatherActivity)getActivity()).getKey()==""){
                mylineHourlyView.addDots(weather.hourly);
                mylineHourlyView.invalidate();
            }else{
                mylineHourlyView.setVisibility(View.GONE);
                layout_hourly.setVisibility(View.GONE);

            }

            //相关信息
            windText.setText(weather.now.wind_sc);
            humText.setText(weather.now.hum);
            flText.setText(weather.now.fl);
            cloudText.setText(weather.now.cloud);

            //lifestyle
            comfortText.setText(weather.lifestyle.get(0).brf);
            carWashText.setText(weather.lifestyle.get(6).brf);
            drsgText.setText(weather.lifestyle.get(1).brf);
            uvText.setText(weather.lifestyle.get(5).brf);
            travText.setText(weather.lifestyle.get(4).brf);
            fluText.setText(weather.lifestyle.get(2).brf);
            airText.setText(weather.lifestyle.get(7).brf);
            sportText.setText(weather.lifestyle.get(3).brf);

            weatherLayout.setVisibility(View.VISIBLE);
            WeatherActivity activity = (WeatherActivity) getActivity();
            activity.changeVarHead(weather.basic.cityName,weather.now.tmp);
            activity.UpdateWidgrt();
            Log.d("FragmentWeather","showWeatherInfor从SharedPrference");
        }

    }

    public void setBlur(){
        final View view_test=((WeatherActivity)getActivity()).blur_main;
        if(((WeatherActivity) getActivity()).getIsBlur()){
            layout_forecast.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    int []location=new int[2];
                    layout_forecast.getLocationInWindow(location);
                    if(location[1]!=hasBlured_top1){
                        Blur.blur(view_test,layout_forecast,ConstValue.radius,ConstValue.scaleFactor,ConstValue.RoundCorner);
                        hasBlured_top1=location[1];
                    }

                    return true;
                }
            });
            layout_suggestion.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    int []location=new int[2];
                    layout_suggestion.getLocationInWindow(location);
                    if(location[1]!=hasBlured_top2){
                        Blur.blur(view_test,layout_suggestion,ConstValue.radius,ConstValue.scaleFactor,ConstValue.RoundCorner);
                        hasBlured_top2=location[1];
                    }

                    return true;
                }
            });
            layout_aqi.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    int []location=new int[2];
                    layout_aqi.getLocationInWindow(location);
                    if(location[1]!=hasBlured_top3){
                        Blur.blur(view_test,layout_aqi,ConstValue.radius,ConstValue.scaleFactor,ConstValue.RoundCorner);
                        hasBlured_top3=location[1];
                    }
                    return true;
                }
            });

            layout_hourly.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    int []location=new int[2];
                    layout_hourly.getLocationInWindow(location);
                    if(location[1]!=hasBlured_top4){
                        Blur.blur(view_test,layout_hourly,ConstValue.radius,ConstValue.scaleFactor,ConstValue.RoundCorner);
                        hasBlured_top4=location[1];
                    }
                    return true;
                }
            });


        }
    }












}
