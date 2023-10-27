package com.echo.skygazer.gfx;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.view.SurfaceView;

import com.echo.skygazer.Main;
import com.echo.skygazer.gfx.skyobj.SkyDot;
import com.echo.skygazer.gfx.skyobj.SkyLine;
import com.echo.skygazer.io.HygDatabase;
import com.echo.skygazer.io.WebResource;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class SkyView extends SurfaceView implements Runnable
{
    private Thread thread;
    private boolean running = false;
    private boolean showingWindow = false;
    private boolean showingPreviewTab = false;
    private float width = 0;
    private float height = 0;
    private float tx = 100; //Translate all objects' x-coords by this much
    private float ty = 500; //Translate all objects' y-coords by this much
    private int selectedSkyDotId = -123456789;

    /**
     * This is a (key, value) list.
     * The key represents the ID of the skyObject (arbitrary for now, but later it will likely represent the row # in the star database).
     * The value represents the skyObject itself.
     * Using a map allows us to find any particular star, given its ID, in constant time.
     */
    Map<Integer, SkyObject> skyObjects = new HashMap<Integer, SkyObject>();

    Paint paint = new Paint();
    float timer = 0;

    public SkyView(Context context) {
        super(context);
        setWillNotDraw(false);

        for(int i = 0; i<8; i++) {
            addSkyObject(new SkyDot());
        }
        getSkyDot(0).setScreenXY(440, 0);
        getSkyDot(1).setScreenXY(140, 40);
        getSkyDot(2).setScreenXY(30, 100);
        getSkyDot(3).setScreenXY(120, 250);
        getSkyDot(4).setScreenXY(320, 130);
        getSkyDot(5).setScreenXY(400, 400);
        getSkyDot(6).setScreenXY(700, 420);
        getSkyDot(7).setScreenXY(100, 640);


        addSkyObject( new SkyLine(getSkyDot(0), getSkyDot(1)) );
        addSkyObject( new SkyLine(getSkyDot(1), getSkyDot(2)) );
        addSkyObject( new SkyLine(getSkyDot(2), getSkyDot(3)) );
        addSkyObject( new SkyLine(getSkyDot(3), getSkyDot(1)) );
        addSkyObject( new SkyLine(getSkyDot(7), getSkyDot(6)) );
        addSkyObject( new SkyLine(getSkyDot(5), getSkyDot(6)) );
        addSkyObject( new SkyLine(getSkyDot(4), getSkyDot(5)) );
        addSkyObject( new SkyLine(getSkyDot(7), getSkyDot(3)) );
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        width = getWidth();
        height = getHeight();

        //Background
        paint.setColor(Color.rgb(21, 22, 48));
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);

        //SkyObject list
        for(int i = 0; i<skyObjects.size(); i++) {
            SkyObject so = skyObjects.get(i);
            if(so!=null) {
                so.draw(canvas, paint, tx, ty);
            }
        }

        //Crosshair
        float chRadius = 24;
        float chWidth = 3;
        paint.setColor(Color.WHITE);
        canvas.drawRect(width/2-chRadius, height/2-chWidth, width/2+chRadius, height/2+chWidth, paint);
        canvas.drawRect(width/2-chWidth, height/2-chRadius, width/2+chWidth, height/2+chRadius, paint);

        //Window
        if(showingWindow) {
            HygDatabase.drawDataWindow(this, canvas, paint, width, height);
        }

        //Star preview tab
        if(showingPreviewTab) {
            HygDatabase.drawDataPreviewTab(this, canvas, paint, width, height);
        }

        timer++;
    }

    public void doTapAt(float tapX, float tapY) {
        //Go through all objects and see if it is being tapped

        //If we clicked on a preview tab
        if( showingPreviewTab ) {
            boolean tappedPreview =
                tapX>=width/2-HygDatabase.ptWidth/2 && tapX<=width/2+HygDatabase.ptWidth/2 &&
                tapY>=height-HygDatabase.navbarHeight && tapY<=height-HygDatabase.navbarHeight+HygDatabase.ptHeight;
            if(tappedPreview) {
                String starName = getSkyDot(selectedSkyDotId).getDisplayName();
                HygDatabase.selectRow(starName);
                WebResource wr = new WebResource("https://en.wikipedia.org/wiki/"+starName, "wiki/"+starName+".html",1234);
                showWindow();
                return;
            } else {
                showingPreviewTab = false;
            }
        }

        //If we clicked OUTSIDE a window (outside + close "button")
        if( showingWindow ) {
            boolean tappedWindow =
                tapX>=HygDatabase.wMargin && tapX<=width-HygDatabase.wMargin &&
                tapY>=HygDatabase.wMargin && tapY<=height-HygDatabase.wMargin*2-140;
            if(!tappedWindow) {
                showingWindow = false;
            }
        }

        //Also, we need to find the closest star being tapped in case the tap is within the radius of multiple stars
        boolean foundSkyDot = false;
        int closestSkyDotID = -123456789;   //Will be unchanged if foundSkyDot stays false
        double minimumDistance = 999999;    //Will be unchanged if foundSkyDot stays false
        float detectionRadius = 60;
        for( Map.Entry<Integer, SkyObject> entry : skyObjects.entrySet() ) {
            //Get the SkyDot object
            SkyDot sd = getSkyDot(entry.getKey());
            //If this is a SkyDot object, detect if it was just tapped
            if(sd!=null) {
                //Object coordinates
                float sdx = sd.getScreenX()+tx;
                float sdy = sd.getScreenY()+ty;

                //Find distance between tap and star
                double dist = Math.hypot(sdx-(tapX), sdy-(tapY));
                //Is it within the 'detectionRadius'? If so, we touched the star.
                if( dist<detectionRadius ) {
                    foundSkyDot = true;
                    //Find closest sky dot by tracking and setting minimumDistance
                    if(dist<minimumDistance) {
                        minimumDistance = dist;
                        closestSkyDotID = entry.getKey();
                    }
                }
            }
        }

        //If we found SkyDot(s)
        if(!showingPreviewTab && !showingWindow && foundSkyDot) {
            selectedSkyDotId = closestSkyDotID;
            showPreviewTab();
        }
    }

    public void doDragAt(float dragX, float dragY) {
        tx += dragX;
        ty += dragY;
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

    public SkyDot getSelectedSkyDot() {
        return getSkyDot(selectedSkyDotId);
    }

    private void showWindow() {
        showingWindow = true;
        showingPreviewTab = false;
    }

    private void showPreviewTab() {
        showingWindow = false;
        showingPreviewTab = true;
    }
}