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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
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

    LinearLayout linearLayoutPb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        users = db.collection("Users");

        linearLayoutPb = (LinearLayout) findViewById(R.id.linlaHeaderProgress);


        toolbar = findViewById(R.id.toolbar);
        Intent intent = getIntent();
        email = intent.getStringExtra("Email");

        email_textview = findViewById(R.id.email_textview);
        email_textview.setText(email);

        setSupportActionBar(toolbar);

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
                    // Το EditText έχει αποκτήσει εστίαση, οποτε αλλάζουμε το χρώμα της υπογράμμισης και το ύψος του
                    underline.setBackgroundColor(getResources().getColor(R.color.underline_color));
                    ViewGroup.LayoutParams layoutParams = underline.getLayoutParams();
                    int heightInDp = 2; // θετουμε το επιθυμητό ύψος σε dp
                    int heightInPixels = (int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP, heightInDp, getResources().getDisplayMetrics());
                    layoutParams.height = heightInPixels;
                    underline.setLayoutParams(layoutParams);
                } else {
                    // Το EditText έχει χάσει την εστίαση, οποτε αλλάζουμε το χρώμα της υπογράμμισης και το ύψος πίσω στο κανονικό
                    underline.setBackgroundColor(getResources().getColor(R.color.black));
                    ViewGroup.LayoutParams layoutParams = underline.getLayoutParams();
                    int heightInDp = 1;  // θετουμε το επιθυμητό ύψος σε dp
                    int heightInPixels = (int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP, heightInDp, getResources().getDisplayMetrics());
                    layoutParams.height = heightInPixels;
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
                //Ενεργοποιουμε και απενεργοποιουμε το κουμπι του login καθως λογω του selector my_button_background.xml
                // που εχουμε φτιαξει για το background του κουμπιου το κανει γκρι η κοκκινο
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

        linearLayoutPb.setVisibility(View.VISIBLE);

        auth.signInWithEmailAndPassword(email,password_editText.getText().toString())
                .addOnCompleteListener((task)->{
                    if(task.isSuccessful()){

                        users.document(auth.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if(documentSnapshot.exists()){
                                    User user = documentSnapshot.toObject(User.class);
                                    //καθε φορα που κανουμε login ειναι αναγκαιο να ενημερωσουμε τα SharedPreferences αναλογα με το αν ο χρηστης
                                    // εχει επιλεξει προτιμησεις η οχι αν δηλ η λιστα preferences εχει size 0 η οχι ωστε αν δεν εχει επιλεξει
                                    // μετα μεσω της onStart μεθοδου της MainActivity που τον στελνουμε να τον ανακατευθυνουμε στο HobbySelectionActivity
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
                                else{
                                    //αν δεν βρεθηκε ο χρηστης στη βαση αλλα εγινε signIn απο το authentication πρεπει να τον κανουμε
                                    //logout ωστε να μην τον ανακατευθυνει η σελιδα welcomeActivity στην MainActivity μεσω της onStart
                                    auth.signOut();
                                    LoginManager.getInstance().logOut();
                                    DisplaySnackbar(view,"Something went wrong!Try again later.");
                                }
                            }
                        });

                    }else {
                        //αν πηγε κατι λαθος με την συνδεση του χρηστη(με το authentication δηλ
                        // οχι την βαση) ανιχνευουμε το exception και εμφανιζουμε καταλληλο μηνυμα.
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            DisplaySnackbar(view,"The password is invalid or the user does not have a password");

                        } else if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                            if (((FirebaseAuthInvalidUserException) task.getException()).getErrorCode().equals("ERROR_USER_DISABLED")) {
                                // Ο λογαριασμός χρήστη είναι αποκλεισμένος
                                DisplaySnackbar(view,"User account is blocked");
                            }
                        }
                        else if(task.getException() instanceof FirebaseNetworkException){
                            // Δεν υπάρχει σύνδεση στο διαδίκτυο
                            DisplaySnackbar(view,"A network error has occurred. Connect to the internet and try again");
                        }
                        else if(task.getException() instanceof FirebaseTooManyRequestsException){
                            // Όλα τα αιτήματα από αυτήν τη συσκευή έχουν αποκλειστε. Εμφάνιση κατάλληλου μηνύματος στον χρήστη
                            DisplaySnackbar(view,"The account has been blocked due to unusual activity.You may entered wrong password too many times.Try again later or reset your password.");

                        }
                        else {
                            Log.e(TAG, task.getException().getLocalizedMessage());
                            DisplaySnackbar(view,task.getException().getLocalizedMessage());
                        }

                    }
                    linearLayoutPb.setVisibility(View.GONE);
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
        // θετουμε τον μέγιστο αριθμό γραμμών για το μήνυμα Snackbar ώστε να εμφανίζεται όλο το κείμενο
        tv.setMaxLines(Integer.MAX_VALUE);
        snackbar.show();
    }

    public void ChangeEmail(View view){
        finish();
    }
}