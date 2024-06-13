package com.example.gpacalculator.byahmadalikhan.auth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.gpacalculator.byahmadalikhan.MainActivity;
import com.example.gpacalculator.byahmadalikhan.R;
import com.example.gpacalculator.byahmadalikhan.model.GradingDetailsModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public class LoginActivity extends AppCompatActivity {

    private String email;
    private String password;

    private FirebaseAuth auth; // Firebase authentication instance
    private DatabaseReference database; // Firebase Realtime Database instance
    private FirebaseDatabase databases;
    private GoogleSignInClient googleSignInClient;


    EditText mail, pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        // Configure Google Sign-in
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .build();

        // Initialization Firebase auth
        auth = FirebaseAuth.getInstance(); // Initializing Firebase Authentication
        // Initialization Firebase Database
        databases = FirebaseDatabase.getInstance(); // Initialize Firebase Realtime Database
        // Initialization Firebase Database
        database = FirebaseDatabase.getInstance().getReference(); // Initializing Firebase Realtime Database

        // Google Signin
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        // Set up Google Sign-in button
        AppCompatButton googleButton = findViewById(R.id.Googlebutton);
        googleButton.setOnClickListener(v -> {
            Intent signIntent = googleSignInClient.getSignInIntent();
            launcher.launch(signIntent);
        });

        // Finding ids of views
        mail = findViewById(R.id.emailLogin);
        pass = findViewById(R.id.passwordLogin);

        // Sending user to SignUp page for creating an account
        TextView txtView = findViewById(R.id.dontHaveAcc);
        txtView.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });


        // Set up Login button
        Button loginButton = findViewById(R.id.loginButon);
        loginButton.setOnClickListener(v -> {

            // Converting into String and trimming
            email = mail.getText().toString().trim();
            password = pass.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                createUserAccount(email, password);
            }

        });
    }

    // Method to create or sign in user account
    private void createUserAccount(String email, String password) {

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {

            if (task.isSuccessful()) {
                FirebaseUser user = auth.getCurrentUser();
                Toast.makeText(this, "Login successfully", Toast.LENGTH_SHORT).show();
                updateUi(user);
            } else {
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        Toast.makeText(this, "New account created successfully", Toast.LENGTH_SHORT).show();
                        saveUserData(); // Save user data to Firebase Realtime Database
                        initializeDataInFirebase();
                        updateUi(user);
                    } else {
                        Toast.makeText(this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                        Log.d("Account", "createUserAccount: Authentication Failed", task.getException());
                    }
                });
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

            if(check.get()){
                Toast.makeText(this, "Successfully set all the data", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, "Failed! Please set data manually", Toast.LENGTH_SHORT).show();
            }

        }else {
            Log.e("saveUserData", "Error in grading first");
        }
    }

    // Method to save user data to Firebase Realtime Database
    private void saveUserData() {

        // Converting into String and trimming
        email = mail.getText().toString().trim();
        password = pass.getText().toString().trim();

        // Create a UserModel with user data
        String userName = "guest";
        String degree = "set value";
        String rollNo = "Not set";
        int semesters = 8;

        HashMap<String, Object> map = new HashMap<>();
        map.put("name", userName);
        map.put("degree", degree);
        map.put("rollNo", rollNo);
        map.put("semesters", semesters);
        map.put("email", email);
        map.put("password", password);

        // Get current authenticated user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Save user data under the "user" node in the database
        if (currentUser != null) {
            String userId = currentUser.getUid();
            database.child("user").child(userId).child("userInfo").setValue(map);
        }
    }

    // Google Sign-in result launcher
    private ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {

                if (result.getResultCode() == Activity.RESULT_OK) {

                    // Get the signed-in account from the GoogleSignInClient
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());

                    try {

                        // Attempt to handle the sign-in result
                        GoogleSignInAccount account = task.getResult(ApiException.class);

                        if (account != null) {
                            // Get Google Sign-in credentials
                            AuthCredential credentials = GoogleAuthProvider.getCredential(account.getIdToken(), null);

                            // Sign in with Firebase using Google credentials
                            auth.signInWithCredential(credentials).addOnCompleteListener(authTask -> {
                                if (authTask.isSuccessful()) {
                                    // Handle successful sign-in with Google
                                    Toast.makeText(this, "Successfully sign-in with Google", Toast.LENGTH_SHORT).show();
                                    updateUi(authTask.isSuccessful() ? authTask.getResult().getUser() : null);
                                    finish();
                                } else {
                                    // Handle failed sign-in with Google
                                    Toast.makeText(this, "Failed sign-in with Google", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                    } catch (ApiException e) {
                        // Handle Google sign-in failure
                        Log.w("GoogleSignIn", "Google sign-in failed", e);
                        Toast.makeText(this, "Failed sign-in with Google", Toast.LENGTH_SHORT).show();
                    }

                }
            });

    @Override
    protected void onStart() {
        super.onStart();

        // Check if a user is already authenticated
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            // If authenticated, go to the main activity and finish the current one
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

    }

    // Method to update UI after successful login or account creation
    private void updateUi(FirebaseUser user) {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

}
