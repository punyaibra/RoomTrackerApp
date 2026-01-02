package com.example.roomtracker.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.roomtracker.R;
import com.example.roomtracker.database.DatabaseHelper;
import com.example.roomtracker.model.Booking;
import com.example.roomtracker.model.Room;
import com.example.roomtracker.model.User;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<Booking> historyList;
    private Context context;
    private DatabaseHelper db;

    private OnHistoryClickListener listener;

    public interface OnHistoryClickListener {
        void onItemClick(Booking booking);
    }

    public HistoryAdapter(List<Booking> historyList, Context context, OnHistoryClickListener listener) {
        this.historyList = historyList;
        this.context = context;
        this.db = new DatabaseHelper(context);
        this.listener = listener;
    }

    public HistoryAdapter(List<Booking> historyList, Context context) {
        this(historyList, context, null);
    }

    public void updateList(List<Booking> newList) {
        this.historyList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history_card, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        Booking booking = historyList.get(position);
        Room room = db.getRoom(booking.getRoomId());
        User user = db.getUser(booking.getUserId());

        // Room & User Info
        holder.tvRoomName.setText(room != null ? room.getName() : "Unknown Room");
        holder.tvUserName.setText(user != null ? user.getName() : "Unknown User");

        // Date & Time
        holder.tvDate.setText(booking.getDate());
        holder.tvTime.setText(booking.getStartTime() + " - " + booking.getEndTime());

        // Reason
        holder.tvReason.setText(booking.getReason());

        // Status Badge
        // Status Badge
        if ("FINISHED".equals(booking.getStatus())) {
            holder.tvStatus.setText("SELESAI");
            holder.tvStatus.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#4CAF50"))); // Green
            holder.tvStatus.setTextColor(Color.WHITE);
        } else if ("REJECTED".equals(booking.getStatus())) {
            holder.tvStatus.setText("DITOLAK");
            holder.tvStatus.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#F44336"))); // Red
            holder.tvStatus.setTextColor(Color.WHITE);
        } else if ("PENDING".equals(booking.getStatus()) || "WAITING".equals(booking.getStatus())) {
            holder.tvStatus.setText("PENDING");
            holder.tvStatus.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#FFC107"))); // Amber
            holder.tvStatus.setTextColor(Color.WHITE); // Or Dark Grey #333 if amber is light
        } else if ("APPROVED".equals(booking.getStatus())) {
            holder.tvStatus.setText("DISETUJUI");
            holder.tvStatus.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#2196F3"))); // Blue
            holder.tvStatus.setTextColor(Color.WHITE);
        } else {
            holder.tvStatus.setText(booking.getStatus());
            holder.tvStatus.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#9E9E9E"))); // Grey
            holder.tvStatus.setTextColor(Color.WHITE);
        }

        // Location if available
        if (room != null) {
            holder.tvLocation.setText(room.getBuilding() + ", Lantai " + room.getFloor());
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null)
                listener.onItemClick(booking);
        });
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvRoomName, tvUserName, tvDate, tvTime, tvReason, tvStatus, tvLocation;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoomName = itemView.findViewById(R.id.tvHistoryRoomName);
            tvUserName = itemView.findViewById(R.id.tvHistoryUserName);
            tvDate = itemView.findViewById(R.id.tvHistoryDate);
            tvTime = itemView.findViewById(R.id.tvHistoryTime);
            tvReason = itemView.findViewById(R.id.tvHistoryReason);
            tvStatus = itemView.findViewById(R.id.tvHistoryStatus);
            tvLocation = itemView.findViewById(R.id.tvHistoryLocation);
        }
    }
}
