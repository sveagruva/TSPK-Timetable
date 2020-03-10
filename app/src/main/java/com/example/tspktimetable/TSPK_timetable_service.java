package com.example.tspktimetable;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.content.Context;
import android.util.Log;
import android.util.TimeUtils;

import java.util.concurrent.TimeUnit;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class TSPK_timetable_service extends IntentService {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("serviceTSPK", "created");
    }

    public TSPK_timetable_service() {
        super("TSPK_timetable_service");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */

    @Override
    public void onDestroy() {
        Log.i("serviceTSPK", "destroy");
        super.onDestroy();
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */


    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i("serviceTSPK", "handler");
        while (true){
            Log.i("serviceTSPK", "окей");
        }

    }
}
