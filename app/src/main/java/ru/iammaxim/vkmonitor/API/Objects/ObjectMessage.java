package ru.iammaxim.vkmonitor.API.Objects;

import android.text.Html;
import android.text.Spanned;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import ru.iammaxim.vkmonitor.API.Objects.Attachments.Attachment;
import ru.iammaxim.vkmonitor.API.Objects.Attachments.AttachmentPhoto;
import ru.iammaxim.vkmonitor.API.Users.Users;

public class ObjectMessage {
    public int random_id;
    public int id, user_id, peer_id = -1;
    public String title, body, photo;
    public JSONObject json;
    public long date;
    public int flags;
    public boolean out, read_state = true, muted = false;
    public boolean isAction = false;
    public boolean isSending = false;

    public ArrayList<AttachmentPhoto> photos = new ArrayList<>();

    private static final int
            READ_STATE_FLAG = 1,
            OUT_FLAG = 2,
            REPLIED_FLAG = 4,
            IMPORTANT_FLAG = 8,
            CHAT_FLAG = 16,
            FRIENDS_FLAG = 32,
            SPAM_FLAG = 64,
            DELETED_FLAG = 128,
            FIXED_FLAG = 256,
            MEDIA_FLAG = 512;

    public ObjectMessage() {
    }

    public ObjectMessage(int peer_id, int user_id, String body) {
        this.peer_id = peer_id;
        this.user_id = user_id;
        this.body = body;

        ObjectUser user = Users.get(user_id);

        if (user_id == Users.get().id)
            out = true;
        else
            out = false;
        this.title = user.getTitle();
        this.date = System.currentTimeMillis();
        this.photo = user.photo_200;
    }

    public Spanned getFullBody() {
        StringBuilder sb = new StringBuilder();
        sb.append(body);
        try {
            if (json.has("fwd_messages")) {
                sb.append(sb.length() > 0 ? " " : "").append("<font color=\"#466991\">").append(json.getJSONArray("fwd_messages").length()).append(" forwarded messages").append("</font>");
            }
            if (json.has("attachments")) {
                JSONArray attachments = json.getJSONArray("attachments");
                for (int i = 0; i < attachments.length(); i++) {
                    JSONObject attachment = attachments.getJSONObject(i);
                    String type = attachment.getString("type");
                    String nameToAdd;
                    if (type.equals("doc")) {
                        nameToAdd = attachment.getJSONObject("doc").getString("title");
                    } else
                        nameToAdd = type;
                    sb.append(" ").append("<font color=\"#466991\">").append(nameToAdd).append("</font>");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return Html.fromHtml(sb.toString());
    }

    public ObjectMessage(JSONObject object) {
        this.json = object;
        try {
            if (object.has("id"))
                id = object.getInt("id");
            body = object.getString("body");
            if (object.has("title")) {
                title = object.getString("title");
            }
            if (object.has("out"))
                out = object.getInt("out") == 1;

            if (object.has("chat_id")) {
                peer_id = 2000000000 + object.getInt("chat_id");
                user_id = object.getInt("user_id");
            } else if (object.has("user_id")) {
                peer_id = object.getInt("user_id");
                if (out)
                    user_id = Users.get().id;
                else
                    user_id = peer_id;
            }

            if (title == null || title.equals(" ... ") || title.equals("")) {
                title = Users.get(peer_id).getTitle();
            }
            date = object.getLong("date") * 1000;
            if (object.has("read_state"))
                read_state = object.getInt("read_state") == 1;
            if (object.has("photo_200"))
                photo = object.getString("photo_200");
            else if (!object.has("chat_id"))
                photo = Users.get(peer_id).photo_200;
            if (object.has("push_settings")) {
                muted = object.getJSONObject("push_settings").getInt("sound") == 1;
            }

            if (object.has("random_id"))
                random_id = object.getInt("random_id");

            if (object.has("action")) {
                isAction = true;
                switch (object.getString("action")) {
                    case "chat_photo_update":
                        body = Users.get(user_id) + " updated chat photo";
                        break;
                    case "chat_photo_remove":
                        body = Users.get(user_id) + " removed chat photo";
                        break;
                    case "chat_create":
                        body = Users.get(user_id) + " created chat \"" + object.getString("action_text") + "\"";
                        break;
                    case "chat_title_update":
                        body = Users.get(user_id) + " changed chat title to \"" + object.getString("action_text") + "\"";
                        break;
                    case "chat_invite_user":
                        body = Users.get(user_id) + " invited " + Users.get(object.getInt("action_mid")).getTitle();
                        break;
                    case "chat_kick_user":
                        body = Users.get(user_id) + " kicked " + Users.get(object.getInt("action_mid")).getTitle();
                        break;
                }
            }

            if (object.has("attachments")) {
                JSONArray arr = object.getJSONArray("attachments");

                for (int i = 0; i < arr.length(); i++) {
                    JSONObject o = arr.getJSONObject(i);
                    if (o.getString("type").equals("photo"))
                        photos.add(new AttachmentPhoto(o.getJSONObject("photo")));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public ObjectMessage(String json) throws JSONException {
        this(new JSONObject(json));
    }

    public void applyFlags(int update_code, int flags) {
        switch (update_code) {
            case 1:
                this.flags = flags;
                break;
            case 2:
                this.flags |= ~flags;
                break;
            case 3:
                this.flags &= ~flags;
                break;
        }
    }

    public void processFlags() {
        read_state = !getFlag(flags, READ_STATE_FLAG);
        out = getFlag(flags, OUT_FLAG);
    }

    private boolean getFlag(int flags, int flag) {
        return (flags & flag) == flag;
    }
}