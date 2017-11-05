package ru.iammaxim.vkmonitor.API.Users;

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
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Scanner;

import ru.iammaxim.vkmonitor.API.Objects.ObjectUser;
import ru.iammaxim.vkmonitor.AccessTokenManager;
import ru.iammaxim.vkmonitor.Net;

/**
 * Created by maxim on 18.08.2016.
 */
public class UserDB {
    private static final String filepath = Environment.getExternalStorageDirectory().getPath() + "/VKMonitor.users";
    private static final HashMap<Integer, ObjectUser> userDB = new HashMap<>();
    public static Thread saveThread;
    private static ObjectUser me;
    private static boolean loaded = false;

    public static void startSaveThread() {
        saveThread = new Thread(() -> {
            while (!saveThread.isInterrupted()) {
                try {
                    Thread.sleep(60000);
                    save();
                } catch (InterruptedException e) {
                }
            }
        }, "UserDBsaveThread");
        saveThread.start();
    }

    public static void update() {
        new Thread(() -> {
            System.out.println("Starting user DB sync...");
            ArrayList<Integer> usersToUpdate = new ArrayList<>(100);
            ArrayList<Integer> chatsToUpdate = new ArrayList<>(100);
            synchronized (userDB) {
                usersToUpdate.addAll(userDB.keySet());

                Iterator<Integer> it = usersToUpdate.iterator();
                while (it.hasNext()) {
                    int id;
                    if ((id = it.next()) > 2000000000) {
                        chatsToUpdate.add(id);
                        it.remove();
                    }
                }

                while (usersToUpdate.size() > 0) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; usersToUpdate.size() > 0 && i < 100; i++) {
                        sb.append(usersToUpdate.remove(0));
                        if (i < 100)
                            sb.append(',');
                    }
                    try {
                        JSONArray arr = new JSONObject(Net.processRequest("users.get", true, "user_ids=" + sb.toString(), "fields=photo_200,online")).getJSONArray("response");
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

                while (chatsToUpdate.size() > 0) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; chatsToUpdate.size() > 0 && i < 100; i++) {
                        sb.append(chatsToUpdate.remove(0) - 2000000000);
                        if (i < 100 && chatsToUpdate.size() > 0)
                            sb.append(',');
                    }
                    try {
                        JSONArray arr = new JSONObject(Net.processRequest("messages.getChat", true, "chat_ids=" + sb.toString())).getJSONArray("response");
                        sb.delete(0, sb.length());
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject o = arr.getJSONObject(i);
                            o.put("id", o.getInt("id") + 2000000000);
                            ObjectUser user = new ObjectUser(o);
                            add(user);
                        }

                        Thread.sleep(500);
                    } catch (JSONException | IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            System.out.println("User DB sync completed.");
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
            synchronized (userDB) {
                File file = new File(filepath);
                if (!file.exists())
                    file.createNewFile();
                FileOutputStream fos = new FileOutputStream(file);
                JSONArray json = new JSONArray();
                for (ObjectUser user : userDB.values()) {
                    JSONObject o = new JSONObject()
                            .put("id", user.id)
                            .put("first_name", user.first_name)
                            .put("last_name", user.last_name)
                            .put("photo", user.photo);

                    if (user.isChat)
                        o.put("chat_title", user.chat_title);

                    json.put(o);
                }
                fos.write(json.toString().getBytes());
                fos.close();
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    public static void load() {
        loaded = true;
        new Thread(() -> {
            try {
                synchronized (userDB) {
                    userDB.clear();
                    File file = new File(filepath);
                    if (!file.exists()) {
                        System.out.println(filepath + " doesn't exists. Couldn't load users database");
                        return;
                    }
                    JSONArray arr = new JSONArray(new Scanner(file).useDelimiter("\\A").next());
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);
                        ObjectUser user = new ObjectUser(obj);
/*                        user.id = obj.getInt("id");
                        user.first_name = obj.getString("first_name");
                        user.last_name = obj.getString("last_name");
                        if (obj.has("photo"))
                            user.photo = obj.getString("photo");*/
                        add(user);
                    }
                }
            } catch (NoSuchElementException | FileNotFoundException | JSONException e) {
                System.err.println(e.getMessage());
            }

            if (AccessTokenManager.getActiveToken() != null)
                UserDB.update();
        }).start();
    }

    public static ObjectUser get(int id) {
        return userDB.get(id);
    }

    public static ObjectUser get() {
        return me;
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

    public static void setMe(ObjectUser me) {
        UserDB.me = me;
    }
}
