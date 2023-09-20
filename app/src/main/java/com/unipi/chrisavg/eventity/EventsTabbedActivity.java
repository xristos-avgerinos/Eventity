package com.unipi.chrisavg.eventity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.tabs.TabLayout;
import com.unipi.chrisavg.eventity.ui.EventsSearch.Tab1Fragment;
import com.unipi.chrisavg.eventity.ui.EventsSearch.Tab2Fragment;
import androidx.appcompat.widget.SearchView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class EventsTabbedActivity extends AppCompatActivity  {

    public SearchView searchView;

    public boolean searchViewFlag = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events_tabbed);

        searchView = findViewById(R.id.search_view);
        // Βρισκουμε το κουμπί κλεισίματος (X)
        ImageView closeButton = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
        if (closeButton != null) {
            closeButton.setColorFilter(R.color.SearchViewColors); // Ορίζουμε το χρώμα του εικονιδίου κλεισίματος σε μαύρο
        }

        // Βρισκουμε το εικονίδιο αναζήτησης (magnifier)
        ImageView searchButton = searchView.findViewById(androidx.appcompat.R.id.search_mag_icon);
        if (searchButton != null) {
            searchButton.setColorFilter(R.color.SearchViewColors); // Ορίζουμε το χρώμα του εικονιδίου αναζητησης σε μαύρο
        }

        // Βρισκουμε το EditText μέσα στο SearchView
        EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);

        int textColor = getResources().getColor(R.color.SearchViewColors);
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
                // Δημιουργια ένος Bundle για να αποθηκεύσουμε τις πληροφορίες τοποθεσίας
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


        SearchView searchView = findViewById(R.id.search_view); // Find the SearchView


        LinearLayout buttonContainer = findViewById(R.id.buttonContainer);

        // Set click listeners for the toggle buttons
        ToggleButton[] toggleButtons = new ToggleButton[]{
                findViewById(R.id.student_party),
                findViewById(R.id.festivals),
                findViewById(R.id.live_music),
                findViewById(R.id.club),
                findViewById(R.id.ArtsAndEntertainment),
                findViewById(R.id.Business),
                findViewById(R.id.concerts),
                findViewById(R.id.Tech),
                findViewById(R.id.Fashion),
                findViewById(R.id.Science),
                findViewById(R.id.sport_events),

        };

        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String filter) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String filter) {

                if (searchViewFlag)
                    uncheckAllToggleButtons(toggleButtons);

                Tab1Fragment.searchViewFilteringTab1(filter); //κανουμε filtering στο listview του Tab1
                Tab2Fragment.searchViewFilteringTab2(filter); //κανουμε filtering στο map του Tab2

                return true;
            }
        });

        HorizontalScrollView horizontalScrollView = findViewById(R.id.horizontalScrollView);

        for (final ToggleButton toggleButton : toggleButtons) {
            toggleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //θετουμε το flag σε false ωστε να μην εκτελεσει την uncheckAllToggleButtons στο onQueryTextChange
                    // του searchView αφου δεν θελουμε να τα κανει uncheck οταν προερχεται απο την setOnClickListener καποιου toggleButton
                    searchViewFlag = false;

                    // Clear the search query and collapse the SearchView
                    searchView.setQuery("", false);
                    searchView.clearFocus();


                    if (toggleButton.isChecked()) {
                        // Reorder the button to the beginning
                        buttonContainer.removeView(toggleButton);
                        buttonContainer.addView(toggleButton, 0);

                    } else {
                        // Reorder the button to its original position
                        int originalPosition = findOriginalPosition(toggleButton.getId());
                        buttonContainer.removeView(toggleButton);
                        buttonContainer.addView(toggleButton, originalPosition);
                    }

                    searchViewFlag = true;

                    //κανουμε filtering στο listview του Tab1
                    Tab1Fragment.toggleButtonsContainerFilteringTab1(getSelectedToggleButtonsText(toggleButtons));

                    //κανουμε filtering στο map του Tab2
                    Tab2Fragment.toggleButtonsContainerFilteringTab2(getSelectedToggleButtonsText(toggleButtons));

                    horizontalScrollView.smoothScrollTo(0, 0); //μεταφερουμε στην εστιαση παλι στο πρωτο toggle button

                }
            });


        }

    }

    private void uncheckAllToggleButtons(ToggleButton[] toggleButtons) {

        for (ToggleButton toggleButton : toggleButtons) {
            if (toggleButton.isChecked()) {
                toggleButton.setChecked(false);
            }
        }
    }

    private List<String> getSelectedToggleButtonsText(ToggleButton[] toggleButtons) {
        List<String> selectedTexts = new ArrayList<>();
        for (ToggleButton toggleButton : toggleButtons) {
            if (toggleButton.isChecked()) {
                selectedTexts.add(toggleButton.getText().toString());
            }
        }
        return selectedTexts;
    }


    // Helper method to find the original position of a button by its ID
    private int findOriginalPosition(int buttonId) {
        ToggleButton[] toggleButtons = new ToggleButton[]{
                findViewById(R.id.student_party),
                findViewById(R.id.festivals),
                findViewById(R.id.live_music),
                findViewById(R.id.club),
                findViewById(R.id.ArtsAndEntertainment),
                findViewById(R.id.Business),
                findViewById(R.id.concerts),
                findViewById(R.id.Tech),
                findViewById(R.id.Fashion),
                findViewById(R.id.Science),
                findViewById(R.id.sport_events),

        };

        for (int i = 0; i < toggleButtons.length; i++) {
            if (toggleButtons[i].getId() == buttonId) {
                return i;
            }
        }
        return -1; // Button not found
    }

    private void setStatusBarWhite(AppCompatActivity activity){
        //Make status bar icons color white
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            activity.getWindow().setStatusBarColor(getResources().getColor(R.color.white));
        }
    }

}
