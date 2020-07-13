package t00212844.comp2161.afinal;

import android.location.Location;

import com.mapbox.geojson.Point;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AnalyzeActivity {

    private static final int JOGMET = 11;
    private static final double KMTOMIL = 1.609344;
    private static final double MTOYD = 1.09361;
    private static final double MPSTOMINKM = 16.6666667;
    private static final double MPSTOKMH = 3.6;

    public static double getDistance(ArrayList<Location> gpsTrack) {
        double distance = 0;
        int index = 0;
        Location cur;
        if (gpsTrack.size() > 1) {
            cur = gpsTrack.get(index);
            while (index < gpsTrack.size() - 1) {
                float dis = cur.distanceTo(gpsTrack.get(++index));
                cur = gpsTrack.get(index);
                distance += dis;
            }
        }
        return distance;
    }

    public static double getDistanceInKm(ArrayList<Location> gpsTrack) {

        return getDistance(gpsTrack) / 1000;
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

    public static double getSpeed(Location loc1, Location loc2) {

        ArrayList<Location> gpsTrack = new ArrayList<>();
        gpsTrack.add(loc1);
        gpsTrack.add(loc2);

        double distance = getDistance(gpsTrack);
        double time = getTime(gpsTrack);

        double mps = distance / (time / 1000);

        return mps * MPSTOKMH;
    }

    public static double getLastKMSpeed(ArrayList<Location> gpsTrack) {
        //returns speed in meters per second
        float speed = 0;
        double distance;
        double totalDistance = 0;
        double time;
        int numOfSpeeds = 0;

        if (gpsTrack.size() > 0) {
            for (int i = gpsTrack.size() - 1; i > 0; i--) {
                distance = gpsTrack.get(i).distanceTo(gpsTrack.get(i - 1));
                totalDistance += distance;
                time = gpsTrack.get(i).getTime() - gpsTrack.get(i - 1).getTime();
                speed += distance / (time / 1000);
                numOfSpeeds++;
                if (totalDistance > 1000) {
                    break;
                }
            }
        }
        return speed / numOfSpeeds;
    }

    public static long getLastKMPace(ArrayList<Location> gpsTrack) {

        return (long) (MPSTOMINKM / (getFastestSpeed(gpsTrack)) * 60000);
    }

    public static double getFastestSpeed(ArrayList<Location> gpsTrack) {
        float speed = 0;
        double distance;
        double totalDistance = 0;
        double time;
        int numOfSpeeds = 0;
        double fastestSpeed = 0;

        if (gpsTrack.size() > 1) {
            for (int i = 0; i < gpsTrack.size() - 2; i++) {
                distance = gpsTrack.get(i).distanceTo(gpsTrack.get(i + 1));
                totalDistance += distance;
                time = gpsTrack.get(i + 1).getTime() - gpsTrack.get(i).getTime();
                speed += distance / (time / 1000);
                numOfSpeeds++;
                if (totalDistance > 500) {
                    if (speed / numOfSpeeds > fastestSpeed || fastestSpeed == 0) {
                        fastestSpeed = speed / numOfSpeeds;
                    }
                    totalDistance = 0;
                    speed = 0;
                    numOfSpeeds = 0;
                }
            }
        }
        return fastestSpeed;
    }

    public static long getFastestPace(ArrayList<Location> gpsTrack) {

        return (long) (MPSTOMINKM / (getFastestSpeed(gpsTrack)) * 60000);
    }

    public static long getOverallPace(ArrayList<Location> gpsTrack) {
        double duration = (double) getTime(gpsTrack);
        return (long) (duration / (getDistance(gpsTrack) / 1000));
    }

    public static int getElevationGain(ArrayList<Location> gpsTrack) {
        int el = 0;

        if (gpsTrack.size() > 1) {
            for (int i = 0; i < gpsTrack.size() - 2; i++) {
                Location loc1 = gpsTrack.get(i);
                Location loc2 = gpsTrack.get(i + 1);
                if (loc2.getAltitude() > loc1.getAltitude() && loc2.getAltitude() != 0.0 && loc1.getAltitude() != 0.0) {
                    el += loc2.getAltitude() - loc1.getAltitude();
                }
            }
        }
        return el;
    }

    public static int getElevationLow(ArrayList<Location> gpsTrack) {
        double el = 0;

        if (gpsTrack.size() > 0) {
            el = gpsTrack.get(0).getAltitude();
            for (Location point : gpsTrack) {
                if (point.getAltitude() < el && point.getAltitude() != 0.0) {
                    el = point.getAltitude();
                } else if (el == 0.0) {
                    el = point.getAltitude();
                }
            }
        }
        return (int) el;
    }

    public static int getElevationHigh(ArrayList<Location> gpsTrack) {
        double el = 0;

        if (gpsTrack.size() > 0) {
            el = gpsTrack.get(0).getAltitude();
            for (Location point : gpsTrack) {
                if (point.getAltitude() > el) {
                    el = point.getAltitude();
                }
            }
        }
        return (int) el;
    }

    public static String getTimeString(ArrayList<Location> gpsTrack) {

        NumberFormat timeFormat = new DecimalFormat("00");
        long time = getTime(gpsTrack);

        long hours = TimeUnit.MILLISECONDS.toHours(time);
        time -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(time);
        time -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(time);
        return timeFormat.format(hours) + ":" + timeFormat.format(minutes) + ":" + timeFormat.format(seconds);
    }

    public static String getTimeString(long time) {

        NumberFormat timeFormat = new DecimalFormat("00");

        long hours = TimeUnit.MILLISECONDS.toHours(time);
        time -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(time);
        time -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(time);
        return timeFormat.format(hours) + ":" + timeFormat.format(minutes) + ":" + timeFormat.format(seconds);
    }

    public static int getCaloriesBurned(int weight, long time, long pace) {
        //Weight is in kilograms
        long minutes = TimeUnit.MILLISECONDS.toMinutes(time);
        double unit;
        //TODO make it capable of being dynamic to support walking to Running

        if (pace < 300000) {
            unit = JOGMET / ((double) pace / 300000);
        } else {
            unit = JOGMET * (300000 / (double) pace);
        }

        //ensures that calories metric never dips too low or too high depending on speed
        unit = unit > 26 ? 26 : unit;
        unit = unit < 6 ? 6 : unit;

        return (int) (((unit * 3.5 * weight) / 200) * minutes);
    }

    /**
     * Determines the distance in kilometers from the start to the point given.
     * mostly used for charts.
     *
     * @param point    the current position
     * @param gpsTrack the gps points
     * @return distance in km from start to Point
     */
    public static double getDistancetoLocation(Location point, ArrayList<Location> gpsTrack) {
        double distance = 0;
        for (int i = 0; i < gpsTrack.size() - 2; i++) {
            if (gpsTrack.get(i) == point) {
                break;
            }
            distance += gpsTrack.get(i).distanceTo(gpsTrack.get(i + 1));
        }
        return distance / 1000;
    }

    /**
     * Method provided by Peter Lawery @ https://stackoverflow.com/questions/23449662/java-round-to-nearest-5
     *
     * @param x        the number to be rounded
     * @param fraction the fraction by which to round the number
     * @return The rounded double
     */
    public static double roundToFraction(double x, long fraction) {
        return (double) Math.round(x * fraction) / fraction;
    }
}
