package com.simweather.gaoch;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.RemoteViews;


import com.simweather.gaoch.service.AutoUpdateService;

/**
 * Implementation of App Widget functionality.
 */
public class WeatherWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int N = appWidgetIds.length;
        Log.d("WeatherWidget:","onUpdate()");
        for (int i=0; i<N; i++) {
            int appWidgetId = appWidgetIds[i];

            Intent intent = new Intent(context, WeatherActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.weather_widget);
            views.setOnClickPendingIntent(R.id.widgrt_ico, pendingIntent);
            views.setOnClickPendingIntent(R.id.widgrt_qlty, pendingIntent);
            views.setOnClickPendingIntent(R.id.widgrt_temp, pendingIntent);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }

    }

    @Override
    public void onEnabled(Context context) {

    }

    @Override
    public void onDisabled(Context context) {
    }

}

