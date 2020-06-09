package t00212844.comp2161.afinal;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;

public class ActivitiesAdapter extends RecyclerView.Adapter<ActivitiesAdapter.ViewHolder> {

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

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        File entry;
        entry = jsonFiles.get(position);

        holder.activityName.setText(entry.getName().substring(0, entry.getName().length() - 5));
        holder.time.setText("28:32");
        holder.distance.setText("5.26km");
    }

    @Override
    public int getItemCount() {
        return jsonFiles.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public final TextView activityName;
        public final TextView time;
        public final TextView distance;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            activityName = itemView.findViewById(R.id.tv_Name);
            time = itemView.findViewById(R.id.tv_time);
            distance = itemView.findViewById(R.id.tv_distance);
        }
    }
}