package com.test.murphy.weatherapp.model

import android.os.Parcel
import android.os.Parcelable
import org.json.JSONException
import org.json.JSONObject
import java.util.*



/**
 * Created by wsmurphy on 10/19/17.
 */

class WeatherConditions() : Parcelable {
    var currentTemperatureF: Double? = null
    var currentTemperatureC: Double? = null

    var conditionCode: Int = 0
    var currentConditions: String? = null
    var location: String? = null
    var iconId: String? = null
    val iconUrl: String
        get() = "http://openweathermap.org/img/w/$iconId.png"

    var date: Date? = null

    constructor(parcel: Parcel) : this() {
        currentTemperatureF = parcel.readDouble()
        currentTemperatureC = parcel.readDouble()
        conditionCode = parcel.readInt()
        currentConditions = parcel.readString()
        location = parcel.readString()
        iconId = parcel.readString()
    }

    constructor(jsonObject: JSONObject) : this() {
        try {
            //Date object will not exist for current conditions, only for forecast
            val tempDate = jsonObject.optLong("dt")
            if (tempDate != 0L) {
                date = Date(tempDate * 1000)
            }

            val mainObject = jsonObject.getJSONObject("main")

            //There is no separate F and C temp in this API
            //We request F and convert to C so that we don't need to make a second network call when the user switches the units
            currentTemperatureF = mainObject.getDouble("temp")
            currentTemperatureF?.let {
                currentTemperatureC = ftoc(it)
            }

            val weatherObject = jsonObject.getJSONArray("weather").getJSONObject(0)
            currentConditions = weatherObject.getString("main")
            conditionCode = weatherObject.getInt("id")

            location = jsonObject.getString("name")

            iconId = weatherObject.getString("icon")

        } catch (je: JSONException) {
            //TODO
        } catch (nfe: NumberFormatException) {
            //TODO
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(currentTemperatureF)
        parcel.writeValue(currentTemperatureC)
        parcel.writeInt(conditionCode)
        parcel.writeString(currentConditions)
        parcel.writeString(location)
        parcel.writeString(iconId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<WeatherConditions> {
        override fun createFromParcel(parcel: Parcel): WeatherConditions {
            return WeatherConditions(parcel)
        }

        override fun newArray(size: Int): Array<WeatherConditions?> {
            return arrayOfNulls(size)
        }
    }

    //Convert fahrenheit to celsius
    private fun ftoc(f: Double): Double {
        return f - 32 / 1.8
    }
}
