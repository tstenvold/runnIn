package t00212844.comp2161.afinal;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.text.InputType;
import android.view.View;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
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
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.io.IOException;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;


public class Record extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback, LocationListener, PermissionsListener {

    private static final int PERMISSION_FINE_LOCATION = 0;
    private static final int ANIMATION_IMMEDIATE = 500;
    private final int DEFAULT_ZOOM = 14;
    private final int ANIMATION_SHORT = 3000;
    private boolean isRunning;
    private long pausedTime = 0;
    private Chronometer chronometer;
    private FloatingActionButton play;
    private FloatingActionButton myLoc;
    private FloatingActionButton resume;
    private View mLayout;
    private LocationManager locationManager;
    private ArrayList<Location> gpsTrack;
    private List<Point> mapTrack;
    private TextView tvDistance;
    private TextView tvCalories;
    private TextView tvResume;
    private TextView tvEnd;
    private CountDownTimer timer;
    private MapboxMap mapboxMap;
    private PermissionsManager permissionsManager;
    private TextToSpeech tts;

    public Record() {
        // Required empty public constructor
    }

    public static Record newInstance(String param1, String param2) {
        Record fragment = new Record();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        mLayout = findViewById(R.id.main_layout);
        chronometer = findViewById(R.id.textView_Time);
        tvDistance = findViewById(R.id.textView_Distance);
        tvCalories = findViewById(R.id.textkcal);
        play = findViewById(R.id.button_record);
        myLoc = findViewById(R.id.fab_mylocation);
        resume = findViewById(R.id.button_resume);
        tvResume = findViewById(R.id.tvResume);
        tvEnd = findViewById(R.id.tvEndRun);
        isRunning = false;
        gpsTrack = new ArrayList<>();
        mapTrack = new ArrayList<>();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (savedInstanceState != null) {
            pausedTime = savedInstanceState.getLong(getString(R.string.time), 0);
            gpsTrack = savedInstanceState.getParcelableArrayList(getString(R.string.gps));
            isRunning = savedInstanceState.getBoolean(getString(R.string.isRunning), false);
        }

        //check power
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        String packageName = getPackageName();
        if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
            Toast.makeText(getBaseContext(), "Please Disable Battery Optimization for this Application", Toast.LENGTH_SHORT).show();
        }

        myLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Move back to previous location
                Location lastKnownLocation = mapboxMap.getLocationComponent().getLastKnownLocation();
                if (lastKnownLocation != null) {
                    CameraPosition position = new CameraPosition.Builder()
                            .target(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()))
                            .zoom(DEFAULT_ZOOM)
                            .bearing(0)
                            .build();

