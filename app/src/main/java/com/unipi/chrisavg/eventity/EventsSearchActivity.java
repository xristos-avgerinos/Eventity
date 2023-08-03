package com.unipi.chrisavg.eventity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.unipi.chrisavg.eventity.databinding.ActivityMapsBinding;

public class EventsSearchActivity extends AppCompatActivity{

    String imageUrl = "https://cdn-az.allevents.in/events7/banners/61ec31a907e18ae6aec7baacdecc6e2e91f36dfaeb5bb3c398141f24f3c5014c-rimg-w960-h503-gmir.jpg?v=1690810850"; // Replace with your image URL
    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events_search);

        ImageView imageView = findViewById(R.id.imageView);
        Glide.with(this)
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache the image for better performance
                .into(imageView);

    }

    public void maps(View view){
        Intent intent = new Intent(EventsSearchActivity.this,MapsActivity.class);
        startActivity(intent);
    }


}