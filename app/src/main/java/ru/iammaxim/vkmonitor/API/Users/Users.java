package ru.iammaxim.vkmonitor.API.Users;

import android.os.AsyncTask;
import android.os.NetworkOnMainThreadException;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import ru.iammaxim.vkmonitor.API.Groups.Groups;
import ru.iammaxim.vkmonitor.API.Objects.ObjectUser;
import ru.iammaxim.vkmonitor.App;
import ru.iammaxim.vkmonitor.Net;

/**
 * Created by maxim on 18.08.2016.
 */
public class Users {
    private static ArrayList<OnUsersUpdate> callbacks = new ArrayList<>();

    public static void addCallback(OnUsersUpdate callback) {
        callbacks.add(callback);
        App.notifyLongPollThread();
    }

    public static void removeCallback(OnUsersUpdate callback) {
        callbacks.remove(callback);
    }

    public static int callbacksSize() {
        return callbacks.size();
    }

    public static ObjectUser get() {
        return UserDB.get();
    }

    private static ObjectUser getUser(int id) throws IOException, JSONException {
        ObjectUser user;
        if (id > 2000000000) {
            user = new ObjectUser();
            user.id = id;
            user.isChat = true;
            String str = Net.processRequest("messages.getChat", true, "chat_id=" + (id - 2000000000));
            System.out.println(str);
            JSONObject o = new JSONObject(str).getJSONObject("response");
            user.chat_title = o.getString("title");
            if (o.has("photo_200"))
                user.photo = o.getString("photo_200");
        } else
            user = new ObjectUser(new JSONObject(Net.processRequest("users.get", true, "user_ids=" + id, "fields=photo_200,photo_100,photo_50,online")).getJSONArray("response").getJSONObject(0));
        return user;
    }

    public static ObjectUser get(int id) {
        return get(id, null);
    }

    public static ObjectUser get(int id, AsyncTask<ObjectUser, Void, ObjectUser> onUserLoad) {
        // It's a group
        if (id < 0) {
            return Groups.getById(-id).asUser();
        }

        ObjectUser user = UserDB.get(id);
        if (user == null) {
            try {
                ObjectUser u = getUser(id);
                UserDB.add(u);
                user = u;
            } catch (NetworkOnMainThreadException e) {
//                e.printStackTrace();
                System.err.println("NetworkOnMainException caught");
                user = new ObjectUser();
                user.first_name = "Error";
                user.last_name = "Error";

                new Thread(() -> {
                    try {
                        ObjectUser user1 = getUser(id);
                        UserDB.add(user1);
                        if (onUserLoad != null) {
                            onUserLoad.execute(user1);
                        }
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                }).start();
            } catch (Exception e) {
                e.printStackTrace();
                user = new ObjectUser();
                user.first_name = "Error";
                user.last_name = "Error";

                System.err.println("Error occurred while updating user " + id);
                System.err.println(TextUtils.join("\n", e.getStackTrace()));
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

    public interface OnUsersUpdate {
        void onStatusChange(int user_id, boolean online);
    }
}
