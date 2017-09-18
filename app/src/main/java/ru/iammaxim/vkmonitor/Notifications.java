package ru.iammaxim.vkmonitor;

import android.app.Notification;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.app.NotificationCompat;

/**
 * Created by maxim on 9/18/2017.
 */

public class Notifications {
    public static void send(Context context, String title, String text, Bitmap icon) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "VKMonitor");
        builder.setSmallIcon(R.mipmap.icon);
        builder.setColor(context.getResources().getColor(R.color.colorPrimary));
        builder.setContentTitle(title);
        builder.setContentText(text);
        if (icon != null)
            builder.setLargeIcon(icon);
    }

    public static void send(Context context, String title, String text) {
        send(context, title, text, null);
    }
}
