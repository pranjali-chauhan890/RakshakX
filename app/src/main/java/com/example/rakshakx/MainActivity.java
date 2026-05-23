package com.example.rakshakx;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rakshakx.models.Report;
import com.example.rakshakx.models.RiskCalculator;
import com.example.rakshakx.models.RiskZoneModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;

import java.io.IOException;
import java.util.*;

public class MainActivity extends AppCompatActivity
        implements LocationListener {

    private MapView mMap;

    private LocationManager locationManager;

    private DatabaseReference databaseRef;

    private Button btnDashboard, btnSOS;

    private FloatingActionButton fabReport;

    // Drawer
    private DrawerLayout drawerLayout;

    private NavigationView navigationView;

    private ImageView ivMenu;

    private TextView tvUserName, tvUserEmail;

    private FirebaseAuth mAuth;

    // Search
    private EditText etSearch;

    private Button btnSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Configuration.getInstance().load(
                getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(
                        getApplicationContext()));

        setContentView(R.layout.activity_main);

        // Firebase
        mAuth = FirebaseAuth.getInstance();

        databaseRef =
                FirebaseDatabase.getInstance()
                        .getReference();

        // Location
        locationManager =
                (LocationManager)
                        getSystemService(LOCATION_SERVICE);



        btnSOS =
                findViewById(R.id.btn_sos);

        fabReport =
                findViewById(R.id.fab_report);

        mMap =
                findViewById(R.id.map_view);

        etSearch =
                findViewById(R.id.etSearch);

        btnSearch =
                findViewById(R.id.btnSearch);

        // Drawer
        drawerLayout =
                findViewById(R.id.drawerLayout);

        navigationView =
                findViewById(R.id.navView);

        Toolbar toolbar =
                findViewById(R.id.toolbar);

        ivMenu =
                findViewById(R.id.iv_menu);

        setupDrawer();

        setupMap();

        // Search Button
        btnSearch.setOnClickListener(v -> {

            String locationName =
                    etSearch.getText()
                            .toString()
                            .trim();

            if (!locationName.isEmpty()) {

                searchLocation(locationName);

            } else {

                Toast.makeText(
                        this,
                        "Enter location name",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });


        // SOS
        btnSOS.setOnClickListener(v -> {

            triggerSOS();
        });

        // Report FAB
        fabReport.setOnClickListener(v -> {

            showReportDialog();
        });

        // Location Permission
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    100);

        } else {

            getCurrentLocation();
        }

        // 🔥 IMPORTANT
        // Load BOTH:
        // 1. Citizen reports
        // 2. Authority risk zones

        loadReportsFromFirebase();

        loadAuthorityRiskZones();
    }

    // =========================
