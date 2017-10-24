package com.test.murphy.weatherapp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.test.murphy.weatherapp.model.WeatherConditions;
import com.test.murphy.weatherapp.model.WeatherForecast;

import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;


public class WeatherActivity extends AppCompatActivity {

    @BindView(R.id.temperatureText) TextView temperatureText;
    @BindView(R.id.conditionsText) TextView conditionsText;
    @BindView(R.id.locationText) TextView locationText;
    @BindView(R.id.locationButton) Button locationButton;
    @BindView(R.id.unitsToggle) ToggleButton unitsToggle;
    @BindView(R.id.forecastGrid) GridLayout gridLayout;

    private String zip = "";

    LocationManager locationManager;

    private static final int REQUEST_LOCATION = 0;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("h aa");

    private Units units = Units.Farenheight;

    private enum Units {
        Farenheight, Celcius;
    }

    private String getUnitsText() {
        switch (this.units) {
            case Farenheight:
                return "° F";
            case Celcius:
                return "° C";
            default:
                return "";
        }
    }

    private String getUnitsType() {
        switch (this.units) {
            case Farenheight:
                return "imperial";
            case Celcius:
                return "metric";
            default:
                return "";
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        ButterKnife.bind(this);

        //If the zip code isn't set, resolve from device location
        if (zip == "") {
            resolveLocation();
        }

        getWeather(zip);
        getForecast(zip);
    }

    void updateViewFields(WeatherConditions weatherConditions) {
        temperatureText.setText(String.valueOf(weatherConditions.currentTemperature) + getUnitsText());
        conditionsText.setText(weatherConditions.currentConditions);
        locationText.setText(weatherConditions.location);
    }

    @OnClick(R.id.locationButton)
    public void locationButtonPressed() {
        //Display prompt to update location
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Location");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(80, 0, 80, 0); //TODO: How far to inset?


        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        layout.addView(input, params);

        builder.setView(layout);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //TODO: Edit checks on zip to ensure 5 digits
                zip = input.getText().toString();
                getWeather(zip);
                getForecast(zip);
            }
        });

        builder.setNeutralButton("Use Current Location", new DialogInterface.OnClickListener() {
           @Override
           public void onClick(DialogInterface dialog, int which) {
                //Resolve GPS\Network location
               resolveLocation();
           }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private Location getLastBestLocation() {
        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.ACCESS_COARSE_LOCATION  },
                    REQUEST_LOCATION );
        }

        if ( ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.ACCESS_FINE_LOCATION  },
                    REQUEST_LOCATION );
        }

        Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location locationNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        long GPSLocationTime = 0;
        if (null != locationGPS) { GPSLocationTime = locationGPS.getTime(); }

        long NetLocationTime = 0;

        if (null != locationNet) {
            NetLocationTime = locationNet.getTime();
        }

        if ( 0 < GPSLocationTime - NetLocationTime ) {
            return locationGPS;
        }
        else {
            return locationNet;
        }
    }

    private String getZipFromLocation(Location location) {
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = gcd.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses.size() > 0) {
                return addresses.get(0).getPostalCode();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    private void resolveLocation() {
        locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);

        //Resolve current location
        Location lastBest = getLastBestLocation();
        if (lastBest != null) {
            String lastZip = getZipFromLocation(lastBest);
            zip = lastZip;
        }
    }

    @OnCheckedChanged(R.id.unitsToggle)
    public void toggleClicked() {
        if (!unitsToggle.isChecked()) {
            units = Units.Farenheight;
        } else {
            units = Units.Celcius;
        }

        //After settings changed, reload weather
        getWeather(zip);
        getForecast(zip);
    }

    void getWeather(String zip) {
        String BASE_URL = "http://api.openweathermap.org/data/2.5/weather?zip=";
        String location = zip + ",us";
        String appId = "&APPID=b4608d4fcb4accac0a8cc2ea6949eeb5";
        String units = "&units=" + getUnitsType();

        final RequestQueue queue = Volley.newRequestQueue(this);
        String url = BASE_URL + location + appId + units;

        JsonObjectRequest jsonRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        WeatherConditions weather = new WeatherConditions(response);
                        updateViewFields(weather);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {}
                });

        // Add the request to the RequestQueue.
        queue.add(jsonRequest);
    }

    void getForecast(String zip) {
        String BASE_URL = "http://api.openweathermap.org/data/2.5/forecast?zip=";
        String location = zip + ",us";
        String appId = "&APPID=b4608d4fcb4accac0a8cc2ea6949eeb5";
        String units = "&units=" + getUnitsType();

        final RequestQueue queue = Volley.newRequestQueue(this);
        String url = BASE_URL + location + appId + units;

        JsonObjectRequest jsonRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        WeatherForecast forecast = new WeatherForecast(response);

                        gridLayout.setColumnCount(3);
                        gridLayout.setRowCount(3);
                        gridLayout.setOrientation(GridLayout.VERTICAL);
                        gridLayout.removeAllViewsInLayout();

                        for (int i = 0; i < 3; i++) {
                            WeatherConditions weather = forecast.forecast.get(i);

                            TextView forecastConditions = new TextView(getApplicationContext());
                            forecastConditions.setText(weather.currentConditions);
                            forecastConditions.setTextColor(Color.WHITE);

                            TextView forecastTemp = new TextView(getApplicationContext());
                            forecastTemp.setText(weather.currentTemperature.toString() + getUnitsText());
                            forecastTemp.setTextColor(Color.WHITE);

                            TextView forecastTime = new TextView(getApplicationContext());
                            String formattedDate = dateFormat.format(weather.date).toString();
                            forecastTime.setText(formattedDate);
                            forecastTime.setTextColor(Color.WHITE);

                            //TODO: Fix layout to spread evenly
                            gridLayout.addView(forecastTime);
                            gridLayout.addView(forecastTemp);
                            gridLayout.addView(forecastConditions);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {}
                });

        // Add the request to the RequestQueue.
        queue.add(jsonRequest);
    }
}