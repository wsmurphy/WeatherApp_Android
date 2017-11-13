package com.test.murphy.weatherapp;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.test.murphy.weatherapp.model.Units;
import com.test.murphy.weatherapp.model.WeatherConditions;
import com.test.murphy.weatherapp.model.WeatherForecast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;

/**
 * Created by wsmurphy on 10/25/17.
 */

public class WeatherManager {

    private static WeatherManager mInstance;

    private OkHttpClient client = new OkHttpClient();

    public WeatherForecast forecast;
    public WeatherConditions conditions;
    private Units units = Units.Fahrenheit;

    private String zip = "";

    public WeatherManager() {}

    public static WeatherManager getInstance() {
        if (mInstance == null) {
            mInstance = new WeatherManager();
        }
        return mInstance;
    }

    public void setZip(String requestedZip) {
        zip = requestedZip;

        reloadWeather();
    }

    public String getZip() {
        return zip;
    }

    public void setUnits(Units requestedUnits) {
        units = requestedUnits;
        Answers.getInstance().logCustom(new CustomEvent("Units Toggle Tapped").putCustomAttribute("Changed To", units.getText()));

        //Reload weather
        reloadWeather();
    }

    public void reloadWeather() {
        try {
            getWeather();
            getForecast();
        } catch (IOException e) {
            //TODO
        }

    }

    public Units getUnits() {
        return units;
    }

    public void getWeather() {
        String location = zip + ",us";

        HttpUrl.Builder builder = new HttpUrl.Builder();
        builder.scheme("http")
                .host("api.openweathermap.org")
                .addPathSegment("data")
                .addPathSegment("2.5")
                .addPathSegment("weather")
                .addQueryParameter("zip", location)
                .addQueryParameter("units", units.getType())
                .addQueryParameter("APPID","b4608d4fcb4accac0a8cc2ea6949eeb5");

        final okhttp3.Request request = new okhttp3.Request.Builder()
                .url(builder.build())
                .header("Accept", "application/json")
                .build();


        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override public void onResponse(Call call, okhttp3.Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                    try {
                        JSONObject jsonObject = new JSONObject(responseBody.string());
                        conditions = new WeatherConditions(jsonObject);

                        Intent successIntent = new Intent();
                        successIntent.setAction("android.intent.action.WEATHER_CHANGED");
                        LocalBroadcastManager.getInstance(WeatherApp.getContext()).sendBroadcast(successIntent);
                    } catch (JSONException e) {
                        //TODO
                    }
                }
            }
        });
    }


    public void getForecast() throws IOException {
        String location = zip + ",us";

        final OkHttpClient client = new OkHttpClient();

        HttpUrl.Builder builder = new HttpUrl.Builder();
        builder.scheme("http")
                .host("api.openweathermap.org")
                .addPathSegment("data")
                .addPathSegment("2.5")
                .addPathSegment("forecast")
                .addQueryParameter("zip", location)
                .addQueryParameter("units", units.getType())
        .addQueryParameter("APPID","b4608d4fcb4accac0a8cc2ea6949eeb5");

        final okhttp3.Request request = new okhttp3.Request.Builder()
        .url(builder.build())
        .header("Accept", "application/json")
        .build();


        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
            }

            @Override public void onResponse(Call call, okhttp3.Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                    try {
                        JSONObject jsonObject = new JSONObject(responseBody.string());
                        forecast = new WeatherForecast(jsonObject);

                        Intent successIntent = new Intent();
                        successIntent.setAction("android.intent.action.WEATHER_CHANGED");
                        LocalBroadcastManager.getInstance(WeatherApp.getContext()).sendBroadcast(successIntent);
                    } catch (JSONException e) {
                        //TODO
                    }
                }
            }
        });
    }

}