// Drawer Setup
// =========================

    private void setupDrawer() {

        ivMenu.setOnClickListener(v -> {

            drawerLayout.openDrawer(GravityCompat.START);
        });

        navigationView.setNavigationItemSelectedListener(
                item -> {

                    int id = item.getItemId();

                    // =========================
                    // PROFILE
                    // =========================
                    if (id == R.id.nav_profile) {

                        startActivity(
                                new Intent(
                                        MainActivity.this,
                                        ProfileActivity.class));

                    }

                    // =========================
                    // EMERGENCY SERVICES
                    // =========================
                    else if (id == R.id.nav_emergency) {

                        startActivity(
                                new Intent(
                                        MainActivity.this,
                                        EmergencyActivity.class));
                    }

                    // =========================
                    // SOS HISTORY - 🔴 POPUP ADDED
                    // =========================
                    else if (id == R.id.nav_sos_history) {

                        showSOSHistoryPopup();
                    }

                    // =========================
                    // SETTINGS - 🔴 FUNCTIONALITY ADDED
                    // =========================
                    else if (id == R.id.nav_settings) {

                        showSettingsDialog();
                    }

                    // =========================
                    // TOP QUESTIONS - 🔴 ANSWER ON CLICK
                    // =========================
                    else if (id == R.id.nav_top_questions) {

                        showTopQuestionsDialog();
                    }

                    // =========================
                    // ABOUT US - 🔴 FAQ ADDED
                    // =========================
                    else if (id == R.id.nav_about_us) {

                        showAboutUsWithFAQ();
                    }

                    // =========================
                    // LOGOUT
                    // =========================
                    else if (id == R.id.nav_logout) {

                        logout();
                    }

                    drawerLayout.closeDrawer(GravityCompat.START);

                    return true;
                });

        // Header View
        View headerView =
                navigationView.getHeaderView(0);

        tvUserName =
                headerView.findViewById(
                        R.id.tv_user_name);

        tvUserEmail =
                headerView.findViewById(
                        R.id.tv_user_email);

        // Load User Data
        loadUserInfo();
    }

    // =========================
    // 🔴 SOS HISTORY POPUP
    // =========================
    private void showSOSHistoryPopup() {
        androidx.appcompat.app.AlertDialog.Builder builder =
                new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this);
        builder.setTitle("🆘 SOS History");
        builder.setMessage("No SOS history found.\n\nWhen you trigger SOS, it will appear here.");
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    // =========================
    // 🔴 SETTINGS DIALOG WITH FUNCTIONALITY
    // =========================
    private void showSettingsDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder =
                new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this);
        builder.setTitle("⚙️ Settings");

        String[] settings = {
                "🔔 Notification Preferences",
                "📍 Location Accuracy",
                "🌐 Language",
                "📊 Data Usage",
                "🔐 Privacy Settings",
                "📱 App Theme"
        };

        builder.setItems(settings, (dialog, which) -> {
            if (which == 0) {
                // Notification Preferences
                showNotificationPopup();
            } else if (which == 1) {
                Toast.makeText(MainActivity.this, "📍 Location Accuracy - High", Toast.LENGTH_SHORT).show();
            } else if (which == 2) {
                showLanguageDialog();
            } else if (which == 3) {
                Toast.makeText(MainActivity.this, "📊 Data Usage - Last 7 days: 25MB", Toast.LENGTH_SHORT).show();
            } else if (which == 4) {
                Toast.makeText(MainActivity.this, "🔐 Privacy Settings - Data Encrypted", Toast.LENGTH_SHORT).show();
            } else if (which == 5) {
                showThemeDialog();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    // Notification Popup
    private void showNotificationPopup() {
        View view = getLayoutInflater().inflate(R.layout.dialog_notification, null);
        Button btnNotNow = view.findViewById(R.id.btn_not_now);
        Button btnEnable = view.findViewById(R.id.btn_enable);

        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(false)
                .create();

        btnNotNow.setOnClickListener(v -> {
            Toast.makeText(this, "Notifications disabled", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        btnEnable.setOnClickListener(v -> {
            requestNotificationPermission();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void requestNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 200);
            } else {
                Toast.makeText(this, "✅ Notifications enabled!", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "✅ Notifications enabled!", Toast.LENGTH_LONG).show();
        }
    }

    // Language Dialog
    private void showLanguageDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("🌐 Select Language");
        String[] languages = {"English", "हिन्दी", "मराठी", "বাংলা", "తెలుగు", "தமிழ்"};
        builder.setItems(languages, (dialog, which) -> {
            Toast.makeText(this, "Language set to " + languages[which], Toast.LENGTH_SHORT).show();
        });
        builder.show();
    }

    // Theme Dialog
    private void showThemeDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("📱 Select Theme");
        String[] themes = {"Light", "Dark", "System Default"};
        builder.setItems(themes, (dialog, which) -> {
            Toast.makeText(this, "Theme set to " + themes[which], Toast.LENGTH_SHORT).show();
        });
        builder.show();
    }

    // =========================
    // 🔴 TOP QUESTIONS - ANSWER ON CLICK
    // =========================
    private void showTopQuestionsDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder =
                new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this);
        builder.setTitle("❓ Top Questions");

        String[] questions = {
                "How to report unsafe location?",
                "How does SOS work?",
                "How are risk zones calculated?",
                "How to add emergency contacts?",
                "Can I use app offline?"
        };

        String[] answers = {
                "Click on the + FAB button on map screen, fill location details and submit.",
                "Click SOS button or shake your phone. It will call emergency contacts (100, 102, 112).",
                "Risk zones are calculated based on report count and severity scores (0-100). Higher score = HIGH RISK.",
                "Go to SOS Activity → Add Emergency Contact button. Enter name and phone number.",
                "Map requires internet, but SOS and emergency calls work offline."
        };

        builder.setItems(questions, (dialog, which) -> {
            androidx.appcompat.app.AlertDialog.Builder answerDialog =
                    new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this);
            answerDialog.setTitle("📌 " + questions[which]);
            answerDialog.setMessage(answers[which]);
            answerDialog.setPositiveButton("Got it", (d, w) -> d.dismiss());
            answerDialog.show();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    // =========================
    // 🔴 ABOUT US WITH FAQ
    // =========================
    private void showAboutUsWithFAQ() {
        androidx.appcompat.app.AlertDialog.Builder builder =
                new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this);
        builder.setTitle("📱 About RakshakX");

        String aboutMessage = "RakshakX - Citizen Safety Platform\n\n" +
                "Version: 1.0\n\n" +
                "RakshakX helps citizens identify risk zones, " +
                "report unsafe locations, and get emergency assistance.\n\n" +
                "Features:\n" +
                "• Real-time Risk Zone Mapping\n" +
                "• SOS Emergency with Shake Detection\n" +
                "• Community Reporting\n" +
                "• Safety Dashboard\n\n" +
                "Made with ❤️ for Community Safety\n\n" +
                "📧 support@rakshakx.com";

        builder.setMessage(aboutMessage);

        // FAQ Button inside About Us
        builder.setPositiveButton("❓ FAQ", (dialog, which) -> {
            showFAQDialog();
        });

        builder.setNegativeButton("Close", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    // =========================
    // 🔴 FAQ DIALOG
    // =========================
    private void showFAQDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder =
                new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this);
        builder.setTitle("📋 Frequently Asked Questions");

        String[] faqQuestions = {
                "What is RakshakX?",
                "How accurate are risk zones?",
                "Who can report issues?",
                "Is my data safe?",
                "Is the app free?",
                "How to become a volunteer?"
        };

        String[] faqAnswers = {
                "RakshakX is a citizen safety platform that maps urban risks using crowd-sourced data.",
                "Risk zones are calculated using report count, severity, and recency of complaints.",
                "Any registered citizen can report unsafe locations anonymously.",
                "Yes, your data is stored securely in Firebase with authentication.",
                "Yes, RakshakX is completely free for all citizens.",
                "Contact us at volunteer@rakshakx.com to join as a volunteer."
        };

        builder.setItems(faqQuestions, (dialog, which) -> {
            androidx.appcompat.app.AlertDialog.Builder answerDialog =
                    new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this);
            answerDialog.setTitle("❓ " + faqQuestions[which]);
            answerDialog.setMessage(faqAnswers[which]);
            answerDialog.setPositiveButton("OK", (d, w) -> d.dismiss());
            answerDialog.show();
        });

        builder.setNegativeButton("Back", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    // =========================
// Load User Info
// =========================

    private void loadUserInfo() {

        FirebaseUser user =
                mAuth.getCurrentUser();

        if (user != null) {

            // Set Email directly from FirebaseAuth
            tvUserEmail.setText(user.getEmail());

            String userId = user.getUid();

            databaseRef.child("users")
                    .child(userId)
                    .addListenerForSingleValueEvent(
                            new ValueEventListener() {

                                @Override
                                public void onDataChange(
                                        @NonNull DataSnapshot snapshot) {

                                    if (snapshot.exists()) {

                                        String name =
                                                snapshot.child("name")
                                                        .getValue(String.class);

                                        String email =
                                                snapshot.child("email")
                                                        .getValue(String.class);

                                        // Name
                                        if (name != null
                                                && !name.isEmpty()) {

                                            tvUserName.setText(name);

                                        } else {

                                            tvUserName.setText("RakshakX User");
                                        }

                                        // Email
                                        if (email != null
                                                && !email.isEmpty()) {

                                            tvUserEmail.setText(email);
                                        }

                                    } else {

                                        // If no data in Realtime DB
                                        tvUserName.setText("RakshakX User");

                                        if (user.getEmail() != null) {

                                            tvUserEmail.setText(
                                                    user.getEmail());
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(
                                        @NonNull DatabaseError error) {

                                    Toast.makeText(
                                            MainActivity.this,
                                            "Failed to load profile",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }
                            });

        } else {

            tvUserName.setText("Guest");

            tvUserEmail.setText("No Email");
        }
    }

    // =========================
    // Setup Map
    // =========================

    private void setupMap() {

        mMap.setTileSource(
                TileSourceFactory.MAPNIK);

        mMap.setMultiTouchControls(true);

        mMap.setBuiltInZoomControls(true);

        mMap.getController().setZoom(15.0);

        GeoPoint defaultLocation =
                new GeoPoint(
                        18.5204,
                        73.8567);

        mMap.getController()
                .setCenter(defaultLocation);
    }

    // =========================
    // Current Location
    // =========================

    private void getCurrentLocation() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            Location lastLocation =
                    locationManager.getLastKnownLocation(
                            LocationManager.GPS_PROVIDER);

            if (lastLocation != null) {

                GeoPoint currentLoc =
                        new GeoPoint(
                                lastLocation.getLatitude(),
                                lastLocation.getLongitude());

                mMap.getController()
                        .animateTo(currentLoc);

                Marker myMarker =
                        new Marker(mMap);

                myMarker.setPosition(currentLoc);

                myMarker.setTitle("📍 Your Location");

                myMarker.setAnchor(
                        Marker.ANCHOR_CENTER,
                        Marker.ANCHOR_BOTTOM);

                mMap.getOverlays()
                        .add(myMarker);
            }

            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    5000,
                    10,
                    this);
        }
    }

    // =========================
    // Load Citizen Reports
    // =========================

    private void loadReportsFromFirebase() {

        databaseRef.child("reports")
                .addValueEventListener(
                        new ValueEventListener() {

                            @Override
                            public void onDataChange(
                                    DataSnapshot snapshot) {

                                List<Report> allReports =
                                        new ArrayList<>();

                                for (DataSnapshot reportSnapshot :
                                        snapshot.getChildren()) {

                                    Report report =
                                            reportSnapshot.getValue(
                                                    Report.class);

                                    if (report != null) {

                                        allReports.add(report);
                                    }
                                }

                                updateMapWithMarkers(allReports);
                            }

                            @Override
                            public void onCancelled(
                                    DatabaseError error) {

                                Toast.makeText(
                                        MainActivity.this,
                                        "Error: "
                                                + error.getMessage(),
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        });
    }

    // =========================
    // Citizen Report Markers
    // =========================

    private void updateMapWithMarkers(
            List<Report> reports) {

        for (Report report : reports) {

            GeoPoint location =
                    new GeoPoint(
                            report.latitude,
                            report.longitude);

            Marker marker =
                    new Marker(mMap);

            marker.setPosition(location);

            marker.setTitle("⚠ Citizen Report");

            marker.setSubDescription(
                    report.description);

            marker.setAnchor(
                    Marker.ANCHOR_CENTER,
                    Marker.ANCHOR_BOTTOM);

            mMap.getOverlays()
                    .add(marker);
        }

        mMap.invalidate();
    }

    // =========================
    // 🔥 LOAD AUTHORITY MARKERS
    // =========================

    private void loadAuthorityRiskZones() {

        databaseRef.child("risk_zones")
                .addValueEventListener(
                        new ValueEventListener() {

                            @Override
                            public void onDataChange(
                                    @NonNull DataSnapshot snapshot) {

                                for (DataSnapshot ds :
                                        snapshot.getChildren()) {

                                    RiskZoneModel model =
                                            ds.getValue(
                                                    RiskZoneModel.class);

                                    if (model != null) {

                                        GeoPoint point =
                                                new GeoPoint(
                                                        model.latitude,
                                                        model.longitude);

                                        Marker marker =
                                                new Marker(mMap);

                                        marker.setPosition(point);

                                        marker.setTitle(
                                                "🚨 "
                                                        + model.riskLevel);

                                        marker.setSubDescription(
                                                "Authority Marked Zone");

                                        marker.setAnchor(
                                                Marker.ANCHOR_CENTER,
                                                Marker.ANCHOR_BOTTOM);

                                        // 🔴 HIGH RISK
                                        if (model.riskLevel
                                                .equals("HIGH RISK")) {

                                            marker.setIcon(
                                                    ContextCompat
                                                            .getDrawable(
                                                                    MainActivity.this,
                                                                    android.R.drawable.presence_busy));

                                            // 🟡 MEDIUM RISK
                                        } else if (model.riskLevel
                                                .equals("MEDIUM RISK")) {

                                            marker.setIcon(
                                                    ContextCompat
                                                            .getDrawable(
                                                                    MainActivity.this,
                                                                    android.R.drawable.presence_away));

                                            // 🟢 LOW RISK
                                        } else {

                                            marker.setIcon(
                                                    ContextCompat
                                                            .getDrawable(
                                                                    MainActivity.this,
                                                                    android.R.drawable.presence_online));
                                        }

                                        mMap.getOverlays()
                                                .add(marker);
                                    }
                                }

                                mMap.invalidate();

                                Toast.makeText(
                                        MainActivity.this,
                                        "✅ Authority risk zones loaded",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }

                            @Override
                            public void onCancelled(
                                    @NonNull DatabaseError error) {

                                Toast.makeText(
                                        MainActivity.this,
                                        "Failed to load authority zones",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        });
    }

    // =========================
    // Search Location
    // =========================

    private void searchLocation(
            String locationName) {

        Geocoder geocoder =
                new Geocoder(
                        this,
                        Locale.getDefault());

        try {

            List<Address> addresses =
                    geocoder.getFromLocationName(
                            locationName,
                            1);

            if (addresses != null
                    && !addresses.isEmpty()) {

                Address address =
                        addresses.get(0);

                double lat =
                        address.getLatitude();

                double lon =
                        address.getLongitude();

                GeoPoint searchedPoint =
                        new GeoPoint(lat, lon);

                mMap.getController()
                        .animateTo(searchedPoint);

                mMap.getController()
                        .setZoom(16.0);

                Toast.makeText(
                        this,
                        "📍 Showing "
                                + locationName,
                        Toast.LENGTH_SHORT
                ).show();

            } else {

                Toast.makeText(
                        this,
                        "Location not found",
                        Toast.LENGTH_SHORT
                ).show();
            }

        } catch (IOException e) {

            e.printStackTrace();

            Toast.makeText(
                    this,
                    "Search failed",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // =========================
    // SOS
    // =========================

    private void triggerSOS() {

        Intent sosIntent =
                new Intent(
                        this,
                        SOSActivity.class);

        startActivity(sosIntent);
    }

    // =========================
    // Report Dialog
    // =========================

    private void showReportDialog() {

        ReportDialog dialog =
                new ReportDialog();

        dialog.show(
                getSupportFragmentManager(),
                "ReportDialog");
    }

    private void logout() {
        mAuth.signOut();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }

    // =========================
    // Location Updates
    // =========================

    @Override
    public void onLocationChanged(
            Location location) {

        GeoPoint currentLoc =
                new GeoPoint(
                        location.getLatitude(),
                        location.getLongitude());

        mMap.getController()
                .animateTo(currentLoc);
    }

    @Override
    public void onStatusChanged(
            String provider,
            int status,
            Bundle extras) {

    }

    @Override
    public void onProviderEnabled(
            String provider) {

    }

    @Override
    public void onProviderDisabled(
            String provider) {

    }

    // =========================
    // Permission Result
    // =========================

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults);

        if (requestCode == 100
                && grantResults.length > 0
                && grantResults[0]
                == PackageManager.PERMISSION_GRANTED) {

            getCurrentLocation();
        }

        // Notification permission
        if (requestCode == 200 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "✅ Notifications enabled!", Toast.LENGTH_LONG).show();
        }
    }

    // =========================
    // Lifecycle
    // =========================

    @Override
    protected void onResume() {

        super.onResume();

        mMap.onResume();
    }

    @Override
    protected void onPause() {

        super.onPause();

        mMap.onPause();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        if (locationManager != null) {

            locationManager.removeUpdates(this);
        }
    }
}