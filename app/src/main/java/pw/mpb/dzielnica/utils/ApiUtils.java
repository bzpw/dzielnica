package pw.mpb.dzielnica.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 *
 */

public class ApiUtils {

    public static final String TAG = "API";
    private static final String BASE_URL = "http://192.168.1.104:8000/";

    private ApiUtils() {}

    public static WebService getAPIService() {

        return RetrofitClient.getClient(BASE_URL).create(WebService.class);
    }

    public static void logResponse(String response) {
        Log.d(TAG, response);
    }

    public static void logFailure(Throwable t) {
        Log.d(TAG, "Failure, throwable is: " + t);
    }

    public static void showErrToast(Context ctx, int code, String err) {
        Toast.makeText(ctx, "Wystąpił błąd ("+code+"):\n"+err, Toast.LENGTH_LONG).show();
    }

    public static String getTAG() { return TAG; }

}