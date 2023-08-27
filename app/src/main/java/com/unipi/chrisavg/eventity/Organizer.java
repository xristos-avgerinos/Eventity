package com.unipi.chrisavg.eventity;


public class Organizer {
    private String AFM;
    private String age;
    private String firstname;
    private String lastname;
    private String phoneNumber;
    private String policeID;
    private String email;


    public Organizer(String AFM, String age, String firstname, String lastname, String phoneNumber, String policeID, String email) {
        this.AFM = AFM;
        this.age = age;
        this.firstname = firstname;
        this.lastname = lastname;
        this.phoneNumber = phoneNumber;
        this.policeID = policeID;
        this.email = email;
    }

    public Organizer() {
    }

    public String getAFM() {
        return AFM;
    }

    public void setAFM(String AFM) {
        this.AFM = AFM;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPoliceID() {
        return policeID;
    }

    public void setPoliceID(String policeID) {
        this.policeID = policeID;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
