package com.example.guardingorganizer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.joda.time.LocalDateTime;

public class HoursRatingActivity extends AppCompatActivity {

    KeepingList keepingList;
    Gson gson;

    ListView listViewKeepingHoursRating;
    KeepingHoursRatingAdapter keepingHoursRatingAdapter;

    Button btnCreate;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor prefsEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hours_rating);

        sharedPreferences = getSharedPreferences("keepingListPrefs", Context.MODE_PRIVATE);
        GsonBuilder builder = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer());
        gson = builder.create();

        keepingList = new KeepingList(new Platoon());

        keepingList.jsonToKeepingList(gson.fromJson(sharedPreferences.getString("keepingList", ""), KeepingList.class));

        keepingHoursRatingAdapter = new KeepingHoursRatingAdapter(this, 0, 0, keepingList.getKeepingHours(), keepingList);

        listViewKeepingHoursRating = findViewById(R.id.listViewKeepingHourRating);
        listViewKeepingHoursRating.setAdapter(keepingHoursRatingAdapter);

        btnCreate = findViewById(R.id.btnCreate);
        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                keepingList.fillPersonsToKeepingHours();
                keepingList.accept();

                keepingList.sortKeepingHoursByTime();

                prefsEditor = sharedPreferences.edit();
                prefsEditor.putString("keepingList", gson.toJson(keepingList));
                prefsEditor.apply();
                startActivity(new Intent(HoursRatingActivity.this, DoneListActivity.class));
            }
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(HoursRatingActivity.this, MainActivity.class));
    }

    @Override
    protected void onPause() {
        super.onPause();
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
