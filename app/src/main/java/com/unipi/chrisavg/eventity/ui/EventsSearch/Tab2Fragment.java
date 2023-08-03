package com.unipi.chrisavg.eventity.ui.EventsSearch;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.maps.android.clustering.ClusterManager;
import com.unipi.chrisavg.eventity.BuildConfig;
import com.unipi.chrisavg.eventity.FetchData;
import com.unipi.chrisavg.eventity.R;


public class Tab2Fragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    double latitude;
    double longitude;
    SearchView searchView;

    ClusterManager clusterManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tab2, container, false);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());

        // initializing our search view.
        searchView =view.findViewById(R.id.idSearchView);

        // adding on query listener for our search view.
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // on below line we are getting the
                // location name from search view.
                String search = searchView.getQuery().toString();
                StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
                sb.append("location="+ latitude + "," + longitude);
                sb.append("&radius=10000");
                sb.append("&types="+ search);//types=hospital|health
                sb.append("&sensor=true");
                sb.append("&key=" + BuildConfig.MAPS_API_KEY);

                String url=sb.toString();
                Object dataFetch[] = new Object[2];
                dataFetch[0] = mMap;
                dataFetch[1] = url;
                FetchData fetchData = new FetchData();
                fetchData.execute(dataFetch);

                float currentZoom =mMap.getCameraPosition().zoom; // Get the current zoom level
                float newZoom = currentZoom / 1.3f; // Calculate the new zoom level by dividing the current zoom by 1.3
                CameraUpdate zoomOut = CameraUpdateFactory.zoomTo(newZoom); // Create a CameraUpdate object with the new zoom level
                mMap.animateCamera(zoomOut,2000,null); // Animate the camera to the new zoom leVEL

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
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

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        LatLng salamina = new LatLng(37.955476, 23.623648);

        // mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney "));
        // mMap.addMarker(new MarkerOptions().position(salamina).title("Marker in aianteio salamina").snippet("https://www.google.com/"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(salamina));


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
        //mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.addCircle(new CircleOptions().center(new LatLng(38.026064, 23.930915)).radius(6000)  // radius of the circle, in meters
                .strokeWidth(2)  // width of the circle's stroke
                .strokeColor(Color.RED)  // color of the circle's stroke
                .fillColor(Color.argb(70, 255, 0, 0)));  // color of the circle's fill);


        // Enable my location button and show my location
        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        } else {
            // Request permission to access the user's location
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        getCurrentLocation();



       /*clusterManager = new ClusterManager<MyItem>(this,mMap);
        // Set some lat/lng coordinates to start with.
        double lat = 51.5145160;
        double lng = -0.1270060;

        // Add ten cluster items in close proximity, for purposes of this example.
        for (int i = 0; i < 10; i++) {
            double offset = i / 60d;
            lat = lat + offset;
            lng = lng + offset;
            MyItem offsetItem = new MyItem(lat, lng, "Title " + i, "Snippet " + i);
            clusterManager.addItem(offsetItem);
        }

        mMap.setOnMarkerClickListener(clusterManager);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(51.5145160,-0.1270060),13));
        mMap.setOnCameraIdleListener(clusterManager);*/


        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        // clusterManager.setAnimation(false);


    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();


                        LatLng LatLng  = new LatLng(latitude,longitude);
                        mMap.addMarker(new MarkerOptions().position(LatLng).title("Current Location"));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng,15));


                    }
                }
            });

        } else {
            requestLocationPermission();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {

            case REQUEST_LOCATION_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getCurrentLocation();
                }
        }

    }
}
