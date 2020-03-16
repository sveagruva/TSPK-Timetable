package com.example.tspktimetable;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;

import java.util.ArrayList;

public class ChooseGroup extends AppCompatActivity {

    private Switch dark;
    private Switch correct;
    private Spinner spingr;
    private Spinner spinteah;
    private Spinner spinroom;
    private ArrayList<String> ForSpinnerGr = new ArrayList<String>();
    private ArrayList<String> ForSpinnerTeach = new ArrayList<String>();
    private ArrayList<String> ForSpinnerRoom = new ArrayList<String>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_group);
        if (getSupportActionBar() != null) { getSupportActionBar().hide(); }

        Memory();

        spingr = findViewById(R.id.spinnerGr);
        spinteah = findViewById(R.id.spinnerTeach);
        spinroom = findViewById(R.id.spinnerRoom);

        ForSpinnerGr.clear();
        ForSpinnerRoom.clear();
        ForSpinnerTeach.clear();

        ForSpinnerGr.add(getString(R.string.TapForChoose));
        ForSpinnerGr.addAll(MainActivity.SGroup);
        ForSpinnerTeach.add(getString(R.string.TapForChoose));
        ForSpinnerTeach.addAll(MainActivity.STeachers);
        ForSpinnerRoom.add(getString(R.string.TapForChoose));
        ForSpinnerRoom.addAll(MainActivity.SRooms);


        ArrayAdapter<String> adp1 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, ForSpinnerGr);
        adp1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spingr.setAdapter(adp1);

        ArrayAdapter<String> adp2 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, ForSpinnerTeach);
        adp2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinteah.setAdapter(adp2);

        ArrayAdapter<String> adp3 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, ForSpinnerRoom);
        adp3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinroom.setAdapter(adp3);


        spingr.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if(!(ForSpinnerGr.get(position).equals(getString(R.string.TapForChoose)))){
                    SharedPreferences.Editor ed = getSharedPreferences("Group", MODE_PRIVATE).edit();
                    ed.putString("Value", ForSpinnerGr.get(position));
                    ed.putString("Type", "Group");
                    ed.apply();
                    MainActivity.current = ForSpinnerGr.get(position);
                    finish();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        spinteah.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if(!(ForSpinnerTeach.get(position).equals(getString(R.string.TapForChoose)))){
                    SharedPreferences.Editor ed = getSharedPreferences("Group", MODE_PRIVATE).edit();
                    ed.putString("Value", ForSpinnerTeach.get(position));
                    ed.putString("Type", "Teacher");
                    ed.apply();
                    MainActivity.current = ForSpinnerTeach.get(position);
                    finish();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        spinroom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if(!(ForSpinnerRoom.get(position).equals(getString(R.string.TapForChoose)))){
                    SharedPreferences.Editor ed = getSharedPreferences("Group", MODE_PRIVATE).edit();
                    ed.putString("Value", ForSpinnerRoom.get(position));
                    ed.putString("Type", "Room");
                    ed.apply();
                    MainActivity.current = ForSpinnerRoom.get(position);
                    finish();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

    }

    private void Memory() {
        SharedPreferences Switches = getSharedPreferences("Switches", MODE_PRIVATE);
        dark = findViewById(R.id.darkmode);
        correct = findViewById(R.id.correct);
        dark.setChecked(Switches.getBoolean("dark", false));
        correct.setChecked(Switches.getBoolean("correct", false));
    }


    public void Switches(View view) {
        SharedPreferences.Editor Switches = getSharedPreferences("Switches", MODE_PRIVATE).edit();
        if(view == findViewById(R.id.darkmode)){
            Switches.putBoolean("dark", dark.isChecked());
            MainActivity.NeedRecreate = true;
            if(dark.isChecked()){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }else{
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            }
        }
        if(view == findViewById(R.id.correct)){
            Switches.putBoolean("correct", correct.isChecked());
        }
        Switches.apply();
    }

    public void GetReport(View view) {
        startActivity(new Intent(getApplicationContext(), Report.class));
    }

    public void finish(View view) {
        finish();
    }
}
