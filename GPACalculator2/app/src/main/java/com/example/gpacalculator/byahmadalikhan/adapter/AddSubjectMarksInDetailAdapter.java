package com.example.gpacalculator.byahmadalikhan.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gpacalculator.byahmadalikhan.R;
import com.example.gpacalculator.byahmadalikhan.model.AssessmentDetailsModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddSubjectMarksInDetailAdapter extends RecyclerView.Adapter<AddSubjectMarksInDetailAdapter.AddSubjectMarksViewHolder> {

    Context context;
    ArrayList<AssessmentDetailsModel> assessmentDetailModels;
    private DatabaseReference gradeItemReference;
    String semester;
    String subjectName;

    // Constructor
    public AddSubjectMarksInDetailAdapter(Context context, ArrayList<AssessmentDetailsModel> assessmentDetailModels, String semester, String subjectName) {
        this.context = context;
        this.assessmentDetailModels = assessmentDetailModels;
        this.semester = semester;
        this.subjectName = subjectName;

        // Firebase initialization
        FirebaseUser userId = FirebaseAuth.getInstance().getCurrentUser();
        if (userId != null) {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            // Database reference for storing subject details
            gradeItemReference = database.getReference("user").child(userId.getUid()).child("subjectsDataSemesterVise").child("semester" + semester).child(subjectName).child("assessmentDetails");
        }
    }

    // onCreateViewHolder
    @NonNull
    @Override
    public AddSubjectMarksViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_subject_details, parent, false);
        return new AddSubjectMarksViewHolder(view);
    }

    // onBindViewHolder method to bind data to ViewHolder
    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    public void onBindViewHolder(@NonNull AddSubjectMarksViewHolder holder, @SuppressLint("RecyclerView") int position) {

        // Get the SubjectDetails object for the current position
        AssessmentDetailsModel model = assessmentDetailModels.get(position);

        // Set the selected item for the Spinner based on the data
        String currentAssessmentType = model.getAssessmentType();
        int index = getIndexForValue(currentAssessmentType, holder.assessmentType.getContext());
        if (index != -1) {
            holder.assessmentType.setSelection(index);
        }

        // Set data to other views in the ViewHolder
        // Format Total Marks
        try {
            double totalMarks = Double.parseDouble(model.getTotalMarks());
            holder.tm.setText(String.format("%.2f", totalMarks));
        } catch (NumberFormatException e) {
            holder.tm.setText(model.getTotalMarks());
        }

        // Format Total Weightage
        try {
            double totalWeightage = Double.parseDouble(model.getTotalWeightage());
            holder.tw.setText(String.format("%.2f", totalWeightage));
        } catch (NumberFormatException e) {
            holder.tw.setText(model.getTotalWeightage());
        }

        // Format Obtained Marks
        try {
            double obtainedMarks = Double.parseDouble(model.getObtainedMarks());
            holder.om.setText(String.format("%.2f", obtainedMarks));
        } catch (NumberFormatException e) {
            holder.om.setText(model.getObtainedMarks());
        }


        // Calculate and display the percentage
        double totalMarks = Double.parseDouble(model.getTotalMarks());
        double obtainedMarks = Double.parseDouble(model.getObtainedMarks());
        double percentage = (obtainedMarks / totalMarks) * 100;

        if (totalMarks > 0 && obtainedMarks > 0) {
            try {
                holder.op.setText(String.format("%.2f", percentage));
            } catch (NumberFormatException e) {
                holder.op.setText(String.valueOf(percentage));
            }
        } else {
            holder.op.setText("Nan");
        }

        // Check for null views in the ViewHolder
        if (holder.tw == null || holder.om == null || holder.tm == null || holder.assessmentType == null) {
            Log.e("ViewHolderError", "One or more views in the subject ViewHolder are null");
            return;
        }





        holder.tm.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.d("debugging_check", "Total Marks Before Text Changed");

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d("debugging_check", "Total Marks On Text Changed");
                String totalMarks = holder.getTm().getText().toString();
                String obtainedMarks = holder.getOm().getText().toString();

                if (totalMarks.isEmpty() || !containsNumeric(totalMarks)){
                    Log.d("debugging_check", "Total Marks is empty or non numeric");
                }
                if (obtainedMarks.isEmpty() || !containsNumeric(obtainedMarks)){
                    Log.d("debugging_check", "Obtained Marks is empty or non numeric");
                }

                if (!totalMarks.isEmpty() && !obtainedMarks.isEmpty() && containsNumeric(totalMarks) && containsNumeric(obtainedMarks)) {
                    if (Double.parseDouble(holder.getTm().getText().toString()) >= Double.parseDouble(holder.getOm().getText().toString())) {
                        // Update data if conditions are met
                        Log.d("debugging_check", "TM ON Text Changed - > Updating value");
                        updateData(position, holder.getTm().getText().toString(), 1, holder);
                    } else {
                        // Show a toast message if the condition is not met
                        Toast.makeText(context, "Total Marks cannot be less than Obtained Marks 🙁", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Show a toast message if either value is empty
                    Toast.makeText(context, "Please enter value in Total Marks", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d("debugging_check", "TM After Text Changed");
            }
        });


        holder.om.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.d("debugging_check", "OM Before Text Changed");
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d("debugging_check", "OM On Text Changed");
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d("debugging_check", "OM After Text Changed");
                String text = holder.getOm().getText().toString();
                String text1 = holder.getTm().getText().toString();
                if (!text.isEmpty() && !text1.isEmpty() && containsNumeric(holder.getOm().getText().toString())) {
                    if (Double.parseDouble(holder.getOm().getText().toString()) <= Double.parseDouble(holder.getTm().getText().toString())) {
                        // Update data if conditions are met
                        Log.d("debugging_check", "OM On Text Changed -> Updating value");
                        updateData(position, holder.getOm().getText().toString(), 3, holder);
                    } else {
                        // Show a toast message if the condition is not met
                        Toast.makeText(context, "You cannot take Marks more than Total Marks 😂", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Show a toast message if either value is empty
                    Toast.makeText(context, "Please enter value in Obtained Marks", Toast.LENGTH_SHORT).show();
                }
            }
        });

        holder.tw.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.d("debugging_check", "TW Before Text Changed");
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d("debugging_check", "TW On Text Changed");
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d("debugging_check", "TW After Text Changed");
                String text = holder.getTw().getText().toString();
                if (!text.isEmpty() ) {
                    Log.d("debugging_check", "TW After Text Changed -> Updating value");
                    updateData(position, holder.getTw().getText().toString(), 2, holder);
                }else{

                }
            }
        });

        holder.assessmentType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int positions, long id) {
                Log.d("debugging_check", "Assessment On Item Selected -> Updating Value");
                Log.d("debugging_check", "Assessment On Item Selected: "+holder.getAssessmentType().getSelectedItem().toString());
                updateData(position, holder.getAssessmentType().getSelectedItem().toString(), 5, holder);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d("debugging_check", "Assessment On Nothing Selected");
            }

        });







//        // Set onFocusChangeListener for Total Marks EditText
//        holder.tm.setOnFocusChangeListener((v, hasFocus) -> {
//            if (!hasFocus) {
//                // TM has lost focus
//                Log.d("AddSubjectAdapterCheck", "TM has lost focus");
//                String text = holder.getTm().getText().toString();
//                String text1 = holder.getOm().getText().toString();
//                if (!text.isEmpty() && !text1.isEmpty()) {
//                    if (Double.parseDouble(holder.getTm().getText().toString()) >= Double.parseDouble(holder.getOm().getText().toString())) {
//                        // Update data if conditions are met
//                        updateData(position, holder.getTm().getText().toString(), 1, holder);
//                    } else {
//                        // Show a toast message if the condition is not met
//                        Toast.makeText(context, "Total Marks cannot be less than Obtained Marks 🙁", Toast.LENGTH_SHORT).show();
//                    }
//                } else {
//                    // Show a toast message if either value is empty
//                    Toast.makeText(context, "Please enter value in Total Marks", Toast.LENGTH_SHORT).show();
//                }
//            } else {
//                // TM has gained focus
//                Log.d("AddSubjectAdapterCheck", "TM has gained focus");
//            }
//        });
//
//        // Set onFocusChangeListener for Total Weightage EditText
//        holder.tw.setOnFocusChangeListener((v, hasFocus) -> {
//            if (!hasFocus) {
//                // TW has lost focus
//                Log.d("AddSubjectAdapterCheck", "TW has lost focus");
//                // Update data
//                updateData(position, holder.getTw().getText().toString(), 2, holder);
//            } else {
//                // TW has gained focus
//                Log.d("AddSubjectAdapterCheck", "TW has gained focus");
//            }
//        });
//
//        // Set onFocusChangeListener for Obtained Marks EditText
//        holder.om.setOnFocusChangeListener((v, hasFocus) -> {
//            if (!hasFocus) {
//                // OM has lost focus
//                Log.d("AddSubjectAdapterCheck", "OM has lost focus");
//                String text = holder.getOm().getText().toString();
//                String text1 = holder.getTm().getText().toString();
//                if (!text.isEmpty() && !text1.isEmpty()) {
//                    if (Double.parseDouble(holder.getOm().getText().toString()) <= Double.parseDouble(holder.getTm().getText().toString())) {
//                        // Update data if conditions are met
//                        updateData(position, holder.getOm().getText().toString(), 3, holder);
//                    } else {
//                        // Show a toast message if the condition is not met
//                        Toast.makeText(context, "You cannot take Marks more than Total Marks 😂", Toast.LENGTH_SHORT).show();
//                    }
//                } else {
//                    // Show a toast message if either value is empty
//                    Toast.makeText(context, "Please enter value in Obtained Marks", Toast.LENGTH_SHORT).show();
//                }
//            } else {
//                // OM has gained focus
//                Log.d("AddSubjectAdapterCheck", "OM has gained focus");
//            }
//        });
//
//         //Set onFocusChangeListener for Assessment Type Spinner
//        holder.assessmentType.setOnFocusChangeListener((v, hasFocus) -> {
//            if (!hasFocus) {
//                // Assessment Type has lost focus
//                Log.d("debugging_check", "Assessment Type has lost focus");
//                // Update data
//                updateData(position, holder.getAssessmentType().getSelectedItem().toString(), 5, holder);
//            } else {
//                // Assessment Type has gained focus
//                Log.d("debugging_check", "Assessment Type has gained focus");
//            }
//        });


    }


    // Method to update data in the adapter based on user input
    public void updateData(int position, String updatedValue, int value, AddSubjectMarksViewHolder holder) {

        Log.d("debugging_check", "Position: "+position);
        Log.d("debugging_check", "Size: "+assessmentDetailModels.size());
        // Check if the position is valid
        if (position >= 0 && position < assessmentDetailModels.size()) {

            // Case 1: Updating Total Marks (value = 1)
            if (value == 1) {
                Log.d("debugging_check", "Updating Total Marks");
                // Check if the updated value is not empty and not equal to "-"
                if (!updatedValue.isEmpty() && !updatedValue.equals("-")) {
                    double newTotalMarks = Double.parseDouble(updatedValue);
                    double obtainedMarks = Double.parseDouble(assessmentDetailModels.get(position).getObtainedMarks());
                    double weightage = (obtainedMarks / newTotalMarks) * 100;
                    @SuppressLint("DefaultLocale") String formattedWeightage = String.format("%.3f", weightage);

                    // Check if Obtained Marks is not empty
                    if (!assessmentDetailModels.get(position).getObtainedMarks().isEmpty()) {
                        // Check if new Total Marks is greater than or equal to Obtained Marks
                        if (newTotalMarks >= obtainedMarks) {
                            Log.d("debugging_check", "Data Updated -> Total Marks");
                            AssessmentDetailsModel gradingDetailsItem = assessmentDetailModels.get(position);
                            gradingDetailsItem.setTotalMarks(updatedValue);
                            gradingDetailsItem.setObtainedPercentage(String.valueOf(weightage));
                            holder.updateObtainedPercentage(formattedWeightage);
                        } else {
                            // Show a toast message if Total Marks is less than Obtained Marks
                            Toast.makeText(context, "Total Marks can't be less than Obtained Marks 🙁", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Show a toast message if Obtained Marks is empty
                        Toast.makeText(context, "Kindly enter Obtained Marks", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Show a toast message if updated value is empty or "-"
                    Toast.makeText(context, "Kindly enter Total Marks value", Toast.LENGTH_SHORT).show();
                }
            }

            // Case 2: Updating Total Weightage (value = 2)
            else if (value == 2) {
                Log.d("debugging_check", "Data Updated -> Total Weightage");
                AssessmentDetailsModel gradingDetailsItem = assessmentDetailModels.get(position);
                gradingDetailsItem.setTotalWeightage(updatedValue);
            }

            // Case 3: Updating Obtained Marks (value = 3)
            else if (value == 3) {
                Log.d("debugging_check", "Updating Obtained Marks");
                // Check if the updated value is not empty and not equal to "-"
                if (!updatedValue.isEmpty() && !updatedValue.equals("-")) {
                    double TotalMarks = Double.parseDouble(assessmentDetailModels.get(position).getTotalMarks());
                    double newObtainedMarks = Double.parseDouble(updatedValue);
                    double weightage = (newObtainedMarks / TotalMarks) * 100;
                    @SuppressLint("DefaultLocale") String formattedWeightage = String.format("%.3f", weightage);

                    // Check if Total Marks is not empty
                    if (!assessmentDetailModels.get(position).getTotalMarks().isEmpty()) {
                        // Check if new Obtained Marks is less than or equal to Total Marks
                        if (newObtainedMarks <= TotalMarks) {
                            Log.d("debugging_check", "Data Updated -> Obtained Marks");
                            AssessmentDetailsModel gradingDetailsItem = assessmentDetailModels.get(position);
                            gradingDetailsItem.setObtainedMarks(updatedValue);
                            gradingDetailsItem.setObtainedPercentage(String.valueOf(weightage));
                            holder.updateObtainedPercentage(formattedWeightage);
                        } else {
                            // Show a toast message if Obtained Marks is greater than Total Marks
                            Toast.makeText(context, "Obtained Marks cannot be greater than Total Marks 🙁", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Show a toast message if Total Marks is empty
                        Toast.makeText(context, "Kindly enter Total Marks value = 2", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Show a toast message if updated value is empty or "-"
                    Toast.makeText(context, "Kindly enter Obtained Marks", Toast.LENGTH_SHORT).show();
                }
            }

            // Case 4: Updating Obtained Percentage (value = 4)
            else if (value == 4) {
                Log.d("debugging_check", "Updating Obtained Percentage");
                AssessmentDetailsModel gradingDetailsItem = assessmentDetailModels.get(position);
                // Check if the updated value is not empty
                if (!updatedValue.isEmpty()) {
                    Log.d("debugging_check", "Data Updated -> Obtained Percentage");
                    gradingDetailsItem.setObtainedPercentage(updatedValue);
                } else {
                    // Show a toast message if updated value is empty
                    Toast.makeText(context, "Kindly enter Total Weightage", Toast.LENGTH_SHORT).show();
                }
            }

            // Case 5: Updating Assessment Type (value = 5)
            else if (value == 5) {
                Log.d("debugging_check", "Data Updated -> Assessment");
                AssessmentDetailsModel gradingDetailsItem = assessmentDetailModels.get(position);
                Log.d("debugging_check", "Assessment: "+updatedValue);
                gradingDetailsItem.setAssessmentType(updatedValue);
            }
        }else{
            Log.d("debugging_check", "Error!");
        }
    }


    // Method to get the index for a given value in the Spinner's adapter
    private int getIndexForValue(String value, Context context) {
        // Get the array of assessment types from the resources
        String[] assessmentTypes = context.getResources().getStringArray(R.array.assessment_type); // Assuming you have an array resource for assessment types

        // Loop through the array to find the index of the given value
        for (int i = 0; i < assessmentTypes.length; i++) {
            if (value.equals(assessmentTypes[i])) {
                return i;  // Return the index if the value is found
            }
        }

        return -1;  // Return -1 if the value is not found in the Spinner's adapter
    }


    @Override
    public int getItemCount() {
        return assessmentDetailModels.size();
    }

    public boolean containsNumeric(String input) {
        Pattern pattern = Pattern.compile(".*\\d+.*");
        Matcher matcher = pattern.matcher(input);
        return matcher.matches() && !input.contains("-");
    }

    // ViewHolder class to hold the views for each item in the RecyclerView
    public class AddSubjectMarksViewHolder extends RecyclerView.ViewHolder {

        // Declare views for each item
        EditText tm, tw, om, op;
        Spinner assessmentType;

        // Constructor for the ViewHolder
        public AddSubjectMarksViewHolder(@NonNull View itemView) {
            super(itemView);

            // Create a reference to the ViewHolder itself
//            final AddSubjectMarksViewHolder holder = this;

            // Initialize views by finding them in the layout
            assessmentType = itemView.findViewById(R.id.assessmentTypeSpinner);
            tm = itemView.findViewById(R.id.totalMarks);
            tw = itemView.findViewById(R.id.totalWeightage);
            om = itemView.findViewById(R.id.obtainedMarks);
            op = itemView.findViewById(R.id.obtainedPercentage);

            // Check and display a toast message if the data is set correctly
            if (!tm.getText().toString().equals("-1") && om.getText().toString().equals("-1") && !tw.getText().toString().equals("-1")) {
                Toast.makeText(context, "Data is set correctly", Toast.LENGTH_SHORT).show();
            }

            // Find the delete button in the layout
            ImageButton delBtn = itemView.findViewById(R.id.delButton);

            // Set an OnClickListener for the delete button
            delBtn.setOnClickListener(v -> {
                int itemPosition = getAdapterPosition();
                // Check if the item position is valid
                if (itemPosition != RecyclerView.NO_POSITION) {
                    // Call the deleteItem method with the position to delete the item
                    deleteItem(itemPosition);
                }
            });

            // Get the current adapter position
//            int position = getAdapterPosition();

            // Attach TextWatchers to the EditTexts for live updates
//            attachTextWatcher(tm, position, 1, holder);
//            attachTextWatcher(tw, position, 2, holder);
//            attachTextWatcher(om, position, 3, holder);
//            attachTextWatcher(op, position, 4, holder);
//
//            // Set an OnItemSelectedListener for the Spinner
//            assessmentType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//                @Override
//                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
//                    // Check if a valid position is selected and update the data
//                    if (position != RecyclerView.NO_POSITION) {
//                        Log.d("AddSubjectAdapterCheck", "Assessment Type item selected");
//                        updateData(position, assessmentType.getSelectedItem().toString(), 5, holder);
//                    }
//                }
//
//                @Override
//                public void onNothingSelected(AdapterView<?> parentView) {
//                    // Do nothing if nothing is selected
//                }
//            });

        }

        // Method to update the Obtained Percentage view
        public void updateObtainedPercentage(String percentage) {
            if (op != null) {
                op.setText(percentage);
            }
        }

        // Getter methods for accessing views from outside the ViewHolder
        public Spinner getAssessmentType() {
            return assessmentType;
        }

        public EditText getTm() {
            return tm;
        }

        public EditText getTw() {
            return tw;
        }

        public EditText getOm() {
            return om;
        }

        public EditText getOp() {
            return op;
        }
    }


    // Method to attach a TextWatcher to an EditText
    private void attachTextWatcher(EditText editText, int position, int value, AddSubjectMarksViewHolder holder) {
        // Add a TextWatcher to the EditText for live updates
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Not used in this case
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Not used in this case
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Check if a valid position is available and update the data
                if (position != RecyclerView.NO_POSITION) {
                    Log.d("debugging_check", "Updating value: "+value);
                    updateData(position, editable.toString(), value, holder);
                }
            }
        });
    }


    // Method to delete an item at a given position
    public void deleteItem(int position) {
        // Get the unique key at the specified position
        getUniqueKeyAtPosition(position, uniqueKey -> {
            // Check if the unique key is not null
            if (uniqueKey != null) {
                // Remove the item using the unique key
                removeItems(position, uniqueKey);
            }
        });
    }

    // Method to remove an item from the database and update the UI
    private void removeItems(int position, String uniqueKey) {
        // Check if the unique key is not null, the list is not empty, and the position is valid
        if (uniqueKey != null && !assessmentDetailModels.isEmpty() && position >= 0 && position < assessmentDetailModels.size()) {
            // Remove the item from the database using the unique key
            gradeItemReference.child(uniqueKey).removeValue().addOnSuccessListener(unused -> {
                try {
                    // Use Handler to update UI on the main thread
                    new Handler(Looper.getMainLooper()).post(() -> {
                        // Show a toast message indicating successful deletion
                        Toast.makeText(context, "Item Deleted Successfully", Toast.LENGTH_SHORT).show();
                        // Notify the adapter that the item has been removed
                        notifyItemRemoved(position);
                    });
                } catch (Exception e) {
                    Log.d("Errors_in_Code", "SemesterViseAdapter: "+ e);
                }
            }).addOnFailureListener(e -> {
                // Use Handler to show Toast on the main thread in case of failure
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(context, "Failed to Delete item", Toast.LENGTH_SHORT).show());
            });
        }
    }

    // Method to retrieve the unique key at a given position
    private void getUniqueKeyAtPosition(int positionRetrieve, OnCompleteListener<String> onComplete) {
        // Check if the gradeItemReference is not null
        if (gradeItemReference != null) {
            // Add a listener for a single value event to fetch the data snapshot
            gradeItemReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String uniqueKey = null;
                    int index = 0;  // Initialize the index

                    // Iterate through the data snapshot's children
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        // Check if the current index matches the requested position
                        if (index == positionRetrieve) {
                            // Retrieve the unique key and break the loop
                            uniqueKey = dataSnapshot.getKey();
                            break;
                        }
                        index++;
                    }

                    // Callback to the onComplete method with the unique key
                    onComplete.onComplete(uniqueKey);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle onCancelled event if needed
                }
            });
        }
    }

    // Interface to define onComplete method for handling asynchronous results
    public interface OnCompleteListener<T> {
        void onComplete(T result);
    }


}
