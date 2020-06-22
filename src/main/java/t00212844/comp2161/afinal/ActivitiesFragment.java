package t00212844.comp2161.afinal;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;

public class ActivitiesFragment extends Fragment {

    ArrayList<File> files;
    RecyclerView.Adapter sAdapter;

    public ActivitiesFragment() {
        // Required empty public constructor
    }

    final ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

        private Drawable icon;
        private ColorDrawable background;

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            icon = ContextCompat.getDrawable(recyclerView.getContext(), R.drawable.delete_sweep_24px);
            background = new ColorDrawable(getResources().getColor(R.color.colorPrimary, null));
            return false;
        }

        /**
         * Copied from and Modified from
         * https://stackoverflow.com/questions/55672351/recyclerview-swipe-to-delete-still-shows-drawable-with-uncomplete-swipe
         * all rights to the original author
         * @param c
         * @param recyclerView
         * @param viewHolder
         * @param dX
         * @param dY
         * @param actionState
         * @param isCurrentlyActive
         */
        @Override
        public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

            View itemView = viewHolder.itemView;
            icon = ContextCompat.getDrawable(recyclerView.getContext(), R.drawable.delete_sweep_24px);
            background = new ColorDrawable(getResources().getColor(R.color.lineColor, null));

            int iconMargin = icon.getIntrinsicHeight() * 2;
            int iconTop = itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight() * 2) / 2;
            int iconBottom = iconTop + icon.getIntrinsicHeight() * 2;

            if (dX > 0) { // Swiping to the right
                int iconLeft = itemView.getLeft() + iconMargin;
                int iconRight = iconLeft + icon.getIntrinsicWidth() * 2;
                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

                background.setBounds(itemView.getLeft(), itemView.getTop(),
                        itemView.getLeft() + ((int) dX), itemView.getBottom());

            } else if (dX < 0) { // Swiping to the left
                int iconLeft = itemView.getRight() - iconMargin - icon.getIntrinsicWidth() * 2;
                int iconRight = itemView.getRight() - iconMargin;
                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

                background.setBounds(itemView.getRight() + ((int) dX),
                        itemView.getTop(), itemView.getRight(), itemView.getBottom());

            } else { // view is unSwiped
                icon.setBounds(0, 0, 0, 0);
                background.setBounds(0, 0, 0, 0);
            }

            background.draw(c);
            icon.draw(c);
        }

        @Override
        public float getSwipeThreshold(RecyclerView.ViewHolder viewHolder) {
            return .9f;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
            Toast.makeText(getContext(), "Activity Deleted", Toast.LENGTH_SHORT).show();
            int position = viewHolder.getAdapterPosition();
            files.remove(position);
            sAdapter.notifyDataSetChanged();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public static Fragment newInstance(String s, String s1) {
        return new ActivitiesFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_activities, container, false);
        Context context = view.getContext();

        RecyclerView sRView = view.findViewById(R.id.rv_activities);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        sRView.setLayoutManager(layoutManager);
        files = GeoJsonHandler.getJsonFiles(context);
        sAdapter = new ActivitiesAdapter(files);
        sRView.setAdapter(sAdapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(sRView);

        return view;
    }

}