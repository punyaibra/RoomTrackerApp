package com.example.roomtracker.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.roomtracker.R;
import com.example.roomtracker.adapter.HistoryAdapter;
import com.example.roomtracker.database.DatabaseHelper;
import com.example.roomtracker.model.Booking;
import com.example.roomtracker.utils.SessionManager;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class LogFragment extends Fragment {

    private EditText etSearch;
    private TabLayout tabLayoutStatus;
    private RecyclerView rvHistory;
    private TextView tvEmptyState;

    private DatabaseHelper db;
    private SessionManager session;
    private HistoryAdapter adapter;

    private List<Booking> allHistory = new ArrayList<>();
    private List<Booking> filteredHistory = new ArrayList<>();
    private String currentFilter = "ALL"; // ALL, FINISHED, REJECTED

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_log, container, false);

        db = new DatabaseHelper(getContext());
        session = new SessionManager(getContext());

        etSearch = view.findViewById(R.id.etSearchHistory);
        tabLayoutStatus = view.findViewById(R.id.tabLayoutHistory);
        rvHistory = view.findViewById(R.id.rvHistory);
        tvEmptyState = view.findViewById(R.id.tvEmptyHistory);

        rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));

        setupTabs();
        setupSearch();
        setupFilterButton(view);

        return view;
    }

    private void setupFilterButton(View view) {
        android.widget.ImageView ivFilterIcon = view.findViewById(R.id.ivFilterIcon);
        if (ivFilterIcon != null) {
            ivFilterIcon.setOnClickListener(v -> {
                android.widget.PopupMenu popup = new android.widget.PopupMenu(getContext(), v);
                popup.getMenu().add("Urutkan: Terbaru");
                popup.getMenu().add("Urutkan: Terlama");
                popup.getMenu().add("Urutkan: Nama Ruangan (A-Z)");

                popup.setOnMenuItemClickListener(item -> {
                    String title = item.getTitle().toString();
                    if (title.contains("Terbaru")) {
                        // Sort by date descending
                        java.util.Collections.sort(filteredHistory, (b1, b2) -> b2.getDate().compareTo(b1.getDate()));
                    } else if (title.contains("Terlama")) {
                        // Sort by date ascending
                        java.util.Collections.sort(filteredHistory, (b1, b2) -> b1.getDate().compareTo(b2.getDate()));
                    } else if (title.contains("Nama Ruangan")) {
                        // Sort by room name
                        java.util.Collections.sort(filteredHistory, (b1, b2) -> {
                            String room1 = db.getRoom(b1.getRoomId()).getName();
                            String room2 = db.getRoom(b2.getRoomId()).getName();
                            return room1.compareToIgnoreCase(room2);
                        });
                    }

                    if (adapter != null) {
                        adapter.updateList(filteredHistory);
                    }
                    return true;
                });
                popup.show();
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadHistory();
    }

    private void setupTabs() {
        tabLayoutStatus.removeAllTabs(); // Clear existing tabs to avoid duplication on resume if any

        tabLayoutStatus.addTab(tabLayoutStatus.newTab().setText("SEMUA"));

        boolean isMahasiswa = "MAHASISWA".equals(session.getRole());
        if (isMahasiswa) {
            tabLayoutStatus.addTab(tabLayoutStatus.newTab().setText("PENDING"));
        }

        tabLayoutStatus.addTab(tabLayoutStatus.newTab().setText("SELESAI"));
        tabLayoutStatus.addTab(tabLayoutStatus.newTab().setText("DITOLAK"));

        tabLayoutStatus.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String tabTitle = tab.getText().toString();
                if ("SEMUA".equals(tabTitle)) {
                    currentFilter = "ALL";
                } else if ("PENDING".equals(tabTitle)) {
                    currentFilter = "PENDING";
                } else if ("SELESAI".equals(tabTitle)) {
                    currentFilter = "FINISHED";
                } else if ("DITOLAK".equals(tabTitle)) {
                    currentFilter = "REJECTED";
                }
                filterHistory(etSearch.getText().toString());
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
                filterHistory(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void loadHistory() {
        // Auto-update expired bookings first
        // db.updateExpiredBookings(); // Commented out for testing Pending status as
        // per request

        String role = session.getRole();
        if ("USER".equals(role) || "MAHASISWA".equals(role)) {
            allHistory = db.getBookingsByUser(session.getUserId());
        } else {
            allHistory = db.getAllBookings(); // Admin/Staff can see all
        }

        // Filter only FINISHED and REJECTED bookings (and PENDING for Mahasiswa)
        List<Booking> historyOnly = new ArrayList<>();
        for (Booking booking : allHistory) {
            boolean shouldInclude = "FINISHED".equals(booking.getStatus()) || "REJECTED".equals(booking.getStatus());
            if ("MAHASISWA".equals(role) && "PENDING".equals(booking.getStatus())) {
                shouldInclude = true;
            }

            if (shouldInclude) {
                historyOnly.add(booking);
            }
        }
        allHistory = historyOnly;

        filterHistory(etSearch.getText().toString());
    }

    private void filterHistory(String query) {
        filteredHistory.clear();
        String lowerQuery = query.toLowerCase().trim();

        for (Booking booking : allHistory) {
            boolean matchesSearch = booking.getReason().toLowerCase().contains(lowerQuery) ||
                    db.getRoom(booking.getRoomId()).getName().toLowerCase().contains(lowerQuery) ||
                    db.getUser(booking.getUserId()).getName().toLowerCase().contains(lowerQuery);

            boolean matchesFilter = true;
            if (!"ALL".equals(currentFilter)) {
                matchesFilter = currentFilter.equals(booking.getStatus());
            }

            if (matchesSearch && matchesFilter) {
                filteredHistory.add(booking);
            }
        }

        if (filteredHistory.isEmpty()) {
            rvHistory.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);

            if ("FINISHED".equals(currentFilter)) {
                tvEmptyState.setText("Belum ada riwayat peminjaman yang selesai");
            } else if ("REJECTED".equals(currentFilter)) {
                tvEmptyState.setText("Belum ada peminjaman yang ditolak");
            } else if ("PENDING".equals(currentFilter)) {
                tvEmptyState.setText("Belum ada peminjaman yang pending");
            } else {
                tvEmptyState.setText("Belum ada riwayat peminjaman");
            }
        } else {
            rvHistory.setVisibility(View.VISIBLE);
            tvEmptyState.setVisibility(View.GONE);
        }

        if (adapter == null) {
            adapter = new HistoryAdapter(filteredHistory, getContext(), this::showBookingDetails);
            rvHistory.setAdapter(adapter);
        } else {
            adapter.updateList(filteredHistory);
        }
    }

    private void showBookingDetails(Booking booking) {
        if (getContext() == null)
            return;

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_booking_detail, null);

        TextView tvRoom = view.findViewById(R.id.tvDetailRoom);
        TextView tvUser = view.findViewById(R.id.tvDetailUser);
        TextView tvDate = view.findViewById(R.id.tvDetailDate);
        TextView tvTime = view.findViewById(R.id.tvDetailTime);
        TextView tvStatus = view.findViewById(R.id.tvDetailStatus);
        TextView tvReason = view.findViewById(R.id.tvDetailReason);
        android.widget.ImageView ivRoomImage = view.findViewById(R.id.ivDetailRoomImage);
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

        // Hide Admin Actions for History Log View
        if (layoutAdminActions != null) {
            layoutAdminActions.setVisibility(View.GONE);
        }

        builder.setView(view);
        android.app.AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(0));

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}
