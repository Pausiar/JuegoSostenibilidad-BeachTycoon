package es.amplya.ay_inout.model;

import com.google.gson.annotations.SerializedName;

// body del login con params gsbase
public class LoginRequest {

    private String ipgsbase;
    private String puertogsbase;
    private String gestgsbase;
    private String ejagsbase;
    private String usuario;
    private String contrasenya;
    private String aplgsbase;

    @SerializedName("fcm_token")
    private String fcmToken;

    public LoginRequest(String usuario, String contrasenya,
                        String ipgsbase, String puertogsbase,
                        String gestgsbase, String aplgsbase,
                        String ejagsbase, String fcmToken) {
        this.usuario = usuario;
        this.contrasenya = contrasenya;
        this.ipgsbase = ipgsbase;
        this.puertogsbase = puertogsbase;
        this.gestgsbase = gestgsbase;
        this.aplgsbase = aplgsbase;
        this.ejagsbase = ejagsbase;
        this.fcmToken = fcmToken;
    }
}

