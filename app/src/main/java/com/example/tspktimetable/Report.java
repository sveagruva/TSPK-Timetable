package com.example.tspktimetable;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.example.tspktimetable.MainActivity.FindAllSubStrings;
import static com.example.tspktimetable.MainActivity.GetLengthOfTable;
import static com.example.tspktimetable.MainActivity.IsItNameOfGroup;
import static com.example.tspktimetable.MainActivity.ItItLessons;
import static com.example.tspktimetable.MainActivity.findStart;
import static com.example.tspktimetable.MainActivity.getMaxHeight;

public class Report extends AppCompatActivity {

    private Spinner spingr;
    private Spinner spinteah;
    private EditText editText;
    private ArrayList<String> ForSpinnerGr = new ArrayList<String>();
    private ArrayList<String> ForSpinnerTeach = new ArrayList<String>();

    private static ArrayList<String> Groups = new ArrayList<String>();
    private static ArrayList<String> Lessons = new ArrayList<String>();


    int id ;
    String timetable;
    ArrayList<String> LessonsFor = new ArrayList<String>();
    List<Integer> Endes;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        if (getSupportActionBar() != null) { getSupportActionBar().hide(); }
        editText = findViewById(R.id.editText);
        fillSpinners();
    }

    //binded on click
    public void PleaseWork(View view) {
        Button btn = (Button) view;
        btn.setEnabled(false);
        String gr = spingr.getItemAtPosition(spingr.getSelectedItemPosition()).toString();
        String tc = spinteah.getItemAtPosition(spinteah.getSelectedItemPosition()).toString().toLowerCase();
        String trail = editText.getText().toString().toLowerCase();

        if(tc.equals(getString(R.string.TapForChoose).toLowerCase())){
            tc = " ";
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        int days = 0;
        while(true){
            calendar.add(Calendar.DAY_OF_WEEK, -1);
            days++;
            if(calendar.get(Calendar.MONTH) == Calendar.SEPTEMBER && calendar.get(Calendar.DAY_OF_MONTH) == 1){
                break;
            }
            if(days > 1000){
                days = 0;
                break;
            }
        }

        LinearLayout haha = (LinearLayout) findViewById(R.id.LinearHAHA);
        int count = 0;
        TextView dayToDay;
        for(int i = 0; i < days; i++) {
            int year = ((calendar.get(Calendar.YEAR)) - (calendar.get(Calendar.YEAR) / 100) * 100);
            String month;
            String day;
            if (calendar.get(Calendar.DAY_OF_MONTH) - 9 > 0) {
                day = calendar.get(Calendar.DAY_OF_MONTH) + "";
            } else {
                day = "0" + calendar.get(Calendar.DAY_OF_MONTH);
            }
            if (calendar.get(Calendar.MONTH) - 9 >= 0) {
                month = (calendar.get(Calendar.MONTH) + 1) + "";
            } else {
                month = "0" + (calendar.get(Calendar.MONTH) + 1);
            }

            String data = day + "." + month + "." + year;
            calendar.add(Calendar.DAY_OF_WEEK, 1);
            MainActivity.download task = new MainActivity.download();
            File file = null;
            try {
                file = task.execute("http://tspk.org/images/raspisanie/" + data + ".xls", getCacheDir() + "/hi.xls").get();
            } catch (Exception ex){}


            Groups.clear();
            Lessons.clear();
            getArrays(file);
            file.delete();


            dayToDay = new TextView(Report.this);
            dayToDay.setText(data);
            dayToDay.setGravity(Gravity.CENTER);
            dayToDay.setTextSize(40);
            haha.addView(dayToDay);


            if(!gr.equals(getString(R.string.TapForChoose))){
                //все пары группы и вывести если есть следы

                try{
                    LessonsFor = MainActivity.StringToArray(Lessons.get(idOfGroup(gr)), trail.toLowerCase(), tc.toLowerCase(), gr);
                    for(int i1 = 0; i1 < LessonsFor.size(); i1++){
                        count++;
                        dayToDay = new TextView(Report.this);
                        dayToDay.setText(MainActivity.SpaceInADust(LessonsFor.get(i1)));
                        dayToDay.setGravity(Gravity.CENTER);
                        dayToDay.setTextSize(15);
                        haha.addView(dayToDay);
                    }
                }catch (Exception ex){}
                LessonsFor.clear();

            }else{
                //for all groups

                for(int idk = 0; idk < Groups.size(); idk++){
                    try{
                        LessonsFor = MainActivity.StringToArray(Lessons.get(idk),tc,trail,  Groups.get(idk));
                        for(int i1 = 0; i1 < LessonsFor.size(); i1++){
                            count++;
                            dayToDay = new TextView(Report.this);
                            dayToDay.setText(MainActivity.SpaceInADust(LessonsFor.get(i1)));
                            dayToDay.setGravity(Gravity.CENTER);
                            dayToDay.setTextSize(15);
                            haha.addView(dayToDay);
                        }
                    }catch (Exception ex){}
                    LessonsFor.clear();
                }
            }
        }
        dayToDay = new TextView(Report.this);
        dayToDay.setText(getString(R.string.TotalTrack) + count + getString(R.string.ForTrack) + days + getString(R.string.Days));
        dayToDay.setGravity(Gravity.LEFT);
        dayToDay.setTextSize(30);
        haha.addView(dayToDay);
    }

    private static int idOfGroup(String Group){
        for(int i = 0; i < Groups.size(); i++){
            if(Groups.get(i).equals(Group)){
                return i;
            }
        }
        return -1;
    }

    private void fillSpinners(){
        spingr = findViewById(R.id.spinnerGroupsGetRep);
        spinteah = findViewById(R.id.spinnerTeachersGetRep);

        ForSpinnerGr.clear();
        ForSpinnerTeach.clear();

        ForSpinnerGr.add(getString(R.string.TapForChoose));
        ForSpinnerGr.addAll(MainActivity.SGroup);
        ForSpinnerTeach.add(getString(R.string.TapForChoose));
        ForSpinnerTeach.addAll(MainActivity.STeachers);


        ArrayAdapter<String> adp1 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, ForSpinnerGr);
        adp1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spingr.setAdapter(adp1);

        ArrayAdapter<String> adp2 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, ForSpinnerTeach);
        adp2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinteah.setAdapter(adp2);
    }

    public void finish(View view) {
        finish();
    }

    private void getArrays(File file){
        if(file != null) {
            try {
                HSSFWorkbook ExcelBook = new HSSFWorkbook(new FileInputStream(file));
                HSSFSheet ExcelSheet = ExcelBook.getSheet("Table 2");
                int maxLength = GetLengthOfTable(ExcelSheet);
                int maxHeight = getMaxHeight(ExcelSheet, 0);
                String lesson;
                int GoRow = findStart(ExcelSheet);
                if (GoRow != -1) {
                    HSSFRow row;
                    StringBuilder Lesson;
                    while (GoRow != maxHeight) {
                        row = ExcelSheet.getRow(GoRow);
                        if (row.getCell(0).getStringCellValue().toString().contains("Диспетчер")) {
                            break;
                        }

                        if (IsItNameOfGroup(row.getCell(1).getStringCellValue().toString())) {
                            int ItIsCorner = 1;
                            int GoDown;
                            HSSFRow row1;
                            for (int i = 0; i < maxLength / 2; i++) {
                                Groups.add(row.getCell(ItIsCorner).getStringCellValue());
                                GoDown = GoRow;
                                GoDown++;
                                row1 = ExcelSheet.getRow(GoDown);
                                Lesson = new StringBuilder();
                                while (GoDown != maxHeight - 1 && !(IsItNameOfGroup(row1.getCell(1).getStringCellValue()))) {
                                    if (ItItLessons(row1, ExcelSheet, GoDown)) {
                                        if (!row1.getCell(ItIsCorner + 1).getStringCellValue().equals("")) {
                                            //splitted cell
                                            Lesson.append("&&").append(row1.getCell(ItIsCorner).getStringCellValue()).append("$");
                                            Lesson.append("&&").append(row1.getCell(ItIsCorner + 1).getStringCellValue()).append("$");
                                        } else {
                                            Lesson.append(row1.getCell(ItIsCorner).getStringCellValue()).append("$");
                                        }
                                    }
                                    GoDown++;
                                    row1 = ExcelSheet.getRow(GoDown);
                                }

                                lesson = Lesson.toString();
                                try {
                                    while (lesson.substring(lesson.length() - 1).contains("$")) {
                                        lesson = lesson.substring(0, lesson.length() - 1);
                                    }
                                } catch (Exception ex) {

                                }

                                Lessons.add(lesson + "$");
                                ItIsCorner += 2;
                            }
                        }
                        GoRow++;
                    }
                }
                while (Groups.get(Groups.size() - 1).equals("")) {
                    Groups.remove(Groups.size() - 1);
                }
            } catch (Exception ex) {
            }
        }
    }
}
