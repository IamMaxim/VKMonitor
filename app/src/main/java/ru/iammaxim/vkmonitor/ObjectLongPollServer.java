package ru.iammaxim.vkmonitor;

import org.json.JSONException;
import org.json.JSONObject;

public class ObjectLongPollServer {
    public String key, server;
    public long ts;

    public ObjectLongPollServer(String json) throws JSONException {
        JSONObject o = new JSONObject(json).getJSONObject("response");
        key = o.getString("key");
        server = o.getString("server");
        ts = o.getLong("ts");
    }

    public static ObjectLongPollServer getServer(String JSON) throws JSONException {
        return new ObjectLongPollServer(JSON);
    }

    public void update(long ts) {
        this.ts = ts;
    }
}