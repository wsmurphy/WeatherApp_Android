package com.test.murphy.weatherapp

import android.app.Activity
import android.os.Bundle
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.answers.Answers
import io.fabric.sdk.android.Fabric


/*
    Potential features:
       - Drive Time: Show drive time to selected destination (daily commute)
       - Let user choose days to show drive time (M-F only, etc)
       - Let user choose time to show drive time (drive at 8 and 5, etc)
       - Link out to google maps to begin drive
       - What to wear (think Swackett) based on conditions (take a jacket!)
 */


class WeatherActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)

        Fabric.with(this, Crashlytics())
        Fabric.with(this, Answers())
    }

    override fun onResume() {
        super.onResume()

        //If the zip code isn't set, resolve from device location
        if (WeatherManager.instance.zip == "") {
            LocationUtils.instance.resolveLocation(this)
            WeatherManager.instance.zip = LocationUtils.instance.zip
        } else {
            WeatherManager.instance.reloadWeather()
        }
    }
}