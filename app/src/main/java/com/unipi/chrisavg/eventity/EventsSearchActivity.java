package com.unipi.chrisavg.eventity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

public class EventsSearchActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events_search);

        LinearLayout customButton = findViewById(R.id.CurrentLocationButton);
        customButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // This code will be executed when the custom button is clicked.
                // You can perform any actions you want here.
                // For example, show a toast, start another activity, etc.
                Toast.makeText(EventsSearchActivity.this, "Button Clicked", Toast.LENGTH_SHORT).show();
            }
        });
    }


}