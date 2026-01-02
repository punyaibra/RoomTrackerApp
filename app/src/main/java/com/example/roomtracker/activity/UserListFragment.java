package com.example.roomtracker.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.roomtracker.R;
import com.example.roomtracker.adapter.UserAdapter;
import com.example.roomtracker.database.DatabaseHelper;
import com.example.roomtracker.model.User;
import com.example.roomtracker.utils.SessionManager;
import com.google.android.material.tabs.TabLayout;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.net.Uri;
import android.widget.Button;
import android.widget.ImageView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class UserListFragment extends Fragment {

    private RecyclerView rvUsers;
    private DatabaseHelper db;
    private UserAdapter adapter;
    private SessionManager session;
    private TabLayout tabLayoutRoles;
    private EditText etSearch;
    private com.google.android.material.floatingactionbutton.FloatingActionButton btnAddUser;
    private android.widget.ImageView ivFilterIcon;
    private TextView btnPrevPage, btnNextPage;
    private LinearLayout layoutPageNumbers;
    private android.widget.Spinner spinnerItemsPerPage;
    private static final int MAX_PAGE_BUTTONS = 5;

    private List<User> allUsers = new ArrayList<>();
    private List<User> filteredUsers = new ArrayList<>();
    private String currentRoleFilter = "ALL";
    private String currentSortOrder = "name"; // name, id, role

    // Pagination
    private int currentPage = 1;
    private int itemsPerPage = 10;

    private int totalPages = 1;

    // Image Picker
    private ActivityResultLauncher<String> pickImageLauncher;
    private Uri tempKtmUri;
    private ImageView tempImageViewForPreview;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                tempKtmUri = uri;
                if (tempImageViewForPreview != null) {
                    tempImageViewForPreview.setImageURI(uri);
                }
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_list, container, false);
        db = new DatabaseHelper(getContext());
        session = new SessionManager(getContext());

        rvUsers = view.findViewById(R.id.rvUsers);
        tabLayoutRoles = view.findViewById(R.id.tabLayoutRoles);
        etSearch = view.findViewById(R.id.etSearch);
        btnAddUser = view.findViewById(R.id.btnAddUser);
        ivFilterIcon = view.findViewById(R.id.ivFilterIcon);
        btnPrevPage = view.findViewById(R.id.btnPrevPage);
        btnNextPage = view.findViewById(R.id.btnNextPage);
        layoutPageNumbers = view.findViewById(R.id.layoutPageNumbers);
        spinnerItemsPerPage = view.findViewById(R.id.spinnerItemsPerPage);

        rvUsers.setLayoutManager(new LinearLayoutManager(getContext()));

        setupTabs();
        setupSearch();
        setupAddButton();
        setupFilterIcon();
        setupPagination();
        setupItemsPerPageSpinner();

        return view;
    }

    private void setupTabs() {
        tabLayoutRoles.addTab(tabLayoutRoles.newTab().setText("All"));
        tabLayoutRoles.addTab(tabLayoutRoles.newTab().setText("Admin"));
        tabLayoutRoles.addTab(tabLayoutRoles.newTab().setText("Staff"));
        tabLayoutRoles.addTab(tabLayoutRoles.newTab().setText("Mahasiswa"));

        tabLayoutRoles.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        currentRoleFilter = "ALL";
                        break;
                    case 1:
                        currentRoleFilter = "ADMIN";
                        break;
                    case 2:
                        currentRoleFilter = "STAFF";
                        break;
                    case 3:
                        currentRoleFilter = "MAHASISWA";
                        break;
                }
                filterUsers(etSearch.getText().toString());
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
                filterUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupAddButton() {
        btnAddUser.setOnClickListener(v -> showAddUserDialog());
    }

    private void showAddUserDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_user_form, null);
        EditText etName = dialogView.findViewById(R.id.etDialogName);
        EditText etEmail = dialogView.findViewById(R.id.etDialogEmail);
        EditText etPassword = dialogView.findViewById(R.id.etDialogPassword);
        android.widget.RadioGroup rgRole = dialogView.findViewById(R.id.rgDialogRole);

        new AlertDialog.Builder(getContext())
                .setTitle("Add New User")
                .setView(dialogView)
                .setPositiveButton("Create", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String email = etEmail.getText().toString().trim();
                    String password = etPassword.getText().toString().trim();

                    String role = "MAHASISWA";
                    int selectedId = rgRole.getCheckedRadioButtonId();
                    if (selectedId == R.id.rbAdmin)
                        role = "ADMIN";
                    else if (selectedId == R.id.rbStaff)
                        role = "STAFF";

                    if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                        Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    User newUser = new User(name, email, password, role, "-", "-", "-", "-");
                    newUser.setIsVerified(1);

                    long id = db.addUser(newUser);
                    if (id > 0) {
                        Toast.makeText(getContext(), "User Created", Toast.LENGTH_SHORT).show();
                        loadUsers();
                    } else {
                        Toast.makeText(getContext(), "Creation Failed (Email may exist)", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditUserDialog(User user) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_user_form, null);

        EditText etName = dialogView.findViewById(R.id.etDialogName);
        EditText etEmail = dialogView.findViewById(R.id.etDialogEmail);
        EditText etPassword = dialogView.findViewById(R.id.etDialogPassword);
        RadioGroup rgRole = dialogView.findViewById(R.id.rgDialogRole);

        // Student specific fields
        android.widget.LinearLayout layoutStudentFields = dialogView.findViewById(R.id.layoutStudentFields);
        EditText etFakultas = dialogView.findViewById(R.id.etDialogFakultas);
        EditText etProdi = dialogView.findViewById(R.id.etDialogProdi);
        EditText etAngkatan = dialogView.findViewById(R.id.etDialogAngkatan);

        // Image Picker UI
        ImageView ivKtm = dialogView.findViewById(R.id.ivKtmPreview);
        Button btnChangeKtm = dialogView.findViewById(R.id.btnChangeKtm);

        // Reset temp variables
        tempKtmUri = null;
        tempImageViewForPreview = ivKtm;

        // Pre-fill existing data
        etName.setText(user.getName());
        etEmail.setText(user.getEmail());
        etPassword.setHint("Leave empty to keep current password");

        // Pre-fill student data if available
        etFakultas.setText(user.getFakultas());
        etProdi.setText(user.getProdi());
        etAngkatan.setText(user.getAngkatan());

        // Load existing KTM image if available
        if (user.getKtm() != null && !user.getKtm().isEmpty() && !user.getKtm().equals("-")) {
            try {
                // Check if it's a file path or URI
                if (user.getKtm().startsWith("/")) {
                    ivKtm.setImageURI(Uri.fromFile(new File(user.getKtm())));
                } else {
                    ivKtm.setImageURI(Uri.parse(user.getKtm()));
                }
            } catch (Exception e) {
                ivKtm.setImageResource(android.R.drawable.ic_menu_camera); // Default if load fails
            }
        } else {
            ivKtm.setImageResource(android.R.drawable.ic_menu_camera);
        }

        // Setup Change Photo Button
        btnChangeKtm.setOnClickListener(v -> {
            pickImageLauncher.launch("image/*");
        });

        // Role change listener to toggle student fields
        rgRole.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbMahasiswa) {
                layoutStudentFields.setVisibility(View.VISIBLE);
            } else {
                layoutStudentFields.setVisibility(View.GONE);
            }
        });

        // Set initial role radio button
        if ("ADMIN".equals(user.getRole())) {
            rgRole.check(R.id.rbAdmin);
            layoutStudentFields.setVisibility(View.GONE);
        } else if ("MAHASISWA".equals(user.getRole())) {
            rgRole.check(R.id.rbMahasiswa);
            layoutStudentFields.setVisibility(View.VISIBLE);
        } else {
            rgRole.check(R.id.rbStaff);
            layoutStudentFields.setVisibility(View.GONE);
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Edit User: " + user.getName())
                .setView(dialogView)
                .setPositiveButton("Update", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String email = etEmail.getText().toString().trim();
                    String password = etPassword.getText().toString().trim();

                    int selectedId = rgRole.getCheckedRadioButtonId();
                    String role = "MAHASISWA";
                    if (selectedId == R.id.rbAdmin)
                        role = "ADMIN";
                    else if (selectedId == R.id.rbStaff)
                        role = "STAFF";

                    if (name.isEmpty() || email.isEmpty()) {
                        Toast.makeText(getContext(), "Name and Email cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Update user data
                    user.setName(name);
                    user.setEmail(email);
                    if (!password.isEmpty()) {
                        user.setPassword(password);
                    }
                    user.setRole(role);

                    // Update student specific fields
                    if ("MAHASISWA".equals(role)) {
                        user.setFakultas(etFakultas.getText().toString().trim());
                        user.setProdi(etProdi.getText().toString().trim());
                        user.setAngkatan(etAngkatan.getText().toString().trim());

                        // Handle KTM Image
                        if (tempKtmUri != null) {
                            String filename = "ktm_" + System.currentTimeMillis() + ".jpg";
                            String savedPath = copyUriToInternalStorage(tempKtmUri, filename);
                            if (savedPath != null) {
                                user.setKtm(savedPath);
                            } else {
                                Toast.makeText(getContext(), "Failed to save KTM image from URI",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                        // If tempKtmUri is null, we keep the old user.getKtm()

                    } else {
                        // Optional: Clear if not student
                        // user.setKtm("-");
                    }

                    boolean success = db.updateUser(user);
                    if (success) {
                        Toast.makeText(getContext(), "User Updated Successfully", Toast.LENGTH_SHORT).show();
                        loadUsers();
                    } else {
                        Toast.makeText(getContext(), "Update Failed", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private String copyUriToInternalStorage(Uri uri, String filename) {
        try {
            InputStream inputStream = getContext().getContentResolver().openInputStream(uri);
            File updateDir = new File(getContext().getFilesDir(), "ktm_images");
            if (!updateDir.exists())
                updateDir.mkdir();
            File dest = new File(updateDir, filename);

            OutputStream outputStream = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
            inputStream.close();
            return dest.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void setupFilterIcon() {
        ivFilterIcon.setOnClickListener(v -> {
            android.widget.PopupMenu popup = new android.widget.PopupMenu(getContext(), v);
            popup.getMenu().add("Sort by Name");
            popup.getMenu().add("Sort by ID");
            popup.getMenu().add("Sort by Role");
            popup.getMenu().add("Sort by Status");

            popup.setOnMenuItemClickListener(item -> {
                String title = item.getTitle().toString();
                if ("Sort by Name".equals(title)) {
                    currentSortOrder = "name";
                } else if ("Sort by ID".equals(title)) {
                    currentSortOrder = "id";
                } else if ("Sort by Role".equals(title)) {
                    currentSortOrder = "role";
                } else if ("Sort by Status".equals(title)) {
                    currentSortOrder = "status";
                }

                sortAndRefreshUsers();
                Toast.makeText(getContext(), "Sorted by: " + title, Toast.LENGTH_SHORT).show();
                return true;
            });
            popup.show();
        });
    }

    private void sortAndRefreshUsers() {
        // Sort filteredUsers based on currentSortOrder
        java.util.Collections.sort(filteredUsers, (u1, u2) -> {
            switch (currentSortOrder) {
                case "id":
                    return Integer.compare(u1.getId(), u2.getId());
                case "role":
                    return u1.getRole().compareTo(u2.getRole());
                case "status":
                    return Integer.compare(u1.getIsVerified(), u2.getIsVerified());
                case "name":
                default:
                    return u1.getName().compareToIgnoreCase(u2.getName());
            }
        });

        if (adapter != null) {
            adapter.updateList(filteredUsers);
        }
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

    private void updatePaginatedList() {
        // Calculate total pages
        totalPages = (int) Math.ceil((double) filteredUsers.size() / itemsPerPage);
        if (totalPages == 0)
            totalPages = 1;

        // Ensure current page is valid
        if (currentPage > totalPages)
            currentPage = totalPages;
        if (currentPage < 1)
            currentPage = 1;

        // Calculate start and end index
        int startIndex = (currentPage - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, filteredUsers.size());

        // Get paginated sublist
        List<User> paginatedList = new ArrayList<>();
        if (startIndex < filteredUsers.size()) {
            paginatedList = filteredUsers.subList(startIndex, endIndex);
        }

        // Update adapter
        if (adapter != null) {
            adapter.updateList(paginatedList);
        }

        // Update pagination UI
        createPageNumberButtons();
        btnPrevPage.setEnabled(currentPage > 1);
        btnNextPage.setEnabled(currentPage < totalPages);

        // Visual feedback for disabled buttons
        btnPrevPage.setAlpha(currentPage > 1 ? 1.0f : 0.4f);
        btnNextPage.setAlpha(currentPage < totalPages ? 1.0f : 0.4f);
    }

    private void createPageNumberButtons() {
        layoutPageNumbers.removeAllViews();

        // Calculate which pages to show
        int startPage = Math.max(1, currentPage - 2);
        int endPage = Math.min(totalPages, startPage + MAX_PAGE_BUTTONS - 1);

        // Adjust start if we're near the end
        if (endPage - startPage < MAX_PAGE_BUTTONS - 1) {
            startPage = Math.max(1, endPage - MAX_PAGE_BUTTONS + 1);
        }

        // Add page number buttons
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
            pageButton.setPadding(0, 0, 0, 0);

            if (pageNum == currentPage) {
                // Current page style
                pageButton.setBackgroundResource(R.drawable.bg_search_bar);
                pageButton.setTextColor(android.graphics.Color.parseColor("#1A73E8"));
                pageButton.setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                // Other pages style
                pageButton.setBackground(null);
                pageButton.setTextColor(android.graphics.Color.parseColor("#757575"));
            }

            pageButton.setOnClickListener(v -> {
                currentPage = pageNum;
                updatePaginatedList();
            });

            layoutPageNumbers.addView(pageButton);
        }

        // Add "..." if there are more pages
        if (endPage < totalPages) {
            TextView dots = new TextView(getContext());
            dots.setText("...");
            dots.setTextSize(11);
            dots.setTextColor(android.graphics.Color.parseColor("#757575"));
            dots.setPadding(6, 0, 6, 0);
            layoutPageNumbers.addView(dots);

            // Add last page
            TextView lastPage = new TextView(getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    (int) (28 * getResources().getDisplayMetrics().density),
                    (int) (28 * getResources().getDisplayMetrics().density));
            params.setMargins(3, 0, 3, 0);
            lastPage.setLayoutParams(params);
            lastPage.setText(String.valueOf(totalPages));
            lastPage.setTextSize(11);
            lastPage.setGravity(android.view.Gravity.CENTER);
            lastPage.setTextColor(android.graphics.Color.parseColor("#757575"));
            lastPage.setClickable(true);
            lastPage.setFocusable(true);
            lastPage.setPadding(0, 0, 0, 0);
            lastPage.setOnClickListener(v -> {
                currentPage = totalPages;
                updatePaginatedList();
            });
            layoutPageNumbers.addView(lastPage);
        }
    }

    private void setupItemsPerPageSpinner() {
        String[] options = { "10 / page", "20 / page", "50 / page", "100 / page" };
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(
                getContext(), R.layout.spinner_item_pagination, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerItemsPerPage.setAdapter(adapter);

        spinnerItemsPerPage.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position,
                    long id) {
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
                    case 3:
                        itemsPerPage = 100;
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

    private void showUserDetailDialog(User user) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_user_detail, null);

        // Bind data to views
        TextView tvName = dialogView.findViewById(R.id.tvDetailName);
        TextView tvOrganization = dialogView.findViewById(R.id.tvDetailOrganization);
        TextView tvRole = dialogView.findViewById(R.id.tvDetailRole);
        TextView tvEmail = dialogView.findViewById(R.id.tvDetailEmail);

        // Student-specific fields
        LinearLayout layoutKtm = dialogView.findViewById(R.id.layoutKtmSection);
        LinearLayout layoutFakultas = dialogView.findViewById(R.id.layoutFakultasSection);
        LinearLayout layoutProdi = dialogView.findViewById(R.id.layoutProdiSection);
        LinearLayout layoutAngkatan = dialogView.findViewById(R.id.layoutAngkatanSection);
        LinearLayout layoutStatus = dialogView.findViewById(R.id.layoutStatusSection);

        TextView tvFakultas = dialogView.findViewById(R.id.tvDetailFakultas);
        TextView tvProdi = dialogView.findViewById(R.id.tvDetailProdi);
        TextView tvAngkatan = dialogView.findViewById(R.id.tvDetailAngkatan);
        TextView tvStatus = dialogView.findViewById(R.id.tvDetailStatus);

        // Set basic info
        tvName.setText(user.getName());
        tvOrganization.setText("Room Tracker System");
        tvRole.setText(user.getRole());
        tvEmail.setText(user.getEmail());

        // Set role color
        if ("ADMIN".equals(user.getRole())) {
            tvRole.setTextColor(android.graphics.Color.parseColor("#F44336")); // Red
        } else if ("STAFF".equals(user.getRole())) {
            tvRole.setTextColor(android.graphics.Color.parseColor("#2196F3")); // Blue
        } else {
            tvRole.setTextColor(android.graphics.Color.parseColor("#4CAF50")); // Green
        }

        // Show student-specific fields only for Mahasiswa
        if ("MAHASISWA".equals(user.getRole())) {
            layoutKtm.setVisibility(View.VISIBLE);
            layoutFakultas.setVisibility(View.VISIBLE);
            layoutProdi.setVisibility(View.VISIBLE);
            layoutAngkatan.setVisibility(View.VISIBLE);
            layoutStatus.setVisibility(View.VISIBLE);

            // Load KTM Image from URI
            android.widget.ImageView ivKtm = dialogView.findViewById(R.id.ivDetailKtm);

            android.util.Log.d("UserDetail", "Loading KTM for: " + user.getName());
            android.util.Log.d("UserDetail", "KTM URI: " + user.getKtm());

            try {
                if (user.getKtm() != null && !user.getKtm().isEmpty() && !"-".equals(user.getKtm())) {
                    android.net.Uri ktmUri = android.net.Uri.parse(user.getKtm());

                    // Use ContentResolver to load bitmap
                    try {
                        java.io.InputStream inputStream = getContext().getContentResolver().openInputStream(ktmUri);
                        android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(inputStream);
                        if (bitmap != null) {
                            ivKtm.setImageBitmap(bitmap);
                            android.util.Log.d("UserDetail", "✓ Image loaded successfully");
                        } else {
                            android.util.Log.e("UserDetail", "✗ Bitmap null after decode");
                            ivKtm.setImageResource(android.R.drawable.ic_menu_camera);
                        }
                        if (inputStream != null)
                            inputStream.close();
                    } catch (Exception e) {
                        android.util.Log.e("UserDetail", "✗ Error: " + e.getMessage());
                        // If ContentResolver fails, try direct setImageURI
                        ivKtm.setImageURI(ktmUri);
                    }
                } else {
                    android.util.Log.d("UserDetail", "No KTM URI - showing placeholder");
                    ivKtm.setImageResource(android.R.drawable.ic_menu_camera);
                }
            } catch (Exception e) {
                android.util.Log.e("UserDetail", "Fatal error: " + e.getMessage());
                ivKtm.setImageResource(android.R.drawable.ic_menu_camera);
            }
            tvFakultas.setText(user.getFakultas());
            tvProdi.setText(user.getProdi());
            tvAngkatan.setText(user.getAngkatan());

            // Set status with color
            if (user.getIsVerified() == 1) {
                tvStatus.setText("Verified");
                tvStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50"));
            } else if (user.getIsVerified() == 0) {
                tvStatus.setText("Pending Verification");
                tvStatus.setTextColor(android.graphics.Color.parseColor("#FFC107"));
            } else {
                tvStatus.setText("Rejected");
                tvStatus.setTextColor(android.graphics.Color.parseColor("#F44336"));
            }
        } else {
            layoutKtm.setVisibility(View.GONE);
            layoutFakultas.setVisibility(View.GONE);
            layoutProdi.setVisibility(View.GONE);
            layoutAngkatan.setVisibility(View.GONE);
            layoutStatus.setVisibility(View.GONE);
        }

        // Create and show dialog
        new AlertDialog.Builder(getContext()).setView(dialogView).setPositiveButton("Close", null).create().show();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUsers();
    }

    private void loadUsers() {
        String myRole = session.getRole();

        if ("ADMIN".equals(myRole)) {
            // Admin sees everyone
            allUsers = db.getAllUsers();
            tabLayoutRoles.setVisibility(View.VISIBLE);
            btnAddUser.setVisibility(View.VISIBLE);
        } else if ("STAFF".equals(myRole)) {
            // Staff only sees MAHASISWA
            List<User> tempUsers = db.getAllUsers();
            allUsers = new ArrayList<>();
            for (User user : tempUsers) {
                if ("MAHASISWA".equals(user.getRole())) {
                    allUsers.add(user);
                }
            }
            // Hide filters and Add button for STAFF as requested
            tabLayoutRoles.setVisibility(View.GONE);
            btnAddUser.setVisibility(View.GONE);
        } else {
            // Profile Mode: Only show self (Regular User)
            allUsers = new ArrayList<>();
            User me = db.getUser(session.getUserId());
            if (me != null)
                allUsers.add(me);
            tabLayoutRoles.setVisibility(View.GONE);
            btnAddUser.setVisibility(View.GONE);
        }

        // Apply current filters
        filterUsers(etSearch.getText().toString());
    }

    private void filterUsers(String query) {
        filteredUsers.clear();
        String lowerQuery = query.toLowerCase().trim();

        for (User user : allUsers) {
            boolean roleMatch = "ALL".equals(currentRoleFilter) || user.getRole().equalsIgnoreCase(currentRoleFilter);
            boolean nameMatch = user.getName().toLowerCase().contains(lowerQuery) ||
                    String.valueOf(user.getId()).contains(lowerQuery);

            if (roleMatch && nameMatch) {
                filteredUsers.add(user);
            }
        }

        // Apply current sort order
        java.util.Collections.sort(filteredUsers, (u1, u2) -> {
            switch (currentSortOrder) {
                case "id":
                    return Integer.compare(u1.getId(), u2.getId());
                case "role":
                    return u1.getRole().compareTo(u2.getRole());
                case "status":
                    return Integer.compare(u1.getIsVerified(), u2.getIsVerified());
                case "name":
                default:
                    return u1.getName().compareToIgnoreCase(u2.getName());
            }
        });

        if (adapter == null) {
            adapter = new UserAdapter(new ArrayList<>(), session.getRole(), new UserAdapter.OnUserActionListener() {
                @Override
                public void onEditProfile(User user) {
                    if ("ADMIN".equals(session.getRole()) || session.getUserId() == user.getId()) {
                        showEditUserDialog(user);
                    } else {
                        Toast.makeText(getContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onChangeRole(User user) {
                    if ("ADMIN".equals(session.getRole())) {
                        showRoleChangeDialog(user);
                    } else {
                        Toast.makeText(getContext(), "Only Admin can change roles", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onDelete(User user) {
                    if ("ADMIN".equals(session.getRole())) {
                        new AlertDialog.Builder(getContext())
                                .setTitle("Delete User")
                                .setMessage("Are you sure you want to delete " + user.getName() + "?")
                                .setPositiveButton("Yes", (dialog, which) -> {
                                    db.deleteUser(user.getId());
                                    loadUsers();
                                    Toast.makeText(getContext(), "User Deleted", Toast.LENGTH_SHORT).show();
                                })
                                .setNegativeButton("No", null)
                                .show();
                    } else {
                        Toast.makeText(getContext(), "Only Admin can delete users", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onVerify(User user) {
                    if ("ADMIN".equals(session.getRole())) {
                        showVerifyDialog(user);
                    }
                }

                @Override
                public void onRowClick(User user) {
                    showUserDetailDialog(user);
                }
            });
            rvUsers.setAdapter(adapter);
        }

        // Reset to page 1 when filter changes
        currentPage = 1;
        updatePaginatedList();
    }

    private void showRoleChangeDialog(User user) {
        String[] roles = { "ADMIN", "STAFF", "MAHASISWA" };
        new AlertDialog.Builder(getContext())
                .setTitle("Change Role for " + user.getName())
                .setItems(roles, (dialog, which) -> {
                    String newRole = roles[which];
                    db.updateUserRole(user.getId(), newRole);
                    loadUsers();
                    Toast.makeText(getContext(), "Role Updated to " + newRole, Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void showVerifyDialog(User user) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_verify_student, null);

        TextView tvName = dialogView.findViewById(R.id.tvVerifyStudentName);
        TextView tvInfo = dialogView.findViewById(R.id.tvVerifyStudentInfo);
        android.widget.ImageView ivKtm = dialogView.findViewById(R.id.ivVerifyKtm);

        // Set student info
        tvName.setText(user.getName());
        tvInfo.setText(user.getFakultas() + " - " + user.getProdi() + " - " + user.getAngkatan());

        // Load KTM Image
        android.util.Log.d("VerifyDialog", "Loading KTM for verification: " + user.getKtm());
        try {
            if (user.getKtm() != null && !user.getKtm().isEmpty() && !"-".equals(user.getKtm())) {
                if (user.getKtm().startsWith("/")) {
                    // It's a file path
                    File imgFile = new File(user.getKtm());
                    if (imgFile.exists()) {
                        ivKtm.setImageURI(Uri.fromFile(imgFile));
                    } else {
                        // File not found, show placeholder
                        ivKtm.setImageResource(android.R.drawable.ic_menu_camera);
                    }
                } else {
                    // It's a URI (likely older data or failure to copy)
                    Uri ktmUri = Uri.parse(user.getKtm());
                    try {
                        InputStream inputStream = getContext().getContentResolver().openInputStream(ktmUri);
                        android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(inputStream);
                        if (bitmap != null) {
                            ivKtm.setImageBitmap(bitmap);
                        } else {
                            ivKtm.setImageResource(android.R.drawable.ic_menu_camera);
                        }
                        if (inputStream != null)
                            inputStream.close();
                    } catch (Exception e) {
                        ivKtm.setImageResource(android.R.drawable.ic_menu_camera);
                    }
                }
            } else {
                ivKtm.setImageResource(android.R.drawable.ic_menu_camera);
            }
        } catch (Exception e) {
            ivKtm.setImageResource(android.R.drawable.ic_menu_camera);
            android.util.Log.e("VerifyDialog", "Error loading KTM: " + e.getMessage());
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Verify Student")
                .setView(dialogView)
                .setPositiveButton("Approve", (dialog, which) -> {
                    db.updateUserVerification(user.getId(), 1);
                    loadUsers();
                    Toast.makeText(getContext(), "User Approved", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Reject", (dialog, which) -> {
                    db.updateUserVerification(user.getId(), 2);
                    loadUsers();
                    Toast.makeText(getContext(), "User Rejected", Toast.LENGTH_SHORT).show();
                })
                .setNeutralButton("Cancel", null)
                .show();
    }
}
