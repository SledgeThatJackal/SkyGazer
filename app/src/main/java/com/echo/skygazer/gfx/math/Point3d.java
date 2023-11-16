package com.echo.skygazer.gfx.math;

import androidx.annotation.NonNull;

import com.echo.skygazer.Main;

public class Point3d {
    public double x;
    public double y;
    public double z;

    public Point3d(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Point3d(Point3d p) {
        this(p.x, p.y, p.z);
    }

    public Point3d(double ra, double dec) {
        double r = 10; //Radius from center
        double theta = (ra);
        double phi = (Math.PI/2.0-dec);

        //5.919529, 7.407063 -> 0.1033152712, 0.1292776373
        //3.189296,151.364387,19.682142

        x = r*Math.sin(theta)*Math.cos(phi);
        y = r*Math.sin(theta)*Math.sin(phi);
        z = r*Math.cos(theta);

        //0.13295499475883815
        //1.0227096634034194
        //9.946677229796387

        //Main.log("ra "+ra);
        //Main.log("dec "+dec);
        //Main.log("x "+x);
        //Main.log("y "+y);
        //Main.log("z "+z);
    }

    public void setPolar()
    {

    }

    public Point3d() {
        this(0, 0, 0);
    }

    @NonNull
    @Override
    public String toString() {
        return "("+x+", "+y+", "+z+")";
    }
}
