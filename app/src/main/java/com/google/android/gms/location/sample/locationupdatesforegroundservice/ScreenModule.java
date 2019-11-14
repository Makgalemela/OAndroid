package com.google.android.gms.location.sample.locationupdatesforegroundservice;

import com.google.gson.annotations.SerializedName;

public class ScreenModule {

    @SerializedName("deviceNumber")
    String deviceNumber;

    @SerializedName("carOwner")
    String carOwner;

    @SerializedName("activeFlag")
    boolean  activeFlag;

    @SerializedName("id")
    int id;

    @SerializedName("longitude")
    double longitude;

    @SerializedName("latitude")
    double latitude;




//    public ScreenModule (double longitude, double latitude ){
//        this.longitude = longitude;
//       this.latitude =latitude;
//
//    }


}