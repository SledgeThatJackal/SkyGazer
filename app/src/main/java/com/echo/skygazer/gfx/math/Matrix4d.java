package com.echo.skygazer.gfx.math;

import android.graphics.Point;

import com.echo.skygazer.Main;

public class Matrix4d {

    double[][] mat = new double[4][4];
    private static boolean loggedWarning = false;

    public Matrix4d() {
        reset();
    }

    public double get(int matX, int matY)
    {
        return mat[matX][matY];
    }

    public void reset()
    {
        //Set matrix to all 0's
        for(int x = 0; x<4; x++) {
            for(int y = 0; y<4; y++) {
                mat[x][y] = 0;
            }
        }
    }

    public void setToProjectionMatrix(int screenWidth, int screenHeight)
    {
        //Matrix taken from https://www.youtube.com/watch?v=ih20l3pJoeU
        double near = 0.1d;     //Near plane (distance from camera/eyes to screen)
        double far = 1000.0d;   //Far plane
        double fov = 90.0;     //Field of View
        double aspectRatio = (double)screenWidth/(double)screenHeight;  //Screen aspect ratio
        double fovRad = 1.0d/Math.tan( fov*0.5d/180.0d*Math.PI );  //modified fov value, in radians
        reset();
        mat[0][0] = aspectRatio*fovRad;
        mat[1][1] = fovRad;
        mat[2][2] = far/(far-near);
        mat[2][3] = 1.0d;
        mat[3][2] = (-far*near)/(far-near);
    }

    public void setToXRotationMatrix(double theta)
    {
        reset();
        mat[0][0] = 1;
        mat[1][1] = Math.cos(theta);
        mat[1][2] = Math.sin(theta);
        mat[2][1] = -Math.sin(theta);
        mat[2][2] = Math.cos(theta);
        mat[3][3] = 1;
    }

    public void setToYRotationMatrix(double theta)
    {
        reset();
        mat[0][0] = Math.cos(theta);
        mat[0][2] = Math.sin(theta);
        mat[1][1] = 1;
        mat[2][0] = -Math.sin(theta);
        mat[2][2] = Math.cos(theta);
        mat[3][3] = 1;
    }

    public void setToZRotationMatrix(double theta)
    {
        reset();
        mat[0][0] = Math.cos(theta);
        mat[0][1] = Math.sin(theta);
        mat[1][0] = -Math.sin(theta);
        mat[1][1] = Math.cos(theta);
        mat[2][2] = 1;
        mat[3][3] = 1;
    }

    public String toString()
    {
        StringBuilder res = new StringBuilder("{");

        for(int x = 0; x<4; x++) {
            res.append("[");
            for(int y = 0; y<4; y++) {
                res.append(mat[x][y]).append(",");
            }
            res.append("] ");
        }

        res.append("}");
        return res.toString();
    }

    /**
     * Multiply vector 'a' by the matrix 'b' (which should be the projection matrix).
     * Here, 'a' is treated as a 4D vector with w=1 and the original 3 coordinates (x,y,z) remaining the same.
     * In the result, each of these 3 coordinates are divided by the w that we obtain.
     * @return 3D point which has been transformed.
     */
    public static Point3d multiply3d(Point3d a, Matrix4d b)
    {
        Point3d res = new Point3d();

        res.x = a.x*b.get(0, 0) + a.y*b.get(1, 0) + a.z*b.get(2, 0) + b.get(3, 0);
        res.y = a.x*b.get(0, 1) + a.y*b.get(1, 1) + a.z*b.get(2, 1) + b.get(3, 1);
        res.z = a.x*b.get(0, 2) + a.y*b.get(1, 2) + a.z*b.get(2, 2) + b.get(3, 2);
        double w = a.x*b.get(0, 3) + a.y*b.get(1, 3) + a.z*b.get(2, 3) + b.get(3, 3);

        if( w!=0.0d ) {
            res.x /= w;
            res.y /= w;
            res.z /= w;
            return res;
        } else {
            if(!loggedWarning) {
                Main.log("Matrix4d.multiply3d() - w should never be zero!");
                loggedWarning = true;
            }
            res.x /= 100d;
            res.y /= 100d;
            res.z /= 100d;
            return res;
        }
    }
}
