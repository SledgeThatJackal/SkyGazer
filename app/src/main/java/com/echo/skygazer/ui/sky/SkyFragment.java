package com.echo.skygazer.ui.sky;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.Display;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.echo.skygazer.Main;
import com.echo.skygazer.MainActivity;
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
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.view.Window;

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
    private static ListView listView = null;
    private List<String>originalStarNames = new ArrayList<>();
    private List<String>filteredStarNames = new ArrayList<>();
    private ArrayAdapter<String>arrayAdapter;

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

        storeOriginalList();

        //find the searchView from the layout
        searchView = rootView.findViewById(R.id.search_view);

        //find the listView from the layout
        listView = rootView.findViewById(R.id.list_view);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(requireContext());
        boolean lowLightModeValue = pref.getBoolean("low_light_mode", false);

        int searchTextViewId = searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
        TextView textView = (TextView) searchView.findViewById(searchTextViewId);

        if (lowLightModeValue) {
            // set the text color
            textView.setTextColor(getResources().getColor(R.color.light_red));
            listView.setBackgroundColor(getResources().getColor(R.color.black));
        } else {
            textView.setTextColor(Color.WHITE);
            listView.setBackgroundColor(getResources().getColor(R.color.night_blue));
        }


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                //hide the keyboard
                InputMethodManager inputMethodManager = (InputMethodManager) requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                if (inputMethodManager != null) {
                    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }

                //show clicked message
                Main.log("Position is: " + position);
                Main.log("Clicked on " + filteredStarNames.get(position));
                skySim.performSearch(filteredStarNames.get(position));
                listView.setVisibility(View.GONE);
                }
        });

        //use an array adapter to find the UI from the listView
        arrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1);
        listView.setAdapter(arrayAdapter);






        //when a user hits enter then it will load up the search feature
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //Main.log(s);
                if(query != null){
                    skySim.performSearch(query);
                    searchView.clearFocus();
                    filterStars(query);
                }


                //make the listView invisible again
                listView.setVisibility(View.GONE);
                return true;
            }
            //if it's empty then show or not show the listView
            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText.isEmpty()){
                    listView.setVisibility(View.GONE);

                } else{
                    filterStars(newText);
                    listView.setVisibility(View.VISIBLE);
                }
                return false;
            }
        });

        if( HygDatabase.isInitialized() ) {
            HygDatabase.setVisibleStars( getSkySim() );
        }

        rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect r = new Rect();
            rootView.getWindowVisibleDisplayFrame(r);
            int screenHeight = rootView.getRootView().getHeight();

            // Calculate the height difference between the rootView height and visible display frame height
            int keypadHeight = screenHeight - r.bottom;

            // If the height difference is greater than a certain threshold (considering keyboard might not take full screen height)
            // Hide the navigation bar, otherwise show it
            if (keypadHeight > screenHeight * 0.15) {
                rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            } else {
                rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            }
        });

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

                        if(searchView != null && searchView.hasFocus()){
                            searchView.clearFocus();
                        }

                        InputMethodManager inputMethodManager = (InputMethodManager) requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                        if (inputMethodManager != null) {
                            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        }
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

    //store the original list
    private void storeOriginalList(){
        if(originalStarNames.isEmpty()){
            if(originalStarNames.isEmpty()){
                Map<String, Integer> hygDict = HygDatabase.getHygDictionary();
                if(hygDict != null) {
                    originalStarNames.addAll(hygDict.keySet());
                }
            }
        }
    }

    //resetFilter when search query is done
    private void resetFilter(){
        if(originalStarNames.isEmpty()){
            storeOriginalList();
        }
        filteredStarNames.clear();
        filteredStarNames.addAll(originalStarNames);
        updateAdapter();
    }


    //go through and create a filtered list for the user
    private void filterStars(String query){
        if (originalStarNames.isEmpty()) {
            storeOriginalList();
        }

        if(query.isEmpty()){
            resetFilter();
        } else {
            filteredStarNames.clear();
            for(String starName: originalStarNames){
                if(starName != null && starName.toLowerCase().contains(query.toLowerCase())){
                    filteredStarNames.add(starName);
                }
            }
            updateAdapter();

            // Display a message using Snackbar when no results are found
            if (filteredStarNames.isEmpty()) {
                View rootView = requireView(); // Get the root view of the fragment

                Snackbar snackbar = Snackbar.make(rootView, "No results found", Snackbar.LENGTH_SHORT);
                snackbar.show();
            }

        }
    }
    //updateAdapter that updates the information into the SkyGazer app
    private void updateAdapter(){

        if(arrayAdapter != null){
            arrayAdapter.clear();
            if(filteredStarNames.isEmpty() && searchView.getQuery().toString().isEmpty()){
                arrayAdapter.addAll(originalStarNames);
            }
            else{
                arrayAdapter.addAll(filteredStarNames);
            }
            arrayAdapter.notifyDataSetChanged();
        }
    }

    //Adding vibration
    public void initializeVibrationFeature(){
        Vibrator vibrator = (Vibrator) requireContext().getSystemService(Context.ACCOUNT_SERVICE);

        if (vibrator != null && vibrator.hasVibrator()){
            vibrator.vibrate(500);
        }
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}