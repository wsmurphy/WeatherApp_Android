package com.test.murphy.weatherapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by wsmurphy on 10/19/17.
 */

public class WeatherConditions implements Parcelable {
    public Double currentTemperature;

    public String currentConditions;
    public String location;
    public String iconId;
    public String iconUrl;

    public Date date;

    public WeatherConditions(JSONObject jsonObject) {
        try {
            //Date object will not exist for current conditions, only for forecast
            Long tempDate = jsonObject.optLong("dt");
            if (tempDate != 0) {
                date = new Date(tempDate * 1000);
            }

            JSONObject mainObject = jsonObject.getJSONObject("main");
            currentTemperature = mainObject.getDouble("temp"); //There is no separate F and C temp in this API

            JSONObject weatherObject = jsonObject.getJSONArray("weather").getJSONObject(0);
            currentConditions = weatherObject.getString("main");

            location = jsonObject.getString("name");

            iconId = weatherObject.getString("icon");
            iconUrl = "http://openweathermap.org/img/w/" + iconId + ".png";
        } catch (JSONException je) {
            //TODO
        } catch (NumberFormatException nfe) {
            //TODO
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeDouble(currentTemperature);
        out.writeString(currentConditions);
        out.writeString(location);
        out.writeString(iconId);
        out.writeString(iconUrl);
        out.writeLong(date.getTime());
    }

    public static final Parcelable.Creator<WeatherConditions> CREATOR
            = new Creator<WeatherConditions>() {
        public WeatherConditions createFromParcel(Parcel in) {
            return new WeatherConditions(in);
        }

        public WeatherConditions[] newArray(int size) {
            return new WeatherConditions[size];
        }
    };

    private WeatherConditions(Parcel in) {
        currentTemperature = in.readDouble();
        currentConditions = in.readString();
        location = in.readString();
        iconId = in.readString();
        iconUrl = in.readString();
        date = new Date(in.readLong());
    }
}
