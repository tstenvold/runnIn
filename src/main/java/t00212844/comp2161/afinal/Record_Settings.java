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

/**
 * Record settings.
 */
public class Record_Settings extends AppCompatActivity {

    TextView voiceText;
    TextView gpsText;
    TextView screenText;
    int voice;
    int gps;
    boolean screen;

    /**
     * Creates the view with the proper layout
     *
     * @param savedInstanceState bundle of saved state variables
     */
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

    /**
     * If the option selected is the back button end the activity
     *
     * @param item the item selected
     * @return if the item was selected is the correct one
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return false;
    }

    /**
     * Sets screen pref.
     *
     * @param value the value to be set
     */
    public void setScreenPref(boolean value) {
        if (!value) {
            screenText.setText(getString(R.string.sysdefault));
        } else {
            screenText.setText(getString(R.string.alwayson));
        }
    }

    /**
     * Change voice command setting
     *
     * @param value the value to be set
     */
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

    /**
     * Change the GPS accuracy.
     *
     * @param value the value to be set
     */
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

    /**
     * Sets screen preference. Either always on or sys default
     */
    public void setScreenPref(View view) {

        screen = !screen;
        setScreenPref(screen);
        setSettings(getString(R.string.scron), screen);
    }

    /**
     * Change voice cmd button clicked
     */
    public void changeVoiceCmd(View view) {

        if (++voice > 2) {
            voice = 0;
        }
        changeVoiceCmd(voice);
        setSettings(getString(R.string.voicecmd), voice);
    }

    /**
     * Change GPS accuracy button clicked
     */
    public void changeGPSAcc(View view) {
        if (++gps > 3) {
            gps = 0;
        }
        changeGPSAcc(gps);
        setSettings(getString(R.string.gpsacc), gps);
    }

    /**
     * Generalized to sets the settings from the shared preferences
     *
     * @param key   the key
     * @param value the value
     */
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