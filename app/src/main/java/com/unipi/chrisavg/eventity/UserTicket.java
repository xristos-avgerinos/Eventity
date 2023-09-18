package com.unipi.chrisavg.eventity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.unipi.chrisavg.eventity.ui.tickets.TicketsFragment;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;

public class UserTicket extends AppCompatActivity {

    FirebaseAuth auth;
    CollectionReference Reservations,Events;
    FirebaseFirestore db;
    String receivedReservationId;
    Organizer organizer;
    Event event;
    TextView purchaserName,seat,eventName,eventDate,eventTime,eventLocation,eventPrice,eventOrganizer,map;
    final Reservation[] reservation = {null};
    String sendingActivity;

    private View loadingLayout; // Reference to the loading layout

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_ticket);

        ScrollView Scroll = findViewById(R.id.SV_general);
        purchaserName = findViewById(R.id.PurchaserName);
        seat = findViewById(R.id.Seat);
        eventName = findViewById(R.id.EventName);
        eventDate = findViewById(R.id.EventDate);
        eventTime = findViewById(R.id.EventTime);
        eventLocation = findViewById(R.id.EventLocation);
        map = findViewById(R.id.map);
        eventPrice = findViewById(R.id.EventPrice);
        eventOrganizer = findViewById(R.id.EventOrganizer);

        loadingLayout = findViewById(R.id.loading_layout);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        // getSupportActionBar().hide(); //hide the title bar
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserTicket.super.onBackPressed();
            }
        });

        setStatusBarCustomColor(this);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        Reservations = db.collection("Reservations");
        Events = db.collection("Events");


        Intent intent = getIntent();
        if (intent != null) {
            receivedReservationId = intent.getStringExtra("ReservationID");
            sendingActivity = intent.getStringExtra("SendingActivity");
        }


        Reservations.document(receivedReservationId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    reservation[0] = documentSnapshot.toObject(Reservation.class);
                    ImageView TicketQRCode = findViewById(R.id.TicketQRCode);
                    Glide.with(getApplicationContext())
                            .load(reservation[0].getTicketQRCodeURL())
                            .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache the image for better performance
                            .into(TicketQRCode);

                    purchaserName.setText(reservation[0].getTicketPersonFirstName() + " " + reservation[0].getTicketPersonLastName());


                    Events.document(reservation[0].getEventId()).get()
                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot2) {
                                    if(documentSnapshot2.exists()){
                                        event = documentSnapshot2.toObject(Event.class);

                                        // Use RequestOptions to set options for Glide (optional)
                                        RequestOptions requestOptions = new RequestOptions()
                                                .diskCacheStrategy(DiskCacheStrategy.ALL); // Cache the image for better performance

                                        Glide.with(getApplicationContext()) // Use the appropriate context (e.g., this for an Activity)
                                                .load(event.getPhotoURL())
                                                .apply(requestOptions) // Apply RequestOptions (optional)
                                                .into(new CustomTarget<Drawable>() {
                                                    @Override
                                                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                                        // Set the loaded image as the background of the RelativeLayout
                                                        Scroll.setBackground(resource);

                                                    }

                                                    @Override
                                                    public void onLoadCleared(@Nullable Drawable placeholder) {
                                                        // Handle the case where the image loading is cleared
                                                    }
                                                });


                                        String paddedSeat = String.format("%0"+String.valueOf(event.getCapacity()).length()+"d", reservation[0].getSeat());
                                        seat.setText( paddedSeat);
                                        eventName.setText(event.getTitle());
                                        int index = event.getDateToCustomFormat().indexOf('•'); // Find the index of the • character
                                        eventDate.setText(event.getDateToCustomFormat().substring(0, index).trim());
                                        eventTime.setText(eventTime.getText() + event.getDateToCustomFormat().substring(index+1).trim());
                                        eventLocation.setText(event.getLocation());

                                        double tempPrice = event.getPrice();
                                        if (tempPrice==0){
                                            eventPrice.setText("Free Ticket");
                                        }else{
                                            eventPrice.setText(String.valueOf(tempPrice));
                                        }

                                        db.collection("Organizers").document(event.getOrganizerId())
                                                .get()
                                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                        if(documentSnapshot.exists()){
                                                            organizer = documentSnapshot.toObject(Organizer.class);
                                                            eventOrganizer.setText(organizer.getFirstname() + " " + organizer.getLastname());
                                                        }
                                                        loadingLayout.setVisibility(View.GONE);
                                                    }
                                                });

                                        map.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                // Specify the latitude and longitude of the location
                                                double latitude = event.getGeopoint().getLatitude(); // Replace with the latitude of your location
                                                double longitude = event.getGeopoint().getLongitude(); // Replace with the longitude of your location

                                                // Create an intent to open the map with directions to the specified coordinates
                                                Uri gmmIntentUri = Uri.parse("geo:" + latitude + "," + longitude);
                                                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                                                mapIntent.setPackage("com.google.android.apps.maps"); // This ensures it opens in the Google Maps app if available

                                                // Check if there's a map application available on the device
                                                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                                                    startActivity(mapIntent);
                                                } else {
                                                    // Handle the case where no map application is installed
                                                    // You can display a message to the user or provide an alternative action
                                                    DisplaySnackbar(v,"Something went wrong!");
                                                }
                                            }
                                        });


                                    }
                                }
                            });

                }
            }
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
    public boolean onCreateOptionsMenu(Menu menu) {
        if (menu instanceof MenuBuilder) {
            ((MenuBuilder) menu).setOptionalIconsVisible(true);
        }
        getMenuInflater().inflate(R.menu.actionbar2,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {

            case R.id.CancelOrder:
                loadingLayout.setVisibility(View.VISIBLE);

                // Delete the document
                Reservations.document(receivedReservationId).delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // Document successfully deleted

                                //Lets reduce ReservedTickets of event by one

                                event.setReservedTickets(event.getReservedTickets()-1);

                                // Create a map to hold the updated data
                                Map<String, Object> updateData = new HashMap<>();
                                updateData.put("ReservedTickets", event.getReservedTickets());

                                // Update the document
                                Events.document(reservation[0].getEventId()).update(updateData)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                //Lets delete the image from storage too
                                                FirebaseStorage storage = FirebaseStorage.getInstance();
                                                StorageReference storageRef = storage.getReference();
                                                String imagePath =   reservation[0].getEventId() + "-" + reservation[0].getUserId();
                                                StorageReference imageRef = storageRef.child(imagePath);
                                                imageRef.delete()
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                // Image successfully deleted
                                                                Toast.makeText(UserTicket.this, "Your order has been cancelled!", Toast.LENGTH_SHORT).show();

                                                                if (sendingActivity.equals("TicketsFragment")) {
                                                                    Intent intent = new Intent(UserTicket.this,MainActivity.class);
                                                                    intent.putExtra("OpenTicketsFragment",true);
                                                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Clear the back stack
                                                                    startActivity(intent);
                                                                    finishAffinity();
                                                                } else if(sendingActivity.equals("CheckOutActivity")) {
                                                                    SpecificEventDetailedActivity.receivedEvent.setReservedTickets(event.getReservedTickets());//inform receivedEvent of SpecificEventDetailedActivity for reserved tickets
                                                                    SpecificEventDetailedActivity.shouldReload=true; //so as to reload the reservedTickets of receivedEvent of SpecificEventDetailedActivity
                                                                    finish();
                                                                }else if(sendingActivity.equals("SpecificEventDetailedActivity")){
                                                                    SpecificEventDetailedActivity.receivedEvent.setReservedTickets(event.getReservedTickets());//inform receivedEvent of SpecificEventDetailedActivity for reserved tickets
                                                                    SpecificEventDetailedActivity.shouldReload=true; //so as to reload the reservedTickets of receivedEvent of SpecificEventDetailedActivity
                                                                    finish();
                                                                }
                                                                //loadingLayout.setVisibility(View.GONE);

                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                // Handle errors if the deletion fails
                                                                Toast.makeText(UserTicket.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(UserTicket.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                            }
                                        });




                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(UserTicket.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                            }
                        });
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

    void DisplaySnackbar(View view,String message){

        Snackbar snackbar =  Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
        View v = snackbar.getView();
        TextView tv = (TextView) v.findViewById(com.google.android.material.R.id.snackbar_text);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        snackbar.show();
    }

}