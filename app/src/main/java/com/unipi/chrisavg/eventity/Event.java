package com.unipi.chrisavg.eventity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Event implements Parcelable {
    private String key;
    private String Title;
    private Timestamp Date;
    private String Location;
    private GeoPoint Geopoint;
    private String OrganizerId;
    private String PhotoURL;
    List<String> Types = new ArrayList<>();
    private int Capacity;
    private int ReservedTickets;
    private double Price;


    private long UserMatchScore;

    public Event() {
    }

    public Event(String title, Timestamp date, String location,GeoPoint geopoint, String organizerId, String photoUrl, List<String> types, int capacity, int reservedTickets, double price) {
        this.Title = title;
        this.Date = date;
        this.Location = location;
        this.Geopoint = geopoint;
        this.OrganizerId = organizerId;
        this.PhotoURL = photoUrl;
        this.Types = types;
        this.Capacity = capacity;
        this.ReservedTickets = reservedTickets;
        this.Price = price;
    }

    protected Event(Parcel in) {
        key = in.readString();
        Title = in.readString();
        Date = in.readParcelable(Timestamp.class.getClassLoader());
        Location = in.readString();
        OrganizerId = in.readString();
        PhotoURL = in.readString();
        Types = in.createStringArrayList();
        // Διαβάστε τις ιδιότητες του GeoPoint(lat,long) από το Parcel και ανακατασκευάζουμε το αντικείμενο GeoPoint
        double latitude = in.readDouble();
        double longitude = in.readDouble();
        Geopoint = new GeoPoint(latitude, longitude);
        Capacity = in.readInt();
        ReservedTickets = in.readInt();
        Price = in.readDouble();
        UserMatchScore = in.readLong();
    }

    public static final Creator<Event> CREATOR = new Creator<Event>() {
        @Override
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };

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

    public long getUserMatchScore() {
        return UserMatchScore;
    }

    public void setUserMatchScore(long userMatchScore) {
        UserMatchScore = userMatchScore;
    }

    public int getCapacity() {
        return Capacity;
    }

    public void setCapacity(int capacity) {
        Capacity = capacity;
    }

    public int getReservedTickets() {
        return ReservedTickets;
    }

    public void setReservedTickets(int reservedTickets) {
        ReservedTickets = reservedTickets;
    }

    public double getPrice() {
        return Price;
    }

    public void setPrice(double price) {
        Price = price;
    }

    public String getDateToCustomFormat() {
        // Δημιουργία ενός αντικειμένου Date από το Timestamp
        Date date = this.Date.toDate();

        // Δημιουργια ενος αντικειμενου calendar για να εξαγάγουμε τα στοιχεία
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        // Ορίζουμε τα ονόματα της ημέρας της εβδομάδας και του μήνα τόσο για τα ελληνικά όσο και για τα αγγλικά
        String[] dayOfWeekNamesGreek = {"Κυριακή", "Δευτέρα", "Τρίτη", "Τετάρτη", "Πέμπτη", "Παρασκευή", "Σάββατο"};
        String[] monthNamesGreek = {"Ιαν", "Φεβ", "Μαρ", "Απρ", "Μαι", "Ιουν", "Ιουλ", "Αυγ", "Σεπτ", "Οκτ", "Νοεμ", "Δεκ"};

        String[] dayOfWeekNamesEnglish = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        String[] monthNamesEnglish = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sept", "Oct", "Nov", "Dec"};

        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1; // Προσαρμογη για 0-based indexing
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int year = calendar.get(Calendar.YEAR);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // Λήψη της τρέχουσας τοπικής γλώσσας ωστε να ξερουμε αν θα εμφανισουμε τα στοιχεια του calendar σε ελληνικα η αγγλικα
        Locale currentLocale = Locale.getDefault();
        String[] dayOfWeekNames = (currentLocale.getLanguage().equals("el")) ? dayOfWeekNamesGreek : dayOfWeekNamesEnglish;
        String[] monthNames = (currentLocale.getLanguage().equals("el")) ? monthNamesGreek : monthNamesEnglish;

        String formattedDate = dayOfWeekNames[dayOfWeek] + ", " + day + " " + monthNames[month] + " " + year +  " • " +
                String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
        return formattedDate;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(key);
        dest.writeString(Title);
        dest.writeParcelable(Date, flags);
        dest.writeString(Location);
        dest.writeString(OrganizerId);
        dest.writeString(PhotoURL);
        dest.writeStringList(Types);
        // Γράφουμε τις ιδιότητες GeoPoint στο Parcel
        dest.writeDouble(Geopoint.getLatitude());
        dest.writeDouble(Geopoint.getLongitude());
        dest.writeInt(Capacity);
        dest.writeInt(ReservedTickets);
        dest.writeDouble(Price);
        dest.writeLong(UserMatchScore);
    }
}
