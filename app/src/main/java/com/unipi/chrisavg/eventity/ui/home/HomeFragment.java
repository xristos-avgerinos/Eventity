package com.unipi.chrisavg.eventity.ui.home;

import static android.content.Context.LOCATION_SERVICE;
import static com.facebook.FacebookSdk.getApplicationContext;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.unipi.chrisavg.eventity.EventsTabbedActivity;
import com.unipi.chrisavg.eventity.MainActivity;
import com.unipi.chrisavg.eventity.OnPlaceItemClickListener;
import com.unipi.chrisavg.eventity.PlacesAutoCompleteAdapter;
import com.unipi.chrisavg.eventity.R;
import com.unipi.chrisavg.eventity.databinding.ActivityMainBinding;
import com.unipi.chrisavg.eventity.databinding.FragmentHomeBinding;
import  com.ncorti.slidetoact.SlideToActView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment implements SlideToActView.OnSlideCompleteListener, OnPlaceItemClickListener, LocationListener {

    private FragmentHomeBinding binding;


    SlideToActView slideToActView;

    private SearchView searchView;
    private RecyclerView recyclerView;
    private PlacesAutoCompleteAdapter adapter;

    ProgressBar progressBar;
    static final int locationRequestCode = 111;

    LocationManager locationManager;
    Location locationForSearch;
    String stringLocationForSearch;
    static final int LOCATION_SETTINGS_REQUEST = 1;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        slideToActView = root.findViewById(R.id.slider);
        slideToActView.setOnSlideCompleteListener(this);

        // Set an OnClickListener on the SlideToActView
        slideToActView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(searchView.getQuery().toString().equals(stringLocationForSearch)){
                    //αν αυτο που εχει επιλεχθει ειναι διο με αυτο που υπαρχει τωρα στα queryText τοτε το κελειδωνουμε για να προχωρησει
                    slideToActView.setLocked(false);
                }else{
                    // αν αυτο που επιλεχθηκε μεσω recyclerView η currentLocation
                    // δεν ειναι ιδιο με το queryText αυτη τη στιγμη επειδη μπορει να διεγραψε καποιους χαρακτηρες απο αυτο
                    // κλειδωμενουμε το slide για να μην μπορει να προχωρησει
                    slideToActView.setLocked(true);
                }

                if (slideToActView.isLocked()){
                    // αν το slide ειναι κλειδωμενο δεν τον αφηνουμε να προχωρησει και του εμφανιζουμε μηνυμα
                    DisplaySnackbar("You have to select a region first to search for events");
                }

            }
        });

        searchView = root.findViewById(R.id.searchView);
        recyclerView = root.findViewById(R.id.recyclerView);

        // Initialize the Places API in order to find places' suggestions for the user
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyDeymNueHgieMsY90ebBi90u5wV_Cgxpsg");
        }

        searchView.setQueryHint("Find events in...");

        // Set up the RecyclerView with an adapter
        adapter = new PlacesAutoCompleteAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Implement the search functionality
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Perform the search with the entered query
                searchForPlaces(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    //when user clear the searchView in order to search a new place we restore the itemSelected
                    // to false so as we can make new predictions
                    adapter.setItemSelected(false);
                    locationForSearch = new Location("");
                    stringLocationForSearch = null;
                    slideToActView.setLocked(true);

                    searchForPlaces(newText);
                }


                // Update the suggestions as the user types
                //if user have selected an item from the suggestions we don't make other predictions
                if ( !adapter.isItemSelected() && newText.length()>2) {
                    searchForPlaces(newText);
                }
                return false;
            }
        });

        progressBar = root.findViewById(R.id.progressBar);
        locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
        LinearLayout CurrentLocationButton = root.findViewById(R.id.CurrentLocationButton);
        CurrentLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    //Αν δεν εχω τα permissions τα ζηταω
                    requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, locationRequestCode);
                } else {
                    //αν τα εχω ψαχνω τη τοποθεσια του
                    //Toast.makeText(MainActivity.this, "I have location permissions", Toast.LENGTH_SHORT).show();
                    findUserLocation();

                }

            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == locationRequestCode) { //ελεγχουμε αν εχει ερθει απο το παραπανω requestPermission με requestCode = 111 που ειναι του CurrentLocationButton
            if (grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED || grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                //Αν ο χρηστης πατησει allow
                Toast.makeText(getContext(), "Permissions accepted", Toast.LENGTH_SHORT).show();
                findUserLocation();
            } else {
                //Αν ο χρηστης αρνηθει τα δικαιωματα εμφανιζω καταλληλο μηνυμα.
                Toast.makeText(getContext(), "Permissions denied", Toast.LENGTH_SHORT).show();
            }

        }
    }


    public void findUserLocation(){
        progressBar.setVisibility(View.VISIBLE);

        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(!isGPSEnabled){ //αν δεν εχει ανοιξει το location στο κινητο του τοτε τον στελνω στα settings αν θελει ωστε να το ανοιξει και να παρω την τοποθεσια του
            showSettingsAlert();
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,this);
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
        // Set an OnCancelListener to detect if the user pressed "Cancel"
        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                // Handle the "Cancel" action here
                // This block of code will be executed when the user presses the back button or touches outside the dialog.
                progressBar.setVisibility(View.GONE);
            }
        });

        alertDialog.show();
    }


    @Override
    public void onLocationChanged(@NonNull Location location) {
        Geocoder geocoder;
        List<Address> addresses = new ArrayList<>();
        geocoder = new Geocoder(getContext(), Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        } catch (IOException e) {
            e.printStackTrace();
        }
        //απο τις συντεταγμενες latitude και longitude παιρνω την διευθνυση του και οτι αλλη πληροφορια θελω
        String address;
        if (addresses.size()!=0){
            address = addresses.get(0).getAddressLine(0);
            // Make itemSelected true so as to stop searching predictions
            adapter.setItemSelected(true);
            searchView.setQuery(address, false);
            locationForSearch = location;
            stringLocationForSearch = address;
            slideToActView.setLocked(false);
            progressBar.setVisibility(View.GONE);
            locationManager.removeUpdates(this);

        }
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }
    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOCATION_SETTINGS_REQUEST) {
            findUserLocation(); // ξανα ζηταω να τη βρω γτ οταν επιστρεφει απο τα settings εχει χασει το action να βρει τη τοποθεσια απο πριν

        }
    }

    private void searchForPlaces(String query) {
        // Perform a Places API autocomplete request
        // Use AutocompleteSessionToken.newInstance() for a new session each time
        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                // .setTypeFilter(TypeFilter.ADDRESS) αν θελω μονο διευθυνσεις να βρισκω
                .setSessionToken(token)
                .setQuery(query)
                .build();

        Places.createClient(getContext()).findAutocompletePredictions(request)
                .addOnSuccessListener((response) -> {
                    List<AutocompletePrediction> predictions = response.getAutocompletePredictions();
                    adapter.setPredictions(predictions);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener((exception) -> {
                    // Handle any errors that occurred during the request
                });
    }

    //μεθοδος του interface ωστε να εχουμε προσβαση σε αυτες τις λεπτομερειες της επιλεγμενης περιοχης σε αυτο το activity
    @Override
    public void onPlaceItemClick(AutocompletePrediction prediction) {
        String placeId = prediction.getPlaceId();
        String placeName = prediction.getPrimaryText(null).toString();
        String fullPlaceName = prediction.getFullText(null).toString();

        // Make itemSelected true so as to stop searching predictions
        adapter.setItemSelected(true);
        // Set the SearchView text to the selected place name
        searchView.setQuery(fullPlaceName, false);

        //τα στελνω σε αυτη τη μεθοδο ωστε να βρω τα ακριβη coordinates της επιλεγμενης περιοχης
        fetchPlaceDetails(placeId, fullPlaceName);

    }

    private void fetchPlaceDetails(String placeId, String fullPlaceName) {
        List<Place.Field> placeFields = Arrays.asList(Place.Field.LAT_LNG);

        FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, placeFields).build();

        Places.createClient(getContext()).fetchPlace(request)
                .addOnSuccessListener((response) -> {
                    Place place = response.getPlace();

                    // Get latitude and longitude of the selected place
                    LatLng latLng = place.getLatLng();
                    double latitude = latLng.latitude;
                    double longitude = latLng.longitude;

                    locationForSearch = new Location("");
                    locationForSearch.setLatitude(latitude);
                    locationForSearch.setLongitude(longitude);
                    stringLocationForSearch = fullPlaceName;
                    slideToActView.setLocked(false);
                })
                .addOnFailureListener((exception) -> {
                    // Handle any errors that occurred during the request
                });
    }


    @Override
    public void onSlideComplete(SlideToActView slideToActView) {
        // Sliding action completed, start the new activity
        Intent intent = new Intent(getContext(), EventsTabbedActivity.class);
        intent.putExtra("latitude", locationForSearch.getLatitude());
        intent.putExtra("longitude", locationForSearch.getLongitude());
        startActivity(intent);

        // Reset the SlideToActView
        slideToActView.resetSlider();
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
}