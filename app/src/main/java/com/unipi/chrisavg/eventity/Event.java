package com.unipi.chrisavg.eventity;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Event {
    private String key;
    private String Title;
    private Timestamp Date;
    private String Location;
    private GeoPoint Geopoint;
    private String OrganizerId;
    private String PhotoURL;
    List<String> Types = new ArrayList<>();

    public Event() {
    }

    public Event(String title, Timestamp date, String location,GeoPoint geopoint, String organizerId, String photoUrl, List<String> types) {
        this.Title = title;
        this.Date = date;
        this.Location = location;
        this.Geopoint = geopoint;
        this.OrganizerId = organizerId;
        this.PhotoURL = photoUrl;
        this.Types = types;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        this.Title = title;
    }

    public Timestamp getDate() {
        return Date;
    }

    public void setDate(Timestamp date) {
        this.Date = date;
    }

    public String getLocation() {
        return Location;
    }

    public void setLocation(String location) {
        this.Location = location;
    }

    public String getOrganizerId() {
        return OrganizerId;
    }

    public void setOrganizerId(String organizerId) {
        this.OrganizerId = organizerId;
    }

    public String getPhotoURL() {
        return PhotoURL;
    }

    public void setPhotoURL(String photoURL) {
        PhotoURL = photoURL;
    }

    public List<String> getTypes() {
        return Types;
    }

    public void setTypes(List<String> types) {
        this.Types = types;
    }

    public GeoPoint getGeopoint() {
        return Geopoint;
    }

    public void setGeopoint(GeoPoint geopoint) {
        Geopoint = geopoint;
    }

    public String getDateToCustomFormat() {
        // Create a Date object from the Timestamp
        Date date = this.Date.toDate();

        // Create a Calendar instance to extract components
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        // Define day of week and month names for both Greek and English
        String[] dayOfWeekNamesGreek = {"Κυριακή", "Δευτέρα", "Τρίτη", "Τετάρτη", "Πέμπτη", "Παρασκευή", "Σάββατο"};
        String[] monthNamesGreek = {"Ιαν", "Φεβ", "Μαρ", "Απρ", "Μαι", "Ιουν", "Ιουλ", "Αυγ", "Σεπτ", "Οκτ", "Νοεμ", "Δεκ"};

        String[] dayOfWeekNamesEnglish = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        String[] monthNamesEnglish = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sept", "Oct", "Nov", "Dec"};

        // Extract components from Calendar
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1; // Adjust for 0-based indexing
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int year = calendar.get(Calendar.YEAR);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // Get the current locale
        Locale currentLocale = Locale.getDefault();
        String[] dayOfWeekNames = (currentLocale.getLanguage().equals("el")) ? dayOfWeekNamesGreek : dayOfWeekNamesEnglish;
        String[] monthNames = (currentLocale.getLanguage().equals("el")) ? monthNamesGreek : monthNamesEnglish;

        String formattedDate = dayOfWeekNames[dayOfWeek] + ", " + day + " " + monthNames[month] + " " + year +  " • " +
                String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
        return formattedDate;
    }


}
