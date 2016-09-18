package ru.iammaxim.vkmonitor;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

public class LongPollService extends Service {
    private LongPollThread thread;
//    Messenger messageHandler;
    private static final int NOTIFICATION_ID = 124678;

    public LongPollService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        messageHandler = (Messenger) intent.getExtras().get("MESSENGER");
        thread = new LongPollThread(getApplicationContext(), "LongPollThread");
        App.longPollThread = thread;
        thread.start();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle("VK Monitor")
                .setContentText("running")
                .setSmallIcon(R.mipmap.icon)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true);
        Notification notification = builder.build();
        startForeground(NOTIFICATION_ID, notification);
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        thread.interrupt();
        stopForeground(true);
        App.showNotification(getApplicationContext(), "Service killed");
        stopSelf();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
