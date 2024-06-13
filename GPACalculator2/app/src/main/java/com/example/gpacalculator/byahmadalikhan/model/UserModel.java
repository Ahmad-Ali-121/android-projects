package com.example.gpacalculator.byahmadalikhan.model;

public class UserModel {

    String name;
    String email;
    String password;
    String degree;
    Integer semesters;

    public UserModel(String name, String email, String password, String degree, Integer semesters) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.degree = degree;
        this.semesters = semesters;
    }

    public String getDegree() {
        return degree;
    }

    public void setDegree(String degree) {
        this.degree = degree;
    }

    public Integer getSemesters() {
        return semesters;
    }

    public void setSemesters(Integer semesters) {
        this.semesters = semesters;
    }

    public UserModel() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
