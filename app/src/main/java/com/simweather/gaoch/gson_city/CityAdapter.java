package com.simweather.gaoch.gson_city;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.simweather.gaoch.R;

import java.util.List;

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

