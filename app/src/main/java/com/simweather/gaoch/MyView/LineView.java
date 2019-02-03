package com.simweather.gaoch.MyView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposePathEffect;
import android.graphics.CornerPathEffect;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Rect;
import android.graphics.SumPathEffect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.simweather.gaoch.gson_weather.Forecast;
import com.simweather.gaoch.util.Utility;

import java.util.ArrayList;
import java.util.List;

public class LineView extends View {
    private int maxValue,minValue;
    private List<Forecast> dotList;
    private Paint mPaint;
    private float maxWidth,maxHeight;
    private float db;
    private float sp;
    private DashPathEffect dashPathEffect;
    float width;
    float height;  //每度的高度
    float startY;
    float startX;
    int textSize=Utility.sp2px(getContext(),10);
    int picSize=(int)Utility.dp2px(getContext(),20);
    PathEffect pathEffect ;
    ComposePathEffect composePathEffect;
    int last_index;
    public  LineView(Context context){
        super(context);
        maxValue=40;
        minValue=0;
        mPaint = new Paint();
        dotList=new ArrayList<Forecast>();
        db=Utility.dp2px(context,1);
        sp=Utility.sp2px(context,1);
    }
    public  LineView(Context context, AttributeSet attrs){
        super(context,attrs);
        maxValue=40;
        minValue=0;
        mPaint = new Paint();
        dotList=new ArrayList<Forecast>();
    }
    public void addDots(List<Forecast> list){
        if(!list.isEmpty()){
            Log.d("LineView:","lise.size()="+list.size()+"");
            dotList.clear();
            dotList.addAll(list);
            maxValue=-100;minValue=100;
            for(int i=0;i<dotList.size();i++){
                int high=Integer.valueOf(list.get(i).tmp_max);
                int low=Integer.valueOf(list.get(i).tmp_min);
                maxValue=high>maxValue?high:maxValue;
                minValue=low<minValue?low:minValue;
            }
        }
        mPaint.setColor(Color.WHITE);
        pathEffect = new CornerPathEffect(20);

        mPaint.setPathEffect(pathEffect);
        mPaint.setStrokeWidth(Utility.dp2px(getContext(),2));
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(Utility.sp2px(getContext(),10));
        mPaint.setStyle(Paint.Style.STROKE);
        //textSize=Utility.sp2px(getContext(),10);
        dashPathEffect=new DashPathEffect(new float[]{Utility.dp2px(getContext(),1),Utility.dp2px(getContext(),2)},0);
        composePathEffect =new ComposePathEffect(dashPathEffect,pathEffect);
        mPaint.setColor(Color.WHITE);
        last_index=dotList.size()-1;
        Log.e("LineView","addDots");
    }

