package com.test.murphy.weatherapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by wsmurphy on 10/23/17.
 */

public class WeatherForecast implements Parcelable {
    public String location;
    public WeatherConditions[] forecast;

    public WeatherForecast(JSONObject jsonObject) {
        try {
            location = jsonObject.getString("city");

            JSONArray list = jsonObject.getJSONArray("list");

            forecast = new WeatherConditions[list.length()];
            for (int i = 0; i < list.length(); i++) {
                JSONObject item = list.getJSONObject(i);
                WeatherConditions conditions = new WeatherConditions(item);
                forecast[i] = conditions;
            }
        } catch (JSONException je) {
            //TODO: Handle exception
        }

    }

    @Override
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(location);
        out.writeTypedArray(forecast, 0);
    }

    public static final Parcelable.Creator<WeatherForecast> CREATOR
            = new Parcelable.Creator<WeatherForecast>() {
        public WeatherForecast createFromParcel(Parcel in) {
            return new WeatherForecast(in);
        }

        public WeatherForecast[] newArray(int size) {
            return new WeatherForecast[size];
        }
    };

    private WeatherForecast(Parcel in) {
        location = in.readString();
        forecast = in.createTypedArray(WeatherConditions.CREATOR);
    }
}
