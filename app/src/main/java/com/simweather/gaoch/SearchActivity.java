package com.simweather.gaoch;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.simweather.gaoch.gson_city.CityAdapter;
import com.simweather.gaoch.gson_city.CitySearch;
import com.simweather.gaoch.util.ConstValue;
import com.simweather.gaoch.util.HttpUtil;
import com.simweather.gaoch.util.MyAppCompatActivity;
import com.simweather.gaoch.util.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class SearchActivity extends MyAppCompatActivity {

    private Button search;
    private EditText input;
    private Button back;
    CityAdapter adapter;
    private List<CitySearch.Basic> cityList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        addDestroyActivity(this,"Search");
        initTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        search = findViewById(R.id.search_ok);
        input = findViewById(R.id.search_input);
        back = findViewById(R.id.search_back);
        RecyclerView recyclerView = findViewById(R.id.search_rv);

        adapter = new CityAdapter(cityList);
        RecyclerView.LayoutManager layoutManager = new StaggeredGridLayoutManager(3,StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s=input.getText().toString();
                if(!s.isEmpty()&&s!="")
                searchFromServer("https://search.heweather.com/find?location="+s+"&key="+ ConstValue.getKey());
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }





    /**
     * 初始主题色
     */
    public void initTheme() {
        SharedPreferences configPref = getSharedPreferences(ConstValue.getConfigDataName(), MODE_PRIVATE);
        String theme = configPref.getString("theme", "blue").toString();
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
     * 根据输入的城市名查询城市id
     */
    public void searchFromServer(String address){
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //closeProgressDialog();
                        Toast.makeText(getApplicationContext(), "加载失败!", Toast.LENGTH_SHORT).show();
                    }
                });
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
                    for(int i=0;i<cities.BasicList.size();i++){
                        cityList.add(cities.BasicList.get(i));
                    }
                    Message msg = new Message();
                    msg.what=1;
                    handler.sendMessage(msg);

                    //System.out.println(cities.BasicList.get(0).cityName);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //closeProgressDialog();
            }
        });
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    adapter.update(cityList);
                    break;
            }
        }
    };

}
