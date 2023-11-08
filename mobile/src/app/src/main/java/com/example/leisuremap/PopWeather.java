package com.example.leisuremap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.widget.TextView;

public class PopWeather extends Activity {

    TextView weatherText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pop_weather);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int)(width*0.8), (int)(height*0.5));

        weatherText = findViewById(R.id.weatherInfo);
        Intent intent = getIntent();
        String str = intent.getStringExtra("Weather");
        weatherText.setText(str);
        weatherText.setMovementMethod(new ScrollingMovementMethod());
    }
}