package com.echo.skygazer.gfx;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.SurfaceView;

import com.echo.skygazer.Main;
import com.echo.skygazer.gfx.skyobj.SkyDot;
import com.echo.skygazer.gfx.skyobj.SkyLine;

import java.util.ArrayList;

public class SkyDrawing extends SurfaceView implements Runnable
{
    private Thread thread;
    private boolean running = false;

    ArrayList<SkyObject> skyObjects = new ArrayList<>();

    Paint paint = new Paint();
    int timer = 0;

    public SkyDrawing(Context context) {
        super(context);
        setWillNotDraw(false);

        SkyDot sd1 = new SkyDot(100+timer/5, 30+timer/2);
        SkyDot sd2 = new SkyDot(123+timer/3, 300+timer/4);
        SkyDot sd3 = new SkyDot(456+timer/4, 200+timer/2);
        SkyDot sd4 = new SkyDot(789-timer/2, 500+timer*2);
        skyObjects.add(sd1);
        skyObjects.add(sd2);
        skyObjects.add(sd3);
        skyObjects.add(sd4);

        SkyLine sl1 = new SkyLine(sd1, sd2);
        SkyLine sl2 = new SkyLine(sd2, sd3);
        SkyLine sl3 = new SkyLine(sd3, sd4);
        SkyLine sl4 = new SkyLine(sd4, sd1);
        skyObjects.add(sl1);
        skyObjects.add(sl2);
        skyObjects.add(sl3);
        skyObjects.add(sl4);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float width = getWidth();
        float height = getHeight();

        //Background
        paint.setColor(Color.rgb(21, 22, 48));
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);

        //SkyObject list
        for(int i = 0; i<skyObjects.size(); i++) {
            skyObjects.get(i).draw(canvas, paint);
        }

        //Crosshair
        float chRadius = 24;
        float chWidth = 3;
        paint.setColor(Color.WHITE);
        canvas.drawRect(width/2-chRadius, height/2-chWidth, width/2+chRadius, height/2+chWidth, paint);
        canvas.drawRect(width/2-chWidth, height/2-chRadius, width/2+chWidth, height/2+chRadius, paint);

        timer++;
    }

    public void startDrawThread() {
        running = true;
        thread = new Thread(this);
        thread.start();
    }

    public void stopDrawThread() {
        running = false;
        try {
            thread.join();
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (running) {
            postInvalidateOnAnimation();
            try {
                Thread.sleep(16); //Sleep for 16 milliseconds -> 60FPS.
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
    }
}