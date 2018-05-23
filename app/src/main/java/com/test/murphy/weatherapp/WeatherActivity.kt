package com.test.murphy.weatherapp

import android.app.AlertDialog
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.CustomEvent
import com.google.firebase.iid.FirebaseInstanceId
import io.fabric.sdk.android.Fabric
import kotlinx.android.synthetic.main.activity_weather.*




class WeatherActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)
        setSupportActionBar(app_toolbar)

        Fabric.with(this, Crashlytics())
        Fabric.with(this, Answers())
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_about -> {
            aboutButtonTapped()
            true
        }

        R.id.action_change_location -> {
            showLocationAlert()
            true
        }

        R.id.action_change_units -> {
            unitsToggleTapped()
            true
        }

        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()

        //If the zip code isn't set, resolve from device location
        if (WeatherManager.instance.zip == "") {
            LocationUtils.instance.resolveLocation(this)
            WeatherManager.instance.zip = LocationUtils.instance.zip
        } else {
            WeatherManager.instance.reloadWeather()
        }

        val refreshedToken = FirebaseInstanceId.getInstance().token
        Log.d("WeatherAct", "Refreshed token: $refreshedToken")
    }

    private fun aboutButtonTapped() {
        AlertDialog.Builder(this)
        .setTitle("About")
        .setMessage("Weather icons courtesy of Icons8 under CC-BY ND 3.0 license.\nhttps://icons8.com/")
        .setPositiveButton("OK") { _, _ ->
            //TODO: Do nothing
        }.show()
    }

    private fun showLocationAlert() {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        val params = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        params.setMargins(80, 0, 80, 0) //TODO: How far to inset?


        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER
        layout.addView(input, params)

        input.requestFocus()

        AlertDialog.Builder(this)
                .setTitle("Change Location")
                .setView(layout)
                .setPositiveButton("OK") { _, _ ->
                    Answers.getInstance().logCustom(CustomEvent("Change Location Tapped").putCustomAttribute("Action", "OK"))

                    WeatherManager.instance.zip = input.text.toString()
                }.setNeutralButton("Use Current Location") { _, _ ->
            Answers.getInstance().logCustom(CustomEvent("Change Location Tapped").putCustomAttribute("Action", "Current Location"))

            //Resolve GPS\Network location
            LocationUtils.instance.resolveLocation(this)

            WeatherManager.instance.zip = LocationUtils.instance.zip
        }.setNegativeButton("Cancel") { dialog, _ ->
            Answers.getInstance().logCustom(CustomEvent("Change Location Tapped").putCustomAttribute("Action", "Cancel"))

            dialog.cancel()
        }.show()
    }

    private fun unitsToggleTapped() {
        WeatherManager.instance.toggleUnits()
    }
}