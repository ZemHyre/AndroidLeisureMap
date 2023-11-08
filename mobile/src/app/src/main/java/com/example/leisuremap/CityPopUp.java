package com.example.leisuremap;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
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

public class CityPopUp extends AppCompatActivity {

    TextView cityText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_pop_up);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int)(width*0.8), (int)(height*0.6));

        String city = getIntent().getStringExtra("City");
        cityText = findViewById(R.id.cityText);
        cityText.setText(city);

        LinearLayout layout = findViewById(R.id.city_pop_up_layout);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.weight = 1.0f;
        params.gravity = Gravity.CENTER;

        Button weatherButton = new Button(this);
        weatherButton.setLayoutParams(params);
        weatherButton.setText("Weather");
        weatherButton.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        layout.addView(weatherButton);
        layout.setOrientation(LinearLayout.VERTICAL);

        weatherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RequestQueue queue = Volley.newRequestQueue(CityPopUp.this);
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
                        Toast.makeText(CityPopUp.this, "ERROR", Toast.LENGTH_SHORT).show();
                    }
                });
                request.setRetryPolicy(new DefaultRetryPolicy(60000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                queue.add(request);
            }
        });

        Button quickRouteButtonD = new Button(this);
        quickRouteButtonD.setLayoutParams(params);
        quickRouteButtonD.setText("Quickest route (driving)");
        quickRouteButtonD.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        layout.addView(quickRouteButtonD);
        layout.setOrientation(LinearLayout.VERTICAL);

        Button quickRouteButtonW = new Button(this);
        quickRouteButtonW.setLayoutParams(params);
        quickRouteButtonW.setText("Quickest route (walking)");
        quickRouteButtonW.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        layout.addView(quickRouteButtonW);
        layout.setOrientation(LinearLayout.VERTICAL);

        Intent intent = new Intent();
        quickRouteButtonW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(1, intent);
                finish();
            }
        });

        quickRouteButtonD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(2, intent);
                finish();
            }
        });

        if(city.matches("Vilnius")) {
            Button eventsButton = new Button(this);
            eventsButton.setLayoutParams(params);
            eventsButton.setText("Events");
            eventsButton.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            layout.addView(eventsButton);
            layout.setOrientation(LinearLayout.VERTICAL);

            eventsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    RequestQueue queue_events = Volley.newRequestQueue(CityPopUp.this);
                    String url_events = "http://.../events";
                    JsonObjectRequest request_events = new JsonObjectRequest(Request.Method.GET, url_events, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                Intent intent = new Intent(getApplicationContext(), PopWeather.class);
                                StringBuilder s = new StringBuilder("City: Vilnius");
                                JSONArray array = response.getJSONArray("Events");
                                for(int i = 0; i < array.length(); i++) {
                                    JSONObject element = (JSONObject) array.get(i);
                                    String date = element.get("date").toString();
                                    date = androidx.core.text.HtmlCompat.fromHtml(date, HtmlCompat.FROM_HTML_MODE_LEGACY).toString();
                                    String title = element.get("title").toString();
                                    title = androidx.core.text.HtmlCompat.fromHtml(title, HtmlCompat.FROM_HTML_MODE_LEGACY).toString();
                                    String content = element.get("content").toString();
                                    content = androidx.core.text.HtmlCompat.fromHtml(content, HtmlCompat.FROM_HTML_MODE_LEGACY).toString();
                                    s.append("\n--------------------------------\n").append("Date: ").append(date).append("\n\nTitle: ").append(title).append("\n\nContent: \n").append(content);
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
                            Toast.makeText(CityPopUp.this, "ERROR", Toast.LENGTH_SHORT).show();
                        }
                    });
                    request_events.setRetryPolicy(new DefaultRetryPolicy(60000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    queue_events.add(request_events);
                }
            });
        }
    }
}