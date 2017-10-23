package com.test.murphy.weatherapp.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by wsmurphy on 10/16/17.
 */

public class WUWeather extends Weather {
    public WUWeather(JSONObject jsonObject) {
        try {
            JSONObject currentObservation = jsonObject.getJSONObject("current_observation");

            currentTemperatureF = currentObservation.getDouble("temp_f");
            currentTemperatureC = currentObservation.getDouble("temp_c");

            currentConditions = currentObservation.getString("weather");

            JSONObject locationObject = currentObservation.getJSONObject("display_location");
            location = locationObject.getString("city"); //Taking city name, could also take full name with State

            iconId = currentObservation.getString("icon");
            iconUrl = "https://icons.wxug.com/i/c/i/" + iconId + ".gif";
        } catch (JSONException je) {
            //TODO
        }
    }
}
