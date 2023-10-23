package com.echo.skygazer.gfx;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.SurfaceView;

import com.echo.skygazer.gfx.skyobj.SkyDot;
import com.echo.skygazer.gfx.skyobj.SkyLine;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class SkyDrawing extends SurfaceView implements Runnable
{
    private Thread thread;
    private boolean running = false;

    /**
     * This is a (key, value) list.
     * The key represents the ID of the skyObject (arbitrary for now, but later it will likely represent the row # in the star database).
     * The value represents the skyObject itself.
     * Using a map allows us to find any particular star, given its ID, in constant time.
     */
    Map<Integer, SkyObject> skyObjects = new HashMap<Integer, SkyObject>();

    Paint paint = new Paint();
    float timer = 0;

    public SkyDrawing(Context context) {
        super(context);
        setWillNotDraw(false);

        for(int i = 0; i<4; i++) {
            addSkyObject(new SkyDot());
        }

        addSkyObject( new SkyLine(getSkyDot(0), getSkyDot(1)) );
        addSkyObject( new SkyLine(getSkyDot(1), getSkyDot(2)) );
        addSkyObject( new SkyLine(getSkyDot(2), getSkyDot(3)) );
        addSkyObject( new SkyLine(getSkyDot(3), getSkyDot(1)) );
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
            SkyObject so = skyObjects.get(i);
            if(so!=null) {
                so.draw(canvas, paint);
            }
        }

        //Crosshair
        float chRadius = 24;
        float chWidth = 3;
        paint.setColor(Color.WHITE);
        canvas.drawRect(width/2-chRadius, height/2-chWidth, width/2+chRadius, height/2+chWidth, paint);
        canvas.drawRect(width/2-chWidth, height/2-chRadius, width/2+chWidth, height/2+chRadius, paint);

        //Move stars
        getSkyDot(0).setScreenXY(100+timer/5, 30+timer/2);
        getSkyDot(1).setScreenXY(123+timer/3, 300+timer/4);
        getSkyDot(2).setScreenXY(456+timer/4, 200+timer/2);
        getSkyDot(3).setScreenXY(789-timer/2, 500+timer*2);

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

    /**
     * Adds the SkyObject 'so' to the hashmap skyObjects, given a key 'id' and a value 'sd'.
     * @param id The ID of the object to add. This can really be anything as long as it isn't a previously used ID.
     * @param so The SkyObject to add to 'skyObjects'.
     * @return -1 if adding object was unsuccessful. 0 if adding object was successful.
     */
    public int addSkyObject(int id, SkyObject so) {
        if(!skyObjects.containsKey(id)) {
            skyObjects.put(id, so);
            return 0;
        }
        return -1;
    }

    /**
     * Same as addSkyObject(int, SkyObject), but automatically assigns the ID (+1 of max). Will always be successful as there are 2^32 possible IDs.
     * @return The new ID that was added.
     */
    public int addSkyObject(SkyObject so) {
        //Try to set newKey to max key ID + 1.
        int newKey = 0;
        try {
            newKey = Collections.max(skyObjects.keySet()) + 1;
        //If key set is empty, use default ID of 0.
        } catch (NoSuchElementException ignored) {}

        //Add the object
        addSkyObject(newKey, so);

        //Return newKey.
        return newKey;
    }

    /**
     *
     * No value of any element in skyObjects should ever be null. If this returns null, it means the value (SkyObject object) for key 'id' doesn't exist.
     * @param id The ID to look up in the 'skyObjects' HashMap.
     * @return The SkyObject object that has the given id. Returns null if the id doesn't exist.
     */
    public SkyObject getSkyObject(int id) {
        if(skyObjects.containsKey(id)) {
            return skyObjects.get(id);
        }
        return null;
    }

    /**
     * Same as getSkyObject, but only for SkyDots.
     */
    public SkyDot getSkyDot(int id) {
        SkyObject so = getSkyObject(id);
        if( so instanceof SkyDot ) {
            return (SkyDot)so;
        }
        return null;
    }
}