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
    private OnPlaceItemClickListener itemClickListener;
    boolean itemSelected = false; //ωστε να ξερουμε αν εχει γινει η τελικη επιλογη περιοχης


    // Constructor to receive the context
    public PlacesAutoCompleteAdapter(OnPlaceItemClickListener itemClickListener) {
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
                // Perform further operations based on the selected place
                if (itemClickListener != null) {
                    itemClickListener.onPlaceItemClick(prediction); //καλω τη μεθοδο του interface ωστε να μεταφερουμε το prediction στο mainActivity
                    clearPredictions();
                }

            });
        }
    }


}
