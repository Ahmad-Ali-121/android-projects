package com.example.gpacalculator.byahmadalikhan.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.gpacalculator.byahmadalikhan.adapter.AddGradingDetailsAdapter;
import com.example.gpacalculator.byahmadalikhan.databinding.FragmentAddDetailsBinding;
import com.example.gpacalculator.byahmadalikhan.model.GradingDetailsModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;


public class AddGradingDetailsFragment extends Fragment {

    private FragmentAddDetailsBinding binding;
    private String userEmail;
    private String pass;

    // User ID and Firebase authentication
    private FirebaseDatabase database;
    private String userId;
    private FirebaseAuth auth;
    private final ArrayList<GradingDetailsModel> gradingDetailModels = new ArrayList<>();
    private Boolean check = false;


    public AddGradingDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAddDetailsBinding.inflate(getLayoutInflater(), container, false);

        // Initialize Firebase authentication
        auth = FirebaseAuth.getInstance();
        // Initialization Firebase Database
        database = FirebaseDatabase.getInstance(); // Initialize Firebase Realtime Database


        binding.userName.setEnabled(check);
        binding.noOfSemesters.setEnabled(check);
        binding.userDegree.setEnabled(check);

        binding.editDetails.setOnClickListener(v -> {

            check = !check;  // Update the value of check
            binding.userName.setEnabled(!check);
            binding.noOfSemesters.setEnabled(!check);
            binding.userDegree.setEnabled(!check);
        });

        binding.resetButton.setOnClickListener(v ->
                resetGradingDataInFirebase()
        );

        binding.floatingActionButton3.setOnClickListener(v -> {
            // Add a new row to your gradingDetails list
            GradingDetailsModel newDetail = new GradingDetailsModel("0", "0", "-", "0");
            gradingDetailModels.add(newDetail);
            // Save the new row to Firebase
            addNewRow(newDetail);
        });


