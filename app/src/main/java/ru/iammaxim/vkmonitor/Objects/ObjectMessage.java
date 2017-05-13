package ru.iammaxim.vkmonitor.Objects;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ru.iammaxim.vkmonitor.AccessTokenManager;
import ru.iammaxim.vkmonitor.Users;

public class ObjectMessage {
    public int id, from_id, user_id;
    public String title, body;
    public JSONObject json;
    public long date;
    public boolean out;

    private static final int OUT_FLAG_OFFSET = 1;

    public ObjectMessage() {
    }

    public ObjectMessage(JSONObject object) {
        this.json = object;
        try {
            if (object.has("id"))
                id = object.getInt("id");
            body = object.getString("body");
            if (object.has("title"))
                title = object.getString("title");
            if (object.has("out"))
                out = object.getInt("out") == 1;
            if (object.has("user_id"))
                user_id = object.getInt("user_id");
            if (object.has("from_id"))
                from_id = object.getInt("from_id");
            else
                from_id = user_id;
            if (title == null || title.equals(" ... "))
                title = Users.get(user_id).getTitle();
            date = object.getLong("date");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public ObjectMessage(String json) throws JSONException {
        this(new JSONObject(json));
    }

    public static ObjectMessage createFromLongPoll(JSONArray object) {
        ObjectMessage message = new ObjectMessage();
        try {
            message.id = object.getInt(1);
            message.processFlags(object.getInt(2));
            message.from_id = object.getInt(3);
            if (message.out)
                message.user_id = AccessTokenManager.currentUser.id;
            else
                message.user_id = message.from_id;
            message.date = object.getLong(4);
            message.title = object.getString(5);
            message.body = object.getString(6);
            message.user_id = Integer.parseInt(object.getJSONObject(7).getString("from"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return message;
    }

    private void processFlags(int flags) {
        out = ((flags & (1 << OUT_FLAG_OFFSET)) >> OUT_FLAG_OFFSET) == 1;
    }
}