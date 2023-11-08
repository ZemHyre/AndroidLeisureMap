package com.example.leisuremap;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ObjectPopUp extends AppCompatActivity {

    Button quickRouteButtonW, quickRouteButtonD, weatherButton, submitButton, favoriteButton;
    RatingBar ratingBar;

    boolean isLoggedIn = false;
    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_object_pop_up);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int) (width * 0.8), (int) (height * 0.7));

        Intent intent = new Intent();
        String city = getIntent().getStringExtra("City");
        String id = getIntent().getStringExtra("Id");
        isLoggedIn = checkUserStatus();
        username = checkUsername();

        ratingBar = findViewById(R.id.rating);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                if(rating<1.0f)
                    ratingBar.setRating(1.0f);
            }
        });

        String username = getIntent().getStringExtra("Username");

        submitButton = findViewById(R.id.submitRating);
        if(username == null)
            submitButton.setEnabled(false);
        else {
            submitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    float rating = ratingBar.getRating();
                    RequestQueue queue = Volley.newRequestQueue(ObjectPopUp.this);
                    String url = "http://.../rateplace?id=" + id + "&rating=" + rating;
                    JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                String status = response.get("STATUS").toString();
                                Toast.makeText(ObjectPopUp.this, status, Toast.LENGTH_SHORT).show();
                                submitButton.setEnabled(false);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(ObjectPopUp.this, "Connection problem", Toast.LENGTH_SHORT).show();
                        }
                    });
                    request.setRetryPolicy(new DefaultRetryPolicy(60000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    queue.add(request);
                }
            });
        }

        quickRouteButtonW = findViewById(R.id.quickRouteW);
        quickRouteButtonW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(1, intent);
                finish();
            }
        });

        quickRouteButtonD = findViewById(R.id.quickRouteD);
        quickRouteButtonD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(2, intent);
                finish();
            }
        });

        weatherButton = findViewById(R.id.weather);
        weatherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RequestQueue queue = Volley.newRequestQueue(ObjectPopUp.this);
                String url ="http://.../weather?city=" + city;

                JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject city = response.getJSONObject("place");
                            String cityName = city.get("name").toString();
                            JSONArray array = response.getJSONArray("forecastTimestamps");
                            Intent intent = new Intent(getApplicationContext(), PopWeather.class);
                            StringBuilder s = new StringBuilder("City: " + cityName);
                            for(int i = 0; i < array.length(); i++) {
                                JSONObject forecast = (JSONObject) array.get(i);
                                String forecastTimeUtc = forecast.get("forecastTimeUtc").toString();
                                String airTemperature = forecast.get("airTemperature").toString();
                                String windSpeed = forecast.get("windSpeed").toString();
                                String cloudCover = forecast.get("cloudCover").toString();
                                String conditionCode = forecast.get("conditionCode").toString();
                                s.append("\n--------------------------------\n").append("Forecast Time:\n").append(forecastTimeUtc).append("\nAir Temperature: ").append(airTemperature).append("\nWind Speed: ").append(windSpeed).append("\nCloud Cover: ").append(cloudCover).append("\nCondition Code: ").append(conditionCode);
                            }
                            intent.putExtra("Weather", s.toString());
                            startActivity(intent);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ObjectPopUp.this, "ERROR", Toast.LENGTH_SHORT).show();
                    }
                });
                request.setRetryPolicy(new DefaultRetryPolicy(60000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                queue.add(request);
            }
        });

        favoriteButton = findViewById(R.id.favorite);
        if(username == null)
            favoriteButton.setEnabled(false);
        else {
            favoriteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    RequestQueue queue = Volley.newRequestQueue(ObjectPopUp.this);
                    String url = "http://.../saveplace?username=" + username + "&id=" + id;
                    JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                String status = response.get("STATUS").toString();
                                Toast.makeText(ObjectPopUp.this, status, Toast.LENGTH_SHORT).show();
                                favoriteButton.setEnabled(false);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(ObjectPopUp.this, "Connection problem", Toast.LENGTH_SHORT).show();
                        }
                    });
                    request.setRetryPolicy(new DefaultRetryPolicy(60000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    queue.add(request);
                }
            });
        }
    }

    public boolean checkUserStatus() {
        return getIntent().getBooleanExtra("UserStatus", false);
    }
    public String checkUsername() {
        return getIntent().getStringExtra("Username");
    }
}