package com.echo.skygazer.gfx.skyobj;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.echo.skygazer.gfx.SkyObject;

public class SkyLine extends SkyObject
{
    SkyDot so1;
    SkyDot so2;

    public SkyLine(SkyDot so1, SkyDot so2) {
        this.so1 = so1;
        this.so2 = so2;
    }

    @Override
    protected void draw(Canvas cs, Paint pt, float tx, float ty) {
        pt.setColor(Color.rgb(255, 200, 200) );
        pt.setStrokeWidth(4);
        cs.drawLine( so1.getScreenX()+tx, so1.getScreenY()+ty, so2.getScreenX()+tx, so2.getScreenY()+ty, pt );
    }
}
