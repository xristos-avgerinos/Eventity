package com.unipi.chrisavg.eventity;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.provider.Telephony;
import android.util.Log;
import androidx.appcompat.widget.SearchView;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.unipi.chrisavg.eventity.databinding.ActivityMapsBinding;

import java.io.IOException;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int REQUEST_LOCATION_PERMISSION = 1;

    double latitude;
    double longitude;
    SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.getApplicationContext());

        // initializing our search view.
        searchView = findViewById(R.id.idSearchView);

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
                sb.append("&key=" +"AIzaSyDeymNueHgieMsY90ebBi90u5wV_Cgxpsg");

                String url=sb.toString();
                Object dataFetch[] = new Object[2];
                dataFetch[0] = mMap;
                dataFetch[1] = url;
                FetchData fetchData = new FetchData();
                fetchData.execute(dataFetch);

                float currentZoom =mMap.getCameraPosition().zoom; // Get the current zoom level
                float newZoom = currentZoom / 1.3f; // Calculate the new zoom level by dividing the current zoom by 1.5
                CameraUpdate zoomOut = CameraUpdateFactory.zoomTo(newZoom); // Create a CameraUpdate object with the new zoom level
                mMap.animateCamera(zoomOut,2000,null); // Animate the camera to the new zoom leVEL

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

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
                AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
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
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        } else {
            // Request permission to access the user's location
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        getCurrentLocation();




    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
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
