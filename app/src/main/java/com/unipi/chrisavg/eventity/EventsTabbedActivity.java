package com.unipi.chrisavg.eventity;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.tabs.TabLayout;
import com.unipi.chrisavg.eventity.ui.EventsSearch.Tab1Fragment;
import com.unipi.chrisavg.eventity.ui.EventsSearch.Tab2Fragment;
import androidx.appcompat.widget.SearchView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class EventsTabbedActivity extends AppCompatActivity  {

    public static SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events_tabbed);

        searchView = findViewById(R.id.search_view);
        // Find the close (X) button
        ImageView closeButton = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
        if (closeButton != null) {
            closeButton.setColorFilter(R.color.SearchViewColors); // Set the color of the close icon to black
        }

        // Find the search (magnifier) icon
        ImageView searchButton = searchView.findViewById(androidx.appcompat.R.id.search_mag_icon);
        if (searchButton != null) {
            searchButton.setColorFilter(R.color.SearchViewColors); // Set the color of the search icon to black
        }
        EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text); // Find the EditText inside the SearchView

        int textColor = getResources().getColor(R.color.SearchViewColors); // Replace with the color you want
        searchEditText.setTextColor(textColor);
        searchEditText.setHintTextColor(textColor);

        searchView.setQueryHint("Search event...");

        Intent intent = getIntent();
        double latitude = intent.getDoubleExtra("latitude", 0.0);
        double longitude = intent.getDoubleExtra("longitude", 0.0);

        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        String cityName = "";

        try {
            List<Address> addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    1
            );

            if (!addresses.isEmpty()) {
                Address address = addresses.get(0);
                cityName = address.getLocality();
            }else{
                cityName = "Untrackable location";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        TextView location_textView = findViewById(R.id.location_textView);
        location_textView.setText(cityName);


        location_textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(EventsTabbedActivity.this,MainActivity.class);
                startActivity(intent1);
                finish();
            }
        });



        ViewPager viewPager = findViewById(R.id.view_pager);
        TabLayout tabLayout = findViewById(R.id.tabs);

        viewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @NonNull
            @Override
            public Fragment getItem(int position) {
                // Create a Bundle to hold the location information
                Bundle args = new Bundle();
                args.putDouble("latitude", latitude);
                args.putDouble("longitude", longitude);
                switch (position) {
                    case 0:
                        Tab1Fragment tab1Fragment = new Tab1Fragment();
                        tab1Fragment.setArguments(args);

                        return tab1Fragment;
                    case 1:
                        Tab2Fragment tab2Fragment = new Tab2Fragment();
                        tab2Fragment.setArguments(args);

                        return tab2Fragment;
                    default:
                        return null;
                }
            }

            @Override
            public int getCount() {
                return 2;
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                switch (position) {
                    case 0:
                        return "Events";
                    case 1:
                        return "Map";
                    default:
                        return null;
                }
            }
        });

        tabLayout.setupWithViewPager(viewPager);
        setStatusBarWhite(this);

    }

    private void setStatusBarWhite(AppCompatActivity activity){
        //Make status bar icons color white
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            activity.getWindow().setStatusBarColor(getResources().getColor(R.color.white));
        }
    }

}
