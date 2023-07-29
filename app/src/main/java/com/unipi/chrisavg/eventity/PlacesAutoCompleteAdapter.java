package com.unipi.chrisavg.eventity;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class PlacesAutoCompleteAdapter extends RecyclerView.Adapter<PlacesAutoCompleteAdapter.PlaceViewHolder> {

    private List<AutocompletePrediction> predictions = new ArrayList<>(); //Η λιστα που θα κραταει ολες τις καθε φορα προβλεψεις αναλογα με τι πληκτρολογησε ο χρηστης στο searchView
    private Context context; // Ωστε να εχουμε προσβαση στο context του MainActivity
    private OnPlaceItemClickListener itemClickListener;
    boolean itemSelected = false; //ωστε να ξερουμε αν εχει γινει η τελικη επιλογη περιοχης


    // Constructor to receive the context
    public PlacesAutoCompleteAdapter(Context context,OnPlaceItemClickListener itemClickListener) {
        this.context = context;
        this.itemClickListener = itemClickListener;
    }

    public void setPredictions(List<AutocompletePrediction> predictions) {
        this.predictions = predictions;
    }
    public void clearPredictions() {
        if (predictions != null) {
            predictions = new ArrayList<>(); // Create a new instance of the list
            notifyDataSetChanged();
        }
    }

    public boolean isItemSelected() {
        return itemSelected;
    }

    public void setItemSelected(boolean itemSelected) {
        this.itemSelected = itemSelected;
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_selectable_list_item, parent, false);
        return new PlaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        AutocompletePrediction prediction = predictions.get(position);
        holder.bind(prediction);
    }

    @Override
    public int getItemCount() {
        return predictions.size();
        /*return Math.min(predictions.size(), 4); αν θελω να περιορισω τις προβλεψεις που θα εμφανιζονται στο recyclerView*/
    }

    class PlaceViewHolder extends RecyclerView.ViewHolder {

        private final TextView textView;

        PlaceViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }

        void bind(AutocompletePrediction prediction) {
            textView.setText(prediction.getFullText(null));
            itemView.setOnClickListener(v -> {
                // Handle the item click (e.g., pass selected place details to the main activity)
                String placeId = prediction.getPlaceId();
                String placeName = prediction.getPrimaryText(null).toString();
                // Perform further operations based on the selected place
                if (itemClickListener != null) {
                    itemClickListener.onPlaceItemClick(prediction);
                    clearPredictions();
                }


                //τα στελνω σε αυτη τη μεθοδο ωστε να βρω τα ακριβη coordinates της επιλεγμενης περιοχης
                fetchPlaceDetails(placeId, placeName);

            });
        }
    }


    private void fetchPlaceDetails(String placeId, String placeName) {
        List<Place.Field> placeFields = Arrays.asList(Place.Field.LAT_LNG);

        FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, placeFields).build();

        Places.createClient(context).fetchPlace(request)
                .addOnSuccessListener((response) -> {
                    Place place = response.getPlace();

                    // Get latitude and longitude of the selected place
                    LatLng latLng = place.getLatLng();
                    double latitude = latLng.latitude;
                    double longitude = latLng.longitude;

                    // Show the Snackbar with the selected place name and location
                    showSnackbar(placeName, latitude, longitude);
                })
                .addOnFailureListener((exception) -> {
                    // Handle any errors that occurred during the request
                });
    }

    private void showSnackbar(String placeName, double latitude, double longitude) {
        String locationInfo = String.format(Locale.getDefault(), "Selected place: %s\nLatitude: %f, Longitude: %f", placeName, latitude, longitude);
        Snackbar.make(((Activity) context).findViewById(android.R.id.content), locationInfo, Snackbar.LENGTH_LONG).show();
    }
}
