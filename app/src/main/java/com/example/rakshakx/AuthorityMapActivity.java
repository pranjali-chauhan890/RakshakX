package com.example.rakshakx;

import android.app.AlertDialog;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class AuthorityMapActivity extends AppCompatActivity {

    private MapView mapView;

    private DatabaseReference riskZoneRef;

    private EditText etSearch;

    private Button btnSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Configuration.getInstance()
                .setUserAgentValue(getPackageName());

        setContentView(R.layout.activity_authority_map);

        // Initialize Views
        mapView = findViewById(R.id.map);

        etSearch = findViewById(R.id.etSearch);

        btnSearch = findViewById(R.id.btnSearch);

        // Firebase Reference
        riskZoneRef = FirebaseDatabase
                .getInstance()
                .getReference("risk_zones");

        // Map Setup
        mapView.setTileSource(TileSourceFactory.MAPNIK);

        mapView.setMultiTouchControls(true);

        IMapController mapController =
                mapView.getController();

        mapController.setZoom(15.0);

        GeoPoint startPoint =
                new GeoPoint(18.5204, 73.8567);

        mapController.setCenter(startPoint);

        // Load Firebase Markers
        loadRiskZones();

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

        // Map Click Listener
        MapEventsReceiver mapEventsReceiver =
                new MapEventsReceiver() {

                    @Override
                    public boolean singleTapConfirmedHelper(
                            GeoPoint p) {

                        showRiskDialog(p);

                        return true;
                    }

                    @Override
                    public boolean longPressHelper(
                            GeoPoint p) {

                        return false;
                    }
                };

        MapEventsOverlay overlayEvents =
                new MapEventsOverlay(mapEventsReceiver);

        mapView.getOverlays().add(overlayEvents);
    }

    // =========================
    // Risk Dialog
    // =========================

    private void showRiskDialog(GeoPoint point) {

        String[] riskLevels = {

                "🔴 High Risk",
                "🟡 Medium Risk",
                "🟢 Low Risk"
        };

        AlertDialog.Builder builder =
                new AlertDialog.Builder(this);

        builder.setTitle("Select Risk Level");

        builder.setItems(riskLevels,
                (dialog, which) -> {

                    switch (which) {

                        case 0:

                            addMarker(
                                    point,
                                    "HIGH RISK",
                                    android.R.drawable.presence_busy
                            );

                            break;

                        case 1:

                            addMarker(
                                    point,
                                    "MEDIUM RISK",
                                    android.R.drawable.presence_away
                            );

                            break;

                        case 2:

                            addMarker(
                                    point,
                                    "LOW RISK",
                                    android.R.drawable.presence_online
                            );

                            break;
                    }
                });

        builder.show();
    }

    // =========================
    // Add Marker
    // =========================

    private void addMarker(
            GeoPoint point,
            String title,
            int iconRes) {

        Marker marker =
                new Marker(mapView);

        marker.setPosition(point);

        marker.setTitle(title);

        marker.setAnchor(
                Marker.ANCHOR_CENTER,
                Marker.ANCHOR_BOTTOM
        );

        marker.setIcon(
                ContextCompat.getDrawable(
                        this,
                        iconRes
                )
        );

        // 🔥 DELETE FEATURE
        marker.setOnMarkerClickListener(
                (clickedMarker, mapView) -> {

                    new AlertDialog.Builder(
                            AuthorityMapActivity.this)

                            .setTitle("Delete Marker")

                            .setMessage(
                                    "Do you want to delete this risk zone?")

                            .setPositiveButton(
                                    "Delete",
                                    (dialog, which) -> {

                                        String markerId =
                                                (String) clickedMarker
                                                        .getRelatedObject();

                                        if (markerId != null) {

                                            riskZoneRef.child(markerId)
                                                    .removeValue();

                                            Toast.makeText(
                                                    AuthorityMapActivity.this,
                                                    "Marker deleted",
                                                    Toast.LENGTH_SHORT
                                            ).show();
                                        }
                                    })

                            .setNegativeButton(
                                    "Cancel",
                                    null)

                            .show();

                    return true;
                });

        mapView.getOverlays().add(marker);

        mapView.invalidate();

        // Save to Firebase
        saveRiskZone(
                point.getLatitude(),
                point.getLongitude(),
                title
        );
    }

    // =========================
    // Save Data
    // =========================

    private void saveRiskZone(
            double latitude,
            double longitude,
            String riskLevel) {

        String id =
                riskZoneRef.push().getKey();

        RiskZoneModel model =
                new RiskZoneModel(
                        id,
                        latitude,
                        longitude,
                        riskLevel
                );

        riskZoneRef.child(id)
                .setValue(model);
    }

    // =========================
    // Load Firebase Data
    // =========================

    private void loadRiskZones() {

        riskZoneRef.addValueEventListener(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot) {

                        // Remove old markers only
                        for (int i =
                             mapView.getOverlays().size() - 1;
                             i >= 0;
                             i--) {

                            Overlay overlay =
                                    mapView.getOverlays().get(i);

                            if (overlay instanceof Marker) {

                                mapView.getOverlays()
                                        .remove(i);
                            }
                        }

                        // Add markers again
                        for (DataSnapshot ds :
                                snapshot.getChildren()) {

                            RiskZoneModel model =
                                    ds.getValue(
                                            RiskZoneModel.class
                                    );

                            if (model != null) {

                                GeoPoint point =
                                        new GeoPoint(
                                                model.latitude,
                                                model.longitude
                                        );

                                int iconRes;

                                switch (model.riskLevel) {

                                    case "HIGH RISK":

                                        iconRes =
                                                android.R.drawable.presence_busy;

                                        break;

                                    case "MEDIUM RISK":

                                        iconRes =
                                                android.R.drawable.presence_away;

                                        break;

                                    default:

                                        iconRes =
                                                android.R.drawable.presence_online;

                                        break;
                                }

                                Marker marker =
                                        new Marker(mapView);

                                marker.setPosition(point);

                                marker.setTitle(
                                        model.riskLevel
                                );

                                marker.setAnchor(
                                        Marker.ANCHOR_CENTER,
                                        Marker.ANCHOR_BOTTOM
                                );

                                marker.setIcon(
                                        ContextCompat.getDrawable(
                                                AuthorityMapActivity.this,
                                                iconRes
                                        )
                                );

                                // 🔥 SAVE FIREBASE ID
                                marker.setRelatedObject(
                                        model.id
                                );

                                // 🔥 CLICK TO DELETE
                                marker.setOnMarkerClickListener(
                                        (clickedMarker, mapView) -> {

                                            new AlertDialog.Builder(
                                                    AuthorityMapActivity.this)

                                                    .setTitle(
                                                            "Delete Risk Zone")

                                                    .setMessage(
                                                            "Delete this marked zone?")

                                                    .setPositiveButton(
                                                            "Delete",
                                                            (dialog, which) -> {

                                                                String markerId =
                                                                        (String)
                                                                                clickedMarker
                                                                                        .getRelatedObject();

                                                                riskZoneRef
                                                                        .child(markerId)
                                                                        .removeValue();

                                                                Toast.makeText(
                                                                        AuthorityMapActivity.this,
                                                                        "Risk zone deleted",
                                                                        Toast.LENGTH_SHORT
                                                                ).show();
                                                            })

                                                    .setNegativeButton(
                                                            "Cancel",
                                                            null)

                                                    .show();

                                            return true;
                                        });

                                mapView.getOverlays()
                                        .add(marker);
                            }
                        }

                        mapView.invalidate();
                    }

                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error) {

                        Toast.makeText(
                                AuthorityMapActivity.this,
                                "Failed to load data",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    // =========================
    // Search Location
    // =========================

    private void searchLocation(String locationName) {

        Geocoder geocoder =
                new Geocoder(
                        this,
                        Locale.getDefault()
                );

        try {

            List<Address> addresses =
                    geocoder.getFromLocationName(
                            locationName,
                            1
                    );

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

                mapView.getController()
                        .setCenter(searchedPoint);

                mapView.getController()
                        .setZoom(16.0);

                Toast.makeText(
                        this,
                        "Location Found",
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

    @Override
    protected void onResume() {

        super.onResume();

        mapView.onResume();
    }

    @Override
    protected void onPause() {

        super.onPause();

        mapView.onPause();
    }
}