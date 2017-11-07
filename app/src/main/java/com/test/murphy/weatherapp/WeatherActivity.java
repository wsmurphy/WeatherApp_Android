package com.test.murphy.weatherapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.test.murphy.weatherapp.model.Units;
import com.test.murphy.weatherapp.model.WeatherConditions;
import com.test.murphy.weatherapp.model.WeatherForecast;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import io.fabric.sdk.android.Fabric;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;


public class WeatherActivity extends AppCompatActivity implements ConnectionsDelegate {

    @BindView(R.id.locationButton) Button locationButton;
    @BindView(R.id.unitsToggle) ToggleButton unitsToggle;
    ConditionsFragment conditionsFragment;
    ForecastFragment forecastFragment;

    private String zip = "";

    LocationManager locationManager;

    private Units units = Units.Farenheight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        Fabric.with(this, new Crashlytics());
        Fabric.with(this, new Answers());

        conditionsFragment = (ConditionsFragment) getFragmentManager().findFragmentById(R.id.conditionsFragment);
        forecastFragment = (ForecastFragment) getFragmentManager().findFragmentById(R.id.forecastFragment);
        ButterKnife.bind(this);

        Connections.getInstance(this.getApplicationContext()).setDelegate(this);

        //If the zip code isn't set, resolve from device location
        if (zip == "") {
            LocationUtils.resolveLocation(this, this);
        }

        reloadWeather();
    }

    void reloadWeather() {
        try {
            if (zip != "") {
                Connections.getInstance(WeatherActivity.this).getWeather(zip, units);
                //Connections.getInstance(WeatherActivity.this).getForecastVolley(zip, units);
                Connections.getInstance(WeatherActivity.this).getForecastOkHttp(zip, units);
            }
        } catch (IOException e) {
            //TODO
        }

    }

    public Units getUnits() {
        return units;
    }

    @OnClick(R.id.locationButton)
    public void locationButtonPressed() {
        //Display prompt to update location
        showLocationAlert();
    }

    @OnCheckedChanged(R.id.unitsToggle)
    public void toggleTapped() {

        if (!unitsToggle.isChecked()) {
            units = Units.Farenheight;
        } else {
            units = Units.Celcius;
        }

        Answers.getInstance().logCustom(new CustomEvent("Units Toggle Tapped").putCustomAttribute("Changed To", units.getText()));

        //After settings changed, reload weather
        reloadWeather();
    }

    @Override
    public void weatherSuccess(WeatherConditions conditions) {
        conditionsFragment.setWeatherConditions(conditions);
    }

    @Override
    public void forecastSuccess(final WeatherForecast forecast) {
        //THis should be sent from a background thread. Return to main before updating UI
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //execute code on main thread
                forecastFragment.setWeatherForecast(forecast);
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
                LocationUtils.resolveLocation(WeatherActivity.this, WeatherActivity.this);
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
}