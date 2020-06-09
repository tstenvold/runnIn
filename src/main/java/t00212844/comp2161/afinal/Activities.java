package t00212844.comp2161.afinal;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;

public class Activities extends AppCompatActivity {

    private RecyclerView sRView;
    private RecyclerView.Adapter sAdapter;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activities);

        sRView = findViewById(R.id.rv_activities);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        sRView.setLayoutManager(layoutManager);
        ArrayList<File> files = GeoJsonHandler.getJsonFiles(getApplicationContext());
        sAdapter = new ActivitiesAdapter(files);
        sRView.setAdapter(sAdapter);
    }
}