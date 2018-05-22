package pw.mpb.dzielnica;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import pw.mpb.dzielnica.pojo.Dzielnica;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    public Button yourButton;

    // adapter REST z Retrofita
    Retrofit retrofit;
    // nasz interfejs
    WebService myWebService;


    private String CLASS_TAG = "ODPOWIEDZSERWERA";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Zapisywanie danych HTTP do Logcata
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        // ustawiamy wybrane parametry adaptera
        retrofit = new Retrofit.Builder()
                // adres API
                .baseUrl("http://192.168.1.104:8000")
                // niech Retrofit loguje wszystko co robi
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // tworzymy klienta
        myWebService = retrofit.create(WebService.class);

        yourButton = (Button)findViewById(R.id.button2);

        yourButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){


//                try {
//                    Call<List<Dzielnica>> call = myWebService.getData();
//                    call.enqueue(new Callback<List<Dzielnica>>() {
//                        @Override
//                        public void onResponse(Call<List<Dzielnica>> call, Response<List<Dzielnica>> response) {
//                            List<Dzielnica> data = response.body();
//                            for (Dzielnica dz: data) {
//                                Log.d(CLASS_TAG, dz.getName());
//                            }
//                        }
//
//                        @Override
//                        public void onFailure(Call<List<Dzielnica>> call, Throwable t) {
//                            Log.d(CLASS_TAG, "Failure, throwable is " + t);
//                        }
//
//                    });
//
//                } catch (Exception e) {
//                    Log.d(CLASS_TAG, e.toString());
//                }

                startActivity(new Intent(MainActivity.this, user_login.class));
            }
        });
    }

    protected void connect_to_db(){
        //sprawdzenie połączenia z bazą
    }

}
