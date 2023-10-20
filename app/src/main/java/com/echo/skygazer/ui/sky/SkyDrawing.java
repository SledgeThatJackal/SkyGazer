package com.echo.skygazer.ui.sky;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.SurfaceView;
import android.view.View;
import android.widget.RelativeLayout;

import com.echo.skygazer.Main;

public class SkyDrawing extends SurfaceView implements Runnable
{
    private Thread thread;
    private boolean running = false;
    Paint paint = new Paint();
    int timer = 0;


    public SkyDrawing(Context context) {
        super(context);
        setWillNotDraw(false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        paint.setColor(Color.GREEN);
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);

        paint.setColor(Color.rgb(21, 22, 48));
        int margin = timer*8;
        if(margin>=3000) {
            timer = 0;
        }
        canvas.drawRect(0+margin, 0+margin, getWidth()-margin, getHeight()-margin, paint);

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