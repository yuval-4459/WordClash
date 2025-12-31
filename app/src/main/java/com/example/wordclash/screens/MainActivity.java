package com.example.wordclash.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordclash.R;

import com.example.wordclash.models.User;
import com.example.wordclash.utils.SharedPreferencesUtils;

public class MainActivity extends AppCompatActivity {


    Button btnUsersTable;
    Button btnLogout;
    Button btnChangeDetails;
    Button btnRanks;
    Button btnWordle;

    private User user;
    private TextView tvHelloUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_page);

        user = SharedPreferencesUtils.getUser(MainActivity.this);
        assert user != null;
        tvHelloUser = findViewById(R.id.tvHelloUser);
        tvHelloUser.setText("Hello "+ user.getUserName());


        btnUsersTable = findViewById(R.id.UsersTableButton);
        btnUsersTable.setOnClickListener(view ->
                startActivity(new Intent(MainActivity.this, UserListActivity.class)));

        btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> logout());

        btnChangeDetails = findViewById(R.id.btnChangeDetails);
        btnChangeDetails.setOnClickListener(view ->
                startActivity(new Intent(MainActivity.this, ChangeDetailsActivity.class)));


        btnRanks = findViewById(R.id.btnRanks);
        btnRanks.setOnClickListener(view ->
                startActivity(new Intent(MainActivity.this, RanksActivity.class)));

        btnWordle = findViewById(R.id.WordleButton);
        btnWordle.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, WordleActivity.class)));

        HideAdminButton();

    }

    private void HideAdminButton(){
        if(!user.isAdmin()){
            btnUsersTable.setVisibility(View.GONE);
        }
        else{
            btnUsersTable.setVisibility(View.VISIBLE);
        }
    }


    private void logout() {
        SharedPreferencesUtils.signOutUser(MainActivity.this);
        Intent intent = new Intent(MainActivity.this, StartPageActivity.class);
        //clear history
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
