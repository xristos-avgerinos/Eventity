package com.unipi.chrisavg.eventity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.ByteArrayOutputStream;
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

    private StorageReference storageRef;

    private View loadingLayout; // Reference to the loading layout

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_out_ticket);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        Reservations = db.collection("Reservations");
        Events = db.collection("Events");

        loadingLayout = findViewById(R.id.loading_layout);

        // Initialize the Firebase Storage reference
        storageRef = FirebaseStorage.getInstance().getReference();

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
            loadingLayout.setVisibility(View.VISIBLE);

            String eventData = "Event: " + receivedEvent.getTitle() + "\nDate: " + receivedEvent.getDateToCustomFormat() +
                    "\nTicket ID: " + receivedEvent.getKey() + "\nPerson name: " + firstnameEditText.getText().toString() + " " + lastnameEditText.getText().toString();
            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
            try {
                BitMatrix bitMatrix = multiFormatWriter.encode(eventData, BarcodeFormat.QR_CODE, 600, 600);
                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);

                // Upload QR Code Image to Firebase storage
                uploadImageToStorage(bitmap);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private void uploadImageToStorage(Bitmap bitmap) {
        // Create a unique filename or document ID for the image in Firestore
        String imageName = receivedEvent.getKey() + "-" + auth.getUid();

        // Create a reference to the Firebase Storage location
        StorageReference imageRef = storageRef.child(imageName);

        // Convert the Bitmap to a byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        // Upload the image to Firebase Storage
        UploadTask uploadTask = imageRef.putBytes(data);

        // Handle the upload success or failure
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Get the download URL of the uploaded image
                Task<Uri> downloadUriTask = imageRef.getDownloadUrl();
                downloadUriTask.addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri downloadUri) {
                        // Save the download URL to Firestore or use it as needed
                        saveReservationToFirestoreDb(downloadUri.toString());
                    }
                });
            }
        });
    }

    private void saveReservationToFirestoreDb(String ticketQRCodeUrl) {
        //add reserved tickets of event by 1
        receivedEvent.setReservedTickets(receivedEvent.getReservedTickets()+1);

        //store the reservation to db
        Reservation reservation = new Reservation(receivedEvent.getKey(),auth.getUid(),firstnameEditText.getText().toString(),lastnameEditText.getText().toString(),ticketQRCodeUrl,receivedEvent.getReservedTickets());

        // Create a new document with an auto-generated ID
        DocumentReference newReservationRef = Reservations.document();
        newReservationRef.set(reservation)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        String ReservationId = newReservationRef.getId();

                        // Create a map to hold the updated data
                        Map<String, Object> updateData = new HashMap<>();
                        updateData.put("ReservedTickets", receivedEvent.getReservedTickets());

                        // Update the document
                        Events.document(receivedEvent.getKey()).update(updateData)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        SpecificEventDetailedActivity.receivedEvent.setReservedTickets(receivedEvent.getReservedTickets());//inform receivedEvent of SpecificEventDetailedActivity for reserved tickets
                                        SpecificEventDetailedActivity.shouldReload=true; //so as to if we have sold out not to sell more tickets

                                        // Event successfully updated
                                        Intent intent = new Intent(CheckOutTicket.this,UserTicket.class);
                                        intent.putExtra("ReservationID",ReservationId);
                                        intent.putExtra("SendingActivity","CheckOutActivity");
                                        startActivity(intent);
                                        finish();

                                        //loadingLayout.setVisibility(View.GONE);

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        DisplaySnackbar(findViewById(android.R.id.content),e.getLocalizedMessage());
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        DisplaySnackbar(findViewById(android.R.id.content),e.getLocalizedMessage());
                    }
                });
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