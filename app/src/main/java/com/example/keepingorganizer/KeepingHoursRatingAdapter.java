package com.example.keepingorganizer;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class KeepingHoursRatingAdapter extends ArrayAdapter<KeepingHour> {

    Context context;
    List<KeepingHour> data;
    KeepingList keepingList;

    public KeepingHoursRatingAdapter(Context context, int resource, int textViewResourceId, List<KeepingHour> data, KeepingList keepingList) {
        super(context, resource, textViewResourceId, data);

        this.context = context;
        this.data = data;
        this.keepingList = keepingList;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final View view = ((Activity)context).getLayoutInflater().inflate(R.layout.keeping_hour_rating_layout, parent ,false);
        TextView tvMinus = view.findViewById(R.id.tvMinus);
        TextView tvPlus = view.findViewById(R.id.tvPlus);
        final TextView tvRating = view.findViewById(R.id.tvRating);
        TextView tvStartTime = view.findViewById(R.id.tvStartTime);
        TextView tvEndTime = view.findViewById(R.id.tvEndTime);

        final KeepingHour keepingHour = data.get(position);

        tvStartTime.setText(String.format("%02d", keepingHour.getStartTime().getHourOfDay()) + ":" + String.format("%02d", keepingHour.getStartTime().getMinuteOfHour()));
        tvEndTime.setText(String.format("%02d", keepingHour.getEndTime().getHourOfDay()) + ":" + String.format("%02d", keepingHour.getEndTime().getMinuteOfHour()));
        tvRating.setText(String.valueOf(keepingHour.getHourRating()));

        tvMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (keepingHour.getHourRating() > 1) {
                    keepingHour.setHourRating(keepingHour.getHourRating() - 1);
                    tvRating.setText(String.valueOf(keepingHour.getHourRating()));
                    keepingList.getPlatoon().getHourRatings().set(position, keepingHour.getHourRating());
                }
            }
        });

        tvPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (keepingHour.getHourRating() < keepingList.MAX_RATE()) {
                    keepingHour.setHourRating(keepingHour.getHourRating() + 1);
                    tvRating.setText(String.valueOf(keepingHour.getHourRating()));
                    keepingList.getPlatoon().getHourRatings().set(position, keepingHour.getHourRating());
                }
            }
        });

        return view;
    }
}