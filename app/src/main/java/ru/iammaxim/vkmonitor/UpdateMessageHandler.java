package ru.iammaxim.vkmonitor;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import ru.iammaxim.vkmonitor.API.Messages.Messages;
import ru.iammaxim.vkmonitor.API.Users.Users;

/**
 * Created by maxim on 11.09.2016.
 */
public class UpdateMessageHandler extends Handler {
    private ArrayList<Callback> callbacks = new ArrayList<>();

    public void addCallback(Callback callback) {
        callbacks.add(callback);
        if (App.longPollThread != null)
            synchronized (App.longPollThread) {
                App.longPollThread.notify();
            }
    }

    public void removeCallback(Callback callback) {
        callbacks.remove(callback);
    }

    public int getCallbacksSize() {
        return callbacks.size();
    }

    public boolean needToSleep() {
        return !(callbacks.size() > 0 || Messages.callbacks.size() > 0 || Users.callbacks.size() > 0);
    }

    @Override
    public void handleMessage(Message msg) {
        Bundle bundle = msg.getData();
        try {
            long date = bundle.getLong("date");
            int peer_id = bundle.getInt("peer_id");
            boolean needToLog = bundle.getBoolean("needToLog");
            JSONArray arr = new JSONArray(bundle.getString("upd"));
            int update_code = arr.getInt(0);
            for (Callback callback : callbacks) {
                callback.run(update_code, needToLog, peer_id, date, arr);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public interface Callback {
        void run(int update_code, boolean needToLog, int user_id, long date, JSONArray arr);
    }
}