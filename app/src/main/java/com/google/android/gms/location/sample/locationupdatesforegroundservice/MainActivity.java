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

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.location.Location;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.preference.PreferenceManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;

import android.Manifest;

import android.content.pm.PackageManager;

import android.net.Uri;

import android.provider.Settings;
import androidx.annotation.NonNull;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;

import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;
import android.widget.VideoView;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.widget.Toast.makeText;

/**
 * The only activity in this sample.
 *
 * Note: Users have three options in "Q" regarding location:
 * <ul>
 *     <li>Allow all the time</li>
 *     <li>Allow while app is in use, i.e., while app is in foreground</li>
 *     <li>Not allow location at all</li>
 * </ul>
 * Because this app creates a foreground service (tied to a Notification) when the user navigates
 * away from the app, it only needs location "while in use." That is, there is no need to ask for
 * location all the time (which requires additional permissions in the manifest).
 *
 * "Q" also now requires developers to specify foreground service type in the manifest (in this
 * case, "location").
 *
 * Note: For Foreground Services, "P" requires additional permission in manifest. Please check
 * project manifest for more information.
 *
 * Note: for apps running in the background on "O" devices (regardless of the targetSdkVersion),
 * location may be computed less frequently than requested when the app is not in the foreground.
 * Apps that use a foreground service -  which involves displaying a non-dismissable
 * notification -  can bypass the background location limits and request location updates as before.
 *
 * This sample uses a long-running bound and started service for location updates. The service is
 * aware of foreground status of this activity, which is the only bound client in
 * this sample. After requesting location updates, when the activity ceases to be in the foreground,
 * the service promotes itself to a foreground service and continues receiving location updates.
 * When the activity comes back to the foreground, the foreground service stops, and the
 * notification associated with that foreground service is removed.
 *
 * While the foreground service notification is displayed, the user has the option to launch the
 * activity from the notification. The user can also remove location updates directly from the
 * notification. This dismisses the notification and stops the service.
 */
public class MainActivity extends AppCompatActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener ,
        SurfaceHolder.Callback, MediaPlayer.OnPreparedListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    // Used in checking for runtime permissions.
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    // The BroadcastReceiver used to listen from broadcasts from the service.
    private MyReceiver myReceiver;

    // A reference to the service used to get location updates.
    private LocationUpdatesService mService = null;

    // Tracks the bound state of the service.
    private boolean mBound = false;


    static String number = "";

    Context context;
    // UI elements.
    private Button mRequestLocationUpdatesButton;
    private Button mRemoveLocationUpdatesButton;
    private EditText Device_id;
    private SurfaceView mediaSection_id;

    private MediaPlayer mediaPlayer;
    private SurfaceHolder vidHolder;
    String vidAddress = "https://archive.org/download/ksnn_compilation_master_the_internet/ksnn_compilation_master_the_internet_512kb.mp4";
    Vector<String> media = new Vector<String>();
    private int  values = 0;

    //ActivityCompat.requestPermissions(MainActivity.this ,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},123);



    // Monitors the state of the connection to the service.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationUpdatesService.LocalBinder binder = (LocationUpdatesService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myReceiver = new MyReceiver();
        setContentView(R.layout.activity_main);

        // Check that the user hasn't revoked permissions by going to Settings.
        if (Utils.requestingLocationUpdates(this)) {
            if (!checkPermissions()) {
                requestPermissions();
            }
        }

        mediaSection_id = (SurfaceView) findViewById(R.id.mediaSection_id);
        vidHolder = mediaSection_id.getHolder();
        vidHolder.addCallback(this);

    }

    @Override
    protected void onStart() {
        super.onStart();

        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);

        mRequestLocationUpdatesButton = (Button) findViewById(R.id.request_location_updates_button);

        Device_id = findViewById(R.id.Device_id);
