package com.google.android.gms.location.sample.locationupdatesforegroundservice;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ScreenModuleService  {

    @GET("api")
    Call<List<ScreenModule>> all();

    @GET("api/{id}")
    Call<ScreenModule> get(@Path("id") int id);


    @POST("api/create")
    Call<ScreenModule> create(@Body ScreenModule screen_);


    @GET("api/{id}")
    Call<ScreenModule> getUser( @Query("id") int id);


    @FormUrlEncoded
    @PATCH("api/{id}/")
    Call<ScreenModule> updateLocation(@Path("id") int id, @Field("longitude") double longitude , @Field("latitude") double latitude);

}