package com.example.wordclash.screens;

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
import java.util.List;
import java.util.Map;

public class admin_users_Activity extends AppCompatActivity {

    private Spinner userSpinner, genderSpinner;
    private EditText emailField, passwordField, confirmPasswordField, usernameField;
    private Button updateBtn, deleteBtn;

    private List<User> userList;
    private User selectedUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_users);

        userList = new ArrayList<>();

        initViews();
        setupGenderSpinner();

        setupListeners();
    }

    private void initViews() {
        userSpinner = findViewById(R.id.userSpinner);
        genderSpinner = findViewById(R.id.Gender);
        emailField = findViewById(R.id.Email);
        passwordField = findViewById(R.id.Password);
        confirmPasswordField = findViewById(R.id.PassswordAuthentication);
        usernameField = findViewById(R.id.UserName);
        updateBtn = findViewById(R.id.updateUserBtn);
        deleteBtn = findViewById(R.id.deleteUserBtn);
    }

    private void setupGenderSpinner() {
        String[] genders = {"Male", "Female", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, genders);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(adapter);
    }


    private void setupListeners() {
        userSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedUser = userList.get(position);
                populateFields();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        updateBtn.setOnClickListener(v -> updateUser());
        deleteBtn.setOnClickListener(v -> deleteUser());
    }

    private void populateFields() {
        if (selectedUser != null) {
            emailField.setText(selectedUser.getEmail());
            passwordField.setText(selectedUser.getPassword());
            confirmPasswordField.setText(selectedUser.getPassword());
            usernameField.setText(selectedUser.getUserName());

            String[] genders = {"Male", "Female", "Other"};
            for (int i = 0; i < genders.length; i++) {
                if (genders[i].equalsIgnoreCase(selectedUser.getGender())) {
                    genderSpinner.setSelection(i);
                    break;
                }
            }
        }
    }

    private void updateUser() {
        if (selectedUser == null) return;

        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();
        String confirmPass = confirmPasswordField.getText().toString().trim();
        String username = usernameField.getText().toString().trim();
        String gender = genderSpinner.getSelectedItem().toString();

        if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPass)) {
            Toast.makeText(this, "Passwords don't match", Toast.LENGTH_SHORT).show();
            return;
        }


        DatabaseService.getInstance().getUser(selectedUser.getId(), new DatabaseService.DatabaseCallback<User>() {
            @Override
            public void onCompleted(User user) {
                Toast.makeText(admin_users_Activity.this, "User updated successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(admin_users_Activity.this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteUser() {
        if (selectedUser == null) return;

        DatabaseService.getInstance().deleteUser(selectedUser.getId(), new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void v) {
                Toast.makeText(admin_users_Activity.this, "User deleted", Toast.LENGTH_SHORT).show();
                clearFields();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(admin_users_Activity.this, "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void clearFields() {
        emailField.setText("");
        passwordField.setText("");
        confirmPasswordField.setText("");
        usernameField.setText("");
        genderSpinner.setSelection(0);
    }

}