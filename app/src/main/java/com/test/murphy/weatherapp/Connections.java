package com.test.murphy.weatherapp;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
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

    private static ConnectionsDelegate delegate;

    private static Connections mInstance;

    private Context mContext;
    private RequestQueue mQueue;

    public Connections(final Context context) {
        mContext = context;
        mQueue = getRequestQueue();
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

    public RequestQueue getRequestQueue(){
        // If RequestQueue is null the initialize new RequestQueue
        if(mQueue == null){
            mQueue = Volley.newRequestQueue(mContext.getApplicationContext());
        }

        // Return RequestQueue
        return mQueue;
    }


    public void getWeather(String zip, Units units) {
        String BASE_URL = "http://api.openweathermap.org/data/2.5/weather?zip=";
        String location = zip + ",us";
        String appId = "&APPID=b4608d4fcb4accac0a8cc2ea6949eeb5";
        String unitsPart = "&units=" + units.getType();

        String url = BASE_URL + location + appId + unitsPart;

        JsonObjectRequest jsonRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        WeatherConditions weather = new WeatherConditions(response);
                        delegate.weatherSuccess(weather);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {}
                });

        // Add the request to the RequestQueue.
        mQueue.add(jsonRequest);
    }

//    public void getForecastVolley(String zip, Units units) {
//        String BASE_URL = "http://api.openweathermap.org/data/2.5/forecast?zip=";
//        String location = zip + ",us";
//        String appId = "&APPID=b4608d4fcb4accac0a8cc2ea6949eeb5";
//        String unitsPart = "&units=" + units.getType();
//
//        String url = BASE_URL + location + appId + unitsPart;
//
//        JsonObjectRequest jsonRequest = new JsonObjectRequest
//                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
//                    @Override
//                    public void onResponse(JSONObject response) {
//                        WeatherForecast forecast = new WeatherForecast(response);
//                        delegate.forecastSuccess(forecast);
//                    }
//                }, new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {}
//                });
//
//        // Add the request to the RequestQueue.
//        mQueue.add(jsonRequest);
//    }


    public void getForecastOkHttp(String zip, Units units) throws IOException {
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
