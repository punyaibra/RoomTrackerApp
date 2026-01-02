package com.example.roomtracker.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.roomtracker.model.Booking;
import com.example.roomtracker.model.Room;
import com.example.roomtracker.model.User;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "RoomTracker.db";
    private static final int DATABASE_VERSION = 7; // Incremented

    // Table Names
    private static final String TABLE_USERS = "users";
    private static final String TABLE_ROOMS = "rooms";
    private static final String TABLE_BOOKINGS = "bookings";

    // Common Column Names
    private static final String KEY_ID = "id";

    // USERS Table Columns
    private static final String KEY_USER_NAME = "name";
    private static final String KEY_USER_EMAIL = "email";
    private static final String KEY_USER_PASSWORD = "password";
    private static final String KEY_USER_ROLE = "role";
    private static final String KEY_USER_KTM = "ktm"; // Now stores Image URI
    private static final String KEY_USER_PROFILE_IMAGE = "profile_image"; // New Column for Profile Image
    private static final String KEY_USER_FAKULTAS = "fakultas";
    private static final String KEY_USER_PRODI = "prodi";
    private static final String KEY_USER_ANGKATAN = "angkatan";
    private static final String KEY_USER_IS_VERIFIED = "is_verified";

    // ROOMS Table Columns
    private static final String KEY_ROOM_NAME = "name";
    private static final String KEY_ROOM_BUILDING = "building";
    private static final String KEY_ROOM_FLOOR = "floor";
    private static final String KEY_ROOM_CAPACITY = "capacity";
    private static final String KEY_ROOM_SIZE = "size";
    private static final String KEY_ROOM_FACILITIES = "facilities";
    private static final String KEY_ROOM_DESCRIPTION = "description";
    private static final String KEY_ROOM_IMAGE_URI = "image_uri";
    private static final String KEY_ROOM_STATUS = "status"; // New Column

    // BOOKINGS Table Columns
    private static final String KEY_BOOKING_ROOM_ID = "room_id";
    private static final String KEY_BOOKING_USER_ID = "user_id";
    private static final String KEY_BOOKING_DATE = "date";
    private static final String KEY_BOOKING_START = "start_time";
    private static final String KEY_BOOKING_END = "end_time";
    private static final String KEY_BOOKING_STATUS = "status";
    private static final String KEY_BOOKING_REASON = "reason"; // New Column

    // Table Create Statements
    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_USER_NAME + " TEXT,"
            + KEY_USER_EMAIL + " TEXT UNIQUE,"
            + KEY_USER_PASSWORD + " TEXT,"
            + KEY_USER_ROLE + " TEXT,"
            + KEY_USER_KTM + " TEXT,"
            + KEY_USER_PROFILE_IMAGE + " TEXT,"
            + KEY_USER_FAKULTAS + " TEXT,"
            + KEY_USER_PRODI + " TEXT,"
            + KEY_USER_ANGKATAN + " TEXT,"
            + KEY_USER_IS_VERIFIED + " INTEGER" + ")";

    private static final String CREATE_TABLE_ROOMS = "CREATE TABLE " + TABLE_ROOMS + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_ROOM_NAME + " TEXT,"
            + KEY_ROOM_BUILDING + " TEXT,"
            + KEY_ROOM_FLOOR + " TEXT,"
            + KEY_ROOM_CAPACITY + " INTEGER,"
            + KEY_ROOM_SIZE + " TEXT,"
            + KEY_ROOM_FACILITIES + " TEXT,"
            + KEY_ROOM_DESCRIPTION + " TEXT,"
            + KEY_ROOM_IMAGE_URI + " TEXT,"
            + KEY_ROOM_STATUS + " TEXT DEFAULT 'ACTIVE'" + ")";

    private static final String CREATE_TABLE_BOOKINGS = "CREATE TABLE " + TABLE_BOOKINGS + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_BOOKING_ROOM_ID + " INTEGER,"
            + KEY_BOOKING_USER_ID + " INTEGER,"
            + KEY_BOOKING_DATE + " TEXT,"
            + KEY_BOOKING_START + " TEXT,"
            + KEY_BOOKING_END + " TEXT,"
            + KEY_BOOKING_STATUS + " TEXT,"
            + KEY_BOOKING_REASON + " TEXT,"
            + "FOREIGN KEY(" + KEY_BOOKING_ROOM_ID + ") REFERENCES " + TABLE_ROOMS + "(" + KEY_ID + "),"
            + "FOREIGN KEY(" + KEY_BOOKING_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + KEY_ID + ")" + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_ROOMS);
        db.execSQL(CREATE_TABLE_BOOKINGS);

        // Insert Default Admin
        ContentValues values = new ContentValues();
        values.put(KEY_USER_NAME, "Admin System");
        values.put(KEY_USER_EMAIL, "admin@campus.com");
        values.put(KEY_USER_PASSWORD, "admin123");
        values.put(KEY_USER_ROLE, "ADMIN");
        values.put(KEY_USER_KTM, "-");
        values.put(KEY_USER_PROFILE_IMAGE, "-");
        values.put(KEY_USER_FAKULTAS, "-");
        values.put(KEY_USER_PRODI, "-");
        values.put(KEY_USER_ANGKATAN, "-");
        values.put(KEY_USER_IS_VERIFIED, 1);
        db.insert(TABLE_USERS, null, values);

        // Insert Default Staff
        values.clear();
        values.put(KEY_USER_NAME, "Petugas Lab");
        values.put(KEY_USER_EMAIL, "staff@campus.com");
        values.put(KEY_USER_PASSWORD, "staff123");
        values.put(KEY_USER_ROLE, "STAFF");
        values.put(KEY_USER_KTM, "-");
        values.put(KEY_USER_PROFILE_IMAGE, "-");
        values.put(KEY_USER_FAKULTAS, "-");
        values.put(KEY_USER_PRODI, "-");
        values.put(KEY_USER_ANGKATAN, "-");
        values.put(KEY_USER_IS_VERIFIED, 1);
        db.insert(TABLE_USERS, null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ROOMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKINGS);
        onCreate(db);
    }

    // --- USER CRUD ---
    public long addUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_USER_NAME, user.getName());
        values.put(KEY_USER_EMAIL, user.getEmail());
        values.put(KEY_USER_PASSWORD, user.getPassword());
        values.put(KEY_USER_ROLE, user.getRole());
        values.put(KEY_USER_KTM, user.getKtm());
        values.put(KEY_USER_PROFILE_IMAGE, user.getProfileImage());
        values.put(KEY_USER_FAKULTAS, user.getFakultas());
        values.put(KEY_USER_PRODI, user.getProdi());
        values.put(KEY_USER_ANGKATAN, user.getAngkatan());
        values.put(KEY_USER_IS_VERIFIED, user.getIsVerified());
        return db.insert(TABLE_USERS, null, values);
    }

    public boolean isEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[] { KEY_ID }, KEY_USER_EMAIL + "=?",
                new String[] { email }, null, null, null);
        boolean exists = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) {
            cursor.close();
        }
        return exists;
    }

    public User checkLogin(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null, KEY_USER_EMAIL + "=? AND " + KEY_USER_PASSWORD + "=?",
                new String[] { email, password }, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            User user = new User(
                    cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_EMAIL)),
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_PASSWORD)),
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_ROLE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_KTM)),
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_PROFILE_IMAGE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_FAKULTAS)),
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_PRODI)),
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_ANGKATAN)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(KEY_USER_IS_VERIFIED)));
            cursor.close();
            return user;
        }
        return null;
    }

    public User getUser(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            User user = new User(
                    cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_EMAIL)),
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_PASSWORD)),
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_ROLE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_KTM)),
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_PROFILE_IMAGE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_FAKULTAS)),
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_PRODI)),
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_ANGKATAN)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(KEY_USER_IS_VERIFIED)));
            cursor.close();
            return user;
        }
        return null;
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_USERS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                User user = new User(
                        cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_EMAIL)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_PASSWORD)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_ROLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_KTM)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_PROFILE_IMAGE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_FAKULTAS)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_PRODI)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_ANGKATAN)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(KEY_USER_IS_VERIFIED)));
                users.add(user);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return users;
    }

    public void updateUserRole(int userId, String newRole) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_USER_ROLE, newRole);
        db.update(TABLE_USERS, values, KEY_ID + " = ?", new String[] { String.valueOf(userId) });
    }

    public void updateUserVerification(int userId, int status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_USER_IS_VERIFIED, status);
        db.update(TABLE_USERS, values, KEY_ID + " = ?", new String[] { String.valueOf(userId) });
    }

    public boolean updateUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_USER_NAME, user.getName());
        values.put(KEY_USER_EMAIL, user.getEmail());
        values.put(KEY_USER_PASSWORD, user.getPassword());
        values.put(KEY_USER_ROLE, user.getRole());
        values.put(KEY_USER_KTM, user.getKtm());
        values.put(KEY_USER_PROFILE_IMAGE, user.getProfileImage());
        values.put(KEY_USER_FAKULTAS, user.getFakultas());
        values.put(KEY_USER_PRODI, user.getProdi());
        values.put(KEY_USER_ANGKATAN, user.getAngkatan());
        values.put(KEY_USER_IS_VERIFIED, user.getIsVerified());

        int rowsAffected = db.update(TABLE_USERS, values, KEY_ID + " = ?",
                new String[] { String.valueOf(user.getId()) });
        return rowsAffected > 0;
    }

    public void deleteUser(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_USERS, KEY_ID + " = ?", new String[] { String.valueOf(userId) });
    }

    // --- ROOM CRUD ---
    public long addRoom(Room room) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ROOM_NAME, room.getName());
        values.put(KEY_ROOM_BUILDING, room.getBuilding());
        values.put(KEY_ROOM_FLOOR, room.getFloor());
        values.put(KEY_ROOM_CAPACITY, room.getCapacity());
        values.put(KEY_ROOM_SIZE, room.getSize());
        values.put(KEY_ROOM_FACILITIES, room.getFacilities());
        values.put(KEY_ROOM_DESCRIPTION, room.getDescription());
        values.put(KEY_ROOM_IMAGE_URI, room.getImageUri());
        values.put(KEY_ROOM_STATUS, room.getStatus());
        return db.insert(TABLE_ROOMS, null, values);
    }

    public Room getRoom(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_ROOMS, null, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            Room room = new Room(
                    cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_ROOM_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_ROOM_BUILDING)),
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_ROOM_FLOOR)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ROOM_CAPACITY)),
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_ROOM_SIZE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_ROOM_FACILITIES)),
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_ROOM_DESCRIPTION)),
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_ROOM_IMAGE_URI)),
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_ROOM_STATUS)));
            cursor.close();
            return room;
        }
        return null;
    }

    public List<Room> getAllRooms() {
        List<Room> rooms = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_ROOMS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                Room room = new Room(
                        cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_ROOM_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_ROOM_BUILDING)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_ROOM_FLOOR)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ROOM_CAPACITY)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_ROOM_SIZE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_ROOM_FACILITIES)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_ROOM_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_ROOM_IMAGE_URI)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_ROOM_STATUS)));
                rooms.add(room);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return rooms;
    }

    public int updateRoom(Room room) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ROOM_NAME, room.getName());
        values.put(KEY_ROOM_BUILDING, room.getBuilding());
        values.put(KEY_ROOM_FLOOR, room.getFloor());
        values.put(KEY_ROOM_CAPACITY, room.getCapacity());
        values.put(KEY_ROOM_SIZE, room.getSize());
        values.put(KEY_ROOM_FACILITIES, room.getFacilities());
        values.put(KEY_ROOM_DESCRIPTION, room.getDescription());
        values.put(KEY_ROOM_IMAGE_URI, room.getImageUri());
        values.put(KEY_ROOM_STATUS, room.getStatus());
        return db.update(TABLE_ROOMS, values, KEY_ID + " = ?", new String[] { String.valueOf(room.getId()) });
    }

    public void deleteRoom(int roomId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ROOMS, KEY_ID + " = ?", new String[] { String.valueOf(roomId) });
    }

    // --- BOOKING CRUD ---
    public boolean isRoomCurrentlyOccupied(int roomId) {
        // Check if there is an ACCEPTED booking for NOW
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd",
                java.util.Locale.getDefault());
        java.text.SimpleDateFormat timeFormat = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
        java.util.Date now = new java.util.Date();
        String currentDate = dateFormat.format(now);
        String currentTime = timeFormat.format(now);

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_BOOKINGS + " WHERE " +
                KEY_BOOKING_ROOM_ID + " = ? AND " +
                KEY_BOOKING_DATE + " = ? AND " +
                KEY_BOOKING_STATUS + " = 'APPROVED' AND " +
                KEY_BOOKING_START + " <= ? AND " +
                KEY_BOOKING_END + " >= ?";

        Cursor cursor = db.rawQuery(query,
                new String[] { String.valueOf(roomId), currentDate, currentTime, currentTime });
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public boolean isTimeSlotAvailable(int roomId, String date, String startTime, String endTime) {
        SQLiteDatabase db = this.getReadableDatabase();
        // Simple overlap check: (StartA <= EndB) and (EndA >= StartB)
        // Note: String comparison for time works if format is HH:MM consistently
        String query = "SELECT * FROM " + TABLE_BOOKINGS + " WHERE " +
                KEY_BOOKING_ROOM_ID + " = ? AND " +
                KEY_BOOKING_DATE + " = ? AND " +
                KEY_BOOKING_STATUS + " = 'APPROVED' AND " +
                KEY_BOOKING_START + " < ? AND " +
                KEY_BOOKING_END + " > ?";

        Cursor cursor = db.rawQuery(query, new String[] { String.valueOf(roomId), date, endTime, startTime });
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return !exists;
    }

    public boolean isRoomActive(int roomId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_ROOMS, new String[] { KEY_ROOM_STATUS },
                KEY_ID + " = ?", new String[] { String.valueOf(roomId) },
                null, null, null);
        boolean active = true;
        if (cursor != null && cursor.moveToFirst()) {
            String status = cursor.getString(cursor.getColumnIndexOrThrow(KEY_ROOM_STATUS));
            active = !"INACTIVE".equals(status);
            cursor.close();
        }
        return active;
    }

    public long addBooking(Booking booking) {
        if (!isRoomActive(booking.getRoomId())) {
            return -2; // Room Closed/Inactive
        }
        if (!isTimeSlotAvailable(booking.getRoomId(), booking.getDate(), booking.getStartTime(),
                booking.getEndTime())) {
            return -1; // Conflict
        }
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_BOOKING_ROOM_ID, booking.getRoomId());
        values.put(KEY_BOOKING_USER_ID, booking.getUserId());
        values.put(KEY_BOOKING_DATE, booking.getDate());
        values.put(KEY_BOOKING_START, booking.getStartTime());
        values.put(KEY_BOOKING_END, booking.getEndTime());
        values.put(KEY_BOOKING_STATUS, booking.getStatus());
        values.put(KEY_BOOKING_REASON, booking.getReason());
        return db.insert(TABLE_BOOKINGS, null, values);
    }

    public List<Booking> getAllBookings() {
        List<Booking> bookings = new ArrayList<>();
        // Join with Rooms and Users to get names
        // Join with Rooms and Users to get names
        String query = "SELECT b.*, r." + KEY_ROOM_NAME + " AS room_name, r." + KEY_ROOM_IMAGE_URI
                + " AS room_image, u." + KEY_USER_NAME + " AS user_name " +
                " FROM " + TABLE_BOOKINGS + " b " +
                " JOIN " + TABLE_ROOMS + " r ON b." + KEY_BOOKING_ROOM_ID + " = r." + KEY_ID +
                " JOIN " + TABLE_USERS + " u ON b." + KEY_BOOKING_USER_ID + " = u." + KEY_ID +
                " ORDER BY b." + KEY_BOOKING_DATE + " DESC, b." + KEY_BOOKING_START + " ASC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                Booking booking = new Booking(
                        cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(KEY_BOOKING_ROOM_ID)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(KEY_BOOKING_USER_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_BOOKING_DATE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_BOOKING_START)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_BOOKING_END)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_BOOKING_STATUS)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_BOOKING_REASON)));
                booking.setRoomName(cursor.getString(cursor.getColumnIndexOrThrow("room_name")));
                booking.setUserName(cursor.getString(cursor.getColumnIndexOrThrow("user_name")));
                booking.setRoomImageUri(cursor.getString(cursor.getColumnIndexOrThrow("room_image")));
                bookings.add(booking);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return bookings;
    }

    public List<Booking> getBookingsByUser(int userId) {
        List<Booking> bookings = new ArrayList<>();
        String query = "SELECT b.*, r." + KEY_ROOM_NAME + " AS room_name, r." + KEY_ROOM_IMAGE_URI
                + " AS room_image, u." + KEY_USER_NAME + " AS user_name " +
                " FROM " + TABLE_BOOKINGS + " b " +
                " JOIN " + TABLE_ROOMS + " r ON b." + KEY_BOOKING_ROOM_ID + " = r." + KEY_ID +
                " JOIN " + TABLE_USERS + " u ON b." + KEY_BOOKING_USER_ID + " = u." + KEY_ID +
                " WHERE b." + KEY_BOOKING_USER_ID + " = ?" +
                " ORDER BY b." + KEY_BOOKING_DATE + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[] { String.valueOf(userId) });
        if (cursor.moveToFirst()) {
            do {
                Booking booking = new Booking(
                        cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(KEY_BOOKING_ROOM_ID)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(KEY_BOOKING_USER_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_BOOKING_DATE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_BOOKING_START)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_BOOKING_END)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_BOOKING_STATUS)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_BOOKING_REASON)));
                booking.setRoomName(cursor.getString(cursor.getColumnIndexOrThrow("room_name")));
                booking.setUserName(cursor.getString(cursor.getColumnIndexOrThrow("user_name")));
                booking.setRoomImageUri(cursor.getString(cursor.getColumnIndexOrThrow("room_image")));
                bookings.add(booking);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return bookings;
    }

    public boolean updateBookingStatus(int bookingId, String status) {
        if ("APPROVED".equals(status)) {
            // Check for conflicts before approving
            SQLiteDatabase dbRead = this.getReadableDatabase();
            String query = "SELECT * FROM " + TABLE_BOOKINGS + " WHERE " + KEY_ID + " = ?";
            Cursor cursor = dbRead.rawQuery(query, new String[] { String.valueOf(bookingId) });
            if (cursor.moveToFirst()) {
                int roomId = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_BOOKING_ROOM_ID));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(KEY_BOOKING_DATE));
                String start = cursor.getString(cursor.getColumnIndexOrThrow(KEY_BOOKING_START));
                String end = cursor.getString(cursor.getColumnIndexOrThrow(KEY_BOOKING_END));
                cursor.close();

                if (!isTimeSlotAvailable(roomId, date, start, end)) {
                    return false; // Conflict found, cannot approve
                }
            } else {
                cursor.close();
                return false; // Booking not found
            }
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_BOOKING_STATUS, status);
        db.update(TABLE_BOOKINGS, values, KEY_ID + " = ?", new String[] { String.valueOf(bookingId) });
        return true;
    }

    public void deleteBooking(int bookingId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_BOOKINGS, KEY_ID + " = ?", new String[] { String.valueOf(bookingId) });
    }

    /**
     * Auto-update APPROVED bookings to FINISHED when end time has passed
     * Call this method when loading booking list
     */
    public void updateExpiredBookings() {
        SQLiteDatabase db = this.getWritableDatabase();

        // Get current date and time
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd",
                java.util.Locale.getDefault());
        java.text.SimpleDateFormat timeFormat = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
        java.util.Date now = new java.util.Date();
        String currentDate = dateFormat.format(now);
        String currentTime = timeFormat.format(now);

        // Update bookings that have passed their end time
        ContentValues values = new ContentValues();
        values.put(KEY_BOOKING_STATUS, "FINISHED");

        // Update where: (date < today) OR (date = today AND end_time < current_time)
        String whereClause = "(" + KEY_BOOKING_STATUS + " = 'APPROVED') AND " +
                "((" + KEY_BOOKING_DATE + " < ?) OR " +
                "(" + KEY_BOOKING_DATE + " = ? AND " + KEY_BOOKING_END + " < ?))";

        String[] whereArgs = new String[] { currentDate, currentDate, currentTime };

        int updated = db.update(TABLE_BOOKINGS, values, whereClause, whereArgs);

        if (updated > 0) {
            android.util.Log.d("DatabaseHelper", "Auto-updated " + updated + " expired bookings to FINISHED");
        }
    }
}
