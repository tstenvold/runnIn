package t00212844.comp2161.afinal;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mapbox.api.staticmap.v1.MapboxStaticMap;
import com.mapbox.api.staticmap.v1.StaticMapCriteria;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ActivitiesAdapter extends RecyclerView.Adapter<ActivitiesAdapter.ViewHolder> {

    private static final int DEFAULT_ZOOM = 13;
    private final ArrayList<File> jsonFiles;

    public ActivitiesAdapter(ArrayList<File> sList) {
        jsonFiles = sList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activites_row, parent, false);
        return new ViewHolder(view);
    }

    private static Point calculateMidpointMapImage(List<Point> points) {

        int midpoint = (points.size() / 2) - 1;

        double longf = (points.get(0).longitude() + points.get(midpoint).longitude()) / 2;
        double latf = (points.get(0).latitude() + points.get(midpoint).latitude()) / 2;
        double longe = (points.get(points.size() - 1).longitude() + points.get(midpoint).longitude()) / 2;
        double late = (points.get(points.size() - 1).latitude() + points.get(midpoint).latitude()) / 2;

        double finalLong = (longf + longe) / 2;
        double finalLat = (latf + late) / 2;

        return Point.fromLngLat(finalLong, finalLat);
    }

    @Override
    public int getItemCount() {
        return jsonFiles.size();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Context context = holder.itemView.getContext();
        File file = jsonFiles.get(position);
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyy HH:mm", Locale.CANADA);

        holder.activityName.setText(file.getName().substring(0, file.getName().length() - 5));
        holder.date.setText(format.format(file.lastModified()));
        holder.time.setText("22:20");
        holder.distance.setText("10km");

        File png = new File(context.getFilesDir(), file.getName().substring(0, file.getName().length() - 5) + ".png");


        if (!png.exists()) {
            try {
                List<Point> points = GeoJsonHandler.getFilePoints(file);
                if (points.size() == 0) {
                    points.add(Point.fromLngLat(-120.364049, 50.670493));
                }
                LineString lineString = LineString.fromLngLats(points);

                Point pointMid = calculateMidpointMapImage(points);
                MapboxStaticMap staticImage = MapboxStaticMap.builder()
                        .accessToken(context.getString(R.string.access_token))
                        .styleId(StaticMapCriteria.STREET_STYLE)
                        //Loads the map center on the middle point of run. Crude but should be ok
                        .cameraPoint(pointMid)
                        .cameraZoom(DEFAULT_ZOOM)
                        .width(720) // Image width
                        .height(720) // Image height
                        .geoJson(lineString)
                        .build();

                String imageUrl = staticImage.url().toString();
                Picasso.with(context).load(imageUrl).into(picassoImageTarget(context, png.getAbsolutePath()));
                Picasso.with(context).load(imageUrl).into(holder.mapView);

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Bitmap myBitmap = BitmapFactory.decodeFile(png.getAbsolutePath());
            if (myBitmap != null) {
                holder.mapView.setImageBitmap(myBitmap);
            } else {
                png.delete();
            }
            //remove on release. This deletes the cached images for testing
            png.delete();
        }

    }

    /**
     * Method copied from https://www.codexpedia.com/android/android-download-and-save-image-through-picasso/
     * All rights to the original author. Slightly modified to suit circumstances
     *
     * @param context   the context
     * @param imageName the path to the image
     * @return returns a Target that is overrrided to write to a file
     */
    private Target picassoImageTarget(Context context, final String imageName) {
        ContextWrapper cw = new ContextWrapper(context);

        return new Target() {
            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                new Thread(() -> {
                    final File myImageFile = new File(imageName); // Create image file
                    FileOutputStream fos = null;
                    try {
                        myImageFile.createNewFile();
                        fos = new FileOutputStream(myImageFile);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }).start();
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }

        };
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public final TextView activityName;
        public final TextView time;
        public final TextView date;
        public final TextView distance;
        public final ImageView mapView;
        public final ImageView avatar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            activityName = itemView.findViewById(R.id.tv_Name);
            time = itemView.findViewById(R.id.tv_time);
            distance = itemView.findViewById(R.id.tv_distance);
            date = itemView.findViewById(R.id.tv_date);
            mapView = itemView.findViewById(R.id.row_mapView);
            avatar = itemView.findViewById(R.id.avatarImage);

        }
    }
}