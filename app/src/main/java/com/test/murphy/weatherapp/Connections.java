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

import org.json.JSONObject;

/**
 * Created by wsmurphy on 10/25/17.
 */


public class Connections {

    private static ConnectionsDelegate delegate;

    private static Context mContext;
    private static Connections mInstance;
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

    public void getForecast(String zip, Units units) {
        String BASE_URL = "http://api.openweathermap.org/data/2.5/forecast?zip=";
        String location = zip + ",us";
        String appId = "&APPID=b4608d4fcb4accac0a8cc2ea6949eeb5";
        String unitsPart = "&units=" + units.getType();

        String url = BASE_URL + location + appId + unitsPart;

        JsonObjectRequest jsonRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        WeatherForecast forecast = new WeatherForecast(response);
                        delegate.forecastSuccess(forecast);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {}
                });

        // Add the request to the RequestQueue.
        mQueue.add(jsonRequest);
    }
}
