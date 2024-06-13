package com.example.gpacalculator.byahmadalikhan.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.gpacalculator.byahmadalikhan.R;
import com.example.gpacalculator.byahmadalikhan.adapter.AddOverallSubjectMarksAdapter;
import com.example.gpacalculator.byahmadalikhan.databinding.FragmentSubjectTotalBinding;
import com.example.gpacalculator.byahmadalikhan.model.GradingDetailsModel;
import com.example.gpacalculator.byahmadalikhan.model.SubjectTotalDetailsModel;
import com.example.gpacalculator.byahmadalikhan.model.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SubjectTotalFragment extends Fragment {

    private FragmentSubjectTotalBinding binding;

    // User-related information
    private String userId;
    private FirebaseAuth auth;

    // Firebase Database
    private FirebaseDatabase database;

    AddOverallSubjectMarksAdapter addOverallSubjectMarksAdapter;

    String subjectNames1;
    String semester;
    String creditHour;
    String totalMarks;
    String obtainedMarks;
    String totalWeightage;
    String obtainedWeightage;
    String subjectType;

    String fetchedSemester = "0";

    private final ArrayList<Integer> semesters = new ArrayList<>();
    private final ArrayList<String> subjectNames = new ArrayList<>();

    // Default user details
    String userName = null;
    String degree = null;
    String email = null;
    String password = null;
    int noOfSemesters = -1;

    // Adapters for UI components
    private ArrayAdapter<String> subjectNameAdapter;
    private ArrayAdapter<Integer> semesterAdapter;
    private ArrayAdapter<CharSequence> subjectTypeAdapter;

    private final ArrayList<SubjectTotalDetailsModel> subjectTotalDetailModels = new ArrayList<>();

    public SubjectTotalFragment() {
        // Required empty public constructor
    }


    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSubjectTotalBinding.inflate(getLayoutInflater(), container, false);

        // Initialize Firebase authentication
        auth = FirebaseAuth.getInstance();
        // Initialize Firebase Realtime Database
        database = FirebaseDatabase.getInstance();

        // Fetch and set up semester information
        fetchAndSetSemester();

        // Initialize adapters
        initializeAdapters();

        // TextWatcher for AutoCompleteTextView
        binding.subjectNameAutoCompleteText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                // Check for existing subject and fetch additional details
                fetchSubjectNames();
                String subject = binding.subjectNameAutoCompleteText.getText().toString().trim();
                // Update the suggestions based on the user input
                subjectNameAdapter.getFilter().filter(subject);
                if (subjectNames.contains(subject)) {
                    fetchDetailsBySubjectName(subject);
                } else {
                    binding.creditHourText.setText("");
                    binding.semesterSpinnerText.setSelection(0);
                    binding.subjectTypeSpinnerText.setSelection(0);
                    binding.totalWeightageText.setText("");
                    binding.totalMarkText.setText("");
                    binding.obtainedWeightageText.setText("");
                    binding.obtainedMarksText.setText("");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.saveSubjectTotalInfoButton.setOnClickListener(v -> {

            if (binding.subjectNameAutoCompleteText.getText().toString().trim().isEmpty()) {

                Toast.makeText(requireContext(), "Please enter subject name", Toast.LENGTH_SHORT).show();

            } else if (binding.semesterSpinnerText.getSelectedItem() == null || binding.semesterSpinnerText.getSelectedItem().toString().trim().isEmpty()) {

                Toast.makeText(requireContext(), "Please select semester no", Toast.LENGTH_SHORT).show();

            } else if (binding.creditHourText.getText().toString().trim().isEmpty() || Integer.parseInt(binding.creditHourText.getText().toString().trim()) < 1) {

                Toast.makeText(requireContext(), "Please enter valid value in credit hour", Toast.LENGTH_SHORT).show();

            } else if (binding.totalWeightageText.getText().toString().trim().isEmpty()) {

                Toast.makeText(requireContext(), "Please enter value in total weightage", Toast.LENGTH_SHORT).show();

            } else if (Double.parseDouble(binding.totalWeightageText.getText().toString().trim()) <= 0) {

                Toast.makeText(requireContext(), "Please enter correct value in total weightage", Toast.LENGTH_SHORT).show();

            } else if (binding.obtainedWeightageText.getText().toString().trim().isEmpty()) {

                Toast.makeText(requireContext(), "Please enter value in obtained weightage", Toast.LENGTH_SHORT).show();

            } else if (Double.parseDouble(binding.obtainedWeightageText.getText().toString().trim()) > Double.parseDouble(binding.totalWeightageText.getText().toString().trim())) {

                Toast.makeText(requireContext(), "Obtained weightage can't be greater than Total weightage", Toast.LENGTH_SHORT).show();

            } else if (binding.subjectTypeSpinnerText.getSelectedItem() == null || binding.subjectTypeSpinnerText.getSelectedItem().toString().trim().isEmpty()) {

                Toast.makeText(requireContext(), "Please select subject type", Toast.LENGTH_SHORT).show();

            } else {

                binding.saveSubjectTotalInfoButton.setEnabled(false); // Disable button to prevent multiple clicks
                binding.saveSubjectTotalInfoButton.post(() -> binding.saveSubjectTotalInfoButton.setText("Saving Data.."));

                subjectNames1 = binding.subjectNameAutoCompleteText.getText().toString().trim();
                semester = binding.semesterSpinnerText.getSelectedItem().toString().trim();
                creditHour = binding.creditHourText.getText().toString().trim();
                totalMarks = binding.totalMarkText.getText().toString().trim();
                obtainedMarks = binding.obtainedMarksText.getText().toString().trim();
                totalWeightage = binding.totalWeightageText.getText().toString().trim();
                obtainedWeightage = binding.obtainedWeightageText.getText().toString().trim();
                subjectType = binding.subjectTypeSpinnerText.getSelectedItem().toString().trim();

                double tm, om, tw, ow;

                if (totalMarks.isEmpty()) {
                    tm = 0.0;
                } else {
                    tm = Double.parseDouble(totalMarks);
                }
                if (obtainedMarks.isEmpty()) {
                    om = 0.0;
                } else {
                    om = Double.parseDouble(obtainedMarks);
                }

                tw = Double.parseDouble(totalWeightage);
                ow = Double.parseDouble(obtainedWeightage);

                if (binding.totalMarkText.getText().toString().trim().isEmpty()) {
                    tm = tw;
                }
                if (binding.obtainedMarksText.getText().toString().trim().isEmpty()) {
                    om = ow;
                }


                saveDataInFirebase(subjectNames1, creditHour, om, ow, semester, subjectType, tm, tw);


            }


        });

        // Subject type spinner adapter
        subjectTypeAdapter = ArrayAdapter.createFromResource(requireContext(), R.array.subject_type, android.R.layout.simple_spinner_item);
        subjectTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.subjectTypeSpinnerText.setAdapter(subjectTypeAdapter);

        retrieveAndDisplayAllSubjectDetails();

        return binding.getRoot();
    }

    private void initializeAdapters() {
        subjectNameAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, subjectNames);
        binding.subjectNameAutoCompleteText.setThreshold(1);
        binding.subjectNameAutoCompleteText.setAdapter(subjectNameAdapter);

        subjectTypeAdapter = ArrayAdapter.createFromResource(requireContext(), R.array.subject_type, android.R.layout.simple_spinner_item);
        subjectTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.subjectTypeSpinnerText.setAdapter(subjectTypeAdapter);

        // Initialize the semester adapter if not already done
        semesterAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, semesters);
        semesterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.semesterSpinnerText.setAdapter(semesterAdapter);
    }

    private void saveDataInFirebase(String subjectName1, String creditHour, double om, double ow, String semester, String subjectType, double tm, double tw) {

        DatabaseReference subAssessmentRef = database.getReference().
                child("user").
                child(userId).
                child("subjectsDataSemesterVise").
                child("semester" + semester).
                child(subjectName1).
                child("assessmentDetails");


        if (!subjectNames.contains(subjectName1)) {

            if (userId != null) {
                dataSaveFunction(ow, tw, tm, om, semester, subjectType, creditHour, subjectName1);
            }

        } else {
            subAssessmentRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if (snapshot.hasChildren()) {

                        Dialog deleteConfirmationDialog = new Dialog(requireContext());
                        deleteConfirmationDialog.setContentView(R.layout.custom_popup);

                        TextView mainHeading = deleteConfirmationDialog.findViewById(R.id.textView5);
                        TextView nameOfSubject = deleteConfirmationDialog.findViewById(R.id.textView11);
                        TextView description = deleteConfirmationDialog.findViewById(R.id.textView7);


                        String main = "Previous assessment's data like Quizzes, Assignments, Mid or Final\n" +
                                "are also found linked with this subject\n" + subjectName1.toUpperCase() + " ";
                        String desc = "Tapping YES will override previous linked data and will result in miscalculation of marks.\n " +
                                "You may have to entered all data again from start.";


                        if (mainHeading != null) {
                            mainHeading.setText(main);
                        }

                        if (nameOfSubject != null) {
                            nameOfSubject.setText("DO YOU WANT TO CONTINUE?");
                        }

                        if (description != null) {
                            description.setText(desc);
                        }

                        AppCompatButton btnYes = deleteConfirmationDialog.findViewById(R.id.btnYes);
                        if (btnYes != null) {
                            btnYes.setOnClickListener(v -> {
                                // Handle "Yes" button click
                                // Perform delete operation or any other action
                                deleteConfirmationDialog.dismiss(); // Close the popup
                                dataSaveFunction(ow, tw, tm, om, semester, subjectType, creditHour, subjectName1);
                            });
                        }

                        // Set click listener for No button
                        AppCompatButton btnNo = deleteConfirmationDialog.findViewById(R.id.btnNo);
                        if (btnNo != null) {
                            btnNo.setOnClickListener(v -> {
                                // Handle "No" button click
                                deleteConfirmationDialog.dismiss(); // Close the popup
                            });
                        }

                        // Show the custom dialog
                        deleteConfirmationDialog.show();


                    } else {
                        dataSaveFunction(ow, tw, tm, om, semester, subjectType, creditHour, subjectName1);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }


    }

    private void dataSaveFunction(double ow, double tw, double tm, double om, String semester, String subjectType, String creditHour, String subjectName2) {


        DatabaseReference gradeRef = database.getReference().child("user").child(userId).child("gradeInformation");

        DatabaseReference subInfoRef = database.getReference().
                child("user").
                child(userId).
                child("subjectsDataSemesterVise").
                child("semester" + semester).
                child(subjectName2).
                child("subjectInfo");


        int percentage = (int) ((ow / tw) * 100);


        if (!fetchedSemester.equals(semester)) {


            DatabaseReference sourceRef = FirebaseDatabase.getInstance().getReference().child("user").child(userId).child("subjectsDataSemesterVise").child("semester" + fetchedSemester).child(subjectName2);
            DatabaseReference destinationRef = FirebaseDatabase.getInstance().getReference().child("user").child(userId).child("subjectsDataSemesterVise").child("semester" + semester).child(subjectName2);

            // Step 1: Read the Data
            sourceRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // Step 2: Write the Data to the Destination Node
                        destinationRef.setValue(snapshot.getValue())
                                .addOnSuccessListener(aVoid -> {
                                    // Successfully moved data to the destination node

                                    // Step 3: (Optional) Delete the Original Data
                                    sourceRef.removeValue()
                                            .addOnSuccessListener(aVoid1 -> {
                                                // Successfully removed data from the source node
                                                fetchedSemester = semester;
                                            })
                                            .addOnFailureListener(e -> {
                                                // Handle failure to remove data
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    // Handle failure to write data to the destination node
                                });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle onCancelled event
                }
            });
        }

        gradeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    if (snapshot.exists()) {
                        GradingDetailsModel details = snapshot1.getValue(GradingDetailsModel.class);

                        if (details != null) {
                            double maxValue = Double.parseDouble(details.getMaxValue());
                            double minValue = Double.parseDouble(details.getMinValue());

                            if (percentage <= maxValue && percentage >= minValue) {
                                String grade = details.getGrade();
                                String gradePoint = details.getGradePoints();

                                SubjectTotalDetailsModel subjectTotalDetailsModel = new SubjectTotalDetailsModel();

                                subjectTotalDetailsModel.setCreditHour(creditHour);
                                subjectTotalDetailsModel.setSubjectType(subjectType);
                                subjectTotalDetailsModel.setSemester(semester);
                                subjectTotalDetailsModel.setTw(String.valueOf(tw));
                                subjectTotalDetailsModel.setOw(String.valueOf(ow));
                                subjectTotalDetailsModel.setTm(String.valueOf(tm));
                                subjectTotalDetailsModel.setOm(String.valueOf(om));
                                subjectTotalDetailsModel.setGrade(grade);
                                subjectTotalDetailsModel.setGradePoint(gradePoint);


                                subInfoRef.setValue(subjectTotalDetailsModel).addOnSuccessListener(unused -> {
                                    Toast.makeText(requireContext(), "Data added successfully", Toast.LENGTH_SHORT).show();
                                    binding.saveSubjectTotalInfoButton.post(() -> {
                                        binding.saveSubjectTotalInfoButton.setText("Save Subject Info");
                                        binding.saveSubjectTotalInfoButton.setEnabled(true); // Re-enable button after saving
                                    });
                                }).addOnFailureListener(e -> {
                                    Toast.makeText(requireContext(), "Failed to add data", Toast.LENGTH_SHORT).show();
                                    binding.saveSubjectTotalInfoButton.post(() -> {
                                        binding.saveSubjectTotalInfoButton.setText("Save Subject Info");
                                        binding.saveSubjectTotalInfoButton.setEnabled(true); // Re-enable button after failure
                                    });
                                });


                                break;
                            }

                        }
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });

    }

    // Fetch subject type and credit hour from Firebase based on selected subject
    private void fetchDetailsBySubjectName(String subjectName2) {


        userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();

        if (noOfSemesters != -1) {
            String semesterNo = binding.semesterSpinnerText.getSelectedItem().toString();
            subjectNames1 = subjectName2;
            semester = semesterNo;

            if (userId != null) {
                DatabaseReference subjectsRef = database.getReference().child("user").child(userId).child("subjectsDataSemesterVise");

                subjectsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        fetchSubjectNames();
                        for (DataSnapshot semesterSnapshot : dataSnapshot.getChildren()) {
                            String semester = semesterSnapshot.getKey();

                            for (DataSnapshot subjectSnapshot : semesterSnapshot.getChildren()) {

                                String subjectName = subjectSnapshot.getKey();

                                assert subjectName != null;
                                if (subjectName.equals(subjectName2)) {


                                    Pattern pattern = Pattern.compile("\\d+");
                                    assert semester != null;
                                    Matcher matcher = pattern.matcher(semester);

                                    if (matcher.find()) {
                                        // Extract the matched numeric part
                                        String semesterNo = matcher.group();


                                        subjectsRef.child("semester" + semesterNo).child(subjectName).child("subjectInfo").addListenerForSingleValueEvent(new ValueEventListener() {

                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.exists()) {
                                                    Toast.makeText(requireContext(), "Existing Data Found", Toast.LENGTH_SHORT).show();

                                                    SubjectTotalDetailsModel subjectTotalDetailsModel1 = snapshot.getValue(SubjectTotalDetailsModel.class);

                                                    assert subjectTotalDetailsModel1 != null;


                                                    if (subjectTotalDetailsModel1.getTw() != null && !subjectTotalDetailsModel1.getTw().isEmpty()) {
                                                        binding.totalWeightageText.setText(subjectTotalDetailsModel1.getTw());
                                                    }
                                                    if (subjectTotalDetailsModel1.getOw() != null && !subjectTotalDetailsModel1.getOw().isEmpty()) {
                                                        binding.obtainedWeightageText.setText(subjectTotalDetailsModel1.getOw());
                                                    }
                                                    if (subjectTotalDetailsModel1.getTm() != null && !subjectTotalDetailsModel1.getTm().isEmpty()) {
                                                        binding.totalMarkText.setText(subjectTotalDetailsModel1.getTm());
                                                    }
                                                    if (subjectTotalDetailsModel1.getOm() != null && !subjectTotalDetailsModel1.getOm().isEmpty()) {
                                                        binding.obtainedMarksText.setText(subjectTotalDetailsModel1.getOm());
                                                    }
                                                    if (subjectTotalDetailsModel1.getCreditHour() != null && !subjectTotalDetailsModel1.getCreditHour().isEmpty()) {
                                                        binding.creditHourText.setText(subjectTotalDetailsModel1.getCreditHour());
                                                    }
                                                    if (subjectTotalDetailsModel1.getSemester() != null && !subjectTotalDetailsModel1.getSemester().isEmpty()) {
                                                        fetchedSemester = subjectTotalDetailsModel1.getSemester();
                                                        int index2 = getIndexForSemesterValue(subjectTotalDetailsModel1.getSemester());
                                                        if (index2 != -1) {
                                                            binding.semesterSpinnerText.setSelection(index2);
                                                        }
                                                    }


                                                    int index = getIndexForSubjectTypeValue(subjectType);
                                                    if (index != -1) {
                                                        binding.subjectTypeSpinnerText.setSelection(index);
                                                    }


                                                } else {
                                                    Toast.makeText(requireContext(), "Existing Data Not Found", Toast.LENGTH_SHORT).show();
                                                    fetchedSemester = "0";
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });

                                    }
                                    retrieveAndDisplayAllSubjectDetails();
                                }
                            }
                        }

                        // Update the AutoCompleteTextView adapter
                        subjectNameAdapter.notifyDataSetChanged();

                        // AutoCompleteTextView adapter
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("FetchSubjectNames", "Error fetching subject names: " + databaseError.getMessage());
                    }
                });

            }
        }

    }

    // Get index of a value in the subjectTypeAdapter
    private int getIndexForSubjectTypeValue(String value) {
        for (int i = 0; i < subjectTypeAdapter.getCount(); i++) {
            if (Objects.requireNonNull(subjectTypeAdapter.getItem(i)).toString().equals(value)) {
                return i;
            }
        }
        return -1; // Return -1 if value not found
    }

    // Get index of a value in the subjectTypeAdapter
    private int getIndexForSemesterValue(String value) {
        for (int i = 0; i < semesterAdapter.getCount(); i++) {
            if (Objects.requireNonNull(semesterAdapter.getItem(i)).toString().equals(value)) {
                return i;
            }
        }
        return -1; // Return -1 if value not found
    }

    private void retrieveAndDisplayAllSubjectDetails() {

        database = FirebaseDatabase.getInstance();
        userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();


        DatabaseReference subjectsRef = database.getReference().child("user").child(userId).child("subjectsDataSemesterVise");
        subjectsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                subjectTotalDetailModels.clear();
                for (DataSnapshot semesterSnapshot : dataSnapshot.getChildren()) {

                    String semester = semesterSnapshot.getKey();

                    for (DataSnapshot subjectSnapshot : semesterSnapshot.getChildren()) {

                        String subjectName = subjectSnapshot.getKey();

                        assert subjectName != null;
                        if (!subjectName.isEmpty()) {


                            Pattern pattern = Pattern.compile("\\d+");
                            assert semester != null;
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
                                            if (subjectTotalDetailsModel1.getTw() != null && subjectTotalDetailsModel1.getTw().isEmpty()) {
                                                subjectTotalDetailsModel1.setTw("Null");
                                            }
                                            if (subjectTotalDetailsModel1.getOw() != null && subjectTotalDetailsModel1.getOw().isEmpty()) {
                                                subjectTotalDetailsModel1.setOw("Null");
                                            }
                                            if (subjectTotalDetailsModel1.getGradePoint() != null && subjectTotalDetailsModel1.getGradePoint().isEmpty()) {
                                                subjectTotalDetailsModel1.setGradePoint("Null");
                                            }
                                            if (subjectTotalDetailsModel1.getGrade() != null && subjectTotalDetailsModel1.getGrade().isEmpty()) {
                                                subjectTotalDetailsModel1.setGrade("Null");
                                            }
                                            if (subjectTotalDetailsModel1.getCreditHour() != null && subjectTotalDetailsModel1.getCreditHour().isEmpty()) {
                                                subjectTotalDetailsModel1.setCreditHour("Null");
                                            } else {
                                                subjectTotalDetailsModel1.setSubjectName(subjectName);
                                                subjectTotalDetailModels.add(subjectTotalDetailsModel1);
                                            }


                                        } else {
                                            SubjectTotalDetailsModel subjectTotalDetailsModel2 = new SubjectTotalDetailsModel(subjectName, "none", "none", "none", "none", "none", "none", "none");
                                            subjectTotalDetailModels.add(subjectTotalDetailsModel2);
                                        }
                                        setSubjectDetailsAdapter(subjectTotalDetailModels);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                            }
                        } else {
                            Toast.makeText(requireContext(), "No details found!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FetchSubjectNames", "Error fetching subject names: " + databaseError.getMessage());
            }
        });

    }

    // Fetch and set up semester information from Firebase
    private void fetchAndSetSemester() {
        userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();


        DatabaseReference semesterRef = database.getReference().child("user").child(userId).child("userInfo");
        semesterRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Fetch user information

                    UserModel userModel = snapshot.getValue(UserModel.class);
                    assert userModel != null;

                    if (userModel.getSemesters() != null && userModel.getSemesters() != 0) {
                        noOfSemesters = userModel.getSemesters();
                        // Populate semester Spinner with appropriate values
                        if (noOfSemesters != -1) {
                            for (int i = 1; i <= noOfSemesters; i++) {
                                semesters.add(i);
                            }
                        }

                        if (userModel.getName() != null && userModel.getName().isEmpty()) {
                            userName = userModel.getName().trim();
                        }
                        if (userModel.getDegree() != null && userModel.getDegree().isEmpty()) {
                            degree = userModel.getDegree().trim();
                        }
                        if (userModel.getEmail() != null && userModel.getEmail().isEmpty()) {
                            email = userModel.getEmail().trim();
                        }
                        if (userModel.getPassword() != null && userModel.getPassword().isEmpty()) {
                            password = userModel.getPassword().trim();

                        }


                        Spinner semesterSpinner = binding.semesterSpinnerText;
                        semesterAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, semesters);
                        semesterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        semesterSpinner.setAdapter(semesterAdapter);
                    }
                } else {
                    Toast.makeText(requireContext(), "Kindly set no of semesters in 'Add or Edit Details' to get started", Toast.LENGTH_SHORT).show();
                }
                fetchSubjectNames();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle onCancelled if needed
            }
        });
    }

    // Fetch subject names from Firebase
    private void fetchSubjectNames() {
        userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();


        DatabaseReference subjectsRef = database.getReference().child("user").child(userId).child("subjectsDataSemesterVise");
        subjectsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                subjectNames.clear();
                for (DataSnapshot semesterSnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot subjectSnapshot : semesterSnapshot.getChildren()) {
                        String subjectName = subjectSnapshot.getKey();
                        if (subjectName != null) {
                            subjectNames.add(subjectName);
                        }
                    }
                }

                // Notify adapter of data changes
                subjectNameAdapter.notifyDataSetChanged();

                // AutoCompleteTextView adapter
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FetchSubjectNames", "Error fetching subject names: " + databaseError.getMessage());
            }
        });
    }


    // Set the adapter for subject details RecyclerView
    private void setSubjectDetailsAdapter(ArrayList<SubjectTotalDetailsModel> subjectDetails) {


        addOverallSubjectMarksAdapter = new AddOverallSubjectMarksAdapter(requireContext(), subjectDetails);
        binding.detailsRecyclerForAllSubjectDetails.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.detailsRecyclerForAllSubjectDetails.setAdapter(addOverallSubjectMarksAdapter);

        int noOfItems = Math.min(subjectDetails.size(), 9);
        // Set the height to show only 5 items
        int itemHeight = getResources().getDimensionPixelSize(R.dimen.small_item_height);// Replace with your actual item height
        binding.detailsRecyclerForAllSubjectDetails.getLayoutParams().height = noOfItems * itemHeight;

    }
}