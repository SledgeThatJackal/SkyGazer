package com.echo.skygazer.io;

import android.content.Context.*;

import android.content.Context;
import android.content.SharedPreferences;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.preference.PreferenceManager;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import com.echo.skygazer.Main;
import com.echo.skygazer.MainActivity;
import com.echo.skygazer.gfx.SkySimulation;
import com.echo.skygazer.gfx.skyobj.SkyDot;
import com.echo.skygazer.ui.sky.SkyFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HygDatabase {

    private static boolean initialized = false;

    private static final Map<Integer, HygDataRow> hygMap = new HashMap<>();       //Find stars given their database ID (~110k)
    private static final Map<String, Integer> hygDictionary = new HashMap<>();    //Find star ID given their proper name (only 367 of them, not that many). These are also the stars we can "search" for.
    private static HygDataRow selectedHygData = null;
    private static boolean initVisuals = false;

    public static void init(ArrayList<String> dlData) {

        Main.log("Starting HYG initialization...");

        if(initialized) {
            Main.log("[WARNING] static class HygDatabase has already been initialized!");
        } else {
            // Build 'hygMap' based on 'dlData'.
            for(int i = 1; i<dlData.size(); i++) {
                String[] rowComponents = dlData.get(i).split(",");

                //Get components
                int id = 0;         //Get ID (id)
                String properName = "Error";
                double dist = 0.0;
                double mag = 0.0;
                double x = 0.0;     //Get x
                double y = 0.0;     //Get y
                double z = 0.0;     //Get z
                try {
                    id = Integer.parseInt(rowComponents[0]);
                    properName = rowComponents[6].trim();
                    dist = Double.parseDouble(rowComponents[9]);
                    mag = Double.parseDouble(rowComponents[13]);
                    x = Double.parseDouble(rowComponents[17]);
                    y = Double.parseDouble(rowComponents[18]);
                    z = Double.parseDouble(rowComponents[19]);
                } catch (NumberFormatException e) {
                    Main.log("[WARNING] Couldn't parse row "+i+" in dlData, defaulting values to 0 and skipping");
                    continue;
                }

                //Build data row
                if(!properName.equals("")) {
                    //Put proper name in data row if it exists
                    hygMap.put( id, new HygDataRow(id, properName, dist, mag, x, y, z) );
                } else {
                    hygMap.put( id, new HygDataRow(id, null, dist, mag, x, y, z) );
                }
            }

            // Build 'hygDictionary' from 'hygMap'
            for(int i : hygMap.keySet()) {
                HygDataRow hdr = hygMap.get(i);

                if(hdr!=null) {
                    hygDictionary.put(hdr.getProperName(), hdr.getId());
                } else {
                    Main.log("null hdr because I did something wrong.");
                }
            }


            SkyFragment.getSkySim().onHygDatabaseInit();
            initialized = true;
        }
    }

    public static boolean isInitialized() { return initialized; }

    public static HygDataRow getSelectedHygData() {
        return selectedHygData;
    }

    public static int selectRandomDictionaryRow() {
        if(!initialized) {
            Main.log("[WARNING] static class HygDatabase not initialized yet...");
            return -1;
        }

        int keySetSize = hygDictionary.keySet().size()-1;

        int item = Main.random.nextInt(keySetSize);
        int i = 1;
        for(Map.Entry<String, Integer> entry : hygDictionary.entrySet())
        {
            if (i == item) {
                selectRow( entry.getValue() );
            }
            i++;
        }
        return i;
    }

    public static void setVisibleStars(SkySimulation ss) {
        if(initVisuals) {
            return;
        }

        //Override specific stars to not add.
        HashSet<Integer> exceptions = new HashSet<>();
        exceptions.add(0);      //"Sol", which is our own sun.
        exceptions.add(71456);  //"Rigil Kentaurus" (Alpha centauri). Too close to the 3D camera for some reason, which causes it to spaz out - we will fix this later on.

        //Get stars that are part of a constellation
        for( int id : Main.getConstellations().getSpecialStars() ) {
            //Add them
            if(!exceptions.contains(id)) {
                selectRow(id);
                ss.addSkyObject(selectedHygData.getId(), new SkyDot(selectedHygData));
            }
        }

        //Get stars that are named
        for( Map.Entry<String, Integer> entry : hygDictionary.entrySet() ) {
            int id = entry.getValue();
            //Add them
            if(!exceptions.contains(id)) {
                selectRow(id);
                ss.addSkyObject(selectedHygData.getId(), new SkyDot(selectedHygData));
            }
        }

        SkyFragment.getSkySim().onHygDatabaseInit();
        initVisuals = true;
    }

    public static int selectRow(int rowID) {
        selectedHygData = null;
        try {
            selectedHygData = hygMap.get(rowID);
        } catch (Exception e) {
            return -1;
        }
        return rowID;
    }

    public static List<Integer> searchStars(String searchInput){
        List<Integer> matchingIDs = new ArrayList<>();
        for(Map.Entry<String, Integer> entry: hygDictionary.entrySet()){
            String starName = entry.getKey();
            if(starName.toLowerCase().contains(searchInput)){
                matchingIDs.add(entry.getValue());
            }
        }
        return matchingIDs;
    }

    public static int selectRow(String starName) {
        selectedHygData = null;

        int row = -1;
        try {
            if(hygDictionary.get(starName) != null) {
                row = hygDictionary.get(starName);
            }
        } catch (NullPointerException ignored) {}


        return selectRow(row);
    }

    public static void reinitVisuals() {
        initVisuals = false;
    }
}