package com.example.leisuremap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainMenu extends AppCompatActivity {

    private Button b_map, b_activities, b_sign_in, b_logOut, b_favorite_places, b_similar_users;
    private LocationRequest locationRequest;
    private LatLng pos;
    boolean isLoggedIn = false;
    String username;
    public static Activity fa;
    int i; //activity type
    ArrayList<String> clickedObjectId = new ArrayList<>();
    ArrayList <String> clickedObjectType = new ArrayList<>();

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

        b_favorite_places = findViewById(R.id.favoritePlaces);
        b_favorite_places.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(pos == null){
                    getCurrentLocation();
                    i = 2;
                }
                else
                    openFavoritePlaces();
            }
        });

        b_similar_users = findViewById(R.id.similarUsers);
        b_similar_users.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(pos == null){
                    getCurrentLocation();
                    i = 3;
                }
                else
                    openSimilarUserRec();
            }
        });

        b_sign_in = findViewById(R.id.signIn);
        b_sign_in.setOnClickListener(view -> openLogin());
        b_logOut = findViewById(R.id.logOut);

        b_logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logOut();
                String startTime = getIntent().getStringExtra("startTime");
                SimpleDateFormat formatter= new SimpleDateFormat("HH:mm");
                Date endTime = new Date(System.currentTimeMillis());
                username = checkUsername();

                JSONObject jsonObject = new JSONObject();
                JSONArray jsonArrayId = new JSONArray();
                JSONArray jsonArrayType = new JSONArray();

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            URL url = new URL("http://.../session" );
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            conn.setRequestMethod("POST");
                            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                            conn.setRequestProperty("Accept","application/json");
                            conn.setDoOutput(true);
                            conn.setDoInput(true);

                            jsonObject.put("username", username);
                            jsonObject.put("start", startTime);
                            jsonObject.put("end", formatter.format(endTime));

                            for (String o : clickedObjectId) {
                                jsonArrayId.put(o);
                            }

                            for (String o : clickedObjectType) {
                                jsonArrayType.put(o);
                            }
                            jsonObject.put("places", jsonArrayId);
                            jsonObject.put("tags", jsonArrayType);

                            Log.i("JSON", jsonObject.toString());
                            DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                            os.writeBytes(jsonObject.toString());

                            os.flush();
                            os.close();

                            Log.i("STATUS", String.valueOf(conn.getResponseCode()));
                            Log.i("MSG" , conn.getResponseMessage());

                            conn.disconnect();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                thread.start();
            }
        });

        isLoggedIn = checkUserStatus();
        username = checkUsername();

        if (!isLoggedIn) {
            b_activities.setEnabled(false);
            b_logOut.setEnabled(false);
            b_favorite_places.setEnabled(false);
            b_similar_users.setEnabled(false);
        }
        else {
            b_sign_in.setEnabled(false);
        }

        if (!isOnline()) {
            try {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainMenu.this);
                alertDialogBuilder.setTitle("Error")
                        .setMessage("Network connection is unavailable, double check your connectivity and try again.")
                        .setCancelable(false)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(" Enable Connection ", new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id){
                                Intent dialogIntent = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
                                dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                MainMenu.this.startActivity(dialogIntent);
                            }
                        });

                alertDialogBuilder.setNegativeButton(" OK ", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        dialog.cancel();
                    }
                });
                AlertDialog alert = alertDialogBuilder.create();
                alert.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isOnline() {
        ConnectivityManager conMgr = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMgr.getActiveNetworkInfo();

        if(netInfo == null || !netInfo.isConnected() || !netInfo.isAvailable()){
            Toast.makeText(MainMenu.this, "No Internet connection!", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

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
        if(requestCode == 0) {
            clickedObjectId.addAll(data.getStringArrayListExtra("result1"));
            clickedObjectType.addAll(data.getStringArrayListExtra("result2"));
        }
        if(requestCode == 1) {
            clickedObjectId.addAll(data.getStringArrayListExtra("result3"));
            clickedObjectType.addAll(data.getStringArrayListExtra("result4"));
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
                    Toast.makeText(MainMenu.this, "Getting current location. Please wait.", Toast.LENGTH_SHORT).show();
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
        intent.putExtra("UserPos", pos);
        intent.putExtra("UserStatus", isLoggedIn);
        intent.putExtra("Username", username);
        intent.putExtra("oID", clickedObjectId);
        intent.putExtra("oType", clickedObjectType);
        startActivityForResult(intent, 1);

    }

    public void openLeisureMap() {
        Intent intent = new Intent(this, LeisureMap.class);
        String m = "menu";
        intent.putExtra("Parent", m);
        intent.putExtra("UserPos", pos);
        intent.putExtra("UserStatus", isLoggedIn);
        intent.putExtra("Username", username);
        intent.putStringArrayListExtra("oID", clickedObjectId);
        intent.putStringArrayListExtra("oType", clickedObjectType);
        startActivityForResult(intent, 0);
    }


    public void openLogin() {
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
    }

    public void openSimilarUserRec() {
        Intent intent = new Intent(this, SimilarUserRecommendations.class);
        intent.putExtra("UserStatus", isLoggedIn);
        intent.putExtra("Username", username);
        intent.putExtra("UserPos", pos);
        startActivity(intent);
    }

    public void openFavoritePlaces() {
        Intent intent = new Intent(this, FavoritePlaces.class);
        intent.putExtra("UserPos", pos);
        intent.putExtra("UserStatus", isLoggedIn);
        intent.putExtra("Username", username);
        startActivity(intent);
    }

    public boolean checkUserStatus() {
        return getIntent().getBooleanExtra("UserStatus", false);
    }
    public String checkUsername() {
        return getIntent().getStringExtra("Username");
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
                else if (i==1){
                    openLeisureMap();
                }
                else if (i==2)
                    openFavoritePlaces();
                else
                    openSimilarUserRec();
            }
        }, 1000);
    }

    public void logOut() {
        SessionManagement sessionManagement = new SessionManagement(MainMenu.this);
        sessionManagement.removeSession();
        Intent intent = new Intent(MainMenu.this, MainMenu.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent(this, ExitService.class);
        intent.putExtra("Username", username);
        intent.putExtra("startTime", getIntent().getStringExtra("startTime"));
        intent.putStringArrayListExtra("ID", clickedObjectId);
        intent.putStringArrayListExtra("Type", clickedObjectType);
        startService(intent);
    }
}