        binding.saveInfoButtons.setOnClickListener(v -> {

            String name = binding.userName.getText().toString();
            String degree = binding.userDegree.getText().toString();
            String noOfSemesters = binding.noOfSemesters.getText().toString();

            if (!name.isEmpty() && !degree.isEmpty() && !noOfSemesters.isEmpty()) {

                if (noOfSemesters.matches("\\d+")) {
                    // The string contains only integers
                    int totalSemester = Integer.parseInt(noOfSemesters);

                    if (totalSemester > 1) {
                        saveUserData(name, degree, totalSemester, userEmail, pass);
                        saveGradingData();
                    } else {
                        Toast.makeText(requireContext(), "Please enter correct values in No of Semesters", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(requireContext(), "Please enter correct value in No of Semesters", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(requireContext(), "Please fill all fields!", Toast.LENGTH_SHORT).show();
            }

        });


        retrieveAndDisplayGradingDetails();


        // Inflate the layout for this fragment
        return binding.getRoot();
    }

    private void saveGradingData() {

        if (userId == null) {
            Log.e("SaveGradingData", "userId is null");
            return;
        }

        Log.e("SaveGradingData", "size" + gradingDetailModels.size());
        DatabaseReference gradingRef = database.getReference().child("user").child(userId).child("gradeInformation");

        // Remove all existing data from "gradeInformation" node
        gradingRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    // Successfully removed existing data, now add new data
                })
                .addOnFailureListener(e -> {
                    Log.e("SaveGradingData", "Error removing existing data: " + e.getMessage());
                    Toast.makeText(requireContext(), "Failed to save grading data", Toast.LENGTH_SHORT).show();
                });

        for (GradingDetailsModel detail : gradingDetailModels) {
            addNewRow(detail);
        }

        Log.e("SaveGradingData", "size" + gradingDetailModels.size());

    }


    private void addNewRow(GradingDetailsModel detail) {
        if (userId == null) {
            Log.e("AddNewRow", "userId is null");
            return;
        }

        DatabaseReference gradingRef = database.getReference().child("user").child(userId).child("gradeInformation");

        // Create a new entry with an auto-generated key
        String newEntryKey = gradingRef.push().getKey();

        // Save the detail to Firebase under the newEntryKey
        assert newEntryKey != null;
        gradingRef.child(newEntryKey).setValue(detail)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Get current authenticated user
                        Toast.makeText(requireContext(), "Data added successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e("AddNewRow", "Error adding new row: " + Objects.requireNonNull(task.getException()).getMessage());
                        Toast.makeText(requireContext(), "Failed to add new row", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void saveUserData(String name, String degree, int semesters, String email, String password) {

        if (userId != null) {
            DatabaseReference userDataRef = database.getReference().child("user").child(userId).child("userInfo");

            HashMap<String, Object> map = new HashMap<>();
            map.put("name", name);
            map.put("degree", degree);
            map.put("semesters", semesters);
            map.put("email", email);
            map.put("password", password);

            userDataRef.setValue(map, (error, ref) -> {
                if (error == null) {
                    // Data saved successfully
                    Toast.makeText(requireContext(), "User data saved successfully", Toast.LENGTH_SHORT).show();
                } else {
                    // An error occurred
                    Log.e("saveUserData", "Error saving user data: " + error.getMessage());
                }
            });
        } else {
            Log.e("saveUserData", "User ID is null");
        }

    }


    private void retrieveAndDisplayGradingDetails() {

        database = FirebaseDatabase.getInstance();
        userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();

        DatabaseReference userDataRef = database.getReference().child("user").child(userId).child("userInfo");

        userDataRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // User data found, retrieve and handle it
                    String name = dataSnapshot.child("name").getValue(String.class);
                    String degree = dataSnapshot.child("degree").getValue(String.class);
                    String email = dataSnapshot.child("email").getValue(String.class);
                    String password = dataSnapshot.child("password").getValue(String.class);
                    Long semestersLong = dataSnapshot.child("semesters").getValue(Long.class);
                    int semesters = semestersLong != null ? semestersLong.intValue() : 8;

                    userEmail = email;
                    pass = password;
                    binding.userName.setText(name);
                    binding.userDegree.setText(degree);
                    binding.noOfSemesters.setText(String.valueOf(semesters));

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors here
                Log.e("fetchUserData", "Error fetching user data: " + databaseError.getMessage());
            }
        });


        DatabaseReference gradingRef = database.getReference().child("user").child(userId).child("gradeInformation");

        gradingRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                gradingDetailModels.clear();
                boolean dataFound = false;
                // loop for through each food item
                for (DataSnapshot foodSnapshot : snapshot.getChildren()) {
                    GradingDetailsModel gradingDetailsModel1 = foodSnapshot.getValue(GradingDetailsModel.class);

                    if (gradingDetailsModel1 != null) {
                        dataFound = true;

                        binding.toChange.setText("Grading Data");
                        gradingDetailModels.add(gradingDetailsModel1);
                    }
                }

                // Check if any data was found
                if (dataFound) {
                    binding.toChange.setText("Grading Data");

                } else {

                    binding.toChange.setText("No Data To Show");
//                        Toast.makeText(requireContext(), "No Grading Data Found", Toast.LENGTH_SHORT).show();
                }

                setGradingDetailsAdapter(gradingDetailModels);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }


    private void setGradingDetailsAdapter(ArrayList<GradingDetailsModel> gradeDetails) {

        AddGradingDetailsAdapter addGradingDetailsAdapter = new AddGradingDetailsAdapter(requireContext(), gradeDetails);
        binding.addGradingDetailsRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.addGradingDetailsRecycler.setAdapter(addGradingDetailsAdapter);


    }

    private void resetGradingDataInFirebase() {

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


        if (userId == null) {
            Log.e("SaveGradingData", "userId is null");
            return;
        }

        DatabaseReference gradingRef = database.getReference().child("user").child(userId).child("gradeInformation");

        // Remove all existing data from "gradeInformation" node
        gradingRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    // Successfully removed existing data, now add new data
                    for (GradingDetailsModel detail : gradingDetailsModelArrayList) {
                        addNewRow(detail);
                    }
                    Toast.makeText(requireContext(), "Grading data reset successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("SaveGradingData", "Error removing existing data: " + e.getMessage());
                    Toast.makeText(requireContext(), "Failed to save grading data", Toast.LENGTH_SHORT).show();
                });
    }
}
