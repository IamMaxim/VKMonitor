package ru.iammaxim.vkmonitor;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.util.Scanner;

public class Net {
    private static final String version = "?v=5.64";

    public static String processRequest(String url) throws IOException {
        StringBuilder sb = new StringBuilder();
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        try {
            Scanner scanner = new Scanner(connection.getInputStream());
            while (scanner.hasNext()) {
                sb.append(scanner.nextLine());
            }
            if (sb.toString().contains("error_code")) {
                int attempts = 5;
                do {
                    try {
                        Thread.sleep(1000);
                        sb.delete(0, sb.length());
                        connection = (HttpURLConnection) new URL(url).openConnection();
                        scanner = new Scanner(connection.getInputStream());
                        while (scanner.hasNext()) {
                            sb.append(scanner.nextLine());
                        }
                    } catch (InterruptedException | SocketException e) {
                        e.printStackTrace();
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                        return processRequest(url);
                    }
                    attempts--;
                } while (sb.toString().contains("error_code") && attempts > 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    //example: messages.getDialogs true count=1, offset=0
    public static String processRequest(String groupAndName, boolean useAccessToken, String... keysAndValues) throws IOException {
        StringBuilder sb = new StringBuilder("https://api.vk.com/method/");
        sb.append(groupAndName).append(version);
        if (useAccessToken) {
            sb.append("&access_token=").append(App.getAccessToken());
        }
        for (String s : keysAndValues) {
            sb.append("&").append(s.replace(" ", "%20"));
        }
        return processRequest(sb.toString());
    }
}
