package com.test.murphy.weatherapp.model

import android.os.Parcel
import android.os.Parcelable
import org.json.JSONObject
import java.util.*

/**
 * Created by wsmurphy on 10/23/17.
 */


class WeatherForecast(var location: String, var forecast: Array<WeatherConditions>) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.createTypedArray(WeatherConditions.CREATOR))

    constructor(jsonObject: JSONObject) : this(
            jsonObject.getString("city"),
            WeatherForecast.parseForecastJSON(jsonObject))

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(location)
        parcel.writeTypedArray(forecast, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<WeatherForecast> {
        override fun createFromParcel(parcel: Parcel): WeatherForecast {
            return WeatherForecast(parcel)
        }

        override fun newArray(size: Int): Array<WeatherForecast?> {
            return arrayOfNulls(size)
        }

        fun parseForecastJSON(jsonObject: JSONObject) : Array<WeatherConditions> {
            val list = jsonObject.getJSONArray("list")

            var forecast: ArrayList<WeatherConditions> = ArrayList(list.length())

            for (i in 0 until list.length()) {
                val item = list.getJSONObject(i)
                val conditions = WeatherConditions(item)
                forecast.add(conditions)
            }

            return forecast.toTypedArray()
        }
    }
}
