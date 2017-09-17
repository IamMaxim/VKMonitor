package ru.iammaxim.vkmonitor.API.Messages;

import android.util.SparseArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import ru.iammaxim.vkmonitor.API.Objects.ObjectDialog;
import ru.iammaxim.vkmonitor.API.Objects.ObjectMessage;
import ru.iammaxim.vkmonitor.App;
import ru.iammaxim.vkmonitor.Net;

/**
 * Created by maxim on 5/21/17.
 */

public class Messages {
    public static ArrayList<ObjectDialog> dialogObjects = new ArrayList<>();
    public static SparseArray<Dialog> dialogs = new SparseArray<>();
    public static int dialogsCount = 0;
    public static ArrayList<OnMessagesUpdate> messageCallbacks = new ArrayList<>();
    public static ArrayList<OnDialogsUpdate> dialogCallbacks = new ArrayList<>();
    private static boolean needToUpdateDialogs = true;
    private static final Object sendLock = new Object();

    public static void setNeedToUpdateDialogs() {
        needToUpdateDialogs = true;
    }

    public static MessagesObject getHistory(int peer_id, int count, int offset) throws IOException, JSONException {
        JSONObject object = new JSONObject(Net.processRequest("messages.getHistory", true, "peer_id=" + peer_id, "count=" + count, "offset=" + offset)).getJSONObject("response");
        int _count = object.getInt("count");
        JSONArray arr = object.getJSONArray("items");
        ArrayList<ObjectMessage> msgs = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            msgs.add(new ObjectMessage(arr.getJSONObject(i)));
        }
        return new MessagesObject(_count, msgs);
    }

    public static void send(ObjectMessage msg) throws IOException {
        synchronized (sendLock) {
            // TODO: implement attachments support
            Net.processRequest("messages.send", true,
                    "peer_id=" + msg.peer_id,
                    "message=" + msg.body,
                    "random_id=" + msg.random_id);
        }
    }

    public static void processLongPollMessage(int update_code, JSONArray arr) {
        try {
            switch (update_code) {
                case 1:
                case 2:
                case 3:
                    updateFlags(update_code, arr);
                    break;
                case 4:
                    newMessage(arr);
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void newMessage(JSONArray arr) throws JSONException, IOException {
        ReturnPair ret = getDialogIndexByPeerID(arr.getInt(3));
        if (ret.index == -1)
            return;
        ObjectDialog dialog = dialogObjects.remove(ret.index);
        dialog.message.id = arr.getInt(1);
        dialog.message.flags = arr.getInt(2);
        dialog.message.processFlags();
        if (!ret.fullyUpdated)
            dialog.updateUnread();
        dialog.message.date = arr.getLong(4) * 1000;
        dialog.message.body = arr.getString(6);
        JSONObject attachments = arr.getJSONObject(7);
        if (attachments.has("from"))
            dialog.message.user_id = attachments.getInt("from");
        dialog.message.random_id = arr.getInt(8);
        dialogObjects.add(0, dialog);

        App.handler.post(() -> {
            for (OnMessagesUpdate c : messageCallbacks) {
                c.onMessageGet(ret.index, dialog.message);
            }
        });
    }

    private static void updateFlags(int update_code, JSONArray arr) throws JSONException, IOException {
        App.handler.post(() -> {
            for (OnMessagesUpdate c : messageCallbacks) {
                try {
                    c.onMessageFlagsUpdated(arr.getInt(1), update_code, arr.getInt(2));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        ReturnPair ret = getDialogIndexByMessageID(arr.getInt(1));
        if (ret.index == -1)
            return;
        ObjectDialog dialog = dialogObjects.get(ret.index);
        dialog.message.applyFlags(update_code, arr.getInt(2));
        dialog.message.processFlags();
        if (!ret.fullyUpdated && dialog.message.read_state)
            dialog.updateUnread();

        App.handler.post(() -> {
            for (OnDialogsUpdate c : dialogCallbacks) {
                c.onDialogFlagsUpdated(ret.index);
            }
        });
    }

    /**
     * @param peer_id peer_id of needed dialog
     * @return ReturnPair(true if dialogs were fully updated,
     *index of dialog or -1 if no such dialog found
     */
    private static ReturnPair getDialogIndexByPeerID(int peer_id) throws IOException, JSONException {
        updateDialogs();

        int i;
        for (i = 0; i < dialogObjects.size(); i++)
            if (dialogObjects.get(i).message.peer_id == peer_id)
                return new ReturnPair(false, i);

        dialogObjects.clear();
        i = 0;
        while (true) {
            JSONArray arr = new JSONObject(
                    Net.processRequest("messages.getDialogs", true, "count=200", "offset=" + i))
                    .getJSONObject("response")
                    .getJSONArray("items");
            i += 200;

            int j;
            ObjectDialog toReturn = null;
            for (j = 0; j < arr.length(); j++) {
                ObjectDialog dialog = new ObjectDialog(arr.getJSONObject(j));
                dialogObjects.add(dialog);
                if (dialog.message.peer_id == peer_id)
                    toReturn = dialog;
            }
            if (toReturn != null)
                return new ReturnPair(true, dialogObjects.indexOf(toReturn));

            if (j == 0)
                return new ReturnPair(true, -1);
        }
    }

    /**
     * @param message_id ID of message
     * @return ReturnPair(true if dialogs were fully updated,
     *index of dialog or -1 if no such dialogs
     */
    public static ReturnPair getDialogIndexByMessageID(int message_id) throws IOException, JSONException {
        updateDialogs();

        int i;
        for (i = 0; i < dialogObjects.size(); i++) {
            if (dialogObjects.get(i).message.id == message_id)
                return new ReturnPair(false, i);
        }

        dialogObjects.clear();
        i = 0;
        while (true) {
            JSONArray arr = new JSONObject(
                    Net.processRequest("messages.getDialogs", true, "count=200", "offset=" + i))
                    .getJSONObject("response")
                    .getJSONArray("items");
            i += 200;

            int j;
            ObjectDialog toReturn = null;
            for (j = 0; j < arr.length(); j++) {
                ObjectDialog dialog = new ObjectDialog(arr.getJSONObject(j));
                dialogObjects.add(dialog);
                if (dialog.message.id == message_id)
                    toReturn = dialog;
            }
            if (toReturn != null)
                return new ReturnPair(true, dialogObjects.indexOf(toReturn));

            if (j == 0)
                return new ReturnPair(true, -1);
        }
    }

    public static void updateDialogs() {
        if (!needToUpdateDialogs)
            return;
        try {
            dialogObjects.clear();
            JSONObject o = new JSONObject(Net.processRequest("messages.getDialogs", true, "count=20")).getJSONObject("response");
            dialogsCount = o.getInt("count");
            JSONArray arr = o.getJSONArray("items");
            for (int i = 0; i < arr.length(); i++) {
                ObjectDialog dialog = new ObjectDialog(arr.getJSONObject(i));
                dialogObjects.add(dialog);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public interface OnMessagesUpdate {
        void onMessageGet(int prevDialogIndex, ObjectMessage msg);

        //        void onMessageFlagsUpdated(int dialogIndex, ObjectMessage msg);
        void onMessageFlagsUpdated(int message_id, int update_code, int flags);
    }

    public interface OnDialogsUpdate {
        /** Local index of updated dialog. Entry is processed automatically */
        void onDialogFlagsUpdated(int dialogIndex);
    }

    public static class MessagesObject {
        /**
         * total messages count
         * NOTE: this is not messages.size()!
         */
        public int count;
        public ArrayList<ObjectMessage> messages;

        public MessagesObject(int count, ArrayList<ObjectMessage> messages) {
            this.count = count;
            this.messages = messages;
        }
    }

    public static class ReturnPair {
        public boolean fullyUpdated;
        public int index;

        public ReturnPair(boolean fullyUpdated, int index) {
            this.fullyUpdated = fullyUpdated;
            this.index = index;
        }
    }
}
