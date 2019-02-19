package com.test.murphy.weatherapp

import android.content.Intent
import android.os.Bundle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.AddTrace
import com.test.murphy.weatherapp.model.Dashboard
import com.test.murphy.weatherapp.model.Units
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException



class WeatherManager private constructor() {

    private val client = OkHttpClient()

    var dashboardInfo: Dashboard? = null

    //Reload weather
    var units = Units.Fahrenheit
        set(requestedUnits) {
            field = requestedUnits

            //Log unit change event
            var firebaseAnalytics = FirebaseAnalytics.getInstance(WeatherApp.context)
            val bundle = Bundle()
            bundle.putString("changed_to", this.units.text)
            firebaseAnalytics.logEvent("units_toggle_tapped", bundle)

            //No reason to make new network call, just tell UI that weather changed
            sendWeatherChangedIntent()
        }

    var zip = ""
        set(requestedZip) {
            field = requestedZip

            reloadWeather()
        }

    fun toggleUnits() {
        if (units == Units.Fahrenheit) {
            units = Units.Celsius
        } else {
            units = Units.Fahrenheit
        }
    }

    fun reloadWeather() {
            sendWeatherUpdateStartedIntent()
            loadDashboard()
    }

    //Call BFF to get info for dashboard
    //Temperatures will be returned in F and converted to C in WeatherConditions object if needed
    @AddTrace(name = "loadDashboard", enabled = true /* optional */)
    private fun loadDashboard() {
        val location = this.zip

        //Make sure the zip isn't empty
        //This should probably be done farther up the stack
        if (location == "") {
            sendWeatherUpdateFailedIntent()
            return
        }

        val builder = HttpUrl.Builder()
        builder.scheme("http")
                .host("immense-depths-81664.herokuapp.com")
                .addPathSegment("dashboard")
                .addQueryParameter("zip", location)

        val request = okhttp3.Request.Builder()
                .url(builder.build())
                .header("Accept", "application/json")
                .build()


            val myTrace = FirebasePerformance.getInstance().newTrace("weather_trace")
            myTrace.start()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    myTrace.incrementMetric("weather_failed", 1)
                    myTrace.stop()

                    sendWeatherUpdateFailedIntent()
                    throw e
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: okhttp3.Response) {
                    response.body()!!.use { responseBody ->
                        myTrace.incrementMetric("weather_response", 1)
                        myTrace.stop()

                        if (!response.isSuccessful) {
                            //TODO: Handle exceptions and send failed intent in single place
                            sendWeatherUpdateFailedIntent()
                            throw IOException("Unexpected code " + response)
                        }

                        try {
                            val jsonObject = JSONObject(responseBody.string())
                            dashboardInfo = Dashboard(jsonObject)

                            sendWeatherChangedIntent()
                        } catch (e: JSONException) {
                            sendWeatherUpdateFailedIntent()
                            throw e
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

    private fun sendWeatherUpdateStartedIntent() {
        val startedIntent = Intent()
        startedIntent.action = "android.intent.action.WEATHER_UPDATE_STARTED"
        LocalBroadcastManager.getInstance(WeatherApp.context).sendBroadcast(startedIntent)
    }

    private fun sendWeatherUpdateFailedIntent() {
        val failedIntent = Intent()
        failedIntent.action = "android.intent.action.WEATHER_UPDATE_FAILED"
        LocalBroadcastManager.getInstance(WeatherApp.context).sendBroadcast(failedIntent)
    }


    companion object {
        @JvmStatic
        val instance by lazy { WeatherManager() }
    }
}