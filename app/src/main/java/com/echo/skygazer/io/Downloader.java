package com.echo.skygazer.io;

import android.annotation.SuppressLint;
import android.os.AsyncTask;

import com.echo.skygazer.Main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Downloader
{
    private String src;
    private String dst = null;
    /**
     * Gets a website URL "[src]" that the device can download from.
     * To save that data to disk, use saveAs(dst).
     * To get that data as a String, use getLine(). Make sure to read getLine()'s documentation before using it.
     *
     * @param url - Source: URL of whatever you are trying to download (ex: "https://raw.githubusercontent.com/astronexus/HYG-Database/master/README.md")
     */
    public Downloader(String url) {
        this.src = url;
    }

    /**
     * NOT IMPLEMENTED yet.
     * Basically, this is what we will use if the user has "caching" set to OFF in the settings.
     * @return Goes to the line number 'lineNum' in the web data and returns that line as a string.
     */
    public String getLine(int lineNum) {
        return "unimplemented";
    }

    /**
     * This will create a new file at "/data/user/0/com.echo.skygazer/files/[dst], based on the URL set during the constructor."
     * @param dst - Destination: Filename/path of where the downloaded data will be (ex: "download.txt").
     */
    public void saveAs(String dst) {
        this.dst = dst;
        if( dst!=null ) {
            new DownloadFileTask().execute(src);
        } else {
            Main.log("Downloader - dst is null...");
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class DownloadFileTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... urls) {
            String url = urls[0];
            try {
                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                con.setRequestMethod("GET");
                InputStream is = con.getInputStream();
                FileOutputStream fos = new FileOutputStream(Main.getRootPath()+"/"+dst);
                byte[] buffer = new byte[1024];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                is.close();
            } catch (IOException e) {
                Main.log(e);
            }
            return null;
        }
    }
}
