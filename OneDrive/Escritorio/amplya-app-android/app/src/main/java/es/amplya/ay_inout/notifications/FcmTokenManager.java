package es.amplya.ay_inout.notifications;

import android.content.Context;
import android.text.TextUtils;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;

import es.amplya.ay_inout.config.SessionManager;

public final class FcmTokenManager {

    public interface TokenCallback {
        void onTokenReady(String token);
    }

    private FcmTokenManager() {}

    public static void fetchAndPersistToken(Context context, TokenCallback callback) {
        Context appContext = context.getApplicationContext();
        SessionManager sessionManager = new SessionManager(appContext);

        if (!isFirebaseConfigured(appContext)) {
            callback.onTokenReady(sessionManager.getFcmToken());
            return;
        }

        try {
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                String token = null;
                if (task.isSuccessful()) {
                    token = task.getResult();
                    persistToken(appContext, token);
                }
                if (TextUtils.isEmpty(token)) {
                    token = sessionManager.getFcmToken();
                }
                callback.onTokenReady(token);
            });
        } catch (IllegalStateException ignored) {
            callback.onTokenReady(sessionManager.getFcmToken());
        }
    }

    public static void persistToken(Context context, String token) {
        if (TextUtils.isEmpty(token)) {
            return;
        }
        new SessionManager(context.getApplicationContext()).setFcmToken(token);
    }

    private static boolean isFirebaseConfigured(Context context) {
        try {
            return !FirebaseApp.getApps(context).isEmpty();
        } catch (IllegalStateException ignored) {
            return false;
        }
    }
}