package ru.iammaxim.vkmonitor.API.Objects;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import ru.iammaxim.vkmonitor.Net;

/**
 * Created by Maxim on 20.06.2016.
 */
public class ObjectUser {
    public boolean isChat = false;
    public boolean online = false;
    public int id, platform = 0;
    public String chat_title, first_name, last_name, photo_200;

    public ObjectUser() {
    }

    //load from users.get()
    public ObjectUser(String json) {
        try {
            JSONObject obj = new JSONObject(json);
            load(obj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public ObjectUser(JSONObject json) {
        load(json);
    }

    private void load(JSONObject json) {
        try {
            id = json.getInt("id");
            if (id > 2000000000) { // is chat
                isChat = true;
                if (json.has("chat_title"))
                    chat_title = json.getString("chat_title");
                else if (json.has("title"))
                    chat_title = json.getString("title");
                else
                    try {
                        JSONObject o = new JSONObject(Net.processRequest("messages.getChat", true, "chat_id=" + (id - 2000000000))).getJSONObject("response");
                        chat_title = o.getString("title");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            } else { // is user
                first_name = json.getString("first_name");
                last_name = json.getString("last_name");

                if (json.has("online"))
                    setStatus(json.getInt("online") == 1);
            }
            if (json.has("photo_200"))
                photo_200 = json.getString("photo_200");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return first_name + " " + last_name;
    }

    public String getTitle() {
        if (isChat)
            return chat_title;
        else
            return first_name + " " + last_name;
    }

    public void setStatus(boolean online, int platform) {
        this.online = online;
        this.platform = platform;
    }

    public void setStatus(boolean online) {
        this.online = online;
    }
}
