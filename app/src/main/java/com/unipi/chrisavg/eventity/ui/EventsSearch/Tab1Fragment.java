package com.unipi.chrisavg.eventity.ui.EventsSearch;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.unipi.chrisavg.eventity.R;

public class Tab1Fragment extends Fragment {
    private ListView listView;
    private String[] data = {"Item 1", "Item 2", "Item 3", "Item 4", "Item 5"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tab1, container, false);

        listView = view.findViewById(R.id.listView);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, data);
        listView.setAdapter(adapter);

        return view;
    }
}
