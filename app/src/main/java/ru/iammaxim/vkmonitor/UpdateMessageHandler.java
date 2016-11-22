package ru.iammaxim.vkmonitor;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.util.ArrayList;

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

    @Override
    public void handleMessage(Message msg) {
        Bundle bundle = msg.getData();
        String date = bundle.getString("date");
        String time = bundle.getString("time");
        int update_code = bundle.getInt("update_code");
        int user_id = bundle.getInt("user_id");
        int[] args = bundle.getIntArray("args");
        for (Callback callback : callbacks) {
            callback.run(update_code, user_id, date, time, args);
        }
    }

    public interface Callback {
        void run(int update_code, int user_id, String date, String time, int[] args);
    }
}