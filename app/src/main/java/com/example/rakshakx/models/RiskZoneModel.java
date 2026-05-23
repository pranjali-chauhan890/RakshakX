package com.example.rakshakx.models;



public class RiskZoneModel {
    public String id;
    public double latitude;
    public double longitude;
    public String riskLevel;
    public String description;
    public long timestamp;
    public String markedBy;

    public RiskZoneModel() {
        // Required empty constructor for Firebase
    }

    public RiskZoneModel(double latitude, double longitude, String riskLevel, String description) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.riskLevel = riskLevel;
        this.description = description;
        this.timestamp = System.currentTimeMillis();
    }
}
