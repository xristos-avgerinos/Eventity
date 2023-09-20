package com.unipi.chrisavg.eventity;

import com.google.android.libraries.places.api.model.AutocompletePrediction;

//φτιαξαμε αυτο το interface ωστε να το κανει implement το MainActivity και οταν ο χρηστης επιλεξει μια περιοχη
// απο τις προτεινομενες τοτε μεσω της μεθοδου αυτης που υλοποιει το interface να μεταφερουμε το prediction στο mainActivity
public interface OnPlaceItemClickListener {
    void onPlaceItemClick(AutocompletePrediction prediction);
}
