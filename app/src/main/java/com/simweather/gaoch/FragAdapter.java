package com.simweather.gaoch;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.List;

public class FragAdapter extends FragmentPagerAdapter {
    private List<FragmentWeather>fragmentList;

    public FragAdapter(FragmentManager fm,List<FragmentWeather>fragmentList) {
        super(fm);
        this.fragmentList=fragmentList;

    }

    @Override
    public Fragment getItem(int i) {
        return fragmentList.get(i);
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }
}
