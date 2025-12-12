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

    private EditText etPassword, etPassword2, etUserName;
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

        etPassword = findViewById(R.id.Password);
        etPassword2 = findViewById(R.id.PassswordAuthentication);
        etUserName = findViewById(R.id.UserName);
        genderSpinner = findViewById(R.id.Gender);
        btnUpdateDetails = findViewById(R.id.btnUpdateDetails);

        setupGenderSpinner();
        populateFields();

        btnUpdateDetails.setOnClickListener(v -> updateDetails());
    }

    private void setupGenderSpinner() {
        ArrayList<String> genders = new ArrayList<>();
        genders.add("Gender");
        genders.add("Male");
        genders.add("Female");
        genders.add("Else");

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

    private void populateFields() {
        etPassword.setText(currentUser.getPassword());
        etPassword2.setText(currentUser.getPassword());
        etUserName.setText(currentUser.getUserName());

        // Set gender spinner selection
        String[] genders = {"Gender", "Male", "Female", "Else"};
        for (int i = 0; i < genders.length; i++) {
            if (genders[i].equalsIgnoreCase(currentUser.getGender())) {
                genderSpinner.setSelection(i);
                break;
            }
        }
    }

    private void updateDetails() {
        String password = etPassword.getText().toString().trim();
        String password2 = etPassword2.getText().toString().trim();
        String userName = etUserName.getText().toString().trim();



        if (!password.equals(password2)) {
            Toast.makeText(this, "הסיסמאות אינן תואמות", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedGender.equals("Gender") || selectedGender.isEmpty()) {
            Toast.makeText(this, "נא לבחור מגדר", Toast.LENGTH_SHORT).show();
            return;
        }

        currentUser.setPassword(password);
        currentUser.setUserName(userName);
        currentUser.setGender(selectedGender);

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