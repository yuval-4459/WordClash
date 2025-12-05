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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {


    Button btnDeleteUser;
    Button btnLogout;

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


        btnDeleteUser = findViewById(R.id.DeleteUserButton);
        btnDeleteUser.setOnClickListener(view ->
                startActivity(new Intent(MainActivity.this, UserListActivity.class)));

        btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> logout());
        HideAdminButton();

    }

    private void HideAdminButton(){
        if(!user.isAdmin()){
            btnDeleteUser.setVisibility(View.GONE);
        }
        else{
            btnDeleteUser.setVisibility(View.VISIBLE);
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
