package com.simweather.gaoch.gson_city;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.simweather.gaoch.R;
import com.simweather.gaoch.WeatherActivity;
import com.simweather.gaoch.gson_weather.Weather;
import com.simweather.gaoch.util.ConstValue;
import com.simweather.gaoch.util.HttpUtil;
import com.simweather.gaoch.util.Utility;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static android.content.Context.MODE_PRIVATE;

public class CityAdapter extends RecyclerView.Adapter<CityAdapter.ViewHolder>implements View.OnClickListener {
    private List<CitySearch.Basic> citySearchList=null;
    private OnItemClickListener mOnItemClickListener = null;
    public CityAdapter(List<CitySearch.Basic>cityList){
        this.citySearchList=cityList;
    }

    //define interface
    public static interface OnItemClickListener {
        void onItemClick(View view , int position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.city_item,parent,false);

        ViewHolder vh=new ViewHolder(itemView);
        itemView.setOnClickListener(this);

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.cityName.setText(citySearchList.get(position).cityName);
//        holder.cityName.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(v.getContext(),WeatherActivity.class);
//                intent.putExtra("id",citySearchList.get(position).id);
//                v.getContext().startActivity(intent);
//                Toast.makeText(v.getContext(), citySearchList.get(position).id, Toast.LENGTH_SHORT).show();
//            }
//        });

        //将position保存在itemView的Tag中，以便点击时进行获取
        holder.itemView.setTag(position);
    }

    @Override
    public int getItemCount() {
        if(citySearchList!=null){
            return citySearchList.size();
        }else{
            return 0;
        }

    }

    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(v,(int)v.getTag());
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView cityName;
        public ViewHolder(View itemView) {
            super(itemView);
            cityName=itemView.findViewById(R.id.search_city_name);

        }
    }
    public void update(List<CitySearch.Basic>cityList){
        this.citySearchList=cityList;
        notifyDataSetChanged();
    }


}

