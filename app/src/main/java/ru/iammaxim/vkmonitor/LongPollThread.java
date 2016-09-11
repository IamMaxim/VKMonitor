package ru.iammaxim.vkmonitor;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by Maxim on 21.06.2016.
 */
public class LongPollThread extends Thread {
    private ObjectLongPollServer currentLongPollServer;

    private void init() throws JSONException {
        try {
            currentLongPollServer = ObjectLongPollServer.getServer(Net.processRequest("messages.getLongPollServer", true, "use_ssl=1", "need_pts=1"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public LongPollThread(String name) {
        super(name);
    }

    @Override
    public void run() {
        System.out.println("starting long poll thread...");
        try {
            UserDB.load();
            UserDB.startSaveThread();
            init();
            while (!isInterrupted()) {
                processLongPollMessage();
            }
            UserDB.saveThread.interrupt();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println("Shutting down long poll thread...");
    }

    private String getStringStart(int user_id) {
        if (user_id >= 2000000000)
            return "Chat " + (user_id - 2000000000);
        else
            return "User " + Users.get(user_id);
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
                    currentLongPollServer = ObjectLongPollServer.getServer(Net.processRequest("messages.getLongPollServer", true, "use_ssl=1", "need_pts=1"));
                    System.out.println("Server data updated.");
                    return;
                }
            }
            long ts = o.getLong("ts");
            JSONArray arr = o.getJSONArray("updates");

            for (int i = 0; i < arr.length(); i++) {
                JSONArray obj = (JSONArray) arr.get(i);
                System.out.println(obj.toString());
                int updateCode = obj.getInt(0);
                switch (updateCode) {
                    /**
                     * $peer_id (integer) $local_id (integer)
                     * Прочтение всех входящих сообщений с $peer_id вплоть до $local_id включительно.
                     */
                    case 6:
                        App.addToLog(obj.getInt(1), 6, obj.getInt(2));
                        App.log("You have read in messages with " + obj.getInt(1) + " upto message #" + obj.getInt(2));
                        break;
                    /**
                     * $peer_id (integer) $local_id (integer)
                     * Прочтение всех исходящих сообщений с $peer_id вплоть до $local_id включительно.
                     */
                    case 7:
                        App.addToLog(obj.getInt(1), 7, obj.getInt(2));
                        App.log("Out messages with " + obj.getInt(1) + " have been read upto message #" + obj.getInt(2));
                        break;
                    /**
                     * -$user_id (integer) $extra (integer)
                     * Друг $user_id стал онлайн. $extra не равен 0, если в mode был передан флаг 64. В младшем байте (остаток от деления на 256) числа extra лежит идентификатор платформы.
                     */
                    case 8:
                        App.addToLog(-obj.getInt(1), 8);
                        App.log(getStringStart(-obj.getInt(1)) + " became online");
                        break;
                    /**
                     * -$user_id (integer) $flags (integer)
                     * Друг $user_id стал оффлайн ($flags равен 0, если пользователь покинул сайт (например, нажал выход) и 1, если оффлайн по таймауту (например, статус away)) .
                     */
                    case 9:
                        App.addToLog(-obj.getInt(1), 9, obj.getInt(2));
                        App.log(getStringStart(-obj.getInt(1)) + " became offine (" + (obj.getInt(2) == 0 ? "force quit" : "timeout") + ")");
                        break;
                    /**
                     * $user_id (integer) $flags (integer)
                     * Пользователь $user_id начал набирать текст в диалоге. Событие должно приходить раз в ~5 секунд при постоянном наборе текста. $flags = 1.
                     */
                    case 61:
                        App.addToLog(obj.getInt(1), 61);
                        App.log(getStringStart(obj.getInt(1)) + " started typing message");
                        break;
                    /**
                     * $user_id (integer) $chat_id (integer)
                     * Пользователь $user_id начал набирать текст в беседе $chat_id.
                     */
                    case 62:
                        App.addToLog(obj.getInt(1), 62, obj.getInt(2));
                        App.log(getStringStart(obj.getInt(1)) + " started typing in chat #" + obj.getInt(2));
                }
            }
            currentLongPollServer.update(ts);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
