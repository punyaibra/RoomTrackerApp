package com.example.roomtracker.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.roomtracker.R;
import com.example.roomtracker.adapter.CalendarAdapter;
import com.example.roomtracker.adapter.ScheduleEventAdapter;
import com.example.roomtracker.database.DatabaseHelper;
import com.example.roomtracker.model.Booking;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ScheduleFragment extends Fragment implements CalendarAdapter.OnItemListener {

    private TextView tvMonth, tvYear, tvSelectedDateFull, tvNoEvents;
    private RecyclerView rvCalendar, rvScheduleEvents;
    private ImageView btnPrev, btnNext;

    private CalendarAdapter calendarAdapter;
    private ScheduleEventAdapter eventAdapter;
    private Calendar currentMonth;
    private Calendar selectedDate;
    private DatabaseHelper db;
    private List<Booking> allBookings;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        // Only inflate the layout here
        return inflater.inflate(R.layout.fragment_schedule, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // All initialization logic happens here where Context is guaranteed
        try {
            db = new DatabaseHelper(requireContext());
            currentMonth = Calendar.getInstance();
            currentMonth.set(Calendar.DAY_OF_MONTH, 1);
            selectedDate = Calendar.getInstance();

            initializeViews(view);
            setupRecyclerViews();
            loadBookings();
            updateCalendar();
            updateEventsList(selectedDate);

        } catch (Exception e) {
            e.printStackTrace();
            // Fail gracefully
        }
    }

    private void initializeViews(View view) {
        tvMonth = view.findViewById(R.id.tvMonth);
        tvYear = view.findViewById(R.id.tvYear);
        tvSelectedDateFull = view.findViewById(R.id.tvSelectedDateFull);
        tvNoEvents = view.findViewById(R.id.tvNoEvents);
        rvCalendar = view.findViewById(R.id.rvCalendar);
        rvScheduleEvents = view.findViewById(R.id.rvScheduleEvents);
        btnPrev = view.findViewById(R.id.btnPrevMonth);
        btnNext = view.findViewById(R.id.btnNextMonth);

        btnPrev.setOnClickListener(v -> {
            if (currentMonth != null) {
                currentMonth.add(Calendar.MONTH, -1);
                updateCalendar();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (currentMonth != null) {
                currentMonth.add(Calendar.MONTH, 1);
                updateCalendar();
            }
        });

        // Month Picker
        View.OnClickListener monthPickerListener = v -> showMonthYearPicker();
        tvMonth.setOnClickListener(monthPickerListener);
        tvYear.setOnClickListener(monthPickerListener);
    }

    private void showMonthYearPicker() {
        if (getContext() == null)
            return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = new android.widget.LinearLayout(getContext());
        ((android.widget.LinearLayout) dialogView).setOrientation(android.widget.LinearLayout.HORIZONTAL);
        ((android.widget.LinearLayout) dialogView).setGravity(android.view.Gravity.CENTER);
        ((android.widget.LinearLayout) dialogView).setPadding(32, 32, 32, 32);

        final android.widget.NumberPicker monthPicker = new android.widget.NumberPicker(getContext());
        final android.widget.NumberPicker yearPicker = new android.widget.NumberPicker(getContext());

        // Setup Month Picker
        String[] months = new java.text.DateFormatSymbols(Locale.getDefault()).getShortMonths();
        // Sometimes getShortMonths returns 13 elements (with empty string at end),
        // filter it if needed or just use Manual list
        if (months.length > 12) {
            // Use manual array to be safe
            months = new String[] { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov",
                    "Dec" };
        }
        monthPicker.setMinValue(0);
        monthPicker.setMaxValue(11);
        monthPicker.setDisplayedValues(months);
        monthPicker.setValue(currentMonth.get(Calendar.MONTH));
        monthPicker.setWrapSelectorWheel(true);

        // Setup Year Picker
        int year = currentMonth.get(Calendar.YEAR);
        yearPicker.setMinValue(year - 10);
        yearPicker.setMaxValue(year + 10);
        yearPicker.setValue(year);
        yearPicker.setWrapSelectorWheel(false);

        ((android.widget.LinearLayout) dialogView).addView(monthPicker);
        ((android.widget.LinearLayout) dialogView).addView(yearPicker);

        builder.setView(dialogView)
                .setTitle("Select Month")
                .setPositiveButton("OK", (dialog, which) -> {
                    currentMonth.set(Calendar.MONTH, monthPicker.getValue());
                    currentMonth.set(Calendar.YEAR, yearPicker.getValue());
                    updateCalendar();
                })
                .setNegativeButton("Cancel", null);

        builder.create().show();
    }

    private void setupRecyclerViews() {
        if (getContext() == null)
            return;

        // Calendar Grid (7 columns)
        rvCalendar.setLayoutManager(new GridLayoutManager(getContext(), 7));
        calendarAdapter = new CalendarAdapter(new ArrayList<>(), new ArrayList<>(), "", this);
        rvCalendar.setAdapter(calendarAdapter);

        // Event List
        rvScheduleEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        eventAdapter = new ScheduleEventAdapter(new ArrayList<>(), this::showBookingDetails);
        rvScheduleEvents.setAdapter(eventAdapter);
    }

    private void loadBookings() {
        if (db == null)
            return;
        List<Booking> rawList = db.getAllBookings();
        allBookings = new ArrayList<>();
        if (rawList != null) {
            for (Booking b : rawList) {
                if ("APPROVED".equals(b.getStatus())) {
                    allBookings.add(b);
                }
            }
        }
    }

    private void updateCalendar() {
        if (currentMonth == null || getContext() == null)
            return;

        // Update Header
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM", Locale.getDefault());
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
        tvMonth.setText(monthFormat.format(currentMonth.getTime()));
        tvYear.setText(yearFormat.format(currentMonth.getTime()));

        // Generate Days
        List<String> days = new ArrayList<>();
        Calendar cal = (Calendar) currentMonth.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);

        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK); // Sun=1, Mon=2, ...
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Adjust for Monday start (Mon=1, ... Sun=7)
        int padding = 0;
        if (firstDayOfWeek == Calendar.SUNDAY) {
            padding = 6;
        } else {
            padding = firstDayOfWeek - 2;
        }

        for (int i = 0; i < padding; i++) {
            days.add(""); // Empty cells
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        List<String> eventDates = new ArrayList<>();

        for (int i = 1; i <= daysInMonth; i++) {
            cal.set(Calendar.DAY_OF_MONTH, i);
            String dateStr = dateFormat.format(cal.getTime());
            days.add(dateStr);

            // Check if this date has any booking
            if (allBookings != null) {
                for (Booking b : allBookings) {
                    if (b.getDate() != null && b.getDate().equals(dateStr)) {
                        if (!eventDates.contains(dateStr)) {
                            eventDates.add(dateStr);
                        }
                        break;
                    }
                }
            }
        }

        String selectedDateStr = dateFormat.format(selectedDate.getTime());
        calendarAdapter.updateData(days, eventDates, selectedDateStr);
    }

    private void updateEventsList(Calendar date) {
        if (date == null || getContext() == null)
            return;

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateStr = dateFormat.format(date.getTime());

        // Update Header Text
        SimpleDateFormat headerFormat = new SimpleDateFormat("EEEE, d MMMM", Locale.getDefault());
        tvSelectedDateFull.setText(headerFormat.format(date.getTime()));

        List<Booking> dailyBookings = new ArrayList<>();
        if (allBookings != null) {
            for (Booking b : allBookings) {
                if (b.getDate() != null && b.getDate().equals(dateStr)) {
                    dailyBookings.add(b);
                }
            }
        }

        if (dailyBookings.isEmpty()) {
            tvNoEvents.setVisibility(View.VISIBLE);
            rvScheduleEvents.setVisibility(View.GONE);
        } else {
            tvNoEvents.setVisibility(View.GONE);
            rvScheduleEvents.setVisibility(View.VISIBLE);
            eventAdapter.updateList(dailyBookings);
        }
    }

    @Override
    public void onItemClick(int position, String date) {
        if (date != null && !date.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                java.util.Date d = sdf.parse(date);
                Calendar cal = Calendar.getInstance();
                cal.setTime(d);
                selectedDate = cal;

                calendarAdapter.setSelectedDate(date);
                updateEventsList(selectedDate);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void showBookingDetails(Booking booking) {
        if (getContext() == null)
            return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_booking_detail, null);

        TextView tvRoom = view.findViewById(R.id.tvDetailRoom);
        TextView tvUser = view.findViewById(R.id.tvDetailUser);
        TextView tvDate = view.findViewById(R.id.tvDetailDate);
        TextView tvTime = view.findViewById(R.id.tvDetailTime);
        TextView tvStatus = view.findViewById(R.id.tvDetailStatus);
        TextView tvReason = view.findViewById(R.id.tvDetailReason);
        ImageView ivRoomImage = view.findViewById(R.id.ivDetailRoomImage);
        TextView btnClose = view.findViewById(R.id.btnCloseDetail);
        View layoutAdminActions = view.findViewById(R.id.layoutAdminActions);

        // Populate Data
        tvRoom.setText(booking.getRoomName());
        tvUser.setText(booking.getUserName());
        tvDate.setText(booking.getDate());
        tvTime.setText(booking.getStartTime() + " - " + booking.getEndTime());
        tvStatus.setText(booking.getStatus());
        tvReason.setText(booking.getReason() != null ? booking.getReason() : "-");

        // Load Image
        if (booking.getRoomImageUri() != null) {
            try {
                ivRoomImage.setImageURI(android.net.Uri.parse(booking.getRoomImageUri()));
            } catch (Exception e) {
                // Keep default
            }
        }

        // Hide Admin Actions for Schedule View (Read Only)
        if (layoutAdminActions != null) {
            layoutAdminActions.setVisibility(View.GONE);
        }

        // Hide Status for MAHASISWA, but show Peminjam
        com.example.roomtracker.utils.SessionManager session = new com.example.roomtracker.utils.SessionManager(
                getContext());
        if ("MAHASISWA".equals(session.getRole())) {
            View layoutStatus = view.findViewById(R.id.layoutDetailStatusSection);
            if (layoutStatus != null)
                layoutStatus.setVisibility(View.GONE);
        }

        builder.setView(view);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(0));

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}
