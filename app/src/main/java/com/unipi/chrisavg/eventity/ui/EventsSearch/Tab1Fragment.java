package com.unipi.chrisavg.eventity.ui.EventsSearch;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.unipi.chrisavg.eventity.ArrayAdapterClass;
import com.unipi.chrisavg.eventity.R;

import java.util.ArrayList;
import java.util.List;

public class Tab1Fragment extends Fragment {
    ListView listView;
    LinearLayout linearLayoutPb;
    TextView emptyView;

    List<String> ListViewItemsTitle = new ArrayList<>();
    List<String> ListViewItemsDescription = new ArrayList<>();
    List<String> ListViewItemsImages = new ArrayList<>();
    ArrayAdapterClass arrayAdapterClass;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tab1, container, false);

        listView= (ListView) view.findViewById(R.id.SpecListview);
        emptyView=view.findViewById(R.id.emptyView);
        listView.setEmptyView(emptyView);
        emptyView.setVisibility(View.GONE);

        linearLayoutPb = (LinearLayout) view.findViewById(R.id.linlaHeaderProgress);

        linearLayoutPb.setVisibility(View.VISIBLE);


        ListViewItemsTitle.add("Sarkrista + Special Guests - Live in Athens");
        ListViewItemsDescription.add("Temple Athens, Ίακχου 17, Athens, Greece, Palaio Faliro, Greece");
        ListViewItemsImages.add("https://cdn-az.allevents.in/events7/banners/61ec31a907e18ae6aec7baacdecc6e2e91f36dfaeb5bb3c398141f24f3c5014c-rimg-w960-h503-gmir.jpg?v=1690810850");

        ListViewItemsTitle.add("Oikonomppoulos ααααα αααα");
        ListViewItemsDescription.add("Thatro petras athens");
        ListViewItemsImages.add("https://www.ticketservices.gr/pictures/original/b_33088_or_OIKONOMOPOULOS-2023.jpg");

        ListViewItemsTitle.add("Oikonomppoulos ααααα αααα");
        ListViewItemsDescription.add("Thatro petras athens");
        ListViewItemsImages.add("https://www.ticketservices.gr/pictures/original/b_33088_or_OIKONOMOPOULOS-2023.jpg");

        ListViewItemsTitle.add("Oikonomppoulos ααααα αααα");
        ListViewItemsDescription.add("Thatro petras athens");
        ListViewItemsImages.add("https://www.ticketservices.gr/pictures/original/b_33088_or_OIKONOMOPOULOS-2023.jpg");

        ListViewItemsTitle.add("Oikonomppoulos ααααα αααα");
        ListViewItemsDescription.add("Thatro petras athens");
        ListViewItemsImages.add("https://www.ticketservices.gr/pictures/original/b_33088_or_OIKONOMOPOULOS-2023.jpg");

        ListViewItemsTitle.add("Oikonomppoulos ααααα αααα");
        ListViewItemsDescription.add("Thatro petras athens");
        ListViewItemsImages.add("https://www.ticketservices.gr/pictures/original/b_33088_or_OIKONOMOPOULOS-2023.jpg");
        arrayAdapterClass = new ArrayAdapterClass(getContext(), ListViewItemsTitle, ListViewItemsDescription, ListViewItemsImages);

        listView.setAdapter(arrayAdapterClass);
        arrayAdapterClass.notifyDataSetChanged();
        linearLayoutPb.setVisibility(View.GONE);

        return view;
    }
}
