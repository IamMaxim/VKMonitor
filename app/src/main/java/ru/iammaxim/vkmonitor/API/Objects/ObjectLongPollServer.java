package ru.iammaxim.vkmonitor.API.Objects;

import org.json.JSONException;
import org.json.JSONObject;

public class ObjectLongPollServer {
    public String key, server;
    public long ts;

    private ObjectLongPollServer(String json) throws JSONException {
        System.out.println(json);
        JSONObject o = new JSONObject(json).getJSONObject("response");
        key = o.getString("key");
        server = o.getString("server");
        ts = o.getLong("ts");
    }

    public void update(long ts) {
        this.ts = ts;
    }

    public static ObjectLongPollServer getServer(String JSON) {
        ObjectLongPollServer server = null;
        while (server == null) {
            try {
                server = new ObjectLongPollServer(JSON);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (server == null)
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        }
        return server;
    }
}