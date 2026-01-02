package com.example.roomtracker.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.roomtracker.R;
import com.example.roomtracker.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private SessionManager session;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        session = new SessionManager(this);
        if (!session.isLoggedIn()) {
            logout();
            return;
        }

        bottomNav = findViewById(R.id.bottom_navigation);

        setupNavigationForRole(session.getRole());

        bottomNav.setOnItemSelectedListener(this::onNavigationItemSelected);

        if (savedInstanceState == null) {
            int navId = getIntent().getIntExtra("NAVIGATE_TO", -1);
            if (navId != -1) {
                bottomNav.setSelectedItemId(navId);
            } else {
                bottomNav.setSelectedItemId(R.id.nav_home);
            }
        }
    }

    private void setupNavigationForRole(String role) {
        // Customize menu based on role
        if ("ADMIN".equals(role) || "STAFF".equals(role)) {
            // Admin/Staff: Map 'Search' tab to 'Users' management
            MenuItem searchItem = bottomNav.getMenu().findItem(R.id.nav_search);
            if (searchItem != null) {
                searchItem.setTitle("Users");
                searchItem.setIcon(R.drawable.ic_nav_users);
                searchItem.setVisible(true);
            }
        } else if ("MAHASISWA".equals(role)) {
            // Mahasiswa:
            // 1. Beranda (Home) - nav_home
            MenuItem homeItem = bottomNav.getMenu().findItem(R.id.nav_home);
            if (homeItem != null) {
                homeItem.setTitle("Beranda");
                homeItem.setIcon(R.drawable.ic_nav_home);
            }

            // 2. Jadwal (Schedule) - nav_schedule
            MenuItem scheduleItem = bottomNav.getMenu().findItem(R.id.nav_schedule);
            if (scheduleItem != null) {
                scheduleItem.setTitle("Jadwal");
            }

            // 3. Riwayat (History) - nav_log
            MenuItem logItem = bottomNav.getMenu().findItem(R.id.nav_log);
            if (logItem != null) {
                logItem.setTitle("Riwayat");
            }

            // 4. Explore (Search slot) - nav_search
            MenuItem searchItem = bottomNav.getMenu().findItem(R.id.nav_search);
            if (searchItem != null) {
                searchItem.setVisible(true);
                searchItem.setTitle("Explore");
                searchItem.setIcon(R.drawable.ic_explore_compass);
            }
        }
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;
        int id = item.getItemId();
        String role = session.getRole();

        if (id == R.id.nav_home) {
            if ("MAHASISWA".equals(role)) {
                fragment = new StudentHomeFragment();
            } else {
                fragment = new RoomListFragment();
            }
        } else if (id == R.id.nav_schedule) { // Jadwal Room
            if ("MAHASISWA".equals(role)) {
                fragment = new ScheduleFragment(); // General Schedule
            } else {
                fragment = new BookingListFragment(); // Admin manages bookings
            }
        } else if (id == R.id.nav_log) { // Riwayat
            if ("MAHASISWA".equals(role)) {
                fragment = new LogFragment();
            } else {
                fragment = new LogFragment(); // Admin Logs
            }
        } else if (id == R.id.nav_search) {
            if ("ADMIN".equals(role) || "STAFF".equals(role)) {
                fragment = new UserListFragment();
            } else {
                fragment = new ExploreFragment();
            }
        } else if (id == R.id.nav_profile) {
            fragment = new ProfileFragment();
        }

        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }

        return false;
    }

    public void navigateToExplore() {
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_search);
        }
    }

    public void logout() {
        session.logoutUser();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
