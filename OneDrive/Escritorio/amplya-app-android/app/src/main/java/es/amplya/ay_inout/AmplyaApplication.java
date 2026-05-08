package es.amplya.ay_inout;

import android.app.Application;

import es.amplya.ay_inout.notifications.NotificationHelper;

public class AmplyaApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        NotificationHelper.ensureNotificationChannel(this);
    }
}