package com.unipi.chrisavg.eventity;

import java.util.HashMap;

public class User {
    private String name;
    private String surname;
    private int age;
    private String phoneNumber;
    private String token;
    HashMap<String, Boolean> preferences = new HashMap<String, Boolean>();

    public User( String token) {
        this.token = token;
    }
    public User() {
        this.token = token;
    }

    public User(String name, String surname, int age, String phoneNumber, String token, HashMap<String, Boolean> preferences) {
        this.name = name;
        this.surname = surname;
        this.age = age;
        this.phoneNumber = phoneNumber;
        this.token = token;
        this.preferences = preferences;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
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
