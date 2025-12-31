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
import com.example.wordclash.utils.SharedPreferencesUtils;

import java.util.ArrayList;

public class ChangeDetailsActivity extends AppCompatActivity {

    private EditText etUserName, etEmail, etPassword;
    private Spinner genderSpinner;
    private Button btnUpdateDetails;
    private String selectedGender = "";

    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_details_page);

        currentUser = SharedPreferencesUtils.getUser(this);
        if (currentUser == null) {
            Toast.makeText(this, "שגיאה בטעינת נתוני משתמש", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        etUserName = findViewById(R.id.UserName);
        etEmail = findViewById(R.id.Email);
        etPassword = findViewById(R.id.Password);
        genderSpinner = findViewById(R.id.Gender);
        btnUpdateDetails = findViewById(R.id.btnUpdateDetails);

        setupGenderSpinner();
        setupFieldsBasedOnRole();
        populateFields();

        btnUpdateDetails.setOnClickListener(v -> updateDetails());
    }

    private void setupGenderSpinner() {
        ArrayList<String> genders = new ArrayList<>();
        genders.add("Male");
        genders.add("Female");
        genders.add("Other");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, genders);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(adapter);

        genderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                selectedGender = adapterView.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                selectedGender = "";
            }
        });
    }

    private void setupFieldsBasedOnRole() {
        if (currentUser.isAdmin()) {
            // Admin can edit everything except admin status
            etEmail.setEnabled(true);
            etPassword.setEnabled(true);
            etUserName.setEnabled(true);
            genderSpinner.setEnabled(true);
        } else {
            // Regular user can only edit username and gender
            etEmail.setEnabled(false);
            etPassword.setEnabled(false);
            etUserName.setEnabled(true);
            genderSpinner.setEnabled(true);
        }
    }

    private void populateFields() {
        etUserName.setText(currentUser.getUserName());
        etEmail.setText(currentUser.getEmail());
        etPassword.setText(currentUser.getPassword());

        String[] genders = {"Male", "Female", "Other"};
        for (int i = 0; i < genders.length; i++) {
            if (genders[i].equalsIgnoreCase(currentUser.getGender())) {
                genderSpinner.setSelection(i);
                selectedGender = genders[i];
                break;
            }
        }
    }

    private void updateDetails() {
        String userName = etUserName.getText().toString().trim();

        if (userName.isEmpty()) {
            Toast.makeText(this, "נא למלא שם משתמש", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedGender.isEmpty()) {
            selectedGender = currentUser.getGender();
        }

        currentUser.setUserName(userName);
        currentUser.setGender(selectedGender);

        // Admin can also update email and password
        if (currentUser.isAdmin()) {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "נא למלא את כל השדות", Toast.LENGTH_SHORT).show();
                return;
            }

            currentUser.setEmail(email);
            currentUser.setPassword(password);
        }

        DatabaseService.getInstance().updateUser(currentUser, new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void v) {
                SharedPreferencesUtils.saveUser(ChangeDetailsActivity.this, currentUser);

                Toast.makeText(ChangeDetailsActivity.this,
                        "הפרטים עודכנו בהצלחה!",
                        Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(ChangeDetailsActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(ChangeDetailsActivity.this,
                        "שגיאה בעדכון פרטים: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}