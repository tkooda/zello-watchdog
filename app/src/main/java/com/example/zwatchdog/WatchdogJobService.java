package com.example.zwatchdog;

import android.app.ActivityManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class WatchdogJobService extends JobService {
    @Override
    public boolean onStartJob(JobParameters params) {
        if (!isZelloRunning(this)) {
            Intent i = getPackageManager().getLaunchIntentForPackage("com.loudtalks");
            if (i != null) {
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                Toast.makeText(this, "Launched Zello", Toast.LENGTH_SHORT).show();
            }
        }
        return false;
    }

    private boolean isZelloRunning(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : am.getRunningServices(20)) {
            if ("com.loudtalks".equals(service.service.getPackageName())) return true;
        }
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) { return false; }
}