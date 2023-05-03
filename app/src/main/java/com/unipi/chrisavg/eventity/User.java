package com.unipi.chrisavg.eventity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class User {
    private String fullname;
    private int age;
    private String phoneNumber; //για τις κρατησεις-επικοινωνια με τους organizers
    private String token;
    List<String> preferences = new ArrayList<>();

    public User( String fullname) {
        this.fullname = fullname;
    }
    public User() {
    }

    public User(String fullname, int age, String phoneNumber, String token, List<String> preferences) {
        this.fullname = fullname;
        this.age = age;
        this.phoneNumber = phoneNumber;
        this.token = token;
        this.preferences = preferences;
    }

    public List<String> getPreferences() {
        return preferences;
    }

    public void setPreferences(List<String> preferences) {
        this.preferences = preferences;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }


}
