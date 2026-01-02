package com.example.roomtracker.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.roomtracker.R;
import com.example.roomtracker.model.Booking;
import java.util.List;

public class ScheduleEventAdapter extends RecyclerView.Adapter<ScheduleEventAdapter.EventViewHolder> {

    private List<Booking> eventList;
    private final OnEventClickListener listener;

    public interface OnEventClickListener {
        void onEventClick(Booking booking);
    }

    public ScheduleEventAdapter(List<Booking> eventList, OnEventClickListener listener) {
        this.eventList = eventList;
        this.listener = listener;
    }

    public void updateList(List<Booking> newList) {
        this.eventList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_schedule_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Booking booking = eventList.get(position);

        // Title
        String title = booking.getReason();
        if (title == null || title.isEmpty())
            title = "Peminjaman Ruangan";
        holder.tvTitle.setText(title);

        // User
        holder.tvUser.setText(booking.getUserName() != null ? booking.getUserName() : "Unknown User");

        // Time
        holder.tvTime.setText(booking.getStartTime() + " - " + booking.getEndTime());

        // Room
        holder.tvRoom.setText(booking.getRoomName());

        // Listener on entire item
        holder.itemView.setOnClickListener(v -> listener.onEventClick(booking));
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvUser, tvTime, tvRoom;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvEventTitle);
            tvUser = itemView.findViewById(R.id.tvEventUser);
            tvTime = itemView.findViewById(R.id.tvEventTime);
            tvRoom = itemView.findViewById(R.id.tvEventRoom);
        }
    }
}
