package com.echo.skygazer;

import android.Manifest;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;

import com.echo.skygazer.databinding.ActivityMainBinding;
import com.echo.skygazer.io.HygDatabase;
import com.echo.skygazer.ui.sky.SkyFragment;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener{

    //Progress Dialog
    public static final int PROGRESS_BAR_TYPE = 0;

    private ActivityMainBinding binding;
    private static double locLatitude = -9999;
    private static double locLongitude = -9999;
    private static double locAzimuth = -9999;
    private static double locPitch = -9999;
    private static double locRoll = -9999;

    private static SensorManager snsrManager;

    public enum NavSectionID { MAP, SKY, SETTINGS, UNKNOWN }
    private NavSectionID navSectionId = NavSectionID.UNKNOWN;

    /**
     * Get current navigation section (SKY, MAP, SETTINGS, UNKNOWN). Use NavSetionID.[id] to reference these.
     * @return
     */
    public NavSectionID getCurrentNavSection() {
        return navSectionId;
    }

    /**
     * Show or hide the upper title bar ("support action bar"). This can be distracting, so it should be hidden when in the sky view.
     * @param shown If true, the upper title bar will become visible upon method call. If false, it will disappear upon method call.
     */
    private void setSupportActionBarState(boolean shown) {
        try {
            if(shown) {
                getSupportActionBar().show();
            } else {
                getSupportActionBar().hide();
            }
        } catch (NullPointerException ignored) {}
    }

    /**
     * Method called automatically near launch of the application. At the end of this function, Main.main() is called.
     * @param savedInstanceState "IDK, but method gets called automatically so it doesn't really matter"
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Get binding, add to 'rootView'
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View rootView = binding.getRoot();
        //Set content view
        setContentView(rootView);

        // Create Shared Preference Manager to listen for theme change
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);

        // I don't know if the if itself is needed, but to be safe it's there to not overwrite the saved value.
        if(!getSettingValue("focused_view")){
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(getString(R.string.focused_view_id), false);
            editor.apply();
        }

//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putBoolean(getString(R.string.focused_view_id), true);
//        editor.apply();

        // Create functional bottom navbar with 'map_view', 'sky_view', 'settings'
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
            R.id.map_view, R.id.sky_view, R.id.settings
        ).build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);


        /** Listener for app location change (Map, Sky, Settings) */
        navController.addOnDestinationChangedListener(
            (thisNavCtrl, navDst, bundle) -> {
                navSectionId = NavSectionID.UNKNOWN;

                assert navDst.getLabel() != null;
                String dstName = navDst.getLabel().toString();
                switch(dstName) {
                    case "Map Overlays": {
                        navSectionId = NavSectionID.MAP;
                        setSupportActionBarState(true);
                        HygDatabase.reinitVisuals();
                        break;
                    }
                    case "Sky View": {
                        navSectionId = NavSectionID.SKY;
                        //Hide upper title bar when displaying the sky
                        setSupportActionBarState(false);

                        break;
                    }
                    case "Settings": {
                        navSectionId = NavSectionID.SETTINGS;
                        setSupportActionBarState(true);
                        HygDatabase.reinitVisuals();
                        break;
                    }
                }

                Main.log("Displaying fragment '"+getCurrentNavSection().toString()+"'");
            }
        );

        /** Listener for real life GPS location */
        LocationListener locationListenerGPS = new LocationListener() {
            @Override
            public void onLocationChanged(android.location.Location location) {
                locLatitude = location.getLatitude();
                locLongitude = location.getLongitude();
            }

            @Override
            public void onProviderEnabled(String provider) {
                Toast.makeText(getBaseContext(), "GPS successfully turned on.", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onProviderDisabled(String provider) {
                Toast.makeText(getBaseContext(), "Warning: GPS was turned off.", Toast.LENGTH_LONG).show();
            }
        };
        //Get permissions and build location manager.
        if (
            ActivityCompat.checkSelfPermission(getBaseContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(getBaseContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 123456);
        } else {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, locationListenerGPS);
        }

        /** Get sensors for phone orientation */
        snsrManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        SensorEventListener sensorEventListener = new SensorEventListener() {
            float[] mAccelerometer;
            float[] mGeomagnetic;

            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    mAccelerometer = event.values;
                    locPitch = event.values[2]*180d/Math.PI/3.2;
                }
                if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                    mGeomagnetic = event.values;
                }

                if ( mAccelerometer!=null && mGeomagnetic!=null) {
                    float[] R = new float[9];
                    float[] I = new float[9];
                    boolean success = SensorManager.getRotationMatrix(R, I, mAccelerometer, mGeomagnetic);

                    if (success) {
                        float[] orientationData = new float[3];
                        SensorManager.getOrientation(R, orientationData);
                        locAzimuth = orientationData[0]*180d/Math.PI;
                        locRoll = orientationData[2]*180d/Math.PI;
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                // Handle accuracy changes if needed
            }
        };

        // Register the listeners
        snsrManager.registerListener(sensorEventListener, snsrManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),  SensorManager.SENSOR_DELAY_NORMAL);
        snsrManager.registerListener(sensorEventListener, snsrManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL);
        snsrManager.registerListener(sensorEventListener, snsrManager.getDefaultSensor(Sensor.TYPE_GRAVITY), SensorManager.SENSOR_DELAY_NORMAL);
        //Main class
        Main.main(this);
        notification();
    }

    public static double getLocLatitude() { return locLatitude; }
    public static double getLocLongitude() { return locLongitude; }
    public static boolean isLocationInvalid() { return locLatitude==-9999d || locLongitude==-9999d; }
    public static double getLocAzimuth() { return locAzimuth; }
    public static double getLocPitch() { return locPitch; }
    public static double getLocRoll() { return locRoll; }

    public static boolean isOrientationInvalid() { return locAzimuth==-9999d || locPitch==-9999d || locRoll==-9999d; }

    /**
     * Method called when the user presses the back button.
     * For now, it just prompts the user whether or not they want to exit
     */
    @Override
    public void onBackPressed() {
        //Create alert dialog: Ask yes/no whether the user wants to exit the application.
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)                //Window icon
                .setTitle("Closing Application")                            //Title
                .setMessage("Are you sure you want to exit SkyGazer?")      //Message
                .setPositiveButton("Yes", (dialogInterface, i) -> {    //Yes button
                    //Finish application and exit program
                    finishAffinity();
                    System.exit(0);
                })
                .setNegativeButton("No", null)                  //No button
                .show();                                                    //Show alert window
    }

    @Override
    public Resources.Theme getTheme() {
        Resources.Theme theme = super.getTheme();


        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean lowLightModeValue = pref.getBoolean("low_light_mode", false);

        if(lowLightModeValue){
            theme.applyStyle(R.style.Theme_LowLight, true);
            HygDatabase.reinitVisuals();
        } else {
            theme.applyStyle(R.style.Theme_Default, true);
            HygDatabase.reinitVisuals();
        }

        return theme;
    }

    public boolean getSettingValue(String setting) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean val = pref.getBoolean(setting, false);
        return val;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if(s.equals("low_light_mode")){
            recreate();
        } else if(s.equals("celestial_notifications")){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
                }
            }
        }
    }

    public void notification(){
        if(!getSettingValue("celestial_notifications")){
            return;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        Intent intent = new Intent(this, Notifications.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, intent, PendingIntent.FLAG_IMMUTABLE);


        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
    }
}



/*  Example code for retrieving a preference variable
    if( Main.getMainActivity().getSettingValue("constellation_highlighting") ) {
        //do stuff
    }
 */