package com.echo.skygazer.gfx.skyobj;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.echo.skygazer.Main;
import com.echo.skygazer.gfx.SkyObject;

public class SkyDot extends SkyObject
{
    int color = Color.rgb(255, 200, 200);  //Color to render shape
    float sizePx = 5;             //Size of circle in pixels
    float screenX = 100;
    float screenY = 100;
    String displayName = "null";

    public SkyDot(float screenX, float screenY, String displayName) {
        this.screenX = screenX;
        this.screenY = screenY;
        this.displayName = displayName;

        int a = Main.random.nextInt(256);
        int b = Main.random.nextInt(256);
        int c = Main.random.nextInt(256);
        sizePx = 4+Main.random.nextInt(10);

        color = Color.rgb( a, b, c );
    }
    public SkyDot(int screenX, int screenY) {
        this( screenX, screenY,"Random Star "+String.valueOf(Main.random.nextInt(100000)) );
    }
    public SkyDot() {
        this( Main.random.nextInt(1000), Main.random.nextInt(1000) );
    }

    @Override
    protected void draw(Canvas cs, Paint pt, float tx, float ty) {
        //Outer circle
        pt.setColor(Color.WHITE);
        cs.drawCircle(screenX+tx, screenY+ty, sizePx+1, pt);
        //Inner circle
        pt.setColor( color );
        cs.drawCircle(screenX+tx, screenY+ty, sizePx, pt);

        pt.setColor(Color.WHITE);
        pt.setTextSize(50);
        cs.drawText(displayName, screenX+tx+sizePx, screenY+ty+sizePx, pt);
    }

    public float getScreenX() { return screenX; }
    public float getScreenY() { return screenY; }
    public String getDisplayName() { return displayName; }
    public void setScreenX(float screenX) { this.screenX = screenX; }
    public void setScreenY(float screenY) { this.screenY = screenY; }
    public void setScreenXY(float screenX, float screenY) { setScreenX(screenX); setScreenY(screenY); }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
}
