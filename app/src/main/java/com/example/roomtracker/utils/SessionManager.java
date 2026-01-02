package com.example.roomtracker.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.roomtracker.model.User;

public class SessionManager {
    private static final String PREF_NAME = "RoomTrackerSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_ROLE = "userRole";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void createLoginSession(User user) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putInt(KEY_USER_ID, user.getId());
        editor.putString(KEY_USER_NAME, user.getName());
        editor.putString(KEY_USER_EMAIL, user.getEmail());
        editor.putString(KEY_USER_ROLE, user.getRole());
        editor.commit();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public User getUserDetails() {
        if (!isLoggedIn())
            return null;

        return new User(
                pref.getInt(KEY_USER_ID, -1), // id
                pref.getString(KEY_USER_NAME, null), // name
                pref.getString(KEY_USER_EMAIL, null), // email
                null, // password (tidak disimpan)
                pref.getString(KEY_USER_ROLE, null), // role
                null, // ktm
                null, // profileImage
                null, // fakultas
                null, // prodi
                null, // angkatan
                0 // isVerified
        );
    }

    public void logoutUser() {
        editor.clear();
        editor.commit();
    }

    public String getRole() {
        return pref.getString(KEY_USER_ROLE, "");
    }

    public int getUserId() {
        return pref.getInt(KEY_USER_ID, -1);
    }
}
