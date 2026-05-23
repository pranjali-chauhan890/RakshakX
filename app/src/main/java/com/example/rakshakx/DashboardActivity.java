package com.example.rakshakx;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.*;
import com.example.rakshakx.models.Report;
import java.util.*;

public class DashboardActivity extends AppCompatActivity {

    private TextView tvTotalReports, tvHighRiskZones, tvAvgSeverity, tvSafetyScore;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        tvTotalReports = findViewById(R.id.tv_total_reports);
        tvHighRiskZones = findViewById(R.id.tv_high_risk_zones);
        tvAvgSeverity = findViewById(R.id.tv_avg_severity);
        tvSafetyScore = findViewById(R.id.tv_safety_score);

        databaseRef = FirebaseDatabase.getInstance().getReference();

        loadStatistics();
    }

    private void loadStatistics() {
        databaseRef.child("reports").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                int totalReports = (int) snapshot.getChildrenCount();
                float totalSeverity = 0;
                Map<String, Integer> areaReportCount = new HashMap<>();

                for (DataSnapshot reportSnapshot : snapshot.getChildren()) {
                    Report report = reportSnapshot.getValue(Report.class);
                    if (report != null) {
                        totalSeverity += report.severity;  // ← Now works with correct Report class

                        String areaKey = report.latitude + "," + report.longitude;  // ← Now works
                        areaReportCount.put(areaKey, areaReportCount.getOrDefault(areaKey, 0) + 1);
                    }
                }

                // Calculate high risk zones (areas with >5 reports)
                int highRiskCount = 0;
                for (Map.Entry<String, Integer> entry : areaReportCount.entrySet()) {
                    if (entry.getValue() > 5) {
                        highRiskCount++;
                    }
                }

                float avgSeverity = totalReports > 0 ? totalSeverity / totalReports : 0;
                float safetyScore = calculateSafetyScore(highRiskCount, totalReports, avgSeverity);

                // Update UI
                tvTotalReports.setText(String.valueOf(totalReports));
                tvHighRiskZones.setText(String.valueOf(highRiskCount));
                tvAvgSeverity.setText(String.format("%.1f", avgSeverity));
                tvSafetyScore.setText(String.format("%.0f", safetyScore) + "%");
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(DashboardActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                tvTotalReports.setText("Error");
            }
        });
    }

    private float calculateSafetyScore(int highRiskZones, int totalReports, float avgSeverity) {
        // Simple formula: Lower risk = higher safety score
        float riskFactor = (highRiskZones * 10) + (totalReports * 2) + (avgSeverity * 5);
        float safetyScore = 100 - Math.min(riskFactor, 95);
        return Math.max(safetyScore, 5);
    }
}