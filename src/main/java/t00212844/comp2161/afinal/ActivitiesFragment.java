package t00212844.comp2161.afinal;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.ArrayList;


/**
 * Activities Fragment class for the saved runs
 */
public class ActivitiesFragment extends Fragment {

    //The number of items to load at once.
    private static final int NUMITEMS = 5;
    ArrayList<File> files;
    RecyclerView.Adapter sAdapter;
    public int count;

    public ActivitiesFragment() {
        // Required empty public constructor
    }

    final ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

        private Drawable icon;
        private ColorDrawable background;

        /**
         * Enables the swipe to delete feature
         *
         * @param recyclerView the recycler view
         * @param viewHolder   the current view holder
         * @param target       the view holder targer
         * @return boolean if being moved
         */
        @Override
        public boolean onMove(RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            icon = ContextCompat.getDrawable(recyclerView.getContext(), R.drawable.delete_sweep_24px);
            background = new ColorDrawable(getResources().getColor(R.color.colorPrimary, null));
            return false;
        }

        /**
         * Copied and Modified for use from
         * https://stackoverflow.com/questions/55672351/recyclerview-swipe-to-delete-still-shows-drawable-with-uncomplete-swipe
         * all rights to the original author
         *
         * @param c                 canvas
         * @param recyclerView      the recyclerview
         * @param viewHolder        view holder
         * @param dX                the x pos
         * @param dY                the y pos
         * @param actionState       the current state
         * @param isCurrentlyActive is the view current
         */
        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

            View itemView = viewHolder.itemView;
            icon = ContextCompat.getDrawable(recyclerView.getContext(), R.drawable.delete_sweep_24px);
            background = new ColorDrawable(getResources().getColor(R.color.lineColor, null));

            int iconMargin = icon.getIntrinsicHeight() * 2;
            int iconTop = itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight() * 2) / 2;
            int iconBottom = iconTop + icon.getIntrinsicHeight() * 2;

            if (dX < 0) { // Swiping to the left
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

        /**
         * Determines how far the item needs to be swiped before it's deleted
         *
         * @param viewHolder the current view holder
         * @return the floating point distance between 0-1
         */
        @Override
        public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
            return .9f;
        }

        /**
         * Item has been swiped so handle it appropriately
         *
         * @param viewHolder the current view holder
         * @param swipeDir   int for swipe direction
         */
        @SuppressWarnings("CollectionAddAllCanBeReplacedWithConstructor")
        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
            int position = viewHolder.getAdapterPosition();
            ArrayList<File> tempFiles = new ArrayList<>();

            tempFiles.addAll(files);
            files.remove(position);
            background.setColor(requireContext().getColor(android.R.color.transparent));
            //Show a snackback with a callback to undo the delete or complete the delete
            Snackbar snackbar = Snackbar.make(viewHolder.itemView, getString(R.string.rundel), Snackbar.LENGTH_LONG);
            View view = snackbar.getView();
            TextView tv = (TextView) view.findViewById(R.id.snackbar_text);
            tv.setTextColor(Color.WHITE);
            snackbar.setAction(getString(R.string.undo), v -> {
                files.clear();
                files.addAll(tempFiles);
                refreshFiles(snackbar.getContext());
            });
            snackbar.addCallback(new Snackbar.Callback() {

                /**
                 * If the timeout was reached, delete the JSON and PNG files
                 * @param snackbar the snackbar displayed
                 * @param event the event that dismissed the snackbar
                 */
                @Override
                public void onDismissed(Snackbar snackbar, int event) {
                    if (event == DISMISS_EVENT_TIMEOUT) {
                        File file = tempFiles.get(position);
                        File png = new File(requireContext().getFilesDir(), file.getName().substring(0, file.getName().length() - 5) + ".png");
                        file.delete();
                        png.delete();
                    }
                    refreshFiles(snackbar.getContext());
                }
            });
            snackbar.show();
        }
    };

    /**
     * New instance of the fragment
     *
     * @return the new fragment
     */
    public static Fragment newInstance() {
        return new ActivitiesFragment();
    }

    /**
     * Create the fragment
     *
     * @param savedInstanceState bundle of saved info
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Creates the view and populates it
     *
     * @param inflater           layoutinflator
     * @param container          view group container
     * @param savedInstanceState bundle of saved state variables
     * @return the created view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_activities, container, false);
        Context context = view.getContext();

        RecyclerView sRView = view.findViewById(R.id.rv_activities);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        sRView.setLayoutManager(layoutManager);

        count = NUMITEMS;
        files = GeoJsonHandler.getJsonFiles(context);
        if (files.size() > count) {
            files = new ArrayList<>(files.subList(0, count));
        }
        sAdapter = new ActivitiesAdapter(files);
        sRView.setAdapter(sAdapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(sRView);

        sRView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            boolean bottom = false;

            /**
             * If the scroll hits the bottom, add more items
             *
             * @param recyclerView the recycler view
             * @param newState     not used
             */
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (!recyclerView.canScrollVertically(1) && !bottom) {
                    super.onScrollStateChanged(recyclerView, newState);
                    bottom = true;
                    count += NUMITEMS;
                    files.clear();
                    refreshFiles(context);
                    bottom = false;
                }
            }
        });

        //Pull down to refresh using swipe to refresh
        final SwipeRefreshLayout refresh = view.findViewById(R.id.pullRefresh);
        refresh.setOnRefreshListener(() -> {
            refreshFiles(context);
            refresh.setRefreshing(false);
        });

        return view;
    }

    /**
     * Refresh the list of JSON run files
     *
     * @param context the application context
     */
    private void refreshFiles(Context context) {
        files.clear();
        ArrayList<File> newList = GeoJsonHandler.getJsonFiles(context);
        files.addAll(newList);
        if (newList.size() > count) {
            for (int i = 0; i <= count; i++) {
                files.add(newList.get(i));
            }
        }
        sAdapter.notifyDataSetChanged();
    }
}