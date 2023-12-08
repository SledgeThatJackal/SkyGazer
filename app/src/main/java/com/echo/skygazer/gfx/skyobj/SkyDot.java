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
    boolean twinkling = false;
    String displayName = "null";

    public SkyDot(HygDataRow data) {
        this.data = data;

        if(data.getProperName()==null) {
            displayName = "";
        } else {
            displayName = data.getProperName();
            color = Color.rgb(255, 255, 200);
        }

        //Calculate sizePx (luminosity) based on apparent magnitude.
        int roundedMag = (int)Math.round( data.getMag() );

        //Find relative brightness
        //Loosely based off of https://en.wikipedia.org/wiki/Apparent_magnitude
        float relBrightness = 5;
        switch (roundedMag) {
            //Visible to human eye
            case -2: sizePx = 15f; break; //Sirius
            case -1: sizePx = 12f; break; //Sirius is brighter than this
            case  0: sizePx = 10f; break; //Arcturus, Canopus are brighter than this
            case  1: sizePx = 8f; break; //15 stars are brighter than this
            case  2: sizePx = 6f; break; //48 brighter
            case  3: sizePx = 4f; break; //171 brigther
            //Twinkling (arbitrary):
            case  4: sizePx = 2.50f; break; //513 brighter
            case  5: sizePx = 1.00f; break; //1602 brighter
            case  6: sizePx = 0.40f; break;
            //Invisible to human eye:
            case  7: sizePx = 0.16f; break;
            case  8: sizePx = 0.63f; break;
            case  9: sizePx = 0.25f; break;
            case 10: sizePx = 0.10f; break; //340,000 brighter
            default: sizePx = 0.05f; break;
        }

        if(roundedMag>3) {
            twinkling = true;
        }

        if(sizePx<0.5f) {
            sizePx = 0.5f;
        }

    }

    public SkyDot() {
        this( new HygDataRow(-1, "Loading...", 0, 0, 0, 0, 0) );
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

        boolean doDraw = true;
        if( twinkling && Main.random.nextInt(100)>95 ) {
            doDraw = false;
        }

        if(doDraw) {
            //Outer circle
            pt.setColor(Color.WHITE);
            cs.drawCircle(screenX, screenY, sizePx+1, pt);
            //Inner circle
            pt.setColor( color );
            cs.drawCircle(screenX, screenY, sizePx, pt);
        }

        pt.setColor(Color.WHITE);
        pt.setTextSize(40);
        cs.drawText(displayName, screenX+sizePx, screenY+sizePx, pt);
        pt.setTextSize(50);
    }

    public boolean hasNegativeDepth() { return hasNegativeDepth; }
    public float getScreenX() { return screenX; }
    public float getScreenY() { return screenY; }
    public String getDisplayName() {
        if(displayName == null){
            return "Loading...";
        }
        return displayName;
    }
    public String toUserString() {
        return
            "Apparent magnitude: "+data.getMag()+"\n"+
            "Equatorial coordinates: "+data.getX()+", "+data.getY()+", "+data.getZ()+"\n"+
            "Distance: "+data.getDist()+" parsecs\n\n";
    }
}
