package com.test.murphy.weatherapp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.test.murphy.weatherapp.model.Units;
import com.test.murphy.weatherapp.model.WeatherConditions;
import com.test.murphy.weatherapp.model.WeatherForecast;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;


public class WeatherActivity extends AppCompatActivity implements ConnectionsDelegate {

    @BindView(R.id.temperatureText) TextView temperatureText;
    @BindView(R.id.conditionsText) TextView conditionsText;
    @BindView(R.id.locationText) TextView locationText;
    @BindView(R.id.locationButton) Button locationButton;
    @BindView(R.id.unitsToggle) ToggleButton unitsToggle;
    @BindView(R.id.forecastGrid) GridLayout gridLayout;

    private String zip = "";

    private int screenWidth = 0;

    LocationManager locationManager;

    private static final int REQUEST_LOCATION = 0;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("h aa", Locale.US);

    private Units units = Units.Farenheight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        ButterKnife.bind(this);

        Connections.getInstance(this.getApplicationContext()).setDelegate(this);

        //Get screenWidth for setting up forecast view
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        screenWidth = size.x;

        //If the zip code isn't set, resolve from device location
        if (zip == "") {
            resolveLocation();
        }

        reloadWeather();
    }

    void reloadWeather() {
        Connections.getInstance(WeatherActivity.this).getWeather(zip, units);
        Connections.getInstance(WeatherActivity.this).getForecast(zip, units);
    }

    void updateConditionsLayout(WeatherConditions weatherConditions) {
        temperatureText.setText(String.valueOf(weatherConditions.currentTemperature) + units.getText());
        conditionsText.setText(weatherConditions.currentConditions);
        locationText.setText(weatherConditions.location);
    }

    void updateForecastLayout(WeatherForecast forecast) {
        //Use lesser of 3 or the size of the forecast
        int columnCount = forecast.forecast.size() < 3 ? forecast.forecast.size() : 3;

        gridLayout.setColumnCount(columnCount);
        gridLayout.setRowCount(3);
        gridLayout.setOrientation(GridLayout.VERTICAL);
        gridLayout.removeAllViewsInLayout();

        for (int i = 0; i < 3; i++) {
            WeatherConditions weather = forecast.forecast.get(i);


            //Time
            GridLayout.LayoutParams first = new GridLayout.LayoutParams(GridLayout.spec(0), GridLayout.spec(i));
            first.width = screenWidth / 3;
            first.height = gridLayout.getHeight() / 3;

            TextView forecastTime = new TextView(getApplicationContext());
            String formattedDate = dateFormat.format(weather.date).toString();
            forecastTime.setText(formattedDate);
            forecastTime.setTextColor(Color.WHITE);
            forecastTime.setLayoutParams(first);
            forecastTime.setGravity(Gravity.CENTER);

            gridLayout.addView(forecastTime, first);


            //Temp
            GridLayout.LayoutParams second = new GridLayout.LayoutParams(GridLayout.spec(1), GridLayout.spec(i));
            second.width = screenWidth / 3;
            second.height = gridLayout.getHeight() / 3;

            TextView forecastTemp = new TextView(getApplicationContext());
            forecastTemp.setText(weather.currentTemperature.toString() + units.getText());
            forecastTemp.setTextColor(Color.WHITE);
            forecastTemp.setLayoutParams(second);
            forecastTemp.setGravity(Gravity.CENTER);

            gridLayout.addView(forecastTemp, second);


            //Conditions
            GridLayout.LayoutParams third = new GridLayout.LayoutParams(GridLayout.spec(2), GridLayout.spec(i));
            third.width = screenWidth / 3;
            third.height = gridLayout.getHeight() / 3;

            TextView forecastConditions = new TextView(getApplicationContext());
            forecastConditions.setText(weather.currentConditions);
            forecastConditions.setTextColor(Color.WHITE);
            forecastConditions.setLayoutParams(third);
            forecastConditions.setGravity(Gravity.CENTER);

            gridLayout.addView(forecastConditions, third);
        }
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

        input.requestFocus();

        builder.setView(layout);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //TODO: Edit checks on zip to ensure 5 digits
                zip = input.getText().toString();
                reloadWeather();
            }
        });

        builder.setNeutralButton("Use Current Location", new DialogInterface.OnClickListener() {
           @Override
           public void onClick(DialogInterface dialog, int which) {
                //Resolve GPS\Network location
               resolveLocation();
               reloadWeather();
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
        reloadWeather();
    }

    @Override
    public void weatherSuccess(WeatherConditions conditions) {
        updateConditionsLayout(conditions);
    }

    @Override
    public void forecastSuccess(WeatherForecast forecast) {
        updateForecastLayout(forecast);
    }
}