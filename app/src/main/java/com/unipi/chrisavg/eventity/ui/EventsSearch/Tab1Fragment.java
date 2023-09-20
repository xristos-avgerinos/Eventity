package com.unipi.chrisavg.eventity.ui.EventsSearch;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.unipi.chrisavg.eventity.ArrayAdapterClass;
import com.unipi.chrisavg.eventity.Event;
import com.unipi.chrisavg.eventity.R;
import com.unipi.chrisavg.eventity.SpecificEventDetailedActivity;
import com.unipi.chrisavg.eventity.User;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class Tab1Fragment extends Fragment {
    static ListView listView;
    LinearLayout linearLayoutPb;
    TextView emptyView;

    List<String> ListViewItemsTitles = new ArrayList<>();
    List<String> ListViewItemsDates = new ArrayList<>();
    List<String> ListViewItemsLocations = new ArrayList<>();
    List<String> ListViewItemsImages = new ArrayList<>();
    static ArrayAdapterClass arrayAdapterClass;

    FirebaseAuth auth;
    CollectionReference events;
    FirebaseFirestore db;

    static List<Event> eventsList = new ArrayList<>();

    //Η λίστα που θα έχει τα πραγματικά events ακόμα και αν φιλτραριστούν από το searchView ή toggleButtons.
    //Χρειαζόμαστε αυτή τη λίστα για να στείλουμε τα events στο SpecificEventDetailedActivity.
    static List<Event> eventsListClone = new ArrayList<>();

    private ListenerRegistration listenerRegistration;

    final static long LOCATION_RANGE = 50000; //ψαχνω σε ακτινα 50km

    User user;
    View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_tab1, container, false);

        // Ανάκτηση των bundle arguments - locationForSearch
        Bundle args = getArguments();
        double latitude = 0;
        double longitude = 0;
        if (args != null) {
            latitude = args.getDouble("latitude", 0.0);
            longitude = args.getDouble("longitude", 0.0);
        }

        Location locationForSearch = new Location("provider");
        locationForSearch.setLatitude(latitude);
        locationForSearch.setLongitude(longitude);

        listView= (ListView) view.findViewById(R.id.SpecListview);
        emptyView=view.findViewById(R.id.emptyView);
        listView.setEmptyView(emptyView);
        emptyView.setVisibility(View.GONE);

        linearLayoutPb = (LinearLayout) view.findViewById(R.id.linlaHeaderProgress);

        linearLayoutPb.setVisibility(View.VISIBLE);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        events = db.collection("Events");

        // Λήψη χρήστη από το collection Users
        db.collection("Users")
                .document(auth.getUid()) // Replace with the actual user document ID
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists()){
                            user = documentSnapshot.toObject(User.class);

                            //Αρχίζουμε να ακούμε ενημερώσεις σε πραγματικό χρόνο.
                            // Λήψη των events και ταξινόμησή τους με βάση τις προτιμήσεις του χρήστη
                            listenerRegistration = events
                                    .addSnapshotListener((querySnapshot, error) -> {
                                        if (error != null) {
                                            // Handle error
                                            return;
                                        }

                                        linearLayoutPb.setVisibility(View.VISIBLE);
                                        // Διαγραφή των προηγούμενων δεδομένων στις λίστες
                                        eventsList.clear();
                                        ListViewItemsTitles.clear();
                                        ListViewItemsDates.clear();
                                        ListViewItemsLocations.clear();
                                        ListViewItemsImages.clear();

                                        for (DocumentSnapshot document : querySnapshot) {
                                            Event event = document.toObject(Event.class);
                                            event.setKey(document.getId());

                                            Date currentDatetime = Calendar.getInstance().getTime();

                                            Location eventLocation = new Location("");
                                            eventLocation.setLatitude(event.getGeopoint().getLatitude());
                                            eventLocation.setLongitude(event.getGeopoint().getLongitude());

                                            int location_distance = (int) eventLocation.distanceTo(locationForSearch);

                                            // Ελέγχουμε την ημερομηνία να είναι μετά την τρέχουσα ημερομηνία και την τοποθεσία κοντά στο locationForSearch μέχρι 50000 χλμ
                                            if (event.getDate() != null && event.getDate().toDate().after(currentDatetime) && location_distance<=LOCATION_RANGE) {
                                                // Υπολογισμός του match score με βάση τις προτιμήσεις
                                                long UserMatchScore = event.getTypes().stream()
                                                        .filter(user.getPreferences()::contains)
                                                        .count();

                                                event.setUserMatchScore(UserMatchScore);
                                                eventsList.add(event);
                                            }
                                        }

                                        // Εδώ, η "eventsList" περιέχει τα ενημερωμένα δεδομένα με αλλαγές σε πραγματικό χρόνο.
                                        ShowClassifiedEventsInListView();
                                        linearLayoutPb.setVisibility(View.GONE);
                                    });
                        }
                    }
                });


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getContext(), SpecificEventDetailedActivity.class);
                intent.putExtra("event", eventsListClone.get(position));
                startActivity(intent);
            }
        });

        return view;
    }

    public static void searchViewFilteringTab1(String filter){
        //Η μέθοδος που κανει το filtering με βαση το τι πληκτρολογει ο χρηστης στο searchview για το tab1
        if (TextUtils.isEmpty(filter)){
            eventsListClone = arrayAdapterClass.filter(eventsList,"");
            listView.clearTextFilter();
        }
        else {
            eventsListClone = arrayAdapterClass.filter(eventsList,filter);
        }
    }

    public static void toggleButtonsContainerFilteringTab1(List<String> selectedTypes){
        //Η μέθοδος που κανει το filtering με βαση το τι επιλεγει ο χρηστης απο τα toggle buttons για το tab1
        eventsListClone = arrayAdapterClass.toggleButtonsContainerFilter(eventsList,selectedTypes);
    }

    public void ShowClassifiedEventsInListView(){

        // Ταξινόμηση των events με βάση το match score σε φθίνουσα σειρά
        Collections.sort(eventsList, new Comparator<Event>() {
            @Override
            public int compare(Event event1, Event event2) {
                return Long.compare(event2.getUserMatchScore(), event1.getUserMatchScore());
            }
        });

        eventsListClone = eventsList;

        for (Event event:eventsList) {
            ListViewItemsTitles.add(event.getTitle());
            ListViewItemsDates.add(event.getDateToCustomFormat());
            ListViewItemsLocations.add(event.getLocation());
            ListViewItemsImages.add(event.getPhotoURL());
        }

        //δημιουργουμε τον ανταπτορα για το listview δινοντας του τις λιστες με τους τιτλους,ημερομηνιες,τοποθεσιες
        // και φωτογραφιες ταξινομημενες με βαση το ταξινομημενο πινακα απο πανω eventsList
        arrayAdapterClass = new ArrayAdapterClass(getContext(), ListViewItemsTitles, ListViewItemsDates,ListViewItemsLocations, ListViewItemsImages);

        if (eventsList.isEmpty()) {
            //αν ειναι αδεια η λιστα εμφανιζουμε το emptyView δηλ ενα μηνυμα στη λιστα οτι ειναι αδεια
            emptyView.setVisibility(View.VISIBLE);
        }

        listView.setAdapter(arrayAdapterClass);
        arrayAdapterClass.notifyDataSetChanged();
    }


    // Φροντίζουμε να αφαιρέσουμε τον listener όταν το activity καταστρέφεται για να αποφύγουμε διαρροές μνήμης(memory leaks).
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }

}
