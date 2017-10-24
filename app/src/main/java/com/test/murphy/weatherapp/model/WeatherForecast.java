package com.test.murphy.weatherapp.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by wsmurphy on 10/23/17.
 */

public class WeatherForecast {
    public String location;
    public ArrayList<WeatherConditions> forecast;

    public WeatherForecast(JSONObject jsonObject) {
        try {
            location = jsonObject.getString("city");

            JSONArray list = jsonObject.getJSONArray("list");

            forecast = new ArrayList<WeatherConditions>(list.length());
            for (int i = 0; i < list.length(); i++) {
                JSONObject item = list.getJSONObject(i);
                WeatherConditions conditions = new WeatherConditions(item);
                forecast.add(conditions);
            }
        } catch (JSONException je) {
            //TODO: Handle exception
        }

    }
}
