package com.example.rakshakx;



import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.rakshakx.R;

public class SOSActivity extends AppCompatActivity implements SensorEventListener {

    TextView txtStatus;
    Button btnAddContact, btnCancel;
    SensorManager sensorManager;
    Sensor accelerometer;

    private long lastUpdate = 0;
    private float lastX = 0, lastY = 0, lastZ = 0;
    private static final int SHAKE_THRESHOLD = 800;

    SharedPreferences sharedPreferences;
    String savedNumber = "";
    String savedName = "";

    CountDownTimer countDownTimer;
    boolean isTimerRunning = false;

    private String pendingCallNumber = ""; // For permission handling

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos);

        txtStatus = findViewById(R.id.txtStatus);
        btnAddContact = findViewById(R.id.btnAddContact);
        btnCancel = findViewById(R.id.btnCancel);

        // Load saved contact
        sharedPreferences = getSharedPreferences("SOS_PREF", MODE_PRIVATE);
        savedNumber = sharedPreferences.getString("phone", "");
        savedName = sharedPreferences.getString("name", "");

        if (!savedNumber.isEmpty()) {
            txtStatus.setText("✓ Ready - Shake to call " + savedName);
        } else {
            txtStatus.setText("⚠️ Add emergency contact first");
        }

        // Setup sensor
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (accelerometer != null) {
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            }
        }

        btnAddContact.setOnClickListener(v -> showContactDialog());
        btnCancel.setOnClickListener(v -> cancelSOS());
    }

    private void showContactDialog() {
        // Create dialog layout programmatically (no separate XML file needed)
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_contact, null);

        // If dialog_contact.xml doesn't exist, create it (see below)
        EditText etName = view.findViewById(R.id.etName);
        EditText etPhone = view.findViewById(R.id.etPhone);

        // Pre-fill existing contact
        if (!savedName.isEmpty()) {
            etName.setText(savedName);
        }
        if (!savedNumber.isEmpty()) {
            etPhone.setText(savedNumber);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Emergency Contact");
        builder.setView(view);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();

            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Please enter name and phone number", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("name", name);
            editor.putString("phone", phone);
            editor.apply();

            savedName = name;
            savedNumber = phone;

            txtStatus.setText("✓ Ready - Shake to call " + name);
            Toast.makeText(this, "Emergency contact saved!", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void cancelSOS() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        isTimerRunning = false;
        txtStatus.setText("SOS Cancelled ✓");
        btnCancel.setVisibility(View.GONE);

        // Reset status after 2 seconds
        txtStatus.postDelayed(() -> {
            if (!savedNumber.isEmpty()) {
                txtStatus.setText("✓ Ready - Shake to call " + savedName);
            } else {
                txtStatus.setText("⚠️ Add emergency contact first");
            }
        }, 2000);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            long currentTime = System.currentTimeMillis();

            if ((currentTime - lastUpdate) > 100) {
                long diffTime = currentTime - lastUpdate;
                lastUpdate = currentTime;

                float speed = Math.abs(x + y + z - lastX - lastY - lastZ) / diffTime * 10000;

                if (speed > SHAKE_THRESHOLD && !isTimerRunning) {
                    startCountdown();
                }

                lastX = x;
                lastY = y;
                lastZ = z;
            }
        }
    }

    private void startCountdown() {
        if (savedNumber.isEmpty()) {
            Toast.makeText(this, "⚠️ Please add emergency contact first!", Toast.LENGTH_LONG).show();
            txtStatus.setText("⚠️ Add emergency contact");
            return;
        }

        isTimerRunning = true;
        btnCancel.setVisibility(View.VISIBLE);

        countDownTimer = new CountDownTimer(5000, 1000) {  // 5 seconds countdown
            @Override
            public void onTick(long millisUntilFinished) {
                txtStatus.setText("🚨 SOS in " + millisUntilFinished / 1000 + " seconds... Shake to cancel?");
            }

            @Override
            public void onFinish() {
                txtStatus.setText("📞 Calling " + savedName + "...");
                btnCancel.setVisibility(View.GONE);
                isTimerRunning = false;
                makeCall(savedNumber);
            }
        }.start();
    }

    private void makeCall(String number) {
        pendingCallNumber = number;

        // Check permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            // Request permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE},
                    100);
            return;
        }

        // Make the call
        try {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + number));
            startActivity(intent);
            Toast.makeText(this, "📞 Calling " + savedName + "...", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Call failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            txtStatus.setText("❌ Call failed - Check number");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not needed
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted - make the call
                makeCall(pendingCallNumber);
                Toast.makeText(this, "Permission granted - Calling...", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "❌ Call permission denied. Cannot make emergency call.", Toast.LENGTH_LONG).show();
                txtStatus.setText("❌ Permission denied - Add contact again");
                isTimerRunning = false;
                btnCancel.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sensorManager != null && accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}