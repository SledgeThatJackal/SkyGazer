package com.echo.skygazer.io;

import com.echo.skygazer.Main;
import com.echo.skygazer.gfx.SkySimulation;
import com.echo.skygazer.gfx.skyobj.SkyDot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
                double mag = 0.0;
                double x = 0.0;     //Get x
                double y = 0.0;     //Get y
                double z = 0.0;     //Get z
                try {
                    id = Integer.parseInt(rowComponents[0]);
                    properName = rowComponents[6];
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
                    hygMap.put( id, new HygDataRow(id, properName, mag, x, y, z) );
                } else {
                    hygMap.put( id, new HygDataRow(id, null, mag, x, y, z) );
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

    /**
     * Whenever the application begins or the Sky view is navigated to, build all visible stars.
     * NOTE: addSkyObject does nothing if we try to add an ID that already exists (this happens for some of the named stars in the 2nd for-loop below).
     * @param ss
     */
    public static void setVisibleStars(SkySimulation ss) {
        //Add all stars that are part of constellations
        for(int i = 0; i<1000; i++) {
            selectRow( Main.random.nextInt(hygDictionary.size()) );
            if( selectedHygData==null ) continue;
            //ss.addSkyObject( selectedHygData.getId(), new SkyDot(selectedHygData));
        }

        //Add all named stars
        for( Map.Entry<String, Integer> entry : hygDictionary.entrySet() ) {
            selectRow( entry.getValue() );
            ss.addSkyObject(selectedHygData.getId(), new SkyDot(selectedHygData));
        }

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
}