    public void getLines(Canvas canvas){
        maxWidth=getWidth();
        maxHeight=getHeight();
        width= maxWidth /(dotList.size());
        height=(maxHeight/3)/(maxValue-minValue);  //每度的高度
        startY=maxHeight/5;
        startX=(float)(width/2.5);
        Path path = new Path();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setPathEffect(pathEffect);
        for (int i = 0; i <= dotList.size() - 1; i++) {
            int high=Integer.valueOf(dotList.get(i).tmp_max);
            if (i == 0) {
                path.moveTo(startX, startY-(high-maxValue)*height);
            } else {
                path.lineTo(startX+width*i,startY-(high-maxValue)*height);
            }
            canvas.drawText(dotList.get(i).tmp_max,startX+width*i-textSize/2,startY-(high-maxValue)*height-(int)(textSize*1.5),mPaint);
            if(i==dotList.size()-1){
                canvas.drawText(dotList.get(last_index).tmp_max,startX+width*(last_index)-textSize/2,startY-(Integer.valueOf(dotList.get( last_index).tmp_max)-maxValue)*height-(int)(textSize*1.5),mPaint);
            }
        }
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawPath(path, mPaint);


        path=new Path();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setPathEffect(composePathEffect);
        for (int i = 0; i <= dotList.size() - 1; i++) {
            int low=Integer.valueOf(dotList.get(i).tmp_min);
            if (i == 0) {
                path.moveTo(startX+width*i,startY-(low-maxValue)*height);
            } else {
                path.lineTo(startX+width*i,startY-(low-maxValue)*height);
            }
            canvas.drawText(dotList.get(i).tmp_min,startX+width*i-textSize/2,startY-(low-maxValue)*height-(int)(textSize*0.5),mPaint);
            if(i==dotList.size()-1){
                canvas.drawText(dotList.get(last_index).tmp_min,startX+width*(last_index)-textSize/2,startY-(Integer.valueOf(dotList.get( last_index).tmp_min)-maxValue)*height-(int)(textSize*0.5),mPaint);
            }
        }
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawPath(path, mPaint);


        mPaint.setStyle(Paint.Style.FILL);
        for(int i=0;i<dotList.size();i++){
            Bitmap bitmap=BitmapFactory.decodeStream(getClass().getResourceAsStream("/res/drawable/w"+dotList.get(i).cond_code_d+".png"));
            drawImage(canvas,bitmap,(int)(startX+width*i-picSize/2),(int)(1.2*startY+maxHeight/2),picSize,picSize,0,0);
            canvas.drawText(data2simData(dotList.get(i).date),startX+width*i-textSize/2,(float)(startY*4.6),mPaint);
        }
        Log.e("LineView","getLines()");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(!dotList.isEmpty()){
            getLines(canvas);
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int minimumWidth = getSuggestedMinimumWidth();
        final int minimumHeight = getSuggestedMinimumHeight();
        int width = measureWidth(minimumWidth, widthMeasureSpec);
        int height = measureHeight(minimumHeight, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    private int measureWidth(int defaultWidth, int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            case MeasureSpec.AT_MOST:
                defaultWidth =  getPaddingLeft() + getPaddingRight();
                break;
            case MeasureSpec.EXACTLY:
                defaultWidth = specSize;
                break;
            case MeasureSpec.UNSPECIFIED:
                defaultWidth = Math.max(defaultWidth, specSize);
        }
        Log.d("LineView","Width:"+defaultWidth);
        return defaultWidth;
    }


    private int measureHeight(int defaultHeight, int measureSpec) {

        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
       switch (specMode) {
            case MeasureSpec.AT_MOST:
                defaultHeight =  getPaddingTop() + getPaddingBottom();
                break;
            case MeasureSpec.EXACTLY:
                defaultHeight = specSize;
                break;
            case MeasureSpec.UNSPECIFIED:
                defaultHeight = Math.max(defaultHeight, specSize);
                break;
        }
        Log.d("LineView","Height:"+defaultHeight);
        return defaultHeight;


    }

    public String data2simData(String origin_date){
        String date[] = origin_date.split("-");
        String date_month = date[date.length-2];
        String date_day = date[date.length-1];
        date_month = date_month.replaceFirst("^0*", "");
        date_day = date_day.replaceFirst("^0*","");
        return date_month+"."+date_day;
    }

    public static void drawImage(Canvas canvas, Bitmap blt, int x, int y, int w, int h, int bx, int by) {
        Rect src = new Rect();// 图片 >>原矩形
        Rect dst = new Rect();// 屏幕 >>目标矩形

        src.left = bx;
        src.top = by;
        src.right = bx + w;
        src.bottom = by + h;

        dst.left = x;
        dst.top = y;
        dst.right = x + w;
        dst.bottom = y + h;
        // 画出指定的位图，位图将自动--》缩放/自动转换，以填补目标矩形
        // 这个方法的意思就像 将一个位图按照需求重画一遍，画后的位图就是我们需要的了
        canvas.drawBitmap(blt, null, dst, null);
        src = null;
        dst = null;
    }
}
