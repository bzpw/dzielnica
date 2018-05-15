package pw.mpb.dzielnica;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button yourButton = (Button)findViewById(R.id.button2);

        yourButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                startActivity(new Intent(MainActivity.this, user_login.class));
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        if (true) {
            finish();
            startActivity(new Intent(this, user_login.class));
        }

    }

}
