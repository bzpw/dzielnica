package pw.mpb.dzielnica;

import android.content.Context;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.api.Api;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import pw.mpb.dzielnica.pojo.Category;
import pw.mpb.dzielnica.pojo.Type;
import pw.mpb.dzielnica.utils.ApiUtils;
import pw.mpb.dzielnica.utils.RetrofitClient;
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
    SharedPreferences sp_typy;
    SharedPreferences sp_kategorie;

    ProgressDialog progressDoalog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        //Przygotowanie SharedPreferences do przechowywania tokena, listy typów i listy kategorii
        sp = getSharedPreferences("authentication", MODE_PRIVATE);
        sp_typy = getSharedPreferences("TYPY", MODE_PRIVATE);
        sp_kategorie = getSharedPreferences("KATEGORIE", MODE_PRIVATE);

        // Tworzymy klienta
        myWebService = ApiUtils.getAPIService();

        // Set up progress before call
        progressDoalog = new ProgressDialog(MainActivity.this);
        progressDoalog.setMax(100);
        progressDoalog.setMessage("Sprawdzam polaczenie z serwerem");
        progressDoalog.setTitle("Połączenie z serwerem");
        progressDoalog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        btnMap = (Button) findViewById(R.id.btnMapa);
        btnLogout = (Button) findViewById(R.id.btnWyloguj);
        btnConnect = (Button) findViewById(R.id.btnConnect);


        yourButton = (Button) findViewById(R.id.button2);

//        yourButton.setEnabled(false);
//        btnMap.setEnabled(false);
        btnLogout.setEnabled(true);

        yourButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
                startActivity(new Intent(MainActivity.this, user_login.class));
            }
        });

        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(MainActivity.this, map_screen.class));
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SessionManager.removeToken(sp);
                Toast.makeText(MainActivity.this, "Wylogowano użytkownika!", Toast.LENGTH_SHORT).show();
            }
        });

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connect_to_db();
            }
        });

        connect_to_db();
    }

    protected void connect_to_db() {

        // Show it
        progressDoalog.show();

        try {
            Call<List<Type>> call = myWebService.listTypes();
            call.enqueue(new Callback<List<Type>>() {
                @Override
                public void onResponse(Call<List<Type>> call, Response<List<Type>> response) {

                    if (response.isSuccessful()) {

                        List<Type> data = response.body();

                        if (data != null) {
                            // Pobieranie ikon
                            downloadCatIcons(data);

                            // Pobieranie listy kategorii
                            myWebService.listCategories().enqueue(new Callback<List<Category>>() {
                                @Override
                                public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                                    if (response.isSuccessful()) {
                                        ApiUtils.logResponse("Jest sukcesful");
                                        saveCategories(response.body());

                                    }
                                }

                                @Override
                                public void onFailure(Call<List<Category>> call, Throwable t) {

                                }
                            });

                            Gson gson = new Gson();
                            String json = gson.toJson(data);

                            // Dodawanie listy typów zgłoszeń do SharedPreferences -> będą tworzyły listę do spinnera
                            SharedPreferences.Editor prefsEditor = sp_typy.edit();
                            //String json = data.string();
                            prefsEditor.putString("Typy", json);
                            prefsEditor.apply();

                            btnMap.setEnabled(true);
                            yourButton.setEnabled(true);
                        } else {
                            Log.d(ApiUtils.TAG, Integer.toString(response.code()));
                        }

                    } else {
                        try {
                            int code = response.code();
                            String err = response.errorBody().string();
                            ApiUtils.logResponse(err);
                            ApiUtils.showErrToast(getApplicationContext(), code, err);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    progressDoalog.dismiss();
                }

                @Override
                public void onFailure(Call<List<Type>> call, Throwable t) {
                    Log.d(ApiUtils.TAG, "Failure, throwable is " + t);
                    Toast.makeText(MainActivity.this, "Połączenie nieudane!\n" + t, Toast.LENGTH_LONG).show();
                    progressDoalog.dismiss();
                }

            });

        } catch (Exception e) {
            Log.d(ApiUtils.TAG, e.toString());
        }

    }

    private void downloadCatIcons(List<Type> data) {
        List<String> usedNames = new ArrayList<>();

        for (Type el : data) {
            //Log.d(ApiUtils.TAG, el.toString());

            String url = el.getCategory().getIcon();
            if (url != null) {
                final String filename = url.substring(url.lastIndexOf("/")+1);
                if(!usedNames.contains(filename)) {
                    Call<ResponseBody> download_resp = myWebService.downloadFileWithDynamicUrlSync(url);
                    download_resp.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.isSuccessful()) {
                                Log.d(ApiUtils.TAG, "server contacted and has file");

                                boolean writtenToDisk = ApiUtils.writeResponseBodyToDisk(getApplicationContext(), response.body(), "cat", filename);

                                Log.d(ApiUtils.TAG, "file download was a success? " + writtenToDisk);

                            } else {
                                Log.d(ApiUtils.TAG, "server contact failed");

                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            ApiUtils.logFailure(t);
                        }
                    });
                    usedNames.add(filename);
                } else {
                    Log.d(ApiUtils.TAG, filename+" already downloaded");
                }
            }
        }
        usedNames.clear();
    }

    private void saveCategories(List<Category> cats) {
                Gson gson = new Gson();
                String json = gson.toJson(cats);

                // Dodawanie listy kategorii do SharedPreferences -> będą potrzebne do ikonek
                SharedPreferences.Editor prefsEditor = sp_kategorie.edit();
                //String json = data.string();
                prefsEditor.putString("Kategorie", json);
                prefsEditor.apply();

    }

}
