package com.example.zwatchdog;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String TARGET_PKG = "com.loudtalks";
    public static final String VERSION = BuildConfig.VERSION_NAME;
    
    @Override
    public void onReceive(Context context, Intent intent) {
        int count = intent.getIntExtra("count", 0);
        performWatchdogCheck(context);

        if (count == 0) {
            Toast.makeText(context, "Z-Watchdog: v" + VERSION, Toast.LENGTH_SHORT).show();
        }

        if (count < 10) {
            scheduleNext(context, count + 1, 60000);
        } else {
            startMaintenanceJob(context);
        }
    }

    public static boolean performWatchdogCheck(Context context) {
        if (!isZelloRunning(context)) {
            Intent i = context.getPackageManager().getLaunchIntentForPackage(TARGET_PKG);
            if (i != null) {
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
                Toast.makeText(context, "Launched Zello", Toast.LENGTH_SHORT).show();
                return true;
            }
        }
        return false;
    }

    private static boolean isZelloRunning(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : am.getRunningServices(20)) {
            if (TARGET_PKG.equals(service.service.getPackageName())) return true;
        }
        return false;
    }

    private void startMaintenanceJob(Context context) {
        if (Build.VERSION.SDK_INT >= 21) {
            JobScheduler js = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            JobInfo.Builder builder = new JobInfo.Builder(1, new ComponentName(context, WatchdogJobService.class));
            builder.setPeriodic(300000).setPersisted(true);
            js.schedule(builder.build());
        }
    }

    public static void scheduleNext(Context context, int count, long delayMs) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, AlarmReceiver.class);
        i.putExtra("count", count);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= 31) flags |= 1 << 25;
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, flags);
        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delayMs, pi);
    }
}
