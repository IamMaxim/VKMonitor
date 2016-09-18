package ru.iammaxim.vkmonitor.Objects;

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

    public void update(long ts) {
        this.ts = ts;
    }
}