package ru.iammaxim.vkmonitor.API.Messages;

import ru.iammaxim.vkmonitor.API.Objects.ObjectMessage;

/**
 * Created by maxim on 11/26/17.
 */

public interface ForEachDumpedMessageCallback {
    void process(ObjectMessage msg);
}
