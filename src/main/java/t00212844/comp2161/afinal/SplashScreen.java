package t00212844.comp2161.afinal;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashScreen.this.getApplicationContext(), PermissionsWelcome.class);
                SplashScreen.this.finish();
                SplashScreen.this.startActivity(intent);
                SplashScreen.this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        }, 600);

    }
}
