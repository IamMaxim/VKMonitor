package ru.iammaxim.vkmonitor.API.Objects;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by maxim on 5/21/17.
 */

public class ObjectGroup {
    public int id;
    public String name, photo_200;

    public ObjectGroup(JSONObject o) {
        try {
            id = o.getInt("id");
            name = o.getString("name");
            photo_200 = o.getString("photo_200");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public ObjectUser asUser() {
        ObjectUser user = new ObjectUser();
        user.id = -id;
        user.first_name = name;
        user.last_name = "";
        user.photo = photo_200;
        return user;
    }
}
