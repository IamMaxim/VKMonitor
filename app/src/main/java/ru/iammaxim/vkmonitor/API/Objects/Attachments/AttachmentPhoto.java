package ru.iammaxim.vkmonitor.API.Objects.Attachments;

import android.util.DisplayMetrics;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by maxim on 07.07.2017.
 */

public class AttachmentPhoto {
    public int id;
    public int album_id;
    public int owner_id;
    public int width;
    public int height;
    public String text;
    public long date;
    public String access_key;
    public HashMap<Integer, String> photos = new HashMap<>();
    private int best = -1;

    public AttachmentPhoto(JSONObject object) throws JSONException {
        id = object.getInt("id");
        album_id = object.getInt("album_id");
        owner_id = object.getInt("owner_id");
        width = object.getInt("width");
        height = object.getInt("height");
        text = object.getString("text");
        date = object.getLong("date");
        access_key = object.getString("access_key");

        Iterator<String> it = object.keys();
        while (it.hasNext()) {
            String key = it.next();
            if (key.startsWith("photo_")) {
                int size = Integer.parseInt(key.substring(6));
                if (size > best)
                    best = size;
                photos.put(size, object.getString(key));
            }
        }
    }

    public String getBestURL() {
        return photos.get(best);
    }
}
