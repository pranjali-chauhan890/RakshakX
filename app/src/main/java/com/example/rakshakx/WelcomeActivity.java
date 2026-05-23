package com.example.rakshakx;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.rakshakx.LoginActivity;
import com.example.rakshakx.R;
import com.example.rakshakx.RegisterActivity;

public class WelcomeActivity extends AppCompatActivity {

    ImageView imgLogo;
    TextView txtTitle;
    Button btnGetStarted, btnAlready, btnEmergencyCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        imgLogo = findViewById(R.id.imgLogo);
        txtTitle = findViewById(R.id.txtTitle);

        btnGetStarted = findViewById(R.id.btnGetStarted);
        btnAlready = findViewById(R.id.btnAlready);
        btnEmergencyCall = findViewById(R.id.btnEmergencyCall);

        Animation fade =
                AnimationUtils.loadAnimation(this, R.anim.fade_in);

        Animation slide =
                AnimationUtils.loadAnimation(this, R.anim.slide_up);

        imgLogo.startAnimation(fade);
        txtTitle.startAnimation(fade);
        btnGetStarted.startAnimation(slide);
        btnAlready.startAnimation(slide);

        // 🆘 EMERGENCY BUTTON ANIMATION
        if (btnEmergencyCall != null) {
            btnEmergencyCall.startAnimation(slide);
        }

        // 🆘 EMERGENCY BUTTON - DIRECT CALL TO 112 (No dialer)
        btnEmergencyCall.setOnClickListener(v -> {
            makeDirectCall();
        });

        // REGISTER BUTTON
        btnGetStarted.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            WelcomeActivity.this,
                            RegisterActivity.class
                    );

            startActivity(intent);

        });

        // LOGIN BUTTON
        btnAlready.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            WelcomeActivity.this,
                            LoginActivity.class
                    );

            startActivity(intent);

        });

    }

    // 🆘 DIRECT CALL TO 112 - Call lagega immediately, dialer nahi khulega
    private void makeDirectCall() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                == PackageManager.PERMISSION_GRANTED) {
            try {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:112"));
                startActivity(callIntent);
            } catch (SecurityException e) {
                Toast.makeText(this, "❌ Call permission required", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Request permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE}, 100);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission granted, make the call
            makeDirectCall();
        } else {
            Toast.makeText(this, "❌ Call permission denied. Cannot make emergency call.", Toast.LENGTH_SHORT).show();
        }
    }
}