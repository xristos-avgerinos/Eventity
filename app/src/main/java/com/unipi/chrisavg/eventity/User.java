package com.unipi.chrisavg.eventity;

import java.util.HashMap;

public class User {
    private String fullname;
    private int age;
    private String phoneNumber;
    private String token;
    HashMap<String, Boolean> preferences = new HashMap<String, Boolean>();

    public User( String fullname) {
        this.fullname = fullname;
    }
    public User() {
    }

    public User(String fullname, int age, String phoneNumber, String token, HashMap<String, Boolean> preferences) {
        this.fullname = fullname;
        this.age = age;
        this.phoneNumber = phoneNumber;
        this.token = token;
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

    public HashMap<String, Boolean> getPreferences() {
        return preferences;
    }

    public void setPreferences(HashMap<String, Boolean> preferences) {
        this.preferences = preferences;
    }
}