                    mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), ANIMATION_SHORT);
                }

            }
        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isRunning && tvEnd.getVisibility() == View.VISIBLE) {
                    endRun(view);
                } else if (!isRunning) {
                    startRecording();
                } else {
                    pauseRecording();
                }
            }
        });

        resume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isRunning) {
                    startRecording();
                }
            }
        });

        Mapbox.getInstance(getBaseContext(), getString(R.string.access_token));

        SupportMapFragment mapFragment;
        if (savedInstanceState == null) {

            final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            MapboxMapOptions options = MapboxMapOptions.createFromAttributes(getBaseContext(), null);
            options.logoEnabled(false);
            options.tiltGesturesEnabled(false);
            options.attributionEnabled(false);
            options.camera(new CameraPosition.Builder().target(new LatLng(50.670493, -120.364049)).zoom(DEFAULT_ZOOM).build());
            mapFragment = SupportMapFragment.newInstance(options);
            transaction.add(R.id.row_frag_container, mapFragment, getString(R.string.mapID));
            transaction.commit();

        } else {
            mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentByTag(getString(R.string.mapID));
        }

        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(@NonNull MapboxMap mbox) {
                    mapboxMap = mbox;
                    mapboxMap.setStyle(Style.OUTDOORS, new Style.OnStyleLoaded() {
                        @Override
                        public void onStyleLoaded(@NonNull Style style) {
                            enableLocationComponent(style);
                            initLayers(style);
                            LineString lineString = LineString.fromLngLats(mapTrack);
                            Feature feature = Feature.fromGeometry(lineString);
                            GeoJsonSource geoJsonSource = new GeoJsonSource(getString(R.string.runGeoJsonId), feature);
                            style.addSource(geoJsonSource);
                        }
                    });
                }
            });
        }
    }

    private void initLayers(@NonNull Style loadedMapStyle) {
        LineLayer routeLayer = new LineLayer(getString(R.string.runGeoJsonLayerId), getString(R.string.runGeoJsonId));

        routeLayer.setProperties(
                lineCap(Property.LINE_CAP_ROUND),
                lineJoin(Property.LINE_JOIN_ROUND),
                lineWidth(8f),
                lineColor(ContextCompat.getColor(getBaseContext(), R.color.lineColor))
        );
        loadedMapStyle.addLayer(routeLayer);
    }

    @SuppressWarnings({"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
// Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(getBaseContext())) {
            LocationComponent locationComponent = mapboxMap.getLocationComponent();
            locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions.builder(getBaseContext(), loadedMapStyle).build());
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
        if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(provider, 15000, 30, this);
        } else {
            requestLocationPermission();
        }
    }

    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            Snackbar.make(mLayout, R.string.request_location, Snackbar.LENGTH_INDEFINITE).setAction(R.string.ok, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ActivityCompat.requestPermissions((Activity) getBaseContext(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_FINE_LOCATION);
                }
            }).show();

        } else {
            Snackbar.make(mLayout, R.string.location_unavailable, Snackbar.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_FINE_LOCATION);
        }
    }

    private void startRecording() {

        isRunning = true;
        play.setImageResource(android.R.drawable.ic_media_pause);
        tvEnd.setVisibility(View.INVISIBLE);
        resume.setVisibility(View.INVISIBLE);
        tvResume.setVisibility(View.INVISIBLE);
        chronometer.setBase(SystemClock.elapsedRealtime() + pausedTime);
        requestLocation();
        chronometer.start();
        runBackgroundCalculations(30000);

    }

    private void pauseRecording() {

        isRunning = false;
        pausedTime = chronometer.getBase() - SystemClock.elapsedRealtime();
        play.setImageResource(0);
        tvEnd.setVisibility(View.VISIBLE);
        tvResume.setVisibility(View.VISIBLE);
        resume.setVisibility(View.VISIBLE);

        locationManager.removeUpdates(this);
        chronometer.stop();
        if (timer != null) {
            timer.cancel();
        }
    }

    @Override
    public void onLocationChanged(Location location) {

        if (isRunning) {
            gpsTrack.add(location);
            mapTrack.add(Point.fromLngLat(location.getLongitude(), location.getLatitude()));
            Location lastKnownLocation = mapboxMap.getLocationComponent().getLastKnownLocation();

            //TODO fix this so it only does it after the view is reloaded
            CameraPosition position = new CameraPosition.Builder()
                    .target(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()))
                    .build();

            mapboxMap.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    GeoJsonSource source = style.getSourceAs(getString(R.string.runGeoJsonId));
                    if (source != null) {
                        source.setGeoJson(LineString.fromLngLats(mapTrack));
                    }
                }
            });
            mapboxMap.getLocationComponent().forceLocationUpdate(location);
            mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), ANIMATION_IMMEDIATE);
        }
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
                double distance = AnalyzeActivity.getDistance(gpsTrack);
                String distanceText = "";

                if (distance < 1000) {
                    format = new DecimalFormat("##0");
                    distanceText = format.format(distance) + " " + getString(R.string.meters);
                    tvDistance.setText(distanceText);
                } else if (distance >= 1000) {
                    format = new DecimalFormat("0.00");
                    distanceText = format.format(distance / 1000) + " " + getString(R.string.kilometers);
                    tvDistance.setText(distanceText);
                }

                if (distance > 10) {
                    format = new DecimalFormat("00");
                    double pace = AnalyzeActivity.getOverallPace(gpsTrack);
                    int min = (int) pace;
                    int sec = (int) ((pace - min) * 60);
                    TextView tvPace = findViewById(R.id.textView_Pace);
                    tvPace.setText(format.format(min) + ":" + format.format(sec) + " min/km");

                    tvCalories.setText(format.format(AnalyzeActivity.getCaloriesBurned(70,
                            (int) (SystemClock.elapsedRealtime() - chronometer.getBase()) / 60000)));
                }

                //tts.speak("meters", TextToSpeech.QUEUE_FLUSH, null,"utterance-id");

                TextView tvElevation = findViewById(R.id.textView_Elevation);
                String elText = AnalyzeActivity.getElevationGain(gpsTrack) + " m";
                tvElevation.setText(elText);
            }

            @Override
            public void onFinish() {
                runBackgroundCalculations(30000);
            }
        }.start();
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {

    }

    @Override
    public void onPermissionResult(boolean granted) {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(getString(R.string.time), chronometer.getBase());
        outState.putParcelableArrayList(getString(R.string.gps), gpsTrack);
        outState.putBoolean(getString(R.string.isRunning), isRunning);
    }

    private void endRun(View view) {
        final String[] userRunName = new String[1];
        String runName = "Evening Run"; //TODO time of day welcome / Name of Run

        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle("Please Name the Run");
        final EditText input = new EditText(view.getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint(runName);
        builder.setView(input);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (input.getText().toString().equals("")) {
                    userRunName[0] = runName;
                } else {
                    userRunName[0] = input.getText().toString();
                }
                final Handler handler = new Handler();
                Runnable runnable = new Runnable() {
                    public void run() {
                        try {
                            GeoJsonHandler.writeJson(view.getContext(), gpsTrack, userRunName[0]);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                };
                runnable.run();

                Intent intent = new Intent(view.getContext(), SingleRun.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("ARRAYLIST", (Serializable) gpsTrack);
                bundle.putString("runname", runName);
                intent.putExtra("BUNDLE", bundle);
                view.getContext().startActivity(intent);
                finish();
            }
        });
        builder.show();
    }
}