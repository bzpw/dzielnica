package pw.mpb.dzielnica;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.IOException;

import pw.mpb.dzielnica.pojo.User;
import pw.mpb.dzielnica.utils.ApiUtils;
import pw.mpb.dzielnica.utils.SessionManager;
import pw.mpb.dzielnica.utils.WebService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileScreen extends AppCompatActivity {
    private WebService mWebService;
    SharedPreferences sp;

    TextView coinsTxtView;
    TextView usernameTxtView;

    EditText firstNameEditTxt;
    EditText lastNameEditTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_screen);

        coinsTxtView = (TextView) findViewById(R.id.numOfCoinsTxt);
        usernameTxtView = (TextView) findViewById(R.id.usernameTxt);

        firstNameEditTxt = (EditText) findViewById(R.id.nameTxt);
        lastNameEditTxt = (EditText) findViewById(R.id.surnameTxt);

        sp = getSharedPreferences("authentication", MODE_PRIVATE);
        mWebService = ApiUtils.getAPIService();

       loadUserInfo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ApiUtils.onUnLoggedRedirect(sp, ProfileScreen.this, UserLogin.class);
    }

    private void loadUserInfo() {
        mWebService.detailUser(SessionManager.getUserID(sp), "JWT "+SessionManager.getToken(sp)).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    User user = response.body();
                    if(user != null) {
                        firstNameEditTxt.setText(user.getFirstName());
                        lastNameEditTxt.setText(user.getLastName());
                        coinsTxtView.setText(Integer.toString(user.getPoints()));
                        usernameTxtView.setText(user.getUsername());
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
                    ApiUtils.logResponse(response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {

            }
        });
    }
}
