package com.simweather.gaoch.util;

import android.util.Log;

public class Bearing {
    /**
     *
     *       @param lat_a 纬度1
     *       @param lng_a 经度1
     *       @param lat_b 纬度2
     *       @param lng_b 经度2
     *       @return
     * */
    public static double lat_a,lng_a;
    public static boolean hasInit=false;
    public static double R;

    public static double getAngle1(double lat_b, double lng_b) {
        double y = Math.sin(lng_b-lng_a) * Math.cos(lat_b);
        double x = Math.cos(lat_a)*Math.sin(lat_b) - Math.sin(lat_a)*Math.cos(lat_b)*Math.cos(lng_b-lng_a);
        double bearing = Math.atan2(y, x);
        bearing = Math.toDegrees(bearing);
        if(bearing < 0){
            bearing = bearing +360;
        }
        Log.d("Location-----",lat_a+"\t"+lng_a+"\t"+lat_b+"\t"+lng_b+"\t"+x+"\t"+y+"\tbearing:"+bearing);
        return bearing;
    }
    public static void test() {
        //灯市口地铁站------东四 地铁站     1.4129872404869301
        //double angle1 = getAngle1(39.917099,116.41765,39.924504,116.41735);
        //System.out.println(360-angle1);
    }
}
