package t00212844.comp2161.afinal;

import android.location.Location;
import android.os.SystemClock;

import java.util.ArrayList;

public class AnalyzeActivity {

    private static final int JOGMET = 11;

    public static double calculateDistance(ArrayList<Location> gpsTrack) {
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

    public static int getLastElevation(ArrayList<Location> gpsTrack) {
        int el = 0;
        if (gpsTrack.size() > 1) {
            Location loc = gpsTrack.get(gpsTrack.size() - 1);
            el = (int) loc.getAltitude();
        }
        return el;
    }

    public static double getCaloriesBurned(int weight, int minutes) {

        //TODO make it capable of being dynamic to support walking to Running
        //estimates the calories burned during jogging
        double cal = JOGMET * 3.5 * weight / 200 * minutes;
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

    public static double getOverallPace(ArrayList<Location> gpsTrack, long time) {
        double duration = (double) SystemClock.elapsedRealtime() - time;

        return (duration / 60000) / (calculateDistance(gpsTrack) / 1000);
    }


}
