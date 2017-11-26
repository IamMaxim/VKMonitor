package ru.iammaxim.vkmonitor.API.Messages;

/**
 * Created by maxim on 11/26/17.
 */

public interface MessageDumpCallback {
    void onUpdate(int curDialog, int totalDialogs, int curMessage, int totalMessages);
}
