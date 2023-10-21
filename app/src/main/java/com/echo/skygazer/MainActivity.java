package com.echo.skygazer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.echo.skygazer.databinding.ActivityMainBinding;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {

    //Progress Dialog
    private ProgressDialog progressDialog;
    public static final int PROGRESS_BAR_TYPE = 0;

    private ActivityMainBinding binding;
    public enum NavSectionID { MAP, SKY, SETTINGS, UNKNOWN }
    private NavSectionID navSectionId = NavSectionID.UNKNOWN;

    /**
     * Get current navigation section (SKY, MAP, SETTINGS, UNKNOWN). Use NavSetionID.[id] to reference these.
     * @return
     */
    public NavSectionID getCurrentNavSection() {
        return navSectionId;
    }
    public ProgressDialog getProgressDialog() { return progressDialog; }

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

        // Create functional bottom navbar with 'map_view', 'sky_view', 'settings'
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
            R.id.map_view, R.id.sky_view, R.id.settings
        ).build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        //Listener for app location change (Map, Sky, Settings)
        navController.addOnDestinationChangedListener(
            (thisNavCtrl, navDst, bundle) -> {
                navSectionId = NavSectionID.UNKNOWN;

                assert navDst.getLabel() != null;
                String dstName = navDst.getLabel().toString();
                switch(dstName) {
                    case "Map": {
                        navSectionId = NavSectionID.MAP;
                        setSupportActionBarState(true);
                        break;
                    }
                    case "Sky": {
                        navSectionId = NavSectionID.SKY;
                        //Hide upper title bar when displaying the sky
                        setSupportActionBarState(false);
                        break;
                    }
                    case "Settings": {
                        navSectionId = NavSectionID.SETTINGS;
                        setSupportActionBarState(true);
                        break;
                    }
                }

                Main.log("Displaying fragment '"+getCurrentNavSection().toString()+"'");
            }
        );

        //Main method
        Main.main(this);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        ProgressDialog pd = progressDialog;
        switch (id) {
            case PROGRESS_BAR_TYPE:
                pd = new ProgressDialog(this);
                pd.setMessage("Downloading file. Please wait...");
                pd.setIndeterminate(false);
                pd.setMax(100);
                pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                pd.setCancelable(true);
                pd.show();
                return pd;
            default:
                return null;
        }
    }

    /**
     * Method called when the user presses the back button
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
}