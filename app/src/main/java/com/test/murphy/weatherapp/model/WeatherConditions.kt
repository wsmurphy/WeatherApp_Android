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
    var currentTemperature: Double? = null

    var conditionCode: Int = 0
    var currentConditions: String? = null
    var location: String? = null
    var iconId: String? = null
    val iconUrl: String
        get() = "http://openweathermap.org/img/w/$iconId.png"

    var date: Date? = null

    constructor(parcel: Parcel) : this() {
        currentTemperature = parcel.readDouble()
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
            currentTemperature = mainObject.getDouble("temp") //There is no separate F and C temp in this API

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
        parcel.writeValue(currentTemperature)
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
}
