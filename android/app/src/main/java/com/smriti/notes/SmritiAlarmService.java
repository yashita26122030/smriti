package com.smriti.notes;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SmritiAlarmService extends Service {
    static final String ACTION_RING = "com.smriti.notes.RING";
    static final String ACTION_DISMISS = "com.smriti.notes.DISMISS";
    static final String ACTION_SNOOZE = "com.smriti.notes.SNOOZE";
    static final String EXTRA_ID = "reminderId";
    static final String EXTRA_AT = "remindAt";
    static final String EXTRA_DELAY = "snoozeDelay";

    private static final String CHANNEL_ID = "smriti_alarm";
    private static final int NOTIFICATION_ID = 8712;
    private final ArrayDeque<String> queue = new ArrayDeque<>();
    private String activeId;
    private MediaPlayer player;
    private PowerManager.WakeLock wakeLock;

    @Override
    public void onCreate() {
        super.onCreate();
        createChannel();
        PowerManager power = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = power.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Smriti:Alarm");
        wakeLock.setReferenceCounted(false);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            String id = intent.getStringExtra(EXTRA_ID);
            if (ACTION_DISMISS.equals(action)) {
                finishReminder(id, false, 0);
                return START_NOT_STICKY;
            }
            if (ACTION_SNOOZE.equals(action)) {
                long at = intent.hasExtra(EXTRA_AT)
                    ? intent.getLongExtra(EXTRA_AT, 0)
                    : System.currentTimeMillis() + intent.getLongExtra(EXTRA_DELAY, 0);
                finishReminder(id, true, at);
                return START_NOT_STICKY;
            }
            if (ACTION_RING.equals(action) && id != null && !queue.contains(id) && !id.equals(activeId)) {
                queue.addLast(id);
            }
        }
        if (activeId == null) {
            restoreRingingQueue();
            startNext();
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        stopPlayer();
        releaseWakeLock();
        super.onDestroy();
    }

    private void restoreRingingQueue() {
        List<JSONObject> ringing = new ArrayList<>();
        com.getcapacitor.JSArray reminders = AlarmScheduler.getReminders(this);
        for (int i = 0; i < reminders.length(); i++) {
            JSONObject reminder = reminders.optJSONObject(i);
            if (reminder != null && "ringing".equals(reminder.optString("state"))) {
                ringing.add(reminder);
            }
        }
        ringing.sort(Comparator.comparingLong(item -> item.optLong("at")));
        for (JSONObject reminder : ringing) {
            String id = reminder.optString("id");
            if (!id.isEmpty() && !queue.contains(id)) {
                queue.addLast(id);
            }
        }
    }

    private void startNext() {
        while (!queue.isEmpty()) {
            String nextId = queue.removeFirst();
            JSONObject reminder = AlarmScheduler.get(this, nextId);
            if (reminder == null || !"ringing".equals(reminder.optString("state"))) {
                continue;
            }
            activeId = nextId;
            startForeground(NOTIFICATION_ID, buildNotification(reminder));
            startPlayer(reminder.optBoolean("silent", false));
            if (!wakeLock.isHeld()) {
                wakeLock.acquire();
            }
            return;
        }
        stopForeground(STOP_FOREGROUND_REMOVE);
        stopSelf();
    }

    private void finishReminder(String id, boolean snooze, long at) {
        if (id == null) {
            return;
        }
        if (snooze) {
            JSONObject reminder = AlarmScheduler.get(this, id);
            if (reminder != null && at > System.currentTimeMillis()) {
                try {
                    reminder.put("at", at);
                    AlarmScheduler.schedule(this, reminder);
                } catch (JSONException error) {
                    throw new IllegalStateException("Could not snooze the alarm.", error);
                }
            }
        } else {
            AlarmScheduler.setState(this, id, "dismissed");
        }
        queue.remove(id);
        if (id.equals(activeId)) {
            activeId = null;
            stopPlayer();
            releaseWakeLock();
            stopForeground(STOP_FOREGROUND_REMOVE);
        }
        if (activeId == null) {
            startNext();
        }
    }

    private Notification buildNotification(JSONObject reminder) {
        String id = reminder.optString("id");
        String title = reminder.optString("title", "Smriti reminder");
        String body = reminder.optString("body");

        Intent launch = getPackageManager().getLaunchIntentForPackage(getPackageName());
        PendingIntent content = PendingIntent.getActivity(
            this, 0, launch, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        PendingIntent done = serviceAction(ACTION_DISMISS, id, 0);
        PendingIntent snooze = serviceAction(ACTION_SNOOZE, id, 5 * 60_000L);

        Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
            ? new Notification.Builder(this, CHANNEL_ID)
            : new Notification.Builder(this);
        return builder
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(body.isEmpty() ? "Your reminder is due." : body)
            .setCategory(Notification.CATEGORY_ALARM)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setContentIntent(content)
            .setOngoing(true)
            .setAutoCancel(false)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Done", done)
            .addAction(android.R.drawable.ic_lock_idle_alarm, "Snooze 5 min", snooze)
            .build();
    }

    private PendingIntent serviceAction(String action, String id, long at) {
        Intent intent = new Intent(this, SmritiAlarmService.class);
        intent.setAction(action);
        intent.putExtra(EXTRA_ID, id);
        intent.putExtra(ACTION_SNOOZE.equals(action) ? EXTRA_DELAY : EXTRA_AT, at);
        return PendingIntent.getService(
            this, requestCode(id, action), intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationChannel channel = new NotificationChannel(
            CHANNEL_ID, "Smriti alarms", NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription("Reminders that ring until dismissed or snoozed.");
        channel.setSound(null, null);
        channel.enableVibration(true);
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
    }

    private void startPlayer(boolean silent) {
        stopPlayer();
        if (silent) {
            return;
        }
        Uri alarm = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM);
        if (alarm == null) {
            alarm = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        }
        if (alarm == null) {
            return;
        }
        try {
            player = new MediaPlayer();
            player.setAudioAttributes(new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build());
            player.setDataSource(this, alarm);
            player.setLooping(true);
            player.setOnPreparedListener(MediaPlayer::start);
            player.setOnErrorListener((mediaPlayer, what, extra) -> {
                Log.e("SmritiAlarm", "Could not play the selected alarm ringtone: " + what + "/" + extra);
                stopPlayer();
                return true;
            });
            player.prepareAsync();
        } catch (IOException | IllegalArgumentException error) {
            Log.e("SmritiAlarm", "Could not start the alarm ringtone.", error);
            stopPlayer();
        }
    }

    private void stopPlayer() {
        if (player == null) {
            return;
        }
        try {
            if (player.isPlaying()) {
                player.stop();
            }
        } catch (IllegalStateException error) {
            Log.w("SmritiAlarm", "Alarm player was already stopped.", error);
        }
        player.release();
        player = null;
    }

    private void releaseWakeLock() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    private int requestCode(String id, String action) {
        return (id + action).hashCode() & 0x7fffffff;
    }
}
