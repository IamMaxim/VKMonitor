package ru.iammaxim.vkmonitor;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Scanner;

import ru.iammaxim.vkmonitor.Activities.LogActivity;
import ru.iammaxim.vkmonitor.Activities.MainActivity;

/**
 * Created by maxim on 10.09.2016.
 */
public class App extends Application {
    public static String logPath = Environment.getExternalStorageDirectory().getPath() + "/VKMonitor.log";
    public static String filterPath = Environment.getExternalStorageDirectory().getPath() + "/VKMonitor.filter";
    private static File logFile;
    private static FileOutputStream logFos;
    public static SimpleDateFormat dateSDF = new SimpleDateFormat("dd.MM.yy");
    public static SimpleDateFormat timeSDF = new SimpleDateFormat("HH:mm:ss");
    public static ArrayList<Integer> filter = new ArrayList<>();
    public static boolean useFilter = false;
    public static UpdateMessageHandler updateMessageHandler = new UpdateMessageHandler();
    public static LongPollService.LongPollThread longPollThread;

    // should be called first from UI thread
    // because UpdateMessageHandler should only be created from UI thread
    public static void init() {}

    static {
        logFile = new File(logPath);
        try {
            if (!logFile.exists())
                logFile.createNewFile();
            logFos = new FileOutputStream(logFile, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        File filterFile = new File(filterPath);
        try {
            if (filterFile.exists()) {
                Scanner scanner = new Scanner(filterFile).useDelimiter("\\A");
                JSONArray json = new JSONArray(scanner.next());
                if (json.length() > 0) useFilter = true;
                for (int i = 0; i < json.length(); i++) {
                    filter.add(json.getInt(i));
                }
                scanner.close();
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        if (AccessTokenManager.getActiveToken() != null)
            UserDB.update();
    }

    public static void saveFilter() {
        try {
            File filterFile = new File(filterPath);
            if (!filterFile.exists())
                filterFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(filterFile);
            JSONArray arr = new JSONArray();
            for (Integer integer : filter) {
                arr.put(integer);
            }
            if (filter.size() == 0)
                useFilter = false;
            else
                useFilter = true;
            fos.write(arr.toString().getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getAccessToken() {
        return AccessTokenManager.getAccessToken();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @SuppressLint("NewApi")
    public static void updateShortcuts(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);

            ShortcutInfo shortcut = new ShortcutInfo.Builder(context, "log")
                    .setShortLabel("Open log")
                    .setLongLabel("Open log")
                    .setIcon(Icon.createWithResource(context, R.drawable.ic_log_black_24dp))
                    .setIntent(new Intent(Intent.ACTION_VIEW, Uri.EMPTY, context, LogActivity.class))
                    .build();

            shortcutManager.setDynamicShortcuts(Collections.singletonList(shortcut));
        }
    }

    public static void addToLog(int user_id, int update_code, int... args) {
        try {
            Date date = new Date(System.currentTimeMillis());
            String dateStr = dateSDF.format(date);
            String timeStr = timeSDF.format(date);
            JSONObject o = new JSONObject();
            o.put("date", dateStr);
            o.put("time", timeStr);
            o.put("user_id", user_id);
            o.put("action", update_code);
            JSONArray arr = new JSONArray();
            for (int i = 0; i < args.length; i++) {
                arr.put(args[i]);
            }
            o.put("args", arr);
            byte[] bytes = (o.toString() + '\n').getBytes();
            logFos.write(bytes);
            Message msg = Message.obtain();
            Bundle data = new Bundle();
            data.putInt("update_code", update_code);
            data.putInt("user_id", user_id);
            data.putString("date", dateStr);
            data.putString("time", timeStr);
            data.putIntArray("args", args);
            msg.setData(data);
            updateMessageHandler.sendMessage(msg);
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void showNotification(Context applicationContext, String text) {
        Intent notificationIntent = new Intent(applicationContext, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(applicationContext, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        Notification.Builder builder = new Notification.Builder(applicationContext);
        builder.setContentIntent(contentIntent)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setContentTitle("VK Monitor")
                .setContentText(text)
                .setSmallIcon(R.mipmap.icon)
                .setVibrate(new long[]{0, 100, 100, 100, 100, 100, 100, 100});
        Notification notification = builder.build();
        NotificationManager notificationManager = (NotificationManager) applicationContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(5246451, notification);
    }

    public static void clearFilter() {
        filter.clear();
        new Thread(new Runnable() {
            @Override
            public void run() {
                saveFilter();
            }
        }).start();
    }
}
