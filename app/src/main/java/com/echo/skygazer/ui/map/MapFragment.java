package com.echo.skygazer.ui.map;

import static com.mapbox.mapboxsdk.style.layers.Property.NONE;
import static com.mapbox.mapboxsdk.style.layers.Property.VISIBLE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.echo.skygazer.R;
import com.echo.skygazer.databinding.FragmentMapBinding;

import com.google.android.material.navigation.NavigationView;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.RasterLayer;
import com.mapbox.mapboxsdk.style.sources.RasterSource;
import com.mapbox.mapboxsdk.style.sources.TileSet;

import java.util.List;
import java.util.concurrent.TimeUnit;


public class MapFragment extends Fragment implements OnMapReadyCallback, PermissionsListener, NavigationView.OnNavigationItemSelectedListener {

    private FragmentMapBinding binding;
    /* Map Related Objects */
    private MapView mapView;
    private MapboxMap mapboxMap;
    private PermissionsManager permissionsManager;
    private View root;
    private NavigationView settingsMenu;
    private DrawerLayout drawerLayout;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Context context = getContext().getApplicationContext();
        Mapbox.getInstance(context, getString(R.string.mapbox_access_token));
        MapViewModel dashboardViewModel = new ViewModelProvider(this).get(MapViewModel.class);

        binding = FragmentMapBinding.inflate(inflater, container, false);
        root = binding.getRoot();

        settingsMenu = root.findViewById(R.id.mapDrawer);
        drawerLayout = root.findViewById(R.id.map_fragment);
        mapView = (MapView) root.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, R.string.open_map_settings, R.string.close_map_settings);
        drawerLayout.addDrawerListener(toggle);
        settingsMenu.setNavigationItemSelectedListener(this::onNavigationItemSelected);
        toggle.syncState();

        root.findViewById(R.id.mapDrawerOpen).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        SeekBar seekBar = root.findViewById(R.id.seekBar);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                long unixTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
                unixTime += i * (3 * 3600);
                Style style = mapboxMap.getStyle();
                if(style != null){
                    RasterSource rasterSource = style.getSourceAs("cloudCover");
                    if(rasterSource != null){
                        TileSet newTileSet = new TileSet("1.0.0", "https://maps.openweathermap.org/maps/2.0/weather/CL/{z}/{x}/{y}?date="+unixTime+"&appid=01f855a4eb997f16a95ed8e54118d97c");
                        RasterSource newRasterSource = new RasterSource("cloudCover", newTileSet);

                        style.removeLayer("cloudCover");

                        style.removeSource("cloudCover");
                        style.addSource(newRasterSource);


                        RasterLayer newRasterLayer = new RasterLayer("cloudCover", "cloudCover");
                        style.addLayer(newRasterLayer);
                    }
                }

                TextView text = root.findViewById(R.id.timeText);
                text.setText("+"+ (i * 3) +" Hours");
                float x = seekBar.getThumb().getBounds().left;
                float y = seekBar.getY();
                text.setX(x);
                text.setY(y + 225);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        return root;
    }
    @Override
    @SuppressWarnings( {"MissingPermission"})
    public void onStart(){
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop(){
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapView.onDestroy();
        binding = null;
    }

    @Override
    public void onExplanationNeeded(List<String> list) {
        Context context = getContext().getApplicationContext();
        Toast.makeText(context, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if(granted){
            mapboxMap.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style){
                    StyleLoaded(style);
                }
            });
        }
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        MapFragment.this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(new Style.Builder().fromUri("asset://style.json"), new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                StyleLoaded(style);
            }
        });
    }

    @SuppressWarnings( {"MissingPermission"} )
    private void enableLocationComponent(@NonNull Style loadedMapStyle){
        Context context = getContext().getApplicationContext();
        if(PermissionsManager.areLocationPermissionsGranted(context)) {
            LocationComponentOptions customLocationComponentOptions = LocationComponentOptions.builder(context)
                    .pulseEnabled(true)
                    .build();

            LocationComponent locationComponent = mapboxMap.getLocationComponent();

            locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions.builder(context, loadedMapStyle)
                            .locationComponentOptions(customLocationComponentOptions)
                            .build());

            locationComponent.setLocationComponentEnabled(true);

            locationComponent.setCameraMode(CameraMode.TRACKING);

            locationComponent.setRenderMode(RenderMode.NORMAL);
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(getActivity());
        }
    }

    public void StyleLoaded(@NonNull Style style) {
        enableLocationComponent(style);
        toggleLightPollutionLayer();
        toggleCloudCoverLayer();
    }

    private void toggleLightPollutionLayer(){
        mapboxMap.getStyle(new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                Layer layer = style.getLayer("vnl");
                if(layer != null){
                    if(VISIBLE.equals(layer.getVisibility().getValue())){
                        layer.setProperties(visibility(NONE));
                    } else {
                        layer.setProperties(visibility(VISIBLE));
                    }
                }
            }
        });
    }

    private void toggleCloudCoverLayer(){
        mapboxMap.getStyle(new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                Layer layer = style.getLayer("cloudCover");
                if(layer != null){
                    if(VISIBLE.equals(layer.getVisibility().getValue())){
                        layer.setProperties(visibility(NONE));
                    } else {
                        layer.setProperties(visibility(VISIBLE));
                    }
                }
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Log.i("Navigation Item Select", item.getItemId()+"");
        if(item.getItemId() == R.id.lightPollutionToggle){
            toggleLightPollutionLayer();
        }
        if(item.getItemId() == R.id.cloudCoverToggle){
            toggleCloudCoverLayer();
        }
        if(item.getItemId() == R.id.mapDrawerClose){
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        return true;
    }
}