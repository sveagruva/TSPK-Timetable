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
    private Spinner spin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_group);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        final Intent intent = getIntent();
        final ArrayList<String> hui = intent.getStringArrayListExtra("arrayGroups");
        final ArrayList<String> ForSpinner = new ArrayList<String>();
        ForSpinner.clear();
        ForSpinner.add(getString(R.string.TapForChoose));
        ForSpinner.addAll(hui);
        spin = findViewById(R.id.spinner);
        ArrayAdapter<String> adp1 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, ForSpinner);
        adp1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(adp1);
        spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if(!(ForSpinner.get(position).equals(getString(R.string.TapForChoose)))){
                    SharedPreferences sPref = getSharedPreferences("Group", MODE_PRIVATE);
                    SharedPreferences.Editor ed = sPref.edit();
                    ed.putString("Group", ForSpinner.get(position));
                    ed.apply();
                    MainActivity.currentGroup = ForSpinner.get(position);
                    finish();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
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
}