//        mediaSection_id = findViewById(R.id.mediaSection_id);



        ActivityCompat.requestPermissions(MainActivity.this , new String[]{Manifest.permission.ACCESS_FINE_LOCATION},123);
        mRequestLocationUpdatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!checkPermissions()) {
                    requestPermissions();
                } else {
                    mService.requestLocationUpdates();
                }
                number = Device_id.getText().toString();
                playVideo();
                mRequestLocationUpdatesButton.setVisibility(view.GONE);

                Device_id.setVisibility(view.GONE);

            }
        });



        // Restore the state of the buttons when the activity (re)launches.
        setButtonsState(Utils.requestingLocationUpdates(this));

        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
        bindService(new Intent(this, LocationUpdatesService.class), mServiceConnection,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver,
                new IntentFilter(LocationUpdatesService.ACTION_BROADCAST));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (mBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            unbindService(mServiceConnection);
            mBound = false;
        }
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onStop();
    }

    /**
     * Returns the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        return  PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            Snackbar.make(
                    findViewById(R.id.activity_main),
                    R.string.permission_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    })
                    .show();
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

//
    public void playVideo(){
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        android.widget.LinearLayout.LayoutParams params = (android.widget.LinearLayout.LayoutParams) mediaSection_id.getLayoutParams();
        params.width = metrics.widthPixels;
        params.height = metrics.heightPixels;
        params.leftMargin = 0;
        mediaSection_id.setLayoutParams(params);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://iqmediaapp.herokuapp.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        final AdvModuleService advService = retrofit.create(AdvModuleService.class);

        advService.all().enqueue(new Callback< List<AdvModule>>(){
            @NonNull
            @Override
            public void onResponse(Call<List<AdvModule>> Call , Response<List<AdvModule>> resp) {

                assert resp.body() != null;

                for(AdvModule adv : resp.body()){
                    if(adv.QueuePlayer == true){
                        media.add(adv.adMedia);
                        Log.i(TAG ,adv.adTitle);
                    }

                }
                try {

                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setDisplay(vidHolder);
                        Log.i(TAG, (String) media.get(values));
                        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {

                                values  +=1;
                                if(values == media.size())
                                    values = 0;
                                media.clear();
                                playVideo();
                                mediaPlayer.reset();
                                Log.i(TAG , String.valueOf(values));

                            }
                        });
                        mediaPlayer.setDataSource(String.valueOf(media.get(values)));
                        mediaPlayer.prepare();
//                    mediaPlayer.setOnPreparedListener(onPrepared());
                        mediaPlayer.start();
                        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
            @NonNull
            @Override
            public void onFailure(Call<List<AdvModule>> _, Throwable t){
                t.printStackTrace();
            }
        });


    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted.
                mService.requestLocationUpdates();
            } else {
                // Permission denied.
                setButtonsState(false);
                Snackbar.make(
                        findViewById(R.id.activity_main),
                        R.string.permission_denied_explanation,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        })
                        .show();
            }
        }
    }


    ///MEDIA PLAYER HERE
    @Override
    public void onPrepared(MediaPlayer mp) {
        mediaPlayer.start();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
//        playVideo();
//        try {
//                vidAddress = media.elementAt(0);
//                mediaPlayer = new MediaPlayer();
//                mediaPlayer.setDisplay(vidHolder);
//                mediaPlayer.setDataSource(vidAddress);
//                mediaPlayer.prepare();
//                mediaPlayer.setOnPreparedListener(this);
//                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//        }
//        catch(Exception e){
//            e.printStackTrace();
//        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    ///MEDIAPLAYER ENDS HERE

    /**
     * Receiver for broadcasts sent by {@link LocationUpdatesService}.
     */
    private class MyReceiver extends BroadcastReceiver  {
      //  @Override
        public void onReceive(Context context, Intent intent) {



            Location location = intent.getParcelableExtra(LocationUpdatesService.EXTRA_LOCATION);
            if (location != null) {
                Utils.getLocationText(location , number);
//                Toast.makeText(MainActivity.this, Utils.getLocationText(location , number),
//                        Toast.LENGTH_SHORT).show();
            }

        }




    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        // Update the buttons state depending on whether location updates are being requested.
        if (s.equals(Utils.KEY_REQUESTING_LOCATION_UPDATES)) {
            setButtonsState(sharedPreferences.getBoolean(Utils.KEY_REQUESTING_LOCATION_UPDATES,
                    false));
        }
    }

    private void setButtonsState(boolean requestingLocationUpdates) {
        if (requestingLocationUpdates) {
            mRequestLocationUpdatesButton.setEnabled(true);

        } else {
            mRequestLocationUpdatesButton.setEnabled(true);

        }
    }
}
