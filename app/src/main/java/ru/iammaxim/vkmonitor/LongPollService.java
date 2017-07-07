package ru.iammaxim.vkmonitor;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import ru.iammaxim.vkmonitor.API.Messages.Messages;
import ru.iammaxim.vkmonitor.API.Users.UserDB;
import ru.iammaxim.vkmonitor.API.Users.Users;
import ru.iammaxim.vkmonitor.Activities.LogActivity;
import ru.iammaxim.vkmonitor.API.Objects.ObjectLongPollServer;

public class LongPollService extends Service {
    private LongPollThread thread;
    private static final int NOTIFICATION_ID = 124678;
    private LocalBroadcastManager broadcaster;
    public static final String STATUS_CHANGED = "ru.iammaxim.vkmonitor.LongPollService.STATUS_CHANGED";
    public static final String STATUS_VALUE = "ru.iammaxim.vkmonitor.LongPollService.STATUS_VALUE";

    public LongPollService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        broadcaster = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        thread = new LongPollThread(getApplicationContext());
        App.longPollThread = thread;
        thread.start();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle("VK Monitor")
                .setContentText("Running")
                .setSmallIcon(R.mipmap.icon)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, LogActivity.class), PendingIntent.FLAG_UPDATE_CURRENT));
        Notification notification = builder.build();
        startForeground(NOTIFICATION_ID, notification);
        return Service.START_NOT_STICKY;
    }

    public void sendConnectionStatus(int value) {
        Intent intent = new Intent(STATUS_CHANGED);
        intent.putExtra(STATUS_VALUE, value);
        broadcaster.sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        thread.interrupt();
        stopForeground(true);
        stopSelf();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public class LongPollThread extends Thread {
        private ObjectLongPollServer currentLongPollServer;
        private Context ctx;

        private boolean init() throws JSONException {
            try {
                currentLongPollServer = ObjectLongPollServer.getServer(Net.processRequest("messages.getLongPollServer", true, "use_ssl=1", "need_pts=1"));
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        public LongPollThread(Context ctx) {
            super("LongPollThread");
            this.ctx = ctx;
        }

        private void log(String s) {
            App.showNotification(ctx, s);
        }

        @Override
        public void run() {
            try {
                if (!init()) {
                    log("Couldn't connect to VK. Does access token work?");
                    sendConnectionStatus(0);
                    ctx.stopService(new Intent(ctx, LongPollService.class));
                    return;
                }
                UserDB.startSaveThread();
                Net.processRequest("stats.trackVisitor", true);

                while (!isInterrupted()) {
                    try {
                        processLongPollMessage();
                    } catch (Exception e) {
                        System.err.println(e.getMessage());
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
                UserDB.saveThread.interrupt();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }

        private void processLongPollMessage() throws IOException, JSONException {
            String json = Net.processRequest("https://" + currentLongPollServer.server + "?act=a_check&key=" + currentLongPollServer.key + "&ts=" + currentLongPollServer.ts + "&wait=50&mode=74");
            System.out.println(json);
            if (isInterrupted()) return;
            JSONObject o = new JSONObject(json);
            if (!o.isNull("failed")) {
                int code = o.getInt("failed");
                if (code == 2 || code == 3) {
                    currentLongPollServer = ObjectLongPollServer.getServer(Net.processRequest("messages.getLongPollServer", true, "use_ssl=1", "need_pts=1"));
                    return;
                }
            }
            JSONArray arr = o.getJSONArray("updates");
            for (int i = 0; i < arr.length(); i++) {
                JSONArray arr1 = (JSONArray) arr.get(i);
                int update_code = arr1.getInt(0);
                Messages.processLongPollMessage(update_code, arr1);
                Users.processLongPollMessage(update_code, arr1);
                App.addToLog(update_code, arr1);
            }
            currentLongPollServer.update(o.getLong("ts"));

            if (App.handler.needToSleep()) {
                synchronized (this) {
                    try {
                        wait(300000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}
