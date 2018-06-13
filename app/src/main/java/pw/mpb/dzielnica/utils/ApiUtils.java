package pw.mpb.dzielnica.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.ResponseBody;
import pw.mpb.dzielnica.MainActivity;
import pw.mpb.dzielnica.MapScreen;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 *
 */

public class ApiUtils {

    public static final String TAG = "API";
    //private static final String BASE_URL = "http://192.168.1.104:8000/"; //LOKALNY ADRES - testy
    public static final String BASE_URL = "http://dzielnica.sytes.net:8000"; //ADRES SERWERA
    private static File lastImage;

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


    // Zapisanie pliku na kartę
    public static boolean writeResponseBodyToDisk(Context context, ResponseBody body, String catalog, String nazwa) {
        try {
            Log.d(ApiUtils.TAG, "nazwa: "+nazwa);

            // todo change the file location/name according to your needs
            File futureStudioIconFile = new File(context.getExternalFilesDir(null) +  File.separator + nazwa);
            if (!futureStudioIconFile.exists()) {
                //Log.d(ApiUtils.TAG, futureStudioIconFile.getPath());

                InputStream inputStream = null;
                OutputStream outputStream = null;

                try {
                    byte[] fileReader = new byte[4096];

                    long fileSize = body.contentLength();
                    long fileSizeDownloaded = 0;

                    inputStream = body.byteStream();
                    outputStream = new FileOutputStream(futureStudioIconFile);

                    while (true) {
                        int read = inputStream.read(fileReader);

                        if (read == -1) {
                            break;
                        }

                        outputStream.write(fileReader, 0, read);

                        fileSizeDownloaded += read;

                        Log.d(ApiUtils.TAG, "file download: " + fileSizeDownloaded + " of " + fileSize);
                    }

                    outputStream.flush();
                    Log.d(ApiUtils.TAG, "FLUSH");

                    return true;
                } catch (IOException e) {
                    return false;
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }

                    if (outputStream != null) {
                        outputStream.close();
                    }
                }
            } else {
                Log.d(ApiUtils.TAG, "File already exists");
                return false; // Plik już istnieje
            }
        } catch (IOException e) {
            return false;
        }
    }


    public static void onLoggedRedirect(final SharedPreferences sp, final Context from, final Class<?> to) {
        // Jeśli użytkownik zalogowany, przekieruj go do  wskazanego Activity
        ApiUtils.getAPIService().checkIsLogged(SessionManager.getToken(sp)).enqueue(new Callback<JSONObject>() {
            @Override
            public void onResponse(Call<JSONObject> call, Response<JSONObject> response) {
                ApiUtils.logResponse(response.toString());
                if(response.code() == 200) {
                    ((Activity)from).finish();
                    from.startActivity(new Intent(from, to));
                }
            }

            @Override
            public void onFailure(Call<JSONObject> call, Throwable t) {
                ApiUtils.logFailure(t);
            }
        });
    }

    public static void onUnLoggedRedirect(final SharedPreferences sp, final Context from, final Class<?> to) {
        // Jeśli użytkownik nie jest zalogowany, przekieruj go do wskazanego activity
        if(null == SessionManager.getToken(sp)) {
            Toast.makeText(from, "Zaloguj się, by otrzymać dostęp do tej funkcji", Toast.LENGTH_SHORT).show();
            redirect(from, to);
            return;
        }
        ApiUtils.getAPIService().checkIsLogged(SessionManager.getToken(sp)).enqueue(new Callback<JSONObject>() {
            @Override
            public void onResponse(Call<JSONObject> call, Response<JSONObject> response) {
                ApiUtils.logResponse(response.toString());
                if(response.code() != 200) {
                    redirect(from, to);
                    Toast.makeText(from, "Twoje ostatnie zalogowanie przedawniło się! Zaloguj się ponownie.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JSONObject> call, Throwable t) {
                ApiUtils.logFailure(t);
            }
        });
    }

    private static void redirect(final Context from, final Class<?> to) {
        ((Activity)from).finish();
        from.startActivity(new Intent(from, to));
    }

    public static void showMapActivity(final Context from) {
        ((Activity)from).finish();
        from.startActivity(new Intent(from, MapScreen.class));
    }

    public static void saveFileToApi(File f) {
        lastImage = f;
    }

    public static File getLastImage() {
        return lastImage;
    }
}