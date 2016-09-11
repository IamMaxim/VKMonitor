package ru.iammaxim.vkmonitor;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class LongPollService extends Service {
    public LongPollService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new LongPollThread("LongPollThread").start();
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
