package com.test.murphy.weatherapp.model

import android.os.Parcel
import android.os.Parcelable
import org.json.JSONException
import org.json.JSONObject

/**
 * Created by wsmurphy on 10/19/17.
 */

class Dashboard() : Parcelable {
    var conditions: WeatherConditions? = null
    var forecast: WeatherForecast? = null
    var fact: String? = null
    var uvIndex: UVIndex? = null

    constructor(parcel: Parcel) : this() {
        conditions = parcel.readParcelable(WeatherConditions::class.java.classLoader)
        fact = parcel.readString()
        uvIndex = parcel.readParcelable(UVIndex::class.java.classLoader)
    }

    constructor(jsonObject: JSONObject) : this() {
        try {
            val weather = jsonObject.getJSONObject("WeatherConditions")
            conditions = WeatherConditions(weather)

            val forecastJSON = jsonObject.getJSONObject("WeatherForecast")
            forecast = WeatherForecast(forecastJSON)

            val factJson = jsonObject.getJSONObject("Fact")
            fact = factJson.getString("value")

            var uviJson = jsonObject.getJSONObject("UVIndex")
            uvIndex = UVIndex(uviJson)

        } catch (je: JSONException) {
            //TODO
        } catch (nfe: NumberFormatException) {
            //TODO
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(conditions, flags)
        parcel.writeString(fact)
        parcel.writeParcelable(uvIndex, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Dashboard> {
        override fun createFromParcel(parcel: Parcel): Dashboard {
            return Dashboard(parcel)
        }

        override fun newArray(size: Int): Array<Dashboard?> {
            return arrayOfNulls(size)
        }
    }
}