package com.example.rakshakx;




import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class EmergencyActivity extends AppCompatActivity {

    private GridLayout gridEmergencyServices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);

        gridEmergencyServices = findViewById(R.id.gridEmergencyServices);

        setupEmergencyButtons();
    }

    private void setupEmergencyButtons() {
        // 7 Emergency Services
        String[][] services = {
                {"🚓", "POLICE", "100", "#2196F3"},
                {"🚒", "FIRE", "101", "#FF9800"},
                {"🚑", "MEDICAL", "102", "#4CAF50"},
                {"🌊", "DISASTER", "108", "#FF5722"},
                {"👩", "WOMAN", "1091", "#E91E63"},
                {"👶", "CHILD", "1098", "#9C27B0"},
                {"🇮🇳", "NATIONAL", "112", "#F44336"}
        };

        for (String[] service : services) {
            Button button = new Button(this);

            // Set text with emoji, title and number
            button.setText(service[0] + " " + service[1] + "\n" + service[2]);
            button.setPadding(16, 32, 16, 32);
            button.setTextSize(16f);
            button.setAllCaps(false);

            // Set background color
            button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.parseColor(service[3])));

            button.setTextColor(android.graphics.Color.WHITE);

            // GridLayout parameters
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(12, 12, 12, 12);
            button.setLayoutParams(params);

            // Direct call on click
            final String phoneNumber = service[2];
            button.setOnClickListener(v -> makeDirectCall(phoneNumber));

            gridEmergencyServices.addView(button);
        }
    }

    private void makeDirectCall(String phoneNumber) {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + phoneNumber));

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                == PackageManager.PERMISSION_GRANTED) {
            startActivity(callIntent);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "✅ Call permission granted. Tap again to call.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "❌ Call permission required", Toast.LENGTH_SHORT).show();
        }
    }
}