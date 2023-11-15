package com.echo.skygazer.gfx;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.preference.PreferenceManager;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import com.echo.skygazer.io.HygDatabase;
import com.echo.skygazer.io.WebResource;

public class InfoView {
    public static final float navbarHeight = 250;
    public static final float ptWidth = 512+256;
    public static final float ptHeight = 96;
    public static final float wMargin = 160;

    public static String cutoffTextAtHeight(String s, int width, int maxHeight)
    {
        //Get current height
        TextPaint txtPt = new TextPaint();
        StaticLayout mTextLayout = new StaticLayout(s, txtPt, width, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
        int currentHeight = mTextLayout.getHeight();

        //While current height is larger than max height
        while( currentHeight>maxHeight ) {
            s = s.substring(0, s.length()-23)+"...";    //Reduce string and add ... at the end.
            StaticLayout tempLayout = new StaticLayout(s, txtPt, width, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            currentHeight = tempLayout.getHeight();
        }

        return s;
    }

    public static void drawWikipediaText(Canvas cs, float sw, float sh) {
        String txtWiki = WebResource.getCurrentWikipediaText();
        TextPaint txtPt = new TextPaint();
        txtPt.setTextSize(40);
        txtPt.setColor(Color.WHITE);

        txtWiki = cutoffTextAtHeight(txtWiki, (int)(sw-2*wMargin)-10,100);
        StaticLayout mTextLayout = new StaticLayout(txtWiki, txtPt, (int)(sw-2*wMargin)-10, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);

        cs.save();
        cs.translate(wMargin+8, wMargin+128);
        mTextLayout.draw(cs);
        cs.restore();
    }

    public static void drawDataWindow(SkySimulation sv, Canvas cs, Paint pt, float sw, float sh) {
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
            String txtDbId = "Database Row ID: " + HygDatabase.selectRow( sv.getSelectedSkyDot().getDisplayName() );
            float txtDbIdW = pt.measureText(txtDbId);
            cs.drawText(txtDbId, sw / 2 - txtDbIdW / 2, sh - wMargin * 2 - 140 - 32, pt);
        }

        //Build "Close" text
        pt.setColor(Color.WHITE);
        String txtClose = "Close";
        float txtCloseW = pt.measureText(txtClose);
        cs.drawText(txtClose, sw/2-txtCloseW/2, sh-wMargin*2-140+64, pt);

        //Build wikipedia text
        drawWikipediaText(cs, sw, sh);
    }

    public static void drawDataPreviewTab(SkySimulation sv, Canvas cs, Paint pt, float sw, float sh) {
        pt.setColor(Color.rgb(0, 160, 225));
        cs.drawRect(sw/2-ptWidth/2, sh-navbarHeight, sw/2+ptWidth/2, sh-navbarHeight+ptHeight, pt);

        pt.setColor(Color.WHITE);
        String txt = sv.getSelectedSkyDot().getDisplayName();
        float txtW = pt.measureText(txt);
        cs.drawText(txt, sw/2-txtW/2, sh-navbarHeight+64, pt);
        //cs.drawText();

    }
}
