package ru.iammaxim.vkmonitor;

import android.os.Environment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by maxim on 15.09.2016.
 */
public class AccessTokenManager {
    public static HashMap<String, Token> tokens = new HashMap<>();
    private static Token activeToken;

    public static String getAccessToken() {
        if (activeToken == null) return "";
        return activeToken.token;
    }

    static {
        try {
            File file = new File(App.tokensPath);
            if (!file.exists())
                file.createNewFile();
            else {
                Scanner scanner = new Scanner(file);
                while (scanner.hasNext()) {
                    String s = scanner.nextLine();
                    JSONObject o = new JSONObject(s);
                    String name = o.getString("name");
                    String token = o.getString("token");
                    boolean isActive = o.getBoolean("active");
                    Token t = new Token(name, token, isActive);
                    tokens.put(name, t);
                    if (isActive) activeToken = t;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void add(String name, String token) {
        tokens.put(name, new Token(name, token, false));
    }

    public static void save() {
        try {
            File file = new File(App.tokensPath);
            if (!file.exists())
                file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            for (Map.Entry<String, Token> entry : tokens.entrySet()) {
                fos.write((new JSONObject().put("name", entry.getValue().name).put("token", entry.getValue().token).put("active", entry.getValue().isActive).toString() + '\n').getBytes());
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    public static class Token {
        public boolean isActive;
        public String name, token;

        public Token(String name, String token, boolean isActive) {
            this.name = name;
            this.token = token;
            this.isActive = isActive;
        }
    }
}
