package com.unipi.chrisavg.eventity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.tabs.TabLayout;
import com.unipi.chrisavg.eventity.ui.EventsSearch.Tab1Fragment;
import com.unipi.chrisavg.eventity.ui.EventsSearch.Tab2Fragment;


public class EventsTabbedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events_tabbed);

        ViewPager viewPager = findViewById(R.id.view_pager);
        TabLayout tabLayout = findViewById(R.id.tabs);

        viewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @NonNull
            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case 0:
                        Intent intent = getIntent();
                        double latitude = intent.getDoubleExtra("latitude", 0.0);
                        double longitude = intent.getDoubleExtra("longitude", 0.0);

                        // Create a Bundle to hold the location information
                        Bundle args = new Bundle();
                        args.putDouble("latitude", latitude);
                        args.putDouble("longitude", longitude);
                        Tab1Fragment tab1Fragment = new Tab1Fragment();
                        tab1Fragment.setArguments(args);

                        return tab1Fragment;
                    case 1:
                        return new Tab2Fragment();
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
    }
}
