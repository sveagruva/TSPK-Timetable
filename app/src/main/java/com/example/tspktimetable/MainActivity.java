package com.example.tspktimetable;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;

public class MainActivity extends AppCompatActivity {

    private TextView textView; // app name))
    private  TextView BeforeClasses;
    private  TextView Choose;
    private  Button Todaybtn;
    private  Button Tomorrowbtn;

    public static String current;
    public static String type;

    public static Boolean NeedRecreate;

    public static ArrayList<String> SGroup = new ArrayList<String>(); // I get it from spec file
    public static ArrayList<String> STeachers = new ArrayList<String>();
    public static ArrayList<String> SRooms = new ArrayList<String>();

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

        sPref = getSharedPreferences("Group", MODE_PRIVATE);

        SharedPreferences Switches = getSharedPreferences("Switches", MODE_PRIVATE);
        if(Switches.getBoolean("dark", false)){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        InizializateLessonsViews();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) { actionBar.hide(); }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        if(Calendar.SATURDAY == calendar.get(Calendar.DAY_OF_WEEK)){ Tomorrowbtn.setText(getString(R.string.TheDayAfterTomorrow)); }
        Change = false;
        NeedRecreate = false;
    }

    @Override
    protected void onStart() {

        super.onStart();
        Timer();
        if(getSharedPreferences("Switches", MODE_PRIVATE).getBoolean("correct", false)){ textView.setText(R.string.StupidName); }
        else{ textView.setText(R.string.timetableText); }

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
                ShowLessons(Lesson);
            }else{
                main();
            }
        }

        current = sPref.getString("Value", "0");
        if(!current.equals("0")){
            Choose.setText(current);
        }else{
            Choose.setText(getString(R.string.shoose));
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        timer.cancel();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if(NeedRecreate){
            recreate();
        }
    }

    private void main(){
        Istoday = sPref.getBoolean("Istoday", true);
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = ((calendar.get(Calendar.YEAR)) - (calendar.get(Calendar.YEAR)/100)*100);
        String data;
        String month;
        String day;
        if(Istoday){
            Todaybtn.setEnabled(false);
            Tomorrowbtn.setEnabled(true);
        }else{
            Todaybtn.setEnabled(true);
            Tomorrowbtn.setEnabled(false);

            if(Calendar.SATURDAY == calendar.get(Calendar.DAY_OF_WEEK)){
                calendar.add(Calendar.DAY_OF_MONTH,1);
            }
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

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
        data = day + "." + month + "." + year;
        //Toast.makeText(this,  data, Toast.LENGTH_SHORT).show();
        Boolean de = DoEverything("http://tspk.org/images/raspisanie/" + data + ".xls");
        clearLessons();
        current = sPref.getString("Value", "0");
        type = sPref.getString("Type", "0");
        if(current.equals("0") || type.equals("0")){
            StartChoose();
        }else{
            Choose.setText(current);
            if(de){
                if(type.equals("Group")){
                    ShowLessons(Lessons.get(idOfGroup(current)));
                    SharedPreferences.Editor ed = sPref.edit();
                    ed.putString("LessonsCurrent", Lessons.get(idOfGroup(current)));
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(new Date());
                    ed.putInt("day", cal.get(Calendar.DAY_OF_MONTH));
                    ed.putString("Value", current);
                    ed.putString("Type", "Group");
                    ed.apply();
                }else{
                    ShowLessonsOnTheTrail(current.toLowerCase());
                }
            }
        }
    }

    private Boolean DoEverything(String url){
        download task = new download();
        download task1 = new download();

        try{
            task1.execute("http://tspk.org/images/raspisanie/android_info/option_android.xls", getFilesDir() + "/data.xls").get();
            File file = task.execute(url, getCacheDir() + "/hi.xls").get();
            getArrays(file);
            file.delete();
//                new File(getFilesDir() + "/data.xls").delete();
            return true;
        }catch (Exception ex){ return false; }
    }

    private Boolean getLists() {
        try{
            File file = new File(getFilesDir() + "/data.xls");
            if (file.exists()) {
                HSSFWorkbook ExcelBook = new HSSFWorkbook(new FileInputStream(file));
                HSSFSheet Sheet = ExcelBook.getSheet(ExcelBook.getSheetName(0));
                int max = getMaxHeight(Sheet, 1);
                SGroup.clear();
                SRooms.clear();
                STeachers.clear();
                String gr;
                String teac;
                String room;
                for (int i = 1; i < max; i++) {
                    try {
                        gr = Sheet.getRow(i).getCell(0).toString();
                        SGroup.add(gr);
                    } catch (Exception ex) { }
                    try {
                        teac = Sheet.getRow(i).getCell(1).toString();
                        STeachers.add(teac);
                    } catch (Exception ex) { }
                    try {
                        room = Sheet.getRow(i).getCell(2).toString();
                        SRooms.add(room);
                    } catch (Exception ex) { }
                }
                return true;
            }
        }catch (Exception ex){}
        return false;
    }

    private void clearLessons(){
        LinearLayout layout = (LinearLayout) findViewById(R.id.Lessons);
        layout.removeAllViews();
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

    static class download extends AsyncTask<String, Void, File>{

        @Override
        protected File doInBackground(String... strings) {
            URL url = null;
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
                Log.i("logs", "not coped" + strings[0]);
            }

            return file;
        }
    }

    private static void getArrays(File file) throws IOException {
        Log.i("hi", "Скачалось. Вес: " + file.length());
        HSSFWorkbook ExcelBook = new HSSFWorkbook(new FileInputStream(file));
        HSSFSheet ExcelSheet = ExcelBook.getSheet("Table 2");
        MaxLength = GetLengthOfTable(ExcelSheet);
        MaxHeight = getMaxHeight(ExcelSheet, 0);
        String lesson;
        int GoRow = findStart(ExcelSheet);
        Groups.clear();
        Lessons.clear();
        if(GoRow != -1){
            HSSFRow row;
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

                        lesson = Lesson.toString();
                        try{
                            while (lesson.substring(lesson.length() - 1).contains("$")) {
                                lesson = lesson.substring(0,lesson.length() - 1);
                            }
                        }catch (Exception ex){

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
    }

    private void ShowLessonsOnTheTrail(String Trail) {
        ArrayList<String> RightLessons;
        ArrayList<String> RightLessonsAll = new ArrayList<String>();
        StringBuilder current = new StringBuilder();
        for(int number = 0; number < Groups.size(); number++){
            RightLessons = StringToArray(Lessons.get(number), Trail, Trail, Groups.get(number));
            for(int i = 0; i < RightLessons.size(); i++){
                RightLessonsAll.add(RightLessons.get(i));
                current.append(RightLessons.get(i).substring(RightLessons.get(i).indexOf(":")+1)).append("$");
            }
        }

        //sort (help me)
        ArrayList<String> returnable = new ArrayList<String>();
        int min;
        String minS;

        while(RightLessonsAll.size() != 0){
            min = Integer.parseInt(RightLessonsAll.get(0).substring(0, RightLessonsAll.get(0).indexOf(":")));
            minS = RightLessonsAll.get(0);
            for(int i =0; i < RightLessonsAll.size(); i++){
                String check = RightLessonsAll.get(i);
                if(min > Integer.parseInt(check.substring(0, check.indexOf(":")))){
                    min = Integer.parseInt(check.substring(0, check.indexOf(":")));
                    minS = check;
                }
            }
            RightLessonsAll.remove(minS);
            returnable.add(minS);
        }

        for(String lesson: returnable){
            addLesson(lesson);
        }

        SharedPreferences.Editor ed = sPref.edit();
        ed.putString("LessonsCurrent", current.toString());
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        ed.putInt("day", cal.get(Calendar.DAY_OF_MONTH));
        ed.putString("Value", Trail);
        ed.putString("Type", "Trail");
        ed.apply();
    }

    public static ArrayList<String> StringToArray(String Lesson){
        ArrayList<String> result = new ArrayList<String>();
        List<Integer> Endes = FindAllSubStrings(Lesson,"$");
        String les = Lesson.substring(0, Endes.get(1)).toLowerCase();
        for(int i = 0; Endes.size() != 0; i++){
            if(!les.contains("&&")){
                result.add(i+1 + ":  " + Lesson.substring(Endes.get(0) +1, Endes.get(1)));
                Endes.remove(0);
            }else{
                result.add(i+1 + ":  " + Lesson.substring(Endes.get(0) +3, Endes.get(1)) + " / " + Lesson.substring(Endes.get(1)+3, Endes.get(2)));
                Endes.remove(0);
                Endes.remove(0);
            }
            if(Endes.size() >= 2){
                les = Lesson.substring(Endes.get(0), Endes.get(1)).toLowerCase();
            }else{
                Endes.clear();
            }
        }
        return result;
    }

    public static ArrayList<String> StringToArray(String Lesson, String Trail, String Trail2, String Group){
        ArrayList<String> result = new ArrayList<String>();
        List<Integer> Endes = FindAllSubStrings(Lesson,"$");
        String les = Lesson.substring(0, Endes.get(1)).toLowerCase();
        for(int i = 0; Endes.size() != 0; i++){
            if(!les.contains("&&")){
                result.add(i+1 + ":  "+ Group + " "  + Lesson.substring(Endes.get(0) +1, Endes.get(1)));
                Endes.remove(0);
            }else{
                result.add(i+1 + ":  " + Group + " " + Lesson.substring(Endes.get(0) +3, Endes.get(1)) + " / " + Lesson.substring(Endes.get(1)+3, Endes.get(2)));
                Endes.remove(0);
                Endes.remove(0);
            }
            if(!les.contains(Trail) || !les.contains(Trail2)){
                result.remove(result.size() - 1);
            }
            if(Endes.size() >= 2){
                les = Lesson.substring(Endes.get(0), Endes.get(1)).toLowerCase();
            }else{
                Endes.clear();
            }
        }

        return result;
    }

    private void addLesson(String text){
        LinearLayout layout = (LinearLayout) findViewById(R.id.Lessons);
        TextView dayToDay = new TextView(this);
        dayToDay.setText(text);
        dayToDay.setGravity(Gravity.CENTER);
        dayToDay.setTextSize(24);
        layout.addView(dayToDay);
    }

    private void ShowLessons(String timetable){
        clearLessons();
        ArrayList<String> RightLessons = StringToArray(timetable);
        for(int i = 0; i < RightLessons.size(); i++){
            addLesson(RightLessons.get(i));
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

    public static List<Integer> FindAllSubStrings(String main, String small){
        int lastIndex = 0;
        List<Integer> result = new ArrayList<Integer>();
        result.add(-1);

        while(lastIndex != -1) {
            lastIndex = main.indexOf(small,lastIndex);
            if(lastIndex != -1){
                result.add(lastIndex);
                lastIndex += 1;
            }
        }
        return result;
    }

    public static Boolean ItItLessons(HSSFRow row, HSSFSheet sheet, int GoDown){
        if((row.getCell(0).getStringCellValue().contains("з") || row.getCell(0).getStringCellValue().contains("у")) && !row.getCell(0).getStringCellValue().equals(sheet.getRow(GoDown+1).getCell(0)) && row.getHeight() > 50){
            return true;
        }else{
            return false;
        }
    }

    // СДЕЛАНО САНЕЙ    CREATED BY SYANYA
    public static Boolean IsItNameOfGroup(String maybeName){
        if ((maybeName.length() - 1 - maybeName.indexOf('-')) == 2) {
            return true;
        } else {
            return false;
        }
    }

    public static int getMaxHeight(HSSFSheet sheet, int cell){
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

    public static int GetLengthOfTable(HSSFSheet sheet){
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

    public static int findStart(HSSFSheet ExcelSheet){
        HSSFRow row;
        try {
            for(int i = 0; i < 100; i++) {
                row = ExcelSheet.getRow(i);
                String name = row.getCell(0).getStringCellValue();
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

            if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                millislnFuture += 86400000;
            }
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
        BeforeClasses = findViewById(R.id.BeforeClases);
        Todaybtn = findViewById(R.id.btnToday);

        Tomorrowbtn = findViewById(R.id.btnTomorrow);
        Choose = findViewById(R.id.GroupChose);
        Choose.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                {
                    Change = true;
                    StartChoose();
                }
            }
        });
    }

    public static String SpaceInADust(String WhoAreU){
        while(WhoAreU.contains("  ")){
            WhoAreU = WhoAreU.replace("  ", " ");
        }
        return WhoAreU;
    }

    private void StartChoose(){
        if(getLists()){ startActivity(new Intent(getApplicationContext(), ChooseGroup.class)); }
    }
}