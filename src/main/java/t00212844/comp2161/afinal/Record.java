package t00212844.comp2161.afinal;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.text.InputType;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
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
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.maps.SupportMapFragment;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

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
    private int curUnitDistance = 0;
    private Chronometer chronometer;
    private FloatingActionButton play;
    private FloatingActionButton resume;
    private View mLayout;
    private LocationManager locationManager;
    private ArrayList<Location> gpsTrack;
    private List<Point> mapTrack;
    private TextView tvDistance;
    private TextView tvCalories;
    private TextView tvResume;
    private TextView tvElevation;
    private TextView tvPace;
    private TextView tvEnd;
    private CountDownTimer timer;
    private MapboxMap mapboxMap;
    private TextToSpeech tts;
    NotificationManager notificationManager;
    private int voicecmd;
    private boolean unitsMetric;
    private String smallUnit;
    private String bigUnit;
    private String paceUnit;
    private String unitString;

    public Record() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        createNotificationChannel();

        mLayout = findViewById(R.id.main_layout);
        chronometer = findViewById(R.id.textView_Time);
        tvDistance = findViewById(R.id.textView_Distance);
        tvCalories = findViewById(R.id.textkcal);
        play = findViewById(R.id.button_record);
        FloatingActionButton myLoc = findViewById(R.id.fab_mylocation);
        resume = findViewById(R.id.button_resume);
        tvResume = findViewById(R.id.tvResume);
        tvEnd = findViewById(R.id.tvEndRun);
        tvPace = findViewById(R.id.textView_Pace);
        tvElevation = findViewById(R.id.textView_Elevation);
        isRunning = false;
        gpsTrack = new ArrayList<>();
        mapTrack = new ArrayList<>();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        setUnits();

        tts = new TextToSpeech(getApplicationContext(), status -> {
            if (status != TextToSpeech.ERROR) {
                tts.setLanguage(Locale.CANADA);
            }
        });

        myLoc.setOnClickListener(view -> {
            //Move back to previous location
            if (mapboxMap.getLocationComponent().isLocationComponentActivated()) {
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

        play.setOnClickListener(view -> {
            if (!isRunning && tvEnd.getVisibility() == View.VISIBLE) {
                endRun(view);
            } else if (!isRunning) {
                startRecording();
            } else {
                pauseRecording();
            }
        });

        resume.setOnClickListener(view -> {
            if (!isRunning) {
                startRecording();
            }
        });

        //Extremely hacky way to do this but it works for now
        //TODO clean up this code into proper methods
        if (savedInstanceState != null) {
            pausedTime = savedInstanceState.getLong(getString(R.string.time), 0);
            gpsTrack = savedInstanceState.getParcelableArrayList(getString(R.string.gps));
            isRunning = savedInstanceState.getBoolean(getString(R.string.isRunning), false);

            for (Location location : gpsTrack) {
                mapTrack.add(Point.fromLngLat(location.getLongitude(), location.getLatitude()));
            }

            if (notificationManager != null) {
                notificationManager.cancel(0);
            }

            if (isRunning) {
                isRunning = false;
                play.performClick();
            } else if (pausedTime != 0) {
                tvEnd.setVisibility(View.VISIBLE);
                tvResume.setVisibility(View.VISIBLE);
                resume.setVisibility(View.VISIBLE);
                play.setImageResource(0);
                //updates the timer to be displayed removed 10ms time to allow display to update
                chronometer.setBase(SystemClock.elapsedRealtime() + pausedTime - 10);
                chronometer.start();
                chronometer.stop();
                runBackgroundCalculations(0);
            }
        }
        initMap(savedInstanceState);
    }

    private void initMap(Bundle savedInstanceState) {
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
            mapFragment.getMapAsync(mbox -> {
                mapboxMap = mbox;
                mapboxMap.setStyle(Style.OUTDOORS, style -> {
                    enableLocationComponent(style);
                    initLayers(style);
                    LineString lineString = LineString.fromLngLats(mapTrack);
                    Feature feature = Feature.fromGeometry(lineString);
                    GeoJsonSource geoJsonSource = new GeoJsonSource(getString(R.string.runGeoJsonId), feature);
                    style.addSource(geoJsonSource);
                });
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

    private void startRecording() {

        isRunning = true;
        play.setImageResource(android.R.drawable.ic_media_pause);
        tvEnd.setVisibility(View.INVISIBLE);
        resume.setVisibility(View.INVISIBLE);
        tvResume.setVisibility(View.INVISIBLE);
        chronometer.setBase(SystemClock.elapsedRealtime() + pausedTime);
        setTTSPref();
        if (voicecmd > 0 && pausedTime == 0) {
            speakTTSCommands(0);
        }
        if (voicecmd > 0 && pausedTime != 0) {
            speakTTSCommands(2);
        }

        requestLocation();
        chronometer.start();
        runBackgroundCalculations(5000);

    }

    private void pauseRecording() {

        isRunning = false;
        pausedTime = chronometer.getBase() - SystemClock.elapsedRealtime();
        play.setImageResource(0);
        tvEnd.setVisibility(View.VISIBLE);
        tvResume.setVisibility(View.VISIBLE);
        resume.setVisibility(View.VISIBLE);
        if (voicecmd > 0) {
            speakTTSCommands(1);
        }

        locationManager.removeUpdates(this);
        chronometer.stop();
        if (timer != null) {
            timer.cancel();
        }
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
            PermissionsManager permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @SuppressLint("MissingPermission") //permission check handled
    private void requestLocation() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String provider;
        int gps = pref.getInt(getString(R.string.gpsacc), 0);
        //GPS accuracy Auto
        if (gps == 0) {
            float battery = getBatteryPercentage();
            if (battery > 65) {
                gps = 3;
            } else if (battery > 40) {
                gps = 2;
            } else {
                gps = 1;
            }
        }

        if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            switch (gps) {
                case 1:
                    criteria.setPowerRequirement(Criteria.POWER_LOW);
                    provider = locationManager.getBestProvider(criteria, true);
                    assert provider != null;
                    locationManager.requestLocationUpdates(provider, 25000, 30, this);
                    break;
                case 2:
                    criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
                    provider = locationManager.getBestProvider(criteria, true);
                    assert provider != null;
                    locationManager.requestLocationUpdates(provider, 12000, 15, this);
                    break;
                case 3:
                    criteria.setPowerRequirement(Criteria.POWER_HIGH);
                    provider = locationManager.getBestProvider(criteria, true);
                    assert provider != null;
                    locationManager.requestLocationUpdates(provider, 5000, 5, this);
                    break;
            }
        } else {
            requestLocationPermission();
        }
    }

    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            Snackbar.make(mLayout, R.string.request_location, Snackbar.LENGTH_INDEFINITE).setAction(R.string.ok, view -> ActivityCompat.requestPermissions((Activity) getBaseContext(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_FINE_LOCATION)).show();

        } else {
            Snackbar.make(mLayout, R.string.location_unavailable, Snackbar.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_FINE_LOCATION);
        }
    }

    @Override
    public void onLocationChanged(Location location) {

        if (isRunning && mapboxMap != null) {
            gpsTrack.add(location);
            mapTrack.add(Point.fromLngLat(location.getLongitude(), location.getLatitude()));
            mapboxMap.getLocationComponent().forceLocationUpdate(location);
            Location lastKnownLocation = mapboxMap.getLocationComponent().getLastKnownLocation();

            //TODO fix this so it only does it after the view is reloaded
            assert lastKnownLocation != null;
            CameraPosition position = new CameraPosition.Builder()
                    .target(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()))
                    .build();

            mapboxMap.getStyle(style -> {
                GeoJsonSource source = style.getSourceAs(getString(R.string.runGeoJsonId));
                if (source != null) {
                    source.setGeoJson(LineString.fromLngLats(mapTrack));
                }
            });
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

                //TODO make units smaller than rest of text
                if (gpsTrack.size() > 1 && isRunning) {

                    double distance = AnalyzeActivity.getDistanceInKm(gpsTrack);
                    format = new DecimalFormat("0.0");
                    if (!unitsMetric) {
                        distance = (distance / 1.609);
                    }
                    String distanceText = format.format(distance);
                    distanceText += " " + bigUnit;

                    format = new DecimalFormat("0");
                    long pace = AnalyzeActivity.getOverallPace(gpsTrack);
                    if (!unitsMetric) {
                        pace = (long) (pace * 1.60934);
                    }
                    String paceString = DateFormat.format("mm:ss", pace) + paceUnit;

                    int elgain = AnalyzeActivity.getElevationGain(gpsTrack, unitsMetric);
                    String elText = elgain + smallUnit;

                    tvPace.setText(paceString);
                    tvDistance.setText(distanceText);
                    tvCalories.setText(format.format(AnalyzeActivity.getCaloriesBurned(70, AnalyzeActivity.getTime(gpsTrack), pace)));
                    tvElevation.setText(elText);

                    if ((int) distance > curUnitDistance && voicecmd > 0) {
                        curUnitDistance = (int) distance;
                        String speakText = getString(R.string.currentdistance) + " " + curUnitDistance + " " + unitString;
                        if (curUnitDistance == 1) {
                            speakText = speakText.substring(0, speakText.length() - 1);
                        }
                        if (voicecmd == 2) {
                            String minutes = " " + DateFormat.format("m", pace) + " " + getString(R.string.minutes) + " ";
                            String seconds = DateFormat.format("s", pace) + " " + getString(R.string.seconds) + " per ";
                            speakText += ". " + getString(R.string.currentpace) + minutes + seconds + unitString.substring(0, unitString.length() - 1);
                        }
                        tts.speak(speakText, TextToSpeech.QUEUE_FLUSH, null, "utterance-id");
                    }
                }
            }

            @Override
            public void onFinish() {
                runBackgroundCalculations(5000);
            }
        }.start();
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {

    }

    @Override
    public void onPermissionResult(boolean granted) {

    }

    public Bundle buildSaveStateBundle() {
        Bundle state = new Bundle();
        if (isRunning) {
            pausedTime = chronometer.getBase() - SystemClock.elapsedRealtime();
        }
        if (timer != null) {
            timer.cancel();
        }
        state.putLong(getString(R.string.time), pausedTime);
        state.putParcelableArrayList(getString(R.string.gps), gpsTrack);
        state.putBoolean(getString(R.string.isRunning), isRunning);
        return state;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putAll(buildSaveStateBundle());
    }

    private void endRun(View view) {
        final String[] userRunName = new String[1];
        String runName = generateRunName();
        if (voicecmd > 0) {
            speakTTSCommands(3);
        }
        if (AnalyzeActivity.getDistanceInKm(gpsTrack) < 0.1 || gpsTrack.size() < 2) {
            Toast.makeText(getBaseContext(), "Run is not long enough to save", Toast.LENGTH_LONG).show();
            finish();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
            builder.setTitle(getString(R.string.enterrunname));
            final EditText input = new EditText(view.getContext());
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
            input.setHint(runName);
            builder.setView(input);
            builder.setCancelable(true);
            builder.setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                if (input.getText().toString().equals("")) {
                    userRunName[0] = runName;
                } else {
                    userRunName[0] = input.getText().toString();
                }
                final Handler handler = new Handler();
                Runnable runnable = () -> {
                    try {
                        GeoJsonHandler.writeJson(view.getContext(), gpsTrack, userRunName[0]);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                };
                runnable.run();

                Intent intent = new Intent(view.getContext(), SingleRun.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("ARRAYLIST", gpsTrack);
                bundle.putString("runname", userRunName[0]);
                intent.putExtra("BUNDLE", bundle);
                view.getContext().startActivity(intent);
                finish();
            });
            builder.setNegativeButton(R.string.cancel, (dialog, which) -> {
                Toast.makeText(getBaseContext(), "Run not saved", Toast.LENGTH_LONG).show();
                finish();
            });
            builder.show();
        }
    }

    private String generateRunName() {
        String runName = "";

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour >= 6 && hour < 12) {
            runName = getString(R.string.morningrun);
        } else if (hour >= 12 && hour < 16) {
            runName = getString(R.string.afternoonrun);
        } else if (hour >= 16 && hour < 21) {
            runName = getString(R.string.eveningrun);
        } else if (hour >= 21) {
            runName = getString(R.string.laterun);
        } else if (hour > 0) {
            runName = getString(R.string.earlyrun);
        }
        return runName;
    }

    public void openSettings(View view) {
        Intent intent = new Intent(view.getContext(), Record_Settings.class);
        view.getContext().startActivity(intent);
    }

    private void speakTTSCommands(int position) {
        String speakText = "";
        switch (position) {
            case 0:
                speakText = getString(R.string.starting);
                break;
            case 1:
                speakText = getString(R.string.pausing);
                break;
            case 2:
                speakText = getString(R.string.resuming);
                break;
            case 3:
                speakText = getString(R.string.ending);
                break;
        }
        if (tts != null) {
            tts.speak(speakText, TextToSpeech.QUEUE_FLUSH, null, "utterance-id");
        }
    }

    public float getBatteryPercentage() {

        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = getApplicationContext().registerReceiver(null, ifilter);

        assert batteryStatus != null;
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        return level * 100 / (float) scale;
    }

    private void setTTSPref() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        voicecmd = pref.getInt(getString(R.string.voicecmd), 0);
    }

    private void setUnits() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        unitsMetric = pref.getBoolean(getString(R.string.units), true);
        if (!unitsMetric) {
            unitString = getString(R.string.milesString);
            smallUnit = " " + getString(R.string.feet);
            bigUnit = " " + getString(R.string.miles);
            paceUnit = " /" + getString(R.string.miles);
        } else {
            unitString = getString(R.string.kilometersString);
            smallUnit = " " + getString(R.string.meters);
            bigUnit = " " + getString(R.string.kilometers);
            paceUnit = " /" + getString(R.string.kilometers);
        }

    }

    @Override
    public void onBackPressed() {
        if (isRunning || resume.getVisibility() == View.VISIBLE) {
            Toast.makeText(getBaseContext(), getString(R.string.endExit), Toast.LENGTH_SHORT).show();
        } else {
            finish();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isRunning) {
            createNotification();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (notificationManager != null) {
            notificationManager.cancel(0);
        }
    }

    public void createNotification() {
        Intent intent = new Intent(this, Record.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, getString(R.string.app_name))
                .setSmallIcon(R.drawable.icon)
                .setContentTitle(getString(R.string.app_name))
                .setNotificationSilent()
                .setContentText("Currently recording your run")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(0, builder.build());

    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(getString(R.string.app_name), name,
                    importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviours after this
            notificationManager = getSystemService(NotificationManager.class);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);
        }
    }
}