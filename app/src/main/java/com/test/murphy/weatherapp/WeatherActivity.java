package com.test.murphy.weatherapp;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.test.murphy.weatherapp.model.Units;

import java.io.IOException;

import io.fabric.sdk.android.Fabric;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;


/*
    Potential features:
       - Drive Time: Show drive time to selected destination (daily commute)
       - Let user choose days to show drive time (M-F only, etc)
       - Let user choose time to show drive time (drive at 8 and 5, etc)
       - Link out to google maps to begin drive
       - What to wear (think Swackett) based on conditions (take a jacket!)
 */


public class WeatherActivity extends AppCompatActivity implements ButtonsFragment.OnFragmentInteractionListener, ActivityCompat.OnRequestPermissionsResultCallback {

    ConditionsFragment conditionsFragment;
    ForecastFragment forecastFragment;
    ButtonsFragment buttonsFragment;

    private String zip = "";

    private Units units = Units.Fahrenheit;

    private BroadcastReceiver mBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, final Intent intent) {
                //This should be sent from a background thread. Return to main before updating UI
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (intent.getAction().equals("android.intent.action.WEATHER_CHANGED")) {
                            conditionsFragment.setWeatherConditions(WeatherManager.getInstance().conditions);
                            forecastFragment.setWeatherForecast(WeatherManager.getInstance().forecast, getScreenWidth());
                        }
                    }
                });
            }
        };

        IntentFilter filter = new IntentFilter("android.intent.action.WEATHER_CHANGED");
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, filter);

        Fabric.with(this, new Crashlytics());
        Fabric.with(this, new Answers());

        conditionsFragment = (ConditionsFragment) getFragmentManager().findFragmentById(R.id.conditionsFragment);
        forecastFragment = (ForecastFragment) getFragmentManager().findFragmentById(R.id.forecastFragment);
        buttonsFragment = (ButtonsFragment) getFragmentManager().findFragmentById(R.id.buttonsFragment);
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
                WeatherManager.getInstance().getWeather(zip);
                WeatherManager.getInstance().getForecast(zip);
            }
        } catch (IOException e) {
            //TODO
        }

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
            WeatherManager.getInstance().setUnits(Units.Fahrenheit);
        } else {
            WeatherManager.getInstance().setUnits(Units.Celsius);
        }

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