package com.test.murphy.weatherapp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class WeatherActivity extends AppCompatActivity {

    @BindView(R.id.temperatureText) TextView temperatureText;
    @BindView(R.id.conditionsText) TextView conditionsText;
    @BindView(R.id.locationText) TextView locationText;
    @BindView(R.id.conditionsImage) ImageView conditionsImage;
    @BindView(R.id.locationButton) Button locationButton;

    private String zip = "";

    LocationManager locationManager;

    private static final int REQUEST_LOCATION = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        ButterKnife.bind(this);

        //If the zipcode isn't set, resolve from device location
        if (zip == "") {
            getLocation();
        }

        getWeather();
    }

    void getWeather() {
        // makeOpenWeatherApiCall();
        makeWeatherUndergroundApiCall();
    }


    void makeOpenWeatherApiCall() {
        String BASE_URL = "http://api.openweathermap.org/data/2.5/weather?zip=";
        String location = zip + ",us"; //TODO: Make location selectable or determined from GPS
        String appId = "&APPID=b4608d4fcb4accac0a8cc2ea6949eeb5";
        String units = "&units=imperial"; //TODO: Make selectable

        final RequestQueue queue = Volley.newRequestQueue(this);
        String url = BASE_URL + location + appId + units;

        JsonObjectRequest jsonRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        OWAWeather weather = new OWAWeather(response);
                        updateViewFields(weather);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //TODO: Handle error
                        temperatureText.setText("Error loading weather.");
                    }
                });

        // Add the request to the RequestQueue.
        queue.add(jsonRequest);
    }

    void makeWeatherUndergroundApiCall() {
        String BASE_URL = "http://api.wunderground.com/api/e2e5d711f79b7497/conditions/q/";
        String location = zip + ".json"; //TODO: Make location selectable or determined from GPS

        // Instantiate the RequestQueue.
        final RequestQueue queue = Volley.newRequestQueue(this);
        String url = BASE_URL + location;

        JsonObjectRequest jsonRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        WUWeather weather = new WUWeather(response);
                        updateViewFields(weather);

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //TODO: Handle error
                        temperatureText.setText("Error loading weather.");
                    }
                });

        // Add the request to the RequestQueue.
        queue.add(jsonRequest);
    }

    void updateViewFields(Weather weather) {
        final RequestQueue queue = Volley.newRequestQueue(this);

        temperatureText.setText(String.valueOf(weather.currentTemperature) + "° F"); //TODO: Make F or C variable on selected units
        conditionsText.setText(weather.currentConditions);
        locationText.setText(weather.location);

        ImageRequest imageRequest = new ImageRequest(weather.iconUrl, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap bitmap) {
                conditionsImage.setImageBitmap(bitmap);
            }
        }, 200, 200, ImageView.ScaleType.FIT_CENTER, null, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                temperatureText.setText("Error loading icon");
            }
        });

        queue.add(imageRequest);
    }

    @OnClick(R.id.locationButton)
    public void locationButtonPressed() {
        //Display prompt to update location
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Location");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //TODO: Edit checks on zip to ensure 5 digits
                zip = input.getText().toString();
                getWeather();
            }
        });

        builder.setNeutralButton("Use Current Location", new DialogInterface.OnClickListener() {

           @Override
           public void onClick(DialogInterface dialog, int which) {
                //Resolve GPS\Network location
               getLocation();
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

    private void getLocation() {
        locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);

        //Resolve current location
        Location lastBest = getLastBestLocation();
        if (lastBest != null) {
            String lastZip = getZipFromLocation(lastBest);
            zip = lastZip;
        }
    }

}