package com.unipi.chrisavg.eventity.ui.EventsSearch;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.maps.android.clustering.ClusterManager;
import com.unipi.chrisavg.eventity.Event;
import com.unipi.chrisavg.eventity.MyClusterItem;
import com.unipi.chrisavg.eventity.R;
import com.unipi.chrisavg.eventity.User;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class Tab2Fragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    static final int LOCATION_SETTINGS_REQUEST = 1;
    LocationManager locationManager;

    FirebaseAuth auth;
    CollectionReference events;
    FirebaseFirestore db;

    List<Event> eventsList = new ArrayList<>();
    private ListenerRegistration listenerRegistration;

    final static long LOCATION_RANGE = 50000;

    User user;
    List<Marker> markerList = new ArrayList<>();

    Location locationForSearch;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tab2, container, false);

        // Retrieve bundle arguments - locationForSearch
        Bundle args = getArguments();
        double latitude = 0;
        double longitude = 0;
        if (args != null) {
            latitude = args.getDouble("latitude", 0.0);
            longitude = args.getDouble("longitude", 0.0);
        }

        locationForSearch = new Location("provider");
        locationForSearch.setLatitude(37.966284);
        locationForSearch.setLongitude(23.4952437);

        //37.966284,23.4952437

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);


        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        events = db.collection("Events");



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
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(@NonNull Marker marker) {
                // Show a dialog box with more information about the location
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(marker.getTitle());
                builder.setMessage(marker.getSnippet());
                builder.setPositiveButton("OK", null);
                builder.show();
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {

                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 13);
                // Move the camera to the marker position
                mMap.animateCamera(cameraUpdate,2000,null);
                marker.showInfoWindow();
                return true;
            }
        });

        // Enable my location button and show my location
        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);

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
        } else {
            // Request permission to access the user's location
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }



        mMap.getUiSettings().setZoomControlsEnabled(true);


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

                                        // Clear the previous data in the list
                                        eventsList.clear();
                                        clearMarkers();

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

                                                eventsList.add(event);
                                            }
                                        }

                                        // Here, "userList" contains the updated data with real-time changes
                                        // You can now use the "userList" in your app
                                        ShowEventsOnMap();
                                    });
                        }
                    }
                });


    }

    public void ShowEventsOnMap(){

        ClusterManager<MyClusterItem> clusterManager = new ClusterManager<MyClusterItem>(getContext(), mMap);
        mMap.setOnCameraIdleListener(clusterManager);
        mMap.setOnMarkerClickListener(clusterManager);

        for (Event event:eventsList) {
            // Adding markers
            LatLng latLng = new LatLng(event.getGeopoint().getLatitude(), event.getGeopoint().getLongitude());
       /*     Marker marker = mMap.addMarker(new MarkerOptions().position(latLng).title(event.getTitle()));
            markerList.add(marker);*/

            MyClusterItem clusterItem = new MyClusterItem(latLng, event.getTitle(), event.getDateToCustomFormat());
            clusterManager.addItem(clusterItem);

        }

        LatLng latLng = new LatLng(40.638366, 22.971797);
        MyClusterItem clusterItem = new MyClusterItem(latLng, "title", "title");
        clusterManager.addItem(clusterItem);

        LatLng latLng2 = new LatLng(37.808647, 23.790229);
        MyClusterItem clusterItem2 = new MyClusterItem(latLng2, "title", "title");
        clusterManager.addItem(clusterItem2);

        LatLng latLng3 = new LatLng(37.966635, 23.934917);
        MyClusterItem clusterItem3 = new MyClusterItem(latLng3, "title", "title");
        clusterManager.addItem(clusterItem3);

        LatLng latLng4 = new LatLng( 38.000636, 23.985542);
        MyClusterItem clusterItem4 = new MyClusterItem(latLng4, "title", "title");
        clusterManager.addItem(clusterItem4);

        LatLng latLng5 = new LatLng(38.525218, 23.867792);
        MyClusterItem clusterItem5 = new MyClusterItem(latLng5, "title", "title");
        clusterManager.addItem(clusterItem5);

        LatLng latLng6 = new LatLng( 38.667766, 23.619803);
        MyClusterItem clusterItem6 = new MyClusterItem(latLng6, "title", "title");
        clusterManager.addItem(clusterItem6);

        /*mMap.setOnMarkerClickListener(clusterManager);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(51.5145160,-0.1270060),13));
        mMap.setOnCameraIdleListener(clusterManager);*/

    }

    public void clearMarkers() {
        for (Marker marker : markerList) {
            marker.remove();
        }
        markerList.clear(); // Clear the list of markers
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



}
