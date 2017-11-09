package com.test.murphy.weatherapp.model;

/**
 * Created by wsmurphy on 10/25/17.
 */

public enum Units {
        Fahrenheit("° F", "imperial"),
        Celsius("° C", "metric");

        private String text;
        private String type;

        Units(String text, String type) {
            this.text = text;
            this.type = type;
        }

    public String getText() {
        return text;
    }

    public String getType() {
        return type;
    }

}
