package ru.iammaxim.vkmonitor;

import android.os.Environment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import ru.iammaxim.vkmonitor.API.Users.Users;
import ru.iammaxim.vkmonitor.API.Objects.ObjectUser;

/**
 * Created by maxim on 15.09.2016.
 */
public class AccessTokenManager {
    public static final String tokensPath = Environment.getExternalStorageDirectory().getPath() + "/VKMonitor.tokens";
    public static ArrayList<Token> tokens = new ArrayList<>();
    private static Token activeToken;
    private static int activeTokenIndex;
    private static boolean loaded = false;

    public static String getAccessToken() {
        if (activeToken == null) return "";
        return activeToken.token;
    }

    public static Token getActiveToken() {
        return activeToken;
    }

    public static void setActiveToken(int index) {
        activeTokenIndex = index;
        activeToken = tokens.get(index);
    }

    public static void load() {
        if (loaded)
            return;
        tokens.clear();
        System.out.println("Loading tokens...");
        try {
            File file = new File(tokensPath);
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
                    tokens.add(t);
                    if (isActive) setActiveToken(tokens.size() - 1);
                }
            }
            loaded = true;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    public static void add(String name, String token) {
        tokens.add(new Token(name, token, false));
    }

    public static void save() {
        System.out.println("Saving tokens...");
        try {
            File file = new File(tokensPath);
            if (!file.exists())
                file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            for (Token t : tokens) {
                if (!t.name.isEmpty() && !t.token.isEmpty()) {
                    fos.write((new JSONObject().put("name", t.name).put("token", t.token).put("active", t.isActive).toString() + '\n').getBytes());
                }
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    public static int getActiveTokenIndex() {
        return activeTokenIndex;
    }

    public static class Token {
        public boolean isActive;
        public String name, token;

        public Token(String name, String token, boolean isActive) {
            this.name = name;
            this.token = token;
            this.isActive = isActive;
        }

        @Override
        public String toString() {
            return name + " " + token + " " + isActive;
        }
    }
}
