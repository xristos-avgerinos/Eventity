package com.unipi.chrisavg.eventity;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.wifi.hotspot2.pps.Credential;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import com.facebook.FacebookSdk;
import java.util.Arrays;
import java.util.List;


public class WelcomeActivity extends AppCompatActivity {

    GoogleSignInClient mGoogleSignInClient;
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


        //για την συνεδεση του χρηστη μεσω google provider
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
        // Αν ο χρηστης ειναι ηδη συνδεμενος τον στελνουμε απευθειας στην MainActivity
        FirebaseUser currentUser = auth.getCurrentUser();

        if(currentUser!= null){
            Intent intent = new Intent(WelcomeActivity.this,MainActivity.class);
            startActivity(intent);
            finish();
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

        continue_btn.setClickable(false); //να ειναι γκρι και να μην μπορει να τον πατησει μεχρι να δωσει εγκυρο email

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
                    //αν το email ειναι εγκυρο τοτε ξανα κανουμε clickable το κουμπι για να ειναι αλληλεπιδρασιμο με τον χρηστη και αλλαζουμε το χρωμα του σε κοκκινο
                    continue_btn.setClickable(true);
                    continue_btn.setBackgroundColor(getResources().getColor(R.color.custom_red_for_buttons));
                }else{
                    //αν το email δεν ειναι εγκυρο τοτε ξανα κανουμε το κουμπι μη αλληλεπιδρασιμο με τον χρηστη και αλλαζουμε το χρωμα του σε γκρι παλι
                    continue_btn.setClickable(false);
                    continue_btn.setBackgroundColor(getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral80));
                }
            }
        });

    }

    public  void ContinueButton(View view){
        String email = email_editText.getText().toString();
        auth.fetchSignInMethodsForEmail(email) //ελεγχουμε με ποιον provider εχει γινει signed up ο χρηστης
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        List<String> signInMethods = task.getResult().getSignInMethods();
                        if (signInMethods != null && !signInMethods.isEmpty()) {
                            // Η διευθυνση email υπαρχει και ειναι συνδεμενη με καποιο provider
                            for (String signInMethod : signInMethods) {
                                switch (signInMethod) {
                                    case EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD:
                                        Intent intent = new Intent(WelcomeActivity.this,LoginActivity.class);
                                        intent.putExtra("Email", email);
                                        startActivity(intent);
                                        break;
                                    case GoogleAuthProvider.GOOGLE_SIGN_IN_METHOD:
                                        DisplaySnackbar(view,"Sign in using Google provider which is associated with this email address.");
                                        break;
                                    case FacebookAuthProvider.FACEBOOK_SIGN_IN_METHOD:
                                        DisplaySnackbar(view,"Sign in using Facebook provider which is associated with this email address.");
                                        break;
                                }
                            }
                        } else {
                            // Η διεύθυνση email δεν υπάρχει ή δεν έχει συσχετιστεί με καποια μέθοδος σύνδεσης οποτε πρεπει
                            // να στειλουμε τον χρηστη για sign up με email-password με βαση το email αυτο που εχει δβσει
                            Intent intent = new Intent(WelcomeActivity.this,SignUpActivity.class);
                            intent.putExtra("Email", email);
                            startActivity(intent);
                        }
                    } else {

                        if(task.getException() instanceof FirebaseNetworkException){
                            // No internet connection
                            DisplaySnackbar(view,"A network error has occurred. Connect to the internet and try again");
                        }
                        else {
                            Log.e(TAG, task.getException().getLocalizedMessage());
                            DisplaySnackbar(view,"Error occurred while checking email address");
                        }
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
                DisplaySnackbar(view,error.getLocalizedMessage());
            }
        });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        SignInOrSignUpUser(credential);
    }


    private void googleFirebaseAuth(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken,null);
        SignInOrSignUpUser(credential);
    }

    void SignInOrSignUpUser(AuthCredential credential){

        //μεθοδος που θα εκτελεστει οταν ο χρηστης παει να κανει log in η sign up με google η facebook Provider με βαση τα credentials
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            users.document(auth.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    if(documentSnapshot.exists()){
                                        //αν ο συγκεκριμενος χρηστης με το συγκεκριμενο Uid υπαρχει ηδη στη βαση
                                        // σημαινει οτι εχει γινει signup ηδη οποτε κανουμε απλο login
                                        //οποτε απλα ενημερωνουμε τα shared Preferences αναλογα με το αν εχει
                                        // γινει επιλογη ενδιαφεροντων και τον στελνουμε στην MainActivity
                                        User user = documentSnapshot.toObject(User.class);
                                        if (user.getPreferences().size()==0){
                                            PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("preferencesSelected", "False").apply();
                                        }else{
                                            PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("preferencesSelected", "True").apply();
                                        }
                                        Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();

                                    }else{
                                        //διαφορετικα αν ο χρηστης αυτος κανει πρωτη φορα signup με facebook η google provider πρεπει να
                                        // γραφτει και στη βαση με fullname το ονομα που εχει και στο Google η Facebook. Κανονικο sign up.
                                        FirebaseUser firebaseUser = auth.getCurrentUser();
                                        User user = new User(firebaseUser.getDisplayName());
                                        users.document(auth.getUid())
                                                .set(user)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("preferencesSelected", "False").apply();
                                                        Toast.makeText(WelcomeActivity.this, getString(R.string.user_registered), Toast.LENGTH_SHORT).show();
                                                        Intent intent = new Intent(WelcomeActivity.this,MainActivity.class);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        auth.signOut();
                                                        LoginManager.getInstance().logOut();
                                                        DisplaySnackbar(findViewById(android.R.id.content),e.getLocalizedMessage());
                                                    }
                                                });
                                    }
                                }
                            });

                        } else {
                            if(task.getException() instanceof FirebaseAuthUserCollisionException){
                                // Ο χρήστης έχει συνδεθεί με άλλο provider
                                DisplaySnackbar(findViewById(android.R.id.content),"An account already exists with the same email address but different sign-in credentials. Sign in using a provider associated with this email address.");
                            }
                            else {
                                // Εάν η σύνδεση αποτύχει, εμφανίζουμε ένα μήνυμα στον χρήστη.
                                DisplaySnackbar(findViewById(android.R.id.content),task.getException().getLocalizedMessage());
                            }

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

    void DisplaySnackbar(View view,String message){

        Snackbar snackbar =  Snackbar.make(view, message, Snackbar.LENGTH_LONG);
        View v = snackbar.getView();
        TextView tv = (TextView) v.findViewById(com.google.android.material.R.id.snackbar_text);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        // Ορίζουμε τον μέγιστο αριθμό γραμμών για το μήνυμα Snackbar ώστε να εμφανίζεται όλο το κείμενο
        tv.setMaxLines(Integer.MAX_VALUE);
        snackbar.show();
    }
}