package com.unipi.chrisavg.eventity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.android.clustering.ClusterManager;
import com.unipi.chrisavg.eventity.ui.EventsSearch.Tab2Fragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SpecificEventDetailedActivity extends AppCompatActivity implements OnMapReadyCallback {

    TextView Title,Date,Time,ApproximateLocation,ExactLocation;
    TextView OrganizerName,OrganizerPhone;
    ImageView imageView;

    FirebaseAuth auth;
    CollectionReference Organizers,reservations;
    FirebaseFirestore db;

    Organizer organizer;

    private static GoogleMap mMap;
    SupportMapFragment mapFragment;
    Toolbar toolbar;

    double latitude;
    double longitude;

    public static Event receivedEvent;

    public static boolean shouldReload = false;

    private View loadingLayout; // Reference to the loading layout

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specific_event_detailed);


        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        Organizers = db.collection("Organizers");
        reservations = db.collection("Reservations");

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        // getSupportActionBar().hide(); //hide the title bar
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SpecificEventDetailedActivity.super.onBackPressed();
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
        imageView = findViewById(R.id.imageView);

        OrganizerName = findViewById(R.id.organizerName);
        OrganizerPhone = findViewById(R.id.organizerPhone);

        loadingLayout = findViewById(R.id.loading_layout);

        Organizers.document(receivedEvent.getOrganizerId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists()){
                            organizer = documentSnapshot.toObject(Organizer.class);
                            OrganizerName.setText(organizer.getFirstname() + " " + organizer.getLastname());
                            OrganizerPhone.setText(organizer.getPhoneNumber());
                        }
                    }
                });

        Glide.with(SpecificEventDetailedActivity.this)
                .load(receivedEvent.getPhotoURL())
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache the image for better performance
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        // Set the drawable as the background of the CollapsingToolbarLayout
                        imageView.setImageDrawable(resource);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        // Called when the resource is cleared from the ImageView
                        // You can handle this case if needed
                    }
                });


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

        GridLayout gridLayout = findViewById(R.id.related_gridview);

        for (String relatedType : receivedEvent.getTypes()) {
            ToggleButton toggleButton = new ToggleButton(this);
            // Set layout parameters
            GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
            layoutParams.width = GridLayout.LayoutParams.WRAP_CONTENT;
            layoutParams.height = GridLayout.LayoutParams.WRAP_CONTENT;
            layoutParams.setGravity(Gravity.FILL);
            int marginInPixels = getResources().getDimensionPixelSize(R.dimen.toggle_button_margin);
            layoutParams.setMargins(marginInPixels, marginInPixels, marginInPixels, marginInPixels);
            toggleButton.setLayoutParams(layoutParams);
            // Set padding
            int paddingRight = getResources().getDimensionPixelSize(R.dimen.toggle_button_padding_right);
            int paddingLeft = getResources().getDimensionPixelSize(R.dimen.toggle_button_padding_left);
            toggleButton.setPadding(paddingLeft, 0, paddingRight, 0);
            toggleButton.setText(relatedType);
            toggleButton.setTextOff(relatedType);
            toggleButton.setTextOn(relatedType);
            toggleButton.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            // Set text size
            float textSize = getResources().getDimension(R.dimen.toggle_button_text_size);
            toggleButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            // Set typeface
            Typeface typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL);
            toggleButton.setTypeface(typeface);
            // Set clickable
            toggleButton.setClickable(false);
            toggleButton.setTextColor(getResources().getColor(R.color.toggle_buttons_color));
            //set background
            toggleButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.toggle_button_background));
            // Set textAllCaps to false
            toggleButton.setAllCaps(false);

            gridLayout.addView(toggleButton);
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        Button getTicketsButton = findViewById(R.id.GetTickets);


        reservations.whereEqualTo("userId", auth.getUid()).whereEqualTo("eventId",receivedEvent.getKey()).get()
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && querySnapshot.getDocuments().size() == 1) {
                            getTicketsButton.setText("See your ticket");
                            getTicketsButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(SpecificEventDetailedActivity.this, UserTicket.class);
                                    intent.putExtra("ReservationID",querySnapshot.getDocuments().get(0).getId());
                                    intent.putExtra("SendingActivity","SpecificEventDetailedActivity");
                                    startActivity(intent);
                                    //finish();
                                }
                            });

                        }else{

                            if (receivedEvent.getCapacity() == receivedEvent.getReservedTickets()){
                                getTicketsButton.setClickable(false);
                                getTicketsButton.setEnabled(false);
                                getTicketsButton.setText("Sold Out");
                                getTicketsButton.setBackgroundColor(getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral80));
                            }

                            getTicketsButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(SpecificEventDetailedActivity.this, CheckOutTicket.class);
                                    intent.putExtra("event", receivedEvent);
                                    startActivity(intent);
                                }
                            });
                        }

                    } else {
                        // Handle errors
                        //DisplaySnackbar(task.getException().getLocalizedMessage());
                    }

                    loadingLayout.setVisibility(View.GONE);

                });

    }

    private void setStatusBarCustomColor(AppCompatActivity activity) {
        //Make status bar icons color white
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            activity.getWindow().setStatusBarColor(getResources().getColor(R.color.statusBarColor));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng eventLocation = new LatLng(latitude, longitude);

        Marker eventMarker = mMap.addMarker(new MarkerOptions().position(eventLocation).title(receivedEvent.getTitle()));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(eventLocation, 15));

    }

    public void shareEvent(View view) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (menu instanceof MenuBuilder) {
            ((MenuBuilder) menu).setOptionalIconsVisible(true);
        }
        getMenuInflater().inflate(R.menu.actionbar1,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {

            case R.id.ShareEvent:
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                String eventDetails = "Check out \"" + receivedEvent.getTitle() + "\"on Eventity! \n\n" +
                        "Date: " + receivedEvent.getDateToCustomFormat() + "\n\n" +
                        "Location: " + receivedEvent.getLocation() + "\n\n" +
                        "Event details: https://eventity.com/event/123";
                shareIntent.putExtra(Intent.EXTRA_TEXT, eventDetails);

                // Start the Android's built-in sharing activity
                startActivity(Intent.createChooser(shareIntent, "Share Event"));
                break;

            case R.id.ContactOrganizer:
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto:" + organizer.getEmail()));
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{organizer.getEmail()});
                startActivity(emailIntent);
                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (shouldReload) {
            recreate();
            shouldReload = false; // Reset the flag to prevent continuous reloading
        }
    }

}