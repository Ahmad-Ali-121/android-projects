package com.example.gpacalculator.byahmadalikhan.adapter;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gpacalculator.byahmadalikhan.R;
import com.example.gpacalculator.byahmadalikhan.model.SubjectTotalDetailsModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SemesterViseResultAdapter extends  RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    ArrayList<SubjectTotalDetailsModel> subjectTotalDetailModels;
    ArrayList<String> creditHourList;
    private DatabaseReference gradeItemReference;
    int check;
    private static final int VIEW_TYPE_NORMAL = 1;
    private static final int VIEW_TYPE_CUSTOM = 2;
    String creditHours;
    String totalMarks;
    String obtainedMarks;
    String obtainedGrade;
    String obtainedGradePoints;
    String percentage;


    public SemesterViseResultAdapter(Context context, ArrayList<SubjectTotalDetailsModel> subjectTotalDetailModels, ArrayList<String> creditHourList, int check) {
        this.context = context;
        this.subjectTotalDetailModels = subjectTotalDetailModels;
        this.creditHourList = creditHourList;
        this.check = check;

        FirebaseUser userId = FirebaseAuth.getInstance().getCurrentUser();
        if (userId != null) {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            gradeItemReference = database.getReference("user").child(userId.getUid()).child("subjectsDataSemesterVise");
        }
    }

    public SemesterViseResultAdapter(Context context, String creditHours, String totalMarks, String obtainedMarks, String obtainedGrade, String obtainedGradePoints, String percentage, int check) {
        this.context = context;
        this.creditHours = creditHours;
        this.totalMarks = totalMarks;
        this.obtainedGrade = obtainedGrade;
        this.obtainedMarks = obtainedMarks;
        this.obtainedGradePoints = obtainedGradePoints;
        this.check = check;
        this.percentage = percentage;

        FirebaseUser userId = FirebaseAuth.getInstance().getCurrentUser();
        if (userId != null) {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            gradeItemReference = database.getReference("user").child(userId.getUid()).child("subjectsDataSemesterVise");
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (check == 2 ) {
            return VIEW_TYPE_CUSTOM;
        } else {
            return VIEW_TYPE_NORMAL;
        }
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_NORMAL) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_semester_details, parent, false);
            return new ShowSubjectMarksViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_semester_details_last_row, parent, false);
            return new CustomViewHolder(view);
        }
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (getItemViewType(position) == VIEW_TYPE_NORMAL) {
            ShowSubjectMarksViewHolder holder1 = (ShowSubjectMarksViewHolder) holder;
            SubjectTotalDetailsModel model = subjectTotalDetailModels.get(position);
            String creditH = creditHourList.get(position);


            if (model.getTw() != null && !model.getTw().isEmpty()) {
                holder1.tm.setText(model.getTw());
            } else {
                holder1.tm.setText("0.0");
            }

            if (model.getOw() != null && !model.getOw().isEmpty()) {
                double value = Double.parseDouble(model.getOw());
                DecimalFormat decimalFormat = new DecimalFormat("#.###");
                String formattedValue = decimalFormat.format(value);
                holder1.om.setText(formattedValue);
            } else {
                holder1.om.setText("0.0");
            }

            if (model.getGrade() != null && !model.getGrade().isEmpty()) {
                holder1.grade.setText(model.getGrade());
            } else {
                holder1.grade.setText("N/A");
            }

            if (model.getSubjectName() != null && !model.getSubjectName().isEmpty()) {
                holder1.subjectName.setText(model.getSubjectName());
            } else {
                holder1.subjectName.setText("N/A");
            }

            if (!creditH.isEmpty()) {
                holder1.creditHour.setText(creditH);
            } else {
                holder1.creditHour.setText("N/A");
            }
        } else {

            CustomViewHolder customHolder = (CustomViewHolder) holder;

            if (creditHours != null && !creditHours.isEmpty()) {
                customHolder.ch.setText(creditHours);
            } else {
                customHolder.ch.setText("Error");
            }

            if (totalMarks != null && !totalMarks.isEmpty()) {
                customHolder.total.setText(totalMarks);
            } else {
                customHolder.total.setText("Error");
            }

            if (obtainedMarks != null && !obtainedMarks.isEmpty()) {
                customHolder.obtained.setText(obtainedMarks);
            } else {
                customHolder.obtained.setText("Error");
            }

            if (obtainedGrade != null && !obtainedGrade.isEmpty()) {
                customHolder.grades.setText(obtainedGrade);
            } else {
                customHolder.grades.setText("Error");
            }

            if (obtainedGradePoints != null && !obtainedGradePoints.isEmpty()) {
                customHolder.points.setText(obtainedGradePoints);
            } else {
                customHolder.points.setText("Error");
            }

            if (percentage != null && !percentage.isEmpty()) {
                customHolder.percent.setText(percentage);
            } else {
                customHolder.percent.setText("Error");
            }



        }


    }


    @Override
    public int getItemCount() {
        if(check == 1) {
            return subjectTotalDetailModels.size();
        }else{
            return 1;
        }
    }

    public class ShowSubjectMarksViewHolder extends RecyclerView.ViewHolder {

        EditText tm, om, grade, subjectName, creditHour;

        public ShowSubjectMarksViewHolder(@NonNull View itemView) {
            super(itemView);


            subjectName = itemView.findViewById(R.id.semSubjectName);
            tm = itemView.findViewById(R.id.semSubjectTotal);
            om = itemView.findViewById(R.id.semSubjectObtained);
            grade = itemView.findViewById(R.id.semSubjectGrade);
            creditHour = itemView.findViewById(R.id.semSubjectCreditHour);


            ImageButton delBtn;
            delBtn = itemView.findViewById(R.id.delButtonOfSubject);

            delBtn.setOnClickListener(v -> {
                int itemPosition = getAdapterPosition();

                if (itemPosition != RecyclerView.NO_POSITION) {
                    deleteSingleSubjectFromSemester(itemPosition, subjectName.getText().toString());
                }
            });

        }
    }


    public class CustomViewHolder extends RecyclerView.ViewHolder {
        TextView ch, total, obtained, grades, points, percent;

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);
            ch = itemView.findViewById(R.id.totalHours);
            total = itemView.findViewById(R.id.TotalWeightage);
            obtained = itemView.findViewById(R.id.ObtainedWeightage);
            grades = itemView.findViewById(R.id.Grade);
            points = itemView.findViewById(R.id.GradePoint);
            percent = itemView.findViewById(R.id.obtainePercentage);
        }
    }


    public void deleteSingleSubjectFromSemester(int position, String subjectName) {
        Dialog deleteConfirmationDialog = new Dialog(context);
        deleteConfirmationDialog.setContentView(R.layout.custom_popup);

        TextView mainHeading = deleteConfirmationDialog.findViewById(R.id.textView5);
        TextView nameOfSubject = deleteConfirmationDialog.findViewById(R.id.textView11);
        TextView description = deleteConfirmationDialog.findViewById(R.id.textView7);

        String main = "Do you really want to delete this subject?";
        String desc = "This will also delete all assessment data linked with this subject.";

        if (mainHeading != null) {
            mainHeading.setText(main.toUpperCase());
        }

        if (nameOfSubject != null) {
            nameOfSubject.setText(subjectName.toUpperCase());
        }

        if (description != null) {
            description.setText(desc.toUpperCase());
        }

        AppCompatButton btnYes = deleteConfirmationDialog.findViewById(R.id.btnYes);
        if (btnYes != null) {
            btnYes.setOnClickListener(v -> {
                // Handle "Yes" button click
                // Perform delete operation or any other action
                deleteConfirmationDialog.dismiss(); // Close the popup
                removeSubjectFromFirebase(position, subjectName);
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

    }


    private void removeSubjectFromFirebase(int position, String subjectName2) {
        if (!subjectTotalDetailModels.isEmpty() && position >= 0 && position < subjectTotalDetailModels.size()) {

            gradeItemReference.addListenerForSingleValueEvent(new ValueEventListener() {
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

                                    gradeItemReference.child("semester" + semesterNo).child(subjectName).removeValue().addOnSuccessListener(unused -> {
                                        try {
                                            // Use Handler to update UI on the main thread
                                            new Handler(Looper.getMainLooper()).post(() -> {
                                                Toast.makeText(context, "Successfully deleted subject: " + subjectName2.toUpperCase(), Toast.LENGTH_SHORT).show();
                                                notifyItemRemoved(position);
                                            });
                                        } catch (Exception e) {
                                            Log.d("Errors_in_Code", "SemesterViseAdapter: " + e);
                                        }
                                    }).addOnFailureListener(e -> {
                                        // Use Handler to show Toast on the main thread
                                        new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(context, "Failed to Delete subject", Toast.LENGTH_SHORT).show());
                                    });

                                }

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
    }


}
