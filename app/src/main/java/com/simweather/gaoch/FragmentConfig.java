package com.simweather.gaoch;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.simweather.gaoch.util.BlurSingle;
import com.simweather.gaoch.util.Utility;


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
    private BlurSingle.BlurLayout blur1,blur2,blur3;

    private boolean hasBlur=false;
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

        Utility.setBelowStatusBar(getContext(),view.findViewById(R.id.fragment__config_title),view,0,0);

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


    }

    @Override
    public void onResume() {
        super.onResume();
        if(!hasBlur){
            setBlur();
        }
    }

    public void setBlur(){
        if(((WeatherActivity)getActivity()).getIsBlur()){
            hasBlur=true;
            final View view_test=((WeatherActivity)getActivity()).blur_main;
            blur1=new BlurSingle.BlurLayout(layout1,view_test);
            blur2=new BlurSingle.BlurLayout(layout2,view_test);
            blur3=new BlurSingle.BlurLayout(layout3,view_test);

        }
    }
}
