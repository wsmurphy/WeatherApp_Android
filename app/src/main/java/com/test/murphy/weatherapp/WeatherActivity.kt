package com.test.murphy.weatherapp

import android.app.AlertDialog
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.android.synthetic.main.activity_weather.*


class WeatherActivity : AppCompatActivity() {

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)
        setSupportActionBar(app_toolbar)

        FirebaseApp.initializeApp(this)

        // Obtain the FirebaseAnalytics instance.
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

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
                    val bundle = Bundle()
                    bundle.putString("action", "OK")
                    firebaseAnalytics.logEvent("change_location", bundle)

                    WeatherManager.instance.zip = input.text.toString()
                }.setNeutralButton("Use Current Location") { _, _ ->
                    val bundle = Bundle()
                    bundle.putString("action", "Use Current Location")
                    firebaseAnalytics.logEvent("change_location", bundle)

                    //Resolve GPS\Network location
                    LocationUtils.instance.resolveLocation(this)

                    WeatherManager.instance.zip = LocationUtils.instance.zip
                }.setNegativeButton("Cancel") { dialog, _ ->
                    val bundle = Bundle()
                    bundle.putString("action", "Cancel")
                    firebaseAnalytics.logEvent("change_location", bundle)

                    dialog.cancel()
                }.show()
    }

    private fun unitsToggleTapped() {
        WeatherManager.instance.toggleUnits()
    }
}