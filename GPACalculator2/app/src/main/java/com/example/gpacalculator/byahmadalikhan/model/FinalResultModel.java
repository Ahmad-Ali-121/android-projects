package com.example.gpacalculator.byahmadalikhan.model;

public class FinalResultModel {
    String semester;
    String creditHour;
    String SGPA;
    String CGPA;
    String percentage;
    boolean check;

    public boolean getCheck() {
        return check;
    }

    public void setCheck(boolean check) {
        this.check = check;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getCreditHour() {
        return creditHour;
    }

    public void setCreditHour(String creditHour) {
        this.creditHour = creditHour;
    }

    public String getSGPA() {
        return SGPA;
    }

    public void setSGPA(String SGPA) {
        this.SGPA = SGPA;
    }

    public String getCGPA() {
        return CGPA;
    }

    public void setCGPA(String CGPA) {
        this.CGPA = CGPA;
    }

    public String getPercentage() {
        return percentage;
    }

    public void setPercentage(String percentage) {
        this.percentage = percentage;
    }

    public FinalResultModel(String semester, String creditHour, String SGPA, String CGPA, String percentage, boolean check ) {
        this.semester = semester;
        this.creditHour = creditHour;
        this.SGPA = SGPA;
        this.CGPA = CGPA;
        this.percentage = percentage;
        this.check = check;
    }
}
