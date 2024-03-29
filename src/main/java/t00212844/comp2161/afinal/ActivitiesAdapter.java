/*
 * Copyright (c) 2020.
 * Programmed by: Terence Stenvold
 * Version 1.0
 *
 */

package t00212844.comp2161.afinal;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.text.format.DateFormat;
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
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The adapter to display the recorded runs!
 */
@SuppressWarnings("UnusedAssignment")
public class ActivitiesAdapter extends RecyclerView.Adapter<ActivitiesAdapter.ViewHolder> {

    private final ArrayList<File> jsonFiles;
    private String bigUnit;

    /**
     * Constructor for the adapter
     *
     * @param sList the list of files (should be sorted by lastmodified descending
     */
    public ActivitiesAdapter(ArrayList<File> sList) {
        jsonFiles = sList;
    }

    /**
     * creates the view and inflates it.
     *
     * @param parent   the parent view group
     * @param viewType ignored but required
     * @return the view holder
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activites_row, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Get the number of runs
     *
     * @return the number of runs
     */
    @Override
    public int getItemCount() {
        return jsonFiles.size();
    }

    /**
     * Method copied from https://www.codexpedia.com/android/android-download-and-save-image-through-picasso/
     * All rights to the original author. Slightly modified to suit circumstances
     *
     * @param context   the context
     * @param imageName the path to the image
     * @return returns a Target that is overrrided to write to a file
     */
    public static Target picassoImageTarget(Context context, final String imageName) {
        ContextWrapper cw = new ContextWrapper(context);

        return new Target() {
            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                new Thread(() -> {
                    final File myImageFile = new File(imageName); // Create image file
                    FileOutputStream fos;
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

    /**
     * Loads all the information for each individual run activity being shown
     *
     * @param holder   the current view holder
     * @param position the item position
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Context context = holder.itemView.getContext();
        File file = jsonFiles.get(position);
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyy HH:mm", Locale.CANADA);
        NumberFormat decimalFormat = new DecimalFormat("0.00");
        File png = new File(context.getFilesDir(), file.getName().substring(0, file.getName().length() - 5) + ".png");
        File ava = new File(context.getFilesDir() + "/" + context.getString(R.string.avatarpath));

        new Thread(() -> holder.date.post(() -> {
            ArrayList<Location> point = new ArrayList<>();
            ArrayList<String> jsonProp = new ArrayList<>();

            try {
                jsonProp = GeoJsonHandler.readJsonProperties(file);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (jsonProp.size() == 5) {
                holder.activityName.setText(jsonProp.get(0));
                setUnits(holder, Boolean.parseBoolean(jsonProp.get(4)));
                long time = Long.parseLong(jsonProp.get(2).replace(".0", ""));
                holder.time.setText(AnalyzeActivity.getTimeString(time));
                String distanceText = decimalFormat.format(Double.parseDouble(jsonProp.get(1)) / 1000) + bigUnit;
                holder.distance.setText(distanceText);
            }

            try {
                point = GeoJsonHandler.readJson(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
            holder.date.setText(DateFormat.format("dd/MM/yyyy HH:mm", point.get(0).getTime()));

            if (ava.exists()) {
                Uri avatarpath = Uri.parse(ava.getAbsolutePath());
                holder.avatar.setImageURI(avatarpath);
            } else {
                holder.avatar.setImageDrawable(context.getDrawable(R.drawable.account_circle_24px));
            }

            holder.itemView.setOnClickListener(view -> {
                Intent intent = new Intent(view.getContext(), SingleRun.class);
                intent.putExtra(view.getContext().getString(R.string.filepath), file.getName());
                context.startActivity(intent);
            });

        })).start();

        if (!png.exists()) {
            new Thread(() -> holder.mapView.post(() -> {
                try {
                    List<Point> points = GeoJsonHandler.getFilePoints(file);
                    if (points.size() == 0) {
                        points.add(Point.fromLngLat(-120.364049, 50.670493));
                    }
                    LineString lineString = LineString.fromLngLats(points);

                    Point pointMid = AnalyzeActivity.calculateMidpointMapImage(points);
                    MapboxStaticMap staticImage = MapboxStaticMap.builder()
                            .accessToken(context.getString(R.string.access_token))
                            .styleId(StaticMapCriteria.OUTDOORS_STYLE)
                            .cameraAuto(true)
                            .attribution(false)
                            .width(1080) // Image width
                            .height(720) // Image height
                            .geoJson(lineString)
                            .build();

                    String imageUrl = staticImage.url().toString();
                    Picasso.with(context).load(imageUrl).into(picassoImageTarget(context, png.getAbsolutePath()));
                    Picasso.with(context).load(imageUrl).into(holder.mapView);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            })).start();
        } else {
            Bitmap myBitmap = BitmapFactory.decodeFile(png.getAbsolutePath());
            if (myBitmap != null) {
                holder.mapView.setImageBitmap(myBitmap);
            } else {
                png.delete();
            }
        }
    }

    /**
     * Sets the units depending on the preferences set
     *
     * @param holder the current view holder
     * @param units  the unit to be displayed imperial or metric
     */
    private void setUnits(ViewHolder holder, boolean units) {
        Context context = holder.itemView.getContext();
        String paceUnit;
        String smallUnit;
        if (!units) {
            smallUnit = " " + context.getString(R.string.feet);
            bigUnit = " " + context.getString(R.string.miles);
            paceUnit = " /" + context.getString(R.string.miles);
        } else {
            smallUnit = " " + context.getString(R.string.meters);
            bigUnit = " " + context.getString(R.string.kilometers);
            paceUnit = " /" + context.getString(R.string.kilometers);
        }

    }

    /**
     * The view holder class that provides all the variables with their handlers
     */
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
            avatar = itemView.findViewById(R.id.avatarImageView);

        }
    }
}