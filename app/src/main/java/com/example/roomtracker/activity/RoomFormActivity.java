package com.example.roomtracker.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.roomtracker.R;
import com.example.roomtracker.database.DatabaseHelper;
import com.example.roomtracker.model.Room;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class RoomFormActivity extends AppCompatActivity {

    private EditText etName, etBuilding, etFloor, etCapacity, etSize, etFacilities, etDescription;
    private android.widget.Spinner spinnerStatus;
    private ImageView ivRoomImage, btnSelectImage;
    private Button btnSave;
    private DatabaseHelper db;
    private int roomId = -1;
    private Uri imageUri;

    // Native Image Picker
    private final ActivityResultLauncher<String> pickImage = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    // Copy to internal storage to ensure permanent access
                    Uri savedUri = saveImageToInternalStorage(uri);
                    if (savedUri != null) {
                        imageUri = savedUri;
                        ivRoomImage.setImageURI(imageUri);
                    } else {
                        Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_form);

        db = new DatabaseHelper(this);

        etName = findViewById(R.id.etRoomName);
        etBuilding = findViewById(R.id.etBuilding);
        etFloor = findViewById(R.id.etFloor);
        etCapacity = findViewById(R.id.etCapacity);
        etSize = findViewById(R.id.etSize);
        etFacilities = findViewById(R.id.etFacilities);
        etDescription = findViewById(R.id.etDescription);
        ivRoomImage = findViewById(R.id.ivRoomImage);
        etDescription = findViewById(R.id.etDescription);
        spinnerStatus = findViewById(R.id.spinnerStatus); // New Spinner
        ivRoomImage = findViewById(R.id.ivRoomImage);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnSave = findViewById(R.id.btnSaveRoom);

        setupStatusSpinner();

        btnSelectImage.setOnClickListener(v -> {
            pickImage.launch("image/*");
        });

        if (getIntent().hasExtra("ROOM_ID")) {
            roomId = getIntent().getIntExtra("ROOM_ID", -1);
            loadRoomData();
        }

        btnSave.setOnClickListener(v -> saveRoom());
    }

    // Helper to copy content URI to internal file
    private Uri saveImageToInternalStorage(Uri sourceUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(sourceUri);
            File localFile = new File(getFilesDir(), "room_" + System.currentTimeMillis() + ".jpg");
            OutputStream outputStream = new FileOutputStream(localFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();
            return Uri.fromFile(localFile);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void loadRoomData() {
        Room room = db.getRoom(roomId);
        if (room != null) {
            etName.setText(room.getName());
            etBuilding.setText(room.getBuilding());
            etFloor.setText(room.getFloor());
            etCapacity.setText(String.valueOf(room.getCapacity()));
            etSize.setText(room.getSize());
            etFacilities.setText(room.getFacilities());
            etDescription.setText(room.getDescription());

            if (room.getImageUri() != null) {
                imageUri = Uri.parse(room.getImageUri());
                ivRoomImage.setImageURI(imageUri);
            }

            if (room.getStatus() != null) {
                if ("INACTIVE".equals(room.getStatus())) {
                    spinnerStatus.setSelection(1);
                } else {
                    spinnerStatus.setSelection(0);
                }
            }
        }
    }

    private void setupStatusSpinner() {
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, new String[] { "Active", "Inactive" });
        spinnerStatus.setAdapter(adapter);
    }

    private void saveRoom() {
        String name = etName.getText().toString().trim();
        String building = etBuilding.getText().toString().trim();
        String floor = etFloor.getText().toString().trim();
        String capacityStr = etCapacity.getText().toString().trim();
        String size = etSize.getText().toString().trim();
        String facilities = etFacilities.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(building) || TextUtils.isEmpty(floor)
                || TextUtils.isEmpty(capacityStr)) {
            Toast.makeText(this, "Please fill required fields (Name, Building, Floor, Capacity)", Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        int capacity = Integer.parseInt(capacityStr);
        String uriString = (imageUri != null) ? imageUri.toString() : null;
        String status = spinnerStatus.getSelectedItemPosition() == 1 ? "INACTIVE" : "ACTIVE";

        Room room = new Room(roomId, name, building, floor, capacity, size, facilities, description, uriString, status);

        if (roomId == -1) {
            long id = db.addRoom(room);
            if (id > 0) {
                Toast.makeText(this, "Room Added Successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to Add Room", Toast.LENGTH_SHORT).show();
            }
        } else {
            int rows = db.updateRoom(room);
            if (rows > 0) {
                Toast.makeText(this, "Room Updated Successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to Update Room", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
