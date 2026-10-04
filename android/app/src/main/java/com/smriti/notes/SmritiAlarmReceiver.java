package com.smriti.notes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class SmritiAlarmReceiver extends BroadcastReceiver {
    static final String ACTION_ALARM = "com.smriti.notes.ALARM";

    @Override
    public void onReceive(Context context, Intent intent) {
        String id = intent.getStringExtra(SmritiAlarmService.EXTRA_ID);
        if (id == null) {
            return;
        }
        AlarmScheduler.setState(context, id, "ringing");
        Intent service = new Intent(context, SmritiAlarmService.class);
        service.setAction(SmritiAlarmService.ACTION_RING);
        service.putExtra(SmritiAlarmService.EXTRA_ID, id);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(service);
        } else {
            context.startService(service);
        }
    }
}
