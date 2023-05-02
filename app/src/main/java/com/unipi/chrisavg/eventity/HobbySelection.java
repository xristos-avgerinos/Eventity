package com.unipi.chrisavg.eventity;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;

public class HobbySelection extends AppCompatActivity {

    GridLayout MusicGridLayout;
    GridLayout VibeGridLayout;
    boolean MusicGridLayoutExpanded;
    boolean VibeGridLayoutExpanded;

    TextView music_expand;
    TextView vibe_expand;
    Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hobby_selection);

        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayShowTitleEnabled(false);
        // getSupportActionBar().hide(); //hide the title bar

        setStatusBarWhite(this);


        music_expand = findViewById(R.id.music_expand);
        vibe_expand = findViewById(R.id.vibe_expand);

        MusicGridLayout = findViewById(R.id.Music_gridview);
        HideRows(MusicGridLayout,9,15);

        VibeGridLayout = findViewById(R.id.Vibe_gridview);
        HideRows(VibeGridLayout,9,17);


    }

    private void setStatusBarWhite(AppCompatActivity activity){
        //Make status bar icons color white
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            activity.getWindow().setStatusBarColor(Color.WHITE);
        }
    }

    public void HideRows(GridLayout gridLayout, int start, int end){

        // Set visibility of remaining rows to GONE
        for (int i = start; i < end; i++) {
            View view = gridLayout.getChildAt(i);
            view.setVisibility(View.GONE);
        }
        if (gridLayout==MusicGridLayout){
            MusicGridLayoutExpanded = false;
            music_expand.setText("See More ▼");
        }else if (gridLayout==VibeGridLayout){
            VibeGridLayoutExpanded = false;
            vibe_expand.setText("See More ▼");
        }
    }

    public void ShowHiddenRows(GridLayout gridLayout, int start, int end){

        // Set visibility of remaining rows to GONE
        for (int i = start; i < end; i++) {
            View view = gridLayout.getChildAt(i);
            view.setVisibility(View.VISIBLE);
        }

        if (gridLayout==MusicGridLayout){
            MusicGridLayoutExpanded = true;
            music_expand.setText("See Less ▲");
        }else if (gridLayout==VibeGridLayout){
            VibeGridLayoutExpanded = true;
            vibe_expand.setText("See Less ▲");
        }

    }

    public void ExpandOrShrinkMusicGrid(View view){
        if (MusicGridLayoutExpanded){
            HideRows(MusicGridLayout,9,15);
        }else{
            ShowHiddenRows(MusicGridLayout,9,15);
        }
    }

    public void ExpandOrShrinkVibeGrid(View view){
        if (VibeGridLayoutExpanded){
            HideRows(VibeGridLayout,9,17);
        }else{
            ShowHiddenRows(VibeGridLayout,9,17);
        }
    }
}