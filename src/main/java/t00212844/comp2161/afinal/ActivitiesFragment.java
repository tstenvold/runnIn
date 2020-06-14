package t00212844.comp2161.afinal;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;

public class ActivitiesFragment extends Fragment {

    public ActivitiesFragment() {
        // Required empty public constructor
    }

    public static Fragment newInstance(String s, String s1) {
        ActivitiesFragment fragment = new ActivitiesFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //RecyclerView activities = Objects.requireNonNull(getActivity()).findViewById(R.id.rv_activities);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_activities, container, false);
        Context context = view.getContext();
        //final Context contextThemeWrapper = new ContextThemeWrapper(getActivity(), Objects.requireNonNull(context).getTheme());
        //LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);

        RecyclerView sRView = view.findViewById(R.id.rv_activities);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        sRView.setLayoutManager(layoutManager);
        ArrayList<File> files = GeoJsonHandler.getJsonFiles(context);
        RecyclerView.Adapter sAdapter = new ActivitiesAdapter(files);
        sRView.setAdapter(sAdapter);

        return view;
    }
}