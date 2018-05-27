package pw.mpb.dzielnica.utils;

import android.util.JsonToken;

import org.json.JSONObject;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import pw.mpb.dzielnica.pojo.Dzielnica;
import pw.mpb.dzielnica.pojo.Token;
import pw.mpb.dzielnica.pojo.User;
import pw.mpb.dzielnica.pojo.Zgloszenie;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Klasa służąca do komunikacji pomiędzy klientem Android, a REST API Django na serwerze
 * Wykorzystuje metody GET i POST i korzysta z biblioteki retrofit2
 */

public interface WebService {
    @GET("/api/dzielnice/list/") // deklarujemy endpoint oraz metodę
        //void getData(Callback<Zgloszenie> pResponse);
    Call<List<Dzielnica>> getData();

    @POST("/api/user/register/") // deklarujemy endpoint, metodę oraz dane do wysłania
    @FormUrlEncoded
    Call<User> registerUser(@Field("username") String username,
                            @Field("password") String password,
                            @Field("email") String email,
                            @Field("first_name") String first_name,
                            @Field("first_name") String last_name);

    @POST("/api/user/login/")
    @FormUrlEncoded
    Call<Token> loginUser(@Field("username") String username,
                          @Field("password") String password);

    @POST("api/user/verify/")
    @FormUrlEncoded
    Call<JSONObject> checkIsLogged(@Field("token") String token);

    @POST("api/zgloszenia/add/")
    @FormUrlEncoded
    Call<Zgloszenie> addZgloszenie(@Header("Authorization") String authHeader,
                                   @Field("type") int field,
                                   @Field("desc") String desc,
                                   @Field("geometry") String geometry,
                                   @Field("user") int user);

    @GET("api/zgloszenia/all/")
    Call<ResponseBody> listZgloszenia(@Header("Authorization") String authHeader);

}
