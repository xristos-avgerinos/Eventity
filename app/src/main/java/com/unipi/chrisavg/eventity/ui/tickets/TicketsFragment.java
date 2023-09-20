package com.unipi.chrisavg.eventity.ui.tickets;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.unipi.chrisavg.eventity.ArrayAdapterClass;
import com.unipi.chrisavg.eventity.ArrayAdapterClass2;
import com.unipi.chrisavg.eventity.Event;
import com.unipi.chrisavg.eventity.MainActivity;
import com.unipi.chrisavg.eventity.R;
import com.unipi.chrisavg.eventity.Reservation;
import com.unipi.chrisavg.eventity.SpecificEventDetailedActivity;
import com.unipi.chrisavg.eventity.User;
import com.unipi.chrisavg.eventity.UserTicket;
import com.unipi.chrisavg.eventity.databinding.FragmentTicketsBinding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class TicketsFragment extends Fragment {

    private FragmentTicketsBinding binding;

    static ListView listView;
    LinearLayout linearLayoutPb;
    TextView emptyView;

    List<String> ListViewItemsTitles = new ArrayList<>();
    List<String> ListViewItemsDates = new ArrayList<>();
    List<String> ListViewItemsLocations = new ArrayList<>();
    List<String> ListViewItemsImages = new ArrayList<>();
    static ArrayAdapterClass2 arrayAdapterClass;

    FirebaseAuth auth;
    CollectionReference events,reservations;
    FirebaseFirestore db;

    static List<Event> eventsList = new ArrayList<>();

    // Δημιουργούμε μια λίστα για να αποθηκεύσουμε τις κρατήσεις
    List<Reservation> reservationList = new ArrayList<>();

    // Λιστα που κραταμε τα ids των reservations
    List<String> reservationListIds = new ArrayList<>();

    List<Reservation> reservationListClone = new ArrayList<>();
    List<String> reservationListIdsClone = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        binding = FragmentTicketsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        listView= (ListView) root.findViewById(R.id.SpecListview);
        emptyView=root.findViewById(R.id.emptyView);
        listView.setEmptyView(emptyView);
        emptyView.setVisibility(View.GONE);

        linearLayoutPb = (LinearLayout) root.findViewById(R.id.linlaHeaderProgress);

        linearLayoutPb.setVisibility(View.VISIBLE);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        events = db.collection("Events");
        reservations = db.collection("Reservations");

        // Παιρνω τις κρατησεις που το userId τους ειναι αυτο του χρήστη που είναι συνδεμένος
        reservations.whereEqualTo("userId", auth.getUid()).get()
                .addOnCompleteListener(task -> {

                    linearLayoutPb.setVisibility(View.VISIBLE);
                    reservationList.clear();
                    reservationListIds.clear();
                    eventsList.clear();

                    ListViewItemsTitles.clear();
                    ListViewItemsDates.clear();
                    ListViewItemsLocations.clear();
                    ListViewItemsImages.clear();

                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null) {
                            for (QueryDocumentSnapshot documentSnapshot : querySnapshot) {
                                Reservation reservation = documentSnapshot.toObject(Reservation.class);
                                // Προσθήκη της κράτησης στη λίστα
                                reservationList.add(reservation);
                                reservationListIds.add(documentSnapshot.getId());
                            }

                            events.get().addOnCompleteListener(task2 -> {
                                if(task2.isSuccessful()){
                                    QuerySnapshot querySnapshot2 = task2.getResult();
                                    if (querySnapshot2 != null) {
                                        for (QueryDocumentSnapshot documentSnapshot : querySnapshot2) {
                                            Event event = documentSnapshot.toObject(Event.class);
                                            event.setKey(documentSnapshot.getId());

                                            if (reservationList.stream()
                                                    .anyMatch(reservation -> reservation.getEventId().equals(event.getKey()))){

                                                Date currentDatetime = Calendar.getInstance().getTime();

                                                // Ημερομηνία ελέγχου μετά την τρέχουσα ημερομηνία
                                                if (event.getDate() != null && event.getDate().toDate().after(currentDatetime)) {
                                                    //Παίρνω τα events που ταυτίζονται με τα eventIds των κρατήσεων ωστε να τα εμφανισω στο listView
                                                    eventsList.add(event);
                                                }
                                            }


                                        }
                                    }

                                    ShowClassifiedEventsInListView();
                                    linearLayoutPb.setVisibility(View.GONE);

                                }
                                else{
                                    // Handle errors
                                    DisplaySnackbar(task2.getException().getLocalizedMessage());
                                }

                            });
                        }

                        } else {

                            // Handle errors
                            DisplaySnackbar(task.getException().getLocalizedMessage());
                        }

        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getContext(), UserTicket.class);
                intent.putExtra("ReservationID",reservationListIdsClone.get(position));
                intent.putExtra("SendingActivity","TicketsFragment");
                startActivity(intent);
            }
        });


        return root;
    }

    public void ShowClassifiedEventsInListView(){

        // Ταξινόμηση των events με βάση την ημερομηνία σε φθίνουσα σειρά
        Collections.sort(eventsList, new Comparator<Event>() {
            @Override
            public int compare(Event event1, Event event2) {
                return event1.getDate().compareTo(event2.getDate());
            }
        });


        reservationListClone.clear();
        reservationListIdsClone.clear();

        for (Event event:eventsList) {
            ListViewItemsTitles.add(event.getTitle());
            ListViewItemsDates.add(event.getDateToCustomFormat());
            ListViewItemsLocations.add(event.getLocation());
            ListViewItemsImages.add(event.getPhotoURL());

            // Παιρνω τη θεση του eventId στο λιστα με τα reservations
            int ReservationListPositionOfEventId = IntStream.range(0, reservationList.size())
                    .filter(i -> event.getKey().equals(reservationList.get(i).getEventId()))
                    .findFirst()
                    .orElse(-1);

            //Φτιαχνω βοηθητικη λιστα με τα ταξινομημενα reservations
            reservationListClone.add(reservationList.get(ReservationListPositionOfEventId));
            reservationListIdsClone.add((reservationListIds.get(ReservationListPositionOfEventId)));
        }

        arrayAdapterClass = new ArrayAdapterClass2(getContext(), ListViewItemsTitles, ListViewItemsDates,ListViewItemsLocations, ListViewItemsImages);

        if (eventsList.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
        }
        listView.setAdapter(arrayAdapterClass);
        arrayAdapterClass.notifyDataSetChanged();


    }

    void DisplaySnackbar(String message){

        Snackbar snackbar =  Snackbar.make(((Activity) getActivity()).findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT);
        View v = snackbar.getView();
        TextView tv = (TextView) v.findViewById(com.google.android.material.R.id.snackbar_text);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        // Ορίζουμε τον μέγιστο αριθμό γραμμών για το μήνυμα Snackbar ώστε να εμφανίζεται όλο το κείμενο
        tv.setMaxLines(Integer.MAX_VALUE);
        snackbar.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}