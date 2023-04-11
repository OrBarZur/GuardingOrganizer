package com.example.keepingorganizer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KeepingHourDoneAdapter extends RecyclerView.Adapter<KeepingHourDoneAdapter.ViewHolder> {

    Context context;
    List<KeepingHour> data;
    KeepingList keepingList;

    // data is passed into the constructor
    public KeepingHourDoneAdapter(Context context, List<KeepingHour> data, KeepingList keepingList) {
        this.context = context;
        this.data = data;
        this.keepingList = keepingList;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = ((Activity)context).getLayoutInflater().inflate(R.layout.keeping_hour_done_layout, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final TextView tvName = holder.itemView.findViewById(R.id.tvName);
        TextView tvStartTime = holder.itemView.findViewById(R.id.tvStartTime);
        TextView tvEndTime = holder.itemView.findViewById(R.id.tvEndTime);

        KeepingHour keepingHour = data.get(position);

        tvName.setText(keepingHour.getPerson().getName());
        tvStartTime.setText(String.format("%02d", keepingHour.getStartTime().getHourOfDay()) + ":" + String.format("%02d", keepingHour.getStartTime().getMinuteOfHour()));
        tvEndTime.setText(String.format("%02d", keepingHour.getEndTime().getHourOfDay()) + ":" + String.format("%02d", keepingHour.getEndTime().getMinuteOfHour()));

        tvName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!keepingList.isEditable())
                    return;

                //popup menu
                PopupMenu popupMenu = new PopupMenu(context, view);

                //add menu items in popup menu
                for (Person person : keepingList.getPlatoon().getPersons())
                    if (!keepingList.isKeeping(person)) {
                        SpannableString name = new SpannableString(person.getName() + " - " + person.getSumValues());

                        if (keepingList.getAbsents().contains(person)) {
                            name.setSpan(new StrikethroughSpan(), 0, name.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
                            name.setSpan(new ForegroundColorSpan(((Activity) context).getResources().getColor(R.color.colorAbsents)), 0, name.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                        else {
                            name.setSpan(new ForegroundColorSpan(((Activity) context).getResources().getColor(R.color.colorDefault)), 0, name.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }

                        popupMenu.getMenu().add(name);
                    }

                //handle menu item clicks
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        //handle clicks
                        String item = menuItem.getTitle().toString();
                        item = item.substring(0, item.length() - 4);
                        Person person = keepingList.getPlatoon().getPersonByName(item);
                        keepingList.getKeepingHours().get(position).setPerson(person);
                        tvName.setText(keepingList.getKeepingHours().get(position).getPerson().getName());

                        return false;
                    }
                });

                popupMenu.show();
            }
        });
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return data.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {

        ViewHolder(View itemView) {
            super(itemView);
        }
    }

    // convenience method for getting data at click position
    KeepingHour getItem(int id) {
        return data.get(id);
    }
}