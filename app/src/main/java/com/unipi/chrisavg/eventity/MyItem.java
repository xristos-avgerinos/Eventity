package com.unipi.chrisavg.eventity;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;


public class MyItem implements ClusterItem {
    private LatLng position;
    private String title;
    private String snippet;

    public MyItem(double lat, double lng, String title, String snippet) {
        position = new LatLng(lat, lng);
        this.title = title;
        this.snippet = snippet;
    }

    @Override
    public LatLng getPosition() {
        return position;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getSnippet() {
        return snippet;
    }
}



