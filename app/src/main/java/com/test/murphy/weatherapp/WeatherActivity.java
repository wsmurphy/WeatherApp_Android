package com.test.murphy.weatherapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.test.murphy.weatherapp.model.Units;
import com.test.murphy.weatherapp.model.WeatherConditions;
import com.test.murphy.weatherapp.model.WeatherForecast;

import java.io.IOException;

import io.fabric.sdk.android.Fabric;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;


public class WeatherActivity extends AppCompatActivity implements ConnectionsDelegate, ButtonsFragment.OnFragmentInteractionListener, ActivityCompat.OnRequestPermissionsResultCallback {

    ConditionsFragment conditionsFragment;
    ForecastFragment forecastFragment;
    ButtonsFragment buttonsFragment;

    private String zip = "";

    private Units units = Units.Fahrenheit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        Fabric.with(this, new Crashlytics());
        Fabric.with(this, new Answers());

        conditionsFragment = (ConditionsFragment) getFragmentManager().findFragmentById(R.id.conditionsFragment);
        forecastFragment = (ForecastFragment) getFragmentManager().findFragmentById(R.id.forecastFragment);
        buttonsFragment = (ButtonsFragment) getFragmentManager().findFragmentById(R.id.buttonsFragment);

        WeatherManager.getInstance(this.getApplicationContext()).setDelegate(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //If the zip code isn't set, resolve from device location
        if (zip.equals("")) {
            LocationUtils.getInstance().resolveLocation(this, this);
            zip = LocationUtils.getInstance().getZip(this);
        }

        reloadWeather();
    }

    void reloadWeather() {
        try {
            if (!zip.equals("")) {
                WeatherManager.getInstance(WeatherActivity.this).getWeather(zip, units);
                //WeatherManager.getInstance(WeatherActivity.this).getForecastVolley(zip, units);
                WeatherManager.getInstance(WeatherActivity.this).getForecast(zip, units);
            }
        } catch (IOException e) {
            //TODO
        }

    }

    public Units getUnits() {
        return units;
    }

    @Override
    public void weatherSuccess(final WeatherConditions conditions) {
        //THis should be sent from a background thread. Return to main before updating UI
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //execute code on main thread
                conditionsFragment.setWeatherConditions(conditions , units);
            }
        });
    }

    @Override
    public void forecastSuccess(final WeatherForecast forecast) {
        //THis should be sent from a background thread. Return to main before updating UI
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //execute code on main thread
                forecastFragment.setWeatherForecast(forecast, units, getScreenWidth());
            }
        });
    }

    private void showLocationAlert() {
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
                Answers.getInstance().logCustom(new CustomEvent("Change Location Tapped").putCustomAttribute("Action","OK"));

                //TODO: Edit checks on zip to ensure 5 digits
                zip = input.getText().toString();
                reloadWeather();
            }
        });

        builder.setNeutralButton("Use Current Location", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Answers.getInstance().logCustom(new CustomEvent("Change Location Tapped").putCustomAttribute("Action","Current Location"));

                //Resolve GPS\Network location
                LocationUtils.getInstance().resolveLocation(WeatherActivity.this, WeatherActivity.this);
                zip = LocationUtils.getInstance().getZip(WeatherActivity.this);
                reloadWeather();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Answers.getInstance().logCustom(new CustomEvent("Change Location Tapped").putCustomAttribute("Action","Cancel"));

                dialog.cancel();
            }
        });

        builder.show();
    }

    @Override
    public void onAboutButtonTapped() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("About");
        builder.setMessage("Weather icons courtesy of Icons8 under CC-BY ND 3.0 license.\nhttps://icons8.com/");
        builder.setPositiveButton("OK",  new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            //TODO: Do nothing
            }
        });
        builder.show();
    }

    @Override
    public void onUnitToggleTapped() {
        if (!buttonsFragment.isUnitsToggleChecked()) {
            units = Units.Fahrenheit;
        } else {
            units = Units.Celsius;
        }

        Answers.getInstance().logCustom(new CustomEvent("Units Toggle Tapped").putCustomAttribute("Changed To", units.getText()));

        //After settings changed, reload weather
        reloadWeather();
    }

    @Override
    public void onLocationButtonTapped() {
        //Display prompt to update location
        showLocationAlert();
    }

    private int getScreenWidth() {
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        return size.x;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //Check if both requested permissions were granted. If denied, do nothing.
        if (requestCode == LocationUtils.REQUEST_LOCATION &&
                grantResults[0] == 0 &&
                grantResults[1] == 0) {
            LocationUtils.getInstance().resolveLocation(this, this);
            zip = LocationUtils.getInstance().getZip(this);
            reloadWeather();
        }
    }
}