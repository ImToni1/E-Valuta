package com.example.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class Settings extends AppCompatActivity {

    private ImageView profileImage;
    private TextInputEditText editTextIme, editTextPrezime, editTextEmail, editTextLozinka;
    private MaterialButton buttonSpremi;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initViews();
        setupBottomNavigation();
        loadUserData();
        setupClickListeners();
    }

    private void initViews() {
        profileImage = findViewById(R.id.profile_image);
        editTextIme = findViewById(R.id.editTextIme);
        editTextPrezime = findViewById(R.id.editTextPrezime);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextLozinka = findViewById(R.id.editTextLozinka);
        buttonSpremi = findViewById(R.id.buttonSpremi);
        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
    }

    private void setupClickListeners() {
        buttonSpremi.setOnClickListener(v -> saveUserData());
        profileImage.setOnClickListener(v -> {
            Toast.makeText(Settings.this, R.string.select_new_image, Toast.LENGTH_SHORT).show();
        });
    }

    private void saveUserData() {
        String ime = editTextIme.getText().toString().trim();
        String prezime = editTextPrezime.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String lozinka = editTextLozinka.getText().toString().trim();

        if (ime.isEmpty() || prezime.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, R.string.all_fields_required, Toast.LENGTH_SHORT).show();
            return;
        }
        SharedPreferences sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("USER_IME", ime);
        editor.putString("USER_PREZIME", prezime);
        editor.putString("USER_EMAIL", email);
        if (!lozinka.isEmpty()) {
            editor.putString("USER_LOZINKA", lozinka);
        }
        editor.apply();

        Toast.makeText(this, R.string.data_saved_successfully, Toast.LENGTH_SHORT).show();
    }

    private void loadUserData() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE);
        String ime = sharedPreferences.getString("USER_IME", "");
        String prezime = sharedPreferences.getString("USER_PREZIME", "");
        String email = sharedPreferences.getString("USER_EMAIL", "");

        editTextIme.setText(ime);
        editTextPrezime.setText(prezime);
        editTextEmail.setText(email);
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_settings);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                startActivity(new Intent(getApplicationContext(), Home.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_wallet) {
                startActivity(new Intent(getApplicationContext(), Wallet.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_trade) {
                startActivity(new Intent(getApplicationContext(), Trade.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_settings) {
                return true;
            }
            return false;
        });
    }
}