package t00212844.comp2161.afinal;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
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

/**
 * Permissions welcome Screen
 */
public class PermissionsWelcome extends AppCompatActivity {

    private static final int PERMISSION_FINE_LOCATION = 0;
    private SharedPreferences pref;

    /**
     * Create the view and check permissions
     *
     * @param savedInstanceState bundle of saved state instance
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions_welcome);
        //Forced Portrait Orientation as it doesn't make sense to have a horizontal splash
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //set the check boxes if
        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (pref.getBoolean(getString(R.string.donotshow), false)) {
            CheckBox donot = findViewById(R.id.doNotShowCheck);
            donot.setChecked(true);
            continuePressed();
        }

        updateCheck();
    }

    /**
     * Checks to make sure all permissions are set correctly
     * Writes to log error if not.
     */
    private void updateCheck() {
        boolean permissionsGranted = true;
        //Check Internet Permissions and set flag
        if (!checkInternetPermission() ||
                !checkLocationPermission() ||
                !checkBattery() ||
                !checkLocationPermission() ||
                !checkFilePermission()) {
            permissionsGranted = false;
        }

        if (!permissionsGranted) {
            Log.e("permissions", "Not all permissions enabled");
        }
    }

    /**
     * Check if battery optimization is disabled
     *
     * @return boolean if battery optimization is disabled or not
     */
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

    /**
     * Checks if location permissions are enabled
     *
     * @return boolean if permission is enabled.
     */
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

    /**
     * Request location permission from user with a dialog box, and update
     */
    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_FINE_LOCATION);
        }
        updateCheck();
    }

    /**
     * Checks if file permissions are enabled
     *
     * @return boolean if permission is enabled.
     */
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

    /**
     * Request file permission from user with a dialog box, and update
     */
    private void requestFilePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
        updateCheck();
    }

    /**
     * Checks if internet permissions are enabled
     *
     * @return boolean if permission is enabled.
     */
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

    /**
     * Request internet permission from user with a dialog box, and update
     */
    private void requestInternetPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.INTERNET)) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.INTERNET}, 0);
        }
        updateCheck();
    }


    /**
     * Continue button clicked, move to the MainScreen Activity
     * Update Do Not Show preference
     */
    private void continuePressed() {
        SharedPreferences.Editor editor = pref.edit();
        CheckBox donot = findViewById(R.id.doNotShowCheck);
        editor.putBoolean(getString(R.string.donotshow), donot.isChecked());
        editor.apply();

        Intent intent = new Intent(getBaseContext(), MainScreen.class);
        startActivity(intent);
        finish();
    }

    /**
     * Method for the button click because the view is required
     */
    public void continuePressed(View view) {
        continuePressed();
    }

    /**
     * Open the battery settings if the icon is clicked
     */
    public void openBatterySettings(View view) {
        Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
        startActivityForResult(intent, 0);
    }

    /**
     * Open the permission settings if the icon is clicked
     */
    public void openPermissionSettings(View view) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 0);
    }

    /**
     * On resume update the checks
     */
    @Override
    public void onResume() {
        super.onResume();
        updateCheck();
    }

}