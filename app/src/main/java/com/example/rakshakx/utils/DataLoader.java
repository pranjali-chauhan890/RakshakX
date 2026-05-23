package com.example.rakshakx.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.example.rakshakx.models.Report;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class DataLoader {

    private static final String TAG = "DataLoader";
    private static final String PREF_NAME = "RakshakXPrefs";
    private static final String KEY_DATA_LOADED = "data_loaded";

    public static void loadInitialData(Context context) {
        InputStream is = null;
        try {
            Log.d(TAG, "Starting to load initial data...");

            // Read JSON from assets
            is = context.getAssets().open("initial_risk_dataset.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            int bytesRead = is.read(buffer);
            is.close();
            String json = new String(buffer, "UTF-8");

            Log.d(TAG, "JSON file read successfully, size: " + size + " bytes, read: " + bytesRead);

            JSONObject obj = new JSONObject(json);
            JSONArray locations = obj.getJSONArray("locations");

            Log.d(TAG, "Found " + locations.length() + " locations in JSON");

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

            // Loop through locations - using index variable 'locationIndex' instead of 'i'
            for (int locationIndex = 0; locationIndex < locations.length(); locationIndex++) {
                try {
                    JSONObject loc = locations.getJSONObject(locationIndex);
                    String key = ref.child("historical_data").push().getKey();

                    Map<String, Object> data = new HashMap<>();

                    // Safe way to get values with fallbacks
                    data.put("area_name", getSafeString(loc, "area_name", "Unknown Area"));
                    data.put("latitude", getSafeDouble(loc, "latitude", 0.0));
                    data.put("longitude", getSafeDouble(loc, "longitude", 0.0));
                    data.put("crime_type", getSafeString(loc, "crime_type", "Unknown"));
                    data.put("severity", getSafeInt(loc, "severity", 1));
                    data.put("reports_count", getSafeInt(loc, "reports_count", 1));
                    data.put("timestamp", getSafeString(loc, "timestamp", "2024-01-01"));

                    if (key != null) {
                        final int currentIndex = locationIndex; // For use in lambda
                        ref.child("historical_data").child(key).setValue(data)
                                .addOnSuccessListener(aVoid -> Log.d(TAG, "Data loaded: " + getSafeString(loc, "area_name", "Area")))
                                .addOnFailureListener(e -> Log.e(TAG, "Failed to load data for index " + currentIndex, e));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error processing location at index " + locationIndex, e);
                }
            }

            // Mark data as loaded
            markDataLoaded(context);
            Log.d(TAG, "Initial data loading completed successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error loading initial data", e);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error closing stream", e);
            }
        }
    }

    // Method to convert historical data to reports
    public static void convertHistoricalDataToReports(Context context) {
        InputStream is = null;
        try {
            Log.d(TAG, "Starting to convert historical data to reports...");

            is = context.getAssets().open("initial_risk_dataset.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, "UTF-8");

            JSONObject obj = new JSONObject(json);
            JSONArray locations = obj.getJSONArray("locations");

            Log.d(TAG, "Converting " + locations.length() + " locations to reports");

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

            // Loop through locations - using index variable 'reportIndex' instead of 'i'
            for (int reportIndex = 0; reportIndex < locations.length(); reportIndex++) {
                try {
                    JSONObject loc = locations.getJSONObject(reportIndex);

                    // Safe extraction with error handling
                    String areaName = getSafeString(loc, "area_name", "Area " + reportIndex);
                    double latitude = getSafeDouble(loc, "latitude", 42.3555);
                    double longitude = getSafeDouble(loc, "longitude", -71.0605);
                    String crimeType = getSafeString(loc, "crime_type", "Unknown");
                    int severity = getSafeInt(loc, "severity", 3);

                    Log.d(TAG, "Creating report: " + areaName + " at (" + latitude + ", " + longitude + ")");

                    Report report = new Report(
                            areaName,
                            latitude,
                            longitude,
                            crimeType,
                            severity,
                            "Historical data from dataset"
                    );

                    final String finalAreaName = areaName; // For use in lambda
                    ref.child("reports").push().setValue(report)
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "Report converted: " + finalAreaName))
                            .addOnFailureListener(e -> Log.e(TAG, "Failed to convert report for " + finalAreaName, e));

                } catch (Exception e) {
                    Log.e(TAG, "Error converting location at index " + reportIndex, e);
                }
            }

            Log.d(TAG, "Historical data conversion completed");

        } catch (Exception e) {
            Log.e(TAG, "Error converting data", e);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error closing stream", e);
            }
        }
    }

    // Safe helper methods to prevent JSON exceptions
    private static String getSafeString(JSONObject obj, String key, String defaultValue) {
        try {
            if (obj != null && obj.has(key) && !obj.isNull(key)) {
                String value = obj.getString(key);
                return value != null ? value : defaultValue;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting string for key: " + key, e);
        }
        return defaultValue;
    }

    private static double getSafeDouble(JSONObject obj, String key, double defaultValue) {
        try {
            if (obj != null && obj.has(key) && !obj.isNull(key)) {
                Object value = obj.get(key);
                if (value instanceof Number) {
                    return ((Number) value).doubleValue();
                } else if (value instanceof String) {
                    String strValue = (String) value;
                    if (strValue != null && !strValue.isEmpty()) {
                        return Double.parseDouble(strValue);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting double for key: " + key, e);
        }
        return defaultValue;
    }

    private static int getSafeInt(JSONObject obj, String key, int defaultValue) {
        try {
            if (obj != null && obj.has(key) && !obj.isNull(key)) {
                Object value = obj.get(key);
                if (value instanceof Number) {
                    return ((Number) value).intValue();
                } else if (value instanceof String) {
                    String strValue = (String) value;
                    if (strValue != null && !strValue.isEmpty()) {
                        return Integer.parseInt(strValue);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting int for key: " + key, e);
        }
        return defaultValue;
    }

    // Method to check if data already exists
    public static boolean isDataLoaded(Context context) {
        if (context == null) {
            return false;
        }
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_DATA_LOADED, false);
    }

    private static void markDataLoaded(Context context) {
        if (context == null) {
            return;
        }
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_DATA_LOADED, true).apply();
    }

    // Method to reset data loaded flag (useful for testing)
    public static void resetDataLoadedFlag(Context context) {
        if (context == null) {
            return;
        }
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_DATA_LOADED, false).apply();
    }
}