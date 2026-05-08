package es.amplya.ay_inout.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import es.amplya.ay_inout.R;

public final class NotificationHelper {

    private NotificationHelper() {}

    public static void ensureNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager == null) {
            return;
        }

        String channelId = context.getString(R.string.fcm_default_channel_id);
        NotificationChannel channel = new NotificationChannel(
                channelId,
                context.getString(R.string.fcm_default_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(context.getString(R.string.fcm_default_channel_description));
        channel.setShowBadge(true);
        manager.createNotificationChannel(channel);
    }
}