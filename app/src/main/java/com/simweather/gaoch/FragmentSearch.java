package com.simweather.gaoch;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.SearchView;
import android.view.ViewTreeObserver;
import android.widget.Toast;
import com.google.gson.Gson;
import com.simweather.gaoch.gson_city.CityAdapter;
import com.simweather.gaoch.gson_city.CitySearch;
import com.simweather.gaoch.gson_weather.Weather;
import com.simweather.gaoch.util.Blur;
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
    private int hasBlured_bottom1=0,hasBlured_bottom2=0;
    private SearchView searchCities;
    private RecyclerView recyclerView;
    CityAdapter adapter;
    private List<CitySearch.Basic> cityList;




    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search,container,false);
       searchCities=view.findViewById(R.id.fragment_search_sv);
       searchCities.setSubmitButtonEnabled(true);
       searchCities.onActionViewExpanded();
       recyclerView=view.findViewById(R.id.fragment_search_rv);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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
        setBlur();

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
                    SharedPreferences.Editor editor = activity.getSharedPreferences(ConstValue.getConfigDataName(), MODE_PRIVATE).edit();
                    editor.putString(ConstValue.sp_responseText, responseText);
                    editor.putString(ConstValue.sp_weatherid, weatherId);
                    editor.apply();
                    Log.d("FragmenSearch", "requestWeather()从服务器获取");
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
        final View view_test=((WeatherActivity)getActivity()).blur_main;
        if(((WeatherActivity) getActivity()).getIsBlur()){
            recyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    int location=recyclerView.getHeight();
                    if(location!=hasBlured_bottom1){
                        Blur.blur(view_test,recyclerView,ConstValue.radius,ConstValue.scaleFactor,ConstValue.RoundCorner);
                        hasBlured_bottom1=location;
                    }

                    return true;
                }
            });
            searchCities.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    int location=searchCities.getHeight();
                    if(location!=hasBlured_bottom2){
                        Blur.blur(view_test,searchCities,ConstValue.radius,ConstValue.scaleFactor,ConstValue.RoundCorner);
                       hasBlured_bottom2=location;
                    }

                    return true;
                }
            });


        }
    }





}


