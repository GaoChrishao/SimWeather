package com.simweather.gaoch.MyView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.simweather.gaoch.gson_weather.HourFor;
import com.simweather.gaoch.util.Utility;

import java.util.ArrayList;
import java.util.List;

public class LineHourlyView extends View {
    private int maxValue,minValue;
    private List<HourFor> dotList;
    private Paint mPaint;
    private float maxWidth,maxHeight;
    private float db;
    private float sp;
    int textSize;
    float width;
    float height;
    float startY;
    float startX;
    int picSize;
    PathEffect pathEffect = new CornerPathEffect(Utility.sp2px(getContext(),20));
    public LineHourlyView(Context context){
        super(context);
        maxValue=-50;minValue=60;
        mPaint = new Paint();
        dotList=new ArrayList<HourFor>();
        db=Utility.dp2px(context,1);
        sp=Utility.sp2px(context,1);

    }
    public LineHourlyView(Context context, AttributeSet attrs){
        super(context,attrs);
        maxValue=-50;minValue=60;
        mPaint = new Paint();
        dotList=new ArrayList<HourFor>();
        db=Utility.dp2px(context,1);
        sp=Utility.sp2px(context,1);

    }
    public void addDots(List<HourFor> list){
        if(list!=null&&list.size()>0){

            dotList.clear();
            dotList.addAll(list);
        }
        for(int i=0;i<dotList.size();i++){
            int high=Integer.valueOf(list.get(i).tmp);
            maxValue=high>maxValue?high:maxValue;
            minValue=high<minValue?high:minValue;
        }
        mPaint.setColor(Color.WHITE);
        mPaint.setPathEffect(pathEffect);
        mPaint.setStrokeWidth(Utility.dp2px(getContext(),2));
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(Utility.sp2px(getContext(),10));
        textSize=Utility.sp2px(getContext(),10);
        picSize=Utility.dp2px(getContext(),20);


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
        for (int i = 0; i <= dotList.size() - 1; i++) {

            int high=Integer.valueOf(dotList.get(i).tmp);
            if (i == 0) {
                path.moveTo(startX, startY-(high-maxValue)*height);
            } else {
                path.lineTo(startX+width*i,startY-(high-maxValue)*height);
            }
            canvas.drawText(dotList.get(i).tmp,startX+width*i-textSize/2,startY-(high-maxValue)*height-(int)(textSize*1.5),mPaint);
        }
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawPath(path, mPaint);
        mPaint.setStyle(Paint.Style.FILL);
        int last_index=dotList.size()-1;
        canvas.drawText(dotList.get(last_index).tmp,startX+width*(last_index)-textSize/2,startY-(Integer.valueOf(dotList.get( last_index).tmp)-maxValue)*height-(int)(textSize*1.5),mPaint);

        for(int i=0;i<dotList.size();i++){
            Bitmap bitmap=BitmapFactory.decodeStream(getClass().getResourceAsStream("/res/drawable/w"+dotList.get(i).cond_code+".png"));
            drawImage(canvas,bitmap,(int)(startX+width*i-picSize/2),(int)(1.2*startY+maxHeight/3),picSize,picSize,0,0);
            canvas.drawText(data2simData(dotList.get(i).time),startX+width*i-textSize,(float)(startY*4.2),mPaint);
            canvas.drawText(data2simData1(dotList.get(i).time),startX+width*i-textSize,(float)(startY*4.6),mPaint);
        }

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
        Log.d("LineHoulyView","measureWidth:"+defaultWidth);
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
        //Log.d("LineView","Height:"+defaultHeight);
        return defaultHeight;


    }

    public String data2simData(String origin_date){
        String odate[]=origin_date.split(" ");
        String date[] = odate[0].split("-");
        String date_month = date[date.length-2];
        String date_day = date[date.length-1];
        date_month = date_month.replaceFirst("^0*", "");
        date_day = date_day.replaceFirst("^0*","");
        return date_month+"."+date_day;
    }

    public String data2simData1(String origin_date){
        String odate[]=origin_date.split(" ");
        return odate[1];
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
