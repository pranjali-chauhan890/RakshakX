package com.example.rakshakx;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.example.rakshakx.models.Report;  // ← FIXED: Changed from com.smartrisk to com.example.rakshakx

public class ReportDialog extends DialogFragment {

    private EditText etAreaName, etDescription;
    private Spinner spinnerCrimeType, spinnerSeverity;
    private Button btnSubmit;
    private DatabaseReference databaseRef;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_report, null);

        etAreaName = view.findViewById(R.id.et_area_name);
        etDescription = view.findViewById(R.id.et_description);
        spinnerCrimeType = view.findViewById(R.id.spinner_crime_type);
        spinnerSeverity = view.findViewById(R.id.spinner_severity);
        btnSubmit = view.findViewById(R.id.btn_submit_report);

        databaseRef = FirebaseDatabase.getInstance().getReference();

        setupSpinners();

        btnSubmit.setOnClickListener(v -> submitReport());

        builder.setView(view)
                .setTitle("Report Unsafe Location")
                .setNegativeButton("Cancel", null);

        return builder.create();
    }

    private void setupSpinners() {
        // Crime types
        String[] crimeTypes = {"Theft", "Assault", "Vandalism", "Harassment", "Suspicious Activity"};
        ArrayAdapter<String> crimeAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, crimeTypes);
        spinnerCrimeType.setAdapter(crimeAdapter);

        // Severity levels
        String[] severityLevels = {"1 - Minor", "2 - Low", "3 - Moderate", "4 - Severe", "5 - Critical"};
        ArrayAdapter<String> severityAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, severityLevels);
        spinnerSeverity.setAdapter(severityAdapter);
    }

    private void submitReport() {
        String areaName = etAreaName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String crimeType = spinnerCrimeType.getSelectedItem().toString();
        int severity = spinnerSeverity.getSelectedItemPosition() + 1;

        if (areaName.isEmpty()) {
            etAreaName.setError("Area name required");
            return;
        }

        // Get current location (simplified - use last known location)
        // For hackathon, use dummy coordinates
        double latitude = 42.3555; // Demo coordinates
        double longitude = -71.0605;

        Report report = new Report(areaName, latitude, longitude, crimeType, severity, description);

        databaseRef.child("reports").push().setValue(report)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getActivity(), "Report submitted! Thank you for helping.", Toast.LENGTH_SHORT).show();
                    dismiss();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}