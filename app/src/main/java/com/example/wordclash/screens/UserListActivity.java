package com.example.wordclash.screens;

import android.content.Intent;
import android.os.Bundle;
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

import java.util.List;

public class UserListActivity extends AppCompatActivity {

    RecyclerView rvUsers;
    UserAdapter userAdapter;

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

        rvUsers = findViewById(R.id.rv_user_list_list);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));

        userAdapter = new UserAdapter(new UserAdapter.OnUserClickListener() {
            @Override
            public void onClick(User user) {
                if(user.getId().equals("-OfhuEaP25z-o6NIsC5K")) {
                    Toast.makeText(UserListActivity.this, "Can't Access this user - He is a Manager", Toast.LENGTH_LONG).show();
                } else {
                    Intent intent = new Intent(UserListActivity.this, admin_users_Activity.class);
                    intent.putExtra("user", user);
                    startActivity(intent);
                }
            }

            @Override
            public void onLongClick(User user) {
                if(user.getId().equals("-OfhuEaP25z-o6NIsC5K")) {
                    Toast.makeText(UserListActivity.this, "Can't Access this user - He is a Manager", Toast.LENGTH_LONG).show();
                } else {
                    Intent intent = new Intent(UserListActivity.this, admin_users_Activity.class);
                    intent.putExtra("user", user);
                    startActivity(intent);
                }
            }
        });

        rvUsers.setAdapter(userAdapter);

        DatabaseService.getInstance().getUserList(new DatabaseService.DatabaseCallback<List<User>>() {
            @Override
            public void onCompleted(List<User> users) {
                userAdapter.setUserList(users);
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(UserListActivity.this, "Failed to load users", Toast.LENGTH_SHORT).show();
            }
        });
    }
}