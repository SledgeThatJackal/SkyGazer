package com.echo.skygazer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.echo.skygazer.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    /**
     * Show or hide the upper title bar ("support action bar"). This can be distracting, so it should be hidden when in the sky view.
     * @param shown
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
     * @param savedInstanceState
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
                assert navDst.getLabel() != null;
                String dstName = navDst.getLabel().toString();
                switch(dstName) {
                    case "Map": {
                        Main.log("Displaying the Map fragment.");
                        setSupportActionBarState(true);
                        break;
                    }
                    case "Sky": {
                        Main.log("Displaying the Sky fragment.");
                        //Hide upper title bar when displaying the sky
                        setSupportActionBarState(false);
                        break;
                    }
                    case "Settings": {
                        Main.log("Displaying the Settings fragment.");
                        setSupportActionBarState(true);
                        break;
                    }
                }
            }
        );

        //Main method
        Main.main(this);
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