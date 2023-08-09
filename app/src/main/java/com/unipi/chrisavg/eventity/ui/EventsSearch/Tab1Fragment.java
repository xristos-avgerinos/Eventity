package com.unipi.chrisavg.eventity.ui.EventsSearch;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.unipi.chrisavg.eventity.ArrayAdapterClass;
import com.unipi.chrisavg.eventity.Event;
import com.unipi.chrisavg.eventity.EventsTabbedActivity;
import com.unipi.chrisavg.eventity.MainActivity;
import com.unipi.chrisavg.eventity.R;
import com.unipi.chrisavg.eventity.User;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class Tab1Fragment extends Fragment {
    ListView listView;
    LinearLayout linearLayoutPb;
    TextView emptyView;

    List<String> ListViewItemsTitles = new ArrayList<>();
    List<String> ListViewItemsDates = new ArrayList<>();
    List<String> ListViewItemsLocations = new ArrayList<>();
    List<String> ListViewItemsImages = new ArrayList<>();
    ArrayAdapterClass arrayAdapterClass;

    FirebaseAuth auth;
    CollectionReference events;
    FirebaseFirestore db;

    List<Event> eventsList = new ArrayList<>();
    private ListenerRegistration listenerRegistration;

    final static long LOCATION_RANGE = 50000;

    User user;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tab1, container, false);

        // Retrieve bundle arguments - locationForSearch
        Bundle args = getArguments();
        double latitude = 0;
        double longitude = 0;
        if (args != null) {
            latitude = args.getDouble("latitude", 0.0);
            longitude = args.getDouble("longitude", 0.0);
        }

        Location locationForSearch = new Location("provider");
        locationForSearch.setLatitude(37.966284);
        locationForSearch.setLongitude(23.4952437);

        //37.966284,23.4952437

        listView= (ListView) view.findViewById(R.id.SpecListview);
        emptyView=view.findViewById(R.id.emptyView);
        listView.setEmptyView(emptyView);
        emptyView.setVisibility(View.GONE);

        linearLayoutPb = (LinearLayout) view.findViewById(R.id.linlaHeaderProgress);

        linearLayoutPb.setVisibility(View.VISIBLE);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        events = db.collection("Events");


        // Fetch user from the Users collection
        List<String> currentUserPreferences = new ArrayList<>();
        db.collection("Users")
                .document(auth.getUid()) // Replace with the actual user document ID
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists()){
                            user = documentSnapshot.toObject(User.class);

                            // Start listening for real-time updates
                            // Fetch events and sort them based on user preferences
                            listenerRegistration = events
                                    .addSnapshotListener((querySnapshot, error) -> {
                                        if (error != null) {
                                            // Handle error
                                            return;
                                        }

                                        linearLayoutPb.setVisibility(View.VISIBLE);
                                        // Clear the previous data in the list
                                        eventsList.clear();
                                        ListViewItemsTitles.clear();
                                        ListViewItemsDates.clear();
                                        ListViewItemsLocations.clear();
                                        ListViewItemsImages.clear();

                                        // Loop through each document in the query result
                                        for (DocumentSnapshot document : querySnapshot) {
                                            Event event = document.toObject(Event.class);
                                            event.setKey(document.getId());

                                            // Get the current datetime
                                            Date currentDatetime = Calendar.getInstance().getTime();

                                            Location eventLocation = new Location("");
                                            eventLocation.setLatitude(event.getGeopoint().getLatitude());
                                            eventLocation.setLongitude(event.getGeopoint().getLongitude());

                                            int location_distance = (int) eventLocation.distanceTo(locationForSearch);

                                            // Check date after current date and location close to locationForSearch until 50000 km
                                            if (event.getDate() != null && event.getDate().toDate().after(currentDatetime) && location_distance<=LOCATION_RANGE) {
                                                // Calculate match score based on preferences
                                                long UserMatchScore = event.getTypes().stream()
                                                        .filter(user.getPreferences()::contains)
                                                        .count();

                                                event.setUserMatchScore(UserMatchScore);
                                                eventsList.add(event);
                                            }
                                        }

                                        // Here, "userList" contains the updated data with real-time changes
                                        // You can now use the "userList" in your app
                                        ShowClassifiedEventsInListView();
                                        linearLayoutPb.setVisibility(View.GONE);
                                    });
                        }
                    }
                });

        SearchView searchView = null;
        if (getActivity() != null) {
            searchView = getActivity().findViewById(R.id.search_view); // Find the SearchView within the activity's layout
            // Use the searchView here as needed
        }
        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (TextUtils.isEmpty(s)){
                    arrayAdapterClass.filter("");
                    listView.clearTextFilter();
                }
                else {
                    arrayAdapterClass.filter(s);
                }
                return true;
            }
        });


        return view;
    }

    public void ShowClassifiedEventsInListView(){

        // Sort events based on match score in descending order
        Collections.sort(eventsList, new Comparator<Event>() {
            @Override
            public int compare(Event event1, Event event2) {
                return Long.compare(event2.getUserMatchScore(), event1.getUserMatchScore());
            }
        });

        for (Event event:eventsList) {
            ListViewItemsTitles.add(event.getTitle());
            ListViewItemsDates.add(event.getDateToCustomFormat());
            ListViewItemsLocations.add(event.getLocation());
            ListViewItemsImages.add(event.getPhotoURL());
        }

        arrayAdapterClass = new ArrayAdapterClass(getContext(), ListViewItemsTitles, ListViewItemsDates,ListViewItemsLocations, ListViewItemsImages);

        if (eventsList.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
        }
        listView.setAdapter(arrayAdapterClass);
        arrayAdapterClass.notifyDataSetChanged();


    }


    // Be sure to remove the listener when the activity is destroyed to avoid memory leaks
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }

}
