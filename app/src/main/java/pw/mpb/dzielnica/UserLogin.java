package pw.mpb.dzielnica;

import android.content.Context;
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

import java.io.IOException;

import pw.mpb.dzielnica.pojo.User;
import pw.mpb.dzielnica.pojo.Token;
import pw.mpb.dzielnica.utils.ApiUtils;
import pw.mpb.dzielnica.utils.SessionManager;
import pw.mpb.dzielnica.utils.WebService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class UserLogin extends AppCompatActivity implements View.OnClickListener {

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

        loginBtn = (Button) findViewById(R.id.loginLoginBtn);
        registerBtn = (TextView) findViewById(R.id.registerUserText);

        usernameET = findViewById(R.id.loginUsernameEdtTxt);
        passwordET = findViewById(R.id.loginPasswordEdtTxt);

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
                                    ApiUtils.logResponse(token);
                                    SessionManager.saveToken(sp, token); // Zapisanie tokena do SharedPref
                                    ApiUtils.showMainActivity(UserLogin.this); // Przeniesienie do MainActivity
                                }
                            } else{
                                try {
                                    int code = response.code();
                                    String err = response.errorBody().string();
                                    ApiUtils.logResponse(err);
                                    ApiUtils.showErrToast(getApplicationContext(), code, err);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<Token> call, Throwable t) {
                            Log.d(TAG, call.request().toString());
                            ApiUtils.logFailure(t);
                        }

                    });
                } else {
                    Toast.makeText(UserLogin.this, "Musisz wypełnić wszystkie pola!", Toast.LENGTH_SHORT).show();
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
        LayoutInflater inflater = (LayoutInflater) UserLogin.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.register_popup, null);
        window = new PopupWindow(layout, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);

        //mWebService = ApiUtils.getAPIService();

        final EditText usernameTextView = (EditText)layout.findViewById(R.id.registerLoginEdtTxt);
        final EditText passwordTextView = ((EditText)layout.findViewById(R.id.registerPasswordEdtTxt));
        final EditText password2TextView = ((EditText)layout.findViewById(R.id.registerPassword2EdtTxt));
        final EditText emailTextView =(EditText)layout.findViewById(R.id.registerEmailEdtTxt);
        final Button btnCloseRegister =(Button)layout.findViewById(R.id.registerBackBtn);


        window.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        window.setOutsideTouchable(false);
        window.showAtLocation(layout, Gravity.CENTER, 40, 60);

        btnCloseRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                window.dismiss();
            }
        });

        // Nacisniety przycisk "ZALOZ KONTO"
        closeRegBtn = (Button) layout.findViewById(R.id.button3);
        closeRegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String username = usernameTextView.getText().toString().trim();
                String password = passwordTextView.getText().toString().trim();
                String password2 = password2TextView.getText().toString().trim();
                String email = emailTextView.getText().toString().trim();

                if(!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(password2)) {

                    if (!password.equals(password2)) {
                        Toast.makeText(UserLogin.this, "Podane hasła się różnią!", Toast.LENGTH_SHORT).show();
                    } else {
                        mWebService.registerUser(username, password, email).enqueue(new Callback<User>() {

                            @Override
                            public void onResponse(Call<User> call, Response<User> response) {
                                if (response.isSuccessful()) {
                                    ApiUtils.logResponse(response.body().toString());
                                    Log.d(TAG, "post submitted to API." + response.body().toString());

                                    Toast.makeText(UserLogin.this, "Zostałeś zarejestrowany pomyślnie! Możesz się teraz zalogować", Toast.LENGTH_SHORT).show();
                                    window.dismiss();

                                } else {
                                    ApiUtils.logResponse(response.toString());

                                }
                            }

                            @Override
                            public void onFailure(Call<User> call, Throwable t) {
                                ApiUtils.logFailure(t);
                            }
                        });
                    }
                } else {
                    Toast.makeText(UserLogin.this, "Musisz wypełnić wszystkie pola!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        ApiUtils.onLoggedRedirect(sp, UserLogin.this, MainActivity.class);
    }

    @Override
    public void onClick(View v) {

    }
}
