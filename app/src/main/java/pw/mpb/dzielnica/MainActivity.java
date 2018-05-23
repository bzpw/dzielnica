package pw.mpb.dzielnica;

import android.content.Context;
import android.app.ProgressDialog;
import android.content.Intent;
import android.preference.PreferenceManager;
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

import org.osmdroid.config.Configuration;

public class MainActivity extends AppCompatActivity {

    public Button yourButton;
    public Button btnMap;
    public Button btnLogout;
    public Button btnConnect;

    // Adapter REST z Retrofita
    Retrofit retrofit;
    // Nasz interfejs API REST
    WebService myWebService;


    SharedPreferences sp;

    private String CLASS_TAG = "ODPOWIEDZSERWERA";

    ProgressDialog progressDoalog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        //Przygotowanie SharedPreferences do przechowywania tokena
        sp = getSharedPreferences("authentication", MODE_PRIVATE);

        // Ustawiamy wybrane parametry adaptera
        retrofit = new Retrofit.Builder()
                // adres API
                .baseUrl("http://192.168.1.104:8000")
                // niech Retrofit loguje wszystko co robi
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Tworzymy klienta
        myWebService = retrofit.create(WebService.class);

        // Set up progress before call
        progressDoalog = new ProgressDialog(MainActivity.this);
        progressDoalog.setMax(100);
        progressDoalog.setMessage("Sprawdzam polaczenie z serwerem");
        progressDoalog.setTitle("Połączenie z serwerem");
        progressDoalog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        btnMap = (Button) findViewById(R.id.btnMapa);
        btnLogout = (Button) findViewById(R.id.btnWyloguj);
        btnConnect = (Button) findViewById(R.id.btnConnect);


        yourButton = (Button)findViewById(R.id.button2);

//        yourButton.setEnabled(false);
//        btnMap.setEnabled(false);
        btnLogout.setEnabled(true);

        yourButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                startActivity(new Intent(MainActivity.this, user_login.class));
            }
        });

        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, map_screen.class));
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                SessionManager.removeToken(sp);
                Toast.makeText(MainActivity.this, "Wylogowano użytkownika!", Toast.LENGTH_SHORT).show();
            }
        });

        btnConnect.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                connect_to_db();
            }
        });

        connect_to_db();
    }

    protected void connect_to_db(){

        // Show it
        progressDoalog.show();

        try {
            Call<List<Dzielnica>> call = myWebService.getData();
            call.enqueue(new Callback<List<Dzielnica>>() {
                @Override
                public void onResponse(Call<List<Dzielnica>> call, Response<List<Dzielnica>> response) {

                    List<Dzielnica> data = response.body();
                    if(data != null) {
                        for (Dzielnica dz : data) {
                            Log.d(CLASS_TAG, dz.getName());
                        }

                        progressDoalog.dismiss();
                        btnMap.setEnabled(true);
                        yourButton.setEnabled(true);

                    } else {
                        Log.d(CLASS_TAG, Integer.toString(response.code()));
                        progressDoalog.dismiss();
                    }
                }

                @Override
                public void onFailure(Call<List<Dzielnica>> call, Throwable t) {
                    Log.d(CLASS_TAG, "Failure, throwable is " + t);
                    Toast.makeText(MainActivity.this, "Połączenie nieudane!\n"+t, Toast.LENGTH_LONG).show();
                    progressDoalog.dismiss();
                }

            });

        } catch (Exception e) {
            Log.d(CLASS_TAG, e.toString());
        }

    }

}
