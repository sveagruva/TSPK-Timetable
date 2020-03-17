package com.example.tspktimetable;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

public class help extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        if (getSupportActionBar() != null) { getSupportActionBar().hide(); }
    }

    public void goGit(View view) {
//        Intent browser = Intent.makeMainSelectorActivity( Intent.ACTION_VIEW, Intent.CATEGORY_APP_BROWSER);
//        startActivity(browser);
        Intent browseIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/sveagruva/TSPK-Timetable"));
        startActivity(browseIntent);
    }
}
