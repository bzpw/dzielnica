package pw.mpb.dzielnica.utils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.ResponseBody;

/**
 *
 */

public class ApiUtils {

    public static final String TAG = "API";
    //private static final String BASE_URL = "http://192.168.1.104:8000/"; //LOKALNY ADRES - testy
    public static final String BASE_URL = "http://dzielnica.sytes.net:8000/"; //ADRES SERWERA

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

}