package com.unipi.chrisavg.eventity;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

//Μια κλαση για τα cluster items του map
public class MyClusterItem implements ClusterItem {
    private final LatLng position;
    private final String title;
    private final String snippet;
    private Event event;

    public MyClusterItem(LatLng position, String title, String snippet,Event event) {
        this.position = position;
        this.title = title;
        this.snippet = snippet;
        this.event = event;
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

    public Event getEvent() {
        return event;
    }
}
