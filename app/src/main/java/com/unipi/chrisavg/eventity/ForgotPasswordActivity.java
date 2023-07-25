package com.unipi.chrisavg.eventity;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    Toolbar toolbar;
    FirebaseAuth mAuth;
    EditText email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        toolbar = findViewById(R.id.toolbar);
        mAuth = FirebaseAuth.getInstance();
        email= findViewById(R.id.et_email);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        // getSupportActionBar().hide(); //hide the title bar
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ForgotPasswordActivity.super.onBackPressed();
            }
        });
        setStatusBarWhite(this);
    }

    private void setStatusBarWhite(AppCompatActivity activity){
        //Make status bar icons color white
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            activity.getWindow().setStatusBarColor(Color.WHITE);
        }
    }

    public void ResetPassword(View view){
        if(TextUtils.isEmpty(email.getText().toString())){
            DisplaySnackbar(view,getString(R.string.email_required));
            email.requestFocus();
        }else if(!Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()){
            DisplaySnackbar(view,getString(R.string.valid_email));
            email.requestFocus();
        }else{
            mAuth.sendPasswordResetEmail(email.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(ForgotPasswordActivity.this, getString(R.string.reset_password), Toast.LENGTH_SHORT).show();
                        //Intent intent = new Intent(ForgotPasswordActivity.this,LoginActivity.class);
                        //intent.putExtra("Email", email.getText().toString());
                        finish(); //κατευθειαν τερματιζουμε το συγκεκριμενο activity και μας στελνει αυτοματα πισω στην login

                    }else{
                        if(task.getException() instanceof FirebaseNetworkException){
                            // No internet connection
                            DisplaySnackbar(view,"A network error has occurred. Connect to the internet and try again");
                        }
                        else {
                            Log.e(TAG, task.getException().getLocalizedMessage());
                            DisplaySnackbar(view,"Error occurred while checking email address");
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