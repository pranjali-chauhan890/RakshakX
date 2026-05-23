package com.example.rakshakx;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rakshakx.models.Report;
import com.example.rakshakx.models.RiskCalculator;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.*;

import java.util.*;

public class AuthorityDashboardActivity
        extends AppCompatActivity {

    private TextView tvTotalReports,
            tvHighRiskZones,
            tvMediumRiskZones,
            tvLowRiskZones,
            tvAvgRiskScore,
            tvPrediction1,
            tvPrediction2,
            tvPrediction3;

    private FloatingActionButton fabMap;

    private DatabaseReference databaseRef;

    // Drawer
    DrawerLayout drawerLayout;
    NavigationView navView;
    ImageView ivMenu;

    // RecyclerView
    RecyclerView recyclerReports;

    List<Report> reportList =
            new ArrayList<>();

    ReportHistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authority_dashboard);

        // RecyclerView
        recyclerReports =
                findViewById(R.id.recyclerReports);

        recyclerReports.setLayoutManager(
                new LinearLayoutManager(this));

        adapter =
                new ReportHistoryAdapter(
                        reportList,
                        new ReportHistoryAdapter.OnDeleteClickListener() {
                            @Override
                            public void onDelete(Report report) {

                                new AlertDialog.Builder(
                                        AuthorityDashboardActivity.this)
                                        .setTitle("Delete Report")
                                        .setMessage("Are you sure you want to delete this report?")
                                        .setPositiveButton("Delete",
                                                (dialog, which) -> {

                                                    if (report.id != null) {

                                                        databaseRef
                                                                .child("reports")
                                                                .child(report.id)
                                                                .removeValue();
                                                    }
                                                })
                                        .setNegativeButton("Cancel", null)
                                        .show();
                            }
                        });

        recyclerReports.setAdapter(adapter);

        // Dashboard TextViews
        tvTotalReports =
                findViewById(R.id.tv_total_reports);

        tvHighRiskZones =
                findViewById(R.id.tv_high_risk_zones);

        tvMediumRiskZones =
                findViewById(R.id.tv_medium_risk_zones);

        tvLowRiskZones =
                findViewById(R.id.tv_low_risk_zones);

        tvAvgRiskScore =
                findViewById(R.id.tv_avg_risk_score);

        tvPrediction1 =
                findViewById(R.id.tv_prediction_1);

        tvPrediction2 =
                findViewById(R.id.tv_prediction_2);

        tvPrediction3 =
                findViewById(R.id.tv_prediction_3);

        fabMap = findViewById(R.id.fabMap);

        // Drawer Views
        drawerLayout =
                findViewById(R.id.drawerLayout);

        navView =
                findViewById(R.id.navView);

        ivMenu =
                findViewById(R.id.iv_menu);

        // Open Drawer
        ivMenu.setOnClickListener(v -> {
            drawerLayout.openDrawer(navView);
        });

        // Navigation Item Clicks
        navView.setNavigationItemSelectedListener(
                item -> {

                    int id = item.getItemId();

                    if (id == R.id.nav_dashboard) {

                        drawerLayout.closeDrawers();

                    } else if (id == R.id.nav_map) {

                        startActivity(
                                new Intent(
                                        AuthorityDashboardActivity.this,
                                        AuthorityMapActivity.class));

                    } else if (id == R.id.nav_logout) {

                        Intent intent =
                                new Intent(
                                        AuthorityDashboardActivity.this,
                                        LoginActivity.class);

                        intent.addFlags(
                                Intent.FLAG_ACTIVITY_NEW_TASK
                                        | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                        startActivity(intent);

                        finish();
                    }

                    drawerLayout.closeDrawers();

                    return true;
                });

        // FAB MAP BUTTON
        fabMap.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            AuthorityDashboardActivity.this,
                            AuthorityMapActivity.class);

            startActivity(intent);
        });

        databaseRef =
                FirebaseDatabase.getInstance()
                        .getReference();

        loadAuthorityStatistics();
    }

    private void loadAuthorityStatistics() {

        databaseRef.child("reports")
                .addValueEventListener(
                        new ValueEventListener() {

                            @Override
                            public void onDataChange(
                                    @NonNull DataSnapshot snapshot) {

                                reportList.clear();

                                int totalReports =
                                        (int) snapshot.getChildrenCount();

                                Map<String, List<Report>>
                                        reportsByLocation =
                                        new HashMap<>();

                                for (DataSnapshot reportSnapshot :
                                        snapshot.getChildren()) {

                                    Report report =
                                            reportSnapshot.getValue(
                                                    Report.class);

                                    if (report != null) {

                                        // Firebase Key
                                        report.id =
                                                reportSnapshot.getKey();

                                        // Add to RecyclerView
                                        reportList.add(report);

                                        String key =
                                                report.latitude
                                                        + ","
                                                        + report.longitude;

                                        if (!reportsByLocation
                                                .containsKey(key)) {

                                            reportsByLocation.put(
                                                    key,
                                                    new ArrayList<>());
                                        }

                                        reportsByLocation
                                                .get(key)
                                                .add(report);
                                    }
                                }

                                adapter.notifyDataSetChanged();

                                int highRisk = 0;
                                int mediumRisk = 0;
                                int lowRisk = 0;

                                float totalRiskScore = 0;

                                for (List<Report> locationReports :
                                        reportsByLocation.values()) {

                                    float riskScore =
                                            RiskCalculator
                                                    .calculateRiskScore(
                                                            locationReports);

                                    totalRiskScore += riskScore;

                                    if (riskScore >= 70) {

                                        highRisk++;

                                    } else if (riskScore >= 40) {

                                        mediumRisk++;

                                    } else {

                                        lowRisk++;
                                    }
                                }

                                float avgRiskScore =
                                        reportsByLocation.size() > 0
                                                ? totalRiskScore
                                                / reportsByLocation.size()
                                                : 0;

                                // Dashboard UI
                                tvTotalReports.setText(
                                        "📌 Total Citizen Complaints : "
                                                + totalReports);

                                tvHighRiskZones.setText(
                                        "🔴 Predicted High Risk Zones : "
                                                + highRisk);

                                tvMediumRiskZones.setText(
                                        "🟡 Predicted Medium Risk Zones : "
                                                + mediumRisk);

                                tvLowRiskZones.setText(
                                        "🟢 Predicted Safe Zones : "
                                                + lowRisk);

                                tvAvgRiskScore.setText(
                                        "📈 Area Risk Index : "
                                                + String.format(
                                                "%.1f",
                                                avgRiskScore));

                                // Predictive Analytics
                                tvPrediction1.setText(
                                        "• "
                                                + highRisk
                                                + " locations may require immediate patrol deployment.");

                                tvPrediction2.setText(
                                        "• Repeated complaints detected in "
                                                + mediumRisk
                                                + " medium-risk areas.");

                                tvPrediction3.setText(
                                        "• Safety index generated using complaint frequency and severity trends.");
                            }

                            @Override
                            public void onCancelled(
                                    @NonNull DatabaseError error) {

                            }
                        });
    }
}