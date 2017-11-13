package com.test.murphy.weatherapp;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;

import com.test.murphy.weatherapp.model.Units;
import com.test.murphy.weatherapp.model.WeatherConditions;
import com.test.murphy.weatherapp.model.WeatherForecast;

import java.text.SimpleDateFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ForecastFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ForecastFragment extends Fragment {
    // the fragment initialization parameters
    private static final String ARG_FORECAST = "weatherForecast";

    private WeatherForecast weatherForecast;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("h aa", Locale.US);

    @BindView(R.id.forecastGrid) GridLayout gridLayout;

    public ForecastFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment ForecastFragment.
     */
    public static ForecastFragment newInstance(WeatherForecast param1) {
        ForecastFragment fragment = new ForecastFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_FORECAST, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            weatherForecast = getArguments().getParcelable(ARG_FORECAST);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_forecast, container, false);
        ButterKnife.bind(this, rootView);

        return rootView;
    }

    private void updateForecastLayout(int screenWidth) {
        Units units = WeatherManager.getInstance().getUnits();

        //Use lesser of 3 or the size of the forecast
        int columnCount = weatherForecast.forecast.length < 3 ? weatherForecast.forecast.length : 3;

        gridLayout.setColumnCount(columnCount);
        gridLayout.setRowCount(3);
        gridLayout.setOrientation(GridLayout.VERTICAL);
        gridLayout.removeAllViewsInLayout();

        for (int i = 0; i < 3; i++) {
            WeatherConditions weather = weatherForecast.forecast[i];


            //Time
            GridLayout.LayoutParams first = new GridLayout.LayoutParams(GridLayout.spec(0), GridLayout.spec(i));
            first.width = screenWidth / 3;
            first.height = gridLayout.getHeight() / 3;

            TextView forecastTime = new TextView(WeatherApp.getContext());
            String formattedDate = dateFormat.format(weather.date);
            forecastTime.setText(formattedDate);
            forecastTime.setTextColor(Color.WHITE);
            forecastTime.setLayoutParams(first);
            forecastTime.setGravity(Gravity.CENTER);

            gridLayout.addView(forecastTime, first);


            //Temp
            GridLayout.LayoutParams second = new GridLayout.LayoutParams(GridLayout.spec(1), GridLayout.spec(i));
            second.width = screenWidth / 3;
            second.height = gridLayout.getHeight() / 3;

            TextView forecastTemp = new TextView(WeatherApp.getContext());
            forecastTemp.setText(weather.currentTemperature.toString() + units.getText());
            forecastTemp.setTextColor(Color.WHITE);
            forecastTemp.setLayoutParams(second);
            forecastTemp.setGravity(Gravity.CENTER);

            gridLayout.addView(forecastTemp, second);


            //Conditions
            GridLayout.LayoutParams third = new GridLayout.LayoutParams(GridLayout.spec(2), GridLayout.spec(i));
            third.width = screenWidth / 3;
            third.height = gridLayout.getHeight() / 3;


            TextView forecastConditions = new TextView(WeatherApp.getContext());
            forecastConditions.setText(weather.currentConditions);
            forecastConditions.setTextColor(Color.WHITE);
            forecastConditions.setLayoutParams(third);
            forecastConditions.setGravity(Gravity.CENTER);

            gridLayout.addView(forecastConditions, third);
        }
    }

    public void setWeatherForecast(WeatherForecast weatherForecast, int screenWidth) {
        if (weatherForecast != null) {
            this.weatherForecast = weatherForecast;
            updateForecastLayout(screenWidth);
        }
    }
}
