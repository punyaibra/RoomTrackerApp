package com.example.roomtracker.activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.roomtracker.R;
import com.example.roomtracker.adapter.BookingAdapter;
import com.example.roomtracker.database.DatabaseHelper;
import com.example.roomtracker.model.Booking;
import com.example.roomtracker.model.Room;
import com.example.roomtracker.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class BookingListFragment extends Fragment {

    private RecyclerView rvBookings;
    private DatabaseHelper db;
    private SessionManager session;
    private BookingAdapter adapter;
    private TabLayout tabLayoutStatus;
    private EditText etSearch;
    private FloatingActionButton btnAddBooking;
    private ImageView ivFilterIcon;
    private TextView btnPrevPage, btnNextPage;
    private LinearLayout layoutPageNumbers;
    private Spinner spinnerItemsPerPage;
    private static final int MAX_PAGE_BUTTONS = 5;

    private List<Booking> allBookings = new ArrayList<>();
    private List<Booking> filteredBookings = new ArrayList<>();
    private String currentStatusFilter = "ALL";
    private String currentSortOrder = "date_desc";

    // Pagination
    private int currentPage = 1;
    private int itemsPerPage = 10;
    private int totalPages = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_booking_list, container, false);

        db = new DatabaseHelper(getContext());
        session = new SessionManager(getContext());

        rvBookings = view.findViewById(R.id.rvBookings);
        tabLayoutStatus = view.findViewById(R.id.tabLayoutStatus);
        etSearch = view.findViewById(R.id.etSearch);
        btnAddBooking = view.findViewById(R.id.btnAddBooking);
        ivFilterIcon = view.findViewById(R.id.ivFilterIcon);
        btnPrevPage = view.findViewById(R.id.btnPrevPage);
        btnNextPage = view.findViewById(R.id.btnNextPage);
        layoutPageNumbers = view.findViewById(R.id.layoutPageNumbers);
        spinnerItemsPerPage = view.findViewById(R.id.spinnerItemsPerPage);

        rvBookings.setLayoutManager(new LinearLayoutManager(getContext()));

        setupTabs();
        setupSearch();
        setupAddButton();
        setupFilterIcon();
        setupPagination();
        setupItemsPerPageSpinner();

        // Reset adapter to ensure it gets re-created/re-attached for the new View
        adapter = null;

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadBookings();
    }

    private void loadBookings() {
        // Auto-update expired bookings to FINISHED
        db.updateExpiredBookings();

        String role = session.getRole();
        if ("USER".equals(role) || "MAHASISWA".equals(role)) {
            allBookings = db.getBookingsByUser(session.getUserId());
        } else {
            allBookings = db.getAllBookings(); // Admin/Staff
        }
        filterBookings(etSearch.getText().toString());
    }

    private void setupTabs() {
        tabLayoutStatus.addTab(tabLayoutStatus.newTab().setText("All"));
        tabLayoutStatus.addTab(tabLayoutStatus.newTab().setText("Pending"));
        tabLayoutStatus.addTab(tabLayoutStatus.newTab().setText("Approved"));
        tabLayoutStatus.addTab(tabLayoutStatus.newTab().setText("Rejected"));

        tabLayoutStatus.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        currentStatusFilter = "ALL";
                        break;
                    case 1:
                        currentStatusFilter = "PENDING";
                        break;
                    case 2:
                        currentStatusFilter = "APPROVED";
                        break;
                    case 3:
                        currentStatusFilter = "REJECTED";
                        break;
                }
                currentPage = 1;
                filterBookings(etSearch.getText().toString());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentPage = 1;
                filterBookings(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void filterBookings(String query) {
        filteredBookings.clear();
        String lowerQuery = query.toLowerCase().trim();

        for (Booking b : allBookings) {
            boolean statusMatch = "ALL".equals(currentStatusFilter)
                    || b.getStatus().equalsIgnoreCase(currentStatusFilter);
            boolean textMatch = b.getRoomName().toLowerCase().contains(lowerQuery) ||
                    b.getUserName().toLowerCase().contains(lowerQuery);

            if (statusMatch && textMatch) {
                filteredBookings.add(b);
            }
        }
        sortAndRefreshBookings();
    }

    private void setupFilterIcon() {
        ivFilterIcon.setOnClickListener(v -> {
            android.widget.PopupMenu popup = new android.widget.PopupMenu(getContext(), v);
            popup.getMenu().add("Sort by Date (Newest)");
            popup.getMenu().add("Sort by Date (Oldest)");
            popup.getMenu().add("Sort by Room");
            popup.getMenu().add("Sort by User");
            popup.getMenu().add("Sort by Status");

            popup.setOnMenuItemClickListener(item -> {
                String title = item.getTitle().toString();
                if ("Sort by Date (Newest)".equals(title)) {
                    currentSortOrder = "date_desc";
                    sortAndRefreshBookings();
                } else if ("Sort by Date (Oldest)".equals(title)) {
                    currentSortOrder = "date_asc";
                    sortAndRefreshBookings();
                } else if ("Sort by Room".equals(title)) {
                    currentSortOrder = "room";
                    sortAndRefreshBookings();
                } else if ("Sort by User".equals(title)) {
                    currentSortOrder = "user";
                    sortAndRefreshBookings();
                } else if ("Sort by Status".equals(title)) {
                    currentSortOrder = "status";
                    sortAndRefreshBookings();
                }
                return true;
            });
            popup.show();
        });
    }

    private void sortAndRefreshBookings() {
        Collections.sort(filteredBookings, (b1, b2) -> {
            switch (currentSortOrder) {
                case "date_asc":
                    return b1.getDate().compareTo(b2.getDate());
                case "room":
                    return b1.getRoomName().compareToIgnoreCase(b2.getRoomName());
                case "user":
                    return b1.getUserName().compareToIgnoreCase(b2.getUserName());
                case "status":
                    return b1.getStatus().compareTo(b2.getStatus());
                case "date_desc":
                default:
                    return b2.getDate().compareTo(b1.getDate());
            }
        });
        updatePaginatedList();
    }

    private void setupPagination() {
        btnPrevPage.setOnClickListener(v -> {
            if (currentPage > 1) {
                currentPage--;
                updatePaginatedList();
            }
        });
        btnNextPage.setOnClickListener(v -> {
            if (currentPage < totalPages) {
                currentPage++;
                updatePaginatedList();
            }
        });
    }

    private void setupItemsPerPageSpinner() {
        String[] options = { "10 / page", "20 / page", "50 / page" };
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                getContext(), R.layout.spinner_item_pagination, options);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerItemsPerPage.setAdapter(spinnerAdapter);

        spinnerItemsPerPage.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        itemsPerPage = 10;
                        break;
                    case 1:
                        itemsPerPage = 20;
                        break;
                    case 2:
                        itemsPerPage = 50;
                        break;
                }
                currentPage = 1;
                updatePaginatedList();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });
    }

    private void updatePaginatedList() {
        totalPages = (int) Math.ceil((double) filteredBookings.size() / itemsPerPage);
        if (totalPages == 0)
            totalPages = 1;
        if (currentPage > totalPages)
            currentPage = totalPages;
        if (currentPage < 1)
            currentPage = 1;

        int startIndex = (currentPage - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, filteredBookings.size());

        List<Booking> paginatedList = new ArrayList<>();
        if (startIndex < filteredBookings.size()) {
            paginatedList = filteredBookings.subList(startIndex, endIndex);
        }

        if (adapter == null) {
            boolean isStaffOrAdmin = "ADMIN".equals(session.getRole()) || "STAFF".equals(session.getRole());
            adapter = new BookingAdapter(paginatedList, isStaffOrAdmin, booking -> showActionDialog(booking));
            rvBookings.setAdapter(adapter);
        } else {
            adapter.updateList(paginatedList);
        }

        createPageNumberButtons();
        btnPrevPage.setEnabled(currentPage > 1);
        btnNextPage.setEnabled(currentPage < totalPages);
        btnPrevPage.setAlpha(currentPage > 1 ? 1.0f : 0.4f);
        btnNextPage.setAlpha(currentPage < totalPages ? 1.0f : 0.4f);
    }

    private void createPageNumberButtons() {
        layoutPageNumbers.removeAllViews();
        int startPage = Math.max(1, currentPage - 2);
        int endPage = Math.min(totalPages, startPage + MAX_PAGE_BUTTONS - 1);
        if (endPage - startPage < MAX_PAGE_BUTTONS - 1) {
            startPage = Math.max(1, endPage - MAX_PAGE_BUTTONS + 1);
        }

        for (int i = startPage; i <= endPage; i++) {
            final int pageNum = i;
            TextView pageButton = new TextView(getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    (int) (28 * getResources().getDisplayMetrics().density),
                    (int) (28 * getResources().getDisplayMetrics().density));
            params.setMargins(3, 0, 3, 0);
            pageButton.setLayoutParams(params);
            pageButton.setText(String.valueOf(pageNum));
            pageButton.setTextSize(11);
            pageButton.setGravity(android.view.Gravity.CENTER);
            pageButton.setClickable(true);
            pageButton.setFocusable(true);

            if (pageNum == currentPage) {
                pageButton.setBackgroundResource(R.drawable.bg_search_bar);
                pageButton.setTextColor(android.graphics.Color.parseColor("#1A73E8"));
            } else {
                pageButton.setBackground(null);
                pageButton.setTextColor(android.graphics.Color.parseColor("#757575"));
            }

            pageButton.setOnClickListener(v -> {
                currentPage = pageNum;
                updatePaginatedList();
            });
            layoutPageNumbers.addView(pageButton);
        }
    }

    private void setupAddButton() {
        if ("STAFF".equals(session.getRole())) {
            btnAddBooking.setVisibility(View.VISIBLE);
            btnAddBooking.setOnClickListener(v -> showAddBookingDialog());
        } else {
            btnAddBooking.setVisibility(View.GONE);
        }
    }

    private void showAddBookingDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_booking_form, null);
        Spinner spinnerRoom = dialogView.findViewById(R.id.spinnerRoom);
        EditText etDate = dialogView.findViewById(R.id.etDialogDate);
        EditText etStartTime = dialogView.findViewById(R.id.etDialogStartTime);
        EditText etEndTime = dialogView.findViewById(R.id.etDialogEndTime);

        // Load Rooms
        List<Room> rooms = db.getAllRooms();
        List<String> roomNames = new ArrayList<>();
        for (Room r : rooms) {
            roomNames.add(r.getName());
        }
        ArrayAdapter<String> roomAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_dropdown_item, roomNames);
        spinnerRoom.setAdapter(roomAdapter);

        // Date Picker
        Calendar calendar = Calendar.getInstance();
        etDate.setOnClickListener(v -> {
            new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
                String date = String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, dayOfMonth);
                etDate.setText(date);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        // Time Pickers
        etStartTime.setOnClickListener(v -> showTimePicker(etStartTime));
        etEndTime.setOnClickListener(v -> showTimePicker(etEndTime));

        new AlertDialog.Builder(getContext())
                .setTitle("Add New Booking")
                .setView(dialogView)
                .setPositiveButton("Create", (dialog, which) -> {
                    if (spinnerRoom.getSelectedItem() == null) {
                        Toast.makeText(getContext(), "No room selected", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int selectedRoomIndex = spinnerRoom.getSelectedItemPosition();
                    Room selectedRoom = rooms.get(selectedRoomIndex);

                    String date = etDate.getText().toString().trim();
                    String start = etStartTime.getText().toString().trim();
                    String end = etEndTime.getText().toString().trim();

                    if (date.isEmpty() || start.isEmpty() || end.isEmpty()) {
                        Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (start.compareTo(end) >= 0) {
                        Toast.makeText(getContext(), "End time must be after start time", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Book as current user (Admin/Staff)
                    Booking newBooking = new Booking(selectedRoom.getId(), session.getUserId(), date, start, end,
                            "APPROVED", "Manual Booking"); // Admin bookings are auto-approved

                    long result = db.addBooking(newBooking);
                    if (result > 0) {
                        Toast.makeText(getContext(), "Booking Created", Toast.LENGTH_SHORT).show();
                        loadBookings();
                    } else {
                        Toast.makeText(getContext(), "Failed: Time Slot Conflict", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showTimePicker(EditText target) {
        Calendar calendar = Calendar.getInstance();
        new TimePickerDialog(getContext(), (view, hourOfDay, minute) -> {
            String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
            target.setText(time);
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
    }

    private void showActionDialog(Booking booking) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_booking_detail, null);

        TextView tvRoom = view.findViewById(R.id.tvDetailRoom);
        TextView tvUser = view.findViewById(R.id.tvDetailUser);
        TextView tvDate = view.findViewById(R.id.tvDetailDate);
        TextView tvTime = view.findViewById(R.id.tvDetailTime);
        TextView tvStatus = view.findViewById(R.id.tvDetailStatus);
        TextView tvReason = view.findViewById(R.id.tvDetailReason);
        ImageView ivRoomImage = view.findViewById(R.id.ivDetailRoomImage);
        LinearLayout layoutAdminActions = view.findViewById(R.id.layoutAdminActions);
        Button btnApprove = view.findViewById(R.id.btnApprove);
        Button btnReject = view.findViewById(R.id.btnReject);
        TextView btnClose = view.findViewById(R.id.btnCloseDetail);

        tvRoom.setText(booking.getRoomName());
        tvUser.setText(booking.getUserName());
        tvDate.setText(booking.getDate());
        tvTime.setText(booking.getStartTime() + " - " + booking.getEndTime());
        tvStatus.setText(booking.getStatus());
        tvReason.setText(booking.getReason() != null ? booking.getReason() : "-");

        if (booking.getRoomImageUri() != null) {
            try {
                ivRoomImage.setImageURI(android.net.Uri.parse(booking.getRoomImageUri()));
            } catch (Exception e) {
                // Keep default
            }
        }

        String role = session.getRole();
        if ("ADMIN".equals(role) || "STAFF".equals(role)) {
            layoutAdminActions.setVisibility(View.VISIBLE);

            // Listeners set after dialog creation to access dismiss()
        } else {
            layoutAdminActions.setVisibility(View.GONE);
        }

        builder.setView(view);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(0));

        // Now set listeners that need dialog instance reference
        if ("ADMIN".equals(role) || "STAFF".equals(role)) {
            btnApprove.setOnClickListener(v -> {
                if (db.updateBookingStatus(booking.getId(), "APPROVED")) {
                    Toast.makeText(getContext(), "Booking Approved", Toast.LENGTH_SHORT).show();
                    loadBookings();
                    dialog.dismiss();
                } else {
                    Toast.makeText(getContext(), "Failed: Conflict with existing booking.", Toast.LENGTH_LONG).show();
                }
            });
            btnReject.setOnClickListener(v -> {
                db.updateBookingStatus(booking.getId(), "REJECTED");
                Toast.makeText(getContext(), "Booking Rejected", Toast.LENGTH_SHORT).show();
                loadBookings();
                dialog.dismiss();
            });
        }

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}
