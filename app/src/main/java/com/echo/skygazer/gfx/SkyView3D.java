package com.echo.skygazer.gfx;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.echo.skygazer.Main;
import com.echo.skygazer.gfx.math.Matrix4d;
import com.echo.skygazer.gfx.math.Point3d;
import com.echo.skygazer.gfx.skyobj.SkyDot;
import com.echo.skygazer.io.Sensors;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class SkyView3D
{
    private double timer = 0;
    private Matrix4d projMatrix = new Matrix4d();
    private Matrix4d pchRotMatrix = new Matrix4d();
    private Matrix4d yRotMatrix = new Matrix4d();
    private Matrix4d aziRotMatrix = new Matrix4d();
    private Matrix4d timeRotMatrix = new Matrix4d();
    private Matrix4d latRotMatrix = new Matrix4d();
    private Matrix4d lonRotMatrix = new Matrix4d();

    private double width = 10;
    private double height = 10;
    private float tx = 0; //Translate all objects' x-coords by this much
    private float ty = 0; //Translate all objects' y-coords by this much

    private double azimuth; //Horizontal
    private double pitch;   //Vertical
    private double roll;    //Rotation of phone screen
    SkyDot sd = new SkyDot();

    public SkyView3D(int width, int height)
    {
        this.width = width;
        this.height = height;
    }

    public void draw(Canvas cs, Paint pt, Map<Integer, SkyObject> skyObjects)
    {
        //Enable experimental AR
        if(!true) {
            azimuth = Sensors.getOrientation(0);
            pitch = Sensors.getOrientation(1);
        }

        timer+=1.0d;

        clampCam();

        //Update rotation matrices based on time, azimuth, pitch, roll
        double theta = timer*0.1;
        double pmScreenSize = height;
        projMatrix.setToProjectionMatrix((int)pmScreenSize, (int)pmScreenSize);     //Set width and height equal, for aspect ratio = 1
        pchRotMatrix.setToXRotationMatrix(pitch*Math.PI/180.0);
        yRotMatrix.setToYRotationMatrix(0);
        aziRotMatrix.setToZRotationMatrix(azimuth*Math.PI/180.0);
        timeRotMatrix.setToYRotationMatrix( 360*Sensors.getDayFractionPassed() );
        latRotMatrix.setToXRotationMatrix( Sensors.getLocation(0)-90d );
        lonRotMatrix.setToZRotationMatrix( Sensors.getLocation(1) );

        /* Background */
        //Set color to dark blue
        pt.setColor(Color.rgb(21, 22, 48));
        //Set color to black if in low light mode
        if(Main.getMainActivity().getSettingValue("low_light_mode")) {
            pt.setColor(Color.rgb(0, 0, 0));
        }
        cs.drawRect(0, 0, (float)width, (float)height, pt);

        //SkyObject list
        for(Map.Entry<Integer, SkyObject> entry : skyObjects.entrySet()) {
            SkyObject so = entry.getValue();
            if( so!=null ) {
                so.draw(cs, pt, this);
            } else {
                Main.log("SkyView3D.onDraw() - null object in 'skyObjects'!");
            }
        }

        //Crosshair
        float chRadius = 24;
        float chWidth = 3;
        pt.setColor(Color.WHITE);
        float fW = (float)width;
        float fH = (float)height;
        cs.drawRect(fW/2-chRadius, fH/2-chWidth, fW/2+chRadius, fH/2+chWidth, pt);
        cs.drawRect(fW/2-chWidth, fH/2-chRadius, fW/2+chWidth, fH/2+chRadius, pt);

        //Debug info
        if( Main.getMainActivity().getSettingValue("advanced_info") ) {
            drawDebug(cs, pt);
        }
    }

    public Point3d getProjectedPoint(Point3d p1)
    {
        //Rotate about y depending on time.
        //We use the % of the day that has already passed based on UTC. This is not completely accurate but it's still good enough.
        Point3d pR1 = Matrix4d.multiply3d(p1, timeRotMatrix);

        //Rotate about x and z depending on position on earth
        Point3d pR2a = Matrix4d.multiply3d(pR1, latRotMatrix);
        Point3d pR2b = Matrix4d.multiply3d(pR2a, lonRotMatrix);

        //Rotate about azimuth, pitch, and y axis
        Point3d pR3a = Matrix4d.multiply3d(pR2b, aziRotMatrix);        //Rotate about azimuth (z)
        Point3d pR3b = Matrix4d.multiply3d(pR3a, yRotMatrix);        //Rotate about y axis
        Point3d pR3c = Matrix4d.multiply3d(pR3b, pchRotMatrix);     //Rotate about pitch (x)

        //Get the translated/rotated point, before projection
        Point3d pT = new Point3d(pR3c);

        //Get projected point
        Point3d pTP = Matrix4d.multiply3d(pT, projMatrix);

        //Scale into view
        pTP.x += 0.5d; pTP.x *= (0.5d*(double)height);
        pTP.y += 1d; pTP.y *= (0.5d*(double)height);// pTP.y += (double)width/2d;

        return new Point3d(pTP.x, pTP.y, pT.z);
    }

    public Point3d getProjectedPoint(double x, double y, double z) { return getProjectedPoint(new Point3d(x, y, z)); }
    public void translate(float x, float y)
    {
        azimuth += -x/10;
        pitch += -y/10;
    }
    public void setWH(int w, int h)  { width = w; height = h; }
    public float getTx()  { return tx; }
    public float getTy()  { return ty; }
    private void clampCam()
    {
        //Pitch should move past straight above (<0) or straight below (>180)
        if( pitch>180d ) {
            pitch = 180d;
        }
        if( pitch<0d ) {
            pitch = 0d;
        }

        //Azimuth should stay within 0-360 degrees
        if(azimuth<0d) { azimuth+=360d; }
        if(azimuth>360d) { azimuth-=360d; }
    }

    @SuppressLint("DefaultLocale")
    private void drawDebug(Canvas cs, Paint pt)
    {
        pt.setColor(Color.GREEN);
        pt.setTextSize(32);

        int dbgAzi = (int)Math.round(azimuth);
        int dbgPch = (int)Math.round(pitch);
        int dbgRll = (int)Math.round(roll);

        String dbgTime = Sensors.getCurrentTimeUTC("hh:mm:ss a");

        String dbgRotDelta = String.format("%.7g", Sensors.getDayFractionPassed());

        String dbgLat = String.format("%.7g", Sensors.getLocation(0) );
        String dbgLong = String.format("%.7g", Sensors.getLocation(1) );

        cs.drawText("(azimuth, pitch, roll) = ("+dbgAzi+", "+dbgPch+", "+dbgRll+")", 0, 40, pt);
        cs.drawText("(lat, long) = ("+dbgLat+", "+dbgLong+") ", 0, 100, pt);
        cs.drawText("(utcTime, rotationFactor) = "+"("+dbgTime+", "+dbgRotDelta+")", 0, 160, pt);
    }
}