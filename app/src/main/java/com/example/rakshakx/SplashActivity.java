package com.example.rakshakx;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    ImageView logo;
    TextView appName, tagline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        logo = findViewById(R.id.logo);
        appName = findViewById(R.id.appName);
        tagline = findViewById(R.id.tagline);

        // Logo Floating Animation
        ObjectAnimator floatAnim = ObjectAnimator.ofFloat(logo, "translationY", -20f, 20f);
        floatAnim.setDuration(1800);
        floatAnim.setRepeatCount(ObjectAnimator.INFINITE);
        floatAnim.setRepeatMode(ObjectAnimator.REVERSE);
        floatAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        floatAnim.start();

        // Fade Animation
        logo.setAlpha(0f);
        logo.animate().alpha(1f).setDuration(1200).start();

        appName.setAlpha(0f);
        appName.animate().alpha(1f).setDuration(1800).start();

        tagline.setAlpha(0f);
        tagline.animate().alpha(1f).setDuration(2200).start();

        // Go to WelcomeActivity
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, WelcomeActivity.class);
            startActivity(intent);
            finish();
        }, 3500);
    }
}