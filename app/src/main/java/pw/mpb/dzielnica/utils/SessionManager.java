package pw.mpb.dzielnica.utils;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONObject;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Pomocnicza klasa służąca do obsługi tokena i autentyfikacji
 */

public class SessionManager {

    private SessionManager() {}

    public static void saveToken(SharedPreferences sp, String token) {
        SharedPreferences.Editor editor=sp.edit();
        editor.putString("JWT-TOKEN", token);
        editor.apply();

    }

    public static String getToken(SharedPreferences sp) {
        return sp.getString("JWT-TOKEN", null);
    }

    public static void removeToken(SharedPreferences sp) {
        SharedPreferences.Editor editor=sp.edit();
        editor.remove("JWT-TOKEN");
        editor.apply();
    }

    public static Boolean isLogged(SharedPreferences sp, WebService ws) throws IOException {
        String current_token = getToken(sp);
        ws.checkIsLogged(current_token).execute();

        return false;
    }

}
