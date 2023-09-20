package com.unipi.chrisavg.eventity;

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
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
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


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity  {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    FirebaseAuth mAuth;
    CollectionReference users;
    FirebaseFirestore db;

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
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_tickets, R.id.nav_settings,R.id.nav_bot)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        //Απο εδώ κάνουμε την ανακατεύθυνση στα items του navigation drawer
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
                case R.id.nav_bot:
                    navController.navigate(R.id.nav_bot);
                    break;
                case R.id.nav_logout:
                    SignOut();
                    break;
            }

            // Κλείσιμο του navigation drawer μετά την επιλογή στοιχείου
            drawer.closeDrawer(GravityCompat.START);

            return true;
        });


        //Αν κάποιο activity έχει δώσει εντολή να ανοίξουμε το tickets fragment τοτε ανακατευθύνουμε στο MainActivity
        // δηλαδή το συγκεκριμένο καθώς ειναι το κυριο activity που ρυθμίζει και τα navigations στα nav drawer items.
        Intent intent = getIntent();
        boolean openTicketsFragment = false;
        if (intent != null) {
            openTicketsFragment = intent.getBooleanExtra("OpenTicketsFragment",false);
        }
        if (openTicketsFragment){
            navController.navigate(R.id.nav_tickets);
        }

        // Αλλάζουμε το title του nav_header_main
        TextView textViewHeaderTitle = navigationView.getHeaderView(0).findViewById(R.id.textViewHeaderTitle);
        textViewHeaderTitle.setText(mAuth.getCurrentUser().getDisplayName());
        // Αλλάζουμε το subtitle του nav_header_main
        TextView textViewHeaderSubtitle = navigationView.getHeaderView(0).findViewById(R.id.textViewHeaderSubtitle);
        textViewHeaderSubtitle.setText(mAuth.getCurrentUser().getEmail());
    }


    private void setStatusBarCustomColor(AppCompatActivity activity){
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
            //Αν ο χρηστης δεν εχει επιλεξει τα ενδιαφεροντα του τον ανακατευθυνουμε στο HobbySelection activity για να επιλεξει
            if (preferencesSelected.equals("False")){
                Intent intent = new Intent(MainActivity.this,HobbySelection.class);
                startActivity(intent);
                finish();
            }
        }

    }



}