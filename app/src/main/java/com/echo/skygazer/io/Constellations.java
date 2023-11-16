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
            InputStream inputStream = getMainActivity().getAssets().open("constellations.json");
            int size = inputStream.available();

            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            String json = new String(buffer);

            JSONObject jsonObject = new JSONObject(json);
            JSONArray constellationArray = jsonObject.getJSONArray("constellations");

            for(int i = 0; i < constellationArray.length(); i++){
                JSONObject constellationInfo = constellationArray.getJSONObject(i);

                int constellationId = constellationInfo.getInt("constellationID");
                String constellationName = constellationInfo.getString("constellationName");
                String links = constellationInfo.getString("links");

                // Break up link String and convert all of the strings into integers
                // This is to make it easier to highlight the constellations in the AR portion
                String[] strStars = links.split(" ");
                int[] stars = new int[strStars.length];
                for(int k = 0; k < strStars.length; k++) {
                    stars[k] = Integer.parseInt(strStars[k]);
                }

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
    public List<SpecificConstellation> getConstellationsFromStarId(int starId){
        List<SpecificConstellation> starConstellation = new ArrayList<>();

        for(Map.Entry<Integer, SpecificConstellation> currEntry: constellations.entrySet()){
            int[] links = currEntry.getValue().getLinks();
            for(int i = 0; i < links.length; i++){
                if(links[i] == starId){
                    starConstellation.add(currEntry.getValue());
                    break;
                }
            }
        }

        return starConstellation;
    }
}
