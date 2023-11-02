package com.echo.skygazer.ui.sky;

import android.os.Bundle;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.echo.skygazer.Main;
import com.echo.skygazer.R;
import com.echo.skygazer.constellationList.ConstellationAdapter;
import com.echo.skygazer.databinding.FragmentSkyBinding;
import com.echo.skygazer.gfx.SkyView;
import com.echo.skygazer.io.HygDatabase;
import com.google.android.material.sidesheet.SideSheetDialog;

public class SkyFragment extends Fragment {

    private FragmentSkyBinding binding;
    private float lastDragX = 0;
    private float lastDragY = 0;
    private boolean dragging = false;
    private static SkyView skyView = null;
    private ImageButton constellationVisibilityButton;
    private SideSheetDialog sideSheetDialog;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sky, container, false);
        LinearLayout rootLayout = rootView.findViewById(R.id.sky_view);

        // Set up FAB for constellation visibility
        constellationVisibilityButton = (ImageButton) rootView.findViewById(R.id.constellation_visible_button);

        // Initialize side sheet that displays the currently visible constellations
        SideSheetDialog sideSheetDialog = new SideSheetDialog(requireContext());
        sideSheetDialog.setContentView(R.layout.constellation_side_view);


        //Build SkyDrawing, add to root layout, start draw thread.
        skyView = new SkyView(getActivity());
        rootLayout.addView(skyView);
        skyView.startDrawThread();

        if( HygDatabase.isInitialized() ) {
            HygDatabase.setStarsRandomly( SkyFragment.getSkyView(), 8 );
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
                        skyView.doTapAt(x, y);
                    } break;
                    case MotionEvent.ACTION_UP: {
                        lastDragX = 0;
                        lastDragY = 0;
                        dragging = false;
                    } break;
                    case MotionEvent.ACTION_MOVE: {
                        if(dragging) {
                            skyView.doDragAt(x-lastDragX, y-lastDragY);
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
            rootLayout.addView(inflater.inflate(R.layout.constellation_side_view, null));

            // Place Holder Values
            String[] visStrArr = {"Constellation A", "Constellation B", "Constellation C"};
            String[] notStrArr = {"Constellation D", "Constellation E", "Constellation F"};

            // Visible Section
            RecyclerView visible = rootLayout.findViewById(R.id.visible_constellation_list);
            visible.setAdapter(new ConstellationAdapter(this.getContext(), visStrArr));

            // Not Visible Section
            RecyclerView notVisible = rootLayout.findViewById(R.id.not_visible_constellation_list);
            notVisible.setAdapter(new ConstellationAdapter(this.getContext(), notStrArr));

            sideSheetDialog.show();
        });

        return rootView;
    }

    public static SkyView getSkyView() { return skyView; }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}