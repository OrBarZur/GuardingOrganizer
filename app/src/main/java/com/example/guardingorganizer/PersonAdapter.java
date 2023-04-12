package com.example.guardingorganizer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class PersonAdapter extends ArrayAdapter<Person> {

    Context context;
    List<Person> data;
    KeepingList keepingList;

    public PersonAdapter(Context context, int resource, int textViewResourceId, List<Person> data, KeepingList keepingList) {
        super(context, resource, textViewResourceId, data);

        this.context = context;
        this.data = data;
        this.keepingList = keepingList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = ((Activity)context).getLayoutInflater().inflate(R.layout.person_layout, parent ,false);
        TextView tvName = view.findViewById(R.id.tvName);
        TextView tvRating = view.findViewById(R.id.tvRating);
        Person person = data.get(position);
        tvName.setText(person.getName());
        tvRating.setText(String.valueOf(person.getSumValues()));

        if (keepingList.getAbsents().contains(person)) {
            tvName.setPaintFlags(tvName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            if (keepingList.isKeeping(person)) {
                tvName.setTextColor(((Activity) context).getResources().getColor(R.color.colorLastKeepingAbsents));
                tvRating.setTextColor(((Activity) context).getResources().getColor(R.color.colorLastKeepingAbsents));
            }
            else {
                tvName.setTextColor(((Activity) context).getResources().getColor(R.color.colorAbsents));
                tvRating.setTextColor(((Activity) context).getResources().getColor(R.color.colorAbsents));
            }
        }
        else {
            tvName.setPaintFlags(tvName.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            if (keepingList.isKeeping(person)) {
                tvName.setTextColor(((Activity) context).getResources().getColor(R.color.colorLastKeeping));
                tvRating.setTextColor(((Activity) context).getResources().getColor(R.color.colorLastKeeping));
            }
            else {
                tvName.setTextColor(((Activity) context).getResources().getColor(R.color.colorDefault));
                tvRating.setTextColor(((Activity) context).getResources().getColor(R.color.colorDefault));
            }
        }

        return view;
    }
}