package com.echo.skygazer.ui.sky;

import android.os.Bundle;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import android.widget.SearchView;
import androidx.fragment.app.Fragment;

import com.echo.skygazer.Main;
import com.echo.skygazer.R;
import com.echo.skygazer.databinding.FragmentSkyBinding;
import com.echo.skygazer.gfx.SkyView;
import com.echo.skygazer.io.HygDataRow;
import com.echo.skygazer.io.HygDatabase;

import java.util.List;

public class SkyFragment extends Fragment {

    private FragmentSkyBinding binding;
    private float lastDragX = 0;
    private float lastDragY = 0;
    private boolean dragging = false;
    private static SkyView skyView = null;
    private static SearchView searchView = null;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sky, container, false);
        LinearLayout rootLayout = rootView.findViewById(R.id.sky_view);

        //find the searchView from the layout
        searchView = rootView.findViewById(R.id.search_view);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //Main.log(s);
                skyView.performSearch(query);
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });



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

        return rootView;
    }

    public static SkyView getSkyView() { return skyView; }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}