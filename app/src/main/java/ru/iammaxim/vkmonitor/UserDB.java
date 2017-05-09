package ru.iammaxim.vkmonitor;

import android.os.Environment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Scanner;

import ru.iammaxim.vkmonitor.Objects.ObjectUser;

/**
 * Created by maxim on 18.08.2016.
 */
public class UserDB {
    private static final String filepath = Environment.getExternalStorageDirectory().getPath() + "/VKMonitor.users";
    private static HashMap<Integer, ObjectUser> userDB = new HashMap<>();
    public static Thread saveThread;
    private static boolean loaded = false;

    public static void startSaveThread() {
        saveThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!saveThread.isInterrupted()) {
                    try {
                        Thread.sleep(60000);
                        save();
                    } catch (InterruptedException e) {
                    }
                }
            }
        }, "UserDBsaveThread");
        saveThread.start();
    }

    public static void update() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<Integer> usersToUpdate = new ArrayList<>(1000);
                for (Integer id : userDB.keySet()) {
                    usersToUpdate.add(id);
                }
                while (usersToUpdate.size() > 0) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < usersToUpdate.size() && i < 100; i++) {
                        sb.append(usersToUpdate.get(0));
                        if (i < 100)
                            sb.append(',');
                    }
                    try {
                        JSONArray arr = new JSONObject(Net.processRequest("users.get", true, "user_ids=" + sb.toString())).getJSONArray("response");
                        sb.delete(0, sb.length());
                        for (int i = 0; i < arr.length(); i++) {
                            ObjectUser user = new ObjectUser(arr.getJSONObject(i));
                            add(user);
                        }

                        Thread.sleep(500);
                    } catch (JSONException | IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public static ObjectUser delete(int id) {
        return userDB.remove(id);
    }

    public static Collection<Integer> getUserIDs() {
        return userDB.keySet();
    }

    public static Collection<ObjectUser> getUsers() {
        return userDB.values();
    }

    public static void save() {
        try {
            File file = new File(filepath);
            if (!file.exists())
                file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            JSONArray json = new JSONArray();
            for (ObjectUser user : userDB.values()) {
                json.put(new JSONObject().put("id", user.id).put("first_name", user.first_name).put("last_name", user.last_name).put("photo_url", user.photo_url));
            }
            fos.write(json.toString().getBytes());
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    public static void load() {
        loaded = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    userDB.clear();
                    File file = new File(filepath);
                    if (!file.exists()) {
                        System.out.println(filepath + " doesn't exists. Couldn't load users database");
                        return;
                    }
                    JSONArray arr = new JSONArray(new Scanner(file).useDelimiter("\\A").next());
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);
                        ObjectUser user = new ObjectUser();
                        user.id = obj.getInt("id");
                        user.first_name = obj.getString("first_name");
                        user.last_name = obj.getString("last_name");
                        if (obj.has("photo_url"))
                            user.photo_url = obj.getString("photo_url");
                        add(user);
                    }
                } catch (NoSuchElementException | FileNotFoundException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static ObjectUser get(int id) {
        return userDB.get(id);
    }

    public static void add(int id, ObjectUser user) {
        userDB.put(id, user);
    }

    public static void add(ObjectUser user) {
        userDB.put(user.id, user);
    }

    public static boolean isLoaded() {
        return loaded;
    }
}
