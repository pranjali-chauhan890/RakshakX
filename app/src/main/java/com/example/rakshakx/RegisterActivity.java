package com.example.rakshakx;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvLogin;
    private ProgressBar progressBar;

    private RadioGroup roleGroup;
    private RadioButton rbCitizen, rbAuthority;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference();

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);

        roleGroup = findViewById(R.id.roleGroup);
        rbCitizen = findViewById(R.id.rbCitizen);
        rbAuthority = findViewById(R.id.rbAuthority);

        btnRegister.setOnClickListener(v -> registerUser());

        tvLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void registerUser() {

        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        String role = rbCitizen.isChecked() ? "Citizen" : "Authority";

        if (name.isEmpty()) {
            etName.setError("Full name is required");
            etName.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter valid email");
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password required");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Minimum 6 characters");
            etPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        showProgress(true);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {

                    showProgress(false);

                    if (task.isSuccessful()) {

                        FirebaseUser firebaseUser = mAuth.getCurrentUser();

                        if (firebaseUser != null) {

                            saveUserData(
                                    firebaseUser.getUid(),
                                    name,
                                    email,
                                    role
                            );

                            Toast.makeText(RegisterActivity.this,
                                    "Registration Successful! Please Login.",
                                    Toast.LENGTH_SHORT).show();

                            Intent intent =
                                    new Intent(RegisterActivity.this,
                                            LoginActivity.class);

                            startActivity(intent);
                            finish();
                        }

                    } else {

                        Toast.makeText(RegisterActivity.this,
                                "Registration Failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserData(String userId,
                              String name,
                              String email,
                              String role) {

        Map<String, Object> userData = new HashMap<>();

        // Basic Info
        userData.put("userId", userId);
        userData.put("name", name);
        userData.put("email", email);
        userData.put("role", role);
        userData.put("createdAt", System.currentTimeMillis());

        // ========== ✅ YAHAN SE DEFAULT PROFILE FIELDS ADD KAREIN ==========
        // Profile Fields (Empty strings - user can fill later)
        userData.put("phone", "");           // Phone number
        userData.put("state", "");           // State
        userData.put("dob", "");             // Date of Birth
        userData.put("gender", "");          // Gender
        userData.put("aboutMe", "");         // About Me
        userData.put("eContact", "");        // Emergency Contact

        // Settings Fields (Default values)
        userData.put("isVolunteer", false);   // Volunteer status
        userData.put("language", "English");  // Language preference
        userData.put("isNotification", false); // Notification preference
        // ========== DEFAULT FIELDS ADD KARNE KA END ==========

        databaseRef.child("users")
                .child(userId)
                .setValue(userData)
                .addOnSuccessListener(unused -> {
                    // Data saved successfully
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(RegisterActivity.this,
                            "Failed to save user data: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void showProgress(boolean show) {

        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }

        btnRegister.setEnabled(!show);

        btnRegister.setText(show ?
                "Creating Account..." :
                "Sign Up");
    }
}