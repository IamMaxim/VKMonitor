package ru.iammaxim.vkmonitor;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;

public class LongPollService extends Service {
    private LongPollThread thread;

    public LongPollService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Messenger messageHandler = (Messenger) intent.getExtras().get("MESSENGER");
        thread = new LongPollThread(getApplicationContext(), messageHandler, "LongPollThread");
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