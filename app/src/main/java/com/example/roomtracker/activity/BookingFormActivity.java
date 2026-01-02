package com.example.roomtracker.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.roomtracker.R;
import com.example.roomtracker.database.DatabaseHelper;
import com.example.roomtracker.model.Booking;
import com.example.roomtracker.utils.SessionManager;

import java.util.Calendar;
import java.util.Locale;

public class BookingFormActivity extends AppCompatActivity {

    private TextView tvRoomName;
    private EditText etDate, etStartTime, etEndTime, etReason;
    private TextView btnSubmit, btnCancel;
    private DatabaseHelper db;
    private SessionManager session;
    private int roomId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_form);

        db = new DatabaseHelper(this);
        session = new SessionManager(this);

        roomId = getIntent().getIntExtra("ROOM_ID", -1);
        String roomName = getIntent().getStringExtra("ROOM_NAME");

        if (roomId == -1) {
            finish();
            return;
        }

        tvRoomName = findViewById(R.id.tvRoomNameDisplay);
        etDate = findViewById(R.id.etBookingDate);
        etStartTime = findViewById(R.id.etStartTime);
        etEndTime = findViewById(R.id.etEndTime);
        etReason = findViewById(R.id.etBookingReason);
        btnSubmit = findViewById(R.id.btnSubmitBooking);
        btnCancel = findViewById(R.id.btnCancelBooking);

        tvRoomName.setText(roomName);

        setupPickers();

        btnSubmit.setOnClickListener(v -> submitBooking());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void setupPickers() {
        Calendar calendar = Calendar.getInstance();

        etDate.setOnClickListener(v -> {
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                String date = String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, dayOfMonth);
                etDate.setText(date);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        etStartTime.setOnClickListener(v -> showTimePicker(etStartTime));
        etEndTime.setOnClickListener(v -> showTimePicker(etEndTime));
    }

    private void showTimePicker(EditText target) {
        Calendar calendar = Calendar.getInstance();
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
            target.setText(time);
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
    }

    private void submitBooking() {
        String date = etDate.getText().toString().trim();
        String start = etStartTime.getText().toString().trim();
        String end = etEndTime.getText().toString().trim();
        String reason = etReason.getText().toString().trim();

        if (TextUtils.isEmpty(date) || TextUtils.isEmpty(start) || TextUtils.isEmpty(end)
                || TextUtils.isEmpty(reason)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Basic validation: End time > Start time
        if (start.compareTo(end) >= 0) {
            Toast.makeText(this, "End time must be after start time", Toast.LENGTH_SHORT).show();
            return;
        }

        Booking booking = new Booking(roomId, session.getUserId(), date, start, end, "PENDING", reason);

        long result = db.addBooking(booking);
        if (result == -1) {
            Toast.makeText(this, "Time slot conflict! Please choose another time.", Toast.LENGTH_LONG).show();
        } else if (result == -2) {
            Toast.makeText(this, "Room is currently INACTIVE and cannot be booked.", Toast.LENGTH_LONG).show();
        } else if (result > 0) {
            Toast.makeText(this, "Booking Submitted Successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to submit booking", Toast.LENGTH_SHORT).show();
        }
    }
}
