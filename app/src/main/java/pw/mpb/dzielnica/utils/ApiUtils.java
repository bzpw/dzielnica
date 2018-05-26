package pw.mpb.dzielnica.utils;

import android.util.Log;

/**
 *
 */

public class ApiUtils {

    private static String TAG = "API";

    private ApiUtils() {}

    //public static final String BASE_URL = "http://dzielnica.sytes.net:8000/";
    public static final String BASE_URL = "http://192.168.1.104:8000/";

    public static WebService getAPIService() {

        return RetrofitClient.getClient(BASE_URL).create(WebService.class);
    }

    public static void logResponse(String response) {
        Log.d(TAG, response);
    }

    public static void logFailure(Throwable t) {
        Log.d(TAG, "Failure, throwable is: " + t);
    }

}