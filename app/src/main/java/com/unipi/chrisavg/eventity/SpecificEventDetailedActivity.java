package com.unipi.chrisavg.eventity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;



import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class SpecificEventDetailedActivity extends AppCompatActivity {

    TextView Title,Date,Time,ApproximateLocation,ExactLocation;
    ImageView imageView;

    Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specific_event_detailed);

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

        Event receivedEvent = null;
        if (intent != null) {
            receivedEvent = intent.getParcelableExtra("event");
        }



        Title = findViewById(R.id.title);
        Date = findViewById(R.id.Date);
        Time = findViewById(R.id.Time);
        ApproximateLocation= findViewById(R.id.ApproximateLocation);
        ExactLocation= findViewById(R.id.ExactLocation);
        imageView = findViewById(R.id.imageView);

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

        double latitude = intent.getDoubleExtra("latitude", 0.0);
        double longitude = intent.getDoubleExtra("longitude", 0.0);

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


    }

    private void setStatusBarCustomColor(AppCompatActivity activity){
        //Make status bar icons color white
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            activity.getWindow().setStatusBarColor(getResources().getColor(R.color.statusBarColor));
        }
    }
}