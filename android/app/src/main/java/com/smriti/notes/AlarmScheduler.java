package com.smriti.notes;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

final class AlarmScheduler {
    private static final String PREFS = "smriti_alarms";
    private static final String REMINDERS = "reminders";

    private AlarmScheduler() {}

    static synchronized void schedule(Context context, JSONObject reminder) throws JSONException {
        String id = reminder.getString("id");
        long at = reminder.getLong("at");

        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager == null) {
            throw new IllegalStateException("Android alarm service is unavailable.");
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !manager.canScheduleExactAlarms()) {
            throw new SecurityException("Allow Smriti to set exact alarms in Android settings.");
        }
        Intent intent = new Intent(context, SmritiAlarmReceiver.class);
        intent.setAction(SmritiAlarmReceiver.ACTION_ALARM);
        intent.putExtra(SmritiAlarmService.EXTRA_ID, id);
        PendingIntent pending = PendingIntent.getBroadcast(
            context, requestCode(id), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        Intent launch = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        PendingIntent show = PendingIntent.getActivity(
            context, 0, launch, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        manager.setAlarmClock(new AlarmManager.AlarmClockInfo(at, show), pending);
        reminder.put("state", "scheduled");
        put(context, id, reminder);
    }

    static synchronized void cancel(Context context, String id) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager != null) {
            Intent intent = new Intent(context, SmritiAlarmReceiver.class);
            intent.setAction(SmritiAlarmReceiver.ACTION_ALARM);
            PendingIntent pending = PendingIntent.getBroadcast(
                context, requestCode(id), intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
            );
            if (pending != null) {
                manager.cancel(pending);
                pending.cancel();
            }
        }
        remove(context, id);
    }

    static synchronized void setState(Context context, String id, String state) {
        JSONObject reminder = get(context, id);
        if (reminder == null) {
            return;
        }
        try {
            reminder.put("state", state);
            put(context, id, reminder);
        } catch (JSONException error) {
            throw new IllegalStateException("Could not update the alarm state.", error);
        }
    }

    static synchronized JSONObject get(Context context, String id) {
        try {
            return all(context).optJSONObject(id);
        } catch (JSONException error) {
            throw new IllegalStateException("Could not read saved alarms.", error);
        }
    }

    static synchronized JSArray getReminders(Context context) {
        JSArray result = new JSArray();
        try {
            JSONObject all = all(context);
            Iterator<String> ids = all.keys();
            while (ids.hasNext()) {
                JSONObject reminder = all.getJSONObject(ids.next());
                JSObject item = new JSObject(reminder.toString());
                result.put(item);
            }
        } catch (JSONException error) {
            throw new IllegalStateException("Could not read saved alarms.", error);
        }
        return result;
    }

    static synchronized void restore(Context context) {
        try {
            JSONObject saved = all(context);
            Iterator<String> ids = saved.keys();
            while (ids.hasNext()) {
                String id = ids.next();
                JSONObject reminder = saved.getJSONObject(id);
                String state = reminder.optString("state");
                if ("scheduled".equals(state) || "ringing".equals(state)) {
                    try {
                        schedule(context, reminder);
                    } catch (SecurityException error) {
                        Log.e("SmritiAlarm", "Exact-alarm access is required to restore reminder " + id, error);
                    }
                }
            }
        } catch (JSONException error) {
            throw new IllegalStateException("Could not restore saved alarms.", error);
        }
    }

    private static JSONObject all(Context context) throws JSONException {
        String raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(REMINDERS, "{}");
        return new JSONObject(raw);
    }

    private static void put(Context context, String id, JSONObject reminder) throws JSONException {
        JSONObject all = all(context);
        all.put(id, reminder);
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(REMINDERS, all.toString()).apply();
    }

    private static void remove(Context context, String id) {
        try {
            JSONObject all = all(context);
            all.remove(id);
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putString(REMINDERS, all.toString()).apply();
        } catch (JSONException error) {
            throw new IllegalStateException("Could not remove the saved alarm.", error);
        }
    }

    private static int requestCode(String id) {
        return id.hashCode() & 0x7fffffff;
    }
}
