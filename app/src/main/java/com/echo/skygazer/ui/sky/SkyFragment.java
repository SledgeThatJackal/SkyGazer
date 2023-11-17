package com.echo.skygazer.ui.sky;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import android.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.echo.skygazer.Main;
import com.echo.skygazer.R;
import com.echo.skygazer.constellationList.ConstellationAdapter;
import com.echo.skygazer.databinding.FragmentSkyBinding;
import com.echo.skygazer.io.Constellations;
import com.echo.skygazer.gfx.SkySimulation;
import com.echo.skygazer.io.HygDataRow;
import com.echo.skygazer.io.HygDatabase;
import com.echo.skygazer.io.SpecificConstellation;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.sidesheet.SideSheetDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.util.List;

public class SkyFragment extends Fragment {

    private FragmentSkyBinding binding;
    private float lastDragX = 0;
    private float lastDragY = 0;
    private boolean dragging = false;
    private ImageButton constellationVisibilityButton;
    private SideSheetDialog sideSheetDialog;
    private BottomSheetDialog bottomSheetDialog;
    private static SkySimulation skySim = null;
    private static SearchView searchView = null;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sky, container, false);
        LinearLayout rootLayout = rootView.findViewById(R.id.sky_view);

        // Set up FAB for constellation visibility
        constellationVisibilityButton = (ImageButton) rootView.findViewById(R.id.constellation_visible_button);

        // Initialize side sheet that displays the currently visible and not visible constellations
        sideSheetDialog = new SideSheetDialog(requireContext());
        sideSheetDialog.setContentView(R.layout.constellation_side_view);

        // Bottom Sheet
        bottomSheetDialog = new BottomSheetDialog(requireContext());
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog_layout);

        //Build SkySim, add to root layout, start draw thread.
        skySim = new SkySimulation(getActivity(), bottomSheetDialog);
        rootLayout.addView(skySim);
        skySim.startDrawThread();
        //find the searchView from the layout
        searchView = rootView.findViewById(R.id.search_view);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        if( HygDatabase.isInitialized() ) {
            HygDatabase.setVisibleStars( getSkySim() );
        }

        //Touch detection listener
        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                int action = motionEvent.getAction();

                float x = motionEvent.getX();
                float y = motionEvent.getY();

                switch (action & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN: {
                        Main.log( "Touched screen @ ("+x+", "+y+")" );
                        rootView.performClick();
                        skySim.doTapAt(x, y);
                    } break;
                    case MotionEvent.ACTION_UP: {
                        lastDragX = 0;
                        lastDragY = 0;
                        dragging = false;
                    } break;
                    case MotionEvent.ACTION_MOVE: {
                        if(dragging) {
                            skySim.doDragAt(x-lastDragX, y-lastDragY);
                        }
                        lastDragX = x;
                        lastDragY = y;
                        dragging = true;
                    } break;
                }
                return true;
            }
        });

        // Create side sheet when the eye button is touched
        constellationVisibilityButton.setOnClickListener(view -> {
            Constellations constellations = new Constellations();

            // Place Holder Values
            int i = 0;
            List<String> visStrList = new ArrayList<>();
            List<String> notStrList = new ArrayList<>();
            for(Map.Entry<Integer, SpecificConstellation> currEntry: constellations.getConstellations().entrySet()){
                if(currEntry.getKey() % 2 == 0){
                    visStrList.add(currEntry.getValue().getConstellationName());
                } else {
                    notStrList.add(currEntry.getValue().getConstellationName());
                }
            }

            // Visible Section
            RecyclerView visible = sideSheetDialog.findViewById(R.id.visible_constellation_list);
            visible.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
            visible.setAdapter(new ConstellationAdapter(getActivity().getApplicationContext(), visStrList));

            // Not Visible Section
            RecyclerView notVisible = sideSheetDialog.findViewById(R.id.not_visible_constellation_list);
            notVisible.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
            notVisible.setAdapter(new ConstellationAdapter(getActivity().getApplicationContext(), notStrList));

            // After gathering Constellations and populating RecyclerViews display the SideSheet
            sideSheetDialog.show();
        });

        bottomSheetDialog.findViewById(R.id.bottomSheetExpand).setOnClickListener(view -> {
            bottomSheetDialog.findViewById(R.id.bottomSheetWikiText).setVisibility(View.VISIBLE);
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
            if(pref.getBoolean("advanced_info", false)){
                bottomSheetDialog.findViewById(R.id.bottomSheetDatabaseId).setVisibility(View.VISIBLE);
            }

            bottomSheetDialog.findViewById(R.id.bottomSheetExpand).setVisibility(View.GONE);
            bottomSheetDialog.findViewById(R.id.bottomSheetCollapse).setVisibility(View.VISIBLE);
        });

        bottomSheetDialog.findViewById(R.id.bottomSheetCollapse).setOnClickListener(view -> {
            bottomSheetDialog.findViewById(R.id.bottomSheetWikiText).setVisibility(View.GONE);
            bottomSheetDialog.findViewById(R.id.bottomSheetDatabaseId).setVisibility(View.GONE);
            bottomSheetDialog.findViewById(R.id.bottomSheetExpand).setVisibility(View.VISIBLE);
            bottomSheetDialog.findViewById(R.id.bottomSheetCollapse).setVisibility(View.GONE);
        });

        return rootView;
    }

    public static SkySimulation getSkySim() { return skySim; }

    //perfoming the search query
    public void performSearch(String query){
        List<Integer> searchResults = HygDatabase.searchStars(query);

        //results
        if(!searchResults.isEmpty()){
            //go through the searchResults
            for(Integer starID: searchResults){
              //do method stuff
            }
        } else{
            Main.log("No stars were found");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}