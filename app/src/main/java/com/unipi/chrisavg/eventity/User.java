package com.unipi.chrisavg.eventity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class User {
    private String fullname;
    private String phoneNumber; //για τις κρατησεις-επικοινωνια με τους organizers
    List<String> preferences = new ArrayList<>();

    public User( String fullname) {
        this.fullname = fullname;
    }
    public User() {
    }

    public User(String fullname, String phoneNumber, List<String> preferences) {
        this.fullname = fullname;
        this.phoneNumber = phoneNumber;
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

}
