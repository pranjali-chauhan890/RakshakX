package com.example.rakshakx;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvSignUp, tvForgotPassword;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Views
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignUp = findViewById(R.id.tvSignUp);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        // Login Button
        btnLogin.setOnClickListener(v -> loginUser());

        // Go to Register
        tvSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this,
                    RegisterActivity.class);
            startActivity(intent);
        });

        // Forgot Password
        tvForgotPassword.setOnClickListener(v -> forgotPassword());
    }

    private void loginUser() {

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validation
        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        showProgress(true);

        // Firebase Login
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this,
                        new OnCompleteListener<AuthResult>() {

                            @Override
                            public void onComplete(
                                    @NonNull Task<AuthResult> task) {

                                showProgress(false);

                                if (task.isSuccessful()) {

                                    FirebaseUser firebaseUser =
                                            mAuth.getCurrentUser();

                                    if (firebaseUser != null) {

                                        String userId =
                                                firebaseUser.getUid();

                                        DatabaseReference ref =
                                                FirebaseDatabase
                                                        .getInstance()
                                                        .getReference("users")
                                                        .child(userId);

                                        // Fetch role from database
                                        ref.get().addOnCompleteListener(task1 -> {

                                            if (task1.isSuccessful()) {

                                                com.google.firebase.database.DataSnapshot snapshot =
                                                        task1.getResult();

                                                String role = "Citizen";

                                                if (snapshot.exists()) {

                                                    String dbRole = snapshot.child("role")
                                                            .getValue(String.class);

                                                    if (dbRole != null) {
                                                        role = dbRole;
                                                    }
                                                }

                                                Toast.makeText(LoginActivity.this,
                                                        "Login Successful!",
                                                        Toast.LENGTH_SHORT).show();

                                                Intent intent;

                                                if (role.equalsIgnoreCase("Authority")) {

                                                    intent = new Intent(
                                                            LoginActivity.this,
                                                            AuthorityDashboardActivity.class
                                                    );

                                                } else {

                                                    intent = new Intent(
                                                            LoginActivity.this,
                                                            MainActivity.class
                                                    );
                                                }

                                                intent.setFlags(
                                                        Intent.FLAG_ACTIVITY_NEW_TASK |
                                                                Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                );

                                                startActivity(intent);
                                                finish();

                                            } else {

                                                Toast.makeText(LoginActivity.this,
                                                        "Failed to fetch user data",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }

                                } else {

                                    String errorMessage =
                                            task.getException().getMessage();

                                    if (errorMessage != null &&
                                            errorMessage.contains("There is no user record")) {

                                        Toast.makeText(
                                                LoginActivity.this,
                                                "No account found! Please sign up.",
                                                Toast.LENGTH_LONG
                                        ).show();

                                    } else {

                                        Toast.makeText(
                                                LoginActivity.this,
                                                "Login Failed: " + errorMessage,
                                                Toast.LENGTH_LONG
                                        ).show();
                                    }
                                }
                            }
                        });
    }

    private void forgotPassword() {

        String email = etEmail.getText().toString().trim();

        if (email.isEmpty()) {

            Toast.makeText(
                    this,
                    "Enter your email address first",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        showProgress(true);

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {

                    showProgress(false);

                    if (task.isSuccessful()) {

                        Toast.makeText(
                                LoginActivity.this,
                                "Password reset email sent to " + email,
                                Toast.LENGTH_LONG
                        ).show();

                    } else {

                        Toast.makeText(
                                LoginActivity.this,
                                "Failed: " +
                                        task.getException().getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    private void showProgress(boolean show) {

        if (progressBar != null) {
            progressBar.setVisibility(
                    show ? View.VISIBLE : View.GONE
            );
        }

        btnLogin.setEnabled(!show);

        btnLogin.setText(
                show ? "Logging in..." : "Login"
        );
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (etPassword != null) {
            etPassword.setText("");
        }
    }
}