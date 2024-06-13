package com.example.gpacalculator.byahmadalikhan.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.gpacalculator.byahmadalikhan.R;


public class CalculateRequiredCgpaFragment extends Fragment {


    public CalculateRequiredCgpaFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_calculate_required_cgpa, container, false);
    }
}