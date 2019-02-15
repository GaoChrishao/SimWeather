package com.simweather.gaoch.gson_city;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.simweather.gaoch.R;
import com.simweather.gaoch.gson_weather.Weather;

import java.util.List;

public class CityHasAdapter extends RecyclerView.Adapter<CityHasAdapter.ViewHolder>{
    private List<Weather> weatherList =null;
    private OnItemClickListener mOnItemClickListener = null;
    public CityHasAdapter(List<Weather>weatherList){
        this.weatherList =weatherList;
    }

    

    //define interface
    public static interface OnItemClickListener {
        void onItemClick(View view, int position);
        void onItemLongClick(View view, int positio);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.weather_item,parent,false);
        ViewHolder vh=new ViewHolder(itemView);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.cityName.setText(weatherList.get(position).basic.cityName);
        holder.tmp_max.setText(weatherList.get(position).forecastList.get(0).tmp_max+"°C");
        holder.tmp_min.setText(weatherList.get(position).forecastList.get(0).tmp_min+"°C");
        holder.tmp_now.setText(weatherList.get(position).now.cond_txt);

        //将position保存在itemView的Tag中，以便点击时进行获取
        if(mOnItemClickListener!=null){
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener(){

                @Override
                public boolean onLongClick(View v) {
                    mOnItemClickListener.onItemLongClick(v,position);
                    return true;
                }
            });
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onItemClick(v,position);
                }
            });
        }
        holder.itemView.setTag(position);
    }

    @Override
    public int getItemCount() {
        if(weatherList !=null){
            return weatherList.size();
        }else{
            return 0;
        }

    }




    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView cityName,tmp_max,tmp_min,tmp_now;
        public ViewHolder(View itemView) {
            super(itemView);
            cityName=itemView.findViewById(R.id.weather_item_cityName);
            tmp_now=itemView.findViewById(R.id.weather_item_now);
            tmp_min=itemView.findViewById(R.id.weather_item_min);
            tmp_max=itemView.findViewById(R.id.weather_item_max);

        }
    }
    public void update(List<Weather>weatherList){
        this.weatherList =weatherList;
        notifyDataSetChanged();
    }


}

