/**
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.gms.location.sample.locationupdatesforegroundservice;


import android.content.Context;
import android.location.Location;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


class Utils {

    static final String KEY_REQUESTING_LOCATION_UPDATES = "requesting_locaction_updates";
    static int device_idNumber = 1;
    private static final String TAG = "MyActivity";

    /**
     * Returns true if requesting location updates, otherwise returns false.
     *
     * @param context The {@link Context}.
     */
    static boolean requestingLocationUpdates(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context )
                .getBoolean(KEY_REQUESTING_LOCATION_UPDATES, false);
    }



    /**
     * Stores the location updates state in SharedPreferences.
     * @param requestingLocationUpdates The location updates state.
     */
    static void setRequestingLocationUpdates(Context context, boolean requestingLocationUpdates) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_REQUESTING_LOCATION_UPDATES, requestingLocationUpdates)
                .apply();
    }

    /**
     * Returns the {@code location} object as a human readable string.
     * @param location  The {@link Location}.
     * @param device_id
     */
    static String getLocationText(Location location , final String device_id) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://iqmediaapp.herokuapp.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        final ScreenModuleService service = retrofit.create(ScreenModuleService.class);

        //Call<List<ScreenModule>> createCall = service.all();
        service.all().enqueue(new Callback<List<ScreenModule>>(){
            int screen_pk = 0;
            @Override
            public  void onResponse(Call<List<ScreenModule>> Call, Response<List<ScreenModule>> resp){


                for(ScreenModule screen_ : resp.body()){
                    //textView_id.append(screen_.deviceNumber +" "+ screen_.latitude +" --- "+ screen_.longitude+"\n");
                    if(screen_.deviceNumber.equals( device_id)){
                        device_idNumber = screen_.id;
                      //  Log.i(TAG , "Found : " + device_id);
                        break;
                    }
                }
            }

            @Override
            public  void onFailure(Call<List<ScreenModule>> _, Throwable t){

                t.printStackTrace();
                //textView_id.setText(t.getMessage());

            }
        });


        final Double latitude = location.getLatitude();
        final Double longitude = location.getLongitude();
        Log.i(TAG , "Found - here: " + device_idNumber+" "+latitude +" "+longitude);
        service.updateLocation(device_idNumber,latitude,longitude).enqueue(new Callback<ScreenModule>() {
            @NonNull
            @Override
            public void onResponse(Call<ScreenModule> _, Response<ScreenModule> resp) {
                ScreenModule screen = resp.body();
               // textView_id.setText("Device connected " );

            }
            @Override
            public void onFailure(Call<ScreenModule> _, Throwable t) {
                t.printStackTrace();
                //textView_id.setText(t.getMessage());
            }

        });


        return location == null ? "Unknown location" :
                "(" + location.getLatitude() + ", " + location.getLongitude() + ", "+ location.getAccuracy() + ")";
    }

    static String getLocationTitle(Context context) {
        return context.getString(R.string.location_updated,
                DateFormat.getDateTimeInstance().format(new Date()));
    }


}
