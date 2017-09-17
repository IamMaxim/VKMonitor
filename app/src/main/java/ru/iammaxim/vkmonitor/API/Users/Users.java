package ru.iammaxim.vkmonitor.API.Users;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

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
/*        if (id > 2000000000) {
            ObjectUser user = new ObjectUser();
            user.first_name = "Chat";
            user.last_name = String.valueOf(id - 2000000000);
            return user;
        }*/

        // It's a group
        if (id < 0) {
            return Groups.getById(-id).asUser();
        }

        ObjectUser user = UserDB.get(id);
        if (user == null) {
            try {
                ObjectUser u;
                if (id > 2000000000) {
                    u = new ObjectUser();
                    u.id = id;
                    u.isChat = true;
                    String str = Net.processRequest("messages.getChat", true, "chat_id=" + (id - 2000000000));
                    System.out.println(str);
                    JSONObject o = new JSONObject(str).getJSONObject("response");
                    u.chat_title = o.getString("title");
                    if (o.has("photo_200"))
                        u.photo_200 = o.getString("photo_200");
                } else
                    u = new ObjectUser(new JSONObject(Net.processRequest("users.get", true, "user_ids=" + id, "fields=photo_200,online")).getJSONArray("response").getJSONObject(0));
                UserDB.add(u);
                user = u;
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error occurred while updating user " + id);
                System.err.println(TextUtils.join("\n", e.getStackTrace()));
                user = new ObjectUser();
                user.first_name = "Error";
                user.last_name = "Error";
            }
        }
        return user;
    }

    public static void processLongPollMessage(int update_code, JSONArray arr) {
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
