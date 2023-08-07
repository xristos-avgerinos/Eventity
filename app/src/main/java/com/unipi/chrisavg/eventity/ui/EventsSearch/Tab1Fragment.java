package com.unipi.chrisavg.eventity.ui.EventsSearch;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.unipi.chrisavg.eventity.ArrayAdapterClass;
import com.unipi.chrisavg.eventity.Event;
import com.unipi.chrisavg.eventity.R;

import java.util.ArrayList;
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
        locationForSearch.setLatitude(latitude);
        locationForSearch.setLongitude((longitude));

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



        // Start listening for real-time updates
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
                        eventsList.add(event);
                    }

                    // Here, "userList" contains the updated data with real-time changes
                    // You can now use the "userList" in your app
                    ShowClassifiedEventsInListView();
                    linearLayoutPb.setVisibility(View.GONE);
                });



        /*ListViewItemsTitles.add("Sarkrista + Special Guests - Live in Athens");
        ListViewItemsDates.add("Πεμπτη, Οκτ 19");
        ListViewItemsLocations.add("Temple Athens, Ίακχου 17, Athens, Greece, Palaio Faliro, Greece");
        ListViewItemsImages.add("https://cdn-az.allevents.in/events7/banners/61ec31a907e18ae6aec7baacdecc6e2e91f36dfaeb5bb3c398141f24f3c5014c-rimg-w960-h503-gmir.jpg?v=1690810850");

        ListViewItemsTitles.add("Oikonomopoulos Tour");
        ListViewItemsDates.add("Πεμπτη, Οκτ 19");
        ListViewItemsLocations.add("Theatro petras athens");
        ListViewItemsImages.add("https://www.ticketservices.gr/pictures/original/b_33088_or_OIKONOMOPOULOS-2023.jpg");
*/


        return view;
    }

    public void ShowClassifiedEventsInListView(){
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
