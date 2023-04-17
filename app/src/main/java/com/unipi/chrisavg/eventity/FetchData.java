package com.unipi.chrisavg.eventity;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class FetchData extends AsyncTask<Object, String, String> {
    String googleNearByPlacesData;
    GoogleMap googleMap;
    String url;


    @Override
    protected void onPostExecute(String s) {
        try{
            JSONObject jsonObject = new JSONObject(s);
            JSONArray jsonArray = jsonObject.getJSONArray("results");
            for(int i = 0; i < jsonArray.length(); i++){
                JSONObject jsOnObject = jsonArray.getJSONObject(i);
                JSONObject getLocation=jsOnObject.getJSONObject("geometry").getJSONObject("location");
                String lat = getLocation.getString("lat");
                String lng = getLocation.getString("lng");
                JSONObject getName = jsonArray.getJSONObject(i);
                String name = getName.getString("name");
                LatLng latlng = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.title(name);
                markerOptions.position(latlng);
                googleMap.addMarker(markerOptions);

                //googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng,15));


            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected String doInBackground(Object... objects) {
        try{
            googleMap = (GoogleMap) objects[0];
            url = (String) objects[1];
            DownloadURL downloadURL = new DownloadURL();
            googleNearByPlacesData = downloadURL.retrieveUrl(url);
            Log.d("near", googleNearByPlacesData);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return googleNearByPlacesData;
    }
}

