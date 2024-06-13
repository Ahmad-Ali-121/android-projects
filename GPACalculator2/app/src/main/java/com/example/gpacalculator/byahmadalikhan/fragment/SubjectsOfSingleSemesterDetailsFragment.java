package com.example.gpacalculator.byahmadalikhan.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.gpacalculator.byahmadalikhan.R;
import com.example.gpacalculator.byahmadalikhan.adapter.SemesterViseResultAdapter;
import com.example.gpacalculator.byahmadalikhan.databinding.FragmentSemesterDetailsBinding;
import com.example.gpacalculator.byahmadalikhan.model.SubjectTotalDetailsModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;


public class SubjectsOfSingleSemesterDetailsFragment extends Fragment {

    private FragmentSemesterDetailsBinding binding;

    private FirebaseAuth auth;

    private final ArrayList<SubjectTotalDetailsModel> subjectDetailsArrayList = new ArrayList<>();
    private final ArrayList<String> creditHourArrayList = new ArrayList<>();

    // Firebase Database
    private FirebaseDatabase database;


    public SubjectsOfSingleSemesterDetailsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSemesterDetailsBinding.inflate(getLayoutInflater(), container, false);

        // Initialize Firebase authentication
        auth = FirebaseAuth.getInstance();

        // Initialize Firebase Realtime Database
        database = FirebaseDatabase.getInstance();

        // Set a listener to be notified of changes in query text
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Handle the query text submission (e.g., perform search)
                if (isNumeric(query)) {
                    binding.textView4.setText("Searching please wait..");
                    retrieveAndDisplaySubjectDetails(query);
                } else {
                    binding.textView4.setText("Enter numeric values\n like 1,2");
                }

                return true;  // Return true to indicate that the query has been handled
            }

            @SuppressLint("SetTextI18n")
            @Override
            public boolean onQueryTextChange(String newText) {
                // Handle changes in the query text (e.g., filter results dynamically)
                if (isNumeric(newText)) {
                    binding.textView4.setText("Searching please wait..");
                    retrieveAndDisplaySubjectDetails(newText);
                } else {
                    binding.textView4.setText("Enter numeric values\n like 1,2");
                }
                return true;  // Return true to indicate that the query change has been handled
            }
        });

        return binding.getRoot();
    }

    public boolean isNumeric(String text) {
        // Regular expression to match only numeric characters
        String numericRegex = "[0-9]+";

        // Return true if the text matches the numeric regex, false otherwise
        return text.matches(numericRegex);
    }

    // Retrieve and display subject details from Firebase
    private void retrieveAndDisplaySubjectDetails(String semester1) {

        database = FirebaseDatabase.getInstance();
        // User-related information
        String userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();

        DatabaseReference semesterRef = database.getReference().
                child("user").
                child(userId).
                child("subjectsDataSemesterVise").
                child("semester" + semester1);

        final double[] totalMarks = {0};
        final double[] obtainedMarks = {0};
        String grade = ".";
        final double[] gradePoints = {0};
        final double[] customCreditHour = {0};
        final double[] percentage = {0};

        // Step 3: Check if data exists in the specified semester node
        semesterRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                subjectDetailsArrayList.clear();
                creditHourArrayList.clear();


                if (dataSnapshot.exists()) {
                    // Data exists in the specified semester node

                    // Loop through subjectName nodes
                    for (DataSnapshot subjectSnapshot : dataSnapshot.getChildren()) {
                        String subjectName = subjectSnapshot.getKey();

                        SubjectTotalDetailsModel subjectInfoData = new SubjectTotalDetailsModel();
                        String creditHour;

                        subjectInfoData.setSubjectName(subjectName);

                        // Access subjectInfo node
                        DataSnapshot subjectInfoSnapshot = subjectSnapshot.child("subjectInfo");
                        DataSnapshot creditHourSnapshot = subjectInfoSnapshot.child("creditHour");


                        if (subjectInfoSnapshot.exists()) {
                            // Now you can work with subjectInfo data
                            subjectInfoData = subjectInfoSnapshot.getValue(SubjectTotalDetailsModel.class);

                            if (creditHourSnapshot.exists() && creditHourSnapshot.getValue() != null && subjectInfoData != null) {
                                // Data exists in the specified node, and creditHourSnapshot.getValue() is not null
                                creditHour = creditHourSnapshot.getValue(String.class);

                                if (subjectInfoData.getTw() == null || subjectInfoData.getOw() == null || subjectInfoData.getCreditHour() == null ||
                                        subjectInfoData.getTw().isEmpty() || subjectInfoData.getOw().isEmpty() || subjectInfoData.getCreditHour().isEmpty()) {

                                    totalMarks[0] = totalMarks[0] + 0;
                                    obtainedMarks[0] = obtainedMarks[0] + 0;
                                    customCreditHour[0] = customCreditHour[0] + 0;
                                    gradePoints[0] = gradePoints[0] + 0;

                                } else {
                                    totalMarks[0] = totalMarks[0] + Double.parseDouble(subjectInfoData.getTw());
                                    obtainedMarks[0] = obtainedMarks[0] + Double.parseDouble(subjectInfoData.getOw());
                                    customCreditHour[0] = customCreditHour[0] + Double.parseDouble(subjectInfoData.getCreditHour());
                                    gradePoints[0] = gradePoints[0] + (Double.parseDouble(subjectInfoData.getCreditHour()) * Double.parseDouble(subjectInfoData.getGradePoint()));


                                }


                            } else {
                                creditHour = "";
                                totalMarks[0] = totalMarks[0] + 0;
                                obtainedMarks[0] = obtainedMarks[0] + 0;
                                customCreditHour[0] = customCreditHour[0] + 0;
                                gradePoints[0] = gradePoints[0] + 0;
                            }
                            assert subjectInfoData != null;
                            subjectInfoData.setSubjectName(subjectName);

                        } else {
                            subjectInfoData.setSubjectName(subjectName);
                            subjectInfoData.setGrade("Null");
                            subjectInfoData.setGradePoint("Null");
                            subjectInfoData.setTw("Null");
                            subjectInfoData.setOw("Null");
                            creditHour = "";
                        }
                        subjectDetailsArrayList.add(subjectInfoData);
                        creditHourArrayList.add(creditHour);
                    }

                    binding.textView4.setText(String.format("Showing Data Of Semester No: %s", semester1));
                    // Set the adapter with new data
                    setSubjectDetailsAdapter();

                    if (gradePoints[0] != 0) {
                        gradePoints[0] = gradePoints[0] / customCreditHour[0];
                    }

                    if (totalMarks[0] == 0) {
                        percentage[0] = 0;
                    } else {
                        percentage[0] = (obtainedMarks[0] * 100) / totalMarks[0];
                    }


                    addCustomData(String.valueOf(customCreditHour[0]), String.format("%.2f", totalMarks[0]), String.format("%.2f", obtainedMarks[0]), grade, String.format("%.2f", gradePoints[0]), String.format("%.2f", percentage[0]));

                } else {
                    // Clear existing data when no data is found
                    subjectDetailsArrayList.clear();
                    creditHourArrayList.clear();

                    // Notify the adapter about data change
                    setSubjectDetailsAdapter();

                    // Clear custom data
                    addCustomData("0", "0", "0", "N/A", "0", "0");
                    binding.textView4.setText(String.format("Cannot Find Data Of Semester No: %s", semester1));
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
            }
        });
    }

    // Set the adapter for subject details RecyclerView
    private void setSubjectDetailsAdapter() {


        SemesterViseResultAdapter addSubjectDetailsAdapter = new SemesterViseResultAdapter(requireContext(), subjectDetailsArrayList, creditHourArrayList, 1);
        binding.semesterDetailsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.semesterDetailsRecyclerView.setAdapter(addSubjectDetailsAdapter);

        int noOfItems = Math.min(subjectDetailsArrayList.size(), 8);
        // Set the height to show only 5 items
        int itemHeight = getResources().getDimensionPixelSize(R.dimen.small_item_height); // Replace with your actual item height
        binding.semesterDetailsRecyclerView.getLayoutParams().height = noOfItems * itemHeight;

    }

    // Method to add custom data
    private void addCustomData(String customCreditHour, String totalMarks, String obtainedMarks, String grade, String gradePoints, String percentage) {


        SemesterViseResultAdapter addSubjectDetailsAdapter = new SemesterViseResultAdapter(requireContext(), customCreditHour, totalMarks, obtainedMarks, grade, gradePoints, percentage, 2);
        binding.totalDetailsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.totalDetailsRecyclerView.setAdapter(addSubjectDetailsAdapter);


    }

}