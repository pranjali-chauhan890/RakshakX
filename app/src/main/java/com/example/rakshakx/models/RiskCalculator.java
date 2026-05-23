package com.example.rakshakx.models;


import java.util.List;

public class RiskCalculator {

    // Simple formula: (Severity * 20) + (ReportCount * 5) - (DaysSince * 2)
    public static float calculateRiskScore(List<Report> reportsInArea) {
        if (reportsInArea == null || reportsInArea.isEmpty()) return 0;

        int totalSeverity = 0;
        int reportCount = reportsInArea.size();
        long currentTime = System.currentTimeMillis();

        for (Report report : reportsInArea) {
            totalSeverity += report.severity;

            // Older reports have less weight
            long daysSince = (currentTime - report.timestamp) / (1000 * 60 * 60 * 24);
            if (daysSince > 30) {
                reportCount--; // Reduce weight for old reports
            }
        }

        float avgSeverity = totalSeverity / (float) reportCount;

        // Risk formula
        float riskScore = (avgSeverity * 20) + (reportCount * 5);

        // Cap at 100
        return Math.min(riskScore, 100);
    }

    public static String getRiskLevel(float score) {
        if (score >= 70) return "🔴 HIGH RISK";
        if (score >= 40) return "🟡 MEDIUM RISK";
        return "🟢 LOW RISK";
    }
}
