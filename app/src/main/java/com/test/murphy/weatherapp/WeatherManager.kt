package com.test.murphy.weatherapp

import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.CustomEvent
import com.test.murphy.weatherapp.model.Units
import com.test.murphy.weatherapp.model.WeatherConditions
import com.test.murphy.weatherapp.model.WeatherForecast
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class WeatherManager private constructor() {
    init { println("This ($this) is a singleton") }

    private val client = OkHttpClient()

    var forecast: WeatherForecast? = null
    var conditions: WeatherConditions? = null

    //Reload weather
    var units = Units.Fahrenheit
        set(requestedUnits) {
            field = requestedUnits
            Answers.getInstance().logCustom(CustomEvent("Units Toggle Tapped").putCustomAttribute("Changed To", this.units.text))
            reloadWeather()
        }

    var zip = ""
        set(requestedZip) {
            field = requestedZip

            reloadWeather()
        }

    fun reloadWeather() {
        try {
            loadWeather()
            loadForecast()
        } catch (e: IOException) {
            //TODO
        }

    }

    private fun loadWeather() {
        val location = this.zip + ",us"

        val builder = HttpUrl.Builder()
        builder.scheme("http")
                .host("api.openweathermap.org")
                .addPathSegment("data")
                .addPathSegment("2.5")
                .addPathSegment("weather")
                .addQueryParameter("zip", location)
                .addQueryParameter("units", this.units.type)
                .addQueryParameter("APPID", "b4608d4fcb4accac0a8cc2ea6949eeb5")

        val request = okhttp3.Request.Builder()
                .url(builder.build())
                .header("Accept", "application/json")
                .build()


        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: okhttp3.Response) {
                response.body()!!.use { responseBody ->
                    if (!response.isSuccessful) throw IOException("Unexpected code " + response)

                    try {
                        val jsonObject = JSONObject(responseBody.string())
                        conditions = WeatherConditions(jsonObject)

                        val successIntent = Intent()
                        successIntent.action = "android.intent.action.WEATHER_CHANGED"
                        LocalBroadcastManager.getInstance(WeatherApp.context).sendBroadcast(successIntent)
                    } catch (e: JSONException) {
                        //TODO
                    }
                }
            }
        })
    }


    @Throws(IOException::class)
    private fun loadForecast() {
        val location = this.zip + ",us"

        val client = OkHttpClient()

        val builder = HttpUrl.Builder()
        builder.scheme("http")
                .host("api.openweathermap.org")
                .addPathSegment("data")
                .addPathSegment("2.5")
                .addPathSegment("forecast")
                .addQueryParameter("zip", location)
                .addQueryParameter("units", this.units.type)
                .addQueryParameter("APPID", "b4608d4fcb4accac0a8cc2ea6949eeb5")

        val request = okhttp3.Request.Builder()
                .url(builder.build())
                .header("Accept", "application/json")
                .build()


        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: okhttp3.Response) {
                response.body()!!.use { responseBody ->
                    if (!response.isSuccessful) throw IOException("Unexpected code " + response)

                    try {
                        val jsonObject = JSONObject(responseBody.string())
                        forecast = WeatherForecast(jsonObject)

                        val successIntent = Intent()
                        successIntent.action = "android.intent.action.WEATHER_CHANGED"
                        LocalBroadcastManager.getInstance(WeatherApp.context).sendBroadcast(successIntent)
                    } catch (e: JSONException) {
                        //TODO
                    }
                }
            }
        })
    }


    companion object {
        @JvmStatic
        val instance by lazy { WeatherManager() }
    }
}