package com.echo.skygazer.gfx.skyobj;

import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import androidx.preference.PreferenceManager;

import com.echo.skygazer.Main;
import com.echo.skygazer.MainActivity;
import com.echo.skygazer.gfx.SkyObject;
import com.echo.skygazer.gfx.SkySimulation;
import com.echo.skygazer.gfx.SkyView3D;
import com.echo.skygazer.ui.sky.SkyFragment;

public class SkyLine extends SkyObject
{
    SkyDot sd1 = null;
    SkyDot sd2 = null;
    int id1 = 0;
    int id2 = 0;

    public SkyLine(int id1, int id2) {
        this.id1 = id1;
        this.id2 = id2;
    }

    @Override
    protected void draw(Canvas cs, Paint pt, SkyView3D sv3d) {
        sd1 = SkyFragment.getSkySim().getSkyDot(id1);
        sd2 = SkyFragment.getSkySim().getSkyDot(id2);

        if( sd1==null || sd2==null ) {
            return;
        }

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(Main.getMainActivity());
        boolean value = pref.getBoolean("constellation_highlighting", true);

        if( !sd1.hasNegativeDepth() && !sd2.hasNegativeDepth() && value) {
            pt.setColor(Color.rgb(255, 200, 200) );
            pt.setStrokeWidth(3f);
            cs.drawLine( sd1.getScreenX(), sd1.getScreenY(), sd2.getScreenX(), sd2.getScreenY(), pt );
        }
    }
}
