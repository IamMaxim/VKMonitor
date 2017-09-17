package ru.iammaxim.vkmonitor.API.Objects.Attachments;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by maxim on 9/17/2017.
 */

public class AttachmentDoc extends Attachment {
    public int id;
    public int owner_id;
    public String title;
    public long size;
    public String ext;
    public String url;
    public long date;
    public int type;
    public String access_key;

    public AttachmentDoc(JSONObject o) {
        super("doc");

        try {
            id = o.getInt("id");
            owner_id = o.getInt("owner_id");
            title = o.getString("title");
            size = o.getLong("size");
            ext = o.getString("ext");
            url = o.getString("url");
            date = o.getLong("date");
            type = o.getInt("type");
            access_key = o.getString("access_key");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
