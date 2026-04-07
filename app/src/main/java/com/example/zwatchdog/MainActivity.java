package com.example.zwatchdog;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startService(new Intent(this, WatchdogService.class));
        AlarmReceiver.scheduleNext(this, 0, 0);
        finish();
    }
}
