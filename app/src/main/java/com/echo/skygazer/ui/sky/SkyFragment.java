package com.echo.skygazer.ui.sky;

import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.echo.skygazer.Main;
import com.echo.skygazer.R;
import com.echo.skygazer.constellationList.ConstellationAdapter;
import com.echo.skygazer.databinding.FragmentSkyBinding;
import com.echo.skygazer.gfx.SkyView;
import com.echo.skygazer.io.Constellations;
import com.echo.skygazer.gfx.SkySimulation;
import com.echo.skygazer.io.HygDatabase;
import com.echo.skygazer.io.SpecificConstellation;
import com.google.android.material.sidesheet.SideSheetDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SkyFragment extends Fragment {

    private FragmentSkyBinding binding;
    private float lastDragX = 0;
    private float lastDragY = 0;
    private boolean dragging = false;
    private static SkyView skyView = null;
    private ImageButton constellationVisibilityButton;
    private SideSheetDialog sideSheetDialog;

    private static SkySimulation skySim = null;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sky, container, false);
        LinearLayout rootLayout = rootView.findViewById(R.id.sky_view);

        // Set up FAB for constellation visibility
        constellationVisibilityButton = (ImageButton) rootView.findViewById(R.id.constellation_visible_button);

        // Initialize side sheet that displays the currently visible and not visible constellations
        sideSheetDialog = new SideSheetDialog(requireContext());
        sideSheetDialog.setContentView(R.layout.constellation_side_view);

        //Build SkySim, add to root layout, start draw thread.
        skySim = new SkySimulation(getActivity());
        rootLayout.addView(skySim);
        skySim.startDrawThread();

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
            ConstellationAdapter constellationAdapter = new ConstellationAdapter(getActivity().getApplicationContext(), visStrList);
            visible.setAdapter(constellationAdapter);

            // Not Visible Section
            RecyclerView notVisible = sideSheetDialog.findViewById(R.id.not_visible_constellation_list);
            notVisible.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
            notVisible.setAdapter(new ConstellationAdapter(getActivity().getApplicationContext(), notStrList));

            // After gathering Constellations and populating RecyclerViews display the SideSheet
            sideSheetDialog.show();
        });

        return rootView;
    }

    public static SkySimulation getSkySim() { return skySim; }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}