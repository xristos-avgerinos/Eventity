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

    private View loadingLayout;

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
        // getSupportActionBar().hide();
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserTicket.super.onBackPressed();
            }
        });

        setStatusBarCustomColor(this);

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
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(TicketQRCode); //περναμε στο imageView με το qrCode ticket το qrCode που παιρνουμε απο το reservation

                    purchaserName.setText(reservation[0].getTicketPersonFirstName() + " " + reservation[0].getTicketPersonLastName());


                    Events.document(reservation[0].getEventId()).get()
                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot2) {
                                    if(documentSnapshot2.exists()){
                                        event = documentSnapshot2.toObject(Event.class);

                                        // Χρησιμοποιουμε το RequestOptions για να ορίσουμε επιλογές για το Glide (προαιρετικά)
                                        RequestOptions requestOptions = new RequestOptions()
                                                .diskCacheStrategy(DiskCacheStrategy.ALL);

                                        Glide.with(getApplicationContext())
                                                .load(event.getPhotoURL())
                                                .apply(requestOptions)
                                                .into(new CustomTarget<Drawable>() {
                                                    @Override
                                                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                                        // Ορίζουμε τη φορτωμένη εικόνα του event ως φόντο του RelativeLayout
                                                        Scroll.setBackground(resource);

                                                    }

                                                    @Override
                                                    public void onLoadCleared(@Nullable Drawable placeholder) {
                                                        // Χειρισμός της περίπτωσης όπου η φόρτωση της εικόνας εκκαθαρίζεται
                                                    }
                                                });


                                        String paddedSeat = String.format("%0"+String.valueOf(event.getCapacity()).length()+"d", reservation[0].getSeat());
                                        seat.setText( paddedSeat);
                                        eventName.setText(event.getTitle());
                                        int index = event.getDateToCustomFormat().indexOf('•'); // Βρισκουμε τη θεση του χαρακτήρα '•'
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
                                                // Καθορίζουμε το γεωγραφικό πλάτος και μήκος της τοποθεσίας
                                                double latitude = event.getGeopoint().getLatitude();
                                                double longitude = event.getGeopoint().getLongitude();

                                                // Δημιουργουμε ενα intent για να ανοίξει ο χάρτης με οδηγίες προς τις καθορισμένες συντεταγμένες
                                                Uri gmmIntentUri = Uri.parse("geo:" + latitude + "," + longitude);
                                                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                                                mapIntent.setPackage("com.google.android.apps.maps"); // Αυτό διασφαλίζει ότι ανοίγει στην εφαρμογή Χάρτες Google, εάν είναι διαθέσιμη

                                                // Ελέγχουμε αν υπάρχει διαθέσιμη εφαρμογή χάρτη στη συσκευή.
                                                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                                                    startActivity(mapIntent);
                                                } else {
                                                    // Χειρισμός της περίπτωσης όπου δεν έχει εγκατασταθεί καμία εφαρμογή χαρτών
                                                    // Εμφανίζουμε ένα μήνυμα στο χρήστη
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
                Reservations.document(receivedReservationId).delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // Διαγραφή εγγράφου απο το reservations collection με επιτυχία

                                //Μειώνοτμε τα ReservedTickets του event κατά ένα
                                event.setReservedTickets(event.getReservedTickets()-1);

                                // Δημιουργήστε ένος map για να κρατήσουμε τα ενημερωμένα δεδομένα
                                Map<String, Object> updateData = new HashMap<>();
                                updateData.put("ReservedTickets", event.getReservedTickets());

                                // Ενημέρωση του εγγράφου
                                Events.document(reservation[0].getEventId()).update(updateData)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                // Ας διαγράψουμε την εικόνα από το storage επίσης
                                                FirebaseStorage storage = FirebaseStorage.getInstance();
                                                StorageReference storageRef = storage.getReference();
                                                String imagePath =   reservation[0].getEventId() + "-" + reservation[0].getUserId();
                                                StorageReference imageRef = storageRef.child(imagePath);
                                                imageRef.delete()
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                // Η εικόνα διαγράφηκε επιτυχώς
                                                                Toast.makeText(UserTicket.this, "Your order has been cancelled!", Toast.LENGTH_SHORT).show();

                                                                if (sendingActivity.equals("TicketsFragment")) {
                                                                    Intent intent = new Intent(UserTicket.this,MainActivity.class);
                                                                    intent.putExtra("OpenTicketsFragment",true);
                                                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Καθαρίστε την προς τα πίσω στοίβα
                                                                    startActivity(intent);
                                                                    finishAffinity();
                                                                } else if(sendingActivity.equals("CheckOutActivity")) {
                                                                    //ενημερώνουμε το receivedEvent του SpecificEventDetailedActivity για τα δεσμευμένα εισιτήρια
                                                                    SpecificEventDetailedActivity.receivedEvent.setReservedTickets(event.getReservedTickets());
                                                                    //έτσι ώστε να επαναφορτωθούν τα reservedTickets του receivedEvent του SpecificEventDetailedActivity
                                                                    SpecificEventDetailedActivity.shouldReload=true;
                                                                    finish();
                                                                }else if(sendingActivity.equals("SpecificEventDetailedActivity")){
                                                                    //ενημερώνουμε το receivedEvent του SpecificEventDetailedActivity για τα δεσμευμένα εισιτήρια
                                                                    SpecificEventDetailedActivity.receivedEvent.setReservedTickets(event.getReservedTickets());
                                                                    //έτσι ώστε να επαναφορτωθούν τα reservedTickets του receivedEvent του SpecificEventDetailedActivity
                                                                    SpecificEventDetailedActivity.shouldReload=true;
                                                                    finish();
                                                                }
                                                                //loadingLayout.setVisibility(View.GONE);

                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                // Χειρισμός σφαλμάτων σε περίπτωση αποτυχίας της διαγραφής
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