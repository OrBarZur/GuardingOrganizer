package com.example.guardingorganizer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.joda.time.LocalDateTime;

import java.util.Collections;

public class DoneListActivity extends AppCompatActivity {

    KeepingList keepingList;
    Gson gson;

    RecyclerView recyclerViewKeepingHourDone;
    KeepingHourDoneAdapter keepingHourDoneAdapter;
    ItemTouchHelper itemTouchHelper;

    Button btnCopy, btnBack;

    Menu doneMenu;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor prefsEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_done_list);

        sharedPreferences = getSharedPreferences("keepingListPrefs", Context.MODE_PRIVATE);
        GsonBuilder builder = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer());
        gson = builder.create();

        keepingList = new KeepingList(new Platoon());

        keepingList.jsonToKeepingList(gson.fromJson(sharedPreferences.getString("keepingList", ""), KeepingList.class));

        recyclerViewKeepingHourDone = findViewById(R.id.RecyclerViewKeepingHourDone);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerViewKeepingHourDone.setLayoutManager(linearLayoutManager);
        keepingHourDoneAdapter = new KeepingHourDoneAdapter(this, keepingList.getKeepingHours(), keepingList);
        recyclerViewKeepingHourDone.setAdapter(keepingHourDoneAdapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerViewKeepingHourDone.getContext(),
                linearLayoutManager.getOrientation());
        recyclerViewKeepingHourDone.addItemDecoration(dividerItemDecoration);

        itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.START | ItemTouchHelper.END, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();

                Collections.swap(keepingList.getKeepingHours(), fromPosition, toPosition);

                LocalDateTime startTimeFrom = keepingList.getKeepingHours().get(fromPosition).getStartTime();
                LocalDateTime endTimeFrom = keepingList.getKeepingHours().get(fromPosition).getEndTime();

                keepingList.getKeepingHours().get(fromPosition).setStartTime(keepingList.getKeepingHours().get(toPosition).getStartTime());
                keepingList.getKeepingHours().get(fromPosition).setEndTime(keepingList.getKeepingHours().get(toPosition).getEndTime());

                keepingList.getKeepingHours().get(toPosition).setStartTime(startTimeFrom);
                keepingList.getKeepingHours().get(toPosition).setEndTime(endTimeFrom);

                int hourRatingFrom = keepingList.getKeepingHours().get(fromPosition).getHourRating();
                keepingList.getKeepingHours().get(fromPosition).setHourRating(keepingList.getKeepingHours().get(toPosition).getHourRating());
                keepingList.getKeepingHours().get(toPosition).setHourRating(hourRatingFrom);

                recyclerView.getAdapter().notifyItemMoved(fromPosition, toPosition);
                recyclerView.getAdapter().notifyItemChanged(fromPosition);
                recyclerView.getAdapter().notifyItemChanged(toPosition);

                prefsEditor = sharedPreferences.edit();
                prefsEditor.putString("keepingList", gson.toJson(keepingList));
                prefsEditor.apply();

                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

            }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);

//                keepingList.sortKeepingHoursByTime();
//                recyclerView.getAdapter().notifyDataSetChanged();
            }

        });

        btnCopy = findViewById(R.id.btnCopy);
        btnBack = findViewById(R.id.btnBack);

        btnCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("keepingList", keepingList.toStringKeepingHours());
                clipboard.setPrimaryClip(clip);

                if (keepingList.isEditable()) {
                    keepingList.setEditable(false);
                    Toast.makeText(DoneListActivity.this,"List is updated and copied to clipboard",Toast.LENGTH_SHORT).show();
                    doneMenu.getItem(0).setTitle("Edit");
                    keepingList.accept();
                    itemTouchHelper.attachToRecyclerView(null);

                    prefsEditor = sharedPreferences.edit();
                    prefsEditor.putString("keepingList", gson.toJson(keepingList));
                    prefsEditor.apply();
                }
                else
                    Toast.makeText(DoneListActivity.this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (keepingList.isEditable()) {
                    keepingList.setEditable(false);
                    Toast.makeText(DoneListActivity.this,"List is updated",Toast.LENGTH_SHORT).show();
                    doneMenu.getItem(0).setTitle("Edit");
                    keepingList.accept();
                    itemTouchHelper.attachToRecyclerView(null);
                }

                prefsEditor = sharedPreferences.edit();
                prefsEditor.putString("keepingList", gson.toJson(keepingList));
                prefsEditor.apply();

                startActivity(new Intent(DoneListActivity.this, MainActivity.class));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.done_menu, menu);
        doneMenu = menu;

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:
                if (item.getTitle().equals("Edit")) {
                    keepingList.setEditable(true);
                    Toast.makeText(DoneListActivity.this,"List is editable",Toast.LENGTH_SHORT).show();
                    item.setTitle("Update");
                    keepingList.removePoints();
                    itemTouchHelper.attachToRecyclerView(recyclerViewKeepingHourDone);

                    prefsEditor = sharedPreferences.edit();
                    prefsEditor.putString("keepingList", gson.toJson(keepingList));
                    prefsEditor.apply();
                }

                else if (item.getTitle().equals("Update")) {
                    keepingList.setEditable(false);
                    Toast.makeText(DoneListActivity.this,"List is updated",Toast.LENGTH_SHORT).show();
                    item.setTitle("Edit");
                    keepingList.accept();
                    itemTouchHelper.attachToRecyclerView(null);

                    prefsEditor = sharedPreferences.edit();
                    prefsEditor.putString("keepingList", gson.toJson(keepingList));
                    prefsEditor.apply();
                }

                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (keepingList.isEditable()) {
            keepingList.setEditable(false);
            doneMenu.getItem(0).setTitle("Edit");
            keepingList.accept();
            itemTouchHelper.attachToRecyclerView(null);
        }

        prefsEditor = sharedPreferences.edit();
        prefsEditor.putString("keepingList", gson.toJson(keepingList));
        prefsEditor.apply();
        startActivity(new Intent(DoneListActivity.this, MainActivity.class));
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (keepingList.isEditable()) {
            keepingList.setEditable(false);
            doneMenu.getItem(0).setTitle("Edit");
            keepingList.accept();
            itemTouchHelper.attachToRecyclerView(null);
        }

        prefsEditor = sharedPreferences.edit();
        prefsEditor.putString("keepingList", gson.toJson(keepingList));
        prefsEditor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        keepingList.jsonToKeepingList(gson.fromJson(sharedPreferences.getString("keepingList", ""), KeepingList.class));
        if (keepingList == null) {
            keepingList = new KeepingList(new Platoon());
            prefsEditor = sharedPreferences.edit();
            prefsEditor.putString("keepingList", gson.toJson(keepingList));
            prefsEditor.apply();
        }
    }
}
