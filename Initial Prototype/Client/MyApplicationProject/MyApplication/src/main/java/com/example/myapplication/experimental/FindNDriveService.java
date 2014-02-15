package com.example.myapplication.experimental;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Michal on 11/02/14.
 */
public class FindNDriveService extends IntentService {

    public FindNDriveService() {
        super("FindNDriveService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.i(this.getClass().getSimpleName(), "Sending GCM heartbeat broadcast...");

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("com.google.android.intent.action.MCS_HEARTBEAT");

        Intent broadcastIntent2 = new Intent();
        broadcastIntent2.setAction("com.google.android.intent.action.GTALK_HEARTBEAT");

        sendBroadcast(broadcastIntent);
        sendBroadcast(broadcastIntent2);
    }
}
