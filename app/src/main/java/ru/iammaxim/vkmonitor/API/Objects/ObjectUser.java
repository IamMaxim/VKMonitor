package ru.iammaxim.vkmonitor.API.Objects;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Maxim on 20.06.2016.
 */
public class ObjectUser {
    public boolean online = false;
    public int id, platform = 0;
    public String first_name, last_name, photo_200;

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
            first_name = json.getString("first_name");
            last_name = json.getString("last_name");
            if (json.has("photo_200"))
                photo_200 = json.getString("photo_200");
            if (json.has("online"))
                setStatus(json.getInt("online") == 1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return first_name + " " + last_name;
    }

    public String getTitle() {
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
