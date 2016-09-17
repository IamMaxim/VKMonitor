package ru.iammaxim.vkmonitor;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;

public class LongPollService extends Service {
    private LongPollThread thread;
    Messenger messageHandler;
    private static final int NOTIFICATION_ID = 124678;

    public LongPollService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        messageHandler = (Messenger) intent.getExtras().get("MESSENGER");
        thread = new LongPollThread(getApplicationContext(), messageHandler, "LongPollThread");
        thread.start();
        Notification.Builder builder = new Notification.Builder(this).setContentTitle("VK Monitor").setContentText("running");
        Notification notification = builder.build();
        startForeground(NOTIFICATION_ID, notification);
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        thread.interrupt();
        App.showNotification(getApplicationContext(), "Service killed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
