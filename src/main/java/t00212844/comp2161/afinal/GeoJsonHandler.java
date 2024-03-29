/*
 * Copyright (c) 2020.
 * Programmed by: Terence Stenvold
 * Version 1.0
 *
 *
 */

package t00212844.comp2161.afinal;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.util.JsonReader;
import android.util.JsonWriter;

import com.mapbox.geojson.Point;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * The class the handles all of the GeoJson file input and output
 */
public class GeoJsonHandler {


    /**
     * Get a list of all the GeoJson files in the app data directory
     *
     * @param context the app context
     * @return an arraylist with all the Files
     */
    public static ArrayList<File> getJsonFiles(Context context) {

        FileFilter ff = file -> file.getName().endsWith(".json");
        File[] files = context.getFilesDir().listFiles(ff);
        assert files != null;
        Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());

        return new ArrayList<>(Arrays.asList(Objects.requireNonNull(files)));

    }

    /**
     * Reads through a files and returns a list of Points
     *
     * @param file the File to read
     * @return an List of Points
     * @throws IOException if the file cannot be read throws the exception
     */
    public static List<Point> getFilePoints(File file) throws IOException {
        ArrayList<Location> gps = readJson(file);
        List<Point> points = new ArrayList<>();
        for (Location loc : gps) {
            points.add(Point.fromLngLat(loc.getLongitude(), loc.getLatitude()));
        }
        return points;
    }

    /**
     * Write the GPS track to a GeoJson file with the runName given
     *
     * @param context  the app context
     * @param gpsTrack the gps track
     * @param runName  the name of the run
     * @throws IOException if the file cannot be read throws the exception
     */
    public static void writeJson(Context context, ArrayList<Location> gpsTrack, String runName) throws IOException {

        NumberFormat format = new DecimalFormat("0.00");
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

        boolean units = pref.getBoolean(context.getString(R.string.units), true);
        double distance = AnalyzeActivity.getDistance(gpsTrack);
        long time = AnalyzeActivity.getTime(gpsTrack);
        double pace = AnalyzeActivity.getOverallPace(gpsTrack);


        final File file = new File(context.getFilesDir(), runName + "_" + format.format(distance) + "_" +
                DateFormat.format("dd_MM_yyyy", Calendar.getInstance().getTime()).toString()
                + "_" + time
                + ".json");

        file.createNewFile();
        FileOutputStream fo = new FileOutputStream(file);
        JsonWriter writer = new JsonWriter(new OutputStreamWriter(fo, StandardCharsets.UTF_16));
        writer.setIndent("  ");

        writer.beginObject();
        writer.name("type").value("FeatureCollection");
        writer.name("features");
        writer.beginArray();
        writer.beginObject();
        writer.name("type").value("Feature");
        writer.name("properties");
        writer.beginObject();
        writer.name("name").value(runName);
        writer.name("distance").value(String.valueOf(distance));
        writer.name("time").value(String.valueOf(time));
        writer.name("pace").value(String.valueOf(pace));
        writer.name("units").value(String.valueOf(units));
        writer.endObject();
        writer.name("geometry");
        writer.beginObject();
        writer.name("type").value("LineString");
        writer.name("coordinates");
        writer.beginArray();
        for (Location loc : gpsTrack) {
            writer.beginArray();
            writer.value(loc.getLongitude());
            writer.value(loc.getLatitude());
            writer.value(loc.getAltitude());
            writer.value(loc.getTime());
            writer.endArray();
        }
        writer.endArray();
        writer.endObject();
        writer.endObject();
        writer.endArray();
        writer.endObject();
        writer.close();
    }


    /**
     * Basic information is stored in the GeoJson properties so the entire file does not need to be read.
     *
     * @param file the file to be read
     * @return return and array list of strings with the values
     * @throws IOException if the file cannot be read throws the exception
     */
    public static ArrayList<String> readJsonProperties(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        ArrayList<String> jsonprop = new ArrayList<>();
        JsonReader reader = new JsonReader(new InputStreamReader(fis, StandardCharsets.UTF_16));
        String runName = "";
        String distance = "";
        String time = "";
        String pace = "";
        String units = "true";

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("features")) {
                reader.beginArray();
                reader.beginObject();
                while (reader.hasNext()) {
                    String name3 = reader.nextName();
                    if (name3.equals("properties")) {
                        reader.beginObject();
                        while (reader.hasNext()) {
                            String name2 = reader.nextName();
                            switch (name2) {
                                case "name":
                                    runName = reader.nextString();
                                    break;
                                case "distance":
                                    distance = reader.nextString();
                                    break;
                                case "time":
                                    time = reader.nextString();
                                    break;
                                case "pace":
                                    pace = reader.nextString();
                                    break;
                                case "units":
                                    units = reader.nextString();
                                    break;
                                default:
                                    reader.skipValue();
                                    break;
                            }
                        }
                        reader.endObject();
                    } else {
                        reader.skipValue();
                    }
                }
                reader.endObject();
                reader.endArray();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();

        if (!runName.equals("")) {
            jsonprop.add(runName);
        }
        if (!distance.equals("")) {
            jsonprop.add(distance);
        }
        if (!time.equals("")) {
            jsonprop.add(time);
        }
        if (!pace.equals("")) {
            jsonprop.add(pace);
        }
        jsonprop.add(units);

        return jsonprop;
    }

    /**
     * Reads the entire GeoJson file and returns the GPS track (ArrayList of Locations)
     *
     * @param file the file to be read
     * @return the GPS track
     * @throws IOException throws the exception if the file cannot be read
     */
    public static ArrayList<Location> readJson(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        JsonReader reader = new JsonReader(new InputStreamReader(fis, StandardCharsets.UTF_16));
        Location loc;
        ArrayList<Location> gpsTrack = new ArrayList<>();

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("features")) {
                reader.beginArray();
                reader.beginObject();
                while (reader.hasNext()) {
                    String name2 = reader.nextName();
                    if (name2.equals("geometry")) {
                        reader.beginObject();
                        while (reader.hasNext()) {
                            String name3 = reader.nextName();
                            if (name3.equals("coordinates")) {
                                reader.beginArray();
                                while (reader.hasNext()) {
                                    reader.beginArray();
                                    loc = new Location(file.getName());
                                    loc.setLongitude(reader.nextDouble());
                                    loc.setLatitude(reader.nextDouble());
                                    loc.setAltitude(reader.nextDouble());
                                    loc.setTime(reader.nextLong());
                                    gpsTrack.add(loc);
                                    reader.endArray();
                                }
                                reader.endArray();
                            } else {
                                reader.skipValue();
                            }
                        }
                        reader.endObject();
                    } else {
                        reader.skipValue();
                    }
                }
                reader.endObject();
                reader.endArray();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();

        return gpsTrack;
    }
}
