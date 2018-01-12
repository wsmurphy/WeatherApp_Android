package com.test.murphy.weatherapp

import android.app.Application
import android.content.Context

class WeatherApp : Application() {

    override fun onCreate() {
        super.onCreate()

        context = applicationContext
    }

    companion object {

        @JvmStatic
        lateinit var context: Context
            private set

        @JvmStatic
        val instance by lazy { WeatherApp() }
    }


}
