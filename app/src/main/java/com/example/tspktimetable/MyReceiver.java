package com.example.tspktimetable;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.util.List;

public class MyReceiver extends BroadcastReceiver {


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        Log.i("serviceTSPK", "receiver");
//        context.startService(new Intent(context, TSPK_timetable_service.class));
       //context.startForegroundService(new Intent(context, TSPK_timetable_service.class));
    }
}
