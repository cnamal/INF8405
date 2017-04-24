package com.ensipoly.project;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.ensipoly.project.utils.StepDetector;

/**
 * Created by Adrien on 23/04/2017.
 */

public class StepsCounter implements SensorEventListener {

    private SensorManager sensorManager;
    private TextView count;
    private Activity currentActivity;
    private int nbSteps = 0;

    // Useful only for under KitKat version
    private StepDetector stepDetector;

    public StepsCounter(Activity activity){
        currentActivity = activity;
        sensorManager = (SensorManager) currentActivity.getSystemService(Context.SENSOR_SERVICE);
        count = (TextView) currentActivity.findViewById(R.id.stepText);
    }

    public void registerListener(){
        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
            if (countSensor != null) {
                sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_UI);
            } else {
                Toast.makeText(currentActivity, "Count sensor not available!", Toast.LENGTH_LONG).show();
            }
        } else {
            Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (countSensor != null) {
                sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_UI);
                stepDetector = new StepDetector();
            } else {
                Toast.makeText(currentActivity, "Count sensor not available!", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void unregisterListener(){
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            this.step();
        } else {
            if(stepDetector.isAStep(event.timestamp, event.values[0], event.values[1], event.values[2]))
                this.step();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void step(){
        nbSteps++;
        count.setText("Steps: " + nbSteps);
    }

    public void resetCounter(){
        this.nbSteps = 0;
    }

}
