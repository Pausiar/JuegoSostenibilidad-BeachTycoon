package es.amplya.ay_inout.notifications;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

import es.amplya.ay_inout.R;
import es.amplya.ay_inout.config.SessionManager;
import es.amplya.ay_inout.ui.DocumentacionActivity;
import es.amplya.ay_inout.ui.MainActivity;
import es.amplya.ay_inout.ui.TareasHoyActivity;

public class AmplyaFirebaseMessagingService extends FirebaseMessagingService {

    private static final String ACTION_OPEN_DOC_ACTIVITY = "OPEN_DOC_ACTIVITY";
    private static final String ACTION_OPEN_TASKS_ACTIVITY = "OPEN_TASKS_ACTIVITY";

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        FcmTokenManager.persistToken(this, token);
    }

    @Override
    public void onMessageReceived(RemoteMessage message) {
        super.onMessageReceived(message);

        Map<String, String> data = new HashMap<>(message.getData());
        RemoteMessage.Notification remoteNotification = message.getNotification();

        String title = remoteNotification != null ? remoteNotification.getTitle() : null;
        String body = remoteNotification != null ? remoteNotification.getBody() : null;
        String clickAction = remoteNotification != null ? remoteNotification.getClickAction() : null;

        title = firstNonEmpty(title, data.get("title"), getString(R.string.app_name));
        body = firstNonEmpty(body, data.get("body"), "");
        clickAction = firstNonEmpty(clickAction, data.get("click_action"));

        if (!TextUtils.isEmpty(clickAction)) {
            data.put("click_action", clickAction);
        }

        showNotification(title, body, data);
    }

    private void showNotification(String title, String body, Map<String, String> data) {
        if (!canPostNotifications()) {
            return;
        }

        NotificationHelper.ensureNotificationChannel(this);

        int notificationId = (int) (System.currentTimeMillis() & 0x0fffffff);
        SessionManager sessionManager = new SessionManager(this);
        int pendingCount = sessionManager.incrementPendingNotificationCount();

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                notificationId,
                buildContentIntent(data),
                getPendingIntentFlags());

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                this,
                getString(R.string.fcm_default_channel_id))
                .setSmallIcon(R.drawable.ic_task_check)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                .setNumber(pendingCount)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat.from(this).notify(notificationId, builder.build());
    }

    private Intent buildContentIntent(Map<String, String> data) {
        Class<?> targetActivity = MainActivity.class;
        String clickAction = data.get("click_action");
        String alertType = data.get("tipo_alerta");

        if (ACTION_OPEN_DOC_ACTIVITY.equals(clickAction)
                || "documentacion_caducada".equalsIgnoreCase(alertType)) {
            targetActivity = DocumentacionActivity.class;
        } else if (ACTION_OPEN_TASKS_ACTIVITY.equals(clickAction)
                || "tarea".equalsIgnoreCase(alertType)
                || "tareas".equalsIgnoreCase(alertType)) {
            targetActivity = TareasHoyActivity.class;
        }

        Intent intent = new Intent(this, targetActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        for (Map.Entry<String, String> entry : data.entrySet()) {
            intent.putExtra(entry.getKey(), entry.getValue());
        }
        return intent;
    }

    private boolean canPostNotifications() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true;
        }
        return ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }

    private int getPendingIntentFlags() {
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        return flags;
    }

    private String firstNonEmpty(String... values) {
        for (String value : values) {
            if (!TextUtils.isEmpty(value)) {
                return value;
            }
        }
        return "";
    }
}