package com.example.wordclash.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordclash.R;

import com.example.wordclash.models.User;
import com.example.wordclash.services.DatabaseService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    // Firebase

    private DatabaseService db;

    // Views from XML
    private EditText etEmail, etPassword, etPassword2, etUserName;
    private Spinner genderSpinner;
    private Button btnConfirm;
    private String selectedGender = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_page);

        // UI Components
        etEmail = findViewById(R.id.Email);
        etPassword = findViewById(R.id.Password);
        etPassword2 = findViewById(R.id.PassswordAuthentication);
        etUserName = findViewById(R.id.UserName);
        genderSpinner = findViewById(R.id.Gender);
        btnConfirm = findViewById(R.id.ConfirmsignUpButton);

        // Gender Spinner
        ArrayList<String> genders = new ArrayList<>();
        genders.add("בחר מגדר");
        genders.add("זכר");
        genders.add("נקבה");
        genders.add("אחר");

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, genders);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(adapter);

        genderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView,
                                       View view, int position, long l) {
                selectedGender = adapterView.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                selectedGender = "";
            }
        });

        // כפתור הרשמה
        btnConfirm.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String password2 = etPassword2.getText().toString().trim();
        String userName = etUserName.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()
                || password2.isEmpty() || userName.isEmpty()) {
            Toast.makeText(this, "נא למלא את כל השדות", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(password2)) {
            Toast.makeText(this, "הסיסמאות אינן תואמות", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedGender.equals("בחר מגדר") || selectedGender.isEmpty()) {
            selectedGender = "לא צוין";
        }

        String id = db.generateUserId();
        User user = new User(id, email, password, userName, selectedGender, false);

        db.checkIfEmailExists(user.getEmail(), new DatabaseService.DatabaseCallback<Boolean>() {
            @Override
            public void onCompleted(Boolean exist) {
                if (exist) {
                    Toast.makeText(SignUpActivity.this, "מייל כבר קיים", Toast.LENGTH_LONG).show();
                } else {
                    db.createNewUser(user, new DatabaseService.DatabaseCallback<Void>() {
                        @Override
                        public void onCompleted(Void v) {
                            Toast.makeText(SignUpActivity.this, "נרשמת בהצלחה!", Toast.LENGTH_SHORT).show();

                            // מעבר למסך MainActivity
                            Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }

                        @Override
                        public void onFailed(Exception e) {
                            Toast.makeText(SignUpActivity.this,
                                            "נכשל בשמירת המשתמש: " + e.getMessage(),
                                            Toast.LENGTH_LONG)
                                    .show();
                        }
                    });
                }
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(SignUpActivity.this,
                                "נכשל בשמירת המשתמש: " + e.getMessage(),
                                Toast.LENGTH_LONG)
                        .show();
            }
        });


    }
}
