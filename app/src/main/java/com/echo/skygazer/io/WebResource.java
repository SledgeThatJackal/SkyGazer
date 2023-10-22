package com.echo.skygazer.io;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.echo.skygazer.Main;
import com.echo.skygazer.MainActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class WebResource
{
    //Source, destination
    private final String src;
    private final String dst;
    //Private flags
    private boolean downloadFinished = false;
    private boolean doCachingFlag = false;
    //Progres dialog
    private ProgressDialog progDialog = null;
    //Downloaded data
    private ArrayList<String> dlData = new ArrayList<>();
    private int id;

    /**
     * Gets a website URL "[src]" that the device can download from.
     * To save that data to disk, use saveAs(dst).
     * To get that data as a String, use getLine(). Make sure to read getLine()'s documentation before using it.
     *
     * @param src - Source: URL of whatever you are trying to download (ex: "https://raw.githubusercontent.com/astronexus/HYG-Database/master/README.md")
     * @param dst - Destination: Filename/path of where the downloaded data will be (ex: "download.txt" -> points to a file at "/data/user/0/com.echo.skygazer/files/download.txt").
     */
    public WebResource(String src, String dst, int id) {
        //Set src (url) and dst (filepath)
        this.src = src;
        this.dst = dst;
        this.id = id;

        //Create File object.
        File file = new File( getExpandedRootPath(dst) );
        //If the file already exists (is cached), build dlData.
        if(file.exists()) {
            //Build dlData directly from file.
            try {
                FileReader in = new FileReader(file);
                BufferedReader bufferedReader = new BufferedReader(in);
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    dlData.add(line);
                }
            } catch (FileNotFoundException ignored){} catch (IOException e) {
                Main.log("Failed to build dlData from file");
            }
            //Call onProcessingFinished() as this file was already downloaded and processed.
            onProcessingFinished();
        //If the file doesn't exist, download file to memory (not to disk, yet!) and store it in 'dlData'.
        } else {
            //Show download progress window.
            showDownloadProgressWindow();
            //Execute asynchronous download task - download web file to memory and store it in 'dlData'.
            //We have to do it this way or else Android will throw a NetworkOnMainThreadException.
            //To store as a physical file on disk, use the cache() function.
            DownloadFileTask dft = new DownloadFileTask();
            dft.execute(src);
            //Do post execution tasks -> DownloadFileTask.onPostExecute(). This may happen long after this constructor finishes.
        }
    }
    public WebResource(String src, String dst) { this(src, dst, -1); }

    /**
     * This method is called whenever the web resource is finished scanning.
     * This could be either when the download finished (slower) or when the cached file is finished being read (faster).
     * Any use of getWikipediaInfo() or getLine() must happen in this method for reliable results.
     * To help with this process, you can assign an ID in the constructor.
     */
    private void onProcessingFinished() {
        Main.log("Web Resource with id="+id+" just finished processing.");

        switch (id) {
            case 1234: {
                //MainActivity ma = Main.getMainActivity();
                //ma.[...]
                
                Main.log("This WebResource is a wikipedia article that says: "+getWikipediaInfo());
            }
        }
    }

    /**
     * Returns a single line from 'dlData'.
     * Basically, this is what we will use if the user has "caching" set to OFF in the settings.
     * @return Goes to the line number 'lineNum' in the web data and returns that line as a string.
     */
    private String getLine(int lineNum) {
        return dlData.get(lineNum);
    }

    /**
     * TODO: Get rid of all unnecessary elements to get the raw text.
     * If 'src' is a Wikipedia article, get the raw text of the top paragraphs.
     * @param numParagraphs Number of paragraphs to get the raw text of.
     * @return A large single string, with proper formatting.
     */
    private String getWikipediaInfo(int numParagraphs) {
        if(!src.startsWith("https://en.wikipedia.org/")) {
            Main.log("This doesn't appear to be a Wikipedia article - returning null.");
            return null;
        }

        //Create StringBuilder
        StringBuilder result = new StringBuilder();

        //Iterate through lines in the HTML file to populate the StringBuilder.
        for(int i = 0; i<dlData.size(); i++) {
            //Get the current line.
            String line = dlData.get(i);

            /*
                Now, we will search for a paragraph element ("<p>").
                We omit the right angle bracket, as there might be a <p class=...> or something like that.
                So, search for "<p"
             */

            //If "<p" not found, continue to next line in loop
            if(!line.contains("<p")) {
                continue;
            //If there is a "<p", parse through it to get the raw text.
            } else {
                //TODO extract unnecessary web elements to just get the raw text
                result.append(line);
            }
        }

        //Return StringBuilder
        return result.toString();
    }
    private String getWikipediaInfo() {
        return getWikipediaInfo(0);
    }

    private String getExpandedRootPath(String dst) {
        return Main.getRootCachePath()+"/"+dst;
    }

    /**
     * This will create a new file with the completely downloaded contents of dlData."
     */
    public void cache() {
        //Create File object.
        File file = new File(getExpandedRootPath(dst));

        //If file already exists, do not attempt to do anything
        if(file.exists()) {
            Main.log("Cached file '"+getExpandedRootPath(dst)+"' exists - all good.");
        //If file doesn't exist yet
        } else {
            if(downloadFinished) {
                forceCache();   //If download is already finished by this point, force cache now.
            } else {
                //Set 'doCachingFlag' = true - this will create the file once the download is finished.
                doCachingFlag = true;
            }
        }
    }

    /**
     * NOTE: Under normal circumstances, use cache() instead!
     * This method, included for debug purposes, will take everything currently in dlData and write it to the 'dst'.
     * This means that if the file is not finished downloading, the written file will also be incomplete.
     */
    public void forceCache() {
        //Create file on disk.
        try {
            File file = new File(getExpandedRootPath(dst));
            if (file.createNewFile()) {
                Main.log("New file "+file.getName()+" created.");
            }
        } catch (IOException e) {
            Main.log("File creation error: "+e.toString());
        }

        //Write everything from the dlData to the file on disk.
        try {
            FileWriter writer = null;
            writer = new FileWriter( getExpandedRootPath(dst) );
            for (int i = 0; i<dlData.size(); i++) {
                writer.write(getLine(i));
                writer.write(System.lineSeparator());
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            Main.log("Could not write - "+e.toString());
        }
    }

    private void showDownloadProgressWindow() {
        //Deprecated, but works for now - we should prefer another way of doing this.
        progDialog = new ProgressDialog(Main.getMainActivity());
        progDialog.setTitle("Fetching Web Resource");
        progDialog.setMessage("Attempting to get '"+dst+"' - make sure you have a network connection. Please wait...");
        progDialog.setIndeterminate(true);
        progDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progDialog.show();
    }

    private void dismissDownloadProgressWindow() {
        progDialog.dismiss();
    }

    /**
     * Download file to memory without saving to a cached file.
     */
    @SuppressLint("StaticFieldLeak")
    private class DownloadFileTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... urls) {
            String url = urls[0];
            Main.log("Attempting download of '"+url+"'...");
            try {
                //Get URL and generate HTTP connection
                URL urlObj = new URL(url);
                HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();
                con.setRequestMethod("GET");

                //Download file to memory (not to disk), store in bufferedReader.
                InputStream is = con.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
                //Move everything in the bufferedReader to dlData.
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    dlData.add(line);
                }
            } catch (IOException e) {
                Main.log(e);
            }
            return null;
        }

        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            Main.log("Finished download of '"+src+"'!");
            dismissDownloadProgressWindow();

            downloadFinished = true;
            if(doCachingFlag) {
                forceCache();
            }
            onProcessingFinished();
        }
    }
}
