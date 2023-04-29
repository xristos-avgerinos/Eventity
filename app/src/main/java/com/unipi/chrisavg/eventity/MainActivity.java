package com.unipi.chrisavg.eventity;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.ktx.Firebase;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    CollectionReference users;
    FirebaseFirestore db;
    EditText email,password;
    FirebaseUser mUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        users = db.collection("Users");

        email = findViewById(R.id.et_email);
        password = findViewById(R.id.et_password);

        mUser = mAuth.getCurrentUser();
    }

    public void go(View view){
        mAuth.signInWithEmailAndPassword(email.getText().toString(),password.getText().toString())
                .addOnCompleteListener((task)->{
                    if(task.isSuccessful()){

                       /*User user = new User("1234567");
                       user.setAge(13);
                       user.setName("chris");
                        HashMap<String, Boolean> preferences = new HashMap<String, Boolean>();
                        preferences.put("music",true);
                        preferences.put("travel",false);
                        preferences.put("arts",true);
                        preferences.put("sports",true);
                       user.setPreferences(preferences);

                        users.document(mAuth.getUid())
                                .set(user)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(getApplicationContext(), "written to db"+ user.getToken(), Toast.LENGTH_SHORT).show();

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Error writing document", e);
                                    }
                                });*/

                        users.document(mAuth.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if(documentSnapshot.exists()){
                                    User user = documentSnapshot.toObject(User.class);

                                }else{
                                    Toast.makeText(getApplicationContext(), "failed to find userId", Toast.LENGTH_SHORT).show();

                                }
                            }

                        });


                    }else{
                        Toast.makeText(this, "notttt found on auth", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    public void maps(View view){
        Intent maps = new Intent(this,MapsActivity.class);
        startActivity(maps);
    }

    public void SignOut(View view){
        mAuth.signOut();
        LoginManager.getInstance().logOut();
        Intent intent = new Intent(MainActivity.this,WelcomeActivity.class);
        startActivity(intent);
        finish();
    }
}