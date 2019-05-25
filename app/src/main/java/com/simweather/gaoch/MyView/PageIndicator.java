package com.simweather.gaoch.MyView;

import android.content.Context;
import androidx.viewpager.widget.ViewPager;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.simweather.gaoch.R;

import java.util.ArrayList;
import java.util.List;

public class PageIndicator implements ViewPager.OnPageChangeListener {
    private int mPageCount;//页数
    private List<ImageView> mImgList;//保存img总个数
    private int img_select;
    private int img_unSelect;

    public PageIndicator(Context context, LinearLayout linearLayout, int pageCount) {
        this.mPageCount = pageCount;

        mImgList = new ArrayList<>();
        img_select = R.drawable.dot_select;
        img_unSelect = R.drawable.dot_unselect;
        final int imgSize = 25;

        for (int i = 0; i < mPageCount; i++) {
            ImageView imageView = new ImageView(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            //为小圆点左右添加间距
            params.leftMargin = 10;
            params.rightMargin = 10;
            //给小圆点一个默认大小
            params.height = imgSize;
            params.width = imgSize;
            if (i == 0) {
                imageView.setBackgroundResource(img_select);
            } else {
                imageView.setBackgroundResource(img_unSelect);
            }
            //为LinearLayout添加ImageView
            linearLayout.addView(imageView, params);
            mImgList.add(imageView);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

    @Override
    public void onPageSelected(int position) {
        for (int i = 0; i < mPageCount; i++) {
            //选中的页面改变小圆点为选中状态，反之为未选中
            if ((position % mPageCount) == i) {
                (mImgList.get(i)).setBackgroundResource(img_select);
            } else {
                (mImgList.get(i)).setBackgroundResource(img_unSelect);
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {}
}
