package com.simweather.gaoch;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.simweather.gaoch.util.Blur;
import com.simweather.gaoch.util.ConstValue;


/**
 * Created by GaoCh on 2018/7/24.
 */

public class FragmentConfig extends Fragment {
    private Button key_button;
    private EditText key_input;
    private Button navButton;
    private Button hoursButtom;
    private EditText hoursInput;
    private Switch backServiceSwitch;
    private LinearLayout layout1,layout2,layoutBG,layout3;
    int hasBlured_top1=0,hasBlured_top2=0,hasBlured_top3=0;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_config,container,false);
        key_button = view.findViewById(R.id.config_key_yes);
        key_input = view.findViewById(R.id.config_key_text);
        navButton = view.findViewById(R.id.nav_button);
        hoursInput = view.findViewById(R.id.config_circle_text);
        hoursButtom = view.findViewById(R.id.config_circle_yes);
        backServiceSwitch = view.findViewById(R.id.switch_back_service);
        //layoutBG = view.findViewById(R.id.config_mother);
        layout1 = view.findViewById(R.id.config_li_1);
        layout2 = view.findViewById(R.id.config_li_2);
        layout3=view.findViewById(R.id.config_li_3);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        WeatherActivity activity = (WeatherActivity) getActivity();
        if(activity.isBackGroundService()){
            backServiceSwitch.setChecked(true);
        }
        hoursInput.setText(String.valueOf(activity.getServiceHours()));
        key_input.setText(activity.getKey());


        backServiceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    WeatherActivity activity= (WeatherActivity)getActivity();
                    activity.serviceOpen();
                }else{
                    WeatherActivity activity= (WeatherActivity)getActivity();
                    activity.serviceClose();
                }
            }
        });

        key_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String key = key_input.getText().toString();
                if(key!=null&&key!=""){
                    WeatherActivity weatherActivity = (WeatherActivity) getActivity();
                    weatherActivity.saveKey(key);
                }
            }
        });

        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WeatherActivity activity = (WeatherActivity) getActivity();
                activity.showDrawer();
            }
        });

        //setBg();

        hoursButtom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hours = Integer.valueOf(hoursInput.getText().toString());
                if(hours<=0){
                    Toast.makeText(getContext(), "时间设置必须为正整数！", Toast.LENGTH_SHORT).show();
                }else {
                    WeatherActivity activity = (WeatherActivity) getActivity();
                    activity.updateServiceHours(hours);
                }
            }
        });


        /**
         *初始背景图片
         */
        setBlur();
    }


    public void setBlur(){
        if(((WeatherActivity)getActivity()).getIsBlur()){
            final View view_test=((WeatherActivity)getActivity()).blur_main;
            layout1.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    if(hasBlured_top1!=layout1.getTop()){
                        Blur.blur_static(view_test,layout1,ConstValue.radius,ConstValue.scaleFactor,ConstValue.RoundCorner);
                        hasBlured_top1=layout1.getTop();
                    }

                    return true;
                }
            });
            layout2.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    if(hasBlured_top2!=layout2.getTop()){
                        Blur.blur_static(view_test,layout2,ConstValue.radius,ConstValue.scaleFactor,ConstValue.RoundCorner);
                        hasBlured_top2=layout2.getTop();
                    }

                    return true;
                }
            });
            layout3.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    if(hasBlured_top3!=layout3.getTop()){
                        Blur.blur_static(view_test,layout3,ConstValue.radius,ConstValue.scaleFactor,ConstValue.RoundCorner);
                        hasBlured_top3=layout3.getTop();
                    }
                    return true;
                }
            });
        }
    }
}
