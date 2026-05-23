package com.example.rakshakx.models;
public class Hotspot {
    public String id;
    public double latitude;
    public double longitude;
    public float riskScore;
    public int reportCount;
    public String riskLevel; // HIGH, MEDIUM, LOW
    public String areaName;
    public String crimeType;
    public long lastUpdated;

    // Required empty constructor for Firebase
    public Hotspot() {}

    public Hotspot(double lat, double lng, float score, int count) {
        this.latitude = lat;
        this.longitude = lng;
        this.riskScore = score;
        this.reportCount = count;
        this.lastUpdated = System.currentTimeMillis();

        if (score > 70) riskLevel = "HIGH";
        else if (score > 40) riskLevel = "MEDIUM";
        else riskLevel = "LOW";
    }

    public Hotspot(String areaName, double lat, double lng, float score, int count, String crimeType) {
        this.areaName = areaName;
        this.latitude = lat;
        this.longitude = lng;
        this.riskScore = score;
        this.reportCount = count;
        this.crimeType = crimeType;
        this.lastUpdated = System.currentTimeMillis();

        if (score > 70) riskLevel = "HIGH";
        else if (score > 40) riskLevel = "MEDIUM";
        else riskLevel = "LOW";
    }

    // Method to update risk level based on score
    public void updateRiskLevel() {
        if (riskScore > 70) riskLevel = "HIGH";
        else if (riskScore > 40) riskLevel = "MEDIUM";
        else riskLevel = "LOW";
    }

    // Method to check if hotspot is still active (within 30 days)
    public boolean isActive() {
        long daysSince = (System.currentTimeMillis() - lastUpdated) / (1000 * 60 * 60 * 24);
        return daysSince <= 30;
    }
}