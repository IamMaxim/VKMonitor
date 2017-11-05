package ru.iammaxim.vkmonitor.API.Objects.Attachments;

import android.util.DisplayMetrics;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import ru.iammaxim.vkmonitor.API.Photos.Photos;

/**
 * Created by maxim on 07.07.2017.
 */

public class AttachmentPhoto extends Attachment {
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
    public boolean isUploading = false;
    public boolean isErrored = false;
    public String localFilepath;

    public AttachmentPhoto(JSONObject object) throws JSONException {
        super("photo");
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

    private AttachmentPhoto() {
        super("photo");
    }

    public static AttachmentPhoto upload(File f, int peer_id) {
        AttachmentPhoto photo = new AttachmentPhoto();
        photo.localFilepath = f.getAbsolutePath();
        photo.height = 60;
        photo.isUploading = true;

        new Thread(() -> {
            try {
                JSONObject res = Photos.upload(f, peer_id);
                int owner_id = res.getInt("owner_id");
                int id = res.getInt("id");
                photo.id = id;
                photo.owner_id = owner_id;
                photo.isUploading = false;
            } catch (Exception e) {
                e.printStackTrace();
                photo.isErrored = true;
            }
        }).start();

        return photo;
    }

    public String getBestURL() {
        return photos.get(best);
    }
}
