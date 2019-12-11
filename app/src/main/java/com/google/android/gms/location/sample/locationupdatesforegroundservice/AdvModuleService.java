package com.google.android.gms.location.sample.locationupdatesforegroundservice;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface AdvModuleService {

    @GET("api/adv")
    Call<List<AdvModule>> all();
}
