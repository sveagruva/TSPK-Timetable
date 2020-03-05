package com.example.tspktimetable;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private static TextView TLesson1;
    private static TextView TLesson2;
    private static TextView TLesson3;
    private static TextView TLesson4;
    private static TextView TLesson5;
    private static TextView TLesson6;
    private static TextView TLesson7;
    private static TextView Choose;
    private static Spinner spinner;

    private static ArrayList<String> Groups = new ArrayList<String>();
    private static ArrayList<String> Lessons = new ArrayList<String>();
    private static int MaxLength;
    private static int MaxHeight;
    private static String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InizializateLessonsViews();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        download task = new download();
        try {
            url = "http://tspk.org/images/raspisanie/03.03.20.xls";
            File file = task.execute(url, getCacheDir() + "/hi.xls").get();
            Log.i("logs", file.getAbsolutePath() + " " + file.length());
            getArrays(file);
            file.delete();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        SharedPreferences sPref = getSharedPreferences("Group", MODE_PRIVATE);
        String currentGroup = sPref.getString("Group", "0");
        if(currentGroup.equals("0")){
            Intent intent = new Intent(getApplicationContext(), ChooseGroup.class);
            intent.putExtra("arrayGroups", Groups);
            startActivity(intent);
        }
    }

//    private void saveText() {
//        SharedPreferences sPref = getSharedPreferences("MyPref", MODE_PRIVATE);
//        SharedPreferences.Editor ed = sPref.edit();
//        ed.putString("sdafasd", "fsafasd");
//        ed.commit();
//    }
//
//    private void loadText() {

//    }

    private static class download extends AsyncTask<String, Void, File>{

        @Override
        protected File doInBackground(String... strings) {
            URL url = null;
            HttpURLConnection urlConnection = null;
            File file = new File(strings[1]);
            try {
                url = new URL(strings[0]);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            try {
                FileUtils.copyURLToFile(url, file);
                Log.i("logs", "copyed");
            } catch (IOException e) {
                e.printStackTrace();
                Log.i("logs", "not copyed");
            }


            return file;
        }
    }

    private static void getArrays(File file) throws IOException {
        Log.i("hi", "Скачалось. Вес: " + file.length());
        HSSFWorkbook ExcelBook = new HSSFWorkbook(new FileInputStream(file));
        HSSFSheet ExcelSheet = ExcelBook.getSheet("Table 2");
        MaxLength = GetLengnhtOfTable(ExcelSheet);
        MaxHeight = getMaxHeight(ExcelSheet);
        Log.i("mx", MaxHeight + "");
        int GoRow = findStart(ExcelSheet);
        if(GoRow != -1){
            HSSFRow row = ExcelSheet.getRow(GoRow);
            String Lesson;
            while(GoRow != MaxHeight){
                row = ExcelSheet.getRow(GoRow);
                if((row.getCell(0).getStringCellValue().toString().indexOf("Диспетчер")) != -1){
                    break;
                }

                if(IsItNameOfGroup(row.getCell(1).getStringCellValue().toString())){
                    int ItIsCorner = 1;
                    int GoDown;
                    HSSFRow row1;
                    for(int i = 0; i < MaxLength/2; i++){
                        Groups.add(row.getCell(ItIsCorner).getStringCellValue());
                        Log.i("Les", row.getCell(ItIsCorner).getStringCellValue());
                        GoDown = GoRow;
                        GoDown++;
                        row1 = ExcelSheet.getRow(GoDown);
                        Lesson = "";
                        while(GoDown != MaxHeight-1 && !(IsItNameOfGroup(row1.getCell(1).getStringCellValue()))){
                            //read
                            if(ItItLessons(row1, ExcelSheet, GoDown)){
                                if(row1.getCell(ItIsCorner+1).getStringCellValue() != ""){
                                    //разделённые ячейки
                                    Lesson += ("&&" + row1.getCell(ItIsCorner).getStringCellValue() + "$");
                                    Lesson += ("&&" + row1.getCell(ItIsCorner+1).getStringCellValue() + "$");
                                }else{
                                    Lesson +=(row1.getCell(ItIsCorner).getStringCellValue() + "$");
                                }
                            }
                            GoDown++;
                            row1 = ExcelSheet.getRow(GoDown);
                        }
                        Lessons.add(Lesson);
                        ItIsCorner += 2;
                    }
                }
                GoRow++;
            }
        }
    }

    private static void ShowLessons(String Group){
        int id = idOfGroup(Group);
        String timetable = Lessons.get(id);
        List<Integer> Endes = FindAllSubStrings(timetable,"$");
        TLesson1.setText(timetable.substring(0, Endes.get(0)));
        TLesson2.setText(timetable.substring(Endes.get(0), Endes.get(1)));
        TLesson3.setText(timetable.substring(Endes.get(1), Endes.get(2)));
        TLesson4.setText(timetable.substring(Endes.get(2), Endes.get(3)));
        TLesson5.setText(timetable.substring(Endes.get(3), Endes.get(4)));
        TLesson6.setText(timetable.substring(Endes.get(4), Endes.get(5)));
        TLesson7.setText(timetable.substring(Endes.get(5), Endes.get(6)));
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

    private static Boolean ItItLessons(HSSFRow row, HSSFSheet sheet, int GoDown){
        if(row.getCell(0).getStringCellValue().indexOf("з") != -1 && !row.getCell(0).getStringCellValue().equals(sheet.getRow(GoDown+1).getCell(0)) && row.getHeight() > 50){
            return true;
        }else{
            return false;
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

    private static int getMaxHeight(HSSFSheet sheet){
        int len = 0;
        try{
            for(int i = 0; i < 1000; i++){
                String hi = sheet.getRow(i).getCell(0).getStringCellValue().toString();
                len = i;
            }
        }catch (Exception ex){

        }
        return len;
    }

    private static int GetLengnhtOfTable(HSSFSheet sheet){
        int len = 0;
        HSSFRow row = sheet.getRow(0);
        try{
            for(int i = 0; i < 1000; i++){
                Cell cell = row.getCell(i);
                String hi = cell.getStringCellValue();
                len = i;
            }
        }catch (Exception ex){

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
                    Log.i("hi",  "строка для начала найдена: " + i);
                    return i;
                }
            }
        } catch (Exception ex) {
            Log.i("hi",  "строка пары не найдена:" + ex);
            return -1;
        }
        return -1;
    }

    private void InizializateLessonsViews() {
        TLesson1 = findViewById(R.id.lesson1);
        TLesson2 = findViewById(R.id.lesson2);
        TLesson3 = findViewById(R.id.lesson3);
        TLesson4 = findViewById(R.id.lesson4);
        TLesson5 = findViewById(R.id.lesson5);
        TLesson6 = findViewById(R.id.lesson6);
        TLesson7 = findViewById(R.id.lesson7);
        Choose = findViewById(R.id.GroupChose);
        Choose.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ChooseGroup.class);
                intent.putExtra("arrayGroups", Groups);
                startActivity(intent);
            }
        });
    }
}