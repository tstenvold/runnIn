package t00212844.comp2161.afinal;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Splash Screen to welcome the user to the app
 */
public class SplashScreen extends AppCompatActivity {

    /**
     * Creates the timer to close the splash screen and starts the next activity
     * @param savedInstanceState bundle of saved user info
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        //Forced Portrait Orientation as it doesn't make sense to have a horizontal splash
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        final Handler handler = new Handler();
        handler.postDelayed(() -> {
            Intent intent = new Intent(SplashScreen.this.getApplicationContext(), PermissionsWelcome.class);
            SplashScreen.this.finish();
            SplashScreen.this.startActivity(intent);
            SplashScreen.this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }, 1250);
    }
}
