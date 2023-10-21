package com.echo.skygazer;

import static com.echo.skygazer.MainActivity.PROGRESS_BAR_TYPE;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.echo.skygazer.io.Downloader;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;

public class Main {

    public static Random random = new Random(123456);
    private static MainActivity mActivity;
    private static String rootPath = null;

    public static String getRootPath() { return rootPath; }

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
        Main.mActivity = mActivity;

        //Automatically set to "/data/user/0/com.echo.skygazer/files"
        rootPath = mActivity.getFilesDir().getAbsolutePath();

        log("Starting SkyGazer...");
        log("Root path is '"+rootPath+"'.");

        //To see the downloaded file(s), go to Toolbar > View > Tool Windows > Device Explorer. Then navigate to "/data/user/0/com.echo.skygazer/files/download.txt".
        Downloader dl = new Downloader("https://raw.githubusercontent.com/astronexus/HYG-Database/master/README.md");
        dl.saveAs("download.txt");
    }
}