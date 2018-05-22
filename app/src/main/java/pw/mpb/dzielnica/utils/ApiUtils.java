package pw.mpb.dzielnica.utils;

/**
 *
 */

public class ApiUtils {

    private ApiUtils() {}

    public static final String BASE_URL = "http://192.168.1.104:8000/";

    public static WebService getAPIService() {

        return RetrofitClient.getClient(BASE_URL).create(WebService.class);
    }
}