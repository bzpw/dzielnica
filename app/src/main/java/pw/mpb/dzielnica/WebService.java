package pw.mpb.dzielnica;

import java.util.List;

import pw.mpb.dzielnica.pojo.Dzielnica;
import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by Mateusz on 20.05.2018.
 */

public interface WebService {
    @GET("/dzielnice/?format=json") // deklarujemy endpoint oraz metodę
    //void getData(Callback<Zgloszenie> pResponse);
    Call<List<Dzielnica>> getData();

    //@POST("/wsexample/") // deklarujemy endpoint, metodę oraz dane do wysłania
    //void postData(@Body Zgloszenie pBody, Callback<Zgloszenie> pResponse);
}
