package com.example.gpacalculator.byahmadalikhan.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gpacalculator.byahmadalikhan.R;
import com.example.gpacalculator.byahmadalikhan.model.FinalResultModel;

import java.util.ArrayList;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.HomeViewHolder> {

    Context context;
    ArrayList<FinalResultModel> finalResultModels;

    public HomeAdapter(Context context, ArrayList<FinalResultModel> finalResultModels) {
        this.context = context;
        this.finalResultModels = finalResultModels;
    }

    @NonNull
    @Override
    public HomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cgpa_total_result, parent, false);
        return new HomeViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull HomeViewHolder holder, int position) {

        FinalResultModel model = finalResultModels.get(position);

        if (model.getCheck()) {
            holder.sem.setText(model.getSemester());
            try {
                double sgpaValue = Double.parseDouble(model.getSGPA());
                @SuppressLint("DefaultLocale") String formattedSGPA = String.format("%.2f", sgpaValue);
                holder.sgpa.setText(formattedSGPA);

                double cgpaValue = Double.parseDouble(model.getCGPA());
                @SuppressLint("DefaultLocale") String formattedCGPA = String.format("%.2f", cgpaValue);
                holder.cgpa.setText(formattedCGPA);

                double creditHoursValue = Double.parseDouble(model.getCreditHour());
                @SuppressLint("DefaultLocale") String formattedCreditHours = String.format("%.2f", creditHoursValue);
                holder.cr.setText(formattedCreditHours);


                double percentageValue = Double.parseDouble(model.getPercentage());
                @SuppressLint("DefaultLocale") String formattedPercentage = String.format("%.2f", percentageValue);
                holder.perc.setText(formattedPercentage);
            } catch (NumberFormatException e) {
                holder.sem.setText("Error");
                holder.sgpa.setText("while");
                holder.cgpa.setText("formatting");
                holder.cr.setText("the");
                holder.perc.setText("values");
            }

        } else {
            holder.sem.setText("Marks");
            holder.sgpa.setText("Are");
            holder.cgpa.setText("Not");
            holder.cr.setText("Set");
            holder.perc.setText("Correctly");
        }

    }

    @Override
    public int getItemCount() {
        return finalResultModels.size();
    }

    public static class HomeViewHolder extends RecyclerView.ViewHolder {

        TextView sem, sgpa, cgpa, cr, perc;


        public HomeViewHolder(@NonNull View itemView) {
            super(itemView);

            sem = itemView.findViewById(R.id.semester);
            sgpa = itemView.findViewById(R.id.sgpa);
            cgpa = itemView.findViewById(R.id.cgpa);
            cr = itemView.findViewById(R.id.credithour);
            perc = itemView.findViewById(R.id.percentage);

        }

    }


}
