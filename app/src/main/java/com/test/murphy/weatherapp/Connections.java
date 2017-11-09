package com.test.murphy.weatherapp;

import android.content.Context;

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

public class Connections {

    private static Connections mInstance;

    private Context mContext;
    private OkHttpClient client = new OkHttpClient();
    private ConnectionsDelegate delegate;


    public Connections(final Context context) {
        mContext = context;
    }

    public static Connections getInstance(final Context context) {
        if (mInstance == null) {
            mInstance = new Connections(context);
        }
        return mInstance;
    }

    //TODO: This pattern seems weird in Java
    //Am i forcing my iOS patterns on Android?
    public void setDelegate(ConnectionsDelegate delegate) {
        this.delegate = delegate;
    }

    public void getWeather(String zip, Units units) {
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
                        WeatherConditions weather = new WeatherConditions(jsonObject);
                        delegate.weatherSuccess(weather);
                    } catch (JSONException e) {
                        //TODO
                    }
                }
            }
        });
    }


    public void getForecast(String zip, Units units) throws IOException {
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
                        WeatherForecast forecast = new WeatherForecast(jsonObject);
                        delegate.forecastSuccess(forecast);
                    } catch (JSONException e) {
                        //TODO
                    }
                }
            }
        });
    }

}
