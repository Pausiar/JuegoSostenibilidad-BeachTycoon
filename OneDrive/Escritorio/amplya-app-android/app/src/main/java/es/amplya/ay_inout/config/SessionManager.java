package es.amplya.ay_inout.config;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import es.amplya.ay_inout.model.CheckInRecord;

// persiste sesiÃ³n de usuario
public class SessionManager {

    private static final String PREFS_NAME = "session";
    private static final String KEY_CHECKIN_HISTORY_PREFIX = "checkin_history_";
    private static final String KEY_SESSION_PASSWORD = "session_contrasenya";
    private static final String KEY_FCM_TOKEN = "fcm_token";
    private static final String KEY_PENDING_NOTIFICATION_COUNT = "pending_notification_count";
    private final SharedPreferences prefs;
    private final Gson gson;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public void setUsuario(String value) {
        prefs.edit().putString("usuario", value).apply();
    }

    public String getUsuario() {
        return prefs.getString("usuario", "");
    }

    public void setContrasenya(String value) {
        prefs.edit().putString("contrasenya", value).apply();
    }

    public String getContrasenya() {
        return prefs.getString("contrasenya", "");
    }

    public void setSessionContrasenya(String value) {
        prefs.edit().putString(KEY_SESSION_PASSWORD, value != null ? value : "").apply();
    }

    public String getSessionContrasenya() {
        return prefs.getString(KEY_SESSION_PASSWORD, "");
    }

        public void setIpgsbase(String value) {
            prefs.edit().putString("ipgsbase_session", value != null ? value : "").apply();
        }

        public String getIpgsbase() {
            return prefs.getString("ipgsbase_session", "");
        }

        public void setPuertogsbase(String value) {
            prefs.edit().putString("puertogsbase_session", value != null ? value : "").apply();
        }

        public String getPuertogsbase() {
            return prefs.getString("puertogsbase_session", "");
        }

        public void setGestgsbase(String value) {
            prefs.edit().putString("gestgsbase_session", value != null ? value : "").apply();
        }

        public String getGestgsbase() {
            return prefs.getString("gestgsbase_session", "");
        }

        public void setAplgsbase(String value) {
            prefs.edit().putString("aplgsbase_session", value != null ? value : "").apply();
        }

        public String getAplgsbase() {
            return prefs.getString("aplgsbase_session", "");
        }

        public void setEjagsbase(String value) {
            prefs.edit().putString("ejagsbase_session", value != null ? value : "").apply();
        }

        public String getEjagsbase() {
            return prefs.getString("ejagsbase_session", "");
        }
    public void setGuardarDatos(boolean value) {
        prefs.edit().putBoolean("guardar_datos", value).apply();
        if (!value) {
            prefs.edit().remove("contrasenya").apply();
        }
    }

    public boolean getGuardarDatos() {
        return prefs.getBoolean("guardar_datos", false);
    }

    public void setCodTrabajador(String value) {
        prefs.edit().putString("cod_trabajador", value).apply();
    }

    public String getCodTrabajador() {
        return prefs.getString("cod_trabajador", "");
    }

    public void setFcmToken(String value) {
        prefs.edit().putString(KEY_FCM_TOKEN, value != null ? value : "").apply();
    }

    public String getFcmToken() {
        return prefs.getString(KEY_FCM_TOKEN, "");
    }

    public int incrementPendingNotificationCount() {
        int nextCount = prefs.getInt(KEY_PENDING_NOTIFICATION_COUNT, 0) + 1;
        prefs.edit().putInt(KEY_PENDING_NOTIFICATION_COUNT, nextCount).apply();
        return nextCount;
    }

    public void clearPendingNotificationCount() {
        prefs.edit().putInt(KEY_PENDING_NOTIFICATION_COUNT, 0).apply();
    }

    public void setLastLoginAddress(String value) {
        prefs.edit().putString("last_login_address", value).apply();
    }

    public String getLastLoginAddress() {
        return prefs.getString("last_login_address", "");
    }

    public void setLastLoginCoordinates(String value) {
        prefs.edit().putString("last_login_coordinates", value).apply();
    }

    public String getLastLoginCoordinates() {
        return prefs.getString("last_login_coordinates", "");
    }

    public void clearLastLoginLocation() {
        prefs.edit()
                .remove("last_login_address")
                .remove("last_login_coordinates")
                .apply();
    }

    public void addCheckInRecord(CheckInRecord record) {
        List<CheckInRecord> records = getCheckInHistory();
        records.add(0, record);
        prefs.edit().putString(getCheckInHistoryKey(), gson.toJson(records)).apply();
    }

    public List<CheckInRecord> getCheckInHistory() {
        String historyKey = getCheckInHistoryKey();
        String json = prefs.getString(historyKey, "");
        if ((json == null || json.trim().isEmpty()) && !Objects.equals(historyKey, getLegacyCheckInHistoryKey())) {
            json = prefs.getString(getLegacyCheckInHistoryKey(), "");
        }
        if (json == null || json.trim().isEmpty()) {
            return new ArrayList<>();
        }

        Type listType = new TypeToken<List<CheckInRecord>>() { } .getType();
        List<CheckInRecord> records = gson.fromJson(json, listType);
        return records != null ? new ArrayList<>(records) : new ArrayList<>();
    }

    public void setCheckInHistory(List<CheckInRecord> records) {
        List<CheckInRecord> safeRecords = records != null ? new ArrayList<>(records) : new ArrayList<>();
        prefs.edit().putString(getCheckInHistoryKey(), gson.toJson(safeRecords)).apply();
    }

    public CheckInRecord getLatestCheckInRecord() {
        List<CheckInRecord> records = getCheckInHistory();
        return records.isEmpty() ? null : records.get(0);
    }

    private String getCheckInHistoryKey() {
        String workerCode = getCodTrabajador();
        if (workerCode != null && !workerCode.trim().isEmpty()) {
            return KEY_CHECKIN_HISTORY_PREFIX + workerCode.trim().toLowerCase(Locale.ROOT);
        }
        return getLegacyCheckInHistoryKey();
    }

    private String getLegacyCheckInHistoryKey() {
        String user = getUsuario();
        if (user == null || user.trim().isEmpty()) {
            return KEY_CHECKIN_HISTORY_PREFIX + "default";
        }
        return KEY_CHECKIN_HISTORY_PREFIX + user.trim().toLowerCase(Locale.ROOT);
    }

    // borra sesiÃ³n (respeta guardar_datos)
    public void logout() {
        boolean guardar = getGuardarDatos();
        SharedPreferences.Editor editor = prefs.edit()
                .remove("cod_trabajador")
                .remove("last_login_address")
                .remove("last_login_coordinates")
                .remove(KEY_SESSION_PASSWORD);

        if (!guardar) {
            editor.remove("guardar_datos")
                    .remove("usuario")
                    .remove("contrasenya");
        }
            // Clear GSBase session config
            editor.remove("ipgsbase_session")
                .remove("puertogsbase_session")
                .remove("gestgsbase_session")
                .remove("aplgsbase_session")
                .remove("ejagsbase_session");

        editor.apply();
    }
}

