package com.example.roomtracker.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.roomtracker.R;
import com.example.roomtracker.database.DatabaseHelper;
import com.example.roomtracker.model.User;
import com.example.roomtracker.utils.SessionManager;

public class ProfileFragment extends Fragment {

    private SessionManager session;
    private DatabaseHelper db;

    private TextView tvName, tvRole, tvEmail, tvFakultas, tvProdi, tvAngkatan;
    private LinearLayout layoutMahasiswaInfo;
    private Button btnLogout;
    private ImageView ivProfileImage;

    private User currentUser;
    private Uri newImageUri;
    private ImageView ivDialogProfileImage; // Ref for dialog image view

    private final ActivityResultLauncher<String> mGetContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    newImageUri = uri;
                    // Update the image view in the dialog if it's open, or main view
                    if (ivDialogProfileImage != null) {
                        ivDialogProfileImage.setImageURI(uri);
                    }
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        session = new SessionManager(getContext());
        db = new DatabaseHelper(getContext());

        // Initialize Views
        tvName = view.findViewById(R.id.tvName);
        tvRole = view.findViewById(R.id.tvRole);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvFakultas = view.findViewById(R.id.tvFakultas);
        tvProdi = view.findViewById(R.id.tvProdi);
        tvAngkatan = view.findViewById(R.id.tvAngkatan);
        layoutMahasiswaInfo = view.findViewById(R.id.layoutMahasiswaInfo);
        btnLogout = view.findViewById(R.id.btnLogout);
        ivProfileImage = view.findViewById(R.id.ivProfileImage);
        ImageView ivEditProfile = view.findViewById(R.id.ivEditProfile);

        // Add Edit Button functionality
        ivEditProfile.setOnClickListener(v -> showEditProfileDialog());

        loadUserData();

        btnLogout.setOnClickListener(v -> showLogoutConfirmation());

        return view;
    }

    private void loadUserData() {
        int userId = session.getUserId();
        currentUser = db.getUser(userId);

        if (currentUser != null) {
            tvName.setText(currentUser.getName());
            tvEmail.setText(currentUser.getEmail());
            tvRole.setText(currentUser.getRole());

            // Load Profile Image (Not KTM)
            String imageUri = currentUser.getProfileImage();
            loadProfileImage(ivProfileImage, imageUri);

            if ("MAHASISWA".equals(currentUser.getRole())) {
                layoutMahasiswaInfo.setVisibility(View.VISIBLE);
                tvFakultas.setText(currentUser.getFakultas() != null ? currentUser.getFakultas() : "-");
                tvProdi.setText(currentUser.getProdi() != null ? currentUser.getProdi() : "-");
                tvAngkatan.setText(currentUser.getAngkatan() != null ? currentUser.getAngkatan() : "-");
            } else {
                layoutMahasiswaInfo.setVisibility(View.GONE);
            }
        }
    }

    private void loadProfileImage(ImageView imageView, String imageUri) {
        if (imageUri != null && !imageUri.isEmpty() && !"-".equals(imageUri)) {
            try {
                Uri uri;
                if (imageUri.startsWith("content://") || imageUri.startsWith("file://")) {
                    uri = Uri.parse(imageUri);
                } else {
                    // Assume absolute path
                    uri = Uri.fromFile(new java.io.File(imageUri));
                }
                imageView.setPadding(0, 0, 0, 0); // Remove padding for photo
                imageView.setImageURI(uri);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } catch (Exception e) {
                imageView.setPadding(40, 40, 40, 40); // Restore padding for icon
                imageView.setImageResource(R.drawable.ic_nav_profile); // Default icon
                imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            }
        } else {
            imageView.setPadding(40, 40, 40, 40); // Restore padding for icon
            imageView.setImageResource(R.drawable.ic_nav_profile); // Default icon
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        }
    }

    private void showEditProfileDialog() {
        if (currentUser == null)
            return;
        newImageUri = null; // Reset

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_edit_profile, null);

        TextView btnSave = view.findViewById(R.id.btnSave);
        TextView btnCancel = view.findViewById(R.id.btnCancel);
        EditText etName = view.findViewById(R.id.etName);
        EditText etEmail = view.findViewById(R.id.etEmail);
        ivDialogProfileImage = view.findViewById(R.id.ivDialogProfileImage);
        ImageView ivCameraIcon = view.findViewById(R.id.ivCameraIcon);

        // Student Fields
        LinearLayout layoutStudentFields = view.findViewById(R.id.layoutStudentFields);
        EditText etFakultas = view.findViewById(R.id.etFakultas);
        EditText etProdi = view.findViewById(R.id.etProdi);
        EditText etAngkatan = view.findViewById(R.id.etAngkatan);

        // Pre-fill data
        etName.setText(currentUser.getName());
        etEmail.setText(currentUser.getEmail());
        loadProfileImage(ivDialogProfileImage, currentUser.getProfileImage());

        if ("MAHASISWA".equals(currentUser.getRole())) {
            layoutStudentFields.setVisibility(View.VISIBLE);
            etFakultas.setText(currentUser.getFakultas());
            etProdi.setText(currentUser.getProdi());
            etAngkatan.setText(currentUser.getAngkatan());
        } else {
            layoutStudentFields.setVisibility(View.GONE);
        }

        // Setup handlers
        View.OnClickListener imagePickerListener = v -> mGetContent.launch("image/*");
        ivDialogProfileImage.setOnClickListener(imagePickerListener);
        ivCameraIcon.setOnClickListener(imagePickerListener);

        builder.setView(view);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(0));
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            String newName = etName.getText().toString().trim();
            String newEmail = etEmail.getText().toString().trim();

            if (newName.isEmpty() || newEmail.isEmpty()) {
                Toast.makeText(getContext(), "Name and Email cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            currentUser.setName(newName);
            currentUser.setEmail(newEmail);

            // Student update logic
            if ("MAHASISWA".equals(currentUser.getRole())) {
                String fak = etFakultas.getText().toString().trim();
                String prodi = etProdi.getText().toString().trim();
                String angkatan = etAngkatan.getText().toString().trim();

                currentUser.setFakultas(fak);
                currentUser.setProdi(prodi);
                currentUser.setAngkatan(angkatan);
            }

            if (newImageUri != null) {
                String savedPath = copyUriToInternalStorage(newImageUri,
                        "profile_" + System.currentTimeMillis() + ".jpg");
                if (savedPath != null) {
                    currentUser.setProfileImage(savedPath); // Update Profile Image
                } else {
                    currentUser.setProfileImage(newImageUri.toString());
                }
            }

            if (db.updateUser(currentUser)) {
                Toast.makeText(getContext(), "Profile Updated", Toast.LENGTH_SHORT).show();
                loadUserData(); // Refresh UI
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), "Update Failed", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private String copyUriToInternalStorage(Uri uri, String filename) {
        try {
            java.io.InputStream inputStream = getContext().getContentResolver().openInputStream(uri);
            java.io.File updateDir = new java.io.File(getContext().getFilesDir(), "profile_images"); // New folder
            if (!updateDir.exists())
                updateDir.mkdir();
            java.io.File dest = new java.io.File(updateDir, filename);

            java.io.OutputStream outputStream = new java.io.FileOutputStream(dest);
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

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(getContext())
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).logout();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
}
