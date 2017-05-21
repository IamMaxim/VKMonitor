package ru.iammaxim.vkmonitor.API.Users;

import java.io.IOException;

import ru.iammaxim.vkmonitor.Net;
import ru.iammaxim.vkmonitor.API.Objects.ObjectUser;

/**
 * Created by maxim on 18.08.2016.
 */
public class Users {
    public static ObjectUser get() {
        return UserDB.get();
    }

    public static ObjectUser get(int id) {
        if (id > 2000000000) {
            ObjectUser user = new ObjectUser();
            user.first_name = "Chat";
            user.last_name = String.valueOf(id - 2000000000);
            return user;
        }
        ObjectUser user = UserDB.get(id);
        if (user == null) {
            try {
                String json = Net.processRequest("users.get", true, "user_ids=" + id, "fields=photo_200");
                user = new ObjectUser(json);
                UserDB.add(user);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return user;
    }
}
