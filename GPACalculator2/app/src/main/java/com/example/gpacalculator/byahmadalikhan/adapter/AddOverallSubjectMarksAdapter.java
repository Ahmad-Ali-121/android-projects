package com.example.gpacalculator.byahmadalikhan.adapter;

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
import com.example.gpacalculator.byahmadalikhan.fragment.SubjectDetailsFragment;
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

public class AddOverallSubjectMarksAdapter extends RecyclerView.Adapter<AddOverallSubjectMarksAdapter.ShowSubjectMarksViewHolder> {

    Context context;
    ArrayList<SubjectTotalDetailsModel> subjectTotalDetailModels;
    private DatabaseReference gradeItemReference;

    private SubjectDetailsFragment subjectDetailsFragment;


    public AddOverallSubjectMarksAdapter(Context context, ArrayList<SubjectTotalDetailsModel> subjectTotalDetailModels) {
        this.context = context;
        this.subjectTotalDetailModels = subjectTotalDetailModels;

        FirebaseUser userId = FirebaseAuth.getInstance().getCurrentUser();
        if (userId != null) {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            gradeItemReference = database.getReference("user").child(userId.getUid()).child("subjectsDataSemesterVise");
        }
    }

    @NonNull
    @Override
    public ShowSubjectMarksViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_added_subject_details, parent, false);
        return new ShowSubjectMarksViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShowSubjectMarksViewHolder holder, int position) {

        SubjectTotalDetailsModel model = subjectTotalDetailModels.get(position);

        if (model.getOw() != null && !model.getOw().isEmpty() && model.getOw().matches(".*\\d.*")) {
            double value = Double.parseDouble(model.getOw());
            DecimalFormat decimalFormat = new DecimalFormat("#.###");
            String formattedValue = decimalFormat.format(value);
            holder.om.setText(formattedValue);
        }else{
            holder.om.setText(model.getOw());
        }

        if (model.getTw() != null && !model.getTw().isEmpty() && model.getTw().matches(".*\\d.*")) {
            double value1 = Double.parseDouble(model.getTw());
            DecimalFormat decimalFormat1 = new DecimalFormat("#.###");
            String formattedValue1 = decimalFormat1.format(value1);
            holder.tm.setText(formattedValue1);
        }else{
            holder.tm.setText(model.getTw());
        }

        holder.grade.setText(model.getGradePoint());
        holder.subjectName.setText(model.getSubjectName());
        holder.semesterNo.setText(model.getSemester());

    }


    @Override
    public int getItemCount() {
        return subjectTotalDetailModels.size();
    }

    public class ShowSubjectMarksViewHolder extends RecyclerView.ViewHolder {

        EditText tm, om, grade, subjectName, semesterNo;

        public ShowSubjectMarksViewHolder(@NonNull View itemView) {
            super(itemView);


            subjectName = itemView.findViewById(R.id.subjectName);
            tm = itemView.findViewById(R.id.subjectTotal);
            om = itemView.findViewById(R.id.subjectObtained);
            grade = itemView.findViewById(R.id.subjectGrade);
            semesterNo = itemView.findViewById(R.id.semesterNo);


            ImageButton delBtn;
            delBtn = itemView.findViewById(R.id.delButtonOfSubject);

            delBtn.setOnClickListener(v -> {
                int itemPosition = getAdapterPosition();
                if (itemPosition != RecyclerView.NO_POSITION) {
                    deleteItem(itemPosition, subjectName.getText().toString());
                }
            });


        }

        public EditText getSubjectName() {
            return subjectName;
        }


    }


    public void deleteItem(int position, String subjectName) {
        Log.d("SubjectAdapterInformation1", "Delete Item function called");
        Dialog deleteConfirmationDialog = new Dialog(context);
        deleteConfirmationDialog.setContentView(R.layout.custom_popup);

        TextView mainHeading = deleteConfirmationDialog.findViewById(R.id.textView5);
        TextView nameOfSubject = deleteConfirmationDialog.findViewById(R.id.textView11);
        TextView description = deleteConfirmationDialog.findViewById(R.id.textView7);

        String main = "Do you really want to delete this subject";
        String desc = "( This will also delete all assessment data linked with this subject )";

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
                removeItems(position, subjectName);
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


    private void removeItems(int position, String subjectName2) {
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
//                                                subjectDetailsFragment.calculateMarksOfSubject();
                                            });
                                        } catch (Exception e) {
                                            Log.d("Errors_in_Code", "ShowSubjectMarksAdapter: "+ e);
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
