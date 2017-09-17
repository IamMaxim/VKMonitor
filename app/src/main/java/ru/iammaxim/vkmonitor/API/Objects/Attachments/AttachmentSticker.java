package ru.iammaxim.vkmonitor.API.Objects.Attachments;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by maxim on 9/17/2017.
 */

public class AttachmentSticker extends Attachment {
    public int id;
    public int product_id;
    public int width, height;
    public HashMap<Integer, String> stickers = new HashMap<>();
    private int best = -1;

    public AttachmentSticker(JSONObject o) {
        super("sticker");

        try {
            id = o.getInt("id");
            product_id = o.getInt("product_id");
            width = o.getInt("width");
            height = o.getInt("height");

            Iterator<String> it = o.keys();
            while (it.hasNext()) {
                String key = it.next();
                if (key.startsWith("photo_")) {
                    int size = Integer.parseInt(key.substring(6));
                    if (size > best)
                        best = size;
                    stickers.put(size, o.getString(key));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getBestURL() {
        return stickers.get(best);
    }
}
