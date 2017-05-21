package ru.iammaxim.vkmonitor.API.Messages;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import ru.iammaxim.vkmonitor.API.Objects.ObjectDialog;
import ru.iammaxim.vkmonitor.Net;

/**
 * Created by maxim on 5/21/17.
 */

public class Messages {
    public static ObjectDialog getDialogByMessageID(int message_id) throws IOException, JSONException {
        return new ObjectDialog(new JSONObject(
                Net.processRequest("messages.getDialogs", true, "start_message_id=" + message_id, "count=1"))
                .getJSONObject("response")
                .getJSONArray("items")
                .getJSONObject(0));
    }
}
