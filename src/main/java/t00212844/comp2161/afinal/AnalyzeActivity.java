package t00212844.comp2161.afinal;

import android.location.Location;

import com.mapbox.geojson.Point;

import java.util.ArrayList;
import java.util.List;

public class AnalyzeActivity {

    private static final int JOGMET = 11;

    public static double getDistance(ArrayList<Location> gpsTrack) {
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

    public static Point calculateMidpointMapImage(List<Point> points) {

        int midpoint = (points.size() / 2) - 1;

        double longf = (points.get(0).longitude() + points.get(midpoint).longitude()) / 2;
        double latf = (points.get(0).latitude() + points.get(midpoint).latitude()) / 2;
        double longe = (points.get(points.size() - 1).longitude() + points.get(midpoint).longitude()) / 2;
        double late = (points.get(points.size() - 1).latitude() + points.get(midpoint).latitude()) / 2;

        double finalLong = (longf + longe) / 2;
        double finalLat = (latf + late) / 2;

        return Point.fromLngLat(finalLong, finalLat);
    }

    public static int getLastElevation(ArrayList<Location> gpsTrack) {
        int el = 0;
        if (gpsTrack.size() > 1) {
            Location loc = gpsTrack.get(gpsTrack.size() - 1);
            el = (int) loc.getAltitude();
        }
        return el;
    }

    public static double getCaloriesBurned(int weight, int minutes) {
        //Weight is in kilograms
        //TODO make it capable of being dynamic to support walking to Running
        //estimates the calories burned during jogging
        double cal = ((JOGMET * 3.5 * weight) / 200) * minutes;
        return cal;
    }

    public static int getElevationGain(ArrayList<Location> gpsTrack) {
        int el = 0;

        if (gpsTrack.size() > 1) {
            for (int i = 0; i < gpsTrack.size() - 2; i++) {
                Location loc1 = gpsTrack.get(i);
                Location loc2 = gpsTrack.get(i + 1);
                if (loc2.getAltitude() > loc1.getAltitude()) {
                    el += loc2.getAltitude() - loc1.getAltitude();
                }
            }
        }
        return el;
    }

    public static double getOverallPace(ArrayList<Location> gpsTrack) {
        double duration = (double) getTime(gpsTrack);
        return (duration / 60000) / (getDistance(gpsTrack) / 1000);
    }

    public static long getTime(ArrayList<Location> gpsTrack) {
        long start;
        long end;
        if (gpsTrack.size() > 1) {
            start = gpsTrack.get(0).getTime();
            end = gpsTrack.get(gpsTrack.size() - 1).getTime();
            return (end - start);
        }
        return 0;
    }


}
