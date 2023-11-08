package com.example.leisuremap;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FavoritePlaces extends AppCompatActivity {

    List<Object> objects = new ArrayList<>();
    LatLng userPos;

    boolean isLoggedIn = false;
    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_places);

        userPos = getIntent().getParcelableExtra("UserPos");
        isLoggedIn = checkUserStatus();
        username = checkUsername();

        RequestQueue queue_objects = Volley.newRequestQueue(FavoritePlaces.this);
        String url_objects = "http://.../getfavourite?username=" + username;
        JsonObjectRequest request_objects = new JsonObjectRequest(Request.Method.GET, url_objects, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray array = response.getJSONArray("Places");
                    for(int i = 0; i < array.length(); i++) {
                        JSONObject element = (JSONObject) array.get(i);
                        String latitude = element.get("lat").toString();
                        String longitude = element.get("lon").toString();
                        double lon = Double.valueOf(longitude);
                        double lat = Double.valueOf(latitude);
                        LatLng pos = new LatLng(lat, lon);
                        String objType = element.get("type").toString();
                        String objName = element.get("name").toString();

                        if(objName.equals("null"))
                            objName = objType;

                        String objCity = element.get("city").toString();

                        Location startPoint = new Location("UserPos");
                        if(userPos == null) {
                            startPoint.setLatitude(0);
                            startPoint.setLongitude(0);
                        }
                        else {
                            startPoint.setLatitude(userPos.latitude);
                            startPoint.setLongitude(userPos.longitude);
                        }

                        Location endPoint = new Location("Object");
                        endPoint.setLatitude(pos.latitude);
                        endPoint.setLongitude(pos.longitude);

                        double distance=startPoint.distanceTo(endPoint) / 1000;

                        Object obj = new Object(objName, distance, objCity, objType, lat, lon);
                        objects.add(obj);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                sortList();
                for(Object o:objects) {
                    createTextViews(o);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(FavoritePlaces.this, "ERROR", Toast.LENGTH_SHORT).show();
            }
        });
        request_objects.setRetryPolicy(new DefaultRetryPolicy(60000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue_objects.add(request_objects);
    }

    public void createTextViews(Object object) {
        LatLng pos = new LatLng(object.getLat(), object.getLon());
        LinearLayout linearLayout = findViewById(R.id.linearLayout);
        TextView textView = new TextView(FavoritePlaces.this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(1000, 300);
        layoutParams.gravity = Gravity.CENTER;
        textView.setLayoutParams(layoutParams);
        layoutParams.setMargins(10, 10, 10, 10);
        textView.setBackgroundDrawable(getResources().getDrawable(R.drawable.pop_up_style));
        textView.setTextColor(Color.BLACK);
        String formatDist = String.format("%.1f", object.getDistance());
        textView.setText("Name: " + object.getName() + "\nDistance: " + formatDist + " km, " + "City: " + object.getCity() + ", Type: " + object.getType());
        textView.setPadding(20, 20, 20, 20);
        textView.setGravity(Gravity.CENTER);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), LeisureMap.class);
                intent.putExtra("Position", pos);
                intent.putExtra("UserPos", userPos);
                intent.putExtra("Username", username);
                intent.putExtra("UserStatus", isLoggedIn);
                startActivityForResult(intent, 5);
            }
        });

        linearLayout.addView(textView);
    }

    public void sortList() {
        Collections.sort(objects, new Comparator<Object>() {
            public int compare(Object o1, Object o2) {
                return Double.compare(o2.getScore(), o1.getScore());
            }
        });
    }

    public boolean checkUserStatus() {
        return getIntent().getBooleanExtra("UserStatus", false);
    }
    public String checkUsername() {
        return getIntent().getStringExtra("Username");
    }
}
