package com.unipi.chrisavg.eventity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.facebook.login.LoginManager;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.material.navigation.NavigationView;

import androidx.appcompat.widget.SearchView;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.unipi.chrisavg.eventity.databinding.ActivityMainBinding;
import  com.ncorti.slidetoact.SlideToActView;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity  implements SlideToActView.OnSlideCompleteListener,OnPlaceItemClickListener {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    FirebaseAuth mAuth;
    CollectionReference users;
    FirebaseFirestore db;

    private SearchView searchView;
    private RecyclerView recyclerView;
    private PlacesAutoCompleteAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);
        setStatusBarCustomColor(this);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        users = db.collection("Users");

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_tickets, R.id.nav_settings)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        //For logout button
        navigationView.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_home:
                    navController.navigate(R.id.nav_home);
                    break;
                case R.id.nav_tickets:
                    navController.navigate(R.id.nav_tickets);
                    break;
                case R.id.nav_settings:
                    navController.navigate(R.id.nav_settings);
                    break;
                case R.id.nav_logout:
                    SignOut();
                    break;
            }

            // Close the navigation drawer after item selection
            drawer.closeDrawer(GravityCompat.START);

            return true;
        });


        // Change the subtitle of the nav_header_main
        TextView textViewHeaderTitle = navigationView.getHeaderView(0).findViewById(R.id.textViewHeaderTitle);
        textViewHeaderTitle.setText(mAuth.getCurrentUser().getDisplayName());

        // Change the subtitle of the nav_header_main
        TextView textViewHeaderSubtitle = navigationView.getHeaderView(0).findViewById(R.id.textViewHeaderSubtitle);
        textViewHeaderSubtitle.setText(mAuth.getCurrentUser().getEmail());

        SlideToActView slideToActView = findViewById(R.id.slider);
        slideToActView.setOnSlideCompleteListener(this);


        searchView = findViewById(R.id.searchView);
        recyclerView = findViewById(R.id.recyclerView);

        // Initialize the Places API in order to find places' suggestions for the user
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyDeymNueHgieMsY90ebBi90u5wV_Cgxpsg");
        }

        searchView.setQueryHint("Find places in...");

        // Set up the RecyclerView with an adapter
        adapter = new PlacesAutoCompleteAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
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
                }

                // Update the suggestions as the user types
                //if user have selected an item from the suggestions we don't make other predictions
                if (newText.length() > 2 && !adapter.isItemSelected()) {
                    searchForPlaces(newText);
                }
                return false;
            }
        });

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

        Places.createClient(this).findAutocompletePredictions(request)
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
        fetchPlaceDetails(placeId, placeName);

    }

    private void fetchPlaceDetails(String placeId, String placeName) {
        List<Place.Field> placeFields = Arrays.asList(Place.Field.LAT_LNG);

        FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, placeFields).build();

        Places.createClient(MainActivity.this).fetchPlace(request)
                .addOnSuccessListener((response) -> {
                    Place place = response.getPlace();

                    // Get latitude and longitude of the selected place
                    LatLng latLng = place.getLatLng();
                    double latitude = latLng.latitude;
                    double longitude = latLng.longitude;

                    // Show the Snackbar with the selected place name and location
                    showSnackbar(placeName, latitude, longitude);
                })
                .addOnFailureListener((exception) -> {
                    // Handle any errors that occurred during the request
                });
    }

    private void showSnackbar(String placeName, double latitude, double longitude) {
        String locationInfo = String.format(Locale.getDefault(), "Selected place: %s\nLatitude: %f, Longitude: %f", placeName, latitude, longitude);
        Snackbar.make(((Activity) MainActivity.this).findViewById(android.R.id.content), locationInfo, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onSlideComplete(SlideToActView slideToActView) {
        // Sliding action completed, start the new activity
        Intent intent = new Intent(MainActivity.this, EventsSearchActivity.class);
        startActivity(intent);
    }

    private void setStatusBarCustomColor(AppCompatActivity activity){
        //Make status bar icons color white
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            activity.getWindow().setStatusBarColor(getResources().getColor(R.color.statusBarColor));
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void maps(View view){
        Intent maps = new Intent(this,MapsActivity.class);
        startActivity(maps);
    }

    public void SignOut(){
        mAuth.signOut();
        LoginManager.getInstance().logOut();
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().remove("preferencesSelected").apply();
        Intent intent = new Intent(MainActivity.this,WelcomeActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        String preferencesSelected = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("preferencesSelected",null);

        if(currentUser!= null && preferencesSelected!=null ){
           /* users.document(mAuth.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if(documentSnapshot.exists()){
                        User user = documentSnapshot.toObject(User.class);
                        if (user.getPreferences().size()==0){
                            Intent intent = new Intent(MainActivity.this,HobbySelection.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                }
            });*/

            //Αν ο χρηστης δεν εχει επιλεξει τα ενδιαφεροντα του τον ανακατευθυνουμε στο HobbySelection activity για να επιλεξει
            if (preferencesSelected.equals("False")){
                Intent intent = new Intent(MainActivity.this,HobbySelection.class);
                startActivity(intent);
                finish();
            }
        }

    }


}