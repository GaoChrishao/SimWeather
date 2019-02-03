package com.simweather.gaoch;


import android.Manifest;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.DisplayCutout;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;


import com.simweather.gaoch.gson_weather.Weather;
import com.simweather.gaoch.service.AutoUpdateJobServer;
import com.simweather.gaoch.service.AutoUpdateService;
import com.simweather.gaoch.util.Blur;
import com.simweather.gaoch.util.ConstValue;
import com.simweather.gaoch.util.HttpUtil;
import com.simweather.gaoch.util.MyAppCompatActivity;
import com.simweather.gaoch.util.Utility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.simweather.gaoch.util.ConstValue.LOCATIONGPS;

public class WeatherActivity extends MyAppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {


    public DrawerLayout drawerLayout;
    public NavigationView navView;
    public FragmentWeather fragmentWeather;
    private TextView locate;
    private TextView temp;
    public String bg_path;
    public Drawable bgPNG;
    public int primaryColor;
    private final int requestPermissionsCode = 100;//权限请求码
    private LocationManager locationManager;




    public FrameLayout blur_main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Blur.bkg=null;
        primaryColor=0;
        initTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        drawerLayout = findViewById(R.id.drawer_layout);
        locate = findViewById(R.id.nav_header_locate);
        temp = findViewById(R.id.nav_header_temp);
        navView = findViewById(R.id.nav_view);
        navView.setCheckedItem(R.id.nav_weather);
        blur_main=findViewById(R.id.main_fragment);






        String id=getIntent().getStringExtra("id");
        requestPermissons();

        if(id!=null&&!id.isEmpty()){
            //根据搜索后选择的城市来获取城市信息
            Log.d("WeatherActivity",id);
            getRequestWeather(id);
        }else{
            if (!getResponseText().equals("")) {
                //已经保存有天气数据
                showFragmentWeather();
                startService();
            } else {
                //未保存天气数据
                String location=getSharedPreferences(ConstValue.getConfigDataName(),MODE_PRIVATE).getString(ConstValue.sp_location,"");
                if(location.equals("")){
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    fragmentWeather=new FragmentWeather();
                    transaction.replace(R.id.main_fragment, fragmentWeather);
                    transaction.commit();
                }else {
                    //保存有经纬度信息
                    getRequestWeather(location);
                }
            }
        }







        /**
         * 抽屉选择
         */
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                switch (item.getItemId()) {
                    case R.id.nav_weather:
                        if (fragmentWeather==null) {
                            String weatherId;
                            SharedPreferences preferences = getSharedPreferences(ConstValue.getConfigDataName(), MODE_PRIVATE);
                            String responseText = getResponseText();
                            if (responseText != "") {
                                //如果已经有天气缓存，则从天气缓存得到天气
                                showFragmentWeather();
                                navView.setCheckedItem(R.id.nav_weather);
                            } else if ((weatherId = preferences.getString(ConstValue.sp_weatherid, "")) != "") {
                                //如果没有天气缓存，根据weatherId，从服务器获取
                                getRequestWeather(weatherId);
                                navView.setCheckedItem(R.id.nav_weather);
                            } else {
                                //如果没有weatherId,请选择城市
                                transaction.replace(R.id.main_fragment, new FragmentSearch());
                                transaction.commit();
                                navView.setCheckedItem(R.id.nav_city);
                            }
                        }
                        break;
                    case R.id.nav_city:
                        transaction.replace(R.id.main_fragment,new FragmentSearch());
                        transaction.commit();
                        if(fragmentWeather!=null){
                            fragmentWeather.onDestroy();
                            fragmentWeather=null;
                        }
                        navView.setCheckedItem(R.id.nav_city);
                        break;
                    case R.id.nav_theme:
                        transaction.replace(R.id.main_fragment, new FragmentTheme());
                        transaction.commit();
                        if(fragmentWeather!=null){
                            fragmentWeather.onDestroy();
                            fragmentWeather=null;
                        }

                        navView.setCheckedItem(R.id.nav_theme);
                        break;
                    case R.id.nav_config:
                        transaction.replace(R.id.main_fragment, new FragmentConfig());
                        transaction.commit();
                        if(fragmentWeather!=null){
                            fragmentWeather.onDestroy();
                            fragmentWeather=null;
                        }
                        navView.setCheckedItem(R.id.nav_config);
                        break;
                }
                drawerLayout.closeDrawers();
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("WeatherActivity","onResume()");
        if(bgPNG ==null){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS );
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |  View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.TRANSPARENT);
            }
            setBg();
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("WeatherActivity", "onDestroy");
    }

    /**
     * 根据天气id请求城市天气信息
     */
    public void requestWeather(final String weatherId) {
        SharedPreferences preferences = getSharedPreferences(ConstValue.getConfigDataName(), MODE_PRIVATE);
        String key = preferences.getString(ConstValue.sp_key, ConstValue.getKey());
        String weatherUrl="https://free-api.heweather.com/s6/weather?key="+key+"&location="+weatherId;
        
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeather6Response(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            SharedPreferences.Editor editor = getSharedPreferences(ConstValue.getConfigDataName(), MODE_PRIVATE).edit();
                            editor.putString(ConstValue.sp_responseText, responseText);
                            editor.putString(ConstValue.sp_weatherid, weatherId);
                            editor.apply();
                            showFragmentWeather();
                            Log.d("WeatherActivity", "requestWeather()从服务器获取");
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败！", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }


    /**
     * 功能同上，从函数会显示刷新的进度条
     * @param weatherId
     */
    public void getRequestWeather(final String weatherId) {
        SharedPreferences preferences = getSharedPreferences(ConstValue.getConfigDataName(), MODE_PRIVATE);
        String key = preferences.getString(ConstValue.sp_key, ConstValue.getKey());
        String weatherUrl="https://free-api.heweather.com/s6/weather?key="+key+"&location="+weatherId;
        fragmentWeather.swipeRefresh.setRefreshing(true);
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        fragmentWeather.swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeather6Response(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            SharedPreferences.Editor editor = getSharedPreferences(ConstValue.getConfigDataName(), MODE_PRIVATE).edit();
                            editor.putString(ConstValue.sp_responseText, responseText);
                            editor.putString(ConstValue.sp_weatherid, weatherId);
                            editor.apply();
                            Log.d("WeatherActivity", "getRequestWeather()成功从服务器获取天气！");
                            fragmentWeather.showWeatherInfo();
                            fragmentWeather.swipeRefresh.setRefreshing(false);
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败！", Toast.LENGTH_SHORT).show();
                            fragmentWeather.swipeRefresh.setRefreshing(false);
                        }
                    }
                });
            }
        });
    }


    /**
     * 切换FragmentWeather
     */
    public void showFragmentWeather() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        fragmentWeather = new FragmentWeather();
        transaction.replace(R.id.main_fragment, fragmentWeather);
        transaction.commit();
        navView.setCheckedItem(R.id.nav_weather);
    }


    /**
     * 显示抽屉
     */
    public void showDrawer() {
        drawerLayout.openDrawer(GravityCompat.START);
    }

    /**
     * 读取responseText
     */
    public String getResponseText() {
        SharedPreferences preferences = getSharedPreferences(ConstValue.getConfigDataName(), MODE_PRIVATE);
        String responseText = preferences.getString(ConstValue.sp_responseText, "");
        return responseText;
    }


    /**
     * 保存key
     */
    public void saveKey(String key) {
        SharedPreferences.Editor editor = getSharedPreferences(ConstValue.getConfigDataName(), MODE_PRIVATE).edit();
        editor.putString("key", key);
        editor.apply();
        Toast.makeText(this, "重启应用前请确认key为有效的！", Toast.LENGTH_SHORT).show();
    }

    /**
     * 得到自己的key
     */
    public String getKey(){
        SharedPreferences preferences = getSharedPreferences(ConstValue.getConfigDataName(),MODE_PRIVATE);
        return preferences.getString("key","");
    }


    /**
     * 保存是否开启毛玻璃效果
     */
    public void saveIsBlur(boolean isBlur){
        SharedPreferences.Editor editor = getSharedPreferences(ConstValue.getConfigDataName(),MODE_PRIVATE).edit();
        editor.putBoolean(ConstValue.isBlur,isBlur);
        editor.apply();
    }

    /**
     * 保存主题
     */
    public void saveTheme(int color) {
        SharedPreferences.Editor editor = getSharedPreferences(ConstValue.getConfigDataName(), MODE_PRIVATE).edit();
        switch (color) {
            case R.id.config_theme_blue:
                setTheme(R.style.AppTheme_blue);
                editor.putString("theme", "blue");
                editor.apply();
                Toast.makeText(this, "主题更改成功，重启生效", Toast.LENGTH_SHORT).show();
                break;
            case R.id.config_theme_grey:
                setTheme(R.style.AppTheme_grey);
                editor.putString("theme", "grey");
                editor.apply();
                Toast.makeText(this, "主题更改成功，重启生效", Toast.LENGTH_SHORT).show();
                break;
            case R.id.config_theme_red:
                setTheme(R.style.AppTheme_red);
                editor.putString("theme", "red");
                editor.apply();
                Toast.makeText(this, "主题更改成功，重启生效", Toast.LENGTH_SHORT).show();
                break;
            case R.id.config_theme_yellow:
                editor.putString("theme", "yellow");
                editor.apply();
                Toast.makeText(this, "主题更改成功，重启生效", Toast.LENGTH_SHORT).show();
                break;
            case R.id.config_theme_pink:
                editor.putString("theme", "pink");
                editor.apply();
                Toast.makeText(this, "主题更改成功，重启生效", Toast.LENGTH_SHORT).show();
                break;
            case R.id.config_theme_green:
                editor.putString("theme", "green");
                editor.apply();
                Toast.makeText(this, "主题更改成功，重启生效", Toast.LENGTH_SHORT).show();
                break;
            case R.id.config_theme_blue1:
                editor.putString("theme", "blue1");
                editor.apply();
                Toast.makeText(this, "主题更改成功，重启生效", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;

        }
    }

    /**
     * 修改var_head的内容
     */
    public void changeVarHead(String name, String temp) {
        View headerView = navView.getHeaderView(0);
        TextView textview = (TextView) headerView.findViewById(R.id.nav_header_locate);
        textview.setText(name);
        TextView textView1 = headerView.findViewById(R.id.nav_header_temp);
        textView1.setText(temp + "°C");
    }

    /**
     * 修改var_head的颜色
     */
    public void changeVarHeadColor() {
        View headerView = navView.getHeaderView(0);
        if(primaryColor!=0){
            RelativeLayout relativeLayout = headerView.findViewById(R.id.nav_header);
            relativeLayout.setBackgroundColor(primaryColor);
            int[] colors = new int[]{ primaryColor,getResources().getColor(R.color.uncheckedColor)};
            int[][] states = new int[][]{
                    new int[]{ android.R.attr.state_checked},
                    new int[]{-android.R.attr.state_checked}
            };
            ColorStateList csl = new ColorStateList(states,colors);
            navView.setItemIconTintList(csl);
            navView.setItemTextColor(csl);
            Log.e("GGG",primaryColor+"");

        }else{
            Toast.makeText(this, "此图片无法提取到主色调，请换一张背景", Toast.LENGTH_SHORT).show();
            Log.e("GGG","color为0");
        }

    }


    /**
     * 初始主题色
     */
    public void initTheme() {
        SharedPreferences configPref = getSharedPreferences(ConstValue.getConfigDataName(), MODE_PRIVATE);
        String theme = configPref.getString("theme", "blue");
        switch (theme) {
            case "blue":
                this.setTheme(R.style.AppTheme_blue);
                break;
            case "red":
                this.setTheme(R.style.AppTheme_red);
                break;
            case "yellow":
                this.setTheme(R.style.AppTheme_yellow);
                break;
            case "grey":
                this.setTheme(R.style.AppTheme_grey);
                break;
            case "pink":
                this.setTheme(R.style.AppTheme_pink);
                break;
            case "green":
                this.setTheme(R.style.AppTheme_green);
                break;
            case "blue1":
                this.setTheme(R.style.AppTheme_blue1);
                break;
        }
    }


    /**
     * 启动后台服务
     */
    public void startService() {
        SharedPreferences preferences = getSharedPreferences(ConstValue.getConfigDataName(), MODE_PRIVATE);
        boolean is = preferences.getBoolean(ConstValue.isBackGroundService, true);
        int circler_hours=getServiceHours();
        if (is) {
            //Intent intent = new Intent(this, AutoUpdateService.class);
            JobService(circler_hours);
            //stopService(intent);
            //startService(intent);
            //Log.d("WeatherActivity", "后台服务开启！");

        } else {
            // Intent intent = new Intent(this, AutoUpdateService.class);
            // stopService(intent);
            JobScheduler mJobScheduler= (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
            mJobScheduler.cancelAll();  //先取消之前已经在运行的jobscheduler,在创建新的
            Log.d("WeatherActivity", "后台服务未开启！");
        }
    }


    /**
     * 后台服务开关
     */
    public void serviceOpen() {
        SharedPreferences.Editor editor = getSharedPreferences(ConstValue.getConfigDataName(), MODE_PRIVATE).edit();
        editor.putBoolean(ConstValue.isBackGroundService, true);
        editor.apply();
        startService();
        Toast.makeText(this, "开启成功", Toast.LENGTH_SHORT).show();
    }

    public void serviceClose() {

        Intent intent = new Intent(this, AutoUpdateService.class);
        stopService(intent);


        SharedPreferences.Editor editor = getSharedPreferences(ConstValue.getConfigDataName(), MODE_PRIVATE).edit();
        editor.putBoolean(ConstValue.isBackGroundService, false);
        editor.apply();
        Toast.makeText(this, "关闭成功", Toast.LENGTH_SHORT).show();
    }


    /**
     * 设置后台时间间隔更新,在FragmentConfig中被调用
     */
    public void updateServiceHours(int hours) {
        SharedPreferences.Editor editor = getSharedPreferences(ConstValue.getConfigDataName(), MODE_PRIVATE).edit();
        editor.putInt(ConstValue.getUpdateHours(), hours);
        Toast.makeText(this, "时间设置为" + hours + "小时更新一次", Toast.LENGTH_SHORT).show();
        startService();
        editor.apply();
    }

    /**
     * 得到后台更新时间
     */
    public int getServiceHours() {
        SharedPreferences preferences = getSharedPreferences(ConstValue.getConfigDataName(), MODE_PRIVATE);
        int hours = preferences.getInt(ConstValue.getUpdateHours(), ConstValue.getDefaultUpdateHours());
        return hours;
    }


    /**
     * 得到主题
     */
    public String getColor() {
        SharedPreferences configPref = getSharedPreferences(ConstValue.getConfigDataName(), MODE_PRIVATE);
        String theme = configPref.getString("theme", "blue");
        return theme;
    }

    /**
     * 是否开启毛玻璃
     * @return
     */
    public boolean getIsBlur(){
        SharedPreferences configPref = getSharedPreferences(ConstValue.getConfigDataName(), MODE_PRIVATE);
        boolean is = configPref.getBoolean(ConstValue.isBlur, false);
        if(is&&configPref.getString(ConstValue.getIsBackGroundPNG(),"")!=""){
            return is;
        }else{
           return false;
        }

    }

    public Boolean isBackGroundService() {
        SharedPreferences configPref = getSharedPreferences(ConstValue.getConfigDataName(), MODE_PRIVATE);
        Boolean is = configPref.getBoolean(ConstValue.isBackGroundService, true);
        return is;
    }

    /**
     * 自定义背景图片
     */
    public void chooseBg() {
        Intent intent_choose = new Intent(Intent.ACTION_PICK);//Intent.ACTION_GET_CONTENT和是获得最近使用过的图片。
        intent_choose.setType("image/*");//应该是指定数据类型是图片。
        startActivityForResult(intent_choose, 0);
    }


    /**
     *初始背景图片
     */
    public void setBg(){
        SharedPreferences configPref = getSharedPreferences(ConstValue.getConfigDataName(), MODE_PRIVATE);
        final String path = configPref.getString(ConstValue.getIsBackGroundPNG(),"");
        if(path!=""){
            bg_path = path;
            Log.d("GGG","读取到图片地址");
            try{
                bgPNG = getCuteedBkg(bg_path);
                blur_main.setBackground(bgPNG);
                primaryColor = colorFromBitmap( BitmapFactory.decodeFile(path));
            }catch (Exception e){
                e.printStackTrace();
                Toast.makeText(this, "读取背景图片错误！请重启软件后重新选择一张背景图片！", Toast.LENGTH_SHORT).show();
                SharedPreferences.Editor editor = getSharedPreferences(ConstValue.getConfigDataName(),MODE_PRIVATE).edit();
                editor.putString(ConstValue.getIsBackGroundPNG(),"");
                editor.apply();
                return;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.TRANSPARENT);
                window.setNavigationBarColor(Color.TRANSPARENT);
            }
            changeVarHeadColor();

        }
    }

    /**
     * 按照屏幕尺寸剪裁图片
     * @param path
     * @return
     */
    public Drawable getCuteedBkg(String path){
        BitmapDrawable bd=(BitmapDrawable)Drawable.createFromPath(path);
        Bitmap bkg1=bd.getBitmap();

        DisplayMetrics metrics =new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(metrics);

        float width = metrics.widthPixels;
        float height = metrics.heightPixels;
        Log.e("SimWeather","屏幕尺寸:"+width+","+height);

        float prePicWidth=bkg1.getWidth();
        float prePicHeight=bkg1.getHeight();
        Log.e("SimWeather","原始图片尺寸:"+prePicWidth+","+prePicHeight);
        float displayScale=width/height;
        float prePicScale=prePicWidth/prePicHeight;
        Log.e("SimWeather","屏幕宽高比例:"+displayScale+",原始图片宽高比例:"+prePicScale);
        if(displayScale<prePicScale){

            //背景图片宽度过大,按照高度缩放
            float scaleFactor=height/prePicHeight;
            //按照高度缩放
            Log.e("SimWeather","缩放后的图片:"+(int)(prePicWidth*scaleFactor)+","+(int)(prePicHeight*scaleFactor));
            Bitmap bkg_scaled= Bitmap.createScaledBitmap(bkg1,(int)(prePicWidth*scaleFactor),(int)(prePicHeight*scaleFactor), true);
            float pic_width=bkg_scaled.getWidth();
            float pic_height=bkg_scaled.getHeight();
            Bitmap bkg_nedded=Bitmap.createBitmap(bkg_scaled,(((int)pic_width-(int)width)/2),0,(int)width,(int)height);
            return new BitmapDrawable(getResources(),bkg_nedded);


        }else{
            //背景图片宽度过小
            float scaleFactor=width/prePicWidth;
            //按照宽度缩放
            Log.e("SimWeather","缩放后的图片:"+(int)(prePicWidth*scaleFactor)+","+(int)(prePicHeight*scaleFactor));
            Bitmap bkg_scaled= Bitmap.createScaledBitmap(bkg1,(int)(prePicWidth*scaleFactor),(int)(prePicHeight*scaleFactor), true);
            float pic_width=bkg_scaled.getWidth();
            float pic_height=bkg_scaled.getHeight();
            Bitmap bkg_nedded=Bitmap.createBitmap(bkg_scaled,0,((int)pic_height-(int)height)/2,(int)width,(int)height);
            return new BitmapDrawable(getResources(),bkg_nedded);
        }
    }


    /**
     *去除背景图片
     */
    public void cancelBg(){
        SharedPreferences.Editor editor = getSharedPreferences(ConstValue.getConfigDataName(),MODE_PRIVATE).edit();
        editor.putString(ConstValue.getIsBackGroundPNG(),"");
        editor.apply();
        Toast.makeText(this, "重启生效", Toast.LENGTH_SHORT).show();
    }

    /**
     *得到主色调
     */
    private int colorFromBitmap(Bitmap bitmap) {
        final int NUMBER_OF_PALETTE_COLORS = ConstValue.colorRange;
        final Palette palette = Palette.generate(bitmap, NUMBER_OF_PALETTE_COLORS);
        if (palette != null && palette.getVibrantSwatch() != null) {
            //活力色
            return palette.getVibrantSwatch().getRgb();
        }else if(palette!=null&&palette.getMutedSwatch()!=null){
            //柔和色
            return palette.getMutedSwatch().getRgb();
        }
        return 0;
    }

    /**
     *得到背景图品地址
     */
    public String getBg_path(){
        SharedPreferences preferences = getSharedPreferences(ConstValue.getConfigDataName(),MODE_PRIVATE);
        return preferences.getString(ConstValue.getIsBackGroundPNG(),"");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 0:
                if (resultCode == RESULT_OK) {
                    Uri imageUri = data.getData();//图片的相对路径
                    Cursor cursor = getContentResolver().query(imageUri, null, null, null, null);//用ContentProvider查找选中的图片
                    cursor.moveToFirst();
                    final String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));//获取图片的绝对路径
                    Log.d("WeatherActivity", path);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            SharedPreferences.Editor editor = getSharedPreferences(ConstValue.getConfigDataName(),MODE_PRIVATE).edit();
                            editor.putString(ConstValue.getIsBackGroundPNG(),path);
                            editor.apply();
                            Log.d("WeatherActivity","保存图片地址");
                        }
                    }).start();
                    FrameLayout layout = findViewById(R.id.main_fragment);
                    layout.setBackground(Drawable.createFromPath(path));
                    cursor.close();
                    Toast.makeText(this, "重启生效！", Toast.LENGTH_SHORT).show();
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    /**
     * 更新桌面小部件
     */
    public void UpdateWidgrt(){
        SharedPreferences preferences = getSharedPreferences(ConstValue.getConfigDataName(),MODE_PRIVATE);
        String responseText = preferences.getString(ConstValue.sp_responseText,"");
        if(responseText!=""){
            Weather weather = Utility.handleWeather6Response(responseText);
            //更新Widget
            RemoteViews remoteViews = new RemoteViews(getApplicationContext().getPackageName(),R.layout.weather_widget);
            remoteViews.setTextViewText(R.id.widgrt_temp,weather.now.tmp +"°C");
            remoteViews.setTextViewText(R.id.widgrt_qlty,weather.lifestyle.get(7).brf);
            int ico_id = getImageByReflect("w"+weather.now.cond_code);
            if(ico_id!=0){
                remoteViews.setImageViewResource(R.id.widgrt_ico,ico_id);
            }else{
                remoteViews.setViewVisibility(R.id.widgrt_ico,0);
            }
            Log.d("WeatherActivity","更新桌面插件成功！"+weather.now.cond_code);
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


    /**
     * 通过JobScheduler来更新后台服务
     * hours==后台自动更新时长
     */

    public void JobService(int hours){
        JobScheduler mJobScheduler= (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        mJobScheduler.cancelAll();  //先停止之前已经在运行的jobscheduler,在创建新的
        JobInfo.Builder builder=new JobInfo.Builder(10002,new ComponentName(this,AutoUpdateJobServer.class));
        //builder.setMinimumLatency(5000);
        //builder.setOverrideDeadline(6000);
        if(hours<=0)hours=4;
        builder.setPeriodic(1000*60*hours); //20 minutes
        builder.setPersisted(true); //autostart after  reboot
        builder.setRequiresCharging(false);
        builder.setRequiresDeviceIdle(false);


        //JobInfo.NETWORK_TYPE_NONE（无网络时执行，默认）、JobInfo.NETWORK_TYPE_ANY（有网络时执行）、JobInfo.NETWORK_TYPE_UNMETERED（网络无需付费时执行）
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);//run with Internet

        Log.d("JobService()","run----------------------");
        if(mJobScheduler.schedule(builder.build())<=0){
            Log.d("JobService()","wrong----------------------");
        }else{
            Log.d("JobService()","build----------------------");
        }
    }


    /**
     * 检测权限是否开启
     */
    private void requestPermissons() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> mPermissionList = new ArrayList<>();
            for (int i = 0; i < LOCATIONGPS.length; i++) {
                if (ContextCompat.checkSelfPermission(this, LOCATIONGPS[i]) != PackageManager.PERMISSION_GRANTED) {
                    mPermissionList.add(LOCATIONGPS[i]);//添加还未授予的权限
                }
            }
            //申请权限
            if (mPermissionList.size() > 0) {//有权限没有通过，需要申请
                ActivityCompat.requestPermissions(this, LOCATIONGPS, requestPermissionsCode);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode== requestPermissionsCode){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Log.e("GGG","授予定位权限成功");
                testLocation();
            }else{
                Log.e("GGG","授予定位权限失败");
                Toast.makeText(this, "定位权限是为了获取当地天气，请手动给予", Toast.LENGTH_LONG).show();
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.main_fragment, new FragmentSearch());
                transaction.commit();
            }
            if(grantResults[2]==PackageManager.PERMISSION_GRANTED){
                Log.e("GGG","授予读写权限成功");
            }else{
                Log.e("GGG","授予读写权限失败");
                Toast.makeText(this, "读写权限是为了自定义背景的图片读取，请手动给予", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void testLocation() {
        String provider;
        //获取定位服务
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //获取当前可用的位置控制器
        List<String> list = locationManager.getProviders(true);

        if (list.contains(LocationManager.GPS_PROVIDER)) {
            //是否为GPS位置控制器
            provider = LocationManager.GPS_PROVIDER;
        } else if (list.contains(LocationManager.NETWORK_PROVIDER)) {
            //是否为网络位置控制器
            provider = LocationManager.NETWORK_PROVIDER;

        } else {
            Toast.makeText(this, "请检查网络或GPS是否打开",Toast.LENGTH_LONG).show();
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = locationManager.getLastKnownLocation(provider);
        if (location != null) {
            //获取当前位置，这里只用到了经纬度
            String string =location.getLongitude()+","+location.getLatitude();
            Log.e("GGG",string);
            double lon=location.getLongitude();
            if(Math.abs(lon)>0.00001){
                SharedPreferences.Editor editor = getSharedPreferences(ConstValue.getConfigDataName(),MODE_PRIVATE).edit();
                editor.putString(ConstValue.sp_location,string);
                editor.apply();
                if(getResponseText().equals("")){
                    getRequestWeather(string);
                }
            }
        }

        //绑定定位事件，监听位置是否改变
        //第一个参数为控制器类型第二个参数为监听位置变化的时间间隔（单位：毫秒）
        //第三个参数为位置变化的间隔（单位：米）第四个参数为位置监听器
        locationManager.requestLocationUpdates(provider, 2000, 2, new LocationListener(){

            @Override
            public void onLocationChanged(Location location) {
                // 更新当前经纬度
                String string =location.getLongitude()+","+location.getLatitude();
                Log.e("GGG",string);
                double lon=location.getLongitude();
                if(Math.abs(lon)>0.00001){
                    SharedPreferences.Editor editor = getSharedPreferences(ConstValue.getConfigDataName(),MODE_PRIVATE).edit();
                    editor.putString(ConstValue.sp_location,string);
                    editor.apply();
                    if(getResponseText().equals("")){
                        getRequestWeather(string);
                    }
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        });

    }

    public String getLocation(){
        return getSharedPreferences(ConstValue.getConfigDataName(),MODE_PRIVATE).getString(ConstValue.sp_location,"");
    }



}
