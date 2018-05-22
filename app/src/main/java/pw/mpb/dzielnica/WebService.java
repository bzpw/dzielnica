package pw.mpb.dzielnica;

import java.util.List;

import okhttp3.MultipartBody;
import pw.mpb.dzielnica.pojo.Dzielnica;
import pw.mpb.dzielnica.pojo.User;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Klasa służąca do komunikacji pomiędzy klientem Android, a REST API Django na serwerze
 * Wykorzystuje metody GET i POST i korzysta z biblioteki retrofit2
 */

public interface WebService {
    @GET("/dzielnice/?format=json") // deklarujemy endpoint oraz metodę
    //void getData(Callback<Zgloszenie> pResponse);
    Call<List<Dzielnica>> getData();

    @Multipart
    @POST("/api/app/user/register/") // deklarujemy endpoint, metodę oraz dane do wysłania
    Call<User> registerUser(@Field("username") String username,
                            @Field("password") String password,
                            @Field("email") String email,
                            @Field("first_name") String first_name,
                            @Field("first_name") String last_name,
                            @Part MultipartBody.Part img);
    //void postData(@Body Zgloszenie pBody, Callback<Zgloszenie> pResponse);
}
