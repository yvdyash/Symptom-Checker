package com.example.tuesdayfirstapp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.Date;

public class AccelerometerService extends Service implements SensorEventListener {

    private SensorManager accSensorManager;
    private Sensor accelSensor;
    private long startTime;
    private ArrayList<Integer> xCoordinateVals = new ArrayList<>();
    private ArrayList<Integer> yCoordinateVals = new ArrayList<>();
    private ArrayList<Integer> zCoordinateVals = new ArrayList<>();

    @Override
    public void onCreate(){
        accSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelSensor = accSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        accSensorManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startTime = new Date().getTime();
        xCoordinateVals.clear();
        yCoordinateVals.clear();
        zCoordinateVals.clear();
        return START_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        Sensor sensor = sensorEvent.sensor;
        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            int multiplier = 100;
            xCoordinateVals.add((int)(sensorEvent.values[0]*multiplier));
            yCoordinateVals.add((int)(sensorEvent.values[1]*multiplier));
            zCoordinateVals.add((int)(sensorEvent.values[2]*multiplier));

            //Sensing stops after 45s at approximately 230 data points
//            if(accelValuesZ.size() >= 230){
//                Log.i("log", "done acc!");
//                stopSelf();
//            }
            if(new Date().getTime() - startTime >= 45*1000){
                Log.i("log", "Accelerometer z coordinates size: "+ zCoordinateVals.size());
                stopSelf();
            }
        }
    }

    @Override
    public void onDestroy(){

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                accSensorManager.unregisterListener(AccelerometerService.this);
                Log.i("Log", "Service stopping");

                //Broadcasts accelerometer Z values
                Intent intent = new Intent("RespDataBroadcast");
                Bundle bundle = new Bundle();
                bundle.putIntegerArrayList("accelValuesZ", zCoordinateVals);
                intent.putExtras(bundle);
                LocalBroadcastManager.getInstance(AccelerometerService.this).sendBroadcast(intent);
            }
        });
        thread.start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        //
        return null;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //
    }
}
