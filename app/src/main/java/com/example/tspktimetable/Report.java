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

public class Report extends AppCompatActivity {

    private Spinner spingr;
    private Spinner spinteah;
    private EditText editText;
    private ArrayList<String> ForSpinnerGr = new ArrayList<String>();
    private ArrayList<String> ForSpinnerTeach = new ArrayList<String>();

    private static ArrayList<String> Groups = new ArrayList<String>();
    private static ArrayList<String> Lessons = new ArrayList<String>();
    private static int MaxLength;
    private static int MaxHeight;

    private TextView DayToDay;
    private LinearLayout haha;


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
            tc = ":";
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

        haha = (LinearLayout) findViewById(R.id.LinearHAHA);
        int count = 0;
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


            DayToDay = new TextView(Report.this);
            DayToDay.setText(data);
            DayToDay.setGravity(Gravity.CENTER);
            DayToDay.setTextSize(40);
            haha.addView(DayToDay);


            if(!gr.equals(getString(R.string.TapForChoose))){
                try{
                    id = idOfGroup(gr);
                    try{
                        timetable = "";
                        timetable = Lessons.get(id);
                        LessonsFor.clear();
                        Endes.clear();
                    }catch (Exception ex){}
                    Endes = FindAllSubStrings(timetable,"$");
                    try{
                        if(!timetable.substring(0, Endes.get(0)).contains("&&")){
                            LessonsFor.add(1 + ":  " + timetable.substring(0, Endes.get(0)));
                        }else{
                            LessonsFor.add(1 + ":  " + timetable.substring(2, Endes.get(0)) + " / " + timetable.substring(Endes.get(0)+3, Endes.get(1)));
                            Endes.remove(0);
                        }
                        try{
                            for(int i1 = 1; i1 < 6; i1++){
                                if(!timetable.substring(Endes.get(0), Endes.get(1)).contains("&&")){
                                    LessonsFor.add(i1+1 + ":  " + timetable.substring(Endes.get(0) +1, Endes.get(1)));
                                    Endes.remove(0);
                                }else{
                                    LessonsFor.add(i1+1 + ":  " + timetable.substring(Endes.get(0) +3, Endes.get(1)) + " / " + timetable.substring(Endes.get(1)+3, Endes.get(2)));
                                    Endes.remove(0);
                                    Endes.remove(0);
                                }
                            }
                        }catch (Exception ex){ }
                    }catch (Exception ex){ }
                }catch (Exception ex){}
                Log.i("lessons", LessonsFor.size()  + " size, groups: " + Groups.size() + " timetable: " + timetable + " lessons: " + Lessons.size() + " " +  id );

                try{
                    for(int i1 = 0; i1 < LessonsFor.size(); i1++){
                        if(LessonsFor.get(i1).toLowerCase().contains(tc) && LessonsFor.get(i1).toLowerCase().contains(trail)) {
                            count++;
                            DayToDay = new TextView(Report.this);
                            DayToDay.setText(MainActivity.SpaceInADust(LessonsFor.get(i1)));
                            DayToDay.setGravity(Gravity.CENTER);
                            DayToDay.setTextSize(15);
                            haha.addView(DayToDay);
                        }
                    }

                }catch (Exception ex){}
                LessonsFor.clear();
            }else{
                //for all groups




                for(int idk = 0; idk < Groups.size(); idk++){
                    try{
                        try{
                            timetable = "";
                            timetable = Lessons.get(idk);
                            LessonsFor.clear();
                            Endes.clear();
                        }catch (Exception ex){}
                        Endes = FindAllSubStrings(timetable,"$");
                        try{
                            if(!timetable.substring(0, Endes.get(0)).contains("&&")){
                                LessonsFor.add(1 + ":  " + timetable.substring(0, Endes.get(0)));
                            }else{
                                LessonsFor.add(1 + ":  " + timetable.substring(2, Endes.get(0)) + " / " + timetable.substring(Endes.get(0)+3, Endes.get(1)));
                                Endes.remove(0);
                            }
                            try{
                                for(int i1 = 1; i1 < 6; i1++){
                                    if(!timetable.substring(Endes.get(0), Endes.get(1)).contains("&&")){
                                        LessonsFor.add(i1+1 + ":  " + timetable.substring(Endes.get(0) +1, Endes.get(1)));
                                        Endes.remove(0);
                                    }else{
                                        LessonsFor.add(i1+1 + ":  " + timetable.substring(Endes.get(0) +3, Endes.get(1)) + " / " + timetable.substring(Endes.get(1)+3, Endes.get(2)));
                                        Endes.remove(0);
                                        Endes.remove(0);
                                    }
                                }
                            }catch (Exception ex){ }
                        }catch (Exception ex){ }
                    }catch (Exception ex){}
                    //Log.i("lessons", LessonsFor.size()  + " size, groups: " + Groups.size() + " timetable: " + timetable + " lessons: " + Lessons.size() + " " +  id );

                    try{
                        for(int i1 = 0; i1 < LessonsFor.size(); i1++){
                            if(LessonsFor.get(i1).toLowerCase().contains(tc) && LessonsFor.get(i1).toLowerCase().contains(trail)) {
                                count++;
                                DayToDay = new TextView(Report.this);
                                DayToDay.setText(Groups.get(idk) + " " + MainActivity.SpaceInADust(LessonsFor.get(i1)));
                                DayToDay.setGravity(Gravity.CENTER);
                                DayToDay.setTextSize(15);
                                haha.addView(DayToDay);
                            }
                        }

                    }catch (Exception ex){}
                    LessonsFor.clear();
                }
            }
        }
        DayToDay = new TextView(Report.this);
        DayToDay.setText(getString(R.string.TotalTrack) + count + getString(R.string.ForTrack) + days + getString(R.string.Days));
        DayToDay.setGravity(Gravity.LEFT);
        DayToDay.setTextSize(30);
        haha.addView(DayToDay);
    }

    private static int idOfGroup(String Group){
        for(int i = 0; i < Groups.size(); i++){
            if(Groups.get(i).equals(Group)){
                return i;
            }
        }
        return -1;
    }

    private static List<Integer> FindAllSubStrings(String main, String small){
        int lastIndex = 0;
        List<Integer> result = new ArrayList<Integer>();
        while(lastIndex != -1) {
            lastIndex = main.indexOf(small,lastIndex);
            if(lastIndex != -1){
                result.add(lastIndex);
                lastIndex += 1;
            }
        }
        return result;
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
                MaxLength = GetLengthOfTable(ExcelSheet);
                MaxHeight = getMaxHeight(ExcelSheet, 0);
                String lesson;
                int GoRow = findStart(ExcelSheet);
                if (GoRow != -1) {
                    HSSFRow row;
                    StringBuilder Lesson;
                    while (GoRow != MaxHeight) {
                        row = ExcelSheet.getRow(GoRow);
                        if (row.getCell(0).getStringCellValue().toString().contains("Диспетчер")) {
                            break;
                        }

                        if (IsItNameOfGroup(row.getCell(1).getStringCellValue().toString())) {
                            int ItIsCorner = 1;
                            int GoDown;
                            HSSFRow row1;
                            for (int i = 0; i < MaxLength / 2; i++) {
                                Groups.add(row.getCell(ItIsCorner).getStringCellValue());
                                GoDown = GoRow;
                                GoDown++;
                                row1 = ExcelSheet.getRow(GoDown);
                                Lesson = new StringBuilder();
                                while (GoDown != MaxHeight - 1 && !(IsItNameOfGroup(row1.getCell(1).getStringCellValue()))) {
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

    // СДЕЛАНО САНЕЙ    CREATED BY SYANYA
    private static Boolean IsItNameOfGroup(String maybeName){
        if ((maybeName.length() - 1 - maybeName.indexOf('-')) == 2) {
            return true;
        } else {
            return false;
        }
    }

    private static Boolean ItItLessons(HSSFRow row, HSSFSheet sheet, int GoDown){
        if((row.getCell(0).getStringCellValue().contains("з") || row.getCell(0).getStringCellValue().contains("у")) && !row.getCell(0).getStringCellValue().equals(sheet.getRow(GoDown+1).getCell(0)) && row.getHeight() > 50){
            return true;
        }else{
            return false;
        }
    }

    private static int getMaxHeight(HSSFSheet sheet, int cell){
        int len = 0;
        try{
            for(int i = 0; i < 1000; i++){
                String hi = sheet.getRow(i).getCell(cell).getStringCellValue().toString();
                len = i;
            }
        }catch (Exception ignored){

        }
        return len;
    }

    private static int GetLengthOfTable(HSSFSheet sheet){
        int len = 0;
        HSSFRow row = sheet.getRow(0);
        try{
            for(int i = 0; i < 1000; i++){
                Cell cell = row.getCell(i);
                String hi = cell.getStringCellValue();
                len = i;
            }
        }catch (Exception ignored){

        }
        len++;
        return len;
    }

    private static int findStart(HSSFSheet ExcelSheet){
        HSSFRow row;
        try {
            for(int i = 0; i < 100; i++) {
                row = ExcelSheet.getRow(i);
                String name = row.getCell(0).getStringCellValue();
                String name1 = "Пара";
                if (name.equals("Пара")) {
                    return i;
                }
            }
        } catch (Exception ex) {
            Log.i("hi",  "строка пары не найдена:" + ex);
            return -1;
        }
        return -1;
    }
}
