package com.unipi.chrisavg.eventity;

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
import com.google.android.material.navigation.NavigationView;

import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.unipi.chrisavg.eventity.databinding.ActivityMainBinding;
import  com.ncorti.slidetoact.SlideToActView;

public class MainActivity extends AppCompatActivity  implements SlideToActView.OnSlideCompleteListener {

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


    }

    @Override
    public void onSlideComplete(SlideToActView slideToActView) {
        // Sliding action completed, start the new activity
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
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