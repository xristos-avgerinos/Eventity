package com.unipi.chrisavg.eventity.ui.EventsSearch;

import static com.facebook.FacebookSdk.getApplicationContext;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.algo.Algorithm;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.unipi.chrisavg.eventity.Event;
import com.unipi.chrisavg.eventity.MainActivity;
import com.unipi.chrisavg.eventity.MyClusterItem;
import com.unipi.chrisavg.eventity.R;
import com.unipi.chrisavg.eventity.User;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class Tab2Fragment extends Fragment implements OnMapReadyCallback, ClusterManager.OnClusterClickListener<MyClusterItem>, ClusterManager.OnClusterItemClickListener<MyClusterItem> {

    private GoogleMap mMap;
    static final int LOCATION_SETTINGS_REQUEST = 1;
    LocationManager locationManager;

    FirebaseAuth auth;
    CollectionReference events;
    FirebaseFirestore db;

    List<Event> eventsList = new ArrayList<>();
    List<Event> tempEventsList = new ArrayList<>();

    private ListenerRegistration listenerRegistration;

    final static long LOCATION_RANGE = 50000;

    User user;

    Location locationForSearch;

    ClusterManager<MyClusterItem> clusterManager;

    ImageView imageViewDetailed;
    TextView titleTextViewDetailed,dateTextViewDetailed,locationTextViewDetailed;

    LinearLayout RL_detailed_window;

    SupportMapFragment mapFragment;
    View view;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
          view = inflater.inflate(R.layout.fragment_tab2, container, false);

        // Retrieve bundle arguments - locationForSearch
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

        //37.966284,23.4952437

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);


        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        events = db.collection("Events");


        imageViewDetailed = view.findViewById(R.id.image);
        titleTextViewDetailed = view.findViewById(R.id.title);
        dateTextViewDetailed = view.findViewById(R.id.Date);
        locationTextViewDetailed = view.findViewById(R.id.Location);

        RL_detailed_window = view.findViewById(R.id.RL_detailed_window);


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

                Tab1Fragment.searchViewFilteringTab1(s); //κανουμε filtering και στο listview του Tab1

                if (RL_detailed_window.getVisibility()==View.VISIBLE){
                    RL_detailed_window.setVisibility(View.GONE);
                    mMap.getUiSettings().setZoomControlsEnabled(true);
                }


                tempEventsList.clear();
                clusterManager.clearItems(); // Clear the cluster items from the data set
                clusterManager.cluster(); // Recalculate and render the clusters

                if (TextUtils.isEmpty(s)){
                    ShowEventsOnMap(eventsList);
                    clusterManager.cluster();
                }
                else {

                    tempEventsList=new ArrayList<>();

                    s = s.toLowerCase(Locale.getDefault());
                    for (Event event : eventsList) {
                        if (event.getTitle().toLowerCase(Locale.getDefault())
                                .contains(s)){
                            tempEventsList.add(event);
                        }
                    }

                    ShowEventsOnMap(tempEventsList);
                    clusterManager.cluster();

                }

                return true;
            }
        });


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

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                if (RL_detailed_window.getVisibility()==View.VISIBLE){
                    RL_detailed_window.setVisibility(View.GONE);
                    mMap.getUiSettings().setZoomControlsEnabled(true);
                }

            }
        });

        RL_detailed_window.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "hey", Toast.LENGTH_SHORT).show();
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
        clusterManager = new ClusterManager<MyClusterItem>(getContext(), mMap);

        ClusterItemRenderer clusterItemRenderer = new ClusterItemRenderer();
        clusterManager.setRenderer(clusterItemRenderer);
        //mMap.setOnCameraChangeListener((GoogleMap.OnCameraChangeListener) clusterManager);
        mMap.setOnCameraIdleListener(clusterManager);
        mMap.setOnMarkerClickListener(clusterManager);
        mMap.setOnInfoWindowClickListener(clusterManager);

        clusterManager.setOnClusterClickListener(this);
        clusterManager.setOnClusterItemClickListener(this);


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
                                        clusterManager.clearItems(); // Clear the cluster items from the data set
                                        clusterManager.cluster(); // Recalculate and render the clusters

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
                                        ShowEventsOnMap(eventsList);
                                        clusterManager.cluster();

                                    });
                        }
                    }
                });


    }

    public void ShowEventsOnMap(List<Event> eventsList){

        for (Event event:eventsList) {
            // Adding cluster items
            LatLng latLng = new LatLng(event.getGeopoint().getLatitude(), event.getGeopoint().getLongitude());
            MyClusterItem clusterItem = new MyClusterItem(latLng, event.getTitle(), event.getDateToCustomFormat(),event);
            clusterManager.addItem(clusterItem);

        }

        if (eventsList.size() != 0)
            zoomToCenterOfClusterItems();

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

        // Calculate the center of all cluster items
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (MyClusterItem clusterItem : clusterItems) {
            builder.include(clusterItem.getPosition());
        }
        LatLngBounds bounds = builder.build();
        LatLng center = bounds.getCenter();

        // Move the camera to the calculated center with a specific zoom level
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 12.0f));
        return true;
    }

    public void zoomToCenterOfClusterItems(){
        // Get all cluster items from the algorithm
        Algorithm<MyClusterItem> algorithm = clusterManager.getAlgorithm();
        Collection<MyClusterItem> clusterItemsCollection = algorithm.getItems();

        // Convert the collection to a list
        List<MyClusterItem> clusterItems = new ArrayList<>(clusterItemsCollection);

        // Calculate the center of all cluster items
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (MyClusterItem clusterItem : clusterItems) {
            builder.include(clusterItem.getPosition());
        }
        LatLngBounds bounds = builder.build();
        LatLng center = bounds.getCenter();

        // Move the camera to the calculated center with a specific zoom level
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 9.0f));
    }

    @Override
    public boolean onClusterItemClick(MyClusterItem item) {
        Glide.with(getContext())
                .load(item.getEvent().getPhotoURL())
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache the image for better performance
                .into(imageViewDetailed);

        titleTextViewDetailed.setText(item.getTitle());
        dateTextViewDetailed.setText(item.getSnippet());
        locationTextViewDetailed.setText(item.getEvent().getLocation());

        // To show the detailed window
        RL_detailed_window.setVisibility(View.VISIBLE);
        mMap.getUiSettings().setZoomControlsEnabled(false);

        // Create a CameraUpdate
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(item.getPosition(),14.0f);

        // Move the camera with animation
        mMap.animateCamera(cameraUpdate, 1500, null);

        return true;
    }
}
