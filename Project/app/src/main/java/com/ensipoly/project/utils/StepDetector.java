package com.ensipoly.project.utils;

import android.util.Log;

/**
 * Created by Adrien on 23/04/2017.
 */

public class StepDetector {

    private final int ACCELERATION_FILTER_SIZE = 50;
    private final int VELOCITY_FILTER_SIZE = 10;
    private final double STEP_THRESHOLD = 9.6;
    private final int STEP_DELAY_NS = 250000000;

    private int accelerationFilterCounter = 0;
    private Vector[] accelerationFilter = new Vector[ACCELERATION_FILTER_SIZE];
    private int velocityFilterCounter = 0;
    private double[] velocityFilter = new double[VELOCITY_FILTER_SIZE];
    private long lastStepTimeNs = 0;
    private float oldVelocityEstimate = 0;

    public StepDetector(){
        for(int i = 0; i < ACCELERATION_FILTER_SIZE; i++){
            accelerationFilter[i] = new Vector();
        }
    }

    public boolean isAStep(long time, double x, double y, double z){
        boolean step = false;
        Vector currentAccel = new Vector(x,y,z);

        accelerationFilterCounter++;
        accelerationFilter[accelerationFilterCounter % ACCELERATION_FILTER_SIZE] = currentAccel;

        Vector worldZ;
        worldZ = Vector.sum(accelerationFilter).normalize();

        double currentZ = Vector.dot(worldZ, currentAccel.normalize());
        velocityFilterCounter++;
        velocityFilter[velocityFilterCounter % VELOCITY_FILTER_SIZE] = currentZ;

        float velocityEstimate = 0;
        for(double d : velocityFilter)
            velocityEstimate += d;

        if (velocityEstimate > STEP_THRESHOLD && oldVelocityEstimate <= STEP_THRESHOLD
                && (time - lastStepTimeNs > STEP_DELAY_NS)) {
            step = true;
            lastStepTimeNs = time;
        }
        oldVelocityEstimate = velocityEstimate;
        return step;
    }
}
