package t00212844.comp2161.afinal;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.snackbar.Snackbar;

public class PermissionsWelcome extends AppCompatActivity {

    private static final int PERMISSION_FINE_LOCATION = 0;
    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions_welcome);
        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (pref.getBoolean(getString(R.string.donotshow), false)) {
            CheckBox donot = findViewById(R.id.doNotShowCheck);
            donot.setChecked(true);
            continuePressed();
        }

        //Check Internet Permissions and set flag

        checkLocationPermission();
        checkBattery();
        checkFilePermission();

    }

    private void checkBattery() {
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        String packageName = getPackageName();
        assert powerManager != null;
        if (powerManager.isIgnoringBatteryOptimizations(packageName)) {
            ImageView batt = findViewById(R.id.batteryImage);
            batt.setImageDrawable(getDrawable(R.drawable.check_circle_outline_24px));
        }
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            ImageView gps = findViewById(R.id.gpsAccessImage);
            gps.setImageDrawable(getDrawable(R.drawable.check_circle_outline_24px));
        } else {
            requestLocationPermission();
        }
    }

    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            Snackbar.make(findViewById(R.id.PermissionsLayout), R.string.request_location, Snackbar.LENGTH_INDEFINITE).setAction(R.string.ok, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ActivityCompat.requestPermissions((Activity) getBaseContext(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_FINE_LOCATION);
                }
            }).show();

        } else {
            Snackbar.make(findViewById(R.id.PermissionsLayout), R.string.location_unavailable, Snackbar.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_FINE_LOCATION);
        }
    }

    private void requestFilePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Snackbar.make(findViewById(R.id.PermissionsLayout), R.string.request_file, Snackbar.LENGTH_INDEFINITE).setAction(R.string.ok, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ActivityCompat.requestPermissions((Activity) PermissionsWelcome.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                }
            }).show();

        } else {
            Snackbar.make(findViewById(R.id.PermissionsLayout), R.string.filedenied, Snackbar.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
    }

    private void checkFilePermission() {
        if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            ImageView file = findViewById(R.id.fileAccessImage);
            file.setImageDrawable(getDrawable(R.drawable.check_circle_outline_24px));
        } else {
            requestFilePermission();
        }
    }

    private void continuePressed() {
        SharedPreferences.Editor editor = pref.edit();
        CheckBox donot = findViewById(R.id.doNotShowCheck);
        editor.putBoolean(getString(R.string.donotshow), donot.isChecked());
        editor.apply();

        Intent intent = new Intent(getBaseContext(), MainScreen.class);
        startActivity(intent);
    }

    public void continuePressed(View view) {
        continuePressed();
    }
}