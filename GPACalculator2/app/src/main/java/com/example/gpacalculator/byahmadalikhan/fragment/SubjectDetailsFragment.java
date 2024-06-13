package com.example.gpacalculator.byahmadalikhan.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.gpacalculator.byahmadalikhan.R;
import com.example.gpacalculator.byahmadalikhan.adapter.AddSubjectMarksInDetailAdapter;
import com.example.gpacalculator.byahmadalikhan.databinding.FragmentSubjectDetailsBinding;
import com.example.gpacalculator.byahmadalikhan.model.AssessmentDetailsModel;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SubjectDetailsFragment extends Fragment {

    private FragmentSubjectDetailsBinding binding;

    // User-related information
    private String userId;
    private FirebaseAuth auth;

    AddSubjectMarksInDetailAdapter addSubjectDetailsAdapter;

    // Subject details
    private final ArrayList<Integer> semesters = new ArrayList<>();
    private final ArrayList<String> subjectNames = new ArrayList<>();
    private final ArrayList<AssessmentDetailsModel> assessmentDetailsModelArrayList = new ArrayList<>();

    // Firebase Database
    private FirebaseDatabase database;

    // Default semester value
    // Default subject name
    String subjectName = null;

    // Default user details
    String userName = null;
    String degree = null;
    String email = null;
    String password = null;
    int noOfSemesters = -1;

    String grade = "--";
    double gradePoint = 0.0;

    String fetchedSemester;

    double totalMarks = 0.0;
    double totalWeightage = 0.0;
    double obtainedMarks = 0.0;
    double obtainedWeightage = 0.0;

    // Adapters for UI components
    private ArrayAdapter<String> subjectNameAdapter;
    private ArrayAdapter<Integer> semesterAdapter;
    private ArrayAdapter<CharSequence> subjectTypeAdapter;

    public SubjectDetailsFragment() {
        // Required empty public constructor
    }


    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSubjectDetailsBinding.inflate(getLayoutInflater(), container, false);

        // Initialize Firebase authentication
        auth = FirebaseAuth.getInstance();

        // Initialize Firebase Realtime Database
        database = FirebaseDatabase.getInstance();

        // Fetch and set up semester information
        fetchAndSetSemester();

        binding.rowCount.setText("Row Count\n=" + assessmentDetailsModelArrayList.size());

        // Initialize the adapter
        addSubjectDetailsAdapter = new AddSubjectMarksInDetailAdapter(requireContext(), assessmentDetailsModelArrayList, "1", "");
        binding.detailsRecyclerViewOfSubjectMarksDetails.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.detailsRecyclerViewOfSubjectMarksDetails.setAdapter(addSubjectDetailsAdapter);


        // Spinner listener for semester selection
        binding.semesterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                binding.semesterSpinner.getSelectedItem();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing here, or implement as needed
            }
        });


        // Subject type spinner adapter
        subjectTypeAdapter = ArrayAdapter.createFromResource(requireContext(), R.array.subject_type, android.R.layout.simple_spinner_item);
        subjectTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.subjectTypeSpinner.setAdapter(subjectTypeAdapter);

        // Button to add a new row of subject details
        binding.addNewRowOfSubject.setOnClickListener(v -> {
            if (!binding.subjectNameAutoComplete.getText().toString().isEmpty() && !binding.creditHourText.getText().toString().isEmpty()) {
                AssessmentDetailsModel newDetail = new AssessmentDetailsModel("Select", "0", "0", "0", "0");
                assessmentDetailsModelArrayList.add(newDetail);
                addNewRowWithSubjectInfo(newDetail);
            } else {
                Toast.makeText(requireContext(), "Please enter credit hour first!", Toast.LENGTH_SHORT).show();
            }
        });

        // TextWatcher for AutoCompleteTextView
        binding.subjectNameAutoComplete.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {
            }

            @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                // Check for existing subject and fetch additional details
                String subject = binding.subjectNameAutoComplete.getText().toString().trim();

                binding.toChangeText.setText("Enter a Subject Name\nTo Continue");
                if (subjectNames.contains(subject)) {
                    Toast.makeText(requireContext(), "Subject Found", Toast.LENGTH_SHORT).show();
                    fetchSubjectType_CreditHour_AssessmentDetails_basedOnSubjectName(subject);
                } else {
                    assessmentDetailsModelArrayList.clear();
                    binding.rowCount.setText("Row Count\n= 0");
                    addSubjectDetailsAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.deleteSubject.setOnClickListener(v -> {
            String subject = binding.subjectNameAutoComplete.getText().toString().trim();
            if (subjectNames.contains(subject)) {
                deleteAllData();
            } else {
                Toast.makeText(requireContext(), "Data is already not Saved or Deleted!", Toast.LENGTH_SHORT).show();
            }
        });


        binding.saveSubjectInfo.setOnClickListener(v -> {

            String sem = binding.semesterSpinner.getSelectedItem().toString();
            int semesterToSave = Integer.parseInt(sem);
            String subjectNameToSave = binding.subjectNameAutoComplete.getText().toString();
            String creditHourToSave = binding.creditHourText.getText().toString();
            String subjectTypeToSave = binding.subjectTypeSpinner.getSelectedItem().toString();

            if (semesterToSave > 0 && !subjectNameToSave.isEmpty() && !creditHourToSave.isEmpty() && !subjectTypeToSave.isEmpty()) {
                calculateMarksOfSubject();
            } else {
                Toast.makeText(requireContext(), "Credit Hour of Subject is not set!", Toast.LENGTH_SHORT).show();
            }


        });

        // Inflate the layout for this fragment
        return binding.getRoot();
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


    public void calculateMarksOfSubject() {

        ArrayList<String> assessmentNameList = new ArrayList<>();
        ArrayList<String> totalMarksList = new ArrayList<>();
        ArrayList<String> obtainedMarksList = new ArrayList<>();
        ArrayList<String> totalWeightageList = new ArrayList<>();
        ArrayList<String> obtainedPercentageList = new ArrayList<>();

        // Create a Map to store the count of each subject
        Map<String, Integer> subjectCountMap = new HashMap<>();

        ArrayList<String> uniqueAssessmentNames = new ArrayList<>();
        ArrayList<Integer> assessmentRepeat = new ArrayList<>();
        ArrayList<Double> assessmentWeightage = new ArrayList<>();

        ArrayList<Double> weightageOfAllAssessment = new ArrayList<>();

        ArrayList<Double> calculatedWeightage = new ArrayList<>();


        boolean check1 = false;
        boolean check2 = false;
        boolean check3 = false;
        boolean check4 = false;

        int modelSize = assessmentDetailsModelArrayList.size();
        Log.d("To_check", "Size: " + modelSize);
        for (int i = 0; i < modelSize; i++) {

            Log.d("To_check", "In for loop");

            AssessmentDetailsModel tempDetails = assessmentDetailsModelArrayList.get(i);

            String assessmentName = tempDetails.getAssessmentType().trim();
            String totalMark = tempDetails.getTotalMarks().trim();
            String obtainedMark = tempDetails.getObtainedMarks().trim();
            String totalWeightage = tempDetails.getTotalWeightage().trim();
            String obtainedPercentage = tempDetails.getObtainedPercentage().trim();

            Log.d("To_check", "Assessment Name: " + assessmentName);
            Log.d("To_check", "Total Marks: " + totalMark);
            Log.d("To_check", "Obtained Marks: " + obtainedMark);
            Log.d("To_check", "Total Weightage: " + totalWeightage);
            Log.d("To_check", "Obtained Percentage: " + obtainedPercentage + "\n\n\n");

            if (assessmentName.equals("None") || assessmentName.equals("Select")) {
                check1 = true;
                break;

            } else if (totalMark.isEmpty() || obtainedMark.isEmpty() || totalWeightage.isEmpty() || obtainedPercentage.isEmpty()) {
                check2 = true;
                break;

            } else if (totalMark.equals("0") || totalWeightage.equals("0")) {
                check3 = true;
                break;

            } else if (Double.parseDouble(obtainedMark) > Double.parseDouble(totalMark)) {
                check4 = true;
                break;

            } else {

                assessmentNameList.add(assessmentName);
                totalMarksList.add(totalMark);
                obtainedMarksList.add(obtainedMark);
                totalWeightageList.add(totalWeightage);
                obtainedPercentageList.add(obtainedPercentage);

            }


        }


        if (check1) {
            Toast.makeText(requireContext(), "Assessment Name is not correct!", Toast.LENGTH_SHORT).show();
        } else if (check2) {
            Toast.makeText(requireContext(), "Kindly fill all details!", Toast.LENGTH_SHORT).show();
        } else if (check3) {
            Toast.makeText(requireContext(), "Total Values cannot be zero!", Toast.LENGTH_SHORT).show();
        } else if (check4) {
            Toast.makeText(requireContext(), "Obtained values cannot be greater than Total values!", Toast.LENGTH_SHORT).show();
        } else {

            binding.toChangeText.setText("Saving Data\nPlease Wait!");

            // Loop through the assessmentNameList
            for (String subject : assessmentNameList) {
                // Update the count in the map
                subjectCountMap.put(subject, subjectCountMap.getOrDefault(subject, 0) + 1);
            }
            // Now, you can iterate over the map and store the unique subjects and their counts
            for (Map.Entry<String, Integer> entry : subjectCountMap.entrySet()) {
                uniqueAssessmentNames.add(entry.getKey());
                assessmentRepeat.add(entry.getValue());
            }


            for (int i = 0; i < uniqueAssessmentNames.size(); i++) {
                int assessmentIndex = assessmentNameList.indexOf(uniqueAssessmentNames.get(i));
                double totalWeightageOfAssessment = Double.parseDouble(totalWeightageList.get(assessmentIndex));
                double totalWeight = totalWeightageOfAssessment / assessmentRepeat.get(i);
                assessmentWeightage.add(totalWeight);
                weightageOfAllAssessment.add(totalWeightageOfAssessment);
            }

            for (int i = 0; i < modelSize; i++) {

                int assessmentIndexForWeightage = uniqueAssessmentNames.indexOf(assessmentNameList.get(i));
                double weightageOfAssessment = assessmentWeightage.get(assessmentIndexForWeightage);

                double temp = Double.parseDouble(obtainedPercentageList.get(i));
                double obtWeightage = (weightageOfAssessment * temp) / 100;
                calculatedWeightage.add(obtWeightage);

            }

            totalWeightage = 0.0;
            for (double value : weightageOfAllAssessment) {
                totalWeightage += value;
            }

            obtainedWeightage = 0.0;
            for (double value : calculatedWeightage) {
                obtainedWeightage += value;
            }

            totalMarks = 0.0;
            for (String value : totalMarksList) {
                totalMarks += Double.parseDouble(value);
            }

            obtainedMarks = 0.0;
            for (String value : obtainedMarksList) {
                obtainedMarks += Double.parseDouble(value);
            }

            getGrade(totalWeightage, obtainedWeightage);


        }

    }

    private void getGrade(double totalWeightage, double obtainedWeightage) {

        if (userId == null) {
            Log.e("AddNewRow", "userId is null");
            return;
        }


        int percentage = (int) ((obtainedWeightage / totalWeightage) * 100);

        DatabaseReference gradeRef = database.getReference().
                child("user").
                child(userId).
                child("gradeInformation");

        gradeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    if (snapshot.exists()) {
                        GradingDetailsModel details = snapshot1.getValue(GradingDetailsModel.class);

                        if (details != null) {
                            double maxValue = Double.parseDouble(details.getMaxValue());
                            double minValue = Double.parseDouble(details.getMinValue());

                            if (percentage <= maxValue && percentage >= minValue) {
                                grade = details.getGrade();
                                gradePoint = Double.parseDouble(details.getGradePoints());
                                break;
                            }

                        }
                    }
                }

                String sem = binding.semesterSpinner.getSelectedItem().toString();
                String subjectNameToSave = binding.subjectNameAutoComplete.getText().toString();

                if (!grade.equals("--") && gradePoint != 0.0) {

                    saveAllDataToFirebase(sem, subjectNameToSave, String.valueOf(totalWeightage), String.valueOf(obtainedWeightage), String.valueOf(totalMarks), String.valueOf(obtainedMarks), grade, String.valueOf(gradePoint));

                } else {
                    saveAllDataToFirebase(sem, subjectNameToSave, "none", "none", "none", "none", "none", "none");

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });


    }

    private void saveAllDataToFirebase(String semester, String subject, String totalWeightage, String obtainedWeightage, String totalMarks, String obtainedMarks, String grade, String gradePoints) {
        if (userId != null) {

            DatabaseReference userSemesterInfoRef = database.getReference().child("user").child(userId).child("subjectsDataSemesterVise").child("semester" + semester).child(subject).child("subjectInfo");
            DatabaseReference userSemesterAssessmentInfoRef = database.getReference().child("user").child(userId).child("subjectsDataSemesterVise").child("semester" + semester).child(subject).child("assessmentDetails");

            if (!Objects.equals(fetchedSemester, semester)) {
                DatabaseReference oldSubjectInfoRef = database.getReference().child("user").child(userId).child("subjectsDataSemesterVise").child("semester" + fetchedSemester).child(subject);
                oldSubjectInfoRef.removeValue().addOnSuccessListener(unused -> {
                    Toast.makeText(requireContext(), "Semester changed", Toast.LENGTH_SHORT).show();
                    fetchedSemester = semester;
                }).addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to add Subject Information", Toast.LENGTH_SHORT).show());
            }


            SubjectTotalDetailsModel subjectTotalDetailsModel = new SubjectTotalDetailsModel();

            subjectTotalDetailsModel.setCreditHour(binding.creditHourText.getText().toString().trim());
            subjectTotalDetailsModel.setSubjectType(binding.subjectTypeSpinner.getSelectedItem().toString().trim());
            subjectTotalDetailsModel.setSemester(semester);
            subjectTotalDetailsModel.setTw(totalWeightage);
            subjectTotalDetailsModel.setOw(obtainedWeightage);
            subjectTotalDetailsModel.setTm(totalMarks);
            subjectTotalDetailsModel.setOm(obtainedMarks);
            subjectTotalDetailsModel.setGrade(grade);
            subjectTotalDetailsModel.setGradePoint(gradePoints);


            userSemesterInfoRef.setValue(subjectTotalDetailsModel).addOnSuccessListener(unused -> {
            }).addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to add Subject Information", Toast.LENGTH_SHORT).show());


            userSemesterAssessmentInfoRef.removeValue().addOnSuccessListener(unused -> Toast.makeText(requireContext(), "Saving data please wait!", Toast.LENGTH_SHORT).show()).addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to save grading data", Toast.LENGTH_SHORT).show());

            for (AssessmentDetailsModel detail : assessmentDetailsModelArrayList) {
                addDataRowByRow(detail);
            }

            fetchAllSubjectNames();


        }
    }

    // Fetch subject names from Firebase
    private void fetchAllSubjectNames() {
        userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();

        DatabaseReference subjectsRef = database.getReference().
                child("user").
                child(userId).
                child("subjectsDataSemesterVise");

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

                // AutoCompleteTextView adapter
                AutoCompleteTextView subjectNameAutoComplete = binding.subjectNameAutoComplete;
                subjectNameAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, subjectNames);
                subjectNameAutoComplete.setThreshold(1);
                subjectNameAutoComplete.setAdapter(subjectNameAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FetchSubjectNames", "Error fetching subject names: " + databaseError.getMessage());
            }
        });
    }

    // Add a new row of subject details to Firebase
    @SuppressLint("SetTextI18n")
    private void addDataRowByRow(AssessmentDetailsModel detail) {
        if (userId == null) {
            Log.e("AddNewRow", "userId is null");
            return;
        }

        DatabaseReference gradingRef = database.getReference().
                child("user").
                child(userId).
                child("subjectsDataSemesterVise").
                child("semester" + binding.semesterSpinner.getSelectedItem().toString()).
                child(binding.subjectNameAutoComplete.getText().toString()).
                child("assessmentDetails");

        String newEntryKey = gradingRef.push().getKey();

        assert newEntryKey != null;
        gradingRef.child(newEntryKey).setValue(detail).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String semester1 = binding.semesterSpinner.getSelectedItem().toString();
                String subjectName1 = binding.subjectNameAutoComplete.getText().toString();
                retrieveAndDisplayAssessmentDetailsOfSubject(semester1, subjectName1, 0);
                binding.toChangeText.setText("Data Saved Successfully");
                retrieveAndSetSubjectGradesInformation();
            } else {
                Log.e("AddNewRow", "Error adding new row: " + Objects.requireNonNull(task.getException()).getMessage());
                Toast.makeText(requireContext(), "Failed to add new data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Add a new row of subject details to Firebase
    @SuppressLint("NotifyDataSetChanged")
    private void addNewRowWithSubjectInfo(AssessmentDetailsModel detail) {
        if (userId == null) {
            Log.e("AddNewRow", "userId is null");
            return;
        }

        DatabaseReference assessmentRef = database.getReference().
                child("user").
                child(userId).
                child("subjectsDataSemesterVise").
                child("semester" + binding.semesterSpinner.getSelectedItem().toString()).
                child(binding.subjectNameAutoComplete.getText().toString()).
                child("assessmentDetails");

        DatabaseReference assessmentInfoRef = database.getReference().
                child("user").child(userId).
                child("subjectsDataSemesterVise").
                child("semester" + binding.semesterSpinner.getSelectedItem().toString()).
                child(binding.subjectNameAutoComplete.getText().toString()).
                child("subjectInfo");

        String newEntryKey = assessmentRef.push().getKey();

        assert newEntryKey != null;
        assessmentRef.child(newEntryKey).setValue(detail).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                Toast.makeText(requireContext(), "New row added successfully", Toast.LENGTH_SHORT).show();
                String semester1 = binding.semesterSpinner.getSelectedItem().toString();
                String subjectName1 = binding.subjectNameAutoComplete.getText().toString();
                subjectNames.add(subjectName1);
                addSubjectDetailsAdapter.notifyDataSetChanged();
                retrieveAndDisplayAssessmentDetailsOfSubject(semester1, subjectName1, 0);

                Map<String, Object> map = new HashMap<>();

                map.put("creditHour", binding.creditHourText.getText().toString().trim());
                map.put("subjectType", binding.subjectTypeSpinner.getSelectedItem().toString().trim());
                map.put("semester", binding.semesterSpinner.getSelectedItem().toString());

                assessmentInfoRef.updateChildren(map).addOnSuccessListener(unused -> binding.toChangeText.setText("Press Save Subject Info\nto save data")).addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to add Subject Information", Toast.LENGTH_SHORT).show());

            } else {
                Log.e("Error_in_code", "Error adding new row: " + Objects.requireNonNull(task.getException()).getMessage());
                Toast.makeText(requireContext(), "Failed to add new data", Toast.LENGTH_SHORT).show();
            }
        });

    }

    // Fetch and set up semester information from Firebase
    private void fetchAndSetSemester() {
        userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();

        DatabaseReference semesterRef = database.getReference().
                child("user").
                child(userId).
                child("userInfo");

        semesterRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Fetch user information

                    UserModel userModel = snapshot.getValue(UserModel.class);
                    assert userModel != null;
                    noOfSemesters = userModel.getSemesters();

                    userName = userModel.getName().trim();
                    degree = userModel.getDegree().trim();
                    email = userModel.getEmail().trim();
                    password = userModel.getPassword().trim();


                    // Populate semester Spinner with appropriate values
                    if (noOfSemesters != -1) {
                        for (int i = 1; i <= noOfSemesters; i++) {
                            semesters.add(i);
                        }

                        Spinner semesterSpinner = binding.semesterSpinner;
                        semesterAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, semesters);
                        semesterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        semesterSpinner.setAdapter(semesterAdapter);
                    }
                } else {
                    Toast.makeText(requireContext(), "Kindly set no of semesters in 'Add or Edit Details' to get started", Toast.LENGTH_LONG).show();
                }
                // Fetch subject names for AutoCompleteTextView
                fetchAllSubjectNames();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle onCancelled if needed
            }
        });
    }

    // Fetch subject type and credit hour from Firebase based on selected subject
    private void fetchSubjectType_CreditHour_AssessmentDetails_basedOnSubjectName(String subjectName2) {
        userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();

        if (noOfSemesters != -1) {
            subjectName = subjectName2;

            DatabaseReference subjectsRef = database.getReference().
                    child("user").
                    child(userId).
                    child("subjectsDataSemesterVise");

            subjectsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
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

                                    subjectsRef.
                                            child("semester" + semesterNo).
                                            child(subjectName).
                                            child("subjectInfo").
                                            addListenerForSingleValueEvent(new ValueEventListener() {

                                                @SuppressLint("DefaultLocale")
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    if (snapshot.exists()) {

                                                        SubjectTotalDetailsModel subjectTotalDetailsModel = snapshot.getValue(SubjectTotalDetailsModel.class);

                                                        assert subjectTotalDetailsModel != null;

                                                        retrieveAndSetSubjectGradesInformation();

                                                        if (subjectTotalDetailsModel.getSemester() == null || subjectTotalDetailsModel.getSemester().isEmpty()) {
                                                            subjectTotalDetailsModel.setSemester("N/A");
                                                        }

                                                        if (subjectTotalDetailsModel.getCreditHour() != null && !subjectTotalDetailsModel.getCreditHour().isEmpty()) {
                                                            binding.creditHourText.setText(subjectTotalDetailsModel.getCreditHour().trim());
                                                        } else {
                                                            binding.creditHourText.setText("N/A");

                                                        }


                                                        fetchedSemester = subjectTotalDetailsModel.getSemester();
                                                        int index2 = getIndexForSemesterValue(subjectTotalDetailsModel.getSemester());
                                                        int index = getIndexForSubjectTypeValue(subjectTotalDetailsModel.getSubjectType());
                                                        if (index != -1) {
                                                            binding.subjectTypeSpinner.setSelection(index);
                                                        } else {
                                                            Log.d("To_check", "Subject Error - Not Fetched");
                                                        }

                                                        if (index2 != -1) {
                                                            binding.semesterSpinner.setSelection(index2);
                                                        } else {
                                                            Log.d("To_check", "Semester Error - Not Fetched");
                                                        }

                                                        retrieveAndDisplayAssessmentDetailsOfSubject(subjectTotalDetailsModel.getSemester(), binding.subjectNameAutoComplete.getText().toString(), 1);

                                                    } else {
                                                        Toast.makeText(requireContext(), "Existing Data Not Found", Toast.LENGTH_SHORT).show();
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });

                                }

                            }
                        }
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("Error_in_code", "Error fetching subject names: " + databaseError.getMessage());
                }
            });

        }

    }

    private void retrieveAndSetSubjectGradesInformation() {


        userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();

        if (noOfSemesters != -1) {
            String subjectName2 = binding.subjectNameAutoComplete.getText().toString().trim();

            DatabaseReference subjectsRef = database.getReference().
                    child("user").
                    child(userId).
                    child("subjectsDataSemesterVise");

            subjectsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
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

                                    subjectsRef.
                                            child("semester" + semesterNo).
                                            child(subjectName).
                                            child("subjectInfo").
                                            addListenerForSingleValueEvent(new ValueEventListener() {

                                                @SuppressLint({"DefaultLocale", "SetTextI18n"})
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    if (snapshot.exists()) {
                                                        SubjectTotalDetailsModel subjectTotalDetailsModel = snapshot.getValue(SubjectTotalDetailsModel.class);

                                                        assert subjectTotalDetailsModel != null;

                                                        // Try-catch block for obtained weightage
                                                        try {
                                                            if (subjectTotalDetailsModel.getOw() != null && !subjectTotalDetailsModel.getOw().isEmpty()) {
                                                                double obtainedWeightage = Double.parseDouble(subjectTotalDetailsModel.getOw().trim());
                                                                binding.obtianedW.setText(String.format("%.2f", obtainedWeightage));
                                                            }
                                                        } catch (NumberFormatException e) {
                                                            binding.obtianedW.setText("Error");
                                                        }

                                                        // Try-catch block for total weightage
                                                        try {
                                                            if (subjectTotalDetailsModel.getTw() != null && !subjectTotalDetailsModel.getTw().isEmpty()) {
                                                                double totalWeightage = Double.parseDouble(subjectTotalDetailsModel.getTw().trim());
                                                                binding.totalW.setText(String.format("%.2f", totalWeightage));
                                                            }
                                                        } catch (NumberFormatException e) {
                                                            binding.totalW.setText("Error");
                                                        }

                                                        // Try-catch block for grade
                                                        try {
                                                            if (subjectTotalDetailsModel.getGrade() != null && !subjectTotalDetailsModel.getGrade().isEmpty()) {
                                                                binding.grade.setText(subjectTotalDetailsModel.getGrade().trim());
                                                            }
                                                        } catch (Exception e) {
                                                            binding.grade.setText("Error");
                                                        }

                                                        // Try-catch block for grade point
                                                        try {
                                                            if (subjectTotalDetailsModel.getGradePoint() != null && !subjectTotalDetailsModel.getGradePoint().isEmpty()) {
                                                                binding.point.setText(subjectTotalDetailsModel.getGradePoint().trim());
                                                            }
                                                        } catch (Exception e) {
                                                            binding.point.setText("Error");
                                                        }

                                                        // Try-catch block for percentage calculation
                                                        try {
                                                            if (subjectTotalDetailsModel.getOw() != null && subjectTotalDetailsModel.getTw() != null
                                                                    && !subjectTotalDetailsModel.getOw().isEmpty() && !subjectTotalDetailsModel.getTw().isEmpty()) {
                                                                double obtainedWeightage = Double.parseDouble(subjectTotalDetailsModel.getOw().trim());
                                                                double totalWeightage = Double.parseDouble(subjectTotalDetailsModel.getTw().trim());
                                                                double percentage = (obtainedWeightage * 100) / totalWeightage;
                                                                binding.obtainedP.setText(String.format("%.2f%%", percentage));
                                                            }
                                                        } catch (NumberFormatException e) {
                                                            binding.obtainedP.setText("Calculation error");
                                                        }
                                                    }

                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });

                                }

                            }
                        }
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("Error_in_code", "Error fetching subject names: " + databaseError.getMessage());
                }
            });

        }


    }

    // Retrieve and display subject details from Firebase
    private void retrieveAndDisplayAssessmentDetailsOfSubject(String semester1, String subjectName1, int num) {

        database = FirebaseDatabase.getInstance();
        userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();

        DatabaseReference subjectsRef = database.getReference().child("user").child(userId).child("subjectsDataSemesterVise").child("semester" + semester1).child(subjectName1).child("assessmentDetails");
        subjectsRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                assessmentDetailsModelArrayList.clear();
                boolean dataFound = false;

                for (DataSnapshot marksSnapshot : snapshot.getChildren()) {
                    AssessmentDetailsModel assessmentDetailsModel1 = marksSnapshot.getValue(AssessmentDetailsModel.class);
                    if (assessmentDetailsModel1 != null) {
                        dataFound = true;
                        assessmentDetailsModelArrayList.add(assessmentDetailsModel1);
                    }
                }

                if (dataFound) {
                    binding.toChangeText.setText("Subject Assessment Data");
                    if (num == 1) {
                        Toast.makeText(requireContext(), "Existing Assessment Data Found for " + binding.subjectNameAutoComplete.getText().toString(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    binding.toChangeText.setText("No Data To Show");
                }

                setSubjectDetailsAdapter(assessmentDetailsModelArrayList, semester1, subjectName1);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void deleteAllData() {
        if (userId != null) {
            DatabaseReference subjectRef = database.getReference().child("user").child(userId).child("subjectsDataSemesterVise").child("semester" + binding.semesterSpinner.getSelectedItem().toString()).child(binding.subjectNameAutoComplete.getText().toString());

            subjectRef.removeValue(new DatabaseReference.CompletionListener() {
                @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
                @Override
                public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                    Toast.makeText(requireContext(), "Remove subject named: " + binding.subjectNameAutoComplete.getText().toString(), Toast.LENGTH_SHORT).show();
                    binding.creditHourText.setText("");
                    binding.semesterSpinner.setSelection(0);
                    binding.subjectTypeSpinner.setSelection(0);
                    binding.subjectNameAutoComplete.clearListSelection();
                    binding.subjectNameAutoComplete.setText("");
                    assessmentDetailsModelArrayList.clear();
                    binding.rowCount.setText("Row Count: 0");
                    addSubjectDetailsAdapter.notifyDataSetChanged();
                    binding.totalW.setText("Enter");
                    binding.obtianedW.setText("Assessment");
                    binding.grade.setText("Details");
                    binding.point.setText("To");
                    binding.obtainedP.setText("Continue");
                }
            });
        }
    }

    // Set the adapter for subject details RecyclerView
    @SuppressLint("SetTextI18n")
    private void setSubjectDetailsAdapter(ArrayList<AssessmentDetailsModel> assessmentDetailModels, String sem, String sub) {

        binding.rowCount.setText("Row Count: " + assessmentDetailModels.size());

        addSubjectDetailsAdapter = new AddSubjectMarksInDetailAdapter(requireContext(), assessmentDetailModels, sem, sub);
        binding.detailsRecyclerViewOfSubjectMarksDetails.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.detailsRecyclerViewOfSubjectMarksDetails.setAdapter(addSubjectDetailsAdapter);

        int noOfItems = Math.min(assessmentDetailModels.size(), 6);
        // Set the height to show only 5 items
        int itemHeight = getResources().getDimensionPixelSize(R.dimen.your_item_height); // Replace with your actual item height
        binding.detailsRecyclerViewOfSubjectMarksDetails.getLayoutParams().height = noOfItems * itemHeight;

    }
}
