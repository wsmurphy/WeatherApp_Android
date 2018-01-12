package com.test.murphy.weatherapp.model

/**
 * Created by wsmurphy on 1/3/18.
 */

enum class Units(val text: String, val type: String) {
    Fahrenheit("° F", "imperial"),
    Celsius("° C", "metric")
}