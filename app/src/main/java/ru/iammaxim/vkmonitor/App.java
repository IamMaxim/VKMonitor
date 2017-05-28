package ru.iammaxim.vkmonitor;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.support.v4.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Scanner;

import ru.iammaxim.vkmonitor.API.Users.UserDB;
import ru.iammaxim.vkmonitor.Activities.LogActivity;
import ru.iammaxim.vkmonitor.Activities.MainActivity;
import ru.iammaxim.vkmonitor.Views.CircleTransformation;

/**
 * Created by maxim on 10.09.2016.
 */
public class App extends Application {
    public static String logPath = Environment.getExternalStorageDirectory().getPath() + "/VKMonitor.log";
    public static String filterPath = Environment.getExternalStorageDirectory().getPath() + "/VKMonitor.filter";
    private static File logFile;
    private static FileOutputStream logFos;
    public static SimpleDateFormat dateSDF = new SimpleDateFormat("dd.MM.yy");
    public static SimpleDateFormat dateSDF2 = new SimpleDateFormat("dd MMM");
    public static SimpleDateFormat timeSDF = new SimpleDateFormat("HH:mm:ss");
    public static ArrayList<Integer> filter = new ArrayList<>();
    public static boolean useFilter = false;
    public static UpdateMessageHandler handler = new UpdateMessageHandler();
    public static LongPollService.LongPollThread longPollThread;
    public static CircleTransformation circleTransformation = new CircleTransformation();

    public static void loadIO() {
        logFile = new File(logPath);
        File filterFile = new File(filterPath);
        try {
            if (!logFile.exists())
                logFile.createNewFile();
            logFos = new FileOutputStream(logFile, true);
        } catch (IOException e) {
            e.printStackTrace();
        }

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
    }

    public static void saveFilter() {
        try {
            useFilter = filter.size() != 0;
            File filterFile = new File(filterPath);
            if (!filterFile.exists())
                filterFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(filterFile);
            JSONArray arr = new JSONArray();
            for (Integer integer : filter) {
                arr.put(integer);
            }
            fos.write(arr.toString().getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void clearLog() {
        try {
            logFos.close();
            logFile.createNewFile();
            logFos = new FileOutputStream(logFile, false);
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

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            loadIO();
            AccessTokenManager.load();
            UserDB.load();
        }
    }

    @SuppressLint("NewApi")
    public static void updateShortcuts(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
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

    public static void addToLog(int update_code, JSONArray arr) {
        try {
            long date = System.currentTimeMillis();
            int peer_id = App.getPeerID(arr, update_code);
            boolean needToLog = needToLog(update_code);
            if (needToLog) {
                JSONObject obj = new JSONObject();
                obj.put("date", date);
                obj.put("peer_id", peer_id);
                obj.put("upd", arr);
                logFos.write((obj.toString() + '\n').getBytes());
            }
            Message msg = Message.obtain();
            Bundle data = new Bundle();
            data.putInt("peer_id", peer_id);
            data.putLong("date", date);
            data.putString("upd", arr.toString());
            data.putBoolean("needToLog", needToLog);
            msg.setData(data);
            handler.sendMessage(msg);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static boolean needToLog(int update_code) {
        switch (update_code) {
            case 6:
            case 7:
            case 8:
            case 9:
            case 61:
            case 62:
                return true;
            default:
                return false;
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
        new Thread(App::saveFilter).start();
    }

    @SuppressLint("WrongConstant")
    public static String formatDate(long date_long) {
        Calendar calendar = Calendar.getInstance();
        Date date = new Date(date_long);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        if (date.before(calendar.getTime())) {
            calendar.add(Calendar.DATE, -1);
            if (date.after(calendar.getTime())) {
                return "Yesterday";
            } else
                return dateSDF2.format(date);
        } else {
            return timeSDF.format(date);
        }
    }


    /**
     * @param update_code update code of event
     * @return index of peer_id in long poll update array
     */
    public static int getPeerID(JSONArray arr, int update_code) {
        try {
            switch (update_code) {
                case 4:
                    return arr.getInt(3);
                case 6:
                case 7:
                case 10:
                case 11:
                case 12:
                case 51: // chat_id here!
                case 61:
                case 62:
                case 70:
                case 114:
                    return arr.getInt(1);
                case 8:
                case 9:
                    return -arr.getInt(1);
                default:
                    return -1;
            }
        } catch (JSONException e) {
            return -1;
        }
    }
}
