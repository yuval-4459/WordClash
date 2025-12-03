package com.example.wordclash.screens;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordclash.R;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private TextView tvHelloUser;

    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_page);   // uses home_page.xml

        tvHelloUser = findViewById(R.id.tvHelloUser);


        usersRef = FirebaseDatabase.getInstance().getReference("users");


    }

}
