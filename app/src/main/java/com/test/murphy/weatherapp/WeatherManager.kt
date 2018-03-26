package com.test.murphy.weatherapp

import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.CustomEvent
import com.test.murphy.weatherapp.model.Dashboard
import com.test.murphy.weatherapp.model.Units
import com.test.murphy.weatherapp.model.WeatherForecast
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class WeatherManager private constructor() {

    private val client = OkHttpClient()

    var forecast: WeatherForecast? = null
    var dashboardInfo: Dashboard? = null

    //Reload weather
    var units = Units.Fahrenheit
        set(requestedUnits) {
            field = requestedUnits
            Answers.getInstance().logCustom(CustomEvent("Units Toggle Tapped").putCustomAttribute("Changed To", this.units.text))
            //No reason to make new network call, just tell UI that weather changed
//            reloadWeather()
            sendWeatherChangedIntent()
        }

    var zip = ""
        set(requestedZip) {
            field = requestedZip

            reloadWeather()
        }

    fun reloadWeather() {
        try {
            loadDashboard()
            loadForecast()
        } catch (e: IOException) {
            //TODO
        }

    }

    //Call BFF to get info for dashboard
    //Temperatures will be returned in F and converted to C in WeatherConditions object if needed
    private fun loadDashboard() {
        val location = this.zip

        val builder = HttpUrl.Builder()
        builder.scheme("http")
                .host("immense-depths-81664.herokuapp.com")
                .addPathSegment("dashboard")
                .addQueryParameter("zip", location)

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
                        dashboardInfo = Dashboard(jsonObject)

                        sendWeatherChangedIntent()
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
                .addQueryParameter("units", Units.Fahrenheit.type) //Always request F and convert to C in UI
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

                        sendWeatherChangedIntent()
                    } catch (e: JSONException) {
                        //TODO
                    }
                }
            }
        })
    }

    private fun sendWeatherChangedIntent() {
        val successIntent = Intent()
        successIntent.action = "android.intent.action.WEATHER_CHANGED"
        LocalBroadcastManager.getInstance(WeatherApp.context).sendBroadcast(successIntent)
    }


    companion object {
        @JvmStatic
        val instance by lazy { WeatherManager() }
    }
}