package com.echo.skygazer.ui.sky;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.echo.skygazer.Main;
import com.echo.skygazer.R;
import com.echo.skygazer.databinding.FragmentSkyBinding;
import com.echo.skygazer.gfx.SkySimulation;
import com.echo.skygazer.io.HygDatabase;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class SkyFragment extends Fragment {

    private FragmentSkyBinding binding;
    private float lastDragX = 0;
    private float lastDragY = 0;
    private boolean dragging = false;

    private static SkySimulation skySim = null;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sky, container, false);
        LinearLayout rootLayout = rootView.findViewById(R.id.sky_view);

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
                    /* User touches screen */
                    case MotionEvent.ACTION_DOWN: {
                        Main.log( "Touched screen @ ("+x+", "+y+")" );
                        rootView.performClick();
                        skySim.doTapAt(x, y);
                    } break;
                    /* User releases screen */
                    case MotionEvent.ACTION_UP: {
                        lastDragX = 0;
                        lastDragY = 0;
                        dragging = false;
                    } break;
                    /* User drags screen */
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

        return rootView;
    }

    public static SkySimulation getSkySim() { return skySim; }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}