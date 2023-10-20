package com.echo.skygazer;

import android.util.Log;

public class Main {

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
        log("Starting SkyGazer...");

        //String dbURL = "https://raw.githubusercontent.com/astronexus/HYG-Database/master/hyg/v3/hyg_v37.csv";
        //dbURLTest = "https://raw.githubusercontent.com/astronexus/HYG-Database/master/hyg/README.md";
    }
}
