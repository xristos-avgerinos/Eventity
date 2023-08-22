package com.unipi.chrisavg.eventity;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class ArrayAdapterClass extends android.widget.ArrayAdapter<String> {

    Context context;

    List<String> rTitles = new ArrayList<>();
    List<String> rDates = new ArrayList<>();
    List<String> rLocations = new ArrayList<>();
    List<String> rImgs = new ArrayList<>();

    ArrayList<String> temp_rTitles =new ArrayList<>();
    ArrayList<String> temp_rDates = new ArrayList<>();
    ArrayList<String> temp_rLocations = new ArrayList<>();
    ArrayList<String> temp_rImgs = new ArrayList<>();

    public ArrayAdapterClass(Context ctx, List<String> titles, List<String> dates,List<String> locations ,List<String> images) {
        super(ctx, R.layout.row, R.id.title, titles);
        this.context = ctx;
        this.rTitles = titles;
        this.rDates = dates;
        this.rLocations = locations;
        this.rImgs = images;

        this.temp_rTitles.addAll(titles);
        this.temp_rDates.addAll(dates);
        this.temp_rLocations.addAll(locations);
        this.temp_rImgs.addAll(images);

    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = layoutInflater.inflate(R.layout.row, parent, false);
        ImageView myImage = row.findViewById(R.id.image);
        TextView myTitle = row.findViewById(R.id.title);
        TextView myDate = row.findViewById(R.id.Date);
        TextView myLocation = row.findViewById(R.id.Location);

        Glide.with(getContext())
                .load(rImgs.get(position))
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache the image for better performance
                .into(myImage);
        myTitle.setText(rTitles.get(position));
        myDate.setText(rDates.get(position));
        myLocation.setText(rLocations.get(position));

        return row;
    }

    //filter for search-view(search by title)
    public void filter(String charText){
        charText = charText.toLowerCase(Locale.getDefault());
        rTitles.clear();
        rDates.clear();
        rLocations.clear();
        rImgs.clear();

        if (charText.length()==0){
            rTitles.addAll(temp_rTitles);
            rDates.addAll(temp_rDates);
            rLocations.addAll(temp_rLocations);
            rImgs.addAll(temp_rImgs);

        }
        else {
            int counter = 0;
            for ( String title : temp_rTitles){
                if (title.toLowerCase(Locale.getDefault())
                        .contains(charText)){
                    rTitles.add(title);
                    rDates.add(temp_rDates.get(counter));
                    rLocations.add(temp_rLocations.get(counter));
                    rImgs.add(temp_rImgs.get(counter));

                }
                counter++;
            }
        }
        notifyDataSetChanged();
    }

    //filter for search-view(search by title)
    public void toggleButtonsContainerFilter (List<Event> eventsList,List<String> selectedTypes){
        rTitles.clear();
        rDates.clear();
        rLocations.clear();
        rImgs.clear();

        if (selectedTypes.size()==0){
            rTitles.addAll(temp_rTitles);
            rDates.addAll(temp_rDates);
            rLocations.addAll(temp_rLocations);
            rImgs.addAll(temp_rImgs);

        }
        else {
            int counter = 0;
            for ( Event event : eventsList){
                if (containsCommonItem(event.getTypes(),selectedTypes)){
                    rTitles.add(event.getTitle());
                    rDates.add(temp_rDates.get(counter));
                    rLocations.add(temp_rLocations.get(counter));
                    rImgs.add(temp_rImgs.get(counter));

                }
                counter++;
            }
        }
        notifyDataSetChanged();
    }

    public static boolean containsCommonItem(List<String> list1, List<String> list2) {
        for (String item : list1) {
            if (list2.contains(item)) {
                return true;
            }
        }
        return false;
    }
}