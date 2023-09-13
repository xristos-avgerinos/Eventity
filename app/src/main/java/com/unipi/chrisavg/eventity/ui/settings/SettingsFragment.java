package com.unipi.chrisavg.eventity.ui.settings;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.AggregateQuerySnapshot;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.unipi.chrisavg.eventity.Event;
import com.unipi.chrisavg.eventity.LoginActivity;
import com.unipi.chrisavg.eventity.MainActivity;
import com.unipi.chrisavg.eventity.R;
import com.unipi.chrisavg.eventity.Reservation;
import com.unipi.chrisavg.eventity.User;
import com.unipi.chrisavg.eventity.databinding.FragmentHomeBinding;
import com.unipi.chrisavg.eventity.databinding.FragmentSettingsBinding;

import java.util.Calendar;
import java.util.Date;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    FirebaseAuth mAuth;
    CollectionReference events,reservations,users;
    FirebaseFirestore db;

    TextView FullnameTv,EmailTv,TicketsCount,TotalEvents;
    EditText EmailEditText,PhoneNumberEditText,FullnameEditText;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        events = db.collection("Events");
        reservations = db.collection("Reservations");
        users = db.collection("Users");

        FullnameTv = root.findViewById(R.id.FullnameTv);
        EmailTv = root.findViewById(R.id.EmailTV);
        TicketsCount = root.findViewById(R.id.tickets_count);
        TotalEvents = root.findViewById(R.id.total_events);
        EmailEditText = root.findViewById(R.id.emailEditText);
        FullnameEditText = root.findViewById(R.id.fullnameEditText);
        PhoneNumberEditText = root.findViewById(R.id.phoneNumberEditText);
        GridLayout gridLayout = root.findViewById(R.id.related_gridview);

        FullnameTv.setText(mAuth.getCurrentUser().getDisplayName());
        EmailTv.setText(mAuth.getCurrentUser().getEmail());

        reservations.whereEqualTo("userId", mAuth.getUid()).count().get(AggregateSource.SERVER)
                .addOnCompleteListener(new OnCompleteListener<AggregateQuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<AggregateQuerySnapshot> task) {
                if (task.isSuccessful()) {
                    // Count fetched successfully
                    AggregateQuerySnapshot snapshot = task.getResult();
                    TicketsCount.setText(String.valueOf(snapshot.getCount()));
                } else {
                    Toast.makeText(getContext(), task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Get the current datetime
        Date currentDatetime = Calendar.getInstance().getTime();

        events.whereGreaterThan("Date", currentDatetime).count().get(AggregateSource.SERVER)
                .addOnCompleteListener(new OnCompleteListener<AggregateQuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<AggregateQuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            // Count fetched successfully
                            AggregateQuerySnapshot snapshot = task.getResult();
                            TotalEvents.setText(String.valueOf(snapshot.getCount()));
                        } else {
                            Toast.makeText(getContext(), task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });



        EmailEditText.setKeyListener(null);
        EmailEditText.setText(mAuth.getCurrentUser().getEmail());
        FullnameEditText.setText(mAuth.getCurrentUser().getDisplayName());

        users.document(mAuth.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    User user = documentSnapshot.toObject(User.class);
                    if (!(user.getPhoneNumber() == null)){
                        PhoneNumberEditText.setText(user.getPhoneNumber());
                    }

                    for (String userPreference : user.getPreferences()) {
                        ToggleButton toggleButton = new ToggleButton(getContext());
                        // Set layout parameters
                        GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
                        layoutParams.width = GridLayout.LayoutParams.WRAP_CONTENT;
                        layoutParams.height = GridLayout.LayoutParams.WRAP_CONTENT;
                        layoutParams.setGravity(Gravity.FILL);
                        int marginInPixels = getResources().getDimensionPixelSize(R.dimen.toggle_button_margin);
                        layoutParams.setMargins(marginInPixels, marginInPixels, marginInPixels, marginInPixels);
                        toggleButton.setLayoutParams(layoutParams);
                        // Set padding
                        int paddingRight = getResources().getDimensionPixelSize(R.dimen.toggle_button_padding_right);
                        int paddingLeft = getResources().getDimensionPixelSize(R.dimen.toggle_button_padding_left);
                        toggleButton.setPadding(paddingLeft, 0, paddingRight, 0);
                        toggleButton.setText(userPreference);
                        toggleButton.setTextOff(userPreference);
                        toggleButton.setTextOn(userPreference);
                        toggleButton.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        // Set text size
                        float textSize = getResources().getDimension(R.dimen.toggle_button_text_size);
                        toggleButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
                        // Set typeface
                        Typeface typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL);
                        toggleButton.setTypeface(typeface);
                        // Set clickable
                        toggleButton.setClickable(false);
                        toggleButton.setTextColor(getResources().getColor(R.color.toggle_buttons_color));
                        //set background
                        toggleButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.toggle_button_background));
                        // Set textAllCaps to false
                        toggleButton.setAllCaps(false);

                        gridLayout.addView(toggleButton);
                    }

                }
                else{
                    Toast.makeText(getContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();

                }
            }
        });








        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}