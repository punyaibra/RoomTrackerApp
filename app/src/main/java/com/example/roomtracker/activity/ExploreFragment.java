package com.example.roomtracker.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

public class ExploreFragment extends Fragment {

    private RecyclerView rvExploreRooms;
    private DatabaseHelper db;
    private ExploreRoomAdapter adapter;
    private EditText etSearch;
    private List<Room> allRooms = new ArrayList<>();
    private List<Room> filteredRooms = new ArrayList<>();

    // Chips
    private TextView chipAll, chipAvailable, chipLantai1, chipLantai2, chipLarge;
    private ImageView ivFilters;
    private String currentFilter = "ALL";
    private String currentSort = "NAME";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_explore_rooms, container, false);

        db = new DatabaseHelper(getContext());
        rvExploreRooms = view.findViewById(R.id.rvExploreRooms);
        etSearch = view.findViewById(R.id.etExploreSearch);

        chipAll = view.findViewById(R.id.chipAll);
        chipAvailable = view.findViewById(R.id.chipAvailable);
        chipLantai1 = view.findViewById(R.id.chipLantai1);
        chipLantai2 = view.findViewById(R.id.chipLantai2);
        chipLarge = view.findViewById(R.id.chipLarge);

        rvExploreRooms.setLayoutManager(new LinearLayoutManager(getContext()));

        setupSearch();
        setupChips();
        setupFilterMenu(view);

        return view;
    }

    private void setupFilterMenu(View view) {
        ivFilters = view.findViewById(R.id.ivFilters);
        if (ivFilters == null)
            return; // Safety check

        ivFilters.setOnClickListener(v -> {
            android.widget.PopupMenu popup = new android.widget.PopupMenu(getContext(), v);
            popup.getMenu().add("Sort by Name (A-Z)");
            popup.getMenu().add("Sort by Capacity (High-Low)");
            popup.getMenu().add("Sort by Floor (1-2)");

            popup.setOnMenuItemClickListener(item -> {
                String title = item.getTitle().toString();
                if (title.contains("Name"))
                    currentSort = "NAME";
                else if (title.contains("Capacity"))
                    currentSort = "CAPACITY";
                else if (title.contains("Floor"))
                    currentSort = "FLOOR";

                applyFilter();
                return true;
            });
            popup.show();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadRooms();
    }

    private void loadRooms() {
        allRooms = db.getAllRooms();
        applyFilter();
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilter();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupChips() {
        View.OnClickListener chipListener = v -> {
            resetChips();
            v.setBackgroundResource(R.drawable.bg_chip_selected);
            ((TextView) v).setTextColor(android.graphics.Color.WHITE);

            if (v == chipAll)
                currentFilter = "ALL";
            else if (v == chipAvailable)
                currentFilter = "AVAILABLE";
            else if (v == chipLantai1)
                currentFilter = "FLOOR1";
            else if (v == chipLantai2)
                currentFilter = "FLOOR2";
            else if (v == chipLarge)
                currentFilter = "LARGE";

            applyFilter();
        };

        chipAll.setOnClickListener(chipListener);
        chipAvailable.setOnClickListener(chipListener);
        chipLantai1.setOnClickListener(chipListener);
        chipLantai2.setOnClickListener(chipListener);
        chipLarge.setOnClickListener(chipListener);
    }

    private void resetChips() {
        styleChipUnselected(chipAll);
        styleChipUnselected(chipAvailable);
        styleChipUnselected(chipLantai1);
        styleChipUnselected(chipLantai2);
        styleChipUnselected(chipLarge);
    }

    private void styleChipUnselected(TextView chip) {
        chip.setBackgroundResource(R.drawable.bg_chip_unselected);
        chip.setTextColor(android.graphics.Color.parseColor("#1A73E8"));
    }

    private void applyFilter() {
        filteredRooms.clear();
        String query = etSearch.getText().toString().toLowerCase().trim();

        for (Room r : allRooms) {
            boolean matchesSearch = r.getName().toLowerCase().contains(query) ||
                    r.getBuilding().toLowerCase().contains(query);

            boolean matchesFilter = true;
            switch (currentFilter) {
                case "FLOOR1":
                    matchesFilter = r.getFloor() != null && r.getFloor().contains("1");
                    break;
                case "FLOOR2":
                    matchesFilter = r.getFloor() != null && r.getFloor().contains("2");
                    break;
                case "LARGE":
                    matchesFilter = r.getCapacity() > 30;
                    break;
                case "AVAILABLE":
                    // Room is available if: ACTIVE and not currently occupied
                    boolean isActive = !"INACTIVE".equals(r.getStatus());
                    boolean isOccupied = db.isRoomCurrentlyOccupied(r.getId());
                    matchesFilter = isActive && !isOccupied;
                    break;
            }

            if (matchesSearch && matchesFilter) {
                filteredRooms.add(r);
            }
        }

        // Apply Sorting
        java.util.Collections.sort(filteredRooms, (r1, r2) -> {
            switch (currentSort) {
                case "CAPACITY":
                    return Integer.compare(r2.getCapacity(), r1.getCapacity()); // Descending
                case "FLOOR":
                    String f1 = r1.getFloor() != null ? r1.getFloor() : "";
                    String f2 = r2.getFloor() != null ? r2.getFloor() : "";
                    return f1.compareTo(f2);
                case "NAME":
                default:
                    return r1.getName().compareToIgnoreCase(r2.getName());
            }
        });

        if (adapter == null) {
            adapter = new ExploreRoomAdapter(filteredRooms, this::showRoomDetail);
            rvExploreRooms.setAdapter(adapter);
        } else {
            adapter.updateList(filteredRooms);
        }
    }

    private void showRoomDetail(Room room) {
        // Re-use logic from RoomListFragment or open BookingActivity directly
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_room_detail, null);

        TextView tvName = view.findViewById(R.id.tvDetailName);
        TextView tvLocation = view.findViewById(R.id.tvDetailLocation);
        TextView tvCapacity = view.findViewById(R.id.tvDetailCapacity);
        TextView tvSize = view.findViewById(R.id.tvDetailSize);
        TextView tvFacilities = view.findViewById(R.id.tvDetailFacilities);
        TextView tvDescription = view.findViewById(R.id.tvDetailDescription);
        ImageView ivImage = view.findViewById(R.id.ivDetailImage);
        android.widget.Button btnClose = view.findViewById(R.id.btnCloseDetail);
        android.widget.Button btnBook = view.findViewById(R.id.btnBookRoom); // New Button

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

        // Booking Logic - Always show button for ACTIVE rooms
        btnBook.setVisibility(View.VISIBLE);

        // Only check if room is globally ACTIVE, not current occupancy
        // User can pick any date/time they want, validation happens on submit
        boolean isActive = !"INACTIVE".equals(room.getStatus());

        if (!isActive) {
            btnBook.setText("Room Inactive");
            btnBook.setEnabled(false);
            btnBook.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.GRAY));
        } else {
            btnBook.setText("Book Room");
            btnBook.setEnabled(true);
            btnBook.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#1A73E8")));
            btnBook.setOnClickListener(v -> {
                dialog.dismiss();
                showBookingDialog(room);
            });
        }

        dialog.show();
    }

    private void showBookingDialog(Room room) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = getLayoutInflater().inflate(R.layout.activity_booking_form, null);

        TextView tvRoomName = view.findViewById(R.id.tvRoomNameDisplay);
        EditText etDate = view.findViewById(R.id.etBookingDate);
        EditText etStartTime = view.findViewById(R.id.etStartTime);
        EditText etEndTime = view.findViewById(R.id.etEndTime);
        EditText etReason = view.findViewById(R.id.etBookingReason);
        TextView btnSubmit = view.findViewById(R.id.btnSubmitBooking);
        TextView btnCancel = view.findViewById(R.id.btnCancelBooking);

        tvRoomName.setText(room.getName());

        // Date Picker
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        etDate.setOnClickListener(v -> {
            new android.app.DatePickerDialog(getContext(), (picker, year, month, dayOfMonth) -> {
                String date = String.format(java.util.Locale.getDefault(), "%d-%02d-%02d", year, month + 1, dayOfMonth);
                etDate.setText(date);
            }, calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH),
                    calendar.get(java.util.Calendar.DAY_OF_MONTH)).show();
        });

        // Time Pickers
        etStartTime.setOnClickListener(v -> showTimePicker(etStartTime));
        etEndTime.setOnClickListener(v -> showTimePicker(etEndTime));

        builder.setView(view);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(0));
        }

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

            if (start.compareTo(end) >= 0) {
                android.widget.Toast
                        .makeText(getContext(), "End time must be after start time", android.widget.Toast.LENGTH_SHORT)
                        .show();
                return;
            }

            com.example.roomtracker.model.Booking booking = new com.example.roomtracker.model.Booking(
                    room.getId(),
                    new SessionManager(getContext()).getUserId(),
                    date, start, end, "PENDING", reason);

            long result = db.addBooking(booking);
            if (result == -1) {
                android.widget.Toast.makeText(getContext(), "Time slot conflict! Please choose another time.",
                        android.widget.Toast.LENGTH_LONG).show();
            } else if (result == -2) {
                android.widget.Toast.makeText(getContext(), "Room is currently INACTIVE and cannot be booked.",
                        android.widget.Toast.LENGTH_LONG).show();
            } else if (result > 0) {
                android.widget.Toast
                        .makeText(getContext(), "Booking Submitted Successfully", android.widget.Toast.LENGTH_SHORT)
                        .show();
                dialog.dismiss();
            } else {
                android.widget.Toast
                        .makeText(getContext(), "Failed to submit booking", android.widget.Toast.LENGTH_SHORT).show();
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
