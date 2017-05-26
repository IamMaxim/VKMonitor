package ru.iammaxim.vkmonitor.API.Users;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import ru.iammaxim.vkmonitor.API.Groups.Groups;
import ru.iammaxim.vkmonitor.App;
import ru.iammaxim.vkmonitor.Net;
import ru.iammaxim.vkmonitor.API.Objects.ObjectUser;

/**
 * Created by maxim on 18.08.2016.
 */
public class Users {
    public static ArrayList<OnUsersUpdate> callbacks = new ArrayList<>();

    public interface OnUsersUpdate {
        void onStatusChange(int user_id, boolean online);
    }

    public static ObjectUser get() {
        return UserDB.get();
    }

    public static ObjectUser get(int id) {
        if (id > 2000000000) {
            ObjectUser user = new ObjectUser();
            user.first_name = "Chat";
            user.last_name = String.valueOf(id - 2000000000);
            return user;
        }
        ObjectUser user = UserDB.get(id);
        if (id < 0) {
            return Groups.getById(-id).asUser();
        }
        if (user == null) {
            try {
                user = new ObjectUser(new JSONObject(Net.processRequest("users.get", true, "user_ids=" + id, "fields=photo_200,online")).getJSONArray("response").getJSONObject(0));
                UserDB.add(user);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                System.err.println("Error occurred while updating user " + id);
                System.err.println(TextUtils.join("\n", e.getStackTrace()));
            }
        }
        return user;
    }

    public static void processLongPollMessage(int update_code, JSONArray arr) {
        System.out.println("processing long poll update in Users");
        try {
            switch (update_code) {
                case 8:
                    get(-arr.getInt(1)).setStatus(true, arr.getInt(2));
                    App.handler.post(() -> {
                        for (OnUsersUpdate callback : callbacks)
                            try {
                                callback.onStatusChange(-arr.getInt(1), true);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                    });
                    break;
                case 9:
                    get(-arr.getInt(1)).setStatus(false);
                    App.handler.post(() -> {
                        for (OnUsersUpdate callback : callbacks)
                            try {
                                callback.onStatusChange(-arr.getInt(1), false);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                    });
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
