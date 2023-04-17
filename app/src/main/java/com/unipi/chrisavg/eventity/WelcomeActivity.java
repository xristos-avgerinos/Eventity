package com.unipi.chrisavg.eventity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

import java.security.Permission;
import java.util.Arrays;


public class WelcomeActivity extends AppCompatActivity {

    GoogleSignInClient mGoogleSignInClient;
    ProgressDialog progressDialog;
    int RC_SIGN_IN = 40;
    FirebaseAuth auth;
    CollectionReference users;
    FirebaseFirestore db;

    CallbackManager mCallbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().hide(); //hide the title bar
        setStatusBarWhite(this);


        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        users = db.collection("Users");


        /*progressDialog = new ProgressDialog(WelcomeActivity.this);
        progressDialog.setTitle(("Creating account"));
        progressDialog.setMessage("We are creating your account");*/

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken((getString(R.string.default_web_client_id)))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);


        FacebookSdk.sdkInitialize(WelcomeActivity.this);
       // AppEventsLogger.activateApp(this);

        // Initialize Facebook Login button
        mCallbackManager = CallbackManager.Factory.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = auth.getCurrentUser();
        if(currentUser!= null){
            updateUI(currentUser);
        }

    }

    private void setStatusBarWhite(AppCompatActivity activity){
        //Make status bar icons color white
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            activity.getWindow().setStatusBarColor(Color.WHITE);
        }
    }
    public void GoogleSignInButton(View view){
        Intent intent =  mGoogleSignInClient.getSignInIntent();
        startActivityForResult(intent,RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode != RESULT_CANCELED){
            if (requestCode == RC_SIGN_IN){

                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

                try{
                    GoogleSignInAccount account = task.getResult(ApiException.class);

                    firebaseAuth(account.getIdToken());
                } catch (ApiException e) {
                    throw new RuntimeException();
                }
            }else if(requestCode == FacebookSdk.getCallbackRequestCodeOffset()){
                mCallbackManager.onActivityResult(requestCode, resultCode, data);
            }
        }

    }

    private void firebaseAuth(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken,null);

        auth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    FirebaseUser u = auth.getCurrentUser();
                    User user = new User("token");
                    user.setName(u.getDisplayName());

                    users.document(auth.getUid())
                            .set(user)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(getApplicationContext(), "written to db"+ user.getToken(), Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(WelcomeActivity.this,MainActivity.class);
                                    startActivity(intent);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getApplicationContext(), "Error writing document", Toast.LENGTH_SHORT).show();
                                }
                            });
                }else{
                    // If sign in fails, display a message to the user.
                    showMessage("Authentication Failed",task.getException().getLocalizedMessage());
                    updateUI(null);
                }
            }
        });
    }

    private void handleFacebookAccessToken(AccessToken token) {

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser u = auth.getCurrentUser();

                            User user = new User("token");
                            user.setName(u.getDisplayName());

                            users.document(auth.getUid())
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
                                            Toast.makeText(getApplicationContext(), "Error writing document", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                            updateUI(u);
                        } else {
                            // If sign in fails, display a message to the user.
                            showMessage("Authentication Failed",task.getException().getLocalizedMessage());
                            updateUI(null);
                        }
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if(user!=null){
            Intent intent = new Intent(WelcomeActivity.this,MainActivity.class);
            startActivity(intent);
        }
    }



    public void FacebookSignUpButton(View view){
        LoginManager.getInstance().logInWithReadPermissions(WelcomeActivity.this, Arrays.asList("email","public_profile"));
        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException error) {
            }
        });
    }

    void showMessage(String title, String message){
        new AlertDialog.Builder(this).setTitle(title).setMessage(message).setCancelable(true).show();
    }
}