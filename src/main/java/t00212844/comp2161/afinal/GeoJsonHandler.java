package t00212844.comp2161.afinal;

import android.content.Context;
import android.location.Location;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

public class GeoJsonHandler {

    public static ArrayList<File> getJsonFiles(Context context) {

        FileFilter ff = new FileFilter() {
            public boolean accept(File file) {
                return file.getName().endsWith(".json");
            }
        };

        File[] ls = context.getFilesDir().listFiles(ff);
        ArrayList<File> files = new ArrayList<>(Arrays.asList(ls));

        return files;

    }

    public void writeJson(File file, ArrayList<Location> gpsTrack) throws IOException {
        int num = 0;
        Log.v("File Written to", file.getAbsolutePath());
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

    public ArrayList<Location> readJson(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        JsonReader reader = new JsonReader(new InputStreamReader(fis, StandardCharsets.UTF_16));
        Location loc;
        ArrayList<Location> gpsTrack = new ArrayList<>();
        long id = -1;

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
