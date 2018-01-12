package com.test.murphy.weatherapp

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.CustomEvent
import java.io.IOException
import java.util.*

/**
 * Created by wsmurphy on 10/31/17.
 */

class LocationUtils private constructor() {

    private var location: Location? = null

    val zip: String
        get() {
            if (location != null) {
                val gcd = Geocoder(WeatherApp.getContext(), Locale.getDefault())
                val addresses: List<Address>
                try {
                    addresses = gcd.getFromLocation(location!!.latitude, location!!.longitude, 1)
                    if (addresses.isNotEmpty()) {
                        return addresses[0].postalCode
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }

            return ""
        }

    private fun getLastBestLocation(context: Context, activity: Activity): Location? {

        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_LOCATION)
            return null
        } else {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val locationNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

            var locationTimeGPS: Long = 0
            if (locationGPS != null) {
                locationTimeGPS = locationGPS.time
            }

            var locationTimeNet: Long = 0
            if (locationNet != null) {
                locationTimeNet = locationNet.time
            }

            return if (locationTimeGPS < locationTimeNet) locationGPS else locationNet
        }
    }

    fun resolveLocation(activity: Activity) {
        //Resolve current location
        val resolvedLocation = getLastBestLocation(WeatherApp.getContext(), activity)

        if (resolvedLocation == null) {
            Answers.getInstance().logCustom(CustomEvent("Failed to resolve location"))
        }

        this.location = resolvedLocation
    }

    companion object {
        @JvmField
        val REQUEST_LOCATION = 0

        @JvmStatic
        val instance by lazy { LocationUtils() }
    }
}
