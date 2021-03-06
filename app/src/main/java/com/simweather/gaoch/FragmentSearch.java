package com.simweather.gaoch;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.gson.Gson;
import com.simweather.gaoch.gson_city.CityAdapter;
import com.simweather.gaoch.gson_city.CityHasAdapter;
import com.simweather.gaoch.gson_city.CitySearch;
import com.simweather.gaoch.gson_weather.Weather;
import com.simweather.gaoch.util.BlurSingle;
import com.simweather.gaoch.util.ConstValue;
import com.simweather.gaoch.util.HttpUtil;
import com.simweather.gaoch.util.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by GaoCh on 2018/7/23.
 */

public class FragmentSearch extends Fragment {
    private ProgressDialog progressDialog;
    private int hasBlured_bottom1=0,hasBlured_bottom2=0,hasBlured_bottom3;
    private BlurSingle.BlurLayout blur1,blur2,blur3;
    private SearchView searchCities;
    private RecyclerView recyclerView,recyclerViewHas;
    private CityAdapter adapter;
    private CityHasAdapter cityHasAdapter;
    private List<CitySearch.Basic> cityList;
    private List<Weather>weatherList;
    private LinearLayout layoutTitle;
    private Button navButton;
    private boolean hasBlur=false;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search,container,false);
       searchCities=view.findViewById(R.id.fragment_search_sv);
       searchCities.setSubmitButtonEnabled(true);
       //searchCities.onActionViewExpanded();
        layoutTitle=view.findViewById(R.id.fragment_search_title);
        navButton=view.findViewById(R.id.nav_button);

        searchCities.setIconifiedByDefault(false);
       recyclerView=view.findViewById(R.id.fragment_search_rv);
       recyclerViewHas=view.findViewById(R.id.fragment_search_rv_has);


        Utility.setBelowStatusBar(getContext(),layoutTitle,view,15,15);
        return view;
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
        cityList=new ArrayList<CitySearch.Basic>();
        adapter = new CityAdapter(cityList);
        adapter.setOnItemClickListener(new CityAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                showProgressDialog();
                requestWeather(cityList.get(position).id);
            }
        });
        RecyclerView.LayoutManager layoutManager = new StaggeredGridLayoutManager(3,StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);
        searchCities.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(TextUtils.isEmpty(query)){
                    Toast.makeText(getContext(), "请输入要查找的城市名！", Toast.LENGTH_SHORT).show();
                }else{
                    showProgressDialog();
                    searchFromServer(query);
                    searchCities.clearFocus();
                }

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });



        weatherList=new ArrayList<Weather>();
        initWeatherList(weatherList);
        cityHasAdapter = new CityHasAdapter(weatherList);
        cityHasAdapter.setOnItemClickListener(new CityHasAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Toast.makeText(getContext(), "长按删除该城市", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemLongClick(View view, int positio) {
                Weather weather = weatherList.get(positio);
                SQLiteDatabase db = ((WeatherActivity)getActivity()).dbHelper.getWritableDatabase();
                Log.e("GGG",weather.basic.weatherId);
                db.delete(LocalDatabaseHelper.tableName,LocalDatabaseHelper.citycode+"=?",new String[]{weather.basic.weatherId});
                db.close();
                Toast.makeText(getContext(), "成功删除城市:"+weather.basic.cityName, Toast.LENGTH_SHORT).show();
                weatherList.remove(weatherList.get(positio));
                cityHasAdapter.update(weatherList);

            }
        });
        RecyclerView.LayoutManager layoutManager1 = new LinearLayoutManager(getContext());
        recyclerViewHas.setAdapter(cityHasAdapter);
        recyclerViewHas.setLayoutManager(layoutManager1);

        if(weatherList.size()<=0){
            recyclerViewHas.setVisibility(View.GONE);
        }

    }


    /**
     * 显示进度对话框
     */
    private void showProgressDialog(){
    if(progressDialog==null){
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("正在加载...");
        progressDialog.setCanceledOnTouchOutside(false);
    }
    progressDialog.show();
    }

    /**
     * 关闭进度对话框
     */
    private void closeProgressDialog(){
        if(progressDialog!=null){
            progressDialog.dismiss();
        }
    }

    /**
     * 根据输入的城市名查询城市id
     */
    public void searchFromServer(String query){
        String address="https://search.heweather.com/find?location="+query+"&key="+ ConstValue.getKey();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Message msg = new Message();
                msg.what=0;
                handler.sendMessage(msg);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(responseText);
                    JSONArray jsonArray = jsonObject.getJSONArray("HeWeather6");
                    String cityContent = jsonArray.getJSONObject(0).toString();

                    System.out.println(cityContent);
                    CitySearch cities= new Gson().fromJson(cityContent,CitySearch.class);
                    cityList.clear();
                    try{
                        for(int i=0;i<cities.BasicList.size();i++){
                            cityList.add(cities.BasicList.get(i));
                        }
                        Message msg = new Message();
                        msg.what=1;
                        handler.sendMessage(msg);
                    }catch (Exception e){
                        e.printStackTrace();
                        Message msg = new Message();
                        msg.what=4;
                        handler.sendMessage(msg);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!hasBlur){
            setBlur();
        }
    }

    public void requestWeather(final String weatherId) {
        final WeatherActivity activity = (WeatherActivity) getActivity();
        SharedPreferences preferences = activity.getSharedPreferences(ConstValue.getConfigDataName(), MODE_PRIVATE);
        String key = preferences.getString(ConstValue.sp_key, ConstValue.getKey());
        String url="https://free-api.heweather.com/s6/weather?key="+key+"&location="+weatherId;
        HttpUtil.sendOkHttpRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Message msg = new Message();
                msg.what=2;
                handler.sendMessage(msg);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeather6Response(responseText);
                if (weather != null && "ok".equals(weather.status)) {
//                    Log.d("FragmenSearch", "requestWeather()从服务器获取");
//
//
//                    try {
//                        JSONObject jsonObject = null;
//                        jsonObject = new JSONObject(responseText);
//                        JSONArray jsonArray = jsonObject.getJSONArray("HeWeather6");
//                        if(!jsonArray.getJSONObject(0).has("now")){
//                            Message msg = new Message();
//                            msg.what=2;
//                            handler.sendMessage(msg);
//                            return;
//                        }
//
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
                Log.e("Frag",responseText);


                    Utility.saveWeatherToDB(((WeatherActivity)getActivity()).dbHelper.getWritableDatabase(),responseText);
                    Message msg = new Message();
                    msg.what=3;
                    handler.sendMessage(msg);
                } else {
                    Message msg = new Message();
                    msg.what=2;
                    handler.sendMessage(msg);
                }
            }
        });
    }


    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:     //查询到城市
                    adapter.update(cityList);
                    recyclerView.setVisibility(View.VISIBLE);
                    closeProgressDialog();
                    break;
                case 0:         //查询城市失败
                    Toast.makeText(getContext(), "失败！", Toast.LENGTH_SHORT).show();
                    closeProgressDialog();
                    break;
                case 2:
                    closeProgressDialog();
                    Toast.makeText(getContext(), "获取天气信息失败", Toast.LENGTH_SHORT).show();;
                    break;
                case 3:       //获取到天气信息
                    closeProgressDialog();
                    ((WeatherActivity)getActivity()).showFragmentWeather();

                    break;
                case 4:     //未查询到城市
                    closeProgressDialog();
                    Toast.makeText(getContext(), "未查询到相关城市信息！", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    public void setBlur(){
        if(((WeatherActivity) getActivity()).getIsBlur()){
            hasBlur=true;
            final View view_test=((WeatherActivity)getActivity()).blur_main;
            blur1=new BlurSingle.BlurLayout(recyclerView,view_test);
            blur2=new BlurSingle.BlurLayout(searchCities,view_test);
            blur3=new BlurSingle.BlurLayout(recyclerViewHas,view_test);
        }
    }

    public void initWeatherList(List<Weather>weatherList){
        SQLiteDatabase db = ((WeatherActivity)getActivity()).dbHelper.getReadableDatabase();
        Cursor cursor = db.query(LocalDatabaseHelper.tableName, null, null, null, null, null, null);
        if(cursor.moveToFirst()){
            do{
                String responseText=cursor.getString(cursor.getColumnIndex(LocalDatabaseHelper.content));
               weatherList.add(Utility.handleWeather6Response(responseText));
            }while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
    }





}


