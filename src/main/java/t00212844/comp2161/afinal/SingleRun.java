package t00212844.comp2161.afinal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.mapbox.api.staticmap.v1.MapboxStaticMap;
import com.mapbox.api.staticmap.v1.StaticMapCriteria;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class SingleRun extends AppCompatActivity {

    ImageView mapView;
    ImageView avatarView;
    TextView distanceTextView;
    TextView timeTextView;
    TextView dateTextView;
    TextView elgainTextView;
    TextView elmaxTextView;
    TextView elminTextView;
    TextView calTextView;
    TextView paceTextView;
    TextView paceFastTextView;
    MenuItem deleteItem;
    MenuItem shareItem;
    GraphView graphel;
    GraphView graphspeed;
    Toolbar toolbar;

    private double distance;
    private String runName;
    private long pace;
    private long paceFast;
    private long time;
    private long elmax;
    private long elmin;
    private long elgain;
    private int calories;
    private ArrayList<Location> gpsTrack;
    private ArrayList<String> jsonProp;
    private int lineGraphSpace = 0;

    private boolean unitsMetric;
    private String smallUnit;
    private String bigUnit;
    private String paceUnit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_run);
        File file;

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        mapView = findViewById(R.id.runMap);
        avatarView = findViewById(R.id.runAvatarImage);
        distanceTextView = findViewById(R.id.runDistance);
        timeTextView = findViewById(R.id.runTime);
        dateTextView = findViewById(R.id.runDate);
        elgainTextView = findViewById(R.id.runElGain);
        elmaxTextView = findViewById(R.id.runMaxEl);
        elminTextView = findViewById(R.id.runMinEl);
        paceTextView = findViewById(R.id.runPace);
        paceFastTextView = findViewById(R.id.runFastPace);
        calTextView = findViewById(R.id.runCal);
        graphel = findViewById(R.id.elgraph);
        graphspeed = findViewById(R.id.speedgraph);

        //TODO load from file path all the variables
        String filepath = getIntent().getStringExtra(getString(R.string.filepath));
        if (filepath != null) {
            file = new File(getFilesDir(), filepath);
            try {
                gpsTrack = GeoJsonHandler.readJson(file);
                jsonProp = GeoJsonHandler.readJsonProperties(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Bundle bundle = getIntent().getBundleExtra("BUNDLE");
            gpsTrack = (ArrayList<Location>) bundle.getSerializable("ARRAYLIST");
            runName = bundle.getString("runname");
            time = AnalyzeActivity.getTime(gpsTrack);
            distance = AnalyzeActivity.getDistance(gpsTrack);
            NumberFormat format = new DecimalFormat("0.00");
            file = new File(getFilesDir(), runName + "_" + format.format(distance) + "_" +
                    DateFormat.format("dd_MM_yyyy", Calendar.getInstance().getTime()).toString()
                    + "_" + time
                    + ".json");

        }

        setUnits();

        if (jsonProp != null && jsonProp.size() == 4) {
            runName = jsonProp.get(0);
        }

        if (gpsTrack != null) {
            //TODO this probably needs to be handled Async
            distance = AnalyzeActivity.getDistanceInKm(gpsTrack);
            time = AnalyzeActivity.getTime(gpsTrack);
            pace = AnalyzeActivity.getOverallPace(gpsTrack);
            paceFast = AnalyzeActivity.getFastestPace(gpsTrack);
            elgain = AnalyzeActivity.getElevationGain(gpsTrack);
            elmax = AnalyzeActivity.getElevationHigh(gpsTrack);
            elmin = AnalyzeActivity.getElevationLow(gpsTrack);
            //TODO get Weight from sharedpref
            calories = AnalyzeActivity.getCaloriesBurned(70, time, pace);
        }

        File mapFile = new File(getBaseContext().getFilesDir(), file.getName().substring(0, file.getName().length() - 5) + ".png");
        if (!mapFile.exists()) {
            getMapView(file);
        }

        Bitmap mapBitmap = BitmapFactory.decodeFile(mapFile.getAbsolutePath());
        if (mapBitmap != null) {
            mapView.setImageBitmap(mapBitmap);
        }

        File avatarFile = new File(getBaseContext().getFilesDir(), getString(R.string.avatarpath));
        Bitmap avaBitmap = BitmapFactory.decodeFile(avatarFile.getAbsolutePath());
        if (avaBitmap != null) {
            avatarView.setImageBitmap(avaBitmap);
        }

        NumberFormat decimalFormat = new DecimalFormat("0.00");
        NumberFormat numberFormat = new DecimalFormat("0");
        distanceTextView.setText(decimalFormat.format(distance) + bigUnit);

        timeTextView.setText(AnalyzeActivity.getTimeString(time));
        dateTextView.setText(DateFormat.format("dd/MM/yyyy HH:mm", gpsTrack.get(0).getTime()));
        elgainTextView.setText(numberFormat.format(elgain) + smallUnit);
        elmaxTextView.setText(numberFormat.format(elmax) + smallUnit);
        elminTextView.setText(numberFormat.format(elmin) + smallUnit);
        paceTextView.setText(DateFormat.format("mm:ss", pace) + paceUnit);
        paceFastTextView.setText(DateFormat.format("mm:ss", paceFast) + paceUnit);
        calTextView.setText(numberFormat.format(calories) + " " + getString(R.string.kcal));

        getSupportActionBar().setTitle(runName);
        drawGraph(graphel, 0);
        drawGraph(graphspeed, 1);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.action_share) {

            Bitmap bitmap = getScreenshot(this.getWindow().getDecorView());
            String path = Environment.getExternalStorageDirectory().toString() + "/RunOverview.jpeg";
            File file = new File(path);

            FileOutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                outputStream.flush();
                outputStream.close();
                bitmap.recycle();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri uri = FileProvider.getUriForFile(SingleRun.this, BuildConfig.APPLICATION_ID + ".provider", file);
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.setType("image/jpg");
            shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Check out at my recent run");
            startActivity(Intent.createChooser(shareIntent, null));

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.single_run_menu, menu);
        deleteItem = menu.findItem(R.id.action_delete);
        shareItem = menu.findItem(R.id.action_share);
        return true;
    }

    private void getMapView(File file) {
        File png = new File(getFilesDir(), file.getName().substring(0, file.getName().length() - 5) + ".png");

        if (!png.exists()) {
            new Thread(() -> {
                // a potentially time consuming task
                mapView.post(() -> {
                    try {
                        List<Point> points = GeoJsonHandler.getFilePoints(file);
                        if (points.size() == 0) {
                            points.add(Point.fromLngLat(-120.364049, 50.670493));
                        }
                        LineString lineString = LineString.fromLngLats(points);

                        Point pointMid = AnalyzeActivity.calculateMidpointMapImage(points);
                        MapboxStaticMap staticImage = MapboxStaticMap.builder()
                                .accessToken(getString(R.string.access_token))
                                .styleId(StaticMapCriteria.STREET_STYLE)
                                //Loads the map center on the middle point of run. Crude but should be ok
                                .cameraPoint(pointMid)
                                .cameraAuto(true)
                                .retina(true)
                                .width(720) // Image width
                                .height(720) // Image height
                                .geoJson(lineString)
                                .build();

                        String imageUrl = staticImage.url().toString();
                        Picasso.with(getBaseContext()).load(imageUrl).into(
                                ActivitiesAdapter.picassoImageTarget(getBaseContext(), png.getAbsolutePath()));
                        Picasso.with(getBaseContext()).load(imageUrl).into(mapView);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }).start();
        }
    }

    private void drawGraph(GraphView graph, int type) {
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
        for (Location point : gpsTrack) {
            if (type == 0) {
                double distancetoLocation = AnalyzeActivity.getDistancetoLocation(point, gpsTrack);
                series.appendData(new DataPoint(distancetoLocation, (int) point.getAltitude()), true, gpsTrack.size() - 1);
            } else if (type == 1) {
                int index = gpsTrack.indexOf(point);
                if (index < gpsTrack.size() - 2 && index > 0) {
                    double distancetoLocation = AnalyzeActivity.getDistancetoLocation(point, gpsTrack);
                    double speed = AnalyzeActivity.getSpeed(gpsTrack.get(index - 1), point);
                    series.appendData(new DataPoint(distancetoLocation, speed), true, gpsTrack.size() - 1);
                }
            }
        }
        series.setColor(getColor(R.color.lineColor));

        graph.addSeries(series);
        graph.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.NONE);
        graph.getViewport().setDrawBorder(true);
        graph.setTitleColor(getColor(R.color.colorPrimaryDark));
        graph.setTitleTextSize(48f);
        graph.getGridLabelRenderer().setLabelVerticalWidth(128);

        if (type == 0) {
            graph.setTitle(getString(R.string.elevation));
            graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
                @Override
                public String formatLabel(double value, boolean isValueX) {
                    if (isValueX) {
                        return super.formatLabel(value, isValueX) + bigUnit;
                    } else {
                        return super.formatLabel(value, isValueX) + smallUnit;
                    }
                }
            });
        } else if (type == 1) {
            graph.setTitle(getString(R.string.speed));
            graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
                @Override
                public String formatLabel(double value, boolean isValueX) {
                    if (isValueX) {
                        return super.formatLabel(value, isValueX) + bigUnit;
                    } else {
                        return super.formatLabel(value, isValueX) + bigUnit + "/h";
                    }
                }
            });
        }
    }

    private Bitmap getScreenshot(View view) {

        View screenView = view.findViewById(R.id.fullview);

        graphel.setVisibility(View.INVISIBLE);
        graphspeed.setVisibility(View.INVISIBLE);
        deleteItem.setVisible(false);
        shareItem.setVisible(false);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
        screenView.setBackgroundColor(getColor(R.color.colorAccent));
        ScrollView scroll = view.findViewById(R.id.runScroll);
        scroll.scrollTo(0, 0);

        screenView.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(screenView.getDrawingCache());
        screenView.setDrawingCacheEnabled(false);

        graphel.setVisibility(View.VISIBLE);
        graphspeed.setVisibility(View.VISIBLE);
        deleteItem.setVisible(true);
        shareItem.setVisible(true);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        screenView.setBackgroundColor(getColor(android.R.color.transparent));

        return bitmap;
    }

    private void setUnits() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        unitsMetric = pref.getBoolean(getString(R.string.units), true);
        if (!unitsMetric) {
            smallUnit = " " + getString(R.string.yards);
            bigUnit = " " + getString(R.string.miles);
            paceUnit = " /" + getString(R.string.miles);
        } else {
            smallUnit = " " + getString(R.string.meters);
            bigUnit = " " + getString(R.string.kilometers);
            paceUnit = " /" + getString(R.string.kilometers);
        }

    }

}