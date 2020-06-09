package t00212844.comp2161.afinal;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.view.View;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.maps.SupportMapFragment;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback, LocationListener, PermissionsListener {

    private static final int PERMISSION_FINE_LOCATION = 0;
    public boolean isRunning;
    private Chronometer chronometer;
    private FloatingActionButton play;
    private View mLayout;
    private LocationManager locationManager;
    private ArrayList<Location> gpsTrack;
    private TextView tvDistance;
    private CountDownTimer timer;
    private MapboxMap mapboxMap;
    private PermissionsManager permissionsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLayout = findViewById(R.id.main_layout);
        chronometer = findViewById(R.id.textView_Time);
        tvDistance = findViewById(R.id.textView_Distance);
        play = findViewById(R.id.button_record);
        isRunning = false;
        gpsTrack = new ArrayList<>();

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        play.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(getApplicationContext(), "Saving Run", Toast.LENGTH_SHORT).show();
                //TODO finish activity save to Json
                return false;
            }
        });

        Mapbox.getInstance(this, getString(R.string.access_token));

        SupportMapFragment mapFragment;
        if (savedInstanceState == null) {

            final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            MapboxMapOptions options = MapboxMapOptions.createFromAttributes(this, null);
            options.camera(new CameraPosition.Builder().target(new LatLng(50.670493, -120.364049)).zoom(15).build());

            mapFragment = SupportMapFragment.newInstance(options);

            transaction.add(R.id.location_frag_container, mapFragment, "com.mapbox.map");
            transaction.commit();

        } else {
            mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentByTag("com.mapbox.map");
        }

        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(@NonNull MapboxMap mapboxMap) {
                    MainActivity.this.mapboxMap = mapboxMap;
                    mapboxMap.setStyle(Style.OUTDOORS, new Style.OnStyleLoaded() {
                        @Override
                        public void onStyleLoaded(@NonNull Style style) {
                            enableLocationComponent(style);
                        }
                    });
                }
            });
        }

    }

    @SuppressWarnings({"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
// Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            LocationComponent locationComponent = mapboxMap.getLocationComponent();
            locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions.builder(this, loadedMapStyle).build());
            locationComponent.setLocationComponentEnabled(true);
            locationComponent.setCameraMode(CameraMode.TRACKING);
            locationComponent.setRenderMode(RenderMode.NORMAL);
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @SuppressLint("MissingPermission") //permission check handled
    private void requestLocation() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        String provider = locationManager.getBestProvider(criteria, true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(provider, 5000, 5, this);
        } else {
            requestLocationPermission();
        }
    }

    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            Snackbar.make(mLayout, R.string.request_location, Snackbar.LENGTH_INDEFINITE).setAction(R.string.ok, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, PERMISSION_FINE_LOCATION);
                }
            }).show();

        } else {
            Snackbar.make(mLayout, R.string.location_unavailable, Snackbar.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // BEGIN_INCLUDE(onRequestPermissionsResult)
        if (requestCode == PERMISSION_FINE_LOCATION) {
            // Request for camera permission.
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted. Start camera preview Activity.
                Snackbar.make(mLayout, R.string.location_allowed, Snackbar.LENGTH_SHORT).show();
            } else {
                // Permission request was denied.
                Snackbar.make(mLayout, R.string.location_unavailable, Snackbar.LENGTH_SHORT).show();
            }
        }
        // END_INCLUDE(onRequestPermissionsResult)
    }

    private void startTimer() {
        if (!isRunning) {
            isRunning = true;
            play.setImageResource(android.R.drawable.ic_media_pause);
            chronometer.setBase(SystemClock.elapsedRealtime());
            requestLocation();
            chronometer.start();

        } else {
            isRunning = false;
            play.setImageResource(android.R.drawable.ic_media_play);
            locationManager.removeUpdates(this);
            chronometer.stop();
        }
    }

    public void startRecording(View view) {
        startTimer();
        runBackgroundCalculations(30000);
    }

    @Override
    public void onLocationChanged(Location location) {
        gpsTrack.add(location);
        mapboxMap.getLocationComponent().forceLocationUpdate(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private void runBackgroundCalculations(int duration) {
        timer = new CountDownTimer(duration, 5000) {

            public void onTick(long millisUntilFinished) {
                NumberFormat format;
                double distance = calculateDistance();
                String distanceText = "";

                if (distance < 1000) {

                    format = new DecimalFormat("##0");
                    distanceText = format.format(distance) + " " + getString(R.string.meters);
                    tvDistance.setText(distanceText);
                } else if (distance >= 1000) {
                    format = new DecimalFormat("0.00");
                    distanceText = format.format(distance / 1000) + " " + getString(R.string.kilometers);
                    tvDistance.setText(distanceText);

                    format = new DecimalFormat("00");
                    double duration = (double) SystemClock.elapsedRealtime() - chronometer.getBase();
                    double pace = (duration / 60000) / (distance / 1000);
                    int min = (int) pace;
                    int sec = (int) ((pace - min) * 60);
                    TextView tvPace = findViewById(R.id.textView_Pace);
                    tvPace.setText(format.format(min) + ":" + format.format(sec) + " min/km");

                }

                TextView tvElevation = findViewById(R.id.textView_Elevation);
                String elText = getLastElevation() + " m";
                tvElevation.setText(elText);
            }

            @Override
            public void onFinish() {
                runBackgroundCalculations(30000);
            }
        }.start();
    }

    private int getLastElevation() {
        int el = 0;
        if (gpsTrack.size() > 1) {
            Location loc = gpsTrack.get(gpsTrack.size() - 1);
            el = (int) loc.getAltitude();
        }
        return el;
    }

    private double calculateDistance() {
        double distance = 0;
        int index = 0;
        Location cur;
        if (gpsTrack.size() > 1) {
            cur = gpsTrack.get(index++);
            while (index < gpsTrack.size() - 1) {
                float dis = cur.distanceTo(gpsTrack.get(index));
                cur = gpsTrack.get(index++);
                distance += dis;
            }
        }
        return distance;
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {

    }

    @Override
    public void onPermissionResult(boolean granted) {

    }
}