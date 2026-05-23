package com.example.rakshakx;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etPhone, etState, etDob, etGender, etAboutMe, etEContact;
    private Button btnUpdate;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseRef;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference();
        userId = mAuth.getCurrentUser().getUid();

        // Initialize views
        etFullName = findViewById(R.id.et_full_name);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        etState = findViewById(R.id.et_state);
        etDob = findViewById(R.id.et_dob);
        etGender = findViewById(R.id.et_gender);
        etAboutMe = findViewById(R.id.et_about_me);
        etEContact = findViewById(R.id.et_e_contact);
        btnUpdate = findViewById(R.id.btn_update);

        // Load existing data from Firebase (not from intent)
        loadDataFromFirebase();

        // Update button click
        btnUpdate.setOnClickListener(v -> updateProfile());
    }

    private void loadDataFromFirebase() {
        databaseRef.child("users").child(userId).get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                // Agar data hai toh dikhao
                String name = snapshot.child("name").getValue(String.class);
                String email = snapshot.child("email").getValue(String.class);
                String phone = snapshot.child("phone").getValue(String.class);
                String state = snapshot.child("state").getValue(String.class);
                String dob = snapshot.child("dob").getValue(String.class);
                String gender = snapshot.child("gender").getValue(String.class);
                String aboutMe = snapshot.child("aboutMe").getValue(String.class);
                String eContact = snapshot.child("eContact").getValue(String.class);

                if (name != null && !name.equals("Not set")) etFullName.setText(name);
                if (email != null && !email.equals("Not set")) etEmail.setText(email);
                if (phone != null && !phone.equals("Not set")) etPhone.setText(phone);
                if (state != null && !state.equals("Not set")) etState.setText(state);
                if (dob != null && !dob.equals("Not set")) etDob.setText(dob);
                if (gender != null && !gender.equals("Not set")) etGender.setText(gender);
                if (aboutMe != null && !aboutMe.equals("Not set")) etAboutMe.setText(aboutMe);
                if (eContact != null && !eContact.equals("Not set")) etEContact.setText(eContact);
            }
        });
    }

    private void updateProfile() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String state = etState.getText().toString().trim();
        String dob = etDob.getText().toString().trim();
        String gender = etGender.getText().toString().trim();
        String aboutMe = etAboutMe.getText().toString().trim();
        String eContact = etEContact.getText().toString().trim();

        if (fullName.isEmpty()) {
            etFullName.setError("Full Name is required");
            etFullName.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", fullName);
        updates.put("email", email);
        updates.put("phone", phone);
        updates.put("state", state);
        updates.put("dob", dob);
        updates.put("gender", gender);
        updates.put("aboutMe", aboutMe);
        updates.put("eContact", eContact);

        databaseRef.child("users").child(userId).updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}