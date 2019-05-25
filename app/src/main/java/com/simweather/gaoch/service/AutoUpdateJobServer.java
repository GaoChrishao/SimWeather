package com.simweather.gaoch.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import androidx.core.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.simweather.gaoch.LocalDatabaseHelper;
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

public class AutoUpdateJobServer extends JobService {
    private LocalDatabaseHelper dbHelper;
    private Weather weather;
    @Override
    public boolean onStartJob(JobParameters params) {
        // 返回true，表示该工作耗时，同时工作处理完成后需要调用onStopJob销毁（jobFinished）
        // 返回false，任务运行不需要很长时间，到return时已完成任务处理
        //mJobHandler.sendMessage(Message.obtain(mJobHandler, 1, params));
        Log.d("JobService()","start-----");
        updateWeather();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        // 有且仅有onStartJob返回值为true时，才会调用onStopJob来销毁job
        // 返回false来销毁这个工作
        return false;
    }

    private Handler mJobHandler = new Handler(new Handler.Callback() {
        // 在Handler中，需要实现handleMessage(Message msg)方法来处理任务逻辑。
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    Toast.makeText(getApplicationContext(), "JobService task running", Toast.LENGTH_SHORT).show();
                    updateWeather();
                    break;
            }
            // 调用jobFinished
            jobFinished((JobParameters) msg.obj, true);
            return true;
        }
    });

    /**
     * 更新天气信息
     */
    public void updateWeather(){
        if(dbHelper==null){
            dbHelper =new LocalDatabaseHelper(this,ConstValue.LocalDatabaseName,null,LocalDatabaseHelper.NEW_VERSION);
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.query(LocalDatabaseHelper.tableName, null, null, null, null, null, null);
            if(cursor.moveToNext()){
                weather=Utility.handleWeather6Response(cursor.getString(cursor.getColumnIndex(LocalDatabaseHelper.content)));
                db.close();
            }
        }
        SharedPreferences prefs = getSharedPreferences(ConstValue.getConfigDataName(),MODE_PRIVATE);
        String weatherId =weather.basic.weatherId;
        if(weatherId!=""&&weatherId!=null){
            String key = prefs.getString(ConstValue.sp_key,"");
            if(key.equals("")||key==null){
                key = ConstValue.getKey();
            }
            String weatherUrl="https://free-api.heweather.com/s6/weather?key="+key+"&location="+weatherId;
            HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d("JobService()","更新天气信息失败！");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final String responseText = response.body().string();
                    final Weather weather = Utility.handleWeather6Response(responseText);
                    if(weather!=null&&"ok".equals(weather.status)){
                        Utility.saveWeatherToDB(dbHelper.getWritableDatabase(),responseText);
                        Log.d("JobService()","更新天气信息成功！"+weather.basic.cityName);
                        UpdateWidgrt(weather);
                        showNotif(weather);
                    }else{
                        Log.d("JobService()","更新天气信息失败！");
                    }
                }
            });
        }
    }



    /**
     * 显示通知
     */
    public void showNotif(Weather weather){
        NotificationManager notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if(android.os.Build.VERSION.SDK_INT>=26){
            //ChannelId为"1",ChannelName为"Channel1"
            NotificationChannel channel = new NotificationChannel("天气预报",
                    "WeatherNotify", NotificationManager.IMPORTANCE_DEFAULT);
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
                    .setVisibility(Notification.VISIBILITY_PUBLIC)
                    .setNumber(3); //久按桌面图标时允许的此条通知的数量

            notifyManager.notify(notificationId, builder.build());


        }else{
            //获取NotificationManager实例
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
                    .setVisibility(Notification.VISIBILITY_PUBLIC)
                    ;
            notifyManager.notify(1, builder.build());
            Log.d("JobService()","发送notif成功！");
        }




    }


    /**
     * 更新桌面小部件
     */
    public void UpdateWidgrt(Weather weather){
        if(weather!=null){
            //更新Widget
            RemoteViews remoteViews = new RemoteViews(getApplicationContext().getPackageName(),R.layout.weather_widget);
            remoteViews.setTextViewText(R.id.widgrt_temp,weather.now.tmp +"°C");
           // if(weather.aqi.city.qlty.length()>1){
                //if(weather.aqi.city.qlty.equals("轻度污染"))remoteViews.setTextViewText(R.id.widgrt_qlty,"轻");
                //else if(weather.aqi.city.qlty.equals("重度污染"))remoteViews.setTextViewText(R.id.widgrt_qlty,"重");
                //else remoteViews.setTextViewText(R.id.widgrt_qlty," ");
            //}else{
               // remoteViews.setTextViewText(R.id.widgrt_qlty,weather.aqi.city.qlty);
           // }
            int ico_id = getImageByReflect("w"+weather.now.cond_txt);

            if(ico_id!=0){
                remoteViews.setImageViewResource(R.id.widgrt_ico,ico_id);
            }else{
                remoteViews.setViewVisibility(R.id.widgrt_ico,0);
            }
          //  Log.d("JobService()","更新桌面插件成功！"+weather.now.more.png_id);
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
