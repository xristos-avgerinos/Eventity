package com.unipi.chrisavg.eventity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class LoginActivity extends AppCompatActivity {

    Button logInBtn;
    TextInputEditText password_editText;
    View underline;
    FirebaseAuth auth;
    FirebaseUser user;
    TextView email_textview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();


        Intent intent = getIntent();
        String email = intent.getStringExtra("Email");

        email_textview = findViewById(R.id.email_textview);
        email_textview.setText(email);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.custom_action_bar_layout);
        View view = getSupportActionBar().getCustomView();

        ImageButton imageButton = (ImageButton) view.findViewById(R.id.action_bar_back);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
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

    private void setStatusBarWhite(AppCompatActivity activity){
        //Make status bar icons color white
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            activity.getWindow().setStatusBarColor(Color.WHITE);
        }
    }
    public void ChangeEmail(View view){
        finish();
    }
}