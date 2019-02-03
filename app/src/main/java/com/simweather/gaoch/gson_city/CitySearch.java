package com.simweather.gaoch.gson_city;

import com.google.gson.annotations.SerializedName;


import java.util.List;

public class CitySearch {
    @SerializedName("basic")
    public List<Basic> BasicList;

    public class Basic{
        @SerializedName("location")
        public String cityName;

        @SerializedName("cnty")
        public String countryName;

        @SerializedName("cid")
        public String id;

        @SerializedName("lat")
        public String lat;

        @SerializedName("lon")
        public String lon;

    }

    @SerializedName("status")
    public String status;



}
