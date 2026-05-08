package es.amplya.ay_inout.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.location.LocationManagerCompat;
import androidx.core.os.CancellationSignal;
import androidx.core.util.Consumer;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import es.amplya.ay_inout.R;
import es.amplya.ay_inout.api.ApiClient;
import es.amplya.ay_inout.api.AuthService;
import es.amplya.ay_inout.api.OpenStreetMapService;
import es.amplya.ay_inout.api.ObrasService;
import es.amplya.ay_inout.api.SafeCallback;
import es.amplya.ay_inout.config.ConfigManager;
import es.amplya.ay_inout.config.SessionManager;
import es.amplya.ay_inout.model.OpenStreetMapReverseResponse;
import es.amplya.ay_inout.model.KillUserRequest;
import es.amplya.ay_inout.model.KillUserResponse;
import es.amplya.ay_inout.model.LoginRequest;
import es.amplya.ay_inout.notifications.FcmTokenManager;
import es.amplya.ay_inout.utils.PermissionUtils;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// pantalla de login
public class LoginActivity extends AppCompatActivity {

    private static final long LOCATION_TIMEOUT_MS = 6000L;

    private TextInputEditText etUsuario;
    private TextInputEditText etContrasenya;
    private TextInputLayout tilUsuario;
    private TextInputLayout tilContrasenya;
    private MaterialSwitch switchGuardar;
    private MaterialButton btnAcceder;
    private SessionManager session;
    private final Handler locationTimeoutHandler = new Handler(Looper.getMainLooper());
    private CancellationSignal locationCancellationSignal;
    private String pendingCodTrabajador;
        private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> { });
    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                if (PermissionUtils.hasLocationPermission(this)) {
                    resolveLoginLocationAndContinue();
                } else {
                    persistLoginLocationFallback(
                            getString(R.string.login_location_unavailable),
                            getString(R.string.login_location_permission_denied));
                    openMainAndFinish();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // TODO: restaurar activaciÃ³n real cuando estÃ© el endpoint
        // ActivationManager activation = new ActivationManager(this);
        // if (!activation.isActivated()) { startActivity(...); finish(); return; }
        // ApiKeyInterceptor.setActivationCode(activation.getActivationCode());

        setContentView(R.layout.activity_login);

        View topBar = findViewById(R.id.loginTopBar);
        View footer = findViewById(R.id.loginFooter);
        int topBarPaddingLeft = topBar.getPaddingLeft();
        int topBarPaddingTop = topBar.getPaddingTop();
        int topBarPaddingRight = topBar.getPaddingRight();
        int topBarPaddingBottom = topBar.getPaddingBottom();
        int footerPaddingLeft = footer.getPaddingLeft();
        int footerPaddingTop = footer.getPaddingTop();
        int footerPaddingRight = footer.getPaddingRight();
        int footerPaddingBottom = footer.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            topBar.setPadding(
                topBarPaddingLeft,
                topBarPaddingTop + systemBars.top,
                topBarPaddingRight,
                topBarPaddingBottom);
            footer.setPadding(
                footerPaddingLeft,
                footerPaddingTop,
                footerPaddingRight,
                footerPaddingBottom + systemBars.bottom);
            return insets;
        });

        session = new SessionManager(this);
        requestNotificationPermissionIfNeeded();

        etUsuario = findViewById(R.id.etUsuario);
        etContrasenya = findViewById(R.id.etContrasenya);
        tilUsuario = findViewById(R.id.tilUsuario);
        tilContrasenya = findViewById(R.id.tilContrasenya);
        switchGuardar = findViewById(R.id.switchGuardar);
        btnAcceder = findViewById(R.id.btnAcceder);

        // restaurar datos guardados
        if (session.getGuardarDatos()) {
            etUsuario.setText(session.getUsuario());
            etContrasenya.setText(session.getContrasenya());
            switchGuardar.setChecked(true);
        }

        View btnLoginConfig = findViewById(R.id.btnLoginConfig);
        if (btnLoginConfig != null) {
            btnLoginConfig.setOnClickListener(v ->
                    startActivity(new Intent(LoginActivity.this, ConfigActivity.class)));
        }

        btnAcceder.setOnClickListener(v -> login());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_config) {
            startActivity(new Intent(this, ConfigActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void login() {
        String usuario = etUsuario.getText().toString().trim();
        String contrasenya = etContrasenya.getText().toString().trim();

        boolean valido = true;

        tilUsuario.setError(null);
        tilContrasenya.setError(null);

        if (TextUtils.isEmpty(usuario)) {
            tilUsuario.setError(getString(R.string.campo_requerido));
            valido = false;
        }
        if (TextUtils.isEmpty(contrasenya)) {
            tilContrasenya.setError(getString(R.string.campo_requerido));
            valido = false;
        }
        if (!valido) return;

        // guardar si toggle activo
        session.setGuardarDatos(switchGuardar.isChecked());
        session.setUsuario(usuario);
        session.setSessionContrasenya(contrasenya);
        if (switchGuardar.isChecked()) {
            session.setContrasenya(contrasenya);
        }

        btnAcceder.setEnabled(false);
        btnAcceder.setText(R.string.login_in_progress);

        // si hay sesiÃ³n previa, kill-user antes de login
        String codPrevio = session.getCodTrabajador();
        if (!TextUtils.isEmpty(codPrevio)) {
            killUserYLogin(codPrevio, usuario, contrasenya);
        } else {
            doLogin(usuario, contrasenya);
        }
    }

    // POST https://obras.amplya.es/ws-obras/kill-user.php
    private void killUserYLogin(String codTrabajador, String usuario, String contrasenya) {
        ObrasService obrasService = ApiClient.getObrasInstance().create(ObrasService.class);
        KillUserRequest killReq = new KillUserRequest(codTrabajador);

        obrasService.killUser(killReq).enqueue(new SafeCallback<KillUserResponse>() {
            @Override
            public void onSuccess(KillUserResponse data) {
                doLogin(usuario, contrasenya);
            }

            @Override
            public void onError(String message) {
                doLogin(usuario, contrasenya);
            }
        });
    }

    private void doLogin(String usuario, String contrasenya) {
        FcmTokenManager.fetchAndPersistToken(this,
            fcmToken -> doLoginWithToken(usuario, contrasenya, fcmToken));
        }

        private void doLoginWithToken(String usuario, String contrasenya, String fcmToken) {
        // POST https://inout.amplya.es/login.php
        ConfigManager config = new ConfigManager(this);
        AuthService authService = ApiClient.getInoutInstance().create(AuthService.class);
        LoginRequest request = new LoginRequest(usuario, contrasenya,
                config.getIpgsbase(), config.getPuertogsbase(),
                config.getGestgsbase(), config.getAplgsbase(),
            config.getEjagsbase(), fcmToken);

        authService.login(request).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                String rawBody = readBody(response.body());
                String rawError = readBody(response.errorBody());

                String payload = !TextUtils.isEmpty(rawBody) ? rawBody : rawError;
                LoginResult loginResult = parseLoginResult(payload);

                if (response.isSuccessful() && loginResult.usuarioValido) {
                    session.setIpgsbase(config.getIpgsbase());
                    session.setPuertogsbase(config.getPuertogsbase());
                    session.setGestgsbase(config.getGestgsbase());
                    session.setAplgsbase(config.getAplgsbase());
                    session.setEjagsbase(config.getEjagsbase());
                    completeLogin(loginResult.codTrabajador);
                } else {
                    String msg = loginResult.mensaje;
                    if (TextUtils.isEmpty(msg) && !response.isSuccessful()) {
                        msg = "error " + response.code();
                    }
                    Toast.makeText(LoginActivity.this,
                            msg != null ? msg : getString(R.string.login_error_invalid_credentials),
                            Toast.LENGTH_LONG).show();
                    resetBoton();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                String message;
                if (t instanceof java.net.SocketTimeoutException) {
                    message = getString(R.string.error_timeout_connection);
                } else if (t instanceof java.net.UnknownHostException) {
                    message = getString(R.string.error_no_internet);
                } else if (t instanceof IOException) {
                    message = "error de red";
                } else {
                    message = t.getMessage();
                }
                Toast.makeText(LoginActivity.this,
                    normalizeServerMessage(TextUtils.isEmpty(message) ? "error inesperado" : message),
                        Toast.LENGTH_LONG).show();
                resetBoton();
            }
        });
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    private String readBody(ResponseBody body) {
        if (body == null) {
            return null;
        }
        try {
            return body.string();
        } catch (IOException ignored) {
            return null;
        }
    }

    private LoginResult parseLoginResult(String payload) {
        LoginResult result = new LoginResult();
        if (TextUtils.isEmpty(payload)) {
            return result;
        }

        String trimmed = normalizeServerMessage(payload.trim());
        try {
            JsonElement root = JsonParser.parseString(trimmed);
            if (root.isJsonObject()) {
                JsonObject object = root.getAsJsonObject();
                JsonObject nested = getNestedObject(object, "dt_envia");
                String usuarioValido = firstNonEmpty(
                    getJsonString(object, "usuario_valido"),
                    getJsonString(nested, "usuario_valido"));
                String status = firstNonEmpty(
                    getJsonString(object, "status"),
                    getJsonString(nested, "status"));
                result.codTrabajador = firstNonEmpty(
                    normalizeServerMessage(getJsonString(object, "cod_trabajador")),
                    normalizeServerMessage(getJsonString(nested, "cod_trabajador")),
                    normalizeServerMessage(getJsonString(object, "codigo_usuario")),
                    normalizeServerMessage(getJsonString(nested, "codigo_usuario")),
                    normalizeServerMessage(getJsonString(object, "codigoUsuario")),
                    normalizeServerMessage(getJsonString(nested, "codigoUsuario")),
                    normalizeServerMessage(getJsonString(object, "codigo_trabajador")),
                    normalizeServerMessage(getJsonString(nested, "codigo_trabajador")),
                    normalizeServerMessage(getJsonString(object, "codTrabajador")),
                    normalizeServerMessage(getJsonString(nested, "codTrabajador")),
                    normalizeServerMessage(getJsonString(object, "cod_trab")),
                    normalizeServerMessage(getJsonString(nested, "cod_trab")),
                    normalizeServerMessage(getJsonString(object, "codtrabajador")),
                    normalizeServerMessage(getJsonString(nested, "codtrabajador")),
                    normalizeServerMessage(getJsonString(object, "cod")),
                    normalizeServerMessage(getJsonString(nested, "cod"))
                );
                result.mensaje = firstNonEmpty(
                    normalizeServerMessage(getJsonString(object, "mensaje")),
                    normalizeServerMessage(getJsonString(nested, "mensaje")),
                    normalizeServerMessage(getJsonString(object, "message")),
                    normalizeServerMessage(getJsonString(nested, "message")),
                    normalizeServerMessage(getJsonString(object, "error")));

                result.usuarioValido = isPositiveFlag(usuarioValido) || isPositiveFlag(status);
                return result;
            }
        } catch (Exception ignored) {
            // si no es JSON vÃ¡lido, usamos el texto recibido como mensaje.
        }

        result.mensaje = trimmed;
        result.usuarioValido = isPositiveFlag(trimmed);
        return result;
    }

    private String getJsonString(JsonObject object, String key) {
        if (object == null || !object.has(key) || object.get(key).isJsonNull()) {
            return null;
        }
        try {
            JsonElement value = object.get(key);
            if (value instanceof JsonPrimitive) {
                return value.getAsString();
            }
            return value.toString();
        } catch (Exception ignored) {
            return null;
        }
    }

    private JsonObject getNestedObject(JsonObject object, String key) {
        if (object == null || !object.has(key) || object.get(key).isJsonNull()) {
            return null;
        }
        try {
            JsonElement value = object.get(key);
            if (value.isJsonObject()) {
                return value.getAsJsonObject();
            }

            // Some backends return nested payload as an escaped JSON string.
            if (value.isJsonPrimitive()) {
                JsonPrimitive primitive = value.getAsJsonPrimitive();
                if (primitive.isString()) {
                    String nestedRaw = normalizeServerMessage(primitive.getAsString());
                    if (!TextUtils.isEmpty(nestedRaw)) {
                        JsonElement nestedElement = JsonParser.parseString(nestedRaw.trim());
                        if (nestedElement.isJsonObject()) {
                            return nestedElement.getAsJsonObject();
                        }
                    }
                }
            }
            return null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private String firstNonEmpty(String... values) {
        for (String value : values) {
            if (!TextUtils.isEmpty(value)) {
                return value;
            }
        }
        return null;
    }

    private boolean isPositiveFlag(String value) {
        if (TextUtils.isEmpty(value)) {
            return false;
        }
        return "ok".equalsIgnoreCase(value)
                || "success".equalsIgnoreCase(value)
                || "true".equalsIgnoreCase(value)
                || "1".equals(value);
    }

    private String normalizeServerMessage(String raw) {
        if (raw == null) {
            return null;
        }

        String normalized = raw
                .replace("\\/", "/")
                .replace("/u", "\\u");

        Pattern pattern = Pattern.compile("\\\\u([0-9a-fA-F]{4,6})");
        Matcher matcher = pattern.matcher(normalized);
        StringBuffer output = new StringBuffer();
        while (matcher.find()) {
            String hex = matcher.group(1);
            if (hex.length() > 4) {
                hex = hex.substring(hex.length() - 4);
            }

            String replacement;
            try {
                int codePoint = Integer.parseInt(hex, 16);
                replacement = String.valueOf((char) codePoint);
            } catch (Exception ignored) {
                replacement = matcher.group(0);
            }
            matcher.appendReplacement(output, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(output);
        return fixCommonMojibake(output.toString());
    }

    private String fixCommonMojibake(String value) {
        if (value == null) {
            return null;
        }
        return value
                .replace("Ã¡", "á")
                .replace("Ã©", "é")
                .replace("Ã­", "í")
                .replace("Ã³", "ó")
                .replace("Ãº", "ú")
                .replace("Ã", "Á")
                .replace("Ã‰", "É")
                .replace("Ã", "Í")
                .replace("Ã“", "Ó")
                .replace("Ãš", "Ú")
                .replace("Ã±", "ñ")
                .replace("Ã‘", "Ñ")
                .replace("Â¿", "¿")
                .replace("Â¡", "¡");
    }

    private static class LoginResult {
        boolean usuarioValido;
        String codTrabajador = "";
        String mensaje;
    }

    private String normalizeCodeValue(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty() || "null".equalsIgnoreCase(trimmed)) {
            return "";
        }
        return trimmed;
    }

    private void resetBoton() {
        btnAcceder.setEnabled(true);
        btnAcceder.setText(R.string.acceder);
    }

    private void completeLogin(String codTrabajador) {
        String parsedCode = normalizeCodeValue(codTrabajador);
        String currentUser = normalizeCodeValue(session.getUsuario());

        // Never reuse stale worker code from a previous user session.
        pendingCodTrabajador = !TextUtils.isEmpty(parsedCode)
            ? parsedCode
            : currentUser;
        session.setCodTrabajador(pendingCodTrabajador);
        session.clearLastLoginLocation();

        if (PermissionUtils.hasLocationPermission(this)) {
            resolveLoginLocationAndContinue();
        } else {
            locationPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void resolveLoginLocationAndContinue() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager == null) {
            persistLoginLocationFallback(
                    getString(R.string.login_location_unavailable),
                    getString(R.string.login_location_permission_denied));
            openMainAndFinish();
            return;
        }

        Location cachedLocation = getBestLastKnownLocation(locationManager);
        if (cachedLocation != null) {
            reverseGeocodeAndContinue(cachedLocation);
            return;
        }

        String provider = getBestProvider(locationManager);
        if (provider == null) {
            persistLoginLocationFallback(
                    getString(R.string.login_location_unavailable),
                    getString(R.string.login_location_permission_denied));
            openMainAndFinish();
            return;
        }

        locationCancellationSignal = new CancellationSignal();
        Runnable timeoutRunnable = () -> {
            if (locationCancellationSignal != null) {
                locationCancellationSignal.cancel();
                locationCancellationSignal = null;
            }
            persistLoginLocationFallback(
                    getString(R.string.login_location_unavailable),
                    getString(R.string.login_location_permission_denied));
            openMainAndFinish();
        };
        locationTimeoutHandler.postDelayed(timeoutRunnable, LOCATION_TIMEOUT_MS);

        LocationManagerCompat.getCurrentLocation(
                locationManager,
                provider,
                locationCancellationSignal,
                ContextCompat.getMainExecutor(this),
                new Consumer<Location>() {
                    @Override
                    public void accept(Location location) {
                        locationTimeoutHandler.removeCallbacks(timeoutRunnable);
                        locationCancellationSignal = null;
                        if (location != null) {
                            reverseGeocodeAndContinue(location);
                        } else {
                            persistLoginLocationFallback(
                                    getString(R.string.login_location_unavailable),
                                    getString(R.string.login_location_permission_denied));
                            openMainAndFinish();
                        }
                    }
                });
    }

    private String getBestProvider(LocationManager locationManager) {
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            return LocationManager.NETWORK_PROVIDER;
        }
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return LocationManager.GPS_PROVIDER;
        }
        if (locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
            return LocationManager.PASSIVE_PROVIDER;
        }
        return null;
    }

    private Location getBestLastKnownLocation(LocationManager locationManager) {
        List<String> providers = Arrays.asList(
                LocationManager.NETWORK_PROVIDER,
                LocationManager.GPS_PROVIDER,
                LocationManager.PASSIVE_PROVIDER);

        Location newestLocation = null;
        for (String provider : providers) {
            if (!locationManager.isProviderEnabled(provider)) {
                continue;
            }

            try {
                Location candidate = locationManager.getLastKnownLocation(provider);
                if (candidate == null) {
                    continue;
                }
                if (newestLocation == null || candidate.getTime() > newestLocation.getTime()) {
                    newestLocation = candidate;
                }
            } catch (SecurityException ignored) {
                return null;
            }
        }
        return newestLocation;
    }

    private void reverseGeocodeAndContinue(Location location) {
        String coordinates = formatCoordinates(location.getLatitude(), location.getLongitude());
        OpenStreetMapService openStreetMapService = ApiClient
                .getOpenStreetMapInstance()
                .create(OpenStreetMapService.class);

        openStreetMapService.reverseGeocode(
                "jsonv2",
                location.getLatitude(),
                location.getLongitude(),
                18,
                1,
                "es")
                .enqueue(new SafeCallback<OpenStreetMapReverseResponse>() {
                    @Override
                    public void onSuccess(OpenStreetMapReverseResponse data) {
                        String displayName = data != null ? data.getDisplayName() : null;
                        String address = shortenDisplayName(displayName);
                        if (TextUtils.isEmpty(address)) {
                            address = getString(R.string.login_location_unavailable);
                        }
                        session.setLastLoginAddress(address);
                        session.setLastLoginCoordinates(coordinates);
                        openMainAndFinish();
                    }

                    @Override
                    public void onError(String message) {
                        persistLoginLocationFallback(
                                getString(R.string.login_location_unavailable),
                                coordinates);
                        openMainAndFinish();
                    }
                });
    }

    private void persistLoginLocationFallback(String address, String coordinates) {
        session.setLastLoginAddress(address);
        session.setLastLoginCoordinates(coordinates);
    }

    private void openMainAndFinish() {
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }

    private String shortenDisplayName(String displayName) {
        if (TextUtils.isEmpty(displayName)) {
            return "";
        }

        String[] parts = displayName.split(",");
        StringBuilder builder = new StringBuilder();
        int limit = Math.min(parts.length, 3);
        for (int i = 0; i < limit; i++) {
            String part = parts[i].trim();
            if (part.isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(part);
        }
        return builder.toString();
    }

    private String formatCoordinates(double latitude, double longitude) {
        DecimalFormat decimalFormat = new DecimalFormat("0.00000");
        return "(" + decimalFormat.format(latitude) + ", " + decimalFormat.format(longitude) + ")";
    }
}

