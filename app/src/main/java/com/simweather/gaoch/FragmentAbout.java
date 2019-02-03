package com.simweather.gaoch;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.LinearLayout;
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

public class FragmentAbout extends Fragment {
    private int hasBlured_bottom1=0,hasBlured_bottom2=0;
    private LinearLayout layout_1,layout_2;
    private Button navButton;





    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about,container,false);
        layout_1=view.findViewById(R.id.about_li_1);
        layout_2=view.findViewById(R.id.about_li_2);
        navButton=view.findViewById(R.id.nav_button);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        layout_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse(ConstValue.githubUrl);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WeatherActivity activity = (WeatherActivity) getActivity();
                activity.showDrawer();
            }
        });
        setBlur();


    }



    public void setBlur(){
        final View view_test=((WeatherActivity)getActivity()).blur_main;
        if(((WeatherActivity) getActivity()).getIsBlur()){
            layout_1.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    int location=layout_1.getHeight();
                    if(location!=hasBlured_bottom1){
                        Blur.blur(view_test,layout_1,ConstValue.radius,ConstValue.scaleFactor,ConstValue.RoundCorner);
                        hasBlured_bottom1=location;
                    }

                    return true;
                }
            });
            layout_2.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    int location=layout_2.getHeight();
                    if(location!=hasBlured_bottom2){
                        Blur.blur(view_test,layout_2,ConstValue.radius,ConstValue.scaleFactor,ConstValue.RoundCorner);
                       hasBlured_bottom2=location;
                    }

                    return true;
                }
            });


        }
    }





}


