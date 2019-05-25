package com.simweather.gaoch.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.IBinder;
import android.os.SystemClock;
import androidx.core.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.simweather.gaoch.R;
import com.simweather.gaoch.WeatherActivity;
import com.simweather.gaoch.WeatherWidget;
import com.simweather.gaoch.gson_weather.Weather;
import com.simweather.gaoch.util.ConstValue;
import com.simweather.gaoch.util.HttpUtil;
import com.simweather.gaoch.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;



public class AutoUpdateService extends Service {
    private int times=0;
    private PendingIntent pi= null;
    private Weather weather;
    public AutoUpdateService() {
        times=0;
    }

    @Override
    public void onCreate() {
        times=0;
        Log.d("AutoUpdateService","onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour = 1*60*60*1000;  //这是一小时的毫秒数

        SharedPreferences preferences = getSharedPreferences(ConstValue.getConfigDataName(),MODE_MULTI_PROCESS);
        int hours = preferences.getInt(ConstValue.getUpdateHours(),1);

        Log.d("AutoUpdateServiceSC","下次更新时间："+hours+"h后");
        long triggerAtTime;
        triggerAtTime = SystemClock.elapsedRealtime()+anHour*hours;
        Intent i = new Intent(this,AutoUpdateService.class);
        if(pi==null) pi = PendingIntent.getService(this,0,i,0);
        //manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi);
        times++;
        updateWeather();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent(this,AutoUpdateService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pi);
        Log.d("AutoUpdateService","onDestroy,服务停止成功！");
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    /**
     * 更新天气信息
     */
    public void updateWeather(){
        SharedPreferences prefs = getSharedPreferences(ConstValue.getConfigDataName(),MODE_PRIVATE);
        String weatherId = prefs.getString("weatherId","");
        if(weatherId!=""&&weatherId!=null){
            String key = prefs.getString("key","");
            if(key.equals("")||key==null){
                key = ConstValue.getKey();
            }
            String weatherUrl = "http://guolin.tech/api/weather?cityid="+weatherId+"&key="+key;
            HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("AutoUpdateService","更新天气信息失败！");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final String responseText = response.body().string();
                    final Weather weather = Utility.handleWeather6Response(responseText);
                    if(weather!=null&&"ok".equals(weather.status)){
                        SharedPreferences.Editor editor = getSharedPreferences(ConstValue.getConfigDataName(),MODE_PRIVATE).edit();
                        editor.putString("responseText",responseText);
                        editor.apply();
                        Log.d("AutoUpdateService","更新天气信息成功！"+weather.basic.cityName);
                        if(times>1)showNotif(weather);
                        UpdateWidgrt();

                    }else{
                        Log.e("AutoUpdateService","更新天气信息失败！");
                    }
                }
            });
        }
    }



    /**
     * 显示通知
     */
    public void showNotif(Weather weather){

        if(android.os.Build.VERSION.SDK_INT>=26){

            NotificationManager notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            //ChannelId为"1",ChannelName为"Channel1"
            NotificationChannel channel = new NotificationChannel("1",
                    "Channel1", NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableLights(true); //是否在桌面icon右上角展示小红点
            channel.setLightColor(Color.GREEN); //小红点颜色
            channel.setShowBadge(true); //是否在久按桌面图标时显示此渠道的通知
            notifyManager.createNotificationChannel(channel);
            Intent intent = new Intent(this,WeatherActivity.class);
            PendingIntent pi = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);

            int notificationId = 0x1234;
            Notification.Builder builder = new Notification.Builder(this,"1"); //与channelId对应
            //icon title text必须包含，不然影响桌面图标小红点的展示
            builder.setSmallIcon(R.drawable.weather)
                    .setContentTitle(weather.basic.cityName)
                    .setContentText(weather.now.tmp +"°C  "+weather.now.cond_txt +"    "+weather.update.loc.split(" ")[1])
                    .setAutoCancel(true)
                    .setContentIntent(pi)
                    .setNumber(3); //久按桌面图标时允许的此条通知的数量
            notifyManager.notify(notificationId, builder.build());


        }else{
            //获取NotificationManager实例
            NotificationManager notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            Intent intent = new Intent(this,WeatherActivity.class);
            PendingIntent pi = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setContentTitle(weather.basic.cityName)
                    .setSmallIcon(R.drawable.weather)
                    .setContentText(weather.now.tmp +"°C  "+weather.now.cond_txt +"    "+weather.update.loc.split(" ")[1])
                    //调用系统默认响铃,设置此属性后setSound()会无效
                    .setDefaults(Notification.DEFAULT_SOUND)
                    .setAutoCancel(true)
                    .setContentIntent(pi)
                    ;

            notifyManager.notify(1, builder.build());
            Log.d("AutoUpdateActivity","发送notif成功！");
        }



    }


    /**
     * 更新桌面小部件
     */
    public void UpdateWidgrt(){
        SharedPreferences preferences = getSharedPreferences(ConstValue.getConfigDataName(),MODE_PRIVATE);
        String responseText = preferences.getString("responseText","");
        if(responseText!=""){
            Weather weather = Utility.handleWeather6Response(responseText);
            //更新Widget
            RemoteViews remoteViews = new RemoteViews(getApplicationContext().getPackageName(),R.layout.weather_widget);
            remoteViews.setTextViewText(R.id.widgrt_temp,weather.now.tmp +"°C");
//            if(weather.aqi.city.qlty.length()>1){
//                if(weather.aqi.city.qlty.equals("轻度污染"))remoteViews.setTextViewText(R.id.widgrt_qlty,"轻");
//                else if(weather.aqi.city.qlty.equals("重度污染"))remoteViews.setTextViewText(R.id.widgrt_qlty,"重");
//                else remoteViews.setTextViewText(R.id.widgrt_qlty," ");
//            }else{
//                remoteViews.setTextViewText(R.id.widgrt_qlty,weather.aqi.city.qlty);
//            }
            int ico_id = getImageByReflect("w"+weather.now.cond_code);
            if(ico_id!=0){
                remoteViews.setImageViewResource(R.id.widgrt_ico,ico_id);
            }else{
                remoteViews.setViewVisibility(R.id.widgrt_ico,0);
            }
            Log.d("AutoUpdateService","更新桌面插件成功！"+weather.now.cond_code);
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
            appWidgetManager.updateAppWidget(new ComponentName(getApplicationContext(), WeatherWidget.class),remoteViews);
        }
    }

    private int getImageByReflect(String imageName) {
        int rID = 0;
        try {
            //Field field = Class.forName("com.simweather.R$drawable").getField( imageName);
            //rID = field.getInt(field);
            //第二种方法
            //Class<com.cntomorrow.magicmirror.R.drawable> cls = R.drawable.class;
            //rID = cls.getDeclaredField(imageName).getInt(null);
            //第三种方法
            rID=this.getResources().getIdentifier(imageName, "drawable" ,"com.simweather.gaoch");

        } catch (Exception e) {

        }
        return rID;
    }




}
