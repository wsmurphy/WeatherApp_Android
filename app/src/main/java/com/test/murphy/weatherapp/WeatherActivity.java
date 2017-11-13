package com.test.murphy.weatherapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;

import io.fabric.sdk.android.Fabric;


/*
    Potential features:
       - Drive Time: Show drive time to selected destination (daily commute)
       - Let user choose days to show drive time (M-F only, etc)
       - Let user choose time to show drive time (drive at 8 and 5, etc)
       - Link out to google maps to begin drive
       - What to wear (think Swackett) based on conditions (take a jacket!)
 */


public class WeatherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        Fabric.with(this, new Crashlytics());
        Fabric.with(this, new Answers());
    }

    @Override
    protected void onResume() {
        super.onResume();

        //If the zip code isn't set, resolve from device location
        if (WeatherManager.getInstance().getZip().equals("")) {
            LocationUtils.getInstance().resolveLocation(this);
            WeatherManager.getInstance().setZip(LocationUtils.getInstance().getZip());
        } else {
            WeatherManager.getInstance().reloadWeather();
        }
    }
}