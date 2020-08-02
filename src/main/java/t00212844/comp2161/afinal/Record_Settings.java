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

        setScreenPref(screen);
        changeGPSAcc(gps);
        changeVoiceCmd(voice);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return false;
    }

    public void setScreenPref(boolean value) {
        if (!value) {
            screenText.setText(getString(R.string.sysdefault));
        } else {
            screenText.setText(getString(R.string.alwayson));
        }
    }

    public void changeVoiceCmd(int value) {
        switch (value) {
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
    }

    public void changeGPSAcc(int value) {
        switch (value) {
            case 0:
                gpsText.setText(getString(R.string.auto));
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
        }
    }

    public void setScreenPref(View view) {

        screen = !screen;
        setScreenPref(screen);
        setSettings(getString(R.string.scron), screen);
    }

    public void changeVoiceCmd(View view) {

        if (++voice > 2) {
            voice = 0;
        }
        changeVoiceCmd(voice);
        setSettings(getString(R.string.voicecmd), voice);
    }

    public void changeGPSAcc(View view) {
        if (++gps > 3) {
            gps = 0;
        }
        changeGPSAcc(gps);
        setSettings(getString(R.string.gpsacc), gps);
    }

    public void setSettings(String key, Object value) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = pref.edit();
        if (value instanceof Integer) {
            editor.putInt(key, (int) value);
        } else if (value instanceof Boolean) {
            editor.putBoolean(key, (boolean) value);
        }
        editor.apply();
    }
}