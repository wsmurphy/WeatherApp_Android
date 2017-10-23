package com.test.murphy.weatherapp;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by wsmurphy on 10/16/17.
 */

public class OWAWeather extends Weather {
    public OWAWeather(JSONObject jsonObject) {
        try {
            JSONObject mainObject = jsonObject.getJSONObject("main");
            currentTemperatureRaw = mainObject.getDouble("temp");
            currentTemperature = currentTemperatureRaw.intValue();

            JSONObject weatherObject = jsonObject.getJSONArray("weather").getJSONObject(0);
            currentConditions = weatherObject.getString("main");

            location = jsonObject.getString("name");

            iconId = weatherObject.getString("icon");
            iconUrl = "http://openweathermap.org/img/w/" + iconId + ".png";
        } catch (JSONException je) {
            //TODO
        }
    }
}
