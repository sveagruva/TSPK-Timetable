package com.example.tspktimetable;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.functions.Choose;
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
import java.sql.Time;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private  TextView TLesson1;
    private  TextView TLesson2;
    private  TextView TLesson3;
    private  TextView TLesson4;
    private  TextView TLesson5;
    private  TextView TLesson6;
    private  TextView TLesson7;
    private  TextView textView;
    private  TextView BeforeClasses;
    private  TextView Choose;
    private  Button Todaybtn;
    private  Button Tomorrowbtn;

    public static String currentGroup;

    private static ArrayList<String> Groups = new ArrayList<String>();
    private static ArrayList<String> Lessons = new ArrayList<String>();
    private static int MaxLength;
    private static int MaxHeight;
    private static String url;
    private static String text;
    private static Boolean Istoday;
    private static Boolean Change;
    private static SharedPreferences sPref;
    private static CountDownTimer timer;


    static{
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences Switches = getSharedPreferences("Switches", MODE_PRIVATE);
        if(Switches.getBoolean("dark", false)){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        InizializateLessonsViews();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        if(Calendar.SATURDAY == calendar.get(Calendar.DAY_OF_WEEK)){
            Tomorrowbtn.setText(getString(R.string.TheDayAfterTomorrow));
        }
        Change = false;
        sPref = getSharedPreferences("Group", MODE_PRIVATE);
        currentGroup = sPref.getString("Group", "0");
        if(currentGroup.equals("0")){
            if(Groups.size() != 0){
                StartChoose();
            }else{
                Toast.makeText(this, "Мы не смогли получить расписание", Toast.LENGTH_SHORT).show();
            }
        }else{
            Choose.setText(currentGroup);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Timer();
        if(Change){
            main();
            Change = false;
        }else{
            SharedPreferences sPref = getSharedPreferences("Group", MODE_PRIVATE);
            String Lesson = sPref.getString("LessonsCurrent", "0");
            int day = sPref.getInt("day", -1);
            Date date = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
            if((day == currentDay) && (!Lesson.equals("0"))){
                ShowLessons(Lesson, 0);
            }else{
                main();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        timer.cancel();
    }

    private void main(){
        Istoday = sPref.getBoolean("Istoday", true);
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = ((calendar.get(Calendar.YEAR)) - (calendar.get(Calendar.YEAR)/100)*100);
        if(Istoday){
            Todaybtn.setEnabled(false);
            Tomorrowbtn.setEnabled(true);
            String month;
            String day;
            if(calendar.get(Calendar.DAY_OF_MONTH) - 9 > 0){
                day = calendar.get(Calendar.DAY_OF_MONTH) + "";
            }else{
                day = "0" + calendar.get(Calendar.DAY_OF_MONTH);
            }
            if(calendar.get(Calendar.MONTH) - 9 > 0){
                month = (calendar.get(Calendar.MONTH) + 1) + "";
            }else{
                month = "0" + (calendar.get(Calendar.MONTH)+ 1);
            }

            String data = day + "." + month + "." + year;
            DoEverything("http://tspk.org/images/raspisanie/" + data + ".xls");
        }else{
            Todaybtn.setEnabled(true);
            Tomorrowbtn.setEnabled(false);
            String month;
            String day;
            if(Calendar.SATURDAY == calendar.get(Calendar.DAY_OF_WEEK)){
                calendar.add(Calendar.DAY_OF_MONTH,1);
            }
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            if(calendar.get(Calendar.DAY_OF_MONTH) - 9 >= 0){
                day = calendar.get(Calendar.DAY_OF_MONTH) + "";
            }else{
                day = "0" + calendar.get(Calendar.DAY_OF_MONTH);
            }

            if(calendar.get(Calendar.MONTH) - 9 >= 0){
                month = (calendar.get(Calendar.MONTH) + 1) + "";
            }else{
                month = "0" + (calendar.get(Calendar.MONTH)+ 1);
            }


            String data = day + "." + month + "." + year;
            DoEverything("http://tspk.org/images/raspisanie/" + data + ".xls");
        }

        currentGroup = sPref.getString("Group", "0");
        if(currentGroup.equals("0")){
            if(Groups.size() != 0){
                StartChoose();
            }else{
                Toast.makeText(this, "Мы не смогли получить расписание", Toast.LENGTH_SHORT).show();
            }
        }else{
            Choose.setText(currentGroup);
            ShowLessons(currentGroup);
        }
    }

    private void DoEverything(String url){
        download task = new download();
        try {

            File file = task.execute(url, getCacheDir() + "/hi.xls").get();
            Log.i("logs", url);
            Log.i("logs", file.getAbsolutePath() + " " + file.length());
            getArrays(file);
            file.delete();
            SharedPreferences.Editor ed = sPref.edit();
            ed.putString("Group", currentGroup);
            ed.apply();
        } catch (ExecutionException | InterruptedException | IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Мы не смогли получить расписание", Toast.LENGTH_SHORT).show();
        }
    }

    public void MakeToday(View view) {
        Istoday = true;
        SharedPreferences sPref = getSharedPreferences("Group", MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putBoolean("Istoday", Istoday);
        ed.apply();
        main();
    }

    public void MakeTomorrow(View view) {
        Istoday = false;
        SharedPreferences sPref = getSharedPreferences("Group", MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putBoolean("Istoday", Istoday);
        ed.apply();
        main();
    }

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
                Log.i("logs", "coped");
            } catch (IOException e) {
                e.printStackTrace();
                Log.i("logs", "not coped");
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
        //Log.i("mx", MaxHeight + "");
        int GoRow = findStart(ExcelSheet);
        Groups.clear();
        Lessons.clear();
        if(GoRow != -1){
            HSSFRow row = ExcelSheet.getRow(GoRow);
            StringBuilder Lesson;
            while(GoRow != MaxHeight){
                row = ExcelSheet.getRow(GoRow);
                if(row.getCell(0).getStringCellValue().toString().contains("Диспетчер")){
                    break;
                }

                if(IsItNameOfGroup(row.getCell(1).getStringCellValue().toString())){
                    int ItIsCorner = 1;
                    int GoDown;
                    HSSFRow row1;
                    for(int i = 0; i < MaxLength/2; i++){
                        Groups.add(row.getCell(ItIsCorner).getStringCellValue());
                        GoDown = GoRow;
                        GoDown++;
                        row1 = ExcelSheet.getRow(GoDown);
                        Lesson = new StringBuilder();
                        while(GoDown != MaxHeight-1 && !(IsItNameOfGroup(row1.getCell(1).getStringCellValue()))){
                            if(ItItLessons(row1, ExcelSheet, GoDown)){
                                if(!row1.getCell(ItIsCorner + 1).getStringCellValue().equals("")){
                                    //splitted cell
                                    Lesson.append("&&").append(row1.getCell(ItIsCorner).getStringCellValue()).append("$");
                                    Lesson.append("&&").append(row1.getCell(ItIsCorner + 1).getStringCellValue()).append("$");
                                }else{
                                    Lesson.append(row1.getCell(ItIsCorner).getStringCellValue()).append("$");
                                }
                            }
                            GoDown++;
                            row1 = ExcelSheet.getRow(GoDown);
                        }
                        Lessons.add(Lesson.toString());
                        ItIsCorner += 2;
                    }
                }
                GoRow++;
            }
        }
        while (Groups.get(Groups.size() - 1).equals("")) {
            Groups.remove(Groups.size() - 1);
        }
    }

    private void ShowLessons(String timetable, int aksdfjlkadsjflkds){
        TLesson1.setText("");
        TLesson2.setText("");
        TLesson3.setText("");
        TLesson4.setText("");
        TLesson5.setText("");
        TLesson6.setText("");
        TLesson7.setText("");
        try{
            //Log.i("hi3", timetable);
            ArrayList<TextView> textViews = new ArrayList<TextView>();
            textViews.add(TLesson1);
            textViews.add(TLesson2);
            textViews.add(TLesson3);
            textViews.add(TLesson4);
            textViews.add(TLesson5);
            textViews.add(TLesson6);
            textViews.add(TLesson7);
            String hi = timetable;
            int count = 0;
            while(hi.indexOf("$") == 0) {
                hi = hi.substring(1);
                count++;
            }
            while(hi.contains("$$")){
                hi = hi.replace("$$", "$");
            }
            for(int i = 0; i < count; i++){
                hi = "$" + hi;
            }
            timetable = hi;
            List<Integer> Endes = FindAllSubStrings(timetable,"$");
            if(!timetable.substring(0, Endes.get(0)).contains("&&")){
                textViews.get(0).setText(1 + ":  " + timetable.substring(0, Endes.get(0)));
            }else{
                textViews.get(0).setText(1 + ":  " + timetable.substring(2, Endes.get(0)) + " / " + timetable.substring(Endes.get(0)+3, Endes.get(1)));
                Endes.remove(0);
            }
            Log.i("hi3", timetable);
            try{
                for(int i = 1; i < 6; i++){
                    if(!timetable.substring(Endes.get(0), Endes.get(1)).contains("&&")){
                        textViews.get(i).setText(i+1 + ":  " + timetable.substring(Endes.get(0) +1, Endes.get(1)));
                        Endes.remove(0);
                    }else{
                        textViews.get(i).setText(i+1 + ":  " + timetable.substring(Endes.get(0) +3, Endes.get(1)) + " / " + timetable.substring(Endes.get(1)+3, Endes.get(2)));
                        Endes.remove(0);
                        Endes.remove(0);
                    }
                }
            }catch (Exception ignored){

            }
        }catch (Exception ignored){

        }
    }

    private void ShowLessons(String Group){
        TLesson1.setText("");
        TLesson2.setText("");
        TLesson3.setText("");
        TLesson4.setText("");
        TLesson5.setText("");
        TLesson6.setText("");
        TLesson7.setText("");
        try{
            int id = idOfGroup(Group);
            String timetable = Lessons.get(id);
            //Log.i("hi3", timetable);
            ArrayList<TextView> textViews = new ArrayList<TextView>();
            textViews.add(TLesson1);
            textViews.add(TLesson2);
            textViews.add(TLesson3);
            textViews.add(TLesson4);
            textViews.add(TLesson5);
            textViews.add(TLesson6);
            textViews.add(TLesson7);
            String hi = timetable;
            int count = 0;
            while(hi.indexOf("$") == 0) {
                hi = hi.substring(1);
                count++;
            }
            while(hi.contains("$$")){
                hi = hi.replace("$$", "$");
            }
            for(int i = 0; i < count; i++){
                hi = "$" + hi;
            }
            timetable = hi;
            List<Integer> Endes = FindAllSubStrings(timetable,"$");
            if(!timetable.substring(0, Endes.get(0)).contains("&&")){
                textViews.get(0).setText(1 + ":  " + timetable.substring(0, Endes.get(0)));
            }else{
                textViews.get(0).setText(1 + ":  " + timetable.substring(2, Endes.get(0)) + " / " + timetable.substring(Endes.get(0)+3, Endes.get(1)));
                Endes.remove(0);
            }
            SharedPreferences.Editor ed = sPref.edit();
            ed.putString("LessonsCurrent", timetable);
            Date date = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            ed.putInt("day", calendar.get(Calendar.DAY_OF_MONTH));
            ed.putString("Group", Group);
            ed.apply();
            try{
                for(int i = 1; i < 6; i++){
                    if(!timetable.substring(Endes.get(0), Endes.get(1)).contains("&&")){
                        textViews.get(i).setText(i+1 + ":  " + timetable.substring(Endes.get(0) +1, Endes.get(1)));
                        Endes.remove(0);
                    }else{
                        textViews.get(i).setText(i+1 + ":  " + timetable.substring(Endes.get(0) +3, Endes.get(1)) + " / " + timetable.substring(Endes.get(1)+3, Endes.get(2)));
                        Endes.remove(0);
                        Endes.remove(0);
                    }
                }
            }catch (Exception ex){

            }
        }catch (Exception ex){

        }
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
        if((row.getCell(0).getStringCellValue().contains("з") || row.getCell(0).getStringCellValue().contains("у")) && !row.getCell(0).getStringCellValue().equals(sheet.getRow(GoDown+1).getCell(0)) && row.getHeight() > 50){
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
        }catch (Exception ignored){

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

    private void Timer() {
        text = "";
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        long millislnFuture = 0;
        millislnFuture += (60 - calendar.get(Calendar.SECOND))*1000;
        long hours = calendar.get(Calendar.HOUR);
        if(calendar.get(Calendar.AM_PM) == Calendar.PM){
            hours += 12;
        }
        long minutes = calendar.get(Calendar.MINUTE);
        minutes += hours * 60;
        if (minutes >= 510 && minutes < 1035) {
            if (minutes < 600) { //1
                millislnFuture += (600 - minutes) * 60000;text += getString(R.string.untilBreak);
            }
            if (minutes >= 600 && minutes < 620) {
                millislnFuture += (620 - minutes) * 60000;text += getString(R.string.untilPara);
            }
            if (minutes >= 620 && minutes < 710) { //2
                millislnFuture += (710 - minutes) * 60000;text += getString(R.string.untilBreak);
            }
            if (minutes >= 710 && minutes < 745) {
                millislnFuture += (745 - minutes) * 60000;text += getString(R.string.untilPara);
            }
            if (minutes >= 745 && minutes < 835) { //3
                millislnFuture += (835 - minutes) * 60000;text += getString(R.string.untilBreak);
            }
            if (minutes >= 835 && minutes < 845) {
                millislnFuture += (845 - minutes) * 60000;text += getString(R.string.untilPara);
            }
            if (minutes >= 845 && minutes < 935) { //4
                millislnFuture += (935 - minutes) * 60000;text += getString(R.string.untilBreak);
            }
            if (minutes >= 935 && minutes < 945) {
                millislnFuture += (945 - minutes) * 60000;text += getString(R.string.untilPara);
            }
            if (minutes >= 945) { //5
                millislnFuture += (1035 - minutes) * 60000;text += getString(R.string.untilBreak);
            }
        }else{
            text += getString(R.string.untilClasses);
            if (minutes >= 1035) {
                millislnFuture += (1440 - minutes) * 60000;
                minutes = 0;
            }
            millislnFuture += (510 - minutes) * 60000;
        }

        if (calendar.get(Calendar.DAY_OF_WEEK) == 7) {
            millislnFuture += 86400000;
        }
        millislnFuture -= 60000;
        timer = new CountDownTimer(millislnFuture, 60000) {
            public void onTick(long millisUntilFinished) {
                BeforeClasses.setText(text + " " + (millisUntilFinished / 60000 / 60) + ":" + (millisUntilFinished / 60000 % 60 < 10 ? "0" + (millisUntilFinished / 60000 % 60) : millisUntilFinished / 60000 % 60));
            }
            public void onFinish() {
                final Handler handler = new Handler();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Timer();
                    }
                });
            }
        };
        timer.start();
    }

    private void InizializateLessonsViews() {
        textView = findViewById(R.id.textView);
        SharedPreferences Switches = getSharedPreferences("Switches", MODE_PRIVATE);
        if(Switches.getBoolean("correct", false)){
            textView.setText(R.string.StupidName);
        }
        TLesson1 = findViewById(R.id.lesson1);
        TLesson2 = findViewById(R.id.lesson2);
        TLesson3 = findViewById(R.id.lesson3);
        TLesson4 = findViewById(R.id.lesson4);
        TLesson5 = findViewById(R.id.lesson5);
        TLesson6 = findViewById(R.id.lesson6);
        TLesson7 = findViewById(R.id.lesson7);
        BeforeClasses = findViewById(R.id.BeforeClases);
        Todaybtn = findViewById(R.id.btnToday);
        Tomorrowbtn = findViewById(R.id.btnTomorrow);
        Choose = findViewById(R.id.GroupChose);
        Choose.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                {
                    main();
                    Change = true;
                    if (Groups.size() != 0) {
                        Intent intent = new Intent(getApplicationContext(), ChooseGroup.class);
                        intent.putExtra("arrayGroups", Groups);
                        startActivity(intent);
                    } else {
                        SharedPreferences sPref = getSharedPreferences("Group", MODE_PRIVATE);
                        Istoday = sPref.getBoolean("Istoday", true);
                        Date date = new Date();
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(date);
                        if(Istoday){
                            Todaybtn.setEnabled(false);
                            Tomorrowbtn.setEnabled(true);
                            String data = calendar.get(Calendar.DAY_OF_MONTH) + (calendar.get(Calendar.MONTH) + 1) + "." +(calendar.get(Calendar.DAY_OF_MONTH) - (calendar.get(Calendar.DAY_OF_MONTH)/100)*100);
                            DoEverything("http://tspk.org/images/raspisanie/" + data + ".xls");
                        }else{
                            Todaybtn.setEnabled(true);
                            Tomorrowbtn.setEnabled(false);
                            calendar.add(Calendar.DAY_OF_MONTH, 1);
                            String data = calendar.get(Calendar.DAY_OF_MONTH) + (calendar.get(Calendar.MONTH) + 1) + "." +(calendar.get(Calendar.DAY_OF_MONTH) - (calendar.get(Calendar.DAY_OF_MONTH)/100)*100);
                            DoEverything("http://tspk.org/images/raspisanie/" + data + ".xls");
                        }

                        if (Groups.size() != 0) {
                            StartChoose();
                        } else {
                            Toast.makeText(MainActivity.this, "Мы не смогли получить расписание", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
    }

    private void StartChoose(){
        Intent intent = new Intent(getApplicationContext(), ChooseGroup.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("arrayGroups", Groups);
        startActivity(intent);
    }
}