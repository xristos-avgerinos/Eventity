package com.unipi.chrisavg.eventity;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class HobbySelection extends AppCompatActivity {

    GridLayout MusicGridLayout;
    GridLayout VibeGridLayout;
    boolean MusicGridLayoutExpanded;
    boolean VibeGridLayoutExpanded;

    TextView music_expand;
    TextView vibe_expand;
    Toolbar toolbar;

    FirebaseAuth auth;
    CollectionReference users;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hobby_selection);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        users = db.collection("Users");

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setStatusBarWhite(this);

        music_expand = findViewById(R.id.music_expand);
        vibe_expand = findViewById(R.id.vibe_expand);

        //κρυβουμε τις 3 τελευταιες γραμμες των δυο GridLayout
        MusicGridLayout = findViewById(R.id.Music_gridview);
        HideRows(MusicGridLayout,9,15);

        VibeGridLayout = findViewById(R.id.Vibe_gridview);
        HideRows(VibeGridLayout,9,17);


    }

    private void setStatusBarWhite(AppCompatActivity activity){
        //Make status bar icons color white
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            activity.getWindow().setStatusBarColor(Color.WHITE);
        }
    }

    public void ContinueButton(View view){
        //αρχικα βρισκουμε ποσα toggleButtons εχουν επιλεχθει για καθε μια απο τις δυο κατηγοριες και τα κραταμε σε λιστες
        int selectedCountMusic = 0;
        int selectedCountVibe = 0;
        List<String> selectedMusicGridList = new ArrayList<>();
        List<String> selectedVibeGridList = new ArrayList<>();

        for (int i = 0; i < MusicGridLayout.getChildCount(); i++) {
            View child = MusicGridLayout.getChildAt(i);
            if (child instanceof ToggleButton) {
                ToggleButton toggleButton = (ToggleButton) child;
                if (toggleButton.isChecked()) {
                    selectedCountMusic++;
                    selectedMusicGridList.add(toggleButton.getText().toString());
                }
            }
        }

        for (int i = 0; i < VibeGridLayout.getChildCount(); i++) {
            View child = VibeGridLayout.getChildAt(i);
            if (child instanceof ToggleButton) {
                ToggleButton toggleButton = (ToggleButton) child;
                if (toggleButton.isChecked()) {
                    selectedCountVibe++;
                    selectedVibeGridList.add(toggleButton.getText().toString());
                }
            }
        }

        if(selectedCountMusic<3 || selectedCountVibe<3 ){
            DisplaySnackbar(view,"You have to select at least 3 interests from each category");
        }else{

            //αποθηκεύουμε στη βαση τις επιλεγμένες προτιμήσεις του χρήστη δημιουργοντας τη λιστα preferencesList που ειναι ο συνδυασμος των αλλων δυο λιστων
            List<String> preferencesList = new ArrayList<>();
            preferencesList.addAll(selectedMusicGridList);
            preferencesList.addAll(selectedVibeGridList);

            users.document(auth.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if(documentSnapshot.exists()){
                        User user = documentSnapshot.toObject(User.class);
                        //ενημερωνουμε την λιστα με τις προτιμησεις του χρηστη στη βαση με την λιστα που περιεχει ολες τις επιλογες και απο τις δυο κατηγοριες
                        user.setPreferences(preferencesList);

                        users.document(auth.getUid())
                                .set(user)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(getApplicationContext(), "Your preferences have been successfully saved!", Toast.LENGTH_SHORT).show();
                                        //ενημερωνουμε τα SharedPreferences οτι ο χρηστης εχει επιλεξει τιε προτιμησεις του
                                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("preferencesSelected", "True").apply();
                                        Intent intent = new Intent(HobbySelection.this,MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        DisplaySnackbar(view,"Something went wrong! Try again later.");
                                    }
                                });

                    }else{
                        DisplaySnackbar(view,"Something went wrong! Try again later.");
                    }
                }

            });
        }

    }

    public void HideRows(GridLayout gridLayout, int start, int end){

        // Ορίζουμε την ορατότητα των υπόλοιπων σειρών σε GONE
        for (int i = start; i < end; i++) {
            View view = gridLayout.getChildAt(i);
            view.setVisibility(View.GONE);
        }
        if (gridLayout==MusicGridLayout){
            MusicGridLayoutExpanded = false;
            music_expand.setText("See More ▼");
        }else if (gridLayout==VibeGridLayout){
            VibeGridLayoutExpanded = false;
            vibe_expand.setText("See More ▼");
        }
    }

    public void ShowHiddenRows(GridLayout gridLayout, int start, int end){

        // Ορίζουμε την ορατότητα των υπόλοιπων σειρών σε VISIBLE
        for (int i = start; i < end; i++) {
            View view = gridLayout.getChildAt(i);
            view.setVisibility(View.VISIBLE);
        }

        if (gridLayout==MusicGridLayout){
            MusicGridLayoutExpanded = true;
            music_expand.setText("See Less ▲");
        }else if (gridLayout==VibeGridLayout){
            VibeGridLayoutExpanded = true;
            vibe_expand.setText("See Less ▲");
        }

    }

    public void ExpandOrShrinkMusicGrid(View view){
        if (MusicGridLayoutExpanded){
            HideRows(MusicGridLayout,9,15);
        }else{
            ShowHiddenRows(MusicGridLayout,9,15);
        }
    }

    public void ExpandOrShrinkVibeGrid(View view){
        if (VibeGridLayoutExpanded){
            HideRows(VibeGridLayout,9,17);
        }else{
            ShowHiddenRows(VibeGridLayout,9,17);
        }
    }

    void DisplaySnackbar(View view,String message){

        Snackbar snackbar =  Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
        View v = snackbar.getView();
        TextView tv = (TextView) v.findViewById(com.google.android.material.R.id.snackbar_text);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        snackbar.show();
    }
}