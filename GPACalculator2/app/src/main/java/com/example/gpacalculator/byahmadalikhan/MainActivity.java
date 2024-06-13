package com.example.gpacalculator.byahmadalikhan;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.gpacalculator.byahmadalikhan.fragment.AboutFragment;
import com.example.gpacalculator.byahmadalikhan.fragment.CalculateRequiredCgpaFragment;
import com.example.gpacalculator.byahmadalikhan.fragment.HomeFragment;
import com.example.gpacalculator.byahmadalikhan.fragment.SubjectsOfSingleSemesterDetailsFragment;
import com.example.gpacalculator.byahmadalikhan.fragment.SubjectDetailsFragment;
import com.example.gpacalculator.byahmadalikhan.fragment.SubjectTotalFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;

    // User-related information
    private String userId;

    // Firebase Database
    private FirebaseDatabase database;

    private FirebaseAuth mAuth;

    TextView nameTextView;

    private ConnectivityReceiver connectivityReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize Firebase Realtime Database
        database = FirebaseDatabase.getInstance();
        userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolbar);

        // Find the TextView by its ID in the navigation header
        nameTextView = navigationView.getHeaderView(0).findViewById(R.id.name);

        setUserName();

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.OpenDrawer,
                R.string.CloseDrawer
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        loadFragment(new HomeFragment());

        if (isConnectedToInternet()) {
            loadFragment(new HomeFragment());
        } else {
            // Display a dialog or navigate to a screen notifying the user to connect to the internet
            showNoInternetDialog();
        }


        navigationView.setNavigationItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.home) {
                loadFragment(new HomeFragment());
            } else if (id == R.id.subjectDetails) {
                loadFragment(new SubjectDetailsFragment());
            } else if (id == R.id.subjectDetailsTotalOnly) {
                loadFragment(new SubjectTotalFragment());
            } else if (id == R.id.semesterDetails) {
                loadFragment(new SubjectsOfSingleSemesterDetailsFragment());
            } else if (id == R.id.calculateRequiredCGPA) {
                loadFragment(new CalculateRequiredCgpaFragment());
            } else if (id == R.id.logout) {
                logout();
            } else if (id == R.id.about) {
                loadFragment(new AboutFragment());
            }

            drawerLayout.closeDrawer(GravityCompat.START);

            return true;
        });

        getOnBackPressedDispatcher().addCallback(this, callback);

        connectivityReceiver = new ConnectivityReceiver();
        registerReceiver(connectivityReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));


    }

    private void setUserName() {

        if (userId != null) {
            DatabaseReference userInfoRef = database.getReference().child("user").child(userId).child("userInfo");

           userInfoRef.addListenerForSingleValueEvent(new ValueEventListener() {
               @Override
               public void onDataChange(@NonNull DataSnapshot snapshot) {
                   if(snapshot.exists()){
                    String name = snapshot.child("name").getValue(String.class);
                    if(name != null){
                        nameTextView.setText(name);
                    }
                   }
               }

               @Override
               public void onCancelled(@NonNull DatabaseError error) {

               }
           });



        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(connectivityReceiver);
    }

    private class ConnectivityReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                if (!isConnectedToInternet()) {
                    // If internet is not available, show the dialog
                    showNoInternetDialog();
                }
            }
        }
    }

    OnBackPressedCallback callback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                // If the drawer is not open, call the super method
                finish();
            }
        }
    };


    private void loadFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        ft.add(R.id.container, fragment);
        ft.commit();
    }

    private void logout() {
        mAuth.signOut();
        Toast.makeText(MainActivity.this, "Logout successful", Toast.LENGTH_SHORT).show();
        finish(); // Close the current activity if needed
    }

    private boolean isConnectedToInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
        return false;
    }

    private void showNoInternetDialog() {
        // Display a dialog or navigate to another screen informing the user to connect to the internet
        // For example, you can use an AlertDialog to inform the user

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("No Internet Connection")
                .setMessage("Please connect to the internet and try again.")
                .setPositiveButton("TRY AGAIN", (dialog, which) -> {
                    // Check for internet connection again
                    if (isConnectedToInternet()) {
                        // If internet is available now, proceed with opening the app
                        loadFragment(new HomeFragment()); // Replace with your layout
                        // Your other initialization code here
                    } else {
                        // If still no internet, show the dialog again
                        showNoInternetDialog();
                    }
                })
                .setNegativeButton("EXIT", (dialog, which) -> finish()) // Close the app or navigate to another screen
                .setCancelable(false)
                .show();

        // Periodically check for internet connectivity
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isConnectedToInternet()) {
                    // If internet is available, dismiss the dialog
                    alertDialog.dismiss();
                } else {
                    // If still no internet, continue checking
                    handler.postDelayed(this, 1000); // Check again after 1 second
                }
            }
        }, 1000); // Initial check after 1 second
    }




}