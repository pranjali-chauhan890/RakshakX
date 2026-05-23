package com.example.rakshakx;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rakshakx.models.Report;

import java.util.List;

public class ReportHistoryAdapter
        extends RecyclerView.Adapter<ReportHistoryAdapter.ViewHolder> {

    private List<Report> reportList;

    private OnDeleteClickListener listener;

    // INTERFACE
    public interface OnDeleteClickListener {
        void onDelete(Report report);
    }

    public ReportHistoryAdapter(
            List<Report> reportList,
            OnDeleteClickListener listener) {

        this.reportList = reportList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view =
                LayoutInflater.from(parent.getContext())
                        .inflate(
                                R.layout.item_report_history,
                                parent,
                                false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position) {

        Report report = reportList.get(position);

        // PLACE / AREA NAME
        if (report.placeName != null
                && !report.placeName.isEmpty()) {

            holder.tvLocation.setText(
                    "📍 " + report.placeName);

        } else {

            holder.tvLocation.setText(
                    "📍 "
                            + report.latitude
                            + ", "
                            + report.longitude);
        }

        // DESCRIPTION
        holder.tvDescription.setText(
                "⚠️ " + report.description);

        // DELETE BUTTON
        holder.ivDelete.setOnClickListener(v -> {

            if (listener != null) {

                listener.onDelete(report);
            }
        });
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    public static class ViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvLocation, tvDescription;

        ImageView ivDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvLocation =
                    itemView.findViewById(R.id.tvLocation);

            tvDescription =
                    itemView.findViewById(R.id.tvDescription);

            ivDelete =
                    itemView.findViewById(R.id.ivDelete);
        }
    }
}