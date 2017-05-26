package ru.iammaxim.vkmonitor.API.Groups;

import android.util.SparseArray;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import ru.iammaxim.vkmonitor.API.Objects.ObjectGroup;
import ru.iammaxim.vkmonitor.Net;

/**
 * Created by maxim on 5/21/17.
 */

public class Groups {
    public static SparseArray<ObjectGroup> groupObjects = new SparseArray<>();

    public static ObjectGroup getById(int group_id) {
        ObjectGroup g = groupObjects.get(group_id);
        if (g != null)
            return g;
        try {
            g = new ObjectGroup(new JSONObject(Net.processRequest("groups.getById", true, "group_ids=" + group_id)).getJSONArray("response").getJSONObject(0));
            groupObjects.put(group_id, g);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return g;
    }
}
