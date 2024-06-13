package com.example.gpacalculator.byahmadalikhan.model;

public class SubjectTotalDetailsModel {

    String subjectName;
    String grade;
    String gradePoint;
    String tw;
    String ow;
    String tm;
    String om;
    String semester;
    String creditHour;
    String subjectType;

    public SubjectTotalDetailsModel(String subjectName, String grade, String gradePoint, String tw, String ow, String tm, String om, String semester, String creditHour, String subjectType) {
        this.subjectName = subjectName;
        this.grade = grade;
        this.gradePoint = gradePoint;
        this.tw = tw;
        this.ow = ow;
        this.tm = tm;
        this.om = om;
        this.semester = semester;
        this.creditHour = creditHour;
        this.subjectType = subjectType;
    }

    public String getTm() {
        return tm;
    }

    public void setTm(String tm) {
        this.tm = tm;
    }

    public String getOm() {
        return om;
    }

    public void setOm(String om) {
        this.om = om;
    }

    public String getSubjectType() {
        return subjectType;
    }

    public void setSubjectType(String subjectType) {
        this.subjectType = subjectType;
    }




    public String getCreditHour() {
        return creditHour;
    }

    public void setCreditHour(String creditHour) {
        this.creditHour = creditHour;
    }

    public String getTw() {
        return tw;
    }

    public void setTw(String tw) {
        this.tw = tw;
    }

    public String getOw() {
        return ow;
    }

    public void setOw(String ow) {
        this.ow = ow;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getGradePoint() {
        return gradePoint;
    }

    public void setGradePoint(String gradePoint) {
        this.gradePoint = gradePoint;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getSubjectName() {
        return subjectName;
    }


    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public SubjectTotalDetailsModel() {
    }

    public SubjectTotalDetailsModel(String subjectName, String tw, String ow, String grade, String gradePoint, String semester, String creditHour, String subjectType) {
        this.subjectName = subjectName;
        this.grade = grade;
        this.gradePoint = gradePoint;
        this.tw = tw;
        this.ow = ow;
        this.semester = semester;
        this.creditHour = creditHour;
        this.subjectType = subjectType;
    }



}
