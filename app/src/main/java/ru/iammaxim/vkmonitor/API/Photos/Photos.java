package ru.iammaxim.vkmonitor.API.Photos;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import ru.iammaxim.vkmonitor.Net;

/**
 * Created by maxim on 10/15/17.
 */

public class Photos {
    public static JSONObject upload(File f, int peer_id) throws IOException, JSONException {
        JSONObject serverObj = new JSONObject(Net.processRequest("photos.getMessagesUploadServer", true, "peer_id=" + peer_id)).getJSONObject("response");
        String upload_url = serverObj.getString("upload_url");
        int album_id = serverObj.getInt("album_id");
        int user_id = serverObj.getInt("user_id");

        String response = Net.postRequest(upload_url, f, "photo");
        JSONObject responseObj = new JSONObject(response);
        int server = responseObj.getInt("server");
        String hash = responseObj.getString("hash");
        String photo = responseObj.getString("photo");

        return new JSONObject(Net.processRequest("photos.saveMessagesPhoto", true, "server=" + server, "hash=" + hash, "photo=" + photo)).getJSONArray("response").getJSONObject(0);
    }
}
