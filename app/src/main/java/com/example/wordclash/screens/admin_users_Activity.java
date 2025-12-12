package com.example.wordclash.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wordclash.R;
import com.example.wordclash.models.User;
import com.example.wordclash.services.DatabaseService;
import com.example.wordclash.utils.SharedPreferencesUtils;

import java.util.ArrayList;

import java.util.List;


public class admin_users_Activity extends AppCompatActivity {

    private Spinner genderSpinner;
    private EditText emailField, passwordField, confirmPasswordField, usernameField;
    private Button updateBtn, deleteBtn;
    private CheckBox isAdminCheckBox;

    private User selectedUser;

    private User user;
    private TextView tvUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_users);



        selectedUser = getIntent().getSerializableExtra("user", User.class);

        initViews();
        setupGenderSpinner();
        setupListeners();
        populateFields();

    /*
        user = SharedPreferencesUtils.getUser(admin_users_Activity.this);
        tvUser = findViewById(R.id.userTitle);
        tvUser.setText("User: "+ selectedUser.getUserName());
    */

    }

    private void initViews() {
        genderSpinner = findViewById(R.id.Gender);
        emailField = findViewById(R.id.Email);
        passwordField = findViewById(R.id.Password);
        usernameField = findViewById(R.id.UserName);
        isAdminCheckBox = findViewById(R.id.isAdminCheckBox);
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
        updateBtn.setOnClickListener(v -> updateUser());
        deleteBtn.setOnClickListener(v -> deleteUser());
    }

    private void populateFields() {
        if (selectedUser == null) {
            return;
        }
        emailField.setText(selectedUser.getEmail());
        passwordField.setText(selectedUser.getPassword());
        usernameField.setText(selectedUser.getUserName());
        isAdminCheckBox.setChecked(selectedUser.isAdmin());

        String[] genders = {"Male", "Female", "Other"};
        for (int i = 0; i < genders.length; i++) {
            if (genders[i].equalsIgnoreCase(selectedUser.getGender())) {
                genderSpinner.setSelection(i);
                break;
            }
        }
    }

    private void updateUser() {
        if (selectedUser == null) return;

        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();
        String username = usernameField.getText().toString().trim();
        String gender = genderSpinner.getSelectedItem().toString();
        boolean isAdmin = isAdminCheckBox.isChecked();

        if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }



        selectedUser.setEmail(email);
        selectedUser.setPassword(password);
        selectedUser.setUserName(username);
        selectedUser.setGender(gender);
        selectedUser.setAdmin(isAdmin);


        DatabaseService.getInstance().updateUser(selectedUser, new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void v) {
                Toast.makeText(admin_users_Activity.this, "User updated successfully", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(admin_users_Activity.this, UserListActivity.class);
                startActivity(intent);

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

                Intent intent = new Intent(admin_users_Activity.this, UserListActivity.class);
                startActivity(intent);
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
        usernameField.setText("");
        genderSpinner.setSelection(0);
        isAdminCheckBox.setChecked(false);
    }

}