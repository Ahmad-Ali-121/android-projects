package com.example.gpacalculator.byahmadalikhan.adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gpacalculator.byahmadalikhan.R;
import com.example.gpacalculator.byahmadalikhan.model.GradingDetailsModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AddGradingDetailsAdapter extends RecyclerView.Adapter<AddGradingDetailsAdapter.AddGradingDetailsViewHolder> {

    Context context;
    ArrayList<GradingDetailsModel> gradingDetailModels;
    private DatabaseReference gradeItemReference;

    public AddGradingDetailsAdapter(Context context, ArrayList<GradingDetailsModel> gradingDetailModels) {
        this.context = context;
        this.gradingDetailModels = gradingDetailModels;

        FirebaseUser userId = FirebaseAuth.getInstance().getCurrentUser();
        if (userId != null) {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            gradeItemReference = database.getReference("user").child(userId.getUid()).child("gradeInformation");
        }

    }

    @NonNull
    @Override
    public AddGradingDetailsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_grade_calculation, parent, false);
        return new AddGradingDetailsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddGradingDetailsViewHolder holder, int position) {

        GradingDetailsModel value = gradingDetailModels.get(position);

        holder.minValue.setText(value.getMinValue());
        holder.maxValue.setText(value.getMaxValue());
        holder.grade.setText(value.getGrade());
        holder.gradingPoint.setText(value.getGradePoints());

        // Add a null check and log an error
        if (holder.minValue == null || holder.maxValue == null || holder.grade == null || holder.gradingPoint == null) {
            Log.e("ViewHolderError", "One or more views in the ViewHolder are null");
            return;
        }

        holder.minValue.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                updateData(position, holder.getMinValue().getText().toString(), 1);
            }
        });

        holder.maxValue.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                updateData(position, holder.getMaxValue().getText().toString(), 2);
            }
        });

        holder.grade.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                updateData(position, holder.getGrade().getText().toString(), 3);
            }
        });

        holder.gradingPoint.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                updateData(position, holder.getGradingPoint().getText().toString(), 4);
            }
        });

    }

    public void updateData(int position, String updatedValue, int value) {


        if (position >= 0 && position < gradingDetailModels.size() && value == 1) {
            GradingDetailsModel gradingDetailsModelItem = gradingDetailModels.get(position);
            gradingDetailsModelItem.setMinValue(updatedValue);
        } else if (position >= 0 && position < gradingDetailModels.size() && value == 2) {
            GradingDetailsModel gradingDetailsModelItem = gradingDetailModels.get(position);
            gradingDetailsModelItem.setMaxValue(updatedValue);
        } else if (position >= 0 && position < gradingDetailModels.size() && value == 3) {
            GradingDetailsModel gradingDetailsModelItem = gradingDetailModels.get(position);
            gradingDetailsModelItem.setGrade(updatedValue);
        } else if (position >= 0 && position < gradingDetailModels.size() && value == 4) {
            GradingDetailsModel gradingDetailsModelItem = gradingDetailModels.get(position);
            gradingDetailsModelItem.setGradePoints(updatedValue);
        }


    }

    @Override
    public int getItemCount() {
        return gradingDetailModels.size();
    }


    public class AddGradingDetailsViewHolder extends RecyclerView.ViewHolder {

        EditText maxValue, minValue, grade, gradingPoint;

        public AddGradingDetailsViewHolder(@NonNull View itemView) {
            super(itemView);

            minValue = itemView.findViewById(R.id.minValuee);
            maxValue = itemView.findViewById(R.id.maxValuee);
            grade = itemView.findViewById(R.id.gradee);
            gradingPoint = itemView.findViewById(R.id.Points);

            // Attach TextWatcher to each EditText
            attachTextWatcher(minValue, 1);
            attachTextWatcher(maxValue, 2);
            attachTextWatcher(grade, 3);
            attachTextWatcher(gradingPoint, 4);

            ImageButton delBtn;
            delBtn = itemView.findViewById(R.id.deleteButton);

            delBtn.setOnClickListener(v -> {
                int itemPosition = getAdapterPosition();

                if (itemPosition != RecyclerView.NO_POSITION) {
                    deleteItem(itemPosition);
                }
            });

            // Add a null check and log an error
            if (minValue == null || maxValue == null || grade == null || gradingPoint == null) {
                Log.e("ViewHolderError", "One or more views in the ViewHolder are null");
            }
        }

        // Attach TextWatcher to EditText
        private void attachTextWatcher(EditText editText, int value) {
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        updateData(position, editable.toString(), value);
                    }
                }
            });
        }

        public EditText getMaxValue() { return maxValue;}

        public EditText getMinValue() {
            return minValue;
        }

        public EditText getGrade() {
            return grade;
        }

        public EditText getGradingPoint() {
            return gradingPoint;
        }

    }

    public void deleteItem(int position) {

        getUniqueKeyAtPosition(position, uniqueKey -> {
            if (uniqueKey != null) {
                removeItems(position, uniqueKey);
            }
        });
    }

    private void removeItems(int position, String uniqueKey) {
        if (uniqueKey != null && !gradingDetailModels.isEmpty() && position >= 0 && position < gradingDetailModels.size()) {

            gradeItemReference.child(uniqueKey).removeValue().addOnSuccessListener(unused -> {
                try {
                    // Use Handler to update UI on the main thread
                    new Handler(Looper.getMainLooper()).post(() -> {
                        Toast.makeText(context, "Item Deleted Successfully", Toast.LENGTH_SHORT).show();
                        notifyItemRemoved(position);
                    });
                } catch (Exception e) {
                    Log.d("Errors_in_Code", "AddGradingDetailsAdapter: "+ e);
                }
            }).addOnFailureListener(e -> {
                // Use Handler to show Toast on the main thread
                new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(context, "Failed to Delete item", Toast.LENGTH_SHORT).show());
            });
        }
    }


    private void getUniqueKeyAtPosition(int positionRetrieve, OnCompleteListener<String> onComplete) {
        if (gradeItemReference != null) {
            gradeItemReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String uniqueKey = null;
                    int index = 0;  // Initialize the index

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        if (index == positionRetrieve) {
                            uniqueKey = dataSnapshot.getKey();
                            break;
                        }
                        index++;
                    }

                    onComplete.onComplete(uniqueKey);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle onCancelled event if needed
                }
            });
        }
    }

    public interface OnCompleteListener<T> {
        void onComplete(T result);
    }


}

