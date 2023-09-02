package com.unipi.chrisavg.eventity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserTicket extends AppCompatActivity {

    FirebaseAuth auth;
    CollectionReference Reservations,Events;
    FirebaseFirestore db;
    String receivedReservationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_ticket);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        Reservations = db.collection("Reservations");
        Events = db.collection("Events");

        Intent intent = getIntent();
        if (intent != null) {
            receivedReservationId = intent.getStringExtra("ReservationID");
        }

        final Reservation[] reservation = {null};
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
                }
            }
        });


    }
}