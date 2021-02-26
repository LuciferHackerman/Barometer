package com.lucifer.hackerman.barometer;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor pressure;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        pressure = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        double mBar = event.values[0]; // get air pressure
        //mBar = mBar - 3.3; //barometer calibration
        EditText editBarometerCalibration = (EditText) findViewById(R.id.barometerCalibration);
        String barometerCalibrationString = editBarometerCalibration.getText().toString();
        try {
            double barCal = Double.valueOf(barometerCalibrationString);
            mBar = mBar + barCal;
        } catch (Exception e) {
            System.out.println("calibration value error!");
        }
        // show pressure in millibars
        TextView millibars = (TextView) findViewById(R.id.millibars);
        final DecimalFormat df_hPa = new DecimalFormat("####.#");
        millibars.setText(String.valueOf(df_hPa.format(mBar)));

        // show pressure in mm
        TextView mmHg = (TextView) findViewById(R.id.mmHg);
        double mmhg = (mBar * 0.750062);
        DecimalFormat df_mmHg = new DecimalFormat("###");
        mmHg.setText(String.valueOf(df_mmHg.format(mmhg)));

        // show pressure in inches
        TextView inHg = (TextView) findViewById(R.id.inch);
        double inhg = (mBar * 0.02953);
        final DecimalFormat df_inhg = new DecimalFormat("##.##");
        inHg.setText(String.valueOf(df_inhg.format(inhg)));

        // getting QNH
        final EditText setQNH = (EditText) findViewById(R.id.setQNH);
        final EditText setQNHinch = (EditText) findViewById(R.id.setQNHinch);

        String QNH = setQNH.getText().toString();
        String QNHinch = setQNHinch.getText().toString();

        double meanSeaLevelPressure = 0;
        try {
            Double qnh = Double.valueOf(QNH);
            meanSeaLevelPressure = qnh;
        } catch (Exception e) {
            System.out.println("input error!");
        }

        double meanSeaLevelPressureInch = 0;
        try {
            Double qnhinch = Double.valueOf(QNHinch);
            meanSeaLevelPressureInch = qnhinch;
        } catch (Exception e) {
            System.out.println("input error!");
        }

        // getting temperature
        final EditText temp = (EditText) findViewById(R.id.temperature);
        final EditText tempFahrenheit = (EditText) findViewById(R.id.temperatureFahrenheit);

        String tFahrenheit = tempFahrenheit.getText().toString();
        String t = temp.getText().toString();

        final DecimalFormat df_temp = new DecimalFormat("##.#");

        double temperatureAtStation = 0;
        try {
            Double tmp = Double.valueOf(t);
            temperatureAtStation = tmp;
        } catch (Exception e) {
            System.out.println("input error!");
        }

        double temperatureFahrenheit = 0;
        try {
            Double tmp = Double.valueOf(tFahrenheit);
            temperatureFahrenheit = tmp;
            temperatureAtStation = (temperatureFahrenheit - 32) * 5 / 9;
        } catch (Exception e) {
            System.out.println("input error!");
        }

        // checking focus
        final double finalTemperature = (temperatureAtStation * 9 / 5) + 32;
        final double finalTemperatureFahrenheit = (temperatureFahrenheit - 32) * 5 / 9;
        final double finalMeanSeaLevelPressure = meanSeaLevelPressure;
        final double finalMeanSeaLevelPressureInch = meanSeaLevelPressureInch;

        // focus pressure
        setQNH.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                setQNHinch.setText("");
            }
        });

        setQNHinch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                setQNH.setText("");
            }
        });

        // focus temp
        temp.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                tempFahrenheit.setText("");
            }
        });

        tempFahrenheit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                temp.setText("");
            }
        });

        if (temp.getText().length() > 0 || tempFahrenheit.getText().length() > 0) {
            temp.setHint(String.valueOf(df_temp.format(finalTemperatureFahrenheit)) + " °C");
            tempFahrenheit.setHint(String.valueOf(df_temp.format(finalTemperature)) + " °F");

        } else {
            temp.setHint("°C");
            tempFahrenheit.setHint("°F");
        }
        if (setQNH.getText().length() > 0 || setQNHinch.getText().length() > 0) {
            setQNHinch.setHint(String.valueOf(df_inhg.format(finalMeanSeaLevelPressure * 0.02953)) + " in Hg");
            setQNH.setHint(String.valueOf(df_hPa.format(finalMeanSeaLevelPressureInch / 0.02953)) + " hPa");
        } else {
            setQNHinch.setHint("29.92 in Hg");
            setQNH.setHint("1013.3 hPa");
        }


        //getting altitude
        double pressureAtStationLevel = mBar;
        double pressureAtStationLevelInInchHg = inhg;
        int hypsometricConstant = 18400;
        double altitude = 0;
        double temporaryStationElevation = 0;
        double coefficientOfExpansion = 0.00367;

        // temporary altitude (using Laplace shortened formula)
        int tempForMeasurement = 0;
        if (setQNH.getText().length() > 0) {
            temporaryStationElevation = hypsometricConstant *
                    (1 + coefficientOfExpansion * tempForMeasurement) *
                    Math.log10(meanSeaLevelPressure / pressureAtStationLevel);
        } else if (setQNHinch.getText().length() > 0) {
            temporaryStationElevation = hypsometricConstant *
                    (1 + coefficientOfExpansion * tempForMeasurement) *
                    Math.log10(meanSeaLevelPressureInch / pressureAtStationLevelInInchHg);
        }
        double meanTemperatureOfAirColumn = temperatureAtStation + (3 * temporaryStationElevation) / 1000;

        // accuracy altitude (using Laplace shortened formula)
        if (setQNH.getText().length() > 0) {
            altitude = hypsometricConstant *
                    (1 + coefficientOfExpansion * meanTemperatureOfAirColumn) *
                    Math.log10(meanSeaLevelPressure / pressureAtStationLevel);
        } else if (setQNHinch.getText().length() > 0) {
            altitude = hypsometricConstant *
                    (1 + coefficientOfExpansion * meanTemperatureOfAirColumn) *
                    Math.log10(meanSeaLevelPressureInch / pressureAtStationLevelInInchHg);
        }

        // show altitude
        TextView altimeter = (TextView) findViewById(R.id.altimeter);
        DecimalFormat df_meters = new DecimalFormat("#####");
        altimeter.setText(String.valueOf(df_meters.format(altitude)) + " m");

        TextView altimeterInFoot = (TextView) findViewById(R.id.altimeterft);
        double footSize = 3.28084;
        DecimalFormat df_ft = new DecimalFormat("#####");
        altimeterInFoot.setText(String.valueOf(df_ft.format(altitude * footSize)) + " ft");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, pressure, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }
}