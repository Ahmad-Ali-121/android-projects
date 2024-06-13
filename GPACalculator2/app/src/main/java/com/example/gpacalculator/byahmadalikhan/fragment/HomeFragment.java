package com.example.gpacalculator.byahmadalikhan.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.gpacalculator.byahmadalikhan.R;
import com.example.gpacalculator.byahmadalikhan.adapter.HomeAdapter;
import com.example.gpacalculator.byahmadalikhan.databinding.FragmentHomeBinding;
import com.example.gpacalculator.byahmadalikhan.model.FinalResultModel;
import com.example.gpacalculator.byahmadalikhan.model.SubjectTotalDetailsModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class HomeFragment extends Fragment {

    public HomeFragment() {
        // Required empty public constructor
    }

    ArrayList<FinalResultModel> finalResultModels = new ArrayList<>();
    // User-related information
    private String userId;

    // Firebase Database
    private FirebaseDatabase database;

    double creditHours = 0;
    double percentage = 0;
    double totalMarks = 0;
    double obtainedMarks = 0;
    double SGPA = 0;
    double CGPA = 0;
    final boolean[] forCheck = {true};
    ArrayList<Double> sgpaList = new ArrayList<>();

    boolean check = true;
    private ValueEventListener subjectsListener;
    private boolean isListenerAttached = false;
    FragmentHomeBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(getLayoutInflater(), container, false);

        // Initialize Firebase authentication
        FirebaseAuth auth = FirebaseAuth.getInstance();
        // Initialize Firebase Realtime Database
        database = FirebaseDatabase.getInstance();
        userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();


        Set<String> seenSemesters = new HashSet<>();


        // Ensure listener is only attached once
        if (!isListenerAttached) {
            attachSubjectsListener(seenSemesters);
            isListenerAttached = true;
        }

        binding.addDetails.setOnClickListener(v -> {
            // Replace the current fragment with AddDetailsFragment
            AddGradingDetailsFragment addGradingDetailsFragment = new AddGradingDetailsFragment();
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.container, addGradingDetailsFragment);
            transaction.addToBackStack(null); // Optional: Add to back stack for back navigation
            transaction.commit();
        });

        return binding.getRoot();
    }


    private void attachSubjectsListener(Set<String> seenSemesters) {
        if (userId != null) {
            DatabaseReference subjectsRef = database.getReference().child("user").child(userId).child("subjectsDataSemesterVise");

            finalResultModels.clear();

            subjectsListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot semesterSnapshot : dataSnapshot.getChildren()) {
                        String semester = semesterSnapshot.getKey();

                        // Ensure each semester is processed only once
                        if (seenSemesters.add(semester)) {

                            List<String> subjectNamesList = new ArrayList<>();
                            for (DataSnapshot subjectSnapshot : semesterSnapshot.getChildren()) {
                                String subjectName = subjectSnapshot.getKey();
                                subjectNamesList.add(subjectName);
                            }
                            // Start processing subjects for the current semester
                            processSubjects(subjectsRef, semester, subjectNamesList);
                        }
                    }


                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle possible errors
                }
            };

            subjectsRef.addListenerForSingleValueEvent(subjectsListener);
        }
    }


    private void processSubjects(DatabaseReference subjectsRef, String semester, @NonNull List<String> subjectlist) {



        final boolean[] dataFetch = {false};
        final int[] index = {0};

        for (String subjectName : subjectlist) {

            assert subjectName != null;

            Pattern pattern = Pattern.compile("\\d+");
            Matcher matcher = pattern.matcher(semester);

            if (matcher.find()) {
                // Extract the matched numeric part
                String semesterNo = matcher.group();

                subjectsRef.child("semester" + semesterNo).child(subjectName).child("subjectInfo").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            SubjectTotalDetailsModel subjectTotalDetailsModel1 = snapshot.getValue(SubjectTotalDetailsModel.class);

                            assert subjectTotalDetailsModel1 != null;

                            if (subjectTotalDetailsModel1.getCreditHour() != null && !subjectTotalDetailsModel1.getCreditHour().isEmpty()) {
                                creditHours = creditHours + Double.parseDouble(subjectTotalDetailsModel1.getCreditHour());
                                if (subjectTotalDetailsModel1.getGradePoint() != null && !subjectTotalDetailsModel1.getGradePoint().isEmpty()) {
                                    SGPA = SGPA + (Double.parseDouble(subjectTotalDetailsModel1.getCreditHour()) * Double.parseDouble(subjectTotalDetailsModel1.getGradePoint()));
                                } else {
                                    SGPA = SGPA + 0;
                                    creditHours = creditHours + 0;
                                    check = false;
                                }
                            } else {
                                creditHours = creditHours + 0;
                                SGPA = SGPA + 0;
                                check = false;
                            }

                            if (subjectTotalDetailsModel1.getTm() != null &&
                                    subjectTotalDetailsModel1.getOm() != null &&
                                    !subjectTotalDetailsModel1.getTm().isEmpty() &&
                                    !subjectTotalDetailsModel1.getOm().isEmpty()) {
                                totalMarks = totalMarks + Double.parseDouble(subjectTotalDetailsModel1.getTm());
                                obtainedMarks = obtainedMarks + Double.parseDouble(subjectTotalDetailsModel1.getOm());
                            } else {
                                totalMarks = totalMarks + 0;
                                obtainedMarks = obtainedMarks + 0;
                                check = false;
                            }
                            index[0] = index[0] + 1;

                            if (forCheck[0] && index[0] >= subjectlist.size()) {

                                SGPA = SGPA/creditHours;
                                sgpaList.add(SGPA);
                                percentage = percentage + ((obtainedMarks * 100) / totalMarks);

                                double sum = 0.0;
                                for (Double sgpa : sgpaList) {
                                    if (sgpa != null) {
                                        sum += sgpa;
                                    }
                                }

                                CGPA = sum / sgpaList.size();

                                FinalResultModel model = new FinalResultModel(semesterNo, String.valueOf(creditHours), String.valueOf(SGPA), String.valueOf(CGPA)
                                        , String.valueOf(percentage), check);

                                finalResultModels.add(model);

                                creditHours = 0;
                                percentage = 0;
                                totalMarks = 0;
                                obtainedMarks = 0;
                                SGPA = 0;
                                CGPA = 0;
                                forCheck[0] = true;

                                HomeAdapter adapter = new HomeAdapter(requireContext(), finalResultModels);
                                binding.homeRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
                                binding.homeRecycler.setAdapter(adapter);

                            }

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        forCheck[0] = false;
                        dataFetch[0] = false;
                    }
                });
            }
        }


    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        detachSubjectsListener();
    }

    private void detachSubjectsListener() {
        if (userId != null && subjectsListener != null) {
            DatabaseReference subjectsRef = database.getReference().child("user").child(userId).child("subjectsDataSemesterVise");
            subjectsRef.removeEventListener(subjectsListener);
            isListenerAttached = false;
        }
    }

}