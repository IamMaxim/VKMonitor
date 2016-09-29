package ru.iammaxim.vkmonitor;

import android.content.Context;
import android.os.Messenger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import ru.iammaxim.vkmonitor.Objects.ObjectLongPollServer;

/**
 * Created by Maxim on 21.06.2016.
 */
public class LongPollThread extends Thread {
    private ObjectLongPollServer currentLongPollServer;
    private Context ctx;

    private void init() throws JSONException {
        try {
            currentLongPollServer = new ObjectLongPollServer(Net.processRequest("messages.getLongPollServer", true, "use_ssl=1", "need_pts=1"));
        } catch (IOException e) {
            log(e.toString());
            e.printStackTrace();
        }
    }

    public LongPollThread(Context ctx, String name) {
        super(name);
        this.ctx = ctx;
    }

    private void log(String s) {
        App.showNotification(ctx, s);
    }

    @Override
    public void run() {
        try {
            UserDB.load();
            UserDB.startSaveThread();
            init();
            Net.processRequest("stats.trackVisitor", true);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        while (!isInterrupted()) {
            try {
                processLongPollMessage();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        UserDB.saveThread.interrupt();
    }

    private void processLongPollMessage() throws JSONException {
        try {
            String json = Net.processRequest("https://" + currentLongPollServer.server + "?act=a_check&key=" + currentLongPollServer.key + "&ts=" + currentLongPollServer.ts + "&wait=50&mode=2");
            if (isInterrupted()) return;
            JSONObject o = new JSONObject(json);
            if (!o.isNull("failed")) {
                int code = o.getInt("failed");
                if (code == 2 || code == 3) {
                    System.out.println("Detected expired long poll token.");
                    currentLongPollServer = new ObjectLongPollServer(Net.processRequest("messages.getLongPollServer", true, "use_ssl=1", "need_pts=1"));
                    System.out.println("Server data updated.");
                    return;
                }
            }
            JSONArray arr = o.getJSONArray("updates");
            for (int i = 0; i < arr.length(); i++) {
                JSONArray obj = (JSONArray) arr.get(i);
                int updateCode = obj.getInt(0);
                switch (updateCode) {
                    case 6:
                        App.addToLog(obj.getInt(1), 6, obj.getInt(2));
                        break;
                    case 7:
                        App.addToLog(obj.getInt(1), 7, obj.getInt(2));
                        break;
                    case 8:
                        App.addToLog(-obj.getInt(1), 8);
                        break;
                    case 9:
                        App.addToLog(-obj.getInt(1), 9, obj.getInt(2));
                        break;
                    case 61:
                        App.addToLog(obj.getInt(1), 61);
                        break;
                    case 62:
                        App.addToLog(obj.getInt(1), 62, obj.getInt(2));
                        break;
                }
            }
            currentLongPollServer.update(o.getLong("ts"));
            if (App.updateMessageHandler.getCallbacksSize() == 0) {
                synchronized (this) {
                    try {
                        wait(300000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
