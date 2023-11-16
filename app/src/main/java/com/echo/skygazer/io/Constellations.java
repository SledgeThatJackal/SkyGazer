package com.echo.skygazer.io;

import static com.echo.skygazer.Main.getMainActivity;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Constellations {
    private HashMap<Integer, SpecificConstellation> constellations = new HashMap<>();

    public Constellations() {
        try {
            // Grab JSON file
            InputStream inputStream = getMainActivity().getAssets().open("constellations.json");
            int size = inputStream.available();

            // Read the JSON file in the InputStream, put it into a byte array, and save it as a string
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            String json = new String(buffer);

            // Turn the String into a JSON object
            JSONObject jsonObject = new JSONObject(json);

            // Grab the Array within the JSON object
            JSONArray constellationArray = jsonObject.getJSONArray("constellations");

            for(int i = 0; i < constellationArray.length(); i++){
                JSONObject constellationInfo = constellationArray.getJSONObject(i);

                // Gather information
                int constellationId = constellationInfo.getInt("constellationID");
                String constellationName = constellationInfo.getString("constellationName");
                String link = constellationInfo.getString("link");

                // Break up link String and convert all of the strings into integers
                // This is to make it easier to highlight the constellations in the AR portion
                String[] strStars = link.split(" ");
                int[] stars = new int[strStars.length];
                for(int k = 0; k < strStars.length; k++) {
                    stars[k] = Integer.parseInt(strStars[k]);
                }

                // Put each Constellation object into a HashMap
                constellations.put(constellationId, new SpecificConstellation(constellationId, constellationName, stars));
            }
        } catch (IOException e) {
            Log.d("JSON- IOException", Objects.requireNonNull(e.getMessage()));
        } catch (JSONException e) {
            Log.d("JSON- JSONException", Objects.requireNonNull(e.getMessage()));
        }
    }

    public HashMap<Integer, SpecificConstellation> getConstellations() {
        return constellations;
    }

    // NOTE: The return type on this is a List because there are some Constellations that have duplicate stars
    public List<SpecificConstellation> getConstellation(int starId){
        // An ArrayList that will contain all of the constellation(s) that contain a specific star
        List<SpecificConstellation> starConstellation = new ArrayList<>();

        // Loops through constellation dictionary
        for(Map.Entry<Integer, SpecificConstellation> currEntry: constellations.entrySet()){

            // Loop through link array and compare the provided argument to the stars in the constellation
            int[] link = currEntry.getValue().getLink();
            for(int i = 0; i < link.length; i++){
                if(link[i] == starId){
                    starConstellation.add(currEntry.getValue());
                }
            }
        }

        return starConstellation;
    }
}
