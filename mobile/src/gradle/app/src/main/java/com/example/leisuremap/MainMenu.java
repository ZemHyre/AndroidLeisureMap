package com.example.leisuremap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class MainMenu extends AppCompatActivity {

    private Button b_map, b_activities, b_sign_in, b_logOut;
    private LocationRequest locationRequest;
    private LatLng pos;
    boolean isLoggedIn = false;
    public static Activity fa;
    int i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        fa = this; // to finish activity if user successfully logs in

        b_map = findViewById(R.id.map);
        b_map.setOnClickListener(view -> openLeisureMap());

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);

        b_activities = findViewById(R.id.findActivities);
        b_activities.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(pos == null){
                    getCurrentLocation();
                    i = 0;
                }
                else
                    openActivities();
            }
        });

        b_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(pos == null){
                    getCurrentLocation();
                    i = 1;
                }
                else
                    openLeisureMap();
            }
        });

        b_sign_in = findViewById(R.id.signIn);
        b_sign_in.setOnClickListener(view -> openLogin());
        b_logOut = findViewById(R.id.logOut);


        isLoggedIn = checkUserStatus();
        if (!isLoggedIn) {
            b_activities.setEnabled(false);
            b_logOut.setEnabled(false);
        }
        else {
            b_sign_in.setEnabled(false);
        }
    }

//    protected void onStart() {
//        super.onStart();
//
//        //check if a user is logged in, if - yes, then move him to MainMenu
//        SessionManagement sessionManagement = new SessionManagement(MainMenu.this);
//        String un = sessionManagement.getSessionUsername();
//        String pw = sessionManagement.getSessionPassword();
//
//        if (un != null && pw != null ) {
//            Login login = new Login();
//            login.moveToMainMenu();
//        }
//        else {
//
//        }
//    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                if (isGPSEnabled()) {
                    getCurrentLocation();
                }
                else {
                    turnOnGPS();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2) {
            if (resultCode == Activity.RESULT_OK) {
                getCurrentLocation();
            }
        }
    }

    private void getCurrentLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(MainMenu.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (isGPSEnabled()) {
                    LocationServices.getFusedLocationProviderClient(MainMenu.this)
                            .requestLocationUpdates(locationRequest, new LocationCallback() {
                                @Override
                                public void onLocationResult(@NonNull LocationResult locationResult) {
                                    super.onLocationResult(locationResult);

                                    LocationServices.getFusedLocationProviderClient(MainMenu.this)
                                            .removeLocationUpdates(this);

                                    if (locationResult.getLocations().size() > 0){
                                        int index = locationResult.getLocations().size() - 1;
                                        double latitude = locationResult.getLocations().get(index).getLatitude();
                                        double longitude = locationResult.getLocations().get(index).getLongitude();
                                        pos = new LatLng(latitude, longitude);
                                    }
                                }
                            }, Looper.getMainLooper());
                    waitForPosition();
                }
                else {
                    turnOnGPS();
                }
            }
            else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    private void turnOnGPS() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(getApplicationContext())
                .checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    Toast.makeText(MainMenu.this, "GPS is turned on", Toast.LENGTH_SHORT).show();
                }
                catch (ApiException e) {
                    switch (e.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                resolvableApiException.startResolutionForResult(MainMenu.this, 2);
                            } catch (IntentSender.SendIntentException ex) {
                                ex.printStackTrace();
                            }
                            break;

                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            break;
                    }
                }
            }
        });
    }

    private boolean isGPSEnabled() {
        LocationManager locationManager = null;
        boolean isEnabled = false;
        if (locationManager == null) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }
        isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return isEnabled;
    }

    public void openActivities() {
        Intent intent = new Intent(this, FindActivities.class);
        intent.putExtra("Pos", pos);
        startActivity(intent);
    }

    public void openLeisureMap() {
        Intent intent = new Intent(this, LeisureMap.class);
        intent.putExtra("Pos", pos);
        startActivity(intent);
    }

    public void openLogin() {
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
    }

    public boolean checkUserStatus() {
        return getIntent().getBooleanExtra("UserStatus", false);
    }

    public void waitForPosition() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                if(pos == null)
                    waitForPosition();
                else if (i==0){
                    openActivities();
                }
                else {
                    openLeisureMap();
                }

            }
        }, 1000);
    }

    public void logOut(View view) {
        SessionManagement sessionManagement = new SessionManagement(MainMenu.this);
        sessionManagement.removeSession();

        Intent intent = new Intent(MainMenu.this, MainMenu.class);
        //any existing task that are associated with the activity are cleared before the activity is started
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}