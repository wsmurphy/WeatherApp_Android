package com.test.murphy.weatherapp

import android.app.AlertDialog
import android.app.Fragment
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.LocalBroadcastManager
import android.text.InputType
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.EditText
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.CustomEvent
import com.test.murphy.weatherapp.model.Units
import kotlinx.android.synthetic.main.fragment_current.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Use the [CurrentFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CurrentFragment : Fragment(), ActivityCompat.OnRequestPermissionsResultCallback {
    private val dateFormat = SimpleDateFormat("h aa", Locale.US)

    private val isUnitsToggleChecked: Boolean
        get() = unitsToggle.isChecked

    private val screenWidth: Int
        get() {
            val size = Point()
            activity.windowManager.defaultDisplay.getSize(size)
            return size.x
        }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_current, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        //Setup the button actions
        locationButton.setOnClickListener { showLocationAlert() }
        aboutButton.setOnClickListener { aboutButtonTapped() }
        unitsToggle.setOnClickListener { unitsToggleTapped() }

        val broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                //This should be sent from a background thread. Return to main before updating UI
                activity.runOnUiThread {
                    if (intent.action == "android.intent.action.WEATHER_CHANGED") {
                        updateConditionsLayout()
                        updateConditionsImage()
                        updateForecastLayout()
                    }
                }
            }
        }

        LocalBroadcastManager.getInstance(WeatherApp.context).registerReceiver(broadcastReceiver, IntentFilter("android.intent.action.WEATHER_CHANGED"))
    }

    private fun updateConditionsLayout() {
        val weatherConditions = WeatherManager.instance.conditions ?: return

        val units = WeatherManager.instance.units

        val tempString = String.format("%.2f %s", weatherConditions.currentTemperature, units.text)
        temperatureText.text = tempString
        conditionsText.text = weatherConditions.currentConditions
        locationText.text = weatherConditions.location
        updateConditionsImage()
    }

    //Update the conditions image based on the condition code
    //Images are sourced from Icons8 http://icons8.com under CC-BY ND 3.0 license
    private fun updateConditionsImage() {
        val weatherConditions = WeatherManager.instance.conditions ?: return

        //If image was previously hidden, show it
        if (conditionsImage.imageAlpha == 0) {
            conditionsImage.imageAlpha = 1
        }
        when (weatherConditions.conditionCode) {
            in 200..299 -> conditionsImage.setImageResource(R.drawable.icons8storm) //Storm
            in 300..599 -> conditionsImage.setImageResource(R.drawable.icons8rain) //Drizzle\Rain
            in 600..699 -> conditionsImage.setImageResource(R.drawable.icons8snow) //Snow
            in 700..799 -> conditionsImage.setImageResource(R.drawable.icons8haze) //Atmosphere - Mist, smoke, etc
            800 -> conditionsImage.setImageResource(R.drawable.icons8sun) //Clear sky
            in 801..899 -> conditionsImage.setImageResource(R.drawable.icons8clouds) //Clouds
            else -> conditionsImage.imageAlpha = 0 //No icons for these, hide the icon
        }

        conditionsImage.contentDescription = weatherConditions.currentConditions
    }

    private fun updateForecastLayout() {
        val weatherForecast = WeatherManager.instance.forecast ?: return

        val units = WeatherManager.instance.units

        val screenWidth = screenWidth

        //Use lesser of 3 or the size of the forecast
        val columnCount = if (weatherForecast.forecast.size < 3) weatherForecast.forecast.size else 3

        forecastGrid.columnCount = columnCount
        forecastGrid.rowCount = 3
        forecastGrid.orientation = GridLayout.VERTICAL
        forecastGrid.removeAllViewsInLayout()

        for (i in 0..2) {
            val weather = weatherForecast.forecast[i]

            //Time
            val first = GridLayout.LayoutParams(GridLayout.spec(0), GridLayout.spec(i))
            first.width = screenWidth / 3
            first.height = forecastGrid.height / 3

            val forecastTime = TextView(WeatherApp.context)
            val formattedDate = dateFormat.format(weather.date)
            forecastTime.text = formattedDate
            forecastTime.setTextColor(Color.WHITE)
            forecastTime.layoutParams = first
            forecastTime.gravity = Gravity.CENTER

            forecastGrid.addView(forecastTime, first)

            //Temp
            val second = GridLayout.LayoutParams(GridLayout.spec(1), GridLayout.spec(i))
            second.width = screenWidth / 3
            second.height = forecastGrid.height / 3

            val forecastTemp = TextView(WeatherApp.context)
            forecastTemp.text = String.format("%.2f %s", weather.currentTemperature, units.text)
            forecastTemp.setTextColor(Color.WHITE)
            forecastTemp.layoutParams = second
            forecastTemp.gravity = Gravity.CENTER

            forecastGrid.addView(forecastTemp, second)

            //Conditions
            val third = GridLayout.LayoutParams(GridLayout.spec(2), GridLayout.spec(i))
            third.width = screenWidth / 3
            third.height = forecastGrid.height / 3

            val forecastConditions = TextView(WeatherApp.context)
            forecastConditions.text = weather.currentConditions
            forecastConditions.setTextColor(Color.WHITE)
            forecastConditions.layoutParams = third
            forecastConditions.gravity = Gravity.CENTER

            forecastGrid.addView(forecastConditions, third)
        }
    }

    fun aboutButtonTapped() {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("About")
        builder.setMessage("Weather icons courtesy of Icons8 under CC-BY ND 3.0 license.\nhttps://icons8.com/")
        builder.setPositiveButton("OK") { _, _ ->
            //TODO: Do nothing
        }
        builder.show()
    }

    fun unitsToggleTapped() {
        WeatherManager.instance.units = if (!isUnitsToggleChecked) Units.Fahrenheit else Units.Celsius
    }

    private fun showLocationAlert() {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Change Location")

        val layout = LinearLayout(activity)
        layout.orientation = LinearLayout.VERTICAL
        val params = LinearLayout.LayoutParams(
                MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        params.setMargins(80, 0, 80, 0) //TODO: How far to inset?


        val input = EditText(activity)
        input.inputType = InputType.TYPE_CLASS_NUMBER
        layout.addView(input, params)

        input.requestFocus()

        builder.setView(layout)
        builder.setPositiveButton("OK") { _, _ ->
            Answers.getInstance().logCustom(CustomEvent("Change Location Tapped").putCustomAttribute("Action", "OK"))

            //TODO: Edit checks on zip to ensure 5 digits
            WeatherManager.instance.zip = input.text.toString()
        }

        builder.setNeutralButton("Use Current Location") { _, _ ->
            Answers.getInstance().logCustom(CustomEvent("Change Location Tapped").putCustomAttribute("Action", "Current Location"))

            //Resolve GPS\Network location
            LocationUtils.instance.resolveLocation(activity)

            WeatherManager.instance.zip = LocationUtils.instance.zip
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            Answers.getInstance().logCustom(CustomEvent("Change Location Tapped").putCustomAttribute("Action", "Cancel"))

            dialog.cancel()
        }

        builder.show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        //Check if both requested permissions were granted. If denied, do nothing.
        if (requestCode == LocationUtils.REQUEST_LOCATION &&
                grantResults[0] == 0 &&
                grantResults[1] == 0) {
            LocationUtils.instance.resolveLocation(activity)

            WeatherManager.instance.zip = LocationUtils.instance.zip
        }
    }
}