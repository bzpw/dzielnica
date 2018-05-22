package pw.mpb.dzielnica;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import pw.mpb.dzielnica.pojo.Dzielnica;
import pw.mpb.dzielnica.utils.SessionManager;
import pw.mpb.dzielnica.utils.WebService;
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


    SharedPreferences sp;

    private String CLASS_TAG = "ODPOWIEDZSERWERA";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Przygotowanie SharedPreferences do przechowywania tokena
        sp = getSharedPreferences("authentication", MODE_PRIVATE);
        Toast.makeText(this, SessionManager.getToken(sp), Toast.LENGTH_SHORT).show();
        //SessionManager.removeToken(sp);

        // Zapisywanie danych HTTP do Logcata
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        // Ustawiamy wybrane parametry adaptera
        retrofit = new Retrofit.Builder()
                // adres API
                .baseUrl("http://192.168.1.104:8000")
                // niech Retrofit loguje wszystko co robi
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Tworzymy klienta
        myWebService = retrofit.create(WebService.class);

        // Set up progress before call
        final ProgressDialog progressDoalog;
        progressDoalog = new ProgressDialog(MainActivity.this);
        progressDoalog.setMax(100);
        progressDoalog.setMessage("Sprawdzam polaczenie z baza");
        progressDoalog.setTitle("Connection to DB");
        progressDoalog.setProgressStyle(ProgressDialog.STYLE_SPINNER);



        yourButton = (Button)findViewById(R.id.button2);
        yourButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){

                // Show it
                progressDoalog.show();

                try {
                    Call<List<Dzielnica>> call = myWebService.getData();
                    call.enqueue(new Callback<List<Dzielnica>>() {
                        @Override
                        public void onResponse(Call<List<Dzielnica>> call, Response<List<Dzielnica>> response) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            List<Dzielnica> data = response.body();
                            if(data != null) {
                                for (Dzielnica dz : data) {
                                    Log.d(CLASS_TAG, dz.getName());
                                }
                                progressDoalog.dismiss();
                                startActivity(new Intent(MainActivity.this, user_login.class));
                            } else {
                                Log.d(CLASS_TAG, Integer.toString(response.code()));
                                progressDoalog.dismiss();
                            }
                        }

                        @Override
                        public void onFailure(Call<List<Dzielnica>> call, Throwable t) {
                            Log.d(CLASS_TAG, "Failure, throwable is " + t);
                            progressDoalog.dismiss();
                        }

                    });

                } catch (Exception e) {
                    Log.d(CLASS_TAG, e.toString());
                }


            }
        });
    }

    protected void connect_to_db(){
        //sprawdzenie połączenia z bazą
    }

}
