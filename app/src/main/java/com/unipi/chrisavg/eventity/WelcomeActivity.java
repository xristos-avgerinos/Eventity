package com.unipi.chrisavg.eventity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
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
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import com.facebook.FacebookSdk;
import java.util.Arrays;
import java.util.List;


public class WelcomeActivity extends AppCompatActivity {

    GoogleSignInClient mGoogleSignInClient;
    ProgressDialog progressDialog;
    int RC_SIGN_IN = 40;
    FirebaseAuth auth;
    CollectionReference users;
    FirebaseFirestore db;

    CallbackManager mCallbackManager;

    RelativeLayout RL_email;
    Button btn_email_login;
    Button continue_btn;
    TextInputEditText email_editText;

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
            Intent intent = new Intent(WelcomeActivity.this,MainActivity.class);
            startActivity(intent);
        }

    }

    private void setStatusBarWhite(AppCompatActivity activity){
        //Make status bar icons color white
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            activity.getWindow().setStatusBarColor(Color.WHITE);
        }
    }

    public void ContinueWithEmailButton(View view){
        RL_email = findViewById(R.id.RL_email);
        btn_email_login = findViewById(R.id.btn_email_login);
        continue_btn = findViewById(R.id.Continue_btn);
        email_editText = findViewById(R.id.et_email);

        RL_email.setVisibility(View.VISIBLE);
        btn_email_login.setVisibility(View.GONE);


        continue_btn.setClickable(false);


        email_editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                continue_btn.setClickable(false);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!TextUtils.isEmpty(s) && Patterns.EMAIL_ADDRESS.matcher(s).matches()) {
                    continue_btn.setClickable(true);
                    continue_btn.setBackgroundColor(getResources().getColor(R.color.custom_red_for_buttons));
                }else{
                    continue_btn.setClickable(false);
                    continue_btn.setBackgroundColor(getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral80));
                }
            }
        });




    }

    public  void ContinueButton(View view){
        String email = email_editText.getText().toString();
        auth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        List<String> signInMethods = task.getResult().getSignInMethods();
                        if (signInMethods != null && !signInMethods.isEmpty()) {
                            // email address exists and has a sign-in method associated with it
                            for (String signInMethod : signInMethods) {
                                switch (signInMethod) {
                                    case EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD:
                                        //Toast.makeText(WelcomeActivity.this, "Email and password authentication", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(WelcomeActivity.this,LoginActivity.class);
                                        intent.putExtra("Email", email);
                                        startActivity(intent);
                                        break;
                                    case GoogleAuthProvider.GOOGLE_SIGN_IN_METHOD:
                                        //Toast.makeText(WelcomeActivity.this, "Use Google authentication", Toast.LENGTH_SHORT).show();
                                        DisplaySnackbar(view,"Use Google authentication");
                                        break;
                                    case FacebookAuthProvider.FACEBOOK_SIGN_IN_METHOD:
                                        //Toast.makeText(WelcomeActivity.this, "Use Facebook authentication", Toast.LENGTH_SHORT).show();
                                        DisplaySnackbar(view,"Use Facebook authentication");
                                        break;
                                }
                            }
                        } else {
                            // email address does not exist or has no sign-in method associated with it
                            Intent intent = new Intent(WelcomeActivity.this,SignUpActivity.class);
                            intent.putExtra("Email", email);
                            startActivity(intent);
                        }
                    } else {
                       // Toast.makeText(WelcomeActivity.this, "error occurred while checking email address", Toast.LENGTH_LONG).show();
                    DisplaySnackbar(view,"error occurred while checking email address");
                    }
                });
    }
    public void ContinueWithGoogleButton(View view){
        Intent intent =  mGoogleSignInClient.getSignInIntent();
        startActivityForResult(intent,RC_SIGN_IN);
    }

    public void ContinueWithFacebookButton(View view){
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

    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser u = auth.getCurrentUser();

                            User user = new User(u.getDisplayName());

                            users.document(auth.getUid())
                                    .set(user)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(getApplicationContext(), "written to db"+ user.getToken(), Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(WelcomeActivity.this,MainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(getApplicationContext(), "Error writing document", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                        } else {
                            // If sign in fails, display a message to the user.
                            showMessage("Authentication Failed",task.getException().getLocalizedMessage());
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode != RESULT_CANCELED){
            if (requestCode == RC_SIGN_IN){

                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

                try{
                    GoogleSignInAccount account = task.getResult(ApiException.class);

                    googleFirebaseAuth(account.getIdToken());
                } catch (ApiException e) {
                    throw new RuntimeException();
                }
            }else if(requestCode == FacebookSdk.getCallbackRequestCodeOffset()){
                mCallbackManager.onActivityResult(requestCode, resultCode, data);
            }
        }

    }

    private void googleFirebaseAuth(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken,null);

        auth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    FirebaseUser u = auth.getCurrentUser();
                    User user = new User(u.getDisplayName());

                    users.document(auth.getUid())
                            .set(user)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(getApplicationContext(), "written to db"+ user.getToken(), Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(WelcomeActivity.this,MainActivity.class);
                                    startActivity(intent);
                                    finish();
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
                }
            }
        });
    }

    void showMessage(String title, String message){
        new AlertDialog.Builder(this).setTitle(title).setMessage(message).setCancelable(true).show();
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