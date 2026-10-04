package com.smriti.notes;

import android.Manifest;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.PermissionState;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(
    name = "SmritiAlarm",
    permissions = {
        @Permission(alias = "notifications", strings = {Manifest.permission.POST_NOTIFICATIONS})
    }
)
public class SmritiAlarmPlugin extends Plugin {
    @PluginMethod
    public void checkNotificationPermission(PluginCall call) {
        JSObject result = new JSObject();
        result.put("granted", Build.VERSION.SDK_INT < 33 || getPermissionState("notifications") == PermissionState.GRANTED);
        call.resolve(result);
    }

    @PluginMethod
    public void requestNotificationPermission(PluginCall call) {
        if (Build.VERSION.SDK_INT < 33 || getPermissionState("notifications") == PermissionState.GRANTED) {
            JSObject result = new JSObject();
            result.put("granted", true);
            call.resolve(result);
            return;
        }
        requestPermissionForAlias("notifications", call, "notificationPermissionCallback");
    }

    @PermissionCallback
    private void notificationPermissionCallback(PluginCall call) {
        JSObject result = new JSObject();
        result.put("granted", getPermissionState("notifications") == PermissionState.GRANTED);
        call.resolve(result);
    }

    @PluginMethod
    public void checkExactAlarmPermission(PluginCall call) {
        JSObject result = new JSObject();
        result.put("granted", canScheduleExactAlarms());
        call.resolve(result);
    }

    @PluginMethod
    public void requestExactAlarmPermission(PluginCall call) {
        if (canScheduleExactAlarms()) {
            JSObject result = new JSObject();
            result.put("granted", true);
            call.resolve(result);
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent intent = new Intent(
                Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                Uri.parse("package:" + getContext().getPackageName())
            );
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(intent);
            JSObject result = new JSObject();
            result.put("granted", false);
            call.resolve(result);
            return;
        }
        JSObject result = new JSObject();
        result.put("granted", true);
        call.resolve(result);
    }

    @PluginMethod
    public void schedule(PluginCall call) {
        if (!canScheduleExactAlarms()) {
            call.reject("Allow Smriti to set exact alarms in Android settings, then try again.");
            return;
        }
        String id = call.getString("id");
        Long at = call.getLong("at");
        if (id == null || at == null || at <= 0) {
            call.reject("A reminder ID and valid time are required.");
            return;
        }
        JSObject reminder = new JSObject();
        reminder.put("id", id);
        reminder.put("at", at);
        reminder.put("title", call.getString("title", "Smriti reminder"));
        reminder.put("body", call.getString("body", ""));
        reminder.put("silent", call.getBoolean("silent", false));
        try {
            AlarmScheduler.schedule(getContext(), reminder);
            call.resolve();
        } catch (Exception error) {
            call.reject("Could not schedule the Android alarm.", error);
        }
    }

    @PluginMethod
    public void cancel(PluginCall call) {
        String id = call.getString("id");
        if (id == null) {
            call.reject("A reminder ID is required.");
            return;
        }
        AlarmScheduler.cancel(getContext(), id);
        call.resolve();
    }

    @PluginMethod
    public void dismiss(PluginCall call) {
        String id = call.getString("id");
        if (id == null) {
            call.reject("A reminder ID is required.");
            return;
        }
        Intent intent = new Intent(getContext(), SmritiAlarmService.class);
        intent.setAction(SmritiAlarmService.ACTION_DISMISS);
        intent.putExtra(SmritiAlarmService.EXTRA_ID, id);
        getContext().startService(intent);
        call.resolve();
    }

    @PluginMethod
    public void snooze(PluginCall call) {
        String id = call.getString("id");
        Long at = call.getLong("at");
        if (id == null || at == null || at <= System.currentTimeMillis()) {
            call.reject("A reminder ID and future snooze time are required.");
            return;
        }
        if (!canScheduleExactAlarms()) {
            call.reject("Allow Smriti to set exact alarms in Android settings before snoozing.");
            return;
        }
        Intent intent = new Intent(getContext(), SmritiAlarmService.class);
        intent.setAction(SmritiAlarmService.ACTION_SNOOZE);
        intent.putExtra(SmritiAlarmService.EXTRA_ID, id);
        intent.putExtra(SmritiAlarmService.EXTRA_AT, at);
        getContext().startService(intent);
        call.resolve();
    }

    @PluginMethod
    public void getStates(PluginCall call) {
        JSObject result = new JSObject();
        result.put("reminders", AlarmScheduler.getReminders(getContext()));
        call.resolve(result);
    }

    private boolean canScheduleExactAlarms() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return true;
        }
        AlarmManager manager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        return manager != null && manager.canScheduleExactAlarms();
    }
}
