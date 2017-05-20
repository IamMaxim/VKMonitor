package ru.iammaxim.vkmonitor.Objects;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by maxim on 5/20/17.
 */

public class ObjectDialog {
    public ObjectMessage message;
    public int unread = 0;

    public ObjectDialog() {}

    public ObjectDialog(JSONObject obj) throws JSONException {
        if (obj.has("unread"))
            unread = obj.getInt("unread");
        message = new ObjectMessage(obj.getJSONObject("message"));
    }
}
