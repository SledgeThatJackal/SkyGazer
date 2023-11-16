package com.echo.skygazer.gfx;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.SurfaceView;
import android.widget.TextView;

import com.echo.skygazer.R;
import com.echo.skygazer.gfx.skyobj.SkyDot;
import com.echo.skygazer.gfx.skyobj.SkyLine;
import com.echo.skygazer.io.WebResource;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class SkySimulation extends SurfaceView implements Runnable
{
    private Thread thread;
    private boolean running = false;
    private boolean showingWindow = false;
    private boolean showingPreviewTab = false;
    private float width = 0;
    private float height = 0;
    private int selectedSkyDotId = -123456789;

    private static SkyView3D skyView;
    private BottomSheetDialog bottomSheetDialog;

    /**
     * This is a (key, value) list.
     * The key represents the ID of the skyObject (arbitrary for now, but later it will likely represent the row # in the star database).
     * The value represents the skyObject itself.
     * Using a map allows us to find any particular star, given its ID, in constant time.
     */
    Map<Integer, SkyObject> skyObjects = new HashMap<Integer, SkyObject>();

    Paint paint = new Paint();
    float timer = 0;

    public SkySimulation(Context context, BottomSheetDialog bottomSheetDialog) {
        super(context);
        this.bottomSheetDialog = bottomSheetDialog;
        setWillNotDraw(false);

        skyView = new SkyView3D(getWidth(), getHeight());

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

        //3d Sky view
        skyView.setWH((int)width, (int)height);
        skyView.draw(canvas, paint, skyObjects);

        TextView wiki = bottomSheetDialog.findViewById(R.id.bottomSheetWikiText);
        if(wiki!=null) {
            SkyDot sd = getSelectedSkyDot();
            String basics = "Loading...\n\n";
            if(sd!=null) {
                basics = sd.toUserString();
            }

            String wikiText = basics+WebResource.getCurrentWikipediaText();
            wiki.setText(wikiText);
        }

        timer++;
    }

    public void doTapAt(float tapX, float tapY) {
        //We need to find the closest star being tapped in case the tap is within the radius of multiple stars
        boolean foundSkyDot = false;
        int closestSkyDotID = -123456789;   //Will be unchanged if foundSkyDot stays false
        double minimumDistance = 999999;    //Will be unchanged if foundSkyDot stays false
        float detectionRadius = 60;
        for( Map.Entry<Integer, SkyObject> entry : skyObjects.entrySet() ) {
            //Get the SkyDot object
            SkyDot sd = getSkyDot(entry.getKey());
            //If this is a SkyDot object, detect if it was just tapped
            if(sd!=null && !sd.hasNegativeDepth() ) {
                //Object coordinates
                float sdx = sd.getScreenX()+skyView.getTx();
                float sdy = sd.getScreenY()+skyView.getTy();

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
        if(foundSkyDot) {
            selectedSkyDotId = closestSkyDotID;
            showPreviewTab(getSkyDot(selectedSkyDotId).getDisplayName());
        }
    }

    public void doDragAt(float dragX, float dragY) {
        skyView.translate(dragX, dragY);
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

    private void showPreviewTab(String starName) {
        WebResource wr = new WebResource("https://en.wikipedia.org/wiki/"+starName, "wiki/"+starName+".html",1234);

        TextView header = bottomSheetDialog.findViewById(R.id.bottomSheetHeader);
        String name = getSkyDot(selectedSkyDotId).getDisplayName();
        if(header!=null) {
            header.setText(name);
        }

        TextView dbID = bottomSheetDialog.findViewById(R.id.bottomSheetDatabaseId);
        if(dbID!=null) {
            String idText = "HYG database ID: "+selectedSkyDotId;
            dbID.setText(idText);
        }

        bottomSheetDialog.show();
    }
}