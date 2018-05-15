package pw.mpb.dzielnica;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import java.io.BufferedReader;
import java.net.HttpURLConnection;

public class user_login extends AppCompatActivity implements View.OnClickListener {

    EditText editTextEmail, editTextPassword;
    String result = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);

        editTextPassword = (EditText)findViewById(R.id.password);
        editTextEmail = (EditText)findViewById(R.id.emailTextView);

        Button btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Log.d("LOGOWANIE", "Klkniety przycisk");
        switch (v.getId()) {
            case R.id.btnLogin:
                Log.d("LOGOWANIE", "Klkniety przycisk");
                Log.d("LOGOWANIE", editTextEmail.getText().toString());

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

}
