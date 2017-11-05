package ru.iammaxim.vkmonitor;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

/**
 * Created by maxim on 9/18/2017.
 */

public class Notifications {
    public static void send(Context context, String title, String text, Bitmap icon) {
        send((int) (Math.random() * Integer.MAX_VALUE), context, title, text, icon);
    }

    public static void send(int id, Context context, String title, String text, Bitmap icon) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "VKMonitor");
        builder.setSmallIcon(R.mipmap.icon);
        builder.setColor(context.getResources().getColor(R.color.colorPrimary));
        builder.setContentTitle(title);
        builder.setContentText(text);
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        builder.setSound(sound);
        long[] pattern = {300,
                300,
                300,
                300,
                300,
                300,
                300,
        };
        builder.setVibrate(pattern);

        if (icon != null)
            builder.setLargeIcon(icon);
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(id, builder.build());
    }

    public static void send(Context context, String title, String text) {
        send(context, title, text, null);
    }
}
