package com.test.murphy.weatherapp;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.test.murphy.weatherapp.model.Units;
import com.test.murphy.weatherapp.model.WeatherConditions;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ConditionsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ConditionsFragment extends Fragment {
    // the fragment initialization parameters
    private static final String ARG_WEATHER_CONDITIONS = "weatherConditions";

    private WeatherConditions weatherConditions;

    @BindView(R.id.temperatureText) TextView temperatureText;
    @BindView(R.id.conditionsText) TextView conditionsText;
    @BindView(R.id.locationText) TextView locationText;
    @BindView(R.id.conditionsImage) ImageView conditionsImage;

    public ConditionsFragment() {
        // Required empty public constructor
    }

    public static ConditionsFragment newInstance(WeatherConditions param1) {
        ConditionsFragment fragment = new ConditionsFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_WEATHER_CONDITIONS, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            weatherConditions = getArguments().getParcelable(ARG_WEATHER_CONDITIONS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_conditions, container, false);
        ButterKnife.bind(this, rootView);
        // Inflate the layout for this fragment
        return rootView;

    }

    public void setWeatherConditions(WeatherConditions weatherConditions) {
        if (weatherConditions != null) {
            this.weatherConditions = weatherConditions;
            updateConditionsLayout();
        }
    }

    private void updateConditionsLayout() {
        Units units = WeatherManager.getInstance().getUnits();

        temperatureText.setText(String.valueOf(weatherConditions.currentTemperature) + units.getText());
        conditionsText.setText(weatherConditions.currentConditions);
        locationText.setText(weatherConditions.location);
        updateConditionsImage();
    }

    //Update the conditions image based on the condition code
    //Images are sourced from Icons8 http://icons8.com under CC-BY ND 3.0 license
    private void updateConditionsImage() {
        //If image was previously hidden, show it
        if (conditionsImage.getImageAlpha() == 0) {
            conditionsImage.setImageAlpha(1);
        }
        if (isBetween(weatherConditions.conditionCode, 200, 299)) {
            //Storm
            conditionsImage.setImageResource(R.drawable.icons8storm);
        } else if (isBetween(weatherConditions.conditionCode, 300, 599)) {
            //Drizzle\Rain
            conditionsImage.setImageResource(R.drawable.icons8rain);
        } else if (isBetween(weatherConditions.conditionCode, 600, 699)) {
            //Snow
            conditionsImage.setImageResource(R.drawable.icons8snow);
        } else if (isBetween(weatherConditions.conditionCode, 700, 799)) {
            //Atmosphere - Mist, smoke, etc
            conditionsImage.setImageResource(R.drawable.icons8haze);
        }  else if (weatherConditions.conditionCode == 800) {
            //Clear sky
            conditionsImage.setImageResource(R.drawable.icons8sun);
        } else if (isBetween(weatherConditions.conditionCode, 801, 899)) {
            //Clouds
            conditionsImage.setImageResource(R.drawable.icons8clouds);
        } else if (isBetween(weatherConditions.conditionCode, 900, 999)) {
            //Severe - No icons for these, hide the icon
            conditionsImage.setImageAlpha(0);
        }

        conditionsImage.setContentDescription(weatherConditions.currentConditions);
    }

    public static boolean isBetween(int x, int lower, int upper) {
        return lower <= x && x <= upper;
    }
}
