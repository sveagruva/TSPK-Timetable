package com.example.tspktimetable;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ChooseGroup extends AppCompatActivity {

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
        ForSpinner.add("Нажмите для выбора группы");
        ForSpinner.addAll(hui);
        spin = findViewById(R.id.spinner);
        ArrayAdapter<String> adp1 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, ForSpinner);
        adp1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(adp1);
        spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if(!(ForSpinner.get(position).equals("Нажмите для выбора группы"))){
                    SharedPreferences sPref = getSharedPreferences("Group", MODE_PRIVATE);
                    SharedPreferences.Editor ed = sPref.edit();
                    ed.putString("Group", ForSpinner.get(position));
                    ed.commit();
                    MainActivity.currentGroup = ForSpinner.get(position);
                    finish();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
    }
}
