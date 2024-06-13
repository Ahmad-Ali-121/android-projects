package com.example.gpacalculator.byahmadalikhan.model;

public class AssessmentDetailsModel {

    String assessmentType;
    String totalMarks;
    String totalWeightage;
    String obtainedMarks;
    String obtainedPercentage;

    public AssessmentDetailsModel() {
    }

    public AssessmentDetailsModel(String assessmentType, String totalMarks, String totalWeightage, String obtainedMarks, String obtainedPercentage) {
        this.assessmentType = assessmentType;
        this.totalMarks = totalMarks;
        this.totalWeightage = totalWeightage;
        this.obtainedMarks = obtainedMarks;
        this.obtainedPercentage = obtainedPercentage;
    }

    public String getAssessmentType() {
        return assessmentType;
    }

    public void setAssessmentType(String assessmentType) {
        this.assessmentType = assessmentType;
    }

    public String getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(String totalMarks) {
        this.totalMarks = totalMarks;
    }

    public String getTotalWeightage() {
        return totalWeightage;
    }

    public void setTotalWeightage(String totalWeightage) {
        this.totalWeightage = totalWeightage;
    }

    public String getObtainedMarks() {
        return obtainedMarks;
    }

    public void setObtainedMarks(String obtainedMarks) {
        this.obtainedMarks = obtainedMarks;
    }

    public String getObtainedPercentage() {
        return obtainedPercentage;
    }

    public void setObtainedPercentage(String obtainedPercentage) {
        this.obtainedPercentage = obtainedPercentage;
    }
}
