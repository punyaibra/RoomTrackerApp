package com.example.roomtracker.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
import com.example.roomtracker.adapter.RoomAdapter;
import com.example.roomtracker.database.DatabaseHelper;
import com.example.roomtracker.model.Room;
import com.example.roomtracker.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RoomListFragment extends Fragment {

    private RecyclerView rvRooms;
    private FloatingActionButton fabAddRoom;
    private DatabaseHelper db;
    private SessionManager session;
    private RoomAdapter adapter;
    private EditText etSearch;
    private ImageView ivFilterIcon;
    private TextView btnPrevPage, btnNextPage;
    private LinearLayout layoutPageNumbers;
    private Spinner spinnerItemsPerPage;
    private static final int MAX_PAGE_BUTTONS = 5;

    private List<Room> allRooms = new ArrayList<>();
    private List<Room> filteredRooms = new ArrayList<>();
    private String currentSortOrder = "name"; // name, capacity, location

    // Pagination
    private int currentPage = 1;
    private int itemsPerPage = 10;
    private int totalPages = 1;

    // Room Form Dialog State
    private android.widget.ImageView ivCurrentDialogImage;
    private String currentImageUriString;

    private ImageView ivMenuIcon;
    private TextView tvPageTitle;

    private final androidx.activity.result.ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new androidx.activity.result.contract.ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null && ivCurrentDialogImage != null) {
                    currentImageUriString = saveImageToInternalStorage(uri);
                    ivCurrentDialogImage.setImageURI(android.net.Uri.parse(currentImageUriString));
                    ivCurrentDialogImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_room_list, container, false);

        db = new DatabaseHelper(getContext());
        session = new SessionManager(getContext());

        rvRooms = view.findViewById(R.id.rvRooms);
        fabAddRoom = view.findViewById(R.id.fabAddRoom);
        etSearch = view.findViewById(R.id.etSearch);
        ivFilterIcon = view.findViewById(R.id.ivFilterIcon);
        btnPrevPage = view.findViewById(R.id.btnPrevPage);
        btnNextPage = view.findViewById(R.id.btnNextPage);
        layoutPageNumbers = view.findViewById(R.id.layoutPageNumbers);
        spinnerItemsPerPage = view.findViewById(R.id.spinnerItemsPerPage);

        rvRooms.setLayoutManager(new LinearLayoutManager(getContext()));

        setupAddButton();
        setupSearch();
        setupFilterIcon();

        ivMenuIcon = view.findViewById(R.id.ivMenuIcon); // Changed ID
        tvPageTitle = view.findViewById(R.id.tvPageTitle); // New ID

        // Set personalized greeting
        String userName = session.getUserDetails().getName();
        if (userName != null && !userName.isEmpty()) {
            tvPageTitle.setText("Hi, " + userName);
        } else {
            tvPageTitle.setText("Hi, User");
        }

        // Setup Menu Icon Click
        ivMenuIcon.setOnClickListener(v -> {
            android.widget.PopupMenu popup = new android.widget.PopupMenu(getContext(), v);
            popup.getMenu().add("Schedule");
            popup.getMenu().add("Explore");

            popup.setOnMenuItemClickListener(item -> {
                String title = item.getTitle().toString();
                if ("Schedule".equals(title)) {
                    getParentFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, new ScheduleFragment())
                            .addToBackStack(null)
                            .commit();
                } else if ("Explore".equals(title)) {
                    getParentFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, new ExploreFragment())
                            .addToBackStack(null)
                            .commit();
                }
                return true;
            });
            popup.show();
        });

        // Initialize other UI elements...
        etSearch = view.findViewById(R.id.etSearch);
        ivFilterIcon = view.findViewById(R.id.ivFilterIcon);
        btnPrevPage = view.findViewById(R.id.btnPrevPage);
        btnNextPage = view.findViewById(R.id.btnNextPage);
        layoutPageNumbers = view.findViewById(R.id.layoutPageNumbers);
        spinnerItemsPerPage = view.findViewById(R.id.spinnerItemsPerPage);
        setupPagination();
        setupItemsPerPageSpinner();

        // Reset adapter to ensure it gets re-created/re-attached for the new View
        adapter = null;

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadRooms();
    }

    private void loadRooms() {
        allRooms = db.getAllRooms();
        filterRooms(etSearch.getText().toString());
    }

    private void setupAddButton() {
        if ("ADMIN".equals(session.getRole()) || "STAFF".equals(session.getRole())) {
            fabAddRoom.setVisibility(View.VISIBLE);
            fabAddRoom.setOnClickListener(v -> showRoomFormDialog(null));
        } else {
            fabAddRoom.setVisibility(View.GONE);
        }
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentPage = 1;
                filterRooms(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void filterRooms(String query) {
        filteredRooms.clear();
        String lowerQuery = query.toLowerCase().trim();

        for (Room r : allRooms) {
            boolean match = r.getName().toLowerCase().contains(lowerQuery) ||
                    r.getLocation().toLowerCase().contains(lowerQuery);
            if (match) {
                filteredRooms.add(r);
            }
        }
        sortAndRefreshRooms();
    }

    private void setupFilterIcon() {
        ivFilterIcon.setOnClickListener(v -> {
            android.widget.PopupMenu popup = new android.widget.PopupMenu(getContext(), v);
            popup.getMenu().add("Sort by Name");
            popup.getMenu().add("Sort by Capacity");
            popup.getMenu().add("Sort by Location");

            popup.setOnMenuItemClickListener(item -> {
                String title = item.getTitle().toString();
                if ("Sort by Name".equals(title)) {
                    currentSortOrder = "name";
                } else if ("Sort by Capacity".equals(title)) {
                    currentSortOrder = "capacity";
                } else if ("Sort by Location".equals(title)) {
                    currentSortOrder = "location";
                }
                sortAndRefreshRooms();
                return true;
            });
            popup.show();
        });
    }

    private void sortAndRefreshRooms() {
        Collections.sort(filteredRooms, (r1, r2) -> {
            switch (currentSortOrder) {
                case "capacity":
                    return Integer.compare(r2.getCapacity(), r1.getCapacity()); // Descending capacity
                case "location":
                    return r1.getLocation().compareToIgnoreCase(r2.getLocation());
                case "name":
                default:
                    return r1.getName().compareToIgnoreCase(r2.getName());
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
        spinnerItemsPerPage.setVisibility(View.VISIBLE); // Force visible
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
        totalPages = (int) Math.ceil((double) filteredRooms.size() / itemsPerPage);
        if (totalPages == 0)
            totalPages = 1;
        if (currentPage > totalPages)
            currentPage = totalPages;
        if (currentPage < 1)
            currentPage = 1;

        int startIndex = (currentPage - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, filteredRooms.size());

        List<Room> paginatedList = new ArrayList<>();
        if (startIndex < filteredRooms.size()) {
            paginatedList = filteredRooms.subList(startIndex, endIndex);
        }

        if (adapter == null) {
            String role = session.getRole();
            adapter = new RoomAdapter(paginatedList, role, new RoomAdapter.OnRoomActionListener() {
                @Override
                public void onViewDetails(Room room) {
                    showRoomDetailDialog(room);
                }

                @Override
                public void onBookRoom(Room room) {
                    Intent intent = new Intent(getContext(), BookingFormActivity.class);
                    intent.putExtra("ROOM_ID", room.getId());
                    intent.putExtra("ROOM_NAME", room.getName());
                    startActivity(intent);
                }

                @Override
                public void onEditRoom(Room room) {
                    showRoomFormDialog(room);
                }

                @Override
                public void onDeleteRoom(Room room) {
                    new AlertDialog.Builder(getContext())
                            .setTitle("Delete Room")
                            .setMessage("Are you sure you want to delete " + room.getName() + "?")
                            .setPositiveButton("Yes", (d, w) -> {
                                db.deleteRoom(room.getId());
                                loadRooms();
                                Toast.makeText(getContext(), "Room Deleted", Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("No", null)
                            .show();
                }
            });
            rvRooms.setAdapter(adapter);
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

    private String saveImageToInternalStorage(android.net.Uri sourceUri) {
        try {
            java.io.InputStream inputStream = getContext().getContentResolver().openInputStream(sourceUri);
            java.io.File internalStorageDir = getContext().getFilesDir();
            String fileName = "room_img_" + System.currentTimeMillis() + ".jpg";
            java.io.File destinationFile = new java.io.File(internalStorageDir, fileName);
            java.io.FileOutputStream fos = new java.io.FileOutputStream(destinationFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }

            fos.close();
            inputStream.close();
            return destinationFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return sourceUri.toString(); // Fallback
        }
    }

    private void showRoomFormDialog(@Nullable Room roomToEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_room_form, null);

        TextView tvTitle = view.findViewById(R.id.tvFormTitle);
        EditText etName = view.findViewById(R.id.etRoomName);
        EditText etBuilding = view.findViewById(R.id.etBuilding);
        EditText etFloor = view.findViewById(R.id.etFloor);
        EditText etCapacity = view.findViewById(R.id.etCapacity);
        EditText etSize = view.findViewById(R.id.etSize);
        EditText etFacilities = view.findViewById(R.id.etFacilities);
        EditText etDescription = view.findViewById(R.id.etDescription);
        LinearLayout layoutImageSelect = view.findViewById(R.id.layoutImageSelect);
        ivCurrentDialogImage = view.findViewById(R.id.ivRoomImagePreview);
        TextView btnCancel = view.findViewById(R.id.btnCancel);
        TextView btnSave = view.findViewById(R.id.btnSave);

        currentImageUriString = null; // Reset

        if (roomToEdit != null) {
            tvTitle.setText("Edit Room");
            btnSave.setText("UPDATE");
            etName.setText(roomToEdit.getName());
            etBuilding.setText(roomToEdit.getBuilding());
            etFloor.setText(roomToEdit.getFloor());
            etCapacity.setText(String.valueOf(roomToEdit.getCapacity()));
            etSize.setText(roomToEdit.getSize());
            etFacilities.setText(roomToEdit.getFacilities());
            etDescription.setText(roomToEdit.getDescription());

            if (roomToEdit.getImageUri() != null && !roomToEdit.getImageUri().isEmpty()) {
                currentImageUriString = roomToEdit.getImageUri();
                try {
                    ivCurrentDialogImage.setImageURI(android.net.Uri.parse(currentImageUriString));
                    ivCurrentDialogImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                } catch (Exception e) {
                    // ignore
                }
            }
        }

        layoutImageSelect.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        builder.setView(view);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow()
                    .setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.WHITE));
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String building = etBuilding.getText().toString().trim();
            String floor = etFloor.getText().toString().trim();
            String capacityStr = etCapacity.getText().toString().trim();
            String size = etSize.getText().toString().trim();
            String facilities = etFacilities.getText().toString().trim();
            String desc = etDescription.getText().toString().trim();

            if (name.isEmpty() || building.isEmpty() || floor.isEmpty() || capacityStr.isEmpty()) {
                Toast.makeText(getContext(), "Please fill in required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            int capacity = 0;
            try {
                capacity = Integer.parseInt(capacityStr);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Invalid capacity", Toast.LENGTH_SHORT).show();
                return;
            }

            if (roomToEdit == null) {
                // ADD
                Room newRoom = new Room(name, building, floor, capacity, size, facilities, desc, currentImageUriString);
                db.addRoom(newRoom);
                Toast.makeText(getContext(), "Room Added", Toast.LENGTH_SHORT).show();
            } else {
                // UPDATE
                roomToEdit.setName(name);
                roomToEdit.setBuilding(building);
                roomToEdit.setFloor(floor);
                roomToEdit.setCapacity(capacity);
                roomToEdit.setSize(size);
                roomToEdit.setFacilities(facilities);
                roomToEdit.setDescription(desc);
                roomToEdit.setImageUri(currentImageUriString);
                db.updateRoom(roomToEdit);
                Toast.makeText(getContext(), "Room Updated", Toast.LENGTH_SHORT).show();
            }
            loadRooms();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showRoomDetailDialog(Room room) {
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

        tvName.setText(room.getName());

        String locationText = room.getBuilding();
        if (room.getFloor() != null && !room.getFloor().isEmpty()) {
            locationText += ", Floor " + room.getFloor();
        }
        tvLocation.setText(locationText);

        tvCapacity.setText(room.getCapacity() + " People");
        tvSize.setText(room.getSize() != null && !room.getSize().isEmpty() ? room.getSize() : "-");
        tvFacilities
                .setText(room.getFacilities() != null && !room.getFacilities().isEmpty() ? room.getFacilities() : "-");
        tvDescription.setText(
                room.getDescription() != null && !room.getDescription().isEmpty() ? room.getDescription() : "-");

        if (room.getImageUri() != null && !room.getImageUri().isEmpty()) {
            try {
                ivImage.setImageURI(android.net.Uri.parse(room.getImageUri()));
                ivImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } catch (Exception e) {
                ivImage.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        } else {
            ivImage.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        builder.setView(view);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(
                    new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}
