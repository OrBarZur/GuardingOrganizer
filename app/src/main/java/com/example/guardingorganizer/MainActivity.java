package com.example.guardingorganizer;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.joda.time.LocalDateTime;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    EditText editTextAddNames, editTextStartTime, editTextEndTime;
    Button btnAdd, btnNewTable;
    ListView listViewPersons;
    PersonAdapter personAdapter;

    TimePickerDialog timePickerDialogStart, timePickerDialogEnd;

    Spinner spinnerDuration;
    ArrayList<String> durationOptions;
    ArrayAdapter arrayAdapterSpinner;

    Switch switchRandom, switchNextDay;

    Person personClicked;
    KeepingList keepingList;

    SharedPreferences sharedPreferences;
    Gson gson;
    SharedPreferences.Editor prefsEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("keepingListPrefs", Context.MODE_PRIVATE);

        GsonBuilder builder = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer());
        gson = builder.create();

        KeepingList jsonKeepingList = gson.fromJson(sharedPreferences.getString("keepingList", ""), KeepingList.class);

        keepingList = new KeepingList(new Platoon());

        if (jsonKeepingList == null) {
            prefsEditor = sharedPreferences.edit();
            prefsEditor.putString("keepingList", gson.toJson(keepingList));
            prefsEditor.apply();
        }
        else
            keepingList.jsonToKeepingList(jsonKeepingList);

        editTextAddNames = findViewById(R.id.editTextAddNames);
        btnAdd = findViewById(R.id.btnAdd);
        btnNewTable = findViewById(R.id.btnNewTable);

        editTextStartTime = findViewById(R.id.editTextStartTime);
        editTextEndTime = findViewById(R.id.editTextEndTime);

        spinnerDuration = findViewById(R.id.spinnerDuration);
        durationOptions = new ArrayList<>();

        switchRandom = findViewById(R.id.switchRandom);
        switchNextDay = findViewById(R.id.switchNextDay);

        listViewPersons = findViewById(R.id.listViewPersons);

        personAdapter = new PersonAdapter(this, 0, 0, keepingList.getPlatoon().getPersons(), keepingList);

        listViewPersons.setAdapter(personAdapter);

        btnNewTable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (keepingList.getStartTime() == null ||
                        keepingList.getEndTime() == null) {
                    Toast.makeText(MainActivity.this, "There are some missing data", Toast.LENGTH_SHORT).show();
                    return;
                }
                int minDuration = keepingList.getMinDuration();
                if (keepingList.getPotentialKeepers().size() == 0 ||
                        (!keepingList.isAutoDuration() && keepingList.getDuration() == 0) ||
                        minDuration == 0) {
                    Toast.makeText(MainActivity.this, "There are some missing data", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!keepingList.getStartTime().isBefore(keepingList.getEndTime())) {
                    Toast.makeText(MainActivity.this, "Make sure the end time is after the start time", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!keepingList.isAutoDuration() && keepingList.getDuration() < minDuration) {
                    Toast.makeText(MainActivity.this, "Duration is too low for this amount of people", Toast.LENGTH_SHORT).show();
                    if (minDuration > durationOptions.size() - 1)
                        return;

                    spinnerDuration.setSelection(minDuration);
                    keepingList.setDuration(minDuration);
                    return;
                }

                if (keepingList.isAutoDuration()) {
                    if (minDuration > durationOptions.size() - 1) {
                        Toast.makeText(MainActivity.this, "Maximum duration is " + durationOptions.size() + ". Please add more people to keep", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    keepingList.setDuration(minDuration);
                }

                keepingList.create();

                prefsEditor = sharedPreferences.edit();
                prefsEditor.putString("keepingList", gson.toJson(keepingList));
                prefsEditor.apply();

                if (keepingList.isRandom()) {
                    keepingList.fillPersonsToKeepingHours();
                    startActivity(new Intent(MainActivity.this, DoneListActivity.class));
                }

                else
                    startActivity(new Intent(MainActivity.this, HoursRatingActivity.class));

            }
        });

        switchRandom.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                keepingList.setRandom(isChecked);
            }
        });

        switchRandom.setChecked(keepingList.isRandom());

        switchNextDay.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                keepingList.setNextDay(isChecked);

                if (keepingList.getEndTime() == null) {
                    return;
                }

                if (!keepingList.isNextDay())
                    keepingList.setEndTime(keepingList.getEndTime().withDate(LocalDateTime.now().getYear(),
                            LocalDateTime.now().getMonthOfYear(),
                            LocalDateTime.now().getDayOfMonth()));
                else
                    keepingList.setEndTime(keepingList.getEndTime().withDate(LocalDateTime.now().plusDays(1).getYear(),
                            LocalDateTime.now().plusDays(1).getMonthOfYear(),
                            LocalDateTime.now().plusDays(1).getDayOfMonth()));
            }
        });

        switchNextDay.setChecked(keepingList.isNextDay());

        durationOptions.add("Automatic");
        for (int i = 1; i <= 120; i++) {
            durationOptions.add(String.valueOf(i));
        }

        arrayAdapterSpinner = new ArrayAdapter(this,android.R.layout.simple_spinner_item, durationOptions);
        arrayAdapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        spinnerDuration.setAdapter(arrayAdapterSpinner);

        spinnerDuration.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();

                if (item.equals("Automatic"))
                    keepingList.setAutoDuration(true);
                else {
                    keepingList.setAutoDuration(false);
                    keepingList.setDuration(Integer.valueOf(item));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        if (keepingList.isAutoDuration())
            spinnerDuration.setSelection(0);
        else
            spinnerDuration.setSelection(keepingList.getDuration());

        editTextStartTime.setInputType(InputType.TYPE_NULL);
        editTextStartTime.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus)
                    return;

                timePickerDialogStart = new TimePickerDialog(MainActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(android.widget.TimePicker timePicker, int hour, int minute) {
                                editTextStartTime.setText(String.format("%02d", hour) + ":" + String.format("%02d", minute));
                                keepingList.setStartTime(keepingList.getStartTime().withTime(hour, minute, 0, 0));
                                keepingList.setStartTime(keepingList.getStartTime().withDate(LocalDateTime.now().getYear(),
                                        LocalDateTime.now().getMonthOfYear(),
                                        LocalDateTime.now().getDayOfMonth()));
                            }
                        }, keepingList.getStartTime() == null ? LocalDateTime.now().getHourOfDay() : keepingList.getStartTime().getHourOfDay()
                        , keepingList.getStartTime() == null ? LocalDateTime.now().getMinuteOfHour() : keepingList.getStartTime().getMinuteOfHour(), true);

                timePickerDialogStart.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        editTextStartTime.setText(String.format("%02d", keepingList.getStartTime().getHourOfDay()) + ":" + String.format("%02d", keepingList.getStartTime().getMinuteOfHour()));
                    }
                });

                timePickerDialogStart.show();
                if (keepingList.getStartTime() == null)
                    keepingList.setStartTime(LocalDateTime.now());

                view.clearFocus();
            }
        });

        editTextEndTime.setInputType(InputType.TYPE_NULL);
        editTextEndTime.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus)
                    return;

                timePickerDialogEnd = new TimePickerDialog(MainActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(android.widget.TimePicker timePicker, int hour, int minute) {
                                editTextEndTime.setText(String.format("%02d", hour) + ":" + String.format("%02d", minute));
                                keepingList.setEndTime(keepingList.getEndTime().withTime(hour, minute, 0, 0));

                                if (!keepingList.isNextDay())
                                    keepingList.setEndTime(keepingList.getEndTime().withDate(LocalDateTime.now().getYear(),
                                            LocalDateTime.now().getMonthOfYear(),
                                            LocalDateTime.now().getDayOfMonth()));
                                else
                                    keepingList.setEndTime(keepingList.getEndTime().withDate(LocalDateTime.now().plusDays(1).getYear(),
                                            LocalDateTime.now().plusDays(1).getMonthOfYear(),
                                            LocalDateTime.now().plusDays(1).getDayOfMonth()));

                            }
                        }, keepingList.getEndTime() == null ? LocalDateTime.now().getHourOfDay() : keepingList.getEndTime().getHourOfDay()
                        , keepingList.getEndTime() == null ? LocalDateTime.now().getMinuteOfHour() : keepingList.getEndTime().getMinuteOfHour(), true);

                timePickerDialogEnd.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        editTextEndTime.setText(String.format("%02d", keepingList.getEndTime().getHourOfDay()) + ":" + String.format("%02d", keepingList.getEndTime().getMinuteOfHour()));
                    }
                });

                timePickerDialogEnd.show();
                if (keepingList.getEndTime() == null)
                    keepingList.setEndTime(LocalDateTime.now());

                view.clearFocus();
            }
        });

        if (keepingList.getStartTime() == null)
            editTextStartTime.setText("");

        else {
            LocalDateTime dt = keepingList.getStartTime();
            editTextStartTime.setText(String.format("%02d", dt.getHourOfDay()) + ":" + String.format("%02d", dt.getMinuteOfHour()));
        }

        if (keepingList.getEndTime() == null)
            editTextEndTime.setText("");

        else {
            LocalDateTime dt = keepingList.getEndTime();
            editTextEndTime.setText(String.format("%02d", dt.getHourOfDay()) + ":" + String.format("%02d", dt.getMinuteOfHour()));
        }

        editTextAddNames.setText("");

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String names = editTextAddNames.getText().toString();
                String[] lines = names.split("\\R");

                for (String name: lines) {
                    name = name.trim();

                    if (!(name.equals("") || keepingList.getPlatoon().hasName(name))) {
                        editTextAddNames.setText("");
                        keepingList.addPerson(new Person(name));
                        personAdapter.notifyDataSetChanged();
                    }
                }

                prefsEditor = sharedPreferences.edit();
                prefsEditor.putString("keepingList", gson.toJson(keepingList));
                prefsEditor.apply();
            }
        });

        listViewPersons.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                personClicked = personAdapter.getItem(position);

                if (!keepingList.isAbsent(personClicked))
                    keepingList.addAbsent(personClicked);
                else
                    keepingList.removeAbsent(personClicked);

                prefsEditor = sharedPreferences.edit();
                prefsEditor.putString("keepingList", gson.toJson(keepingList));
                prefsEditor.apply();

                personAdapter.notifyDataSetChanged();
            }
        });
        listViewPersons.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                personClicked = personAdapter.getItem(position);
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Warning!");
                builder.setMessage("Are you sure you want to delete it?");
                builder.setCancelable(true);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        Toast.makeText(MainActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                        keepingList.removePerson(personClicked);
                        personAdapter.notifyDataSetChanged();
                        dialog.dismiss();

                        prefsEditor = sharedPreferences.edit();
                        prefsEditor.putString("keepingList", gson.toJson(keepingList));
                        prefsEditor.apply();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        Toast.makeText(MainActivity.this,"Cancelled",Toast.LENGTH_SHORT).show();
                        personClicked = null;
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog=builder.create();
                dialog.show();
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_reset:
                keepingList.getPlatoon().resetValues();
                keepingList.emptyKeepingHours();
                keepingList.setAbsents(new ArrayList<Person>());

                prefsEditor = sharedPreferences.edit();
                prefsEditor.putString("keepingList", gson.toJson(keepingList));
                prefsEditor.apply();

                personAdapter.notifyDataSetChanged();

                break;


            case R.id.action_last_list:
                if (keepingList.getKeepingHours().isEmpty()) {
                    Toast.makeText(MainActivity.this, "There is no list", Toast.LENGTH_SHORT).show();
                }
                else {
                    prefsEditor = sharedPreferences.edit();
                    prefsEditor.putString("keepingList", gson.toJson(keepingList));
                    prefsEditor.apply();

                    startActivity(new Intent(MainActivity.this, DoneListActivity.class));
                }

                break;

            case R.id.action_check_last:
                for (KeepingHour kh: keepingList.getKeepingHours()) {
                    if (kh.getPerson() != null && !keepingList.isAbsent(keepingList.getPlatoon().getPersonByName(kh.getPerson().getName())))
                        keepingList.addAbsent(keepingList.getPlatoon().getPersonByName(kh.getPerson().getName()));
                }

                prefsEditor = sharedPreferences.edit();
                prefsEditor.putString("keepingList", gson.toJson(keepingList));
                prefsEditor.apply();

                personAdapter.notifyDataSetChanged();

                break;

            case R.id.action_uncheck_last:
                for (KeepingHour kh: keepingList.getKeepingHours()) {
                    if (kh.getPerson() != null && keepingList.isAbsent(keepingList.getPlatoon().getPersonByName(kh.getPerson().getName())))
                        keepingList.removeAbsent(keepingList.getPlatoon().getPersonByName(kh.getPerson().getName()));
                }

                prefsEditor = sharedPreferences.edit();
                prefsEditor.putString("keepingList", gson.toJson(keepingList));
                prefsEditor.apply();

                personAdapter.notifyDataSetChanged();

                break;

            case R.id.action_uncheck_all:
                keepingList.setAbsents(new ArrayList<Person>());

                prefsEditor = sharedPreferences.edit();
                prefsEditor.putString("keepingList", gson.toJson(keepingList));
                prefsEditor.apply();

                personAdapter.notifyDataSetChanged();

                break;

            case R.id.action_delete_all:
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Warning!");
                builder.setMessage("Are you sure you want to delete all items?");
                builder.setCancelable(true);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        Toast.makeText(MainActivity.this, "All Deleted", Toast.LENGTH_SHORT).show();
                        keepingList = new KeepingList(new Platoon());
                        switchRandom.setChecked(keepingList.isRandom());
                        switchNextDay.setChecked(keepingList.isNextDay());
                        editTextStartTime.setText("");
                        editTextEndTime.setText("");
                        spinnerDuration.setSelection(0);
                        editTextAddNames.setText("");

                        personAdapter = new PersonAdapter(MainActivity.this, 0, 0, keepingList.getPlatoon().getPersons(), keepingList);

                        listViewPersons.setAdapter(personAdapter);

                        prefsEditor = sharedPreferences.edit();
                        prefsEditor.putString("keepingList", gson.toJson(keepingList));
                        prefsEditor.apply();

                        personAdapter.notifyDataSetChanged();
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        Toast.makeText(MainActivity.this,"Cancelled",Toast.LENGTH_SHORT).show();
                        personClicked = null;
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        prefsEditor = sharedPreferences.edit();
        prefsEditor.putString("keepingList", gson.toJson(keepingList));
        prefsEditor.apply();
        finish();
        System.exit(0);
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
