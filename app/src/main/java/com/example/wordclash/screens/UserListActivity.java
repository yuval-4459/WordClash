package com.example.wordclash.screens;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wordclash.R;
import com.example.wordclash.adapters.UserAdapter;
import com.example.wordclash.models.User;
import com.example.wordclash.services.DatabaseService;
import com.example.wordclash.utils.SharedPreferencesUtils;

import java.util.ArrayList;
import java.util.List;

public class UserListActivity extends AppCompatActivity {

    RecyclerView rvUsers;
    UserAdapter userAdapter;
    EditText searchUsername, searchEmail;
    Spinner searchGender;
    Button btnBackToMain;

    List<User> allUsers = new ArrayList<>();
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        currentUser = SharedPreferencesUtils.getUser(this);

        rvUsers = findViewById(R.id.rv_user_list_list);
        searchUsername = findViewById(R.id.searchUsername);
        searchEmail = findViewById(R.id.searchEmail);
        searchGender = findViewById(R.id.searchGender);
        btnBackToMain = findViewById(R.id.btnBackToMain);

        rvUsers.setLayoutManager(new LinearLayoutManager(this));

        // Back button click listener
        btnBackToMain.setOnClickListener(v -> {
            Intent intent = new Intent(UserListActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        userAdapter = new UserAdapter(new UserAdapter.OnUserClickListener() {
            @Override
            public void onClick(User user) {
                if (user.getId().equals("-OfhuEaP25z-o6NIsC5K")) {
                    Toast.makeText(UserListActivity.this, "Can't Access this user - He is a Manager", Toast.LENGTH_LONG).show();
                } else {
                    Intent intent = new Intent(UserListActivity.this, admin_users_Activity.class);
                    intent.putExtra("user", user);
                    startActivity(intent);
                }
            }

            @Override
            public void onLongClick(User user) {
                if (user.getId().equals("-OfhuEaP25z-o6NIsC5K")) {
                    Toast.makeText(UserListActivity.this, "Can't Access this user - He is a Manager", Toast.LENGTH_LONG).show();
                } else {
                    Intent intent = new Intent(UserListActivity.this, admin_users_Activity.class);
                    intent.putExtra("user", user);
                    startActivity(intent);
                }
            }
        });

        rvUsers.setAdapter(userAdapter);

        // Setup gender spinner
        setupGenderSpinner();
        // Setup search functionality for all fields
        // - instead of clicking confirm to search for someone it will automaticly change
        TextWatcher searchWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        searchUsername.addTextChangedListener(searchWatcher);
        searchEmail.addTextChangedListener(searchWatcher);

        searchGender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterUsers();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        DatabaseService.getInstance().getUserList(new DatabaseService.DatabaseCallback<List<User>>() {
            @Override
            public void onCompleted(List<User> users) {
                allUsers.clear();
                // Sort users: admins first, then regular users
                List<User> adminUsers = new ArrayList<>();
                List<User> regularUsers = new ArrayList<>();
                for (User user : users) {
                    // Skip current admin user from the list
                    if (currentUser != null && user.getId().equals(currentUser.getId())) {
                        continue;
                    }

                    if (user.isAdmin()) {
                        adminUsers.add(user);
                    } else {
                        regularUsers.add(user);
                    }
                }
                allUsers.addAll(adminUsers);
                allUsers.addAll(regularUsers);

                userAdapter.setUserList(allUsers);
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(UserListActivity.this, "Failed to load users", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupGenderSpinner() {
        String[] genders = {"All", "Male", "Female", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, genders);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        searchGender.setAdapter(adapter);
    }

    private void filterUsers() {
        String usernameQuery = searchUsername.getText().toString().toLowerCase().trim();
        String emailQuery = searchEmail.getText().toString().toLowerCase().trim();
        String genderQuery = searchGender.getSelectedItem().toString();

        List<User> filteredList = new ArrayList<>();

        for (User user : allUsers) {

            // Check username criteria (only if field is not empty)
            if (!usernameQuery.isEmpty()) {
                if (user.getUserName() == null ||
                        !user.getUserName().toLowerCase().contains(usernameQuery)) {
                    continue;
                }
            }

            // Check email criteria (only if field is not empty)
            if (!emailQuery.isEmpty()) {
                if (user.getEmail() == null ||
                        !user.getEmail().toLowerCase().contains(emailQuery)) {
                    continue;
                }
            }

            // Check gender criteria (only if not "All")
            if (!genderQuery.equals("All")) {
                if (user.getGender() == null ||
                        !user.getGender().equalsIgnoreCase(genderQuery)) {
                    continue;
                }
            }

            // Add user only if ALL criteria have been continued
            filteredList.add(user);

        }

        userAdapter.setUserList(filteredList);
        userAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        DatabaseService.getInstance().getUserList(new DatabaseService.DatabaseCallback<List<User>>() {
            @Override
            public void onCompleted(List<User> users) {
                allUsers.clear();
                // Sort users: admins first, then regular users
                List<User> adminUsers = new ArrayList<>();
                List<User> regularUsers = new ArrayList<>();
                for (User user : users) {
                    // Skip current admin user from the list
                    if (currentUser != null && user.getId().equals(currentUser.getId())) {
                        continue;
                    }

                    if (user.isAdmin()) {
                        adminUsers.add(user);
                    } else {
                        regularUsers.add(user);
                    }
                }
                allUsers.addAll(adminUsers);
                allUsers.addAll(regularUsers);

                filterUsers();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(UserListActivity.this, "Failed to load users", Toast.LENGTH_SHORT).show();
            }
        });
    }
}