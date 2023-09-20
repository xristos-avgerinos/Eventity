package com.unipi.chrisavg.eventity.ui.settings;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.AggregateQuerySnapshot;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.unipi.chrisavg.eventity.HobbySelectionModify;
import com.unipi.chrisavg.eventity.R;
import com.unipi.chrisavg.eventity.User;
import com.unipi.chrisavg.eventity.databinding.FragmentSettingsBinding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    FirebaseAuth mAuth;
    CollectionReference events,reservations,users;
    FirebaseFirestore db;

    TextView FullnameTv,EmailTv,TicketsCount,TotalEvents,FullnameTextView,PhoneNumberTextView;
    EditText EmailEditText;

    GridLayout gridLayout;

    LinearLayout FullnameLinLay,PhoneNumberLinLay;

    FirebaseUser firebaseUser;

    FloatingActionButton floatingActionButton;
    private View loadingLayout;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


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
        FullnameTextView = root.findViewById(R.id.fullnameTextView);
        PhoneNumberTextView = root.findViewById(R.id.phoneNumberTextView);
        gridLayout = root.findViewById(R.id.related_gridview);
        FullnameLinLay = root.findViewById(R.id.fullnameLinLay);
        PhoneNumberLinLay = root.findViewById(R.id.phoneNumberLinLay);
        floatingActionButton = root.findViewById(R.id.floatingActionButton);

        loadingLayout = root.findViewById(R.id.loading_layout);


        firebaseUser = mAuth.getCurrentUser();

        FullnameTv.setText(mAuth.getCurrentUser().getDisplayName());
        EmailTv.setText(mAuth.getCurrentUser().getEmail());

        // Βρίσκουμε τον αριθμό κλεισμένων εισητηρίων του συνδεμένου user
        reservations.whereEqualTo("userId", mAuth.getUid()).count().get(AggregateSource.SERVER)
                .addOnCompleteListener(new OnCompleteListener<AggregateQuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<AggregateQuerySnapshot> task) {
                if (task.isSuccessful()) {
                    AggregateQuerySnapshot snapshot = task.getResult();
                    TicketsCount.setText(String.valueOf(snapshot.getCount()));
                } else {
                    Toast.makeText(getContext(), task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Παίρνουμε την τρέχουσα ημερομηνία
        Date currentDatetime = Calendar.getInstance().getTime();


        // Βρίσκουμε τον αριθμό των event που έχουν καταχωρηθεί με date μελλοντικό
        events.whereGreaterThan("Date", currentDatetime).count().get(AggregateSource.SERVER)
                .addOnCompleteListener(new OnCompleteListener<AggregateQuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<AggregateQuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            AggregateQuerySnapshot snapshot = task.getResult();
                            TotalEvents.setText(String.valueOf(snapshot.getCount()));
                        } else {
                            Toast.makeText(getContext(), task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }

                        loadingLayout.setVisibility(View.GONE);
                    }

                });

        EmailEditText.setKeyListener(null); //Για να μην μπορεί να αλλαξει το email του αφου ειναι στανταρ

        EmailEditText.setText(mAuth.getCurrentUser().getEmail());
        FullnameTextView.setText(mAuth.getCurrentUser().getDisplayName());

        FullnameLinLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEditDialogForFullname();
            }
        });

        PhoneNumberLinLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEditDialogForPhoneNumber();
            }
        });

        // Ορισμός ενός DocumentReference στο document Firestore του χρήστη
        DocumentReference userDocumentRef = users.document(mAuth.getUid());

        // Δημιουργούμε έναν snapshot listener για το document του χρήστη ωστε να ανανεώνει
        // το gridLayout με τα preferences του χρήστη οταν γυρνάει πισω απο την HobbySelectionModify
        userDocumentRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.e(TAG, "Listen failed.", e);
                    return;
                }

                if(documentSnapshot != null && documentSnapshot.exists()){

                    User user = documentSnapshot.toObject(User.class);
                    if (!(user.getPhoneNumber() == null)){
                        PhoneNumberTextView.setText(user.getPhoneNumber());
                    }

                    gridLayout.removeAllViews();

                    for (String userPreference : user.getPreferences()) {
                        ToggleButton toggleButton = new ToggleButton(getContext());
                        // Ορίζουμε τα layout parameters
                        GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
                        layoutParams.width = GridLayout.LayoutParams.WRAP_CONTENT;
                        layoutParams.height = GridLayout.LayoutParams.WRAP_CONTENT;
                        layoutParams.setGravity(Gravity.FILL);
                        int marginInPixels = getResources().getDimensionPixelSize(R.dimen.toggle_button_margin);
                        layoutParams.setMargins(marginInPixels, marginInPixels, marginInPixels, marginInPixels);
                        toggleButton.setLayoutParams(layoutParams);
                        // Οριζουμε το padding
                        int paddingRight = getResources().getDimensionPixelSize(R.dimen.toggle_button_padding_right);
                        int paddingLeft = getResources().getDimensionPixelSize(R.dimen.toggle_button_padding_left);
                        toggleButton.setPadding(paddingLeft, 0, paddingRight, 0);
                        toggleButton.setText(userPreference);
                        toggleButton.setTextOff(userPreference);
                        toggleButton.setTextOn(userPreference);
                        toggleButton.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        // Ορίζουμε text size
                        float textSize = getResources().getDimension(R.dimen.toggle_button_text_size);
                        toggleButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
                        // Ορίζουμε typeface
                        Typeface typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL);
                        toggleButton.setTypeface(typeface);
                        // Το κάνουμε clickable
                        toggleButton.setClickable(false);
                        toggleButton.setTextColor(getResources().getColor(R.color.toggle_buttons_color));
                        // Ορίζουμε background
                        toggleButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.toggle_button_background));
                        // Ορίζουμε το textAllCaps σε false
                        toggleButton.setAllCaps(false);

                        gridLayout.addView(toggleButton);
                    }

                    floatingActionButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getContext(), HobbySelectionModify.class);
                            ArrayList<String> userPreferencesList = (ArrayList<String>) user.getPreferences();
                            intent.putStringArrayListExtra("UserPreferencesList",userPreferencesList);
                            startActivity(intent);
                        }
                    });

                }
                else{
                    Toast.makeText(getContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();

                }
            }
        });




        return root;
    }

    private void openEditDialogForFullname() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Your fullname");


        // Δημιουργούμε ένα container (LinearLayout) για να τυλίξετε το EditText
        LinearLayout container = new LinearLayout(getContext());
        container.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        int leftPaddingInDp = 21;
        int rightPaddingInDp = 21;

        int leftPaddingInPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, leftPaddingInDp, getResources().getDisplayMetrics());

        int rightPaddingInPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, rightPaddingInDp, getResources().getDisplayMetrics());

        // Δημιουργούμε ενα EditText
        final EditText input = new EditText(getContext());
        input.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        input.setText(FullnameTextView.getText());

        container.setPadding(leftPaddingInPx, 0, rightPaddingInPx, 0);

        // Προσθέτουμε το EditText στο container
        container.addView(input);

        builder.setView(container);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newText = input.getText().toString();
                if (!newText.isEmpty()){
                    FullnameTextView.setText(newText);
                    FullnameTv.setText(newText);
                    // Δημιουργούμε έναν χάρτη για να αποθηκεύσουμετα ενημερωμένα δεδομένα (fullname)
                    Map<String, Object> updateData = new HashMap<>();
                    updateData.put("fullname", newText);

                    // Ενημερώνουμε το document
                    users.document(mAuth.getUid()).update(updateData).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            DisplaySnackbar("Something went wrong!");
                        }
                    });


                    //αλλαζω το displayName του FirebaseUser οπως το Fullname που δινει ο χρηστης για να εχω απευθειας προσβαση στην MainActivity που το δειχνω στο navigation drawer
                    if (firebaseUser != null) {
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(newText)
                                .build();

                        firebaseUser.updateProfile(profileUpdates)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d("SignUpActivity", "User profile updated.");
                                        } else {
                                            Log.e("SignUpActivity", "Failed to update user profile.", task.getException());
                                        }
                                    }
                                });
                    }
                }else{
                    DisplaySnackbar("Please give a valid fullname!");
                }
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    private void openEditDialogForPhoneNumber() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Your Phone Number");


        // Δημιουργούμε ένα container (LinearLayout) για να τυλίξετε το EditText
        LinearLayout container = new LinearLayout(getContext());
        container.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        int leftPaddingInDp = 21;
        int rightPaddingInDp = 21;

        int leftPaddingInPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, leftPaddingInDp, getResources().getDisplayMetrics());

        int rightPaddingInPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, rightPaddingInDp, getResources().getDisplayMetrics());

        final EditText input = new EditText(getContext());
        input.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        input.setText(PhoneNumberTextView.getText());
        input.setHint("Write your phone number");

        container.setPadding(leftPaddingInPx, 0, rightPaddingInPx, 0);

        container.addView(input);

        builder.setView(container);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newText = input.getText().toString();
                if (newText.isEmpty()) {
                    DisplaySnackbar("Please give a phone number!");
                }else if(!newText.matches("\\d+") || newText.length() !=10){
                    DisplaySnackbar("Mobile phone number must be 10 digits!");
                }else{

                    PhoneNumberTextView.setText(newText);
                    // Δημιουργούμε έναν χάρτη για να αποθηκεύσουμετα ενημερωμένα δεδομένα (phone number)
                    Map<String, Object> updateData = new HashMap<>();
                    updateData.put("phoneNumber", newText);

                    // Ενημερώνουμε το document
                    users.document(mAuth.getUid()).update(updateData).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            DisplaySnackbar("Something went wrong!");
                        }
                    });

                }
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    void DisplaySnackbar(String message){

        Snackbar snackbar =  Snackbar.make(((Activity) getActivity()).findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT);
        View v = snackbar.getView();
        TextView tv = (TextView) v.findViewById(com.google.android.material.R.id.snackbar_text);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        // Ορίζουμε τον μέγιστο αριθμό γραμμών για το μήνυμα Snackbar ώστε να εμφανίζεται όλο το κείμενο
        tv.setMaxLines(Integer.MAX_VALUE);
        snackbar.show();
    }
}