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
import com.echo.skygazer.gfx.SkyView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HygDatabase {

    public static final float navbarHeight = 250;
    public static final float ptWidth = 512+256;
    public static final float ptHeight = 96;
    public static final float wMargin = 160;

    private static boolean initialized = false;

    private static Map<Integer, HygDataRow> hygMap = new HashMap<>();       //Find stars given their database ID (~110k)
    private static Map<String, Integer> hygDictionary = new HashMap<>();    //Find star ID given their proper name (only 367 of them, not that many). These are the stars we can "search" for when we get to that function.
    private static int selectedRowId = -1;

    public static void init(ArrayList<String> dlData) {

        Main.log("Starting HYG initialization...");

        if(initialized) {
            Main.log("[WARNING] static class HygDatabase has already been initialized!");
        } else {

            // Build 'hygMap' based on 'dlData'.
            for(int i = 1; i<dlData.size(); i++) {

                String[] rowComponents = dlData.get(i).split(",");
                int id = 0;
                try {
                    id = Integer.parseInt(rowComponents[0]);
                } catch (NumberFormatException e) {
                    continue;
                }

                String properName = rowComponents[6];
                if(!properName.equals("")) {
                    HygDataRow hygDR = new HygDataRow(id, properName);
                    hygMap.put(id, hygDR);
                }
            }

            // Build 'hygDictionary' from 'hygMap'
            for(int i : hygMap.keySet()) {
                HygDataRow hdr = hygMap.get(i);

                if(hdr!=null) {
                    hygDictionary.put(hdr.getProperName(), hdr.getId());
                } else {
                    Main.log("null hdr. rip, I did something wrong.");
                }
            }

            initialized = true;
        }
    }

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
        Context context = sv.getContext();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if(sharedPreferences.getBoolean("advanced_info", false)) {
            pt.setColor(Color.BLUE);
            String txtDbId = "Database Row ID: " + hygDictionary.get(sv.getSelectedSkyDot().getDisplayName());
            float txtDbIdW = pt.measureText(txtDbId);
            cs.drawText(txtDbId, sw / 2 - txtDbIdW / 2, sh - wMargin * 2 - 140 - 32, pt);
        }

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
                selectedRowId = entry.getValue();
            }
            i++;
        }

        if( hygMap.get(selectedRowId)==null ) {
            Main.log("null map get. rip, I did something wrong.");
            return -2;
        }
        return i;
    }

    /**
     * Turns all current stars into random stars from the set of stars that have proper names.
     */
    public static void setStarsRandomly(SkyView sv, int numStars) {
        for(int i = 0; i<numStars; i++) {
            selectRandomDictionaryRow();
            sv.getSkyDot(i).setDisplayName( hygMap.get(selectedRowId).getProperName() );
        }
    }

    public static int selectRow(String starName) {
        try {
            selectedRowId = hygDictionary.get(starName);
        } catch (Exception e) {
            return -1;
        }
        return selectedRowId;
    }

    public static int selectRow(int rowID) {
        return -1;
    }



    public static boolean isInitialized() { return initialized; }
}
