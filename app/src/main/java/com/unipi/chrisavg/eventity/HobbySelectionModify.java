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

public class HobbySelectionModify extends AppCompatActivity {

    GridLayout MusicGridLayout;
    GridLayout VibeGridLayout;
    Toolbar toolbar;

    FirebaseAuth auth;
    CollectionReference users;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hobby_selection_modify);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        users = db.collection("Users");

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setStatusBarWhite(this);

        MusicGridLayout = findViewById(R.id.Music_gridview);
        VibeGridLayout = findViewById(R.id.Vibe_gridview);

        Intent intent = getIntent();
        ArrayList<String> userPreferencesList = intent.getStringArrayListExtra("UserPreferencesList");

        if (userPreferencesList != null) {
            for (int i = 0; i < MusicGridLayout.getChildCount(); i++) {
                View childView = MusicGridLayout.getChildAt(i);

                if (childView instanceof ToggleButton) {
                    ToggleButton toggleButton = (ToggleButton) childView;
                    String buttonText = toggleButton.getText().toString();

                    // Ελέγχουμε αν το κείμενο του ToggleButton του MusicGridLayout βρίσκεται
                    // στη λίστα userPreferencesList ωστε να το κανουμε checked
                    if (userPreferencesList.contains(buttonText)) {
                        toggleButton.setChecked(true);
                    }
                }
            }


            for (int i = 0; i < VibeGridLayout.getChildCount(); i++) {
                View childView = VibeGridLayout.getChildAt(i);

                if (childView instanceof ToggleButton) {
                    ToggleButton toggleButton = (ToggleButton) childView;
                    String buttonText = toggleButton.getText().toString();

                    // Ελέγχουμε αν το κείμενο του ToggleButton του VibeGridLayout βρίσκεται
                    // στη λίστα userPreferencesList ωστε να το κανουμε checked
                    if (userPreferencesList.contains(buttonText)) {
                        toggleButton.setChecked(true);
                    }
                }
            }
        }


    }

    private void setStatusBarWhite(AppCompatActivity activity){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            activity.getWindow().setStatusBarColor(Color.WHITE);
        }
    }

    public void ModifyButton(View view){
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
                                        Toast.makeText(getApplicationContext(), "Your preferences have been successfully modified!", Toast.LENGTH_SHORT).show();
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

    void DisplaySnackbar(View view,String message){

        Snackbar snackbar =  Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
        View v = snackbar.getView();
        TextView tv = (TextView) v.findViewById(com.google.android.material.R.id.snackbar_text);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        snackbar.show();
    }
}