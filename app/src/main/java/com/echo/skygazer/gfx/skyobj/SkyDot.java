package com.echo.skygazer.gfx.skyobj;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.echo.skygazer.Main;
import com.echo.skygazer.gfx.SkyObject;
import com.echo.skygazer.gfx.SkyView3D;
import com.echo.skygazer.gfx.math.Point3d;
import com.echo.skygazer.io.HygDataRow;

public class SkyDot extends SkyObject
{
    HygDataRow data = null;

    float screenX = 100;
    float screenY = 100;
    boolean hasNegativeDepth = false;   //Whether this point is in the camera's inverted FOV (FOV behind the camera). If it is, we can't see it or click on it!
    int color = Color.rgb(255, 200, 200);  //Color to render shape
    float sizePx = 5;             //Size of circle in pixels
    String displayName = "null";

    public SkyDot(HygDataRow data) {
        this.data = data;

        if(data.getProperName()==null) {
            displayName = "";
        } else {
            displayName = data.getProperName();
            color = Color.rgb(255, 255, 200);
        }

        sizePx = (float)data.getMag()/2.0f+2f;
    }

    public SkyDot() {
        this( new HygDataRow(-1, "Loading...", 0, 0, 0, 0) );
    }

    @Override
    public void draw(Canvas cs, Paint pt, SkyView3D sv3d) {
        Point3d pp3d = sv3d.getProjectedPoint(data.getX(), data.getY(), data.getZ());
        screenX = (float)pp3d.x;
        screenY = (float)pp3d.y;
        hasNegativeDepth = false;
        if(pp3d.z<0) {
            hasNegativeDepth = true;
            return;
        }

        //Outer circle
        pt.setColor(Color.WHITE);
        cs.drawCircle(screenX, screenY, sizePx+1, pt);
        //Inner circle
        pt.setColor( color );
        cs.drawCircle(screenX, screenY, sizePx, pt);

        pt.setColor(Color.WHITE);
        pt.setTextSize(40);
        cs.drawText(displayName, screenX+sizePx, screenY+sizePx, pt);
        pt.setTextSize(50);
    }

    public boolean hasNegativeDepth() { return hasNegativeDepth; }
    public float getScreenX() { return screenX; }
    public float getScreenY() { return screenY; }
    public String getDisplayName() { return displayName; }
}
