package com.simweather.gaoch;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.simweather.gaoch.MyView.PageIndicator;
import com.simweather.gaoch.util.Utility;

import java.util.ArrayList;
import java.util.List;


public class FragmentWeathers extends Fragment {
    private List<FragmentWeather> fragmentWeatherList;
    private ViewPager viewPager;
    private Button navButton;
    private FragAdapter adapter;
    private ProgressBar progressBar;
    private LinearLayout dots;



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
        fragmentWeatherList=new ArrayList<FragmentWeather>();
        SQLiteDatabase db = ((WeatherActivity)getActivity()).dbHelper.getReadableDatabase();
        Cursor cursor = db.query(LocalDatabaseHelper.tableName, null, null, null, null, null, null);
        if(cursor.moveToFirst()){
            do{
                String responseText=cursor.getString(cursor.getColumnIndex(LocalDatabaseHelper.content));
                //Log.e("GGG","发送weather");
                Bundle bundle = new Bundle();
                bundle.putString("weather",responseText);
                FragmentWeather fragmentWeather = new FragmentWeather();
                fragmentWeather.setArguments(bundle);
                fragmentWeatherList.add(fragmentWeather);

            }while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        adapter= new FragAdapter(getChildFragmentManager(), fragmentWeatherList);
        //设定适配器
        viewPager.setAdapter(adapter);
        Log.e("GGG","FragmentWeathers的onActivityCreate");
        if(fragmentWeatherList.size()>0){
            progressBar.setVisibility(View.GONE);
        }
        if(fragmentWeatherList.size()>1){
            viewPager.addOnPageChangeListener(new PageIndicator(getContext(),dots,fragmentWeatherList.size()));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.e("GGG","FragmentWeathers的onCreateView");
        View view =inflater.inflate(R.layout.fragment_fragment_weathers, container, false);
        viewPager = view.findViewById(R.id.viewPager);
        navButton = view.findViewById(R.id.nav_button);
        progressBar=view.findViewById(R.id.fragment_weathers_pb);
        dots=view.findViewById(R.id.dot_horizontal);


        Utility.setBelowStatusBar(getContext(),view.findViewById(R.id.fragment_weathers),view,0,0);



        return view;
    }


}
