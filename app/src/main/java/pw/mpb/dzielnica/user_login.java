package pw.mpb.dzielnica;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;


// Retrofit???
import android.os.AsyncTask;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import java.io.BufferedReader;
import java.net.HttpURLConnection;
import android.util.Log;

import pw.mpb.dzielnica.pojo.User;
import pw.mpb.dzielnica.utils.ApiUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class user_login extends AppCompatActivity implements View.OnClickListener {

    String result = "";
    private String TAG = "ODPOWIEDZSERWERA";

    public Button loginBtn;
    public Button closeRegBtn;
    public TextView registerBtn;
    private PopupWindow window;

    // Adapter REST z Retrofita
    Retrofit retrofit;
    // Interfejs API
    private WebService mWebService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);

        loginBtn = (Button) findViewById(R.id.button);
        registerBtn = (TextView) findViewById(R.id.textView4);



        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               ShowPopupWindow();
            }
        });

    }

    private void ShowPopupWindow(){
        LayoutInflater inflater = (LayoutInflater) user_login.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.register_popup, null);
        window = new PopupWindow(layout, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);

        mWebService = ApiUtils.getAPIService();

        final EditText usernameTextView = (EditText)layout.findViewById(R.id.editText3);
        final EditText passwordTextView = ((EditText)layout.findViewById(R.id.editText4));
        final EditText emailTextView =(EditText)layout.findViewById(R.id.editText3);
        final EditText fnTextView = (EditText)layout.findViewById(R.id.editText3);
        final EditText lnTextView = (EditText)layout.findViewById(R.id.editText3);

        window.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        window.setOutsideTouchable(true);
        window.showAtLocation(layout, Gravity.CENTER, 40, 60);
        closeRegBtn = (Button) layout.findViewById(R.id.button3);
        closeRegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String username = usernameTextView.getText().toString().trim();
                String password = passwordTextView.getText().toString().trim();
//                String username = "koleszkoleszko";
//                String password = "haselkomaselko";
//                String email = "da";
//                String first_name = "";
//                String last_name = "";
                String email = emailTextView.getText().toString().trim();
                String first_name = fnTextView.getText().toString().trim();
                String last_name = lnTextView.getText().toString().trim();

                if(!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
                    mWebService.registerUser(username, password, email, first_name, last_name).enqueue(new Callback<User>() {

                        @Override
                        public void onResponse(Call<User> call, Response<User> response) {
                            if(response.isSuccessful()) {
                                showResponse(response.body().toString());
                                Log.d(TAG, "post submitted to API." + response.body().toString());
                            } else {
                                showResponse(response.toString());

                            }
                        }

                        @Override
                        public void onFailure(Call<User> call, Throwable t) {
                            Log.d(TAG, "Failure, throwable is: " + t);
                        }
                    });
                }


                //window.dismiss();
            }
        });
    }


    @Override
    public void onClick(View v) {
        //Log.d("LOGOWANIE", "Klkniety przycisk");
        switch (v.getId()) {
            case R.id.button:
                Log.d("LOGOWANIE", "Klkniety przycisk");
                //Log.d("LOGOWANIE", editTextEmail.getText().toString());

                new CallAPI().execute("http://192.168.1.104:8000/dzielnice");

                break;

        }
    }


    // Asynchroniczne wysyłanie zapytania do DJANGO

    public class CallAPI extends AsyncTask<String , Void ,String> {
        String server_response;

        @Override
        protected String doInBackground(String... strings) {

            URL url;
            HttpURLConnection urlConnection = null;
            String getParams = strings[0];
            //String dataPOST = strings[1];

            try {
                url = new URL(getParams);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(15000);
                urlConnection.setConnectTimeout(15000);
                urlConnection.setRequestMethod("GET");
                //urlConnection.setDoInput(true);
                //urlConnection.setDoOutput(true);

                int responseCode = urlConnection.getResponseCode();

                if(responseCode == HttpURLConnection.HTTP_OK){
                    server_response = readStream(urlConnection.getInputStream());
                    Log.v("CatalogClient", server_response);
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            Log.e("Response", "" + server_response);


        }
    }

    // Converting InputStream to String

    private String readStream(InputStream in) {
        BufferedReader reader = null;
        StringBuffer response = new StringBuffer();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return response.toString();
    }



    public void showResponse(String response) {
        Log.d(TAG, response);
    }
}
