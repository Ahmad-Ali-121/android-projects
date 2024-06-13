package com.example.gpacalculator.byahmadalikhan.model;

public class GradingDetailsModel {

    public GradingDetailsModel() {
    }

    public GradingDetailsModel(String minValue, String maxValue, String grade, String gradePoints) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.grade = grade;
        this.gradePoints = gradePoints;
    }

    public String getMinValue() {
        return minValue;
    }

    public void setMinValue(String minValue) {
        this.minValue = minValue;
    }

    public String getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(String maxValue) {
        this.maxValue = maxValue;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getGradePoints() {
        return gradePoints;
    }

    public void setGradePoints(String gradePoints) {
        this.gradePoints = gradePoints;
    }

    String minValue;
    String maxValue;
    String grade;
    String gradePoints;


}
