package com.unipi.chrisavg.eventity;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignUpActivity extends AppCompatActivity {

    EditText email,fullname,password,confirmPassword;
    Toolbar toolbar;

    FirebaseAuth mAuth;
    CollectionReference users;
    FirebaseFirestore db;
    FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        email= findViewById(R.id.et_email);
        fullname=findViewById(R.id.et_fullname);
        password=findViewById(R.id.et_password);
        confirmPassword=findViewById(R.id.et_confirm_password);


        //Βαζουμε το email του user που δοθηκε απο την welcomeActivity
        Intent intent = getIntent();
        String user_email = intent.getStringExtra("Email");
        email.setText(user_email);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignUpActivity.super.onBackPressed();
            }
        });
        setStatusBarWhite(this);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        users = db.collection("Users");

    }

    private void setStatusBarWhite(AppCompatActivity activity){
        //Make status bar icons color dark
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            activity.getWindow().setStatusBarColor(Color.WHITE);
        }
    }

    public void signUp(View view){

        if(TextUtils.isEmpty(fullname.getText().toString())){
            fullname.requestFocus();
            DisplaySnackbar(view,getString(R.string.fullname_required));
        }
        else if(TextUtils.isEmpty(password.getText().toString())){
            password.requestFocus();
            DisplaySnackbar(view,getString(R.string.password_required));
        }
        else if(password.getText().toString().length() < 6){
            password.requestFocus();
            DisplaySnackbar(view,getString(R.string.password_min_length));
        }
        else if(TextUtils.isEmpty(confirmPassword.getText().toString())){
            confirmPassword.requestFocus();
            DisplaySnackbar(view,getString(R.string.password_confirmation));

        }else if(!(password.getText().toString()).equals(confirmPassword.getText().toString())){
            confirmPassword.requestFocus();
            DisplaySnackbar(view,getString(R.string.passwords_dont_match));

            //Clear the entered passwords
            password.clearComposingText();
            confirmPassword.clearComposingText();
        }
        else{
            //κανουμε ολους τους ελεγχους σε αυτον τον listener.γινεται και ελεγχος αν το email ειναι μοναδικο
            mAuth.createUserWithEmailAndPassword(email.getText().toString(),password.getText().toString())
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                firebaseUser = mAuth.getCurrentUser();
                                User user = new User(fullname.getText().toString());
                                user.setToken("token");

                                users.document(firebaseUser.getUid())
                                        .set(user)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                Toast.makeText(SignUpActivity.this, getString(R.string.user_registered), Toast.LENGTH_SHORT).show();

                                                //οταν δημιουργειται ενας χρηστης προφανως δεν εχει επιλεξει ενδιαφεροντα οποτε θετουμε τα sharedPreferences σε false και τον
                                                // στελνουμε στην MainActivity που αυτη κατευθειαν μεσω της OnStart θα τον ανακατευθυνει στην HobbySelection
                                                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("preferencesSelected", "False").apply();
                                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                                //To prevent User from returning back to Sign Up Activity on pressing back button after registration
                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                        | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(intent);
                                                finish();

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() { //κατι πηγε λαθος με το γραψιμο του χρηστη στη βαση
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                DisplaySnackbar(view,e.getLocalizedMessage());
                                            }
                                        });

                            }else { //κατι πηγε λαθος με την δημιουργια του χρηστη στο authentication
                                if(task.getException() instanceof FirebaseNetworkException){
                                    // No internet connection
                                    DisplaySnackbar(view,"A network error has occurred. Connect to the internet and try again.");
                                }
                                else {
                                    Log.e(TAG, task.getException().getLocalizedMessage());
                                    DisplaySnackbar(view,"Error occurred while signing you up.");
                                }
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