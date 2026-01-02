package com.example.roomtracker.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.text.Html;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.roomtracker.R;
import com.example.roomtracker.database.DatabaseHelper;
import com.example.roomtracker.model.User;
import com.example.roomtracker.utils.SessionManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class LoginActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText etEmail, etPassword, etName, etFakultas, etProdi, etAngkatan;
    private Button btnLogin, btnUploadKtm;
    private TextView tvRegister, tvKtmStatus;
    private ImageView ivKtmPreview;
    private View layoutRegistrationFields;
    private DatabaseHelper db;
    private SessionManager session;
    private boolean isRegisterMode = false;
    private Uri ktmImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = new DatabaseHelper(this);
        session = new SessionManager(this);

        if (session.isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        initializeViews();

        btnUploadKtm.setOnClickListener(v -> openFileChooser());
        btnLogin.setOnClickListener(v -> handleLoginOrRegister());
        tvRegister.setOnClickListener(v -> toggleMode());

        // Google login removed as per request
    }

    private void initializeViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etName = findViewById(R.id.etName);
        etFakultas = findViewById(R.id.etFakultas);
        etProdi = findViewById(R.id.etProdi);
        etAngkatan = findViewById(R.id.etAngkatan);

        btnLogin = findViewById(R.id.btnLogin);
        btnUploadKtm = findViewById(R.id.btnUploadKtm);

        tvRegister = findViewById(R.id.tvRegister);
        tvKtmStatus = findViewById(R.id.tvKtmStatus);

        ivKtmPreview = findViewById(R.id.ivKtmPreview);
        layoutRegistrationFields = findViewById(R.id.layoutRegistrationFields);

        // Password Visibility Toggle Action
        etPassword.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (etPassword.getRight()
                        - etPassword.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width()
                        - etPassword.getPaddingRight())) {

                    if (etPassword
                            .getTransformationMethod() instanceof android.text.method.PasswordTransformationMethod) {
                        etPassword.setTransformationMethod(
                                android.text.method.HideReturnsTransformationMethod.getInstance());
                        etPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_visibility, 0);
                    } else {
                        etPassword.setTransformationMethod(
                                android.text.method.PasswordTransformationMethod.getInstance());
                        etPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_visibility_off, 0);
                    }

                    etPassword.setSelection(etPassword.getText().length());
                    return true;
                }
            }
            return false;
        });

        updateRegisterText();
    }

    private void openFileChooser() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (checkSelfPermission(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] { android.Manifest.permission.READ_EXTERNAL_STORAGE }, 100);
                return;
            }
        }

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(Intent.createChooser(intent, "Select KTM Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                openFileChooser();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            ktmImageUri = data.getData();
            ivKtmPreview.setImageURI(ktmImageUri);
            tvKtmStatus.setText("Image Selected");
        }
    }

    private String copyUriToInternalStorage(Uri uri, String filename) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            File updateDir = new File(getFilesDir(), "ktm_images");
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

    private void handleLoginOrRegister() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isRegisterMode) {
            String name = etName.getText().toString().trim();
            String fakultas = etFakultas.getText().toString().trim();
            String prodi = etProdi.getText().toString().trim();
            String angkatan = etAngkatan.getText().toString().trim();

            if (TextUtils.isEmpty(name)) {
                Toast.makeText(this, "Please enter your Name", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(fakultas) || TextUtils.isEmpty(prodi) || TextUtils.isEmpty(angkatan)) {
                Toast.makeText(this, "Please fill all student details", Toast.LENGTH_SHORT).show();
                return;
            }

            if (ktmImageUri == null) {
                Toast.makeText(this, "Please upload KTM Photo", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save KTM image to internal storage
            String savedKtmPath = copyUriToInternalStorage(ktmImageUri, "ktm_" + System.currentTimeMillis() + ".jpg");
            if (savedKtmPath == null) {
                savedKtmPath = ktmImageUri.toString(); // Fallback, though risky
            }

            // Register as MAHASISWA
            User newUser = new User(name, email, password, "MAHASISWA", savedKtmPath, fakultas, prodi, angkatan);
            long id = db.addUser(newUser);
            if (id > 0) {
                Toast.makeText(this, "Registration Successful! Waiting for Admin verification.", Toast.LENGTH_LONG)
                        .show();
                clearRegistrationFields();
                toggleMode();
            } else {
                Toast.makeText(this, "Registration Failed. Email might exist.", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Login
            User user = db.checkLogin(email, password);
            if (user != null) {
                session.createLoginSession(user);
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else {
                // Check specifically why login failed
                if (db.isEmailExists(email)) {
                    Toast.makeText(this, "Incorrect Password", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Email not found", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void clearRegistrationFields() {
        etEmail.setText("");
        etPassword.setText("");
        etName.setText("");
        etFakultas.setText("");
        etProdi.setText("");
        etAngkatan.setText("");
        ivKtmPreview.setImageResource(android.R.drawable.ic_menu_camera);
        tvKtmStatus.setText("No file selected");
        ktmImageUri = null;
    }

    private void toggleMode() {
        isRegisterMode = !isRegisterMode;
        updateRegisterText();

        if (isRegisterMode) {
            btnLogin.setText("Register");
            layoutRegistrationFields.setVisibility(View.VISIBLE);
        } else {
            btnLogin.setText("Login");
            layoutRegistrationFields.setVisibility(View.GONE);
        }
    }

    private void updateRegisterText() {
        if (isRegisterMode) {
            String text = "Already have an account? <font color='#007AFF'><b>Login</b></font>";
            tvRegister.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY));
        } else {
            String text = "Don't have an account? <font color='#007AFF'><b>Sign Up</b></font>";
            tvRegister.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY));
        }
    }
}
