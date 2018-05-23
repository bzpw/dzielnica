package pw.mpb.dzielnica;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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



import android.util.Log;
import android.widget.Toast;

// Retrofit
import org.json.JSONException;
import org.json.JSONObject;

import pw.mpb.dzielnica.pojo.User;
import pw.mpb.dzielnica.pojo.Token;
import pw.mpb.dzielnica.utils.ApiUtils;
import pw.mpb.dzielnica.utils.SessionManager;
import pw.mpb.dzielnica.utils.WebService;

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

    EditText usernameET;
    EditText passwordET;

    // Adapter REST z Retrofita
    Retrofit retrofit;
    // Interfejs API
    private WebService mWebService;

    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);

        sp = getSharedPreferences("authentication", MODE_PRIVATE);

        loginBtn = (Button) findViewById(R.id.button);
        registerBtn = (TextView) findViewById(R.id.registerUserText);

        usernameET = findViewById(R.id.editText);
        passwordET = findViewById(R.id.editText2);

        mWebService = ApiUtils.getAPIService();

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameET.getText().toString().trim();
                String password = passwordET.getText().toString().trim();
                if(!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
                    mWebService.loginUser(username, password).enqueue(new Callback<Token>() {
                        @Override
                        public void onResponse(Call<Token> call, Response<Token> response) {
                            if(response.isSuccessful()) {
                                if(response.body() != null) {
                                    String token = response.body().getToken();
                                    showResponse(token);
                                    SessionManager.saveToken(sp, token); // Zapisanie tokena do SharedPref
                                    Toast.makeText(user_login.this, SessionManager.getToken(sp), Toast.LENGTH_SHORT).show();
                                    showMainActivity(); // Przeniesienie do MainActivity
                                }
                            } else{
                                showResponse("NIESUKCESFUL");
                            }
                        }

                        @Override
                        public void onFailure(Call<Token> call, Throwable t) {
                            Log.d(TAG, call.request().toString());
                            showFailure(t);
                        }

                    });
                }
            }
        });

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

        //mWebService = ApiUtils.getAPIService();

        final EditText usernameTextView = (EditText)layout.findViewById(R.id.editText3);
        final EditText passwordTextView = ((EditText)layout.findViewById(R.id.editText4));
        final EditText emailTextView =(EditText)layout.findViewById(R.id.editText3);
        final EditText fnTextView = (EditText)layout.findViewById(R.id.editText3);
        final EditText lnTextView = (EditText)layout.findViewById(R.id.editText3);

        window.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        window.setOutsideTouchable(false);
        window.showAtLocation(layout, Gravity.CENTER, 40, 60);

        // Nacisniety przycisk "ZALOZ KONTO"
        closeRegBtn = (Button) layout.findViewById(R.id.button3);
        closeRegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //TODO: pozmieniac tak zeby pasowalo
                String username = usernameTextView.getText().toString().trim();
                String password = passwordTextView.getText().toString().trim();
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
                                Toast.makeText(user_login.this, "Zarejestrowano pomyslnie!", Toast.LENGTH_LONG).show();
                            } else {
                                showResponse(response.toString());

                            }
                        }

                        @Override
                        public void onFailure(Call<User> call, Throwable t) {
                            showFailure(t);
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        // Jeśli użytkownik zalogowany, przekieruj go do MainActivity
        mWebService.checkIsLogged(SessionManager.getToken(sp)).enqueue(new Callback<JSONObject>() {
            @Override
            public void onResponse(Call<JSONObject> call, Response<JSONObject> response) {
                showResponse(response.toString());
                if(response.code() == 200)
                    showMainActivity();
            }

            @Override
            public void onFailure(Call<JSONObject> call, Throwable t) {
                showFailure(t);
            }
        });
    }

    public void showResponse(String response) {
        Log.d(TAG, response);
    }

    public void showFailure(Throwable t) {
        Log.d(TAG, "Failure, throwable is: " + t);
    }

    public void showMainActivity() {
        startActivity(new Intent(user_login.this, MainActivity.class));
    }

    @Override
    public void onClick(View v) {

    }
}
