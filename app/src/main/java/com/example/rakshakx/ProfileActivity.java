package com.example.rakshakx;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    // CardView 1 - User Info
    private TextView tvFullName, tvEmail, tvPhone;
    private ImageView ivEditProfile;

    // CardView 2 - Personal Details
    private TextView tvState, tvDob, tvGender, tvAboutMe, tvEContact;

    // Logout Button
    private Button btnLogout;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseRef;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            userId = user.getUid();
        }

        // Initialize views
        initViews();

        // Load user data
        loadUserData();

        // Setup click listeners
        setupClickListeners();
    }

    private void initViews() {
        // CardView 1
        tvFullName = findViewById(R.id.tv_full_name);
        tvEmail = findViewById(R.id.tv_email);
        tvPhone = findViewById(R.id.tv_phone);
        ivEditProfile = findViewById(R.id.iv_edit_profile);

        // CardView 2
        tvState = findViewById(R.id.tv_state);
        tvDob = findViewById(R.id.tv_dob);
        tvGender = findViewById(R.id.tv_gender);
        tvAboutMe = findViewById(R.id.tv_about_me);
        tvEContact = findViewById(R.id.tv_e_contact);

        // Logout Button
        btnLogout = findViewById(R.id.btn_logout);
    }

    private void loadUserData() {
        databaseRef.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // CardView 1
                    String name = snapshot.child("name").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String phone = snapshot.child("phone").getValue(String.class);

                    tvFullName.setText(name != null ? name : "Not set");
                    tvEmail.setText(email != null ? email : "Not set");
                    tvPhone.setText(phone != null ? phone : "Not set");

                    // CardView 2
                    String state = snapshot.child("state").getValue(String.class);
                    String dob = snapshot.child("dob").getValue(String.class);
                    String gender = snapshot.child("gender").getValue(String.class);
                    String aboutMe = snapshot.child("aboutMe").getValue(String.class);
                    String eContact = snapshot.child("eContact").getValue(String.class);

                    tvState.setText(state != null ? state : "Not set");
                    tvDob.setText(dob != null ? dob : "Not set");
                    tvGender.setText(gender != null ? gender : "Not set");
                    tvAboutMe.setText(aboutMe != null ? aboutMe : "Not set");
                    tvEContact.setText(eContact != null ? eContact : "Not set");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Error loading data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupClickListeners() {
        // Single Edit Icon - Updates entire profile (opens edit screen)
        ivEditProfile.setOnClickListener(v -> {
            // Pass current data to edit screen
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            intent.putExtra("full_name", tvFullName.getText().toString());
            intent.putExtra("email", tvEmail.getText().toString());
            intent.putExtra("phone", tvPhone.getText().toString());
            intent.putExtra("state", tvState.getText().toString());
            intent.putExtra("dob", tvDob.getText().toString());
            intent.putExtra("gender", tvGender.getText().toString());
            intent.putExtra("about_me", tvAboutMe.getText().toString());
            intent.putExtra("e_contact", tvEContact.getText().toString());
            startActivity(intent);
        });

        // Logout Button
        btnLogout.setOnClickListener(v -> logout());
    }

    private void logout() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Logout");
        builder.setMessage("Are you sure you want to logout?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            mAuth.signOut();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when coming back from edit screen
        loadUserData();
    }
}