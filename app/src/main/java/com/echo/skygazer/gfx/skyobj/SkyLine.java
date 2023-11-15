package com.echo.skygazer.gfx.skyobj;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.echo.skygazer.gfx.SkyObject;
import com.echo.skygazer.gfx.SkyView3D;

public class SkyLine extends SkyObject
{
    SkyDot so1 = null;
    SkyDot so2 = null;

    public SkyLine(SkyDot so1, SkyDot so2) {
        this.so1 = so1;
        this.so2 = so2;
    }

    @Override
    protected void draw(Canvas cs, Paint pt, SkyView3D sv3d) {
        pt.setColor(Color.rgb(255, 200, 200) );
        pt.setStrokeWidth(4);

        if(so1!=null && so2!=null) {
            cs.drawLine( so1.getScreenX(), so1.getScreenY(), so2.getScreenX(), so2.getScreenY(), pt );
        }
    }
}
