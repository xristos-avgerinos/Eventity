package com.unipi.chrisavg.eventity.ui.EventsSearch;

import static com.facebook.FacebookSdk.getApplicationContext;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.algo.Algorithm;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.unipi.chrisavg.eventity.Event;
import com.unipi.chrisavg.eventity.MyClusterItem;
import com.unipi.chrisavg.eventity.R;
import com.unipi.chrisavg.eventity.SpecificEventDetailedActivity;
import com.unipi.chrisavg.eventity.User;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class Tab2Fragment extends Fragment implements OnMapReadyCallback, ClusterManager.OnClusterClickListener<MyClusterItem>, ClusterManager.OnClusterItemClickListener<MyClusterItem> {

    private static GoogleMap mMap;
    static final int LOCATION_SETTINGS_REQUEST = 1;
    static final int locationRequestCode = 123;
    LocationManager locationManager;

    FirebaseAuth auth;
    CollectionReference events;
    FirebaseFirestore db;

    static List<Event> eventsList = new ArrayList<>();
    static List<Event> searchViewTempEventsList = new ArrayList<>(); //λιστα που κραταει τα καθε φορα events που μενουν μετα το filtering μεσω searchView
    static List<Event> ToggleButtonsTempEventsList = new ArrayList<>(); //λιστα που κραταει τα καθε φορα events που μενουν μετα το filtering μεσω toggleButtons

    private ListenerRegistration listenerRegistration;

    final static long LOCATION_RANGE = 50000;

    User user;

    Location locationForSearch;

    static ClusterManager<MyClusterItem> clusterManager;

    ImageView imageViewDetailed;
    TextView titleTextViewDetailed,dateTextViewDetailed,locationTextViewDetailed;

    static LinearLayout RL_detailed_window;

    SupportMapFragment mapFragment;
    View view;

    String selectedEventItemKey;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_tab2, container, false);

        // Ανάκτηση των bundle arguments - locationForSearch
        Bundle args = getArguments();
        double latitude = 0;
        double longitude = 0;
        if (args != null) {
            latitude = args.getDouble("latitude", 0.0);
            longitude = args.getDouble("longitude", 0.0);
        }

        locationForSearch = new Location("provider");
        locationForSearch.setLatitude(latitude);
        locationForSearch.setLongitude(longitude);


        // Αποκταμε το SupportMapFragment και θα ειδοποιηθουμε όταν ο χάρτης είναι έτοιμος να χρησιμοποιηθεί(OnMapReady).
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        events = db.collection("Events");

        imageViewDetailed = view.findViewById(R.id.image);
        titleTextViewDetailed = view.findViewById(R.id.title);
        dateTextViewDetailed = view.findViewById(R.id.Date);
        locationTextViewDetailed = view.findViewById(R.id.Location);

        RL_detailed_window = view.findViewById(R.id.RL_detailed_window);


        return view;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    public static void searchViewFilteringTab2(String filter){
        //Η μέθοδος που κανει το filtering με βαση το τι πληκτρολογει ο χρηστης στο searchview για το tab2

        if (RL_detailed_window.getVisibility()==View.VISIBLE){
            RL_detailed_window.setVisibility(View.GONE);
            mMap.getUiSettings().setZoomControlsEnabled(true);
        }


        searchViewTempEventsList.clear();
        clusterManager.clearItems(); // Διαγραφή των cluster items από το σύνολο δεδομένων
        clusterManager.cluster(); // Επαναϋπολογισμός και απόδοση των clusters

        if (TextUtils.isEmpty(filter)){
            ShowEventsOnMap(eventsList);
            clusterManager.cluster();
        }
        else {

            searchViewTempEventsList =new ArrayList<>();

            filter = filter.toLowerCase(Locale.getDefault());
            for (Event event : eventsList) {
                if (event.getTitle().toLowerCase(Locale.getDefault())
                        .contains(filter)){
                    searchViewTempEventsList.add(event);
                }
            }

            ShowEventsOnMap(searchViewTempEventsList);
            clusterManager.cluster();

        }
    }

    public static void toggleButtonsContainerFilteringTab2(List<String> selectedTypes){
        //Η μέθοδος που κανει το filtering με βαση το τι επιλεγει ο χρηστης απο τα toggle buttons για το tab2

        if (RL_detailed_window.getVisibility()==View.VISIBLE){
            RL_detailed_window.setVisibility(View.GONE);
            mMap.getUiSettings().setZoomControlsEnabled(true);
        }


        ToggleButtonsTempEventsList.clear();
        clusterManager.clearItems(); // Διαγραφή των cluster items από το σύνολο δεδομένων
        clusterManager.cluster(); // Επαναϋπολογισμός και απόδοση των clusters

        if (selectedTypes.size()==0){
            ShowEventsOnMap(eventsList);
            clusterManager.cluster();
        }
        else {

            ToggleButtonsTempEventsList =new ArrayList<>();

            for ( Event event : eventsList) {
                if (containsCommonItem(event.getTypes(), selectedTypes)) {
                    ToggleButtonsTempEventsList.add(event);
                }
            }

            ShowEventsOnMap(ToggleButtonsTempEventsList);
            clusterManager.cluster();

        }
    }


    public static boolean containsCommonItem(List<String> list1, List<String> list2) {
        for (String item : list1) {
            if (list2.contains(item)) {
                return true;
            }
        }
        return false;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                if (RL_detailed_window.getVisibility()==View.VISIBLE){
                    RL_detailed_window.setVisibility(View.GONE); //εμφανιζουμε το παραθυρακι του event
                    mMap.getUiSettings().setZoomControlsEnabled(true);
                }

            }
        });

        RL_detailed_window.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Παιρνουμε το event με το eventKey που πατηθηκε και ανοιγουμε το SpecificEventDetailedActivity στελνοντας το συγκεκριμενο event
                Event targetEvent = eventsList.stream()
                        .filter(event -> event.getKey().equals(selectedEventItemKey))
                        .findFirst()
                        .orElse(null);

                Intent intent = new Intent(getContext(), SpecificEventDetailedActivity.class);
                intent.putExtra("event", targetEvent);
                startActivity(intent);
            }
        });


        // Ενεργοποίηση του κουμπιού "Η τοποθεσία μου" και εμφάνιση της τοποθεσίας μου αν εχω τα permissions
        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        } else {
            // Διαφορετικά αίτηση άδειας πρόσβασης στην τοποθεσία του χρήστη(request permissions)
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, locationRequestCode);
        }

        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                if(!isGPSEnabled){ //αν δεν εχει ανοιξει το location στο κινητο του τοτε τον στελνω στα settings αν θελει ωστε να το ανοιξει και να παρω την τοποθεσια του
                    showSettingsAlert();
                }
                return false;
            }
        });

        mMap.getUiSettings().setZoomControlsEnabled(true);

        clusterManager = new ClusterManager<MyClusterItem>(getContext(), mMap);

        //για το clustering των markers στο map
        ClusterItemRenderer clusterItemRenderer = new ClusterItemRenderer();
        clusterManager.setRenderer(clusterItemRenderer);

        //mMap.setOnCameraChangeListener((GoogleMap.OnCameraChangeListener) clusterManager);
        mMap.setOnCameraIdleListener(clusterManager);
        mMap.setOnMarkerClickListener(clusterManager);
        mMap.setOnInfoWindowClickListener(clusterManager);

        clusterManager.setOnClusterClickListener(this);
        clusterManager.setOnClusterItemClickListener(this);


        List<String> currentUserPreferences = new ArrayList<>();
        db.collection("Users")
                .document(auth.getUid()) // Replace with the actual user document ID
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists()){
                            user = documentSnapshot.toObject(User.class);

                            // Αρχίζουμε να ακούμε ενημερώσεις σε πραγματικό χρόνο
                            // Λήψη events και ταξινόμησή τους με βάση τις προτιμήσεις του χρήστη
                            listenerRegistration = events
                                    .addSnapshotListener((querySnapshot, error) -> {
                                        if (error != null) {
                                            // Handle error
                                            return;
                                        }

                                        eventsList.clear();
                                        clusterManager.clearItems();
                                        clusterManager.cluster();

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
                                                eventsList.add(event);
                                            }
                                        }

                                        // Εδώ, η "eventsList" περιέχει τα ενημερωμένα δεδομένα με αλλαγές σε πραγματικό χρόνο.
                                        ShowEventsOnMap(eventsList);
                                        clusterManager.cluster();

                                    });
                        }
                    }
                });


    }

    public static void ShowEventsOnMap(List<Event> eventsList){

        for (Event event:eventsList) {
            // προσθετουμε αντικειμενα cluster
            LatLng latLng = new LatLng(event.getGeopoint().getLatitude(), event.getGeopoint().getLongitude());
            MyClusterItem clusterItem = new MyClusterItem(latLng, event.getTitle(), event.getDateToCustomFormat(),event);
            clusterManager.addItem(clusterItem);
        }

        if (eventsList.size() != 0)
            zoomToCenterOfClusterItems(); //αν υπαρχουν events στο map τοτε οταν εκκινειται το map κανω zoom στο κεντρο απο ολα τα event ωστε να εχει μια σφαιρικη οψη προς ολα τα events

    }


    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
        alertDialog.setTitle("GPS settings");
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
        alertDialog.setPositiveButton("Settings", (dialog, which) -> {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent,LOCATION_SETTINGS_REQUEST);

        });
        alertDialog.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        alertDialog.show();
    }

    /**
     * Draws profile photos inside markers (using IconGenerator).
     * When there are multiple people in the cluster, draw multiple photos (using MultiDrawable).
     */
    private class ClusterItemRenderer extends DefaultClusterRenderer<MyClusterItem> {
        public ClusterItemRenderer() {
            super(getApplicationContext(), mMap, clusterManager);
        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster cluster) {
            // Always render clusters.
            return cluster.getSize() > 1;
        }
    }


    @Override
    public boolean onClusterClick(Cluster<MyClusterItem> cluster) {

        Collection<MyClusterItem> clusterItems = cluster.getItems();

        // Υπολογίζουμε το κέντρο όλων των cluster items απο το cluster που πατησε ωστε να μεταφερω τη καμερα πανω απο το κεντρο αυτων
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (MyClusterItem clusterItem : clusterItems) {
            builder.include(clusterItem.getPosition());
        }
        LatLngBounds bounds = builder.build();
        LatLng center = bounds.getCenter();

        // Μετακίνηση της κάμερας στο υπολογισμένο κέντρο με ένα συγκεκριμένο επίπεδο ζουμ(12)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 12.0f));
        return true;
    }

    public static void zoomToCenterOfClusterItems(){
        // Λήψη όλων των cluster items (απο ολα τα clusters) από τον αλγόριθμο
        Algorithm<MyClusterItem> algorithm = clusterManager.getAlgorithm();
        Collection<MyClusterItem> clusterItemsCollection = algorithm.getItems();

        // Μετατροπή της συλλογής σε λίστα
        List<MyClusterItem> clusterItems = new ArrayList<>(clusterItemsCollection);

        // Υπολογίζουμε το κέντρο όλων των cluster items
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (MyClusterItem clusterItem : clusterItems) {
            builder.include(clusterItem.getPosition());
        }
        LatLngBounds bounds = builder.build();
        LatLng center = bounds.getCenter();

        // Μετακίνηση της κάμερας στο υπολογισμένο κέντρο με ένα συγκεκριμένο επίπεδο ζουμ(9)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 9.0f));
    }

    @Override
    public boolean onClusterItemClick(MyClusterItem item) {
        Glide.with(getContext())
                .load(item.getEvent().getPhotoURL())
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Κρυφή μνήμη(cache) της εικόνας για καλύτερη απόδοση
                .into(imageViewDetailed);

        titleTextViewDetailed.setText(item.getTitle());
        dateTextViewDetailed.setText(item.getSnippet());
        locationTextViewDetailed.setText(item.getEvent().getLocation());

        selectedEventItemKey = item.getEvent().getKey();

        // Εμφανιζουμε το detailed window για το event
        RL_detailed_window.setVisibility(View.VISIBLE);
        mMap.getUiSettings().setZoomControlsEnabled(false);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(item.getPosition(),14.0f);

        // Μετακίνηση της κάμερας με animation
        mMap.animateCamera(cameraUpdate, 1500, null);

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == locationRequestCode) { //ελεγχουμε αν εχει ερθει απο το παραπανω requestPermission με requestCode = 111 που ειναι του CurrentLocationButton
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Αν ο χρηστης πατησει allow
                Toast.makeText(getContext(), "Permissions accepted", Toast.LENGTH_SHORT).show();
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                //Αν ο χρηστης αρνηθει τα δικαιωματα εμφανιζω καταλληλο μηνυμα.
                Toast.makeText(getContext(), "Permissions denied", Toast.LENGTH_SHORT).show();
            }

        }
    }
}
