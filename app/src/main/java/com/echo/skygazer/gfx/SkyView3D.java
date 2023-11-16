package com.echo.skygazer.gfx;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.echo.skygazer.Main;
import com.echo.skygazer.gfx.math.Matrix4d;
import com.echo.skygazer.gfx.math.Point3d;
import com.echo.skygazer.gfx.skyobj.SkyDot;
import com.echo.skygazer.gfx.skyobj.SkyLine;

import java.util.Map;

public class SkyView3D
{
    private double timer = 0;
    private Matrix4d projMatrix = new Matrix4d();
    private Matrix4d xRotMatrix = new Matrix4d();
    private Matrix4d yRotMatrix = new Matrix4d();
    private Matrix4d zRotMatrix = new Matrix4d();
    private double width = 10;
    private double height = 10;
    private float tx = 0; //Translate all objects' x-coords by this much
    private float ty = 0; //Translate all objects' y-coords by this much

    private double yaw;     //Horizontal
    private double pitch;   //Vertical
    SkyDot sd = new SkyDot();

    public SkyView3D(int width, int height)
    {
        this.width = width;
        this.height = height;
    }

    public void draw(Canvas cs, Paint pt, Map<Integer, SkyObject> skyObjects)
    {
        timer+=1.0d;

        //Pitch cannot move past straight above (180) or straight below (-180)
        if( pitch>180d ) {
            pitch = 180d;
        }
        if( pitch<0 ) {
            pitch = 0;
        }

        //Update rotation matrices based on time, pitch, and yaw
        double theta = timer*0.1;
        double pmScreenSize = height;
        projMatrix.setToProjectionMatrix((int)pmScreenSize, (int)pmScreenSize);     //Set width and height equal, for aspect ratio = 1
        xRotMatrix.setToXRotationMatrix(pitch*Math.PI/180.0);
        yRotMatrix.setToYRotationMatrix(timer*Math.PI/180.0);
        yRotMatrix.setToYRotationMatrix(0);
        zRotMatrix.setToZRotationMatrix(yaw*Math.PI/180.0);

        //Background
        pt.setColor(Color.rgb(21, 22, 48));
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
        pt.setColor(Color.YELLOW);
        cs.drawText("Yaw: "+yaw, 0, 40, pt);
        cs.drawText("Pitch: "+pitch, 0, 120, pt);
    }

    public Point3d getProjectedPoint(Point3d p1)
    {
        //Create rotated points based of of initial point
        Point3d pRZ = Matrix4d.multiply3d(p1, zRotMatrix);      //Rotate about z axis
        Point3d pRYZ = Matrix4d.multiply3d(pRZ, yRotMatrix);    //Rotate about y axis
        Point3d pRXYZ = Matrix4d.multiply3d(pRYZ, xRotMatrix);  //Rotate about x axis

        //Get translated point
        Point3d pT = new Point3d(pRXYZ);
        //pT.x += 123.0f;
        //pT.y +=-456.0f;
        //pT.z += 300.0f;

        //Get projected point
        Point3d pTP = Matrix4d.multiply3d(pT, projMatrix);

        //Scale into view
        pTP.x += 0.5d; pTP.x *= (0.5d*(double)height);
        pTP.y += 1d; pTP.y *= (0.5d*(double)height);// pTP.y += (double)width/2d;

        return new Point3d(pTP.x, pTP.y, pT.z);
    }

    public Point3d getProjectedPoint(double x, double y, double z)
    {
        return getProjectedPoint(new Point3d(x, y, z));
    }

    public void translate(float x, float y)
    {
        yaw += -x/10;
        pitch += -y/10;
    }

    public void setWH(int w, int h)
    {
        width = w;
        height = h;
    }

    public float getTx()  { return tx; }
    public float getTy()  { return ty; }
}