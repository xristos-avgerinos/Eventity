package com.unipi.chrisavg.eventity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CheckOutTicket extends AppCompatActivity {

    TextView Title,Date,Time,ApproximateLocation,ExactLocation;
    EditText firstnameEditText,lastnameEditText,emailEditText;

    FirebaseAuth auth;
    CollectionReference Reservations,Events;
    FirebaseFirestore db;

    Toolbar toolbar;

    double latitude;
    double longitude;

    Event receivedEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_out_ticket);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        Reservations = db.collection("Reservations");
        Events = db.collection("Events");

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // getSupportActionBar().hide(); //hide the title bar
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckOutTicket.super.onBackPressed();
            }
        });


        setStatusBarCustomColor(this);

        Intent intent = getIntent();

        receivedEvent = null;
        if (intent != null) {
            receivedEvent = intent.getParcelableExtra("event");
        }

        Title = findViewById(R.id.title);
        Date = findViewById(R.id.Date);
        Time = findViewById(R.id.Time);
        ApproximateLocation= findViewById(R.id.ApproximateLocation);
        ExactLocation= findViewById(R.id.ExactLocation);


        Title.setText(receivedEvent.getTitle());

        int index = receivedEvent.getDateToCustomFormat().indexOf('•'); // Find the index of the • character

        Date.setText(receivedEvent.getDateToCustomFormat().substring(0, index).trim());
        Time.setText(receivedEvent.getDateToCustomFormat().substring(index+1).trim());
        ApproximateLocation.setText(receivedEvent.getLocation());

        latitude = receivedEvent.getGeopoint().getLatitude();
        longitude = receivedEvent.getGeopoint().getLongitude();

        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        String exLoc = "";

        try {
            List<Address> addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    1
            );

            if (!addresses.isEmpty()) {
                Address address = addresses.get(0);
                exLoc = address.getAddressLine(0);
            }else{
                exLoc = "Untrackable location";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        ExactLocation.setText(exLoc);

        firstnameEditText = findViewById(R.id.firstnameEditText);
        lastnameEditText = findViewById(R.id.lastnameEditText);
        emailEditText = findViewById(R.id.emailEditText);

        emailEditText.setKeyListener(null);
        emailEditText.setText(auth.getCurrentUser().getEmail());

    }

    private void setStatusBarCustomColor(AppCompatActivity activity) {
        //Make status bar icons color white
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            activity.getWindow().setStatusBarColor(getResources().getColor(R.color.statusBarColor));
        }
    }

    public void GetTicket(View view){
        if(TextUtils.isEmpty(firstnameEditText.getText().toString().trim())){
            firstnameEditText.requestFocus();
            DisplaySnackbar(view,"Please fill in your first name");
        }
        else if(TextUtils.isEmpty(lastnameEditText.getText().toString().trim())){
            lastnameEditText.requestFocus();
            DisplaySnackbar(view,"Please fill in your last name");
        }else{
            //store the reservation to db
            Reservation reservation = new Reservation(receivedEvent.getKey(),auth.getUid(),firstnameEditText.getText().toString(),lastnameEditText.getText().toString());
            Reservations.document().set(reservation)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                   @Override
                   public void onSuccess(Void unused) {

                       //add reserved tickets of event by 1
                       receivedEvent.setReservedTickets(receivedEvent.getReservedTickets()+1);


                       // Create a map to hold the updated data
                       Map<String, Object> updateData = new HashMap<>();
                       updateData.put("ReservedTickets", receivedEvent.getReservedTickets());

                       // Update the document
                       Events.document(receivedEvent.getKey()).update(updateData)
                               .addOnSuccessListener(new OnSuccessListener<Void>() {
                                   @Override
                                   public void onSuccess(Void aVoid) {
                                       // Event successfully updated
                                       Toast.makeText(CheckOutTicket.this, "Ticket Reserved", Toast.LENGTH_SHORT).show();

                                   }
                               })
                               .addOnFailureListener(new OnFailureListener() {
                                   @Override
                                   public void onFailure(@NonNull Exception e) {
                                       DisplaySnackbar(view,e.getLocalizedMessage());
                                   }
                               });
                   }
               })
               .addOnFailureListener(new OnFailureListener() {
                   @Override
                   public void onFailure(@NonNull Exception e) {
                       DisplaySnackbar(view,e.getLocalizedMessage());
                   }
               });
        }
    }

    void DisplaySnackbar(View view,String message){

        Snackbar snackbar =  Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
        View v = snackbar.getView();
        TextView tv = (TextView) v.findViewById(com.google.android.material.R.id.snackbar_text);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        // θετουμε τον μέγιστο αριθμό γραμμών για το μήνυμα Snackbar ώστε να εμφανίζεται όλο το κείμενο
        tv.setMaxLines(Integer.MAX_VALUE);
        snackbar.show();
    }
}