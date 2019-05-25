package com.simweather.gaoch;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.simweather.gaoch.util.BlurSingle;
import com.simweather.gaoch.util.ConstValue;
import com.simweather.gaoch.util.Utility;


/**
 * Created by GaoCh on 2018/7/24.
 */

public class FragmentTheme extends Fragment implements CompoundButton.OnCheckedChangeListener {
    private RadioGroup theme_radio_group,theme_radio_group_2;
    private RadioButton rb_blue, rb_blue1, rb_green, rb_grey, rb_pink, rb_red, rb_yellow;
    private Switch switch_blur;
    private Button navButton;
    private Button BgButton;
    private Button BgButton1;
    private SeekBar seek_blur;
    private TextView text_blur;
    private LinearLayout layout1, layout2,layout3;
    int hasSetRadioButtomsColor=0;
    private BlurSingle.BlurLayout blur1,blur2,blur3;
    private boolean hasBlur=false;
    private ScrollView scrollView;
    private LinearLayout layout_title;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_theme,container,false);
        initView(view);
        return view;
    }
    public void initView(View view){
        navButton = view.findViewById(R.id.nav_button);
        BgButton = view.findViewById(R.id.theme_chooseBG);
        BgButton1 = view.findViewById(R.id.theme_cancelBG);
        layout1 = view.findViewById(R.id.fragment_theme_layout_1);
        layout2 = view.findViewById(R.id.fragment_theme_layout_2);
        layout3=view.findViewById(R.id.fragment_theme_layout_blur);
        switch_blur=view.findViewById(R.id.config_theme_switch_blur);
        seek_blur=view.findViewById(R.id.config_theme_seek_blur);
        text_blur=view.findViewById(R.id.config_theme_text_blur);


        theme_radio_group = view.findViewById(R.id.config_theme);
        theme_radio_group_2=view.findViewById(R.id.config_theme_1);
        rb_blue =view.findViewById(R.id.config_theme_blue);
        rb_blue1 =view.findViewById(R.id.config_theme_blue1);
        rb_green =view.findViewById(R.id.config_theme_green);
        rb_grey =view.findViewById(R.id.config_theme_grey);
        rb_pink =view.findViewById(R.id.config_theme_pink);
        rb_red =view.findViewById(R.id.config_theme_red);
        rb_yellow =view.findViewById(R.id.config_theme_yellow);

        rb_blue.setOnCheckedChangeListener(this);
        rb_blue1.setOnCheckedChangeListener(this);
        rb_green.setOnCheckedChangeListener(this);
        rb_grey.setOnCheckedChangeListener(this);
        rb_pink.setOnCheckedChangeListener(this);
        rb_red.setOnCheckedChangeListener(this);
        rb_yellow.setOnCheckedChangeListener(this);

        layout_title=view.findViewById(R.id.fragment_theme_layout);
        scrollView=view.findViewById(R.id.fragment_theme_mother);
        Utility.setBelowStatusBar(getContext(),layout_title,view,0,0);


    }


    @Override
    public void onResume() {
        super.onResume();
        if(!hasBlur){
            setBlur();
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        switch_blur.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked&& ((WeatherActivity) getActivity()).getIsBlur()==false){
                    WeatherActivity activity = (WeatherActivity) getActivity();
                    activity.saveIsBlur(true);
                    setBlur();
                    seek_blur.setVisibility(View.VISIBLE);
                    text_blur.setVisibility(View.VISIBLE);
                }else if(!isChecked){
                    WeatherActivity activity = (WeatherActivity) getActivity();
                    activity.saveIsBlur(false);
                    Toast.makeText(activity, "重启生效", Toast.LENGTH_SHORT).show();
                    seek_blur.setVisibility(View.GONE);
                    text_blur.setVisibility(View.GONE);


                }
            }
        });
        seek_blur.setProgress(ConstValue.radius);
        text_blur.setText("模糊程度:"+ConstValue.radius+"/25");
        seek_blur.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                text_blur.setText("模糊程度:"+progress+"/25");
                ConstValue.radius=progress;

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(ConstValue.radius<2){
                    ConstValue.radius=2;
                }
                SharedPreferences.Editor editor = getActivity().getSharedPreferences(ConstValue.getConfigDataName(),Context.MODE_PRIVATE).edit();
                editor.putInt(ConstValue.sp_radius,ConstValue.radius);
                editor.apply();
                final View view_test=((WeatherActivity)getActivity()).blur_main;
                BlurSingle.initBkg(view_test,ConstValue.radius,ConstValue.scaleFactor);
                blur1.reSetPositions();
                blur2.reSetPositions();
                blur3.reSetPositions();

            }
        });


        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WeatherActivity activity = (WeatherActivity) getActivity();
                activity.showDrawer();
            }
        });

        BgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WeatherActivity activity = (WeatherActivity) getActivity();
                activity.chooseBg();
                if(hasBlur){
                    blur1.reSetPositions();
                    blur2.reSetPositions();
                    blur3.reSetPositions();
                }

            }
        });
        BgButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WeatherActivity activity = (WeatherActivity) getActivity();
                activity.cancelBg();
                activity.saveIsBlur(false);
                switch_blur.setChecked(false);
            }
        });

        WeatherActivity activity = (WeatherActivity) getActivity();
        if(activity.getIsBlur()){
            switch_blur.setChecked(true);
            seek_blur.setVisibility(View.VISIBLE);
            text_blur.setVisibility(View.VISIBLE);
        }else{
            seek_blur.setVisibility(View.GONE);
            text_blur.setVisibility(View.GONE);
        }

        setRadioButtomsColor(activity);


    }




    public void setRadioButtomsColor(WeatherActivity activity){
        switch (activity.getColor()){
            case "blue":
                rb_blue.setChecked(true);
                break;
            case "red":
                rb_red.setChecked(true);
                break;
            case "yellow":
                rb_yellow.setChecked(true);
                break;
            case "grey":
                rb_grey.setChecked(true);
                break;
            case "pink":
                rb_pink.setChecked(true);
                break;
            case "green":
                rb_green.setChecked(true);
                break;
            case "blue1":
                rb_blue1.setChecked(true);
                break;
        }
        hasSetRadioButtomsColor=1;
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(isChecked){
            int color=-1;
            switch (buttonView.getId()){
                case R.id.config_theme_blue:
                    color=R.id.config_theme_blue;
                    theme_radio_group_2.clearCheck();
                    break;
                case R.id.config_theme_blue1:
                    color=R.id.config_theme_blue1;
                    theme_radio_group.clearCheck();
                    break;
                case R.id.config_theme_green:
                    color=R.id.config_theme_green;
                    theme_radio_group.clearCheck();
                    break;
                case R.id.config_theme_grey:
                    color=R.id.config_theme_grey;
                    theme_radio_group_2.clearCheck();
                    break;
                case R.id.config_theme_pink:
                    color=R.id.config_theme_pink;
                    theme_radio_group.clearCheck();
                    break;
                case R.id.config_theme_red:
                    color=R.id.config_theme_red;
                    theme_radio_group_2.clearCheck();
                    break;
                case R.id.config_theme_yellow:
                    color=R.id.config_theme_yellow;
                    theme_radio_group_2.clearCheck();
                    break;
            }
            if(color!=-1&&hasSetRadioButtomsColor==1){
                WeatherActivity activity = (WeatherActivity) getActivity();
                activity.saveTheme(color);
            }
        }

    }
    public void setBlur(){
        if(((WeatherActivity)getActivity()).getIsBlur()){
            final View view_test=((WeatherActivity)getActivity()).blur_main;
            blur1=new BlurSingle.BlurLayout(layout1,view_test);
            blur2=new BlurSingle.BlurLayout(layout2,view_test);
            blur3=new BlurSingle.BlurLayout(layout3,view_test);
        }
    }
}
