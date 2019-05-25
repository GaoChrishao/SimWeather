package com.simweather.gaoch;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.simweather.gaoch.util.BlurSingle;
import com.simweather.gaoch.util.ConstValue;
import com.simweather.gaoch.util.Utility;

/**
 * Created by GaoCh on 2018/7/23.
 */

public class FragmentAbout extends Fragment {
    private BlurSingle.BlurLayout blur1,blur2;
    private LinearLayout layout_1,layout_2;
    private Button navButton;





    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about,container,false);
        layout_1=view.findViewById(R.id.about_li_1);
        layout_2=view.findViewById(R.id.about_li_2);
        navButton=view.findViewById(R.id.nav_button);

        Utility.setBelowStatusBar(getContext(),view.findViewById(R.id.fragment_about_title),view,0,0);

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

        layout_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data=new Intent(Intent.ACTION_SENDTO);
                data.setData(Uri.parse("mailto:"+ConstValue.myMail));
                data.putExtra(Intent.EXTRA_SUBJECT, "SimWeather使用反馈");
                data.putExtra(Intent.EXTRA_TEXT, "这是内容");
                startActivity(data);
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
            blur1=new BlurSingle.BlurLayout(layout_1,view_test);
            blur2=new BlurSingle.BlurLayout(layout_2,view_test);


        }
    }





}


