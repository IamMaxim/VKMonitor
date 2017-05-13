package ru.iammaxim.vkmonitor.Objects;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Maxim on 20.06.2016.
 */
public class ObjectUser {
    public int id;
    public String first_name, last_name, photo_url;

    public ObjectUser() {
    }

    //load from users.get()
    public ObjectUser(String json) {
        JSONObject o;
        try {
            o = new JSONObject(json).getJSONArray("response").getJSONObject(0);
            id = o.getInt("id");
            first_name = o.getString("first_name");
            last_name = o.getString("last_name");
            photo_url = o.getString("photo_200");
        } catch (JSONException e) {}
    }

    public ObjectUser(JSONObject json) {
        try {
            json = json.getJSONArray("response").getJSONObject(0);
            id = json.getInt("id");
            first_name = json.getString("first_name");
            last_name = json.getString("last_name");
            photo_url = json.getString("photo_200");
        } catch (JSONException e) {}
    }

    @Override
    public String toString() {
        return first_name + " " + last_name;
    }

    public String getTitle() {
        return first_name + " " + last_name;
    }
}
