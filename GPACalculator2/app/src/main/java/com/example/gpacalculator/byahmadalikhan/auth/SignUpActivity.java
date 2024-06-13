package com.example.gpacalculator.byahmadalikhan.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gpacalculator.byahmadalikhan.R;
import com.example.gpacalculator.byahmadalikhan.model.GradingDetailsModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public class SignUpActivity extends AppCompatActivity {

    private String email;
    private String password;
    private String userName;

    EditText mail, pass, name;

    private FirebaseAuth auth; // Firebase authentication instance
    private DatabaseReference database; // Firebase Realtime Database instance
    private FirebaseDatabase databases;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Finding ids of views
        mail = findViewById(R.id.email);
        pass = findViewById(R.id.password);
        name = findViewById(R.id.ownerName);

        // Initialization Firebase auth
        auth = FirebaseAuth.getInstance(); // Initializing Firebase Authentication
        // Initialization Firebase Database
        databases = FirebaseDatabase.getInstance(); // Initialize Firebase Realtime Database
        // Initialization Firebase Database
        database = FirebaseDatabase.getInstance().getReference(); // Initializing Firebase Realtime Database

        Button createAccountButton = findViewById(R.id.signIn);
        createAccountButton.setOnClickListener(view -> {

            // Converting into String and trimming
            email = mail.getText().toString().trim();
            password = pass.getText().toString().trim();
            userName = name.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty() || userName.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                createAccount(email, password);
            }

        });


        // Sending user to Login In Page
        TextView txtView = findViewById(R.id.alreadyHaveAccount);
        txtView.setOnClickListener(view -> {
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    // Method to create user account
    private void createAccount(String email, String password) {

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {

            if (task.isSuccessful()) {
                Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show();
                saveUserData(); // Save user data to Firebase Realtime Database
                initializeDataInFirebase();
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Account creation failed", Toast.LENGTH_SHORT).show();
                Log.d("Account", "createAccount: Failure", task.getException());
            }
        });
    }

    private void initializeDataInFirebase() {

        ArrayList<GradingDetailsModel> gradingDetailsModelArrayList = new ArrayList<>();

        GradingDetailsModel gradingDetailsModel1 = new GradingDetailsModel("85", "100", "A", "4.00");
        GradingDetailsModel gradingDetailsModel2 = new GradingDetailsModel("80", "84", "A-", "3.66");
        GradingDetailsModel gradingDetailsModel3 = new GradingDetailsModel("75", "79", "B+", "3.33");
        GradingDetailsModel gradingDetailsModel4 = new GradingDetailsModel("71", "74", "B", "3.00");
        GradingDetailsModel gradingDetailsModel5 = new GradingDetailsModel("68", "70", "B-", "2.66");
        GradingDetailsModel gradingDetailsModel6 = new GradingDetailsModel("64", "67", "C+", "2.33");
        GradingDetailsModel gradingDetailsModel7 = new GradingDetailsModel("61", "63", "C", "2.00");
        GradingDetailsModel gradingDetailsModel8 = new GradingDetailsModel("58", "60", "C-", "1.66");
        GradingDetailsModel gradingDetailsModel9 = new GradingDetailsModel("54", "57", "D+", "1.30");
        GradingDetailsModel gradingDetailsModel10 = new GradingDetailsModel("50", "53", "D", "1.00");
        GradingDetailsModel gradingDetailsModel11 = new GradingDetailsModel("0", "49", "F", "0.00");

        gradingDetailsModelArrayList.add(gradingDetailsModel1);
        gradingDetailsModelArrayList.add(gradingDetailsModel2);
        gradingDetailsModelArrayList.add(gradingDetailsModel3);
        gradingDetailsModelArrayList.add(gradingDetailsModel4);
        gradingDetailsModelArrayList.add(gradingDetailsModel5);
        gradingDetailsModelArrayList.add(gradingDetailsModel6);
        gradingDetailsModelArrayList.add(gradingDetailsModel7);
        gradingDetailsModelArrayList.add(gradingDetailsModel8);
        gradingDetailsModelArrayList.add(gradingDetailsModel9);
        gradingDetailsModelArrayList.add(gradingDetailsModel10);
        gradingDetailsModelArrayList.add(gradingDetailsModel11);

        AtomicReference<Boolean> check = new AtomicReference<>(true);

        String userId = auth.getCurrentUser().getUid();
        int size = gradingDetailsModelArrayList.size();

        if (userId != null) {
            DatabaseReference gradingRef = databases.getReference().child("user").child(userId).child("gradeInformation");

            for (int i = 0; i < size; i++) {

                String newEntryKey = gradingRef.push().getKey();
                GradingDetailsModel gradingDetailsModelSingle = gradingDetailsModelArrayList.get(i);

                gradingRef.child(newEntryKey).setValue(gradingDetailsModelSingle).addOnSuccessListener(aVoid -> {
                            // Get current authenticated user
                            Toast.makeText(this, "Added new row successfully", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Log.e("AddNewRow", "Error adding new row: " + e.getMessage());
                            Toast.makeText(this, "Failed to add new row", Toast.LENGTH_SHORT).show();
                        });

            }

            if (check.get()) {
                Toast.makeText(this, "Successfully set all the data", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed! Please set data manually", Toast.LENGTH_SHORT).show();
            }

        } else {
            Log.e("saveUserData", "Error in grading first");
        }
    }


    // Save user data into Firebase Realtime Database
    private void saveUserData() {

        // Converting into String and trimming
        email = mail.getText().toString().trim();
        password = pass.getText().toString().trim();
        userName = name.getText().toString().trim();

        String degree = "set value";
        int semesters = 8;
        String rollNo = "not set";

        HashMap<String, Object> map = new HashMap<>();
        map.put("name", userName);
        map.put("degree", degree);
        map.put("rollNo", rollNo);
        map.put("semesters", semesters);
        map.put("email", email);
        map.put("password", password);


        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            // Save data into Firebase Realtime Database under the "user" node
            database.child("user").child(userId).child("userInfo").setValue(map);
        }
    }

}
