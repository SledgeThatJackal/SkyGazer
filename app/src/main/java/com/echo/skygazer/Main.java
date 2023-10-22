package com.echo.skygazer;

import android.util.Log;

import com.echo.skygazer.io.WebResource;

import java.io.File;
import java.util.Random;

public class Main {

    /* Utility objects */
    public static Random random = new Random(123456);
    /* Private class members */
    //MainActivity object
    private static MainActivity mActivity;
    //String holding root filepath belonging to this application (should be "/data/user/0/com.echo.skygazer/cache").
    private static String rootCachePath = null;

    /**
     * Make a new directory in the root cache path.
     * @param dirName Name of the new directory
     */
    private static void mkdir(String dirName) {
        File myDir = new File(getRootCachePath(), dirName);
        if(!myDir.mkdir()) {
            if(!myDir.exists()) {
                log("ERROR - failed to create directory '"+myDir.getName()+"'");
            }
        }
    }

    public static String getRootCachePath() { return rootCachePath; }
    public static MainActivity getMainActivity() { return mActivity; }

    /**
     * Log string to logcat (System.out.println() doesn't work)
     * <p>
     * To find log messages:
     * Open Logcat: Tab 4 at the bottom of the window (Git, Run, Profiler, ->Logcat, ...).
     * Then, in the filter text box ("Press Ctrl+Space to see suggestions"), type: "tag: echo-out"
     */
    public static void log(String s) {
        Log.d("echo-out", s);
    }
    public static void log(Object o) { log(o.toString()); }


    /**
     * Main function. Called at the end of MainActivity.onCreate().
     */
    public static void main(MainActivity mActivity) {
        //Set MainActivity object and set rootCachePath to "/data/user/0/com.echo.skygazer/cache"
        Main.mActivity = mActivity;
        rootCachePath = mActivity.getCacheDir().getAbsolutePath();

        //Starting log messages
        log("Starting SkyGazer...");
        log("Root cache path is '"+rootCachePath+"'.");

        //Create needed cache folders
        mkdir("wiki");  //Cached wikipedia pages (entire "Sirius" article: ~400KB)
        mkdir("db");    //Cached databases (For now, one file 'hyg.csv', ~30.8 MB)

        //To see the downloaded file(s), go to Toolbar > View > Tool Windows > Device Explorer. Then navigate to "/data/user/0/com.echo.skygazer/cache".
        //Download example is below. If you want to use getLine() or getWikipediaInfo(), that must be done in WebResource.onProcessingFinished().
        WebResource wr = new WebResource("https://en.wikipedia.org/wiki/Sirius", "wiki/Sirius.html", 1234);
        wr.cache();

        WebResource hyg = new WebResource("https://raw.githubusercontent.com/astronexus/HYG-Database/master/hyg/v3/hyg_v37.csv", "db/hyg.csv");
        hyg.cache();
    }
}