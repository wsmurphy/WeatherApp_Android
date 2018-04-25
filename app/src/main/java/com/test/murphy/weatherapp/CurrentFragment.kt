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
import android.support.design.widget.Snackbar
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
import java.util.Locale

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

    private var updateSnackbar: Snackbar? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_current, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        updateSnackbar = Snackbar.make(activity.findViewById(android.R.id.content), "Updating Weather. Please wait.", Snackbar.LENGTH_INDEFINITE)

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
                        updateFact()
                        updateAirQuality()

                        updateSnackbar?.dismiss()
                    } else if (intent.action == "android.intent.action.WEATHER_UPDATE_STARTED") {
                        updateSnackbar?.show()
                    }
                }
            }
        }

        LocalBroadcastManager.getInstance(WeatherApp.context).registerReceiver(broadcastReceiver, IntentFilter("android.intent.action.WEATHER_CHANGED"))
        LocalBroadcastManager.getInstance(WeatherApp.context).registerReceiver(broadcastReceiver, IntentFilter("android.intent.action.WEATHER_UPDATE_STARTED"))
    }

    private fun updateConditionsLayout() {
        val info = WeatherManager.instance.dashboardInfo ?: return
        val conditions = info.conditions ?: return

        val units = WeatherManager.instance.units

        val tempString = when (units) {
            Units.Fahrenheit -> String.format("%.1f %s", conditions.currentTemperatureF, units.text)
            Units.Celsius -> String.format("%.1f %s", conditions.currentTemperatureC, units.text)
        }

        temperatureText.text = tempString
        conditionsText.text = conditions.currentConditions
        locationText.text =  String.format("%s (%s)", conditions.location, WeatherManager.instance.zip)
        updateConditionsImage()
    }

    //Update the conditions image based on the condition code
    //Images are sourced from Icons8 http://icons8.com under CC-BY ND 3.0 license
    private fun updateConditionsImage() {
        val conditions = WeatherManager.instance.dashboardInfo?.conditions ?: return

        //If image was previously hidden, show it
        if (conditionsImage.imageAlpha == 0) {
            conditionsImage.imageAlpha = 1
        }
        when (conditions.conditionCode) {
            in 200..299 -> conditionsImage.setImageResource(R.drawable.icons8storm) //Storm
            in 300..599 -> conditionsImage.setImageResource(R.drawable.icons8rain) //Drizzle\Rain
            in 600..699 -> conditionsImage.setImageResource(R.drawable.icons8snow) //Snow
            in 700..799 -> conditionsImage.setImageResource(R.drawable.icons8haze) //Atmosphere - Mist, smoke, etc
            800 -> conditionsImage.setImageResource(R.drawable.icons8sun) //Clear sky
            in 801..899 -> conditionsImage.setImageResource(R.drawable.icons8clouds) //Clouds
            else -> conditionsImage.imageAlpha = 0 //No icons for these, hide the icon
        }

        conditionsImage.contentDescription = conditions.currentConditions
    }

    private fun updateForecastLayout() {
        val info = WeatherManager.instance.dashboardInfo ?: return
        val weatherForecast = info.forecast ?: return

        val units = WeatherManager.instance.units

        val screenWidth = screenWidth

        //Scroll the entire forecast
        val columnCount = weatherForecast.forecast.size

        forecastGrid.columnCount = columnCount
        forecastGrid.rowCount = 3
        forecastGrid.orientation = GridLayout.VERTICAL
        forecastGrid.removeAllViewsInLayout()

        for (i in 0 until columnCount) {
            val weather = weatherForecast.forecast[i]

            //Time
            val first = GridLayout.LayoutParams(GridLayout.spec(0), GridLayout.spec(i))
            first.width = screenWidth / 5
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
            second.width = screenWidth / 5
            second.height = forecastGrid.height / 3

            val forecastTemp = TextView(WeatherApp.context)
            forecastTemp.text = when (units) {
                Units.Fahrenheit -> String.format("%.1f %s", weather.currentTemperatureF, units.text)
                Units.Celsius -> String.format("%.1f %s", weather.currentTemperatureC, units.text)
            }

            forecastTemp.setTextColor(Color.WHITE)
            forecastTemp.layoutParams = second
            forecastTemp.gravity = Gravity.CENTER

            forecastGrid.addView(forecastTemp, second)

            //Conditions
            val third = GridLayout.LayoutParams(GridLayout.spec(2), GridLayout.spec(i))
            third.width = screenWidth / 5
            third.height = forecastGrid.height / 3

            val forecastConditions = TextView(WeatherApp.context)
            forecastConditions.text = weather.currentConditions
            forecastConditions.setTextColor(Color.WHITE)
            forecastConditions.layoutParams = third
            forecastConditions.gravity = Gravity.CENTER

            forecastGrid.addView(forecastConditions, third)
        }
    }

    fun updateFact() {
        val info = WeatherManager.instance.dashboardInfo ?: return
        val fact = info.fact ?: return

        factTextView.text = "Fact: " + fact
    }

    fun updateAirQuality() {
        val info = WeatherManager.instance.dashboardInfo ?: return
        val uvIndex = info.uvIndex ?: return

        uvindexTextView.text = uvIndex.stringValue

        when (uvIndex.colorValue) {
            "Green" -> uvindexTextView.setBackgroundColor(resources.getColor(R.color.uvGreen))
            "Yellow" -> uvindexTextView.setBackgroundColor(resources.getColor(R.color.uvYellow))
            "Orange" -> uvindexTextView.setBackgroundColor(resources.getColor(R.color.uvOrange))
            "Red" -> uvindexTextView.setBackgroundColor(resources.getColor(R.color.uvRed))
            "Violet" -> uvindexTextView.setBackgroundColor(resources.getColor(R.color.uvViolet))
            "Blue" -> uvindexTextView.setBackgroundColor(resources.getColor(R.color.uvBlue))

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
        val layout = LinearLayout(activity)
        layout.orientation = LinearLayout.VERTICAL
        val params = LinearLayout.LayoutParams(
                MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        params.setMargins(80, 0, 80, 0) //TODO: How far to inset?


        val input = EditText(activity)
        input.inputType = InputType.TYPE_CLASS_NUMBER
        layout.addView(input, params)

        input.requestFocus()

        AlertDialog.Builder(activity)
                .setTitle("Change Location")
                .setView(layout)
                .setPositiveButton("OK") { _, _ ->
            Answers.getInstance().logCustom(CustomEvent("Change Location Tapped").putCustomAttribute("Action", "OK"))

            WeatherManager.instance.zip = input.text.toString()
        }.setNeutralButton("Use Current Location") { _, _ ->
            Answers.getInstance().logCustom(CustomEvent("Change Location Tapped").putCustomAttribute("Action", "Current Location"))

            //Resolve GPS\Network location
            LocationUtils.instance.resolveLocation(activity)

            WeatherManager.instance.zip = LocationUtils.instance.zip
        }.setNegativeButton("Cancel") { dialog, _ ->
            Answers.getInstance().logCustom(CustomEvent("Change Location Tapped").putCustomAttribute("Action", "Cancel"))

            dialog.cancel()
        }.show()

        //                .setOnKeyListener { dialog, keyCode, event ->
//            //TODO: Edit checks on zip to ensure 5 digits
//            //TODO: Check if key is number and total is < 5
//
//        }

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
