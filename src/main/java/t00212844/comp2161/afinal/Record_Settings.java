package t00212844.comp2161.afinal;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Objects;

public class Record_Settings extends AppCompatActivity {

    TextView voiceText;
    TextView gpsText;
    TextView screenText;

    int voice;
    int gps;
    boolean screen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record__settings);

        Toolbar toolbar = findViewById(R.id.settingsToolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        voiceText = findViewById(R.id.voiceSettingsText);
        gpsText = findViewById(R.id.gpsSettingText);
        screenText = findViewById(R.id.screenSettingsText);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        voice = pref.getInt(getString(R.string.voicecmd), 0);
        gps = pref.getInt(getString(R.string.gpsacc), 0);
        screen = pref.getBoolean(getString(R.string.scron), false);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }


    public void setScreenPref(View view) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = pref.edit();
        if (screen) {
            screenText.setText(getString(R.string.sysdefault));
            screen = false;
        } else {
            screenText.setText(getString(R.string.alwayson));
            screen = true;
        }
        editor.putBoolean(getString(R.string.scron), screen);
        editor.apply();
    }

    public void changeVoiceCmd(View view) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = pref.edit();
        if (++voice > 2) {
            voice = 0;
        }
        switch (voice) {
            case 0:
                voiceText.setText(getString(R.string.off));
                break;
            case 1:
                voiceText.setText(getString(R.string.min));
                break;
            case 2:
                voiceText.setText(getString(R.string.detailed));
                break;

        }
        editor.putInt(getString(R.string.voicecmd), voice);
        editor.apply();
    }

    public void changeGPSAcc(View view) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = pref.edit();
        if (++gps > 4) {
            gps = 0;
        }
        switch (gps) {
            case 0:
                gpsText.setText(getString(R.string.off));
                break;
            case 1:
                gpsText.setText(getString(R.string.low));
                break;
            case 2:
                gpsText.setText(getString(R.string.medium));
                break;
            case 3:
                gpsText.setText(getString(R.string.high));
                break;
            case 4:
                gpsText.setText(getString(R.string.auto));
                break;

        }
        editor.putInt(getString(R.string.gpsacc), gps);
        editor.apply();
    }
}