package com.example.roomtracker.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.roomtracker.R;
import java.util.List;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder> {

    private final List<String> daysOfMonth;
    private final List<String> eventDates;
    private String selectedDate;
    private final String todayDate;
    private final OnItemListener onItemListener;

    public CalendarAdapter(List<String> daysOfMonth, List<String> eventDates, String selectedDate,
            OnItemListener onItemListener) {
        this.daysOfMonth = daysOfMonth;
        this.eventDates = eventDates;
        this.selectedDate = selectedDate;
        this.onItemListener = onItemListener;
        this.todayDate = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                .format(new java.util.Date());
    }

    public void updateData(List<String> days, List<String> events, String selected) {
        this.daysOfMonth.clear();
        this.daysOfMonth.addAll(days);
        this.eventDates.clear();
        this.eventDates.addAll(events);
        this.selectedDate = selected;
        notifyDataSetChanged();
    }

    public void setSelectedDate(String date) {
        this.selectedDate = date;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_day, parent, false);
        return new CalendarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        String date = daysOfMonth.get(position);

        if (date.isEmpty()) {
            holder.tvDay.setText("");
            holder.viewSelected.setVisibility(View.GONE);
            holder.viewToday.setVisibility(View.GONE);
            holder.viewDoT.setVisibility(View.GONE);
            holder.itemView.setOnClickListener(null);
            return;
        }

        // date format in list is YYYY-MM-DD
        String day = date.substring(date.lastIndexOf("-") + 1);
        if (day.startsWith("0"))
            day = day.substring(1);

        holder.tvDay.setText(day);

        // Check if selected
        if (date.equals(selectedDate)) {
            // Selected: Solid Circle, White Text
            holder.viewSelected.setVisibility(View.VISIBLE);
            holder.viewToday.setVisibility(View.GONE);
            holder.tvDay.setTextColor(Color.WHITE);
        } else if (date.equals(todayDate)) {
            // Today but NOT selected: Outlined Circle, Default Text (or Colored)
            holder.viewSelected.setVisibility(View.GONE);
            holder.viewToday.setVisibility(View.VISIBLE);
            holder.tvDay.setTextColor(Color.parseColor("#1A73E8")); // Use primary color for text too if desired, or
                                                                    // black
        } else {
            // Normal
            holder.viewSelected.setVisibility(View.GONE);
            holder.viewToday.setVisibility(View.GONE);
            holder.tvDay.setTextColor(Color.parseColor("#333333"));
        }

        if (eventDates.contains(date)) {
            holder.viewDoT.setVisibility(View.VISIBLE);
        } else {
            holder.viewDoT.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (onItemListener != null) {
                onItemListener.onItemClick(position, date);
            }
        });
    }

    @Override
    public int getItemCount() {
        return daysOfMonth.size();
    }

    public interface OnItemListener {
        void onItemClick(int position, String date);
    }

    public static class CalendarViewHolder extends RecyclerView.ViewHolder {
        public final TextView tvDay;
        public final View viewSelected;
        public final View viewToday;
        public final View viewDoT;

        public CalendarViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tvCalendarDay);
            viewSelected = itemView.findViewById(R.id.viewSelectedBackground);
            viewToday = itemView.findViewById(R.id.viewTodayBackground);
            viewDoT = itemView.findViewById(R.id.viewEventDot);
        }
    }
}
