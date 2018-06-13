package pw.mpb.dzielnica.utils;

import android.util.JsonToken;

import org.json.JSONObject;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import pw.mpb.dzielnica.pojo.Category;
import pw.mpb.dzielnica.pojo.Dzielnica;
import pw.mpb.dzielnica.pojo.Token;
import pw.mpb.dzielnica.pojo.Type;
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
import retrofit2.http.Path;
import retrofit2.http.Url;

/**
 * Klasa służąca do komunikacji pomiędzy klientem Android, a REST API Django na serwerze
 * Wykorzystuje metody GET i POST i korzysta z biblioteki retrofit2
 */

public interface WebService {
    @GET("/api/dzielnice/list/") // deklarujemy oraz endpoint
    Call<List<Dzielnica>> getData();

    @POST("/api/user/register/") // deklarujemy endpoint, metodę oraz dane do wysłania
    @FormUrlEncoded
    Call<User> registerUser(@Field("username") String username,
                            @Field("password") String password,
                            @Field("email") String email);

    @POST("/api/user/login/")
    @FormUrlEncoded
    Call<Token> loginUser(@Field("username") String username,
                          @Field("password") String password);

    @GET("/api/user/detail/{uid}")
    Call<User> detailUser(@Path("uid") int uid, @Header("Authorization") String authHeader);

    @POST("api/user/verify/")
    @FormUrlEncoded
    Call<JSONObject> checkIsLogged(@Field("token") String token);

//    @POST("api/zgloszenia/add/")
//    @FormUrlEncoded
//    Call<Zgloszenie> addZgloszenie(@Header("Authorization") String authHeader,
//                                   @Field("type") int field,
//                                   @Field("desc") String desc,
//                                   @Field("geometry") String geometry,
//                                   @Field("user") int user);

    @POST("api/zgloszenia/add/")
    @Multipart
    Call<ResponseBody> addZgloszenie(@Header("Authorization") String authHeader,
                                     @Part("type") RequestBody type,
                                   @Part("desc") RequestBody desc,
                                   @Part("geometry") RequestBody geometry,
                                   @Part("user") RequestBody user,
                                   @Part MultipartBody.Part image);

    @GET("api/zgloszenia/list/")
    Call<ResponseBody> listZgloszenia(@Header("Authorization") String authHeader);

    @GET("api/types/list/")
    Call<List<Type>> listTypes();

    @GET("api/cats/icons/")
    Call<List<Category>> listCategories();

    @GET
    Call<ResponseBody> getCatIcons();

    @GET
    Call<ResponseBody> downloadFileWithDynamicUrlSync(@Url String fileUrl);

}
