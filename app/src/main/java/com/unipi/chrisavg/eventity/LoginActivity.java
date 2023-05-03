package com.unipi.chrisavg.eventity;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.accounts.NetworkErrorException;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class LoginActivity extends AppCompatActivity {

    Button logInBtn;
    TextInputEditText password_editText;
    View underline;
    TextView email_textview;
    String email;
    Toolbar toolbar;

    FirebaseAuth auth;
    CollectionReference users;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        users = db.collection("Users");


        toolbar = findViewById(R.id.toolbar);
        Intent intent = getIntent();
        email = intent.getStringExtra("Email");

        email_textview = findViewById(R.id.email_textview);
        email_textview.setText(email);


        setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayShowTitleEnabled(false);
        // getSupportActionBar().hide(); //hide the title bar
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginActivity.super.onBackPressed();
            }
        });


        setStatusBarWhite(LoginActivity.this);

        logInBtn = findViewById(R.id.LogIn);
        password_editText = findViewById(R.id.et_password);
        underline = findViewById(R.id.underline);

        logInBtn.setEnabled(false);


        password_editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    // The EditText has gained focus, change the color of the underline and the height
                    underline.setBackgroundColor(getResources().getColor(R.color.underline_color));
                    ViewGroup.LayoutParams layoutParams = underline.getLayoutParams();
                    int heightInDp = 2; // set the desired height in dp
                    int heightInPixels = (int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP, heightInDp, getResources().getDisplayMetrics());
                    layoutParams.height = heightInPixels; // set the height to 2dp
                    underline.setLayoutParams(layoutParams);
                } else {
                    // The EditText has lost focus, do something here
                    // For example, change the color of the underline and height back to normal
                    underline.setBackgroundColor(getResources().getColor(R.color.black));
                    ViewGroup.LayoutParams layoutParams = underline.getLayoutParams();
                    int heightInDp = 1; // set the desired height in dp
                    int heightInPixels = (int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP, heightInDp, getResources().getDisplayMetrics());
                    layoutParams.height = heightInPixels; // set the height to 2dp
                    underline.setLayoutParams(layoutParams);
                }
            }
        });

        password_editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!TextUtils.isEmpty(s)) {
                   logInBtn.setEnabled(true);

                }else{
                    logInBtn.setEnabled(false);
                }
            }
        });



    }

    public void GoForgetPasswordActivity(View view){
        Intent intent = new Intent(this,ForgotPasswordActivity.class);
        startActivity(intent);
    }

    public void GoMainActivity(View view){

        auth.signInWithEmailAndPassword(email,password_editText.getText().toString())
                .addOnCompleteListener((task)->{
                    if(task.isSuccessful()){

                        users.document(auth.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if(documentSnapshot.exists()){
                                    User user = documentSnapshot.toObject(User.class);
                                    if (user.getPreferences().size()==0){
                                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("preferencesSelected", "False").apply();
                                    }else{
                                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("preferencesSelected", "True").apply();
                                    }
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    //To prevent User from returning back to LOGIN Activity on pressing back button
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK
                                            | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        });


                    }else {

                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            DisplaySnackbar(view,"The password is invalid or the user does not have a password");

                        } else if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                            if (((FirebaseAuthInvalidUserException) task.getException()).getErrorCode().equals("ERROR_USER_DISABLED")) {
                                // User account is blocked
                                DisplaySnackbar(view,"User account is blocked");
                            }
                        }
                        else if(task.getException() instanceof FirebaseNetworkException){
                            // No internet connection
                            DisplaySnackbar(view,"A network error has occurred. Connect to the internet and try again");
                        }
                        else if(task.getException() instanceof FirebaseTooManyRequestsException){
                            // All requests from this device have been blocked
                            // Show an appropriate message to the user
                            DisplaySnackbar(view,"The account has been blocked due to unusual activity.You may entered wrong password too many times.Try again later or reset your password.");

                        }
                        else {
                            Log.e(TAG, task.getException().getLocalizedMessage());
                            DisplaySnackbar(view,task.getException().getLocalizedMessage());
                        }

                    }
                });

    }


    private void setStatusBarWhite(AppCompatActivity activity){
        //Make status bar icons color white
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            activity.getWindow().setStatusBarColor(Color.WHITE);
        }
    }

    void DisplaySnackbar(View view,String message){

        Snackbar snackbar =  Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
        View v = snackbar.getView();
        TextView tv = (TextView) v.findViewById(com.google.android.material.R.id.snackbar_text);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        // Set the maximum number of lines for the Snackbar message to display all the text
        tv.setMaxLines(Integer.MAX_VALUE);
        snackbar.show();
    }

    void showMessage(String title, String message){
        new AlertDialog.Builder(this).setTitle(title).setMessage(message).setCancelable(true).show();
    }

    public void ChangeEmail(View view){
        finish();
    }
}