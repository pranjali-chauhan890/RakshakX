package com.example.rakshakx;

public class RiskZoneModel {

    public String id;
    public double latitude;
    public double longitude;
    public String riskLevel;

    public RiskZoneModel() {
    }

    public RiskZoneModel(
            String id,
            double latitude,
            double longitude,
            String riskLevel) {

        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.riskLevel = riskLevel;
    }
}