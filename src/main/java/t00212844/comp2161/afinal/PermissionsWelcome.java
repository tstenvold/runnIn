package t00212844.comp2161.afinal;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

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

        updateCheck();
    }

    private void updateCheck() {
        boolean permissionsGranted;
        //Check Internet Permissions and set flag
        permissionsGranted = checkInternetPermission();
        permissionsGranted = checkLocationPermission();
        permissionsGranted = checkBattery();
        permissionsGranted = checkFilePermission();

        if (!permissionsGranted) {
            Log.v("notall permissions", "Not all perms");
        }
    }

    private boolean checkBattery() {
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        String packageName = getPackageName();
        assert powerManager != null;
        if (powerManager.isIgnoringBatteryOptimizations(packageName)) {
            ImageView batt = findViewById(R.id.batteryImage);
            batt.setImageDrawable(getDrawable(R.drawable.check_circle_outline_24px));
            return true;
        }

        return false;
    }

    private boolean checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            ImageView gps = findViewById(R.id.gpsAccessImage);
            gps.setImageDrawable(getDrawable(R.drawable.check_circle_outline_24px));
            return true;
        } else {
            requestLocationPermission();
        }
        return false;
    }

    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_FINE_LOCATION);
        }
    }

    private boolean checkFilePermission() {
        if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            ImageView file = findViewById(R.id.fileAccessImage);
            file.setImageDrawable(getDrawable(R.drawable.check_circle_outline_24px));
            return true;
        } else {
            requestFilePermission();
        }
        return false;
    }

    private void requestFilePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
    }

    private boolean checkInternetPermission() {
        if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED) {
            ImageView internet = findViewById(R.id.internetAccessImage);
            internet.setImageDrawable(getDrawable(R.drawable.check_circle_outline_24px));
            return true;
        } else {
            requestInternetPermission();
        }
        return false;
    }

    private void requestInternetPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.INTERNET)) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.INTERNET}, 0);
        }
    }

    private void continuePressed() {
        SharedPreferences.Editor editor = pref.edit();
        CheckBox donot = findViewById(R.id.doNotShowCheck);
        editor.putBoolean(getString(R.string.donotshow), donot.isChecked());
        editor.apply();

        Intent intent = new Intent(getBaseContext(), MainScreen.class);
        startActivity(intent);
        finish();
    }

    public void continuePressed(View view) {
        continuePressed();
    }

    public void openBatterySettings(View view) {
        Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
        startActivityForResult(intent, 0);
    }

    public void openPermissionSettings(View view) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 0);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateCheck();
    }

    public void onPause() {
        super.onPause();
    }
}