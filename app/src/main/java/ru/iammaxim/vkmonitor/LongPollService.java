package ru.iammaxim.vkmonitor;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class LongPollService extends Service {
    private LongPollThread thread;

    public LongPollService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        thread = new LongPollThread(getApplicationContext(), "LongPollThread");
        thread.start();
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        thread.interrupt();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
