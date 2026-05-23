package com.example.rakshakx.models;

public class Report {
    public String id;
    public String areaName;
    public double latitude;      // ← Must have this
    public double longitude;     // ← Must have this
    public String crimeType;
    public int severity;         // ← Must have this
    public long timestamp;
    public String userId;
    public String description;

    public String placeName;


    // Required empty constructor for Firebase
    public Report() {}

    public Report(String areaName, double lat, double lng, String crimeType, int severity, String desc) {
        this.areaName = areaName;
        this.latitude = lat;
        this.longitude = lng;
        this.crimeType = crimeType;
        this.severity = severity;
        this.timestamp = System.currentTimeMillis();
        this.description = desc;
    }
}