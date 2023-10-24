package com.echo.skygazer.ui.map;

import static androidx.fragment.app.FragmentManager.TAG;

import static com.mapbox.mapboxsdk.style.layers.Property.NONE;
import static com.mapbox.mapboxsdk.style.layers.Property.VISIBLE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.echo.skygazer.R;
import com.echo.skygazer.databinding.FragmentMapBinding;

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

import java.util.List;


public class MapFragment extends Fragment implements OnMapReadyCallback, PermissionsListener {

    private FragmentMapBinding binding;
    private MapView mapView;
    private MapboxMap mapboxMap;
    private PermissionsManager permissionsManager;
    private View root;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Context context = getContext().getApplicationContext();
        Mapbox.getInstance(context, getString(R.string.mapbox_access_token));

        MapViewModel dashboardViewModel = new ViewModelProvider(this).get(MapViewModel.class);

        binding = FragmentMapBinding.inflate(inflater, container, false);
        root = binding.getRoot();


        mapView = (MapView) root.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

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
        mapboxMap.setStyle(new Style.Builder().fromUri("mapbox://styles/cpitre1/clo3tz2g000fd01oz015efa1i/draft"), new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                root.findViewById(R.id.lightPollutionToggle).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        toggleLightPollutionLayer();
                    }
                });
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
}