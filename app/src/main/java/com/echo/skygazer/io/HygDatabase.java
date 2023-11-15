package com.echo.skygazer.io;

<<<<<<< Updated upstream
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

=======
>>>>>>> Stashed changes
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

<<<<<<< Updated upstream
    public static void drawDataWindow(SkyView sv, Canvas cs, Paint pt, float sw, float sh) {
        //Outer
        pt.setColor(Color.rgb(0, 80, 255));
        cs.drawRect(wMargin, wMargin, sw-wMargin, sh-wMargin*2-40, pt);
        //Inner
        pt.setColor(Color.rgb(0, 160, 225));
        cs.drawRect(wMargin+8, wMargin+8, sw-wMargin-8, sh-wMargin*2-40-8, pt);

        pt.setColor(Color.rgb(0, 160, 160));
        cs.drawRect(wMargin+8, sh-wMargin*2-140+8, sw-wMargin-8, sh-wMargin*2-40-8, pt);

        //Build Title text
        pt.setColor(Color.WHITE);
        String txtTitle = sv.getSelectedSkyDot().getDisplayName();
        float txtTitleW = pt.measureText(txtTitle);
        cs.drawText(txtTitle, sw/2-txtTitleW/2, wMargin+64, pt);

        //Build database row text
        //ID can be found in constant time, so we are fine calling get() at 60 times a second.
        pt.setColor(Color.BLUE);
        String txtDbId = "Database Row ID: "+hygDictionary.get( sv.getSelectedSkyDot().getDisplayName() );
        float txtDbIdW = pt.measureText(txtDbId);
        cs.drawText(txtDbId, sw/2-txtDbIdW/2, sh-wMargin*2-140-32, pt);

        //Build "Close" text
        pt.setColor(Color.WHITE);
        String txtClose = "Close";
        float txtCloseW = pt.measureText(txtClose);
        cs.drawText(txtClose, sw/2-txtCloseW/2, sh-wMargin*2-140+64, pt);

        //Build wikipedia text
        String txtWiki = WebResource.getCurrentWikipediaText();
        TextPaint txtPt = new TextPaint();
        txtPt.setTextSize(40);
        txtPt.setColor(Color.WHITE);
        StaticLayout mTextLayout = new StaticLayout(txtWiki, txtPt, (int)(sw-2*wMargin)-10, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
        cs.save();
        cs.translate(wMargin+8, wMargin+128);
        mTextLayout.draw(cs);
        cs.restore();
    }

    public static void drawDataPreviewTab(SkyView sv, Canvas cs, Paint pt, float sw, float sh) {
        pt.setColor(Color.rgb(0, 160, 225));
        cs.drawRect(sw/2-ptWidth/2, sh-navbarHeight, sw/2+ptWidth/2, sh-navbarHeight+ptHeight, pt);

        pt.setColor(Color.WHITE);
        String txt = sv.getSelectedSkyDot().getDisplayName();
        float txtW = pt.measureText(txt);
        cs.drawText(txt, sw/2-txtW/2, sh-navbarHeight+64, pt);
        //cs.drawText();
=======
    public static boolean isInitialized() { return initialized; }
>>>>>>> Stashed changes

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
            //return;
        }

        for(int i = 0; i<1000; i++) {
            selectRow( Main.random.nextInt(hygDictionary.size()) );
            if( selectedHygData==null ) continue;
            //ss.addSkyObject( selectedHygData.getId(), new SkyDot(selectedHygData));
        }

        for( Map.Entry<String, Integer> entry : hygDictionary.entrySet() ) {
            selectRow( entry.getValue() );
            ss.addSkyObject(selectedHygData.getId(), new SkyDot(selectedHygData));
        }

        //selectRow( "Betelgeuse" );
        //ss.addSkyObject(selectedHygData.getId(), new SkyDot(selectedHygData));

        //Main.log(selectedHygData.getId());
        Main.log(selectedHygData);
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
