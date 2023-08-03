package com.unipi.chrisavg.eventity;

import android.content.Context;
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

    List<String> rTitle = new ArrayList<>();
    List<String> rDescription = new ArrayList<>();
    List<String> rImgs = new ArrayList<>();
    ArrayList<String> temp_rTitle =new ArrayList<>();
    ArrayList<String> temp_rDescription = new ArrayList<>();
    ArrayList<String> temp_rImgs = new ArrayList<>();

    public ArrayAdapterClass(Context c, List<String> t, List<String> d, List<String> i) {
        super(c, R.layout.row, R.id.title, t);
        this.context = c;
        this.rTitle = t;
        this.rDescription = d;
        this.rImgs = i;

        this.temp_rTitle.addAll(t);
        this.temp_rImgs.addAll(i);
        this.temp_rDescription.addAll(d);

    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = layoutInflater.inflate(R.layout.row, parent, false);
        ImageView images = row.findViewById(R.id.image);
        TextView myTitle = row.findViewById(R.id.title);
        TextView myDescription = row.findViewById(R.id.Date);

        // now set our resources on views

        Glide.with(getContext())
                .load(rImgs.get(position))
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache the image for better performance
                .into(images);
        myTitle.setText(rTitle.get(position));
        myDescription.setText(rDescription.get(position));

        return row;
    }

    //filter for search-view(search by title)
    public void filter(String charText){
        charText = charText.toLowerCase(Locale.getDefault());
        rTitle.clear();
        rImgs.clear();
        rDescription.clear();
        if (charText.length()==0){
            rTitle.addAll(temp_rTitle);
            rImgs.addAll(temp_rImgs);
            rDescription.addAll(temp_rDescription);
        }
        else {
            int counter = 0;
            for ( String title : temp_rTitle){
                if (title.toLowerCase(Locale.getDefault())
                        .contains(charText)){
                    rTitle.add(title);
                    rImgs.add(temp_rImgs.get(counter));
                    rDescription.add(temp_rDescription.get(counter));
                }
                counter++;
            }
        }
        notifyDataSetChanged();
    }
}