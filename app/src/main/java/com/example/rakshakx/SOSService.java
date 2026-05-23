package com.example.rakshakx;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.widget.Toast;

public class SOSService extends Service implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private long lastShakeTime = 0;
    private static final int SHAKE_THRESHOLD = 15;
    private static final int SHAKE_INTERVAL = 1000;

    @Override
    public void onCreate() {
        super.onCreate();
        setupShakeDetection();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Start without foreground (simpler, works on all versions)
        setupShakeDetection();
        Toast.makeText(this, "SOS Service Started - Shake phone for emergency", Toast.LENGTH_SHORT).show();
        return START_STICKY;
    }

    private void setupShakeDetection() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (accelerometer != null) {
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            float gForce = (x * x + y * y + z * z) / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);

            if (gForce > SHAKE_THRESHOLD) {
                long now = System.currentTimeMillis();
                if (now - lastShakeTime > SHAKE_INTERVAL) {
                    lastShakeTime = now;
                    sendEmergencySMS();
                }
            }
        }
    }

    private void sendEmergencySMS() {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            String message = "EMERGENCY SOS! I need help! Using RakshakX App";
            String phoneNumber = "5551234567";
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(this, "🚨 SOS Alert Sent!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "SMS Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }
}