package com.test.murphy.weatherapp;

import com.test.murphy.weatherapp.model.WeatherConditions;
import com.test.murphy.weatherapp.model.WeatherForecast;

/**
 * Created by wsmurphy on 10/27/17.
 */

public interface ConnectionsDelegate {
    void weatherSuccess(WeatherConditions conditions);
    void forecastSuccess(WeatherForecast forecast);
}