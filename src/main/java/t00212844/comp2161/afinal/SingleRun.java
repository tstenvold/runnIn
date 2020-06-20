package t00212844.comp2161.afinal;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.mapbox.api.staticmap.v1.MapboxStaticMap;
import com.mapbox.api.staticmap.v1.StaticMapCriteria;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class SingleRun extends AppCompatActivity {

    ImageView mapView;
    ImageView avatarView;
    TextView distanceTextView;
    TextView timeTextView;
    TextView dateTextView;
    TextView runNameTextView;
    TextView elgainTextView;
    TextView elmaxTextView;
    TextView elminTextView;
    TextView paceTextView;
    TextView paceFastTextView;
    private double distance;
    private String runName;
    private double pace;
    private double paceFast;
    private long time;
    private long elmax;
    private long elmin;
    private long elgain;
    private ArrayList<Location> gpsTrack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_run);
        File file = null;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(runName);

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

        //TODO load from file path all the variables
        String filepath = getIntent().getStringExtra(getString(R.string.filepath));
        if (filepath != null) {
            file = new File(getFilesDir(), filepath);
            try {
                gpsTrack = GeoJsonHandler.readJson(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Bundle bundle = getIntent().getBundleExtra("BUNDLE");
            gpsTrack = (ArrayList<Location>) bundle.getSerializable("ARRAYLIST");
            runName = bundle.getString("runname");
            NumberFormat format = new DecimalFormat("0.00");
            file = new File(getFilesDir(), runName + "_" + format.format(distance) + "_" +
                    DateFormat.format("dd_MM_yyyy", Calendar.getInstance().getTime()).toString()
                    + "_" + time
                    + ".json");

        }

        if (gpsTrack != null) {
            //TODO this probably needs to be handled Async
            distance = AnalyzeActivity.getDistance(gpsTrack);
            time = AnalyzeActivity.getTime(gpsTrack);
            pace = AnalyzeActivity.getOverallPace(gpsTrack);
            elgain = AnalyzeActivity.getElevationGain(gpsTrack);
        }

        assert file != null;
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

        NumberFormat decimalFormat = new DecimalFormat("0.0");
        NumberFormat numberFormat = new DecimalFormat("0");
        NumberFormat timeFormat = new DecimalFormat("00");
        distanceTextView.setText(numberFormat.format(distance));

        long hours = TimeUnit.MILLISECONDS.toHours(time);
        time -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(time);
        time -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(time);
        String timeString = timeFormat.format(hours) + ":" + timeFormat.format(minutes) + ":" + timeFormat.format(seconds);

        timeTextView.setText(timeString);
        dateTextView.setText(DateFormat.format("dd/MM/yyyy HH:mm", gpsTrack.get(0).getTime()));
        elgainTextView.setText(numberFormat.format(elgain));

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.single_run_menu, menu);
        return true;
    }

    private void getMapView(File file) {
        File png = new File(getFilesDir(), file.getName().substring(0, file.getName().length() - 5) + ".png");
        if (!png.exists()) {
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
                        .cameraZoom(14)
                        .width(720) // Image width
                        .height(720) // Image height
                        .geoJson(lineString)
                        .build();

                String imageUrl = staticImage.url().toString();
                Picasso.with(getBaseContext()).load(imageUrl).into(
                        ActivitiesAdapter.picassoImageTarget(getBaseContext(), png.getAbsolutePath()));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}