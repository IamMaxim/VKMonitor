package ru.iammaxim.vkmonitor;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Scanner;

/**
 * Created by maxim on 10.09.2016.
 */
public class App extends Application {
    public static String logPath = Environment.getExternalStorageDirectory().getPath() + "/VKMonitor.log";
    public static String filterPath = Environment.getExternalStorageDirectory().getPath() + "/VKMonitor.filter";
    private static String access_token;
    private static File logFile;
    private static FileOutputStream fos;
    private static Date date;
    private static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    public static ArrayList<Integer> filter = new ArrayList<>();
    public static boolean useFilter = false;
    public static UpdateMessageHandler updateMessageHandler = new UpdateMessageHandler();

    static {
        logFile = new File(logPath);
        try {
            if (!logFile.exists())
                logFile.createNewFile();
            fos = new FileOutputStream(logFile, true);
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

    public static void setAccessToken(String s) {
        access_token = s;
    }

    public static String getAccessToken() {
        return access_token;
    }

    public static void addToLog(int user_id, int update_code, int... args) {
        try {
            String time = sdf.format(new Date(System.currentTimeMillis()));
            JSONObject o = new JSONObject();
            o.put("time", time);
            o.put("user_id", user_id);
            o.put("action", update_code);
            JSONArray arr = new JSONArray();
            for (int i = 0; i < args.length; i++) {
                arr.put(args[i]);
            }
            o.put("args", arr);
            fos.write((o.toString() + '\n').getBytes());
            Message msg = Message.obtain();
            Bundle data = new Bundle();
            data.putInt("update_code", update_code);
            data.putInt("user_id", user_id);
            data.putString("time", time);
            data.putIntArray("args", args);
            msg.setData(data);
            updateMessageHandler.sendMessage(msg);
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void log(String s) {
        String str = sdf.format(new Date(System.currentTimeMillis())) + s + '\n';
        System.out.println(str);
    }

    public static void showNotification(Context applicationContext, String text) {
        Intent notificationIntent = new Intent(applicationContext, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(applicationContext,
                0, notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        Notification.Builder builder = new Notification.Builder(applicationContext);
        builder.setContentIntent(contentIntent)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setContentTitle("title")
                .setContentText(text)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setVibrate(new long[]{0, 100, 100, 100, 100, 100, 100, 100});
        Notification notification = builder.build();
        NotificationManager notificationManager = (NotificationManager) applicationContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(5246451, notification);
    }
}
