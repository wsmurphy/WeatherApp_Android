package com.test.murphy.weatherapp.model

import android.os.Parcel
import android.os.Parcelable
import org.json.JSONException
import org.json.JSONObject

/**
 * Created by wsmurphy on 3/30/18.
 */

class UVIndex() : Parcelable {
    var value: Double? = null
    var stringValue: String? = null
    var colorValue: String? = null

    constructor(parcel: Parcel) : this() {
        value = parcel.readDouble()
        stringValue = parcel.readString()
        colorValue = parcel.readString()
    }

    constructor(jsonObject: JSONObject) : this() {
        try {

            value = jsonObject.getDouble("value")
            stringValue = jsonObject.getString("stringValue")
            colorValue = jsonObject.getString("colorValue")

        } catch (je: JSONException) {
            //TODO
        } catch (nfe: NumberFormatException) {
            //TODO
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(value)
        parcel.writeString(stringValue)
        parcel.writeString(colorValue)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UVIndex> {
        override fun createFromParcel(parcel: Parcel): UVIndex {
            return UVIndex(parcel)
        }

        override fun newArray(size: Int): Array<UVIndex?> {
            return arrayOfNulls(size)
        }
    }
}