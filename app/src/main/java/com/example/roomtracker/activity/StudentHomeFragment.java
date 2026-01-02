package com.example.roomtracker.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.roomtracker.R;
import com.example.roomtracker.adapter.ExploreRoomAdapter;
import com.example.roomtracker.database.DatabaseHelper;
import com.example.roomtracker.model.Room;
import com.example.roomtracker.utils.SessionManager;
import java.util.ArrayList;
import java.util.List;

public class StudentHomeFragment extends Fragment {

    private RecyclerView rvRecommended;
    private RecyclerView rvLatest; // Added rvLatest
    private DatabaseHelper db;
    private com.example.roomtracker.adapter.SliderRoomAdapter adapter; // Changed to SliderRoomAdapter
    private View cardSearch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_student_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = new DatabaseHelper(getContext());
        rvRecommended = view.findViewById(R.id.rvRecommended);
        rvLatest = view.findViewById(R.id.rvLatest); // Bind rvLatest
        cardSearch = view.findViewById(R.id.cardSearch);

        // Set Horizontal Layout Managers
        rvRecommended.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvRecommended.setNestedScrollingEnabled(false);

        rvLatest.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvLatest.setNestedScrollingEnabled(false);

        androidx.core.widget.NestedScrollView nestedScrollView = view.findViewById(R.id.nestedScrollView);
        if (nestedScrollView != null) {
            nestedScrollView.post(() -> nestedScrollView.scrollTo(0, 0));
        }

        cardSearch.setOnClickListener(v -> {
            // Navigate to Explore Fragment (Full Search)
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToExplore();
            }
        });

        loadRecommendedRooms();
    }

    private void loadRecommendedRooms() {
        if (db == null)
            return;
        List<Room> allRooms = db.getAllRooms();
        List<Room> recommended = new ArrayList<>();

        if (allRooms == null)
            return;

        // Recommended: Filter key "Recommended" or just simple logic (e.g. Available &
        // Large?)
        // For now, let's just take the first 5 Active rooms
        for (Room r : allRooms) {
            if (!"INACTIVE".equals(r.getStatus())) {
                recommended.add(r);
                if (recommended.size() >= 5)
                    break;
            }
        }

        // Latest: Just take the last 5 Active rooms (simulated by reversing)
        List<Room> latest = new ArrayList<>();
        // In a real app, sort by ID desc or date. Here we just reverse for variety
        List<Room> reversedList = new ArrayList<>(allRooms);
        java.util.Collections.reverse(reversedList);
        for (Room r : reversedList) {
            if (!"INACTIVE".equals(r.getStatus())) {
                latest.add(r);
                if (latest.size() >= 5)
                    break;
            }
        }

        // Use SliderAdapter for horizontal layout
        adapter =  new com.example.roomtracker.adapter.SliderRoomAdapter(recommended, this::showRoomDetail);
        rvRecommended.setAdapter(adapter);

        com.example.roomtracker.adapter.SliderRoomAdapter adapterLatest = new com.example.roomtracker.adapter.SliderRoomAdapter(
                latest, this::showRoomDetail);
        rvLatest.setAdapter(adapterLatest);
    }

    private void showRoomDetail(Room room) {
        if (getContext() == null)
            return;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_room_detail, null);

        TextView tvName = view.findViewById(R.id.tvDetailName);
        TextView tvLocation = view.findViewById(R.id.tvDetailLocation);
        TextView tvCapacity = view.findViewById(R.id.tvDetailCapacity);
        TextView tvSize = view.findViewById(R.id.tvDetailSize);
        TextView tvFacilities = view.findViewById(R.id.tvDetailFacilities);
        TextView tvDescription = view.findViewById(R.id.tvDetailDescription);
        ImageView ivImage = view.findViewById(R.id.ivDetailImage);
        android.widget.Button btnClose = view.findViewById(R.id.btnCloseDetail);
        android.widget.Button btnBook = view.findViewById(R.id.btnBookRoom);

        tvName.setText(room.getName());
        tvLocation.setText(room.getBuilding() + ", Floor " + room.getFloor());
        tvCapacity.setText(room.getCapacity() + " People");
        tvSize.setText(room.getSize());
        tvFacilities.setText(room.getFacilities());
        tvDescription.setText(room.getDescription());

        if (room.getImageUri() != null) {
            try {
                ivImage.setImageURI(android.net.Uri.parse(room.getImageUri()));
            } catch (Exception e) {
            }
        }

        builder.setView(view);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(0));

        btnClose.setOnClickListener(v -> dialog.dismiss());
        btnBook.setVisibility(View.VISIBLE);
        btnBook.setOnClickListener(v -> {
            dialog.dismiss();
            showBookingDialog(room);
        });

        dialog.show();
    }

    private void showBookingDialog(Room room) {
        if (getContext() == null)
            return;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.activity_booking_form, null);

        TextView tvRoomName = view.findViewById(R.id.tvRoomNameDisplay);
        EditText etDate = view.findViewById(R.id.etBookingDate);
        EditText etStartTime = view.findViewById(R.id.etStartTime);
        EditText etEndTime = view.findViewById(R.id.etEndTime);
        EditText etReason = view.findViewById(R.id.etBookingReason);
        TextView btnSubmit = view.findViewById(R.id.btnSubmitBooking);
        TextView btnCancel = view.findViewById(R.id.btnCancelBooking);

        tvRoomName.setText(room.getName());

        java.util.Calendar calendar = java.util.Calendar.getInstance();
        etDate.setOnClickListener(v -> {
            new android.app.DatePickerDialog(getContext(), (picker, year, month, dayOfMonth) -> {
                String date = String.format(java.util.Locale.getDefault(), "%d-%02d-%02d", year, month + 1, dayOfMonth);
                etDate.setText(date);
            }, calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH),
                    calendar.get(java.util.Calendar.DAY_OF_MONTH)).show();
        });

        etStartTime.setOnClickListener(v -> showTimePicker(etStartTime));
        etEndTime.setOnClickListener(v -> showTimePicker(etEndTime));

        builder.setView(view);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(0));

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSubmit.setOnClickListener(v -> {
            String date = etDate.getText().toString().trim();
            String start = etStartTime.getText().toString().trim();
            String end = etEndTime.getText().toString().trim();
            String reason = etReason.getText().toString().trim();

            if (android.text.TextUtils.isEmpty(date) || android.text.TextUtils.isEmpty(start) ||
                    android.text.TextUtils.isEmpty(end) || android.text.TextUtils.isEmpty(reason)) {
                android.widget.Toast.makeText(getContext(), "Please fill all fields", android.widget.Toast.LENGTH_SHORT)
                        .show();
                return;
            }

            com.example.roomtracker.model.Booking booking = new com.example.roomtracker.model.Booking(
                    room.getId(),
                    new SessionManager(getContext()).getUserId(),
                    date, start, end, "PENDING", reason);

            long result = db.addBooking(booking);
            if (result > 0) {
                android.widget.Toast
                        .makeText(getContext(), "Booking Submitted Successfully", android.widget.Toast.LENGTH_SHORT)
                        .show();
                dialog.dismiss();
            } else {
                android.widget.Toast
                        .makeText(getContext(), "Booking Failed: " + result, android.widget.Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void showTimePicker(EditText target) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        new android.app.TimePickerDialog(getContext(), (view, hourOfDay, minute) -> {
            String time = String.format(java.util.Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
            target.setText(time);
        }, calendar.get(java.util.Calendar.HOUR_OF_DAY), calendar.get(java.util.Calendar.MINUTE), true).show();
    }
}
