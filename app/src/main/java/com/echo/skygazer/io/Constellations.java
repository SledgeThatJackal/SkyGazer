package com.echo.skygazer.io;

import static com.echo.skygazer.Main.getMainActivity;

import android.content.Intent;
import android.content.res.AssetManager;
import android.util.JsonReader;
import android.util.Log;

import com.echo.skygazer.MainActivity;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Constellations {
    private List<SpecificConstellation> constellations = new ArrayList<>();

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
                int constellationID = constellationInfo.getInt("constellationID");
                String constellationName = constellationInfo.getString("constellationName");
                String link = constellationInfo.getString("link");

                String[] strStars = link.split(" ");
                int[] stars = new int[strStars.length];
                for(int k = 0; k < strStars.length; k++) {
                    stars[k] = Integer.parseInt(strStars[k]);
                }

                constellations.add(new SpecificConstellation(constellationID, constellationName, stars));
            }
        } catch (IOException e) {
            Log.d("Constellation Json File", "Something went wrong.");
        } catch (JSONException e) {
            Log.d("Constellation Json File", e.getMessage());
        }

        Log.d("Constellation List", constellations.toString());
    }



    public String getConstellationName(int id){
        for(SpecificConstellation constellation: constellations){
            if(constellation.getConstellationID() == id){
                return constellation.getConstellationName();
            }
        }

        return "";
    }

    public int getConstellationId(String name){
        for(SpecificConstellation constellation: constellations){
            if(constellation.getConstellationName().equals(name)){
                return constellation.getConstellationID();
            }
        }

        return -1;
    }

    public int[] getConstellationStars(int id){
        for(SpecificConstellation constellation: constellations){
            if(constellation.getConstellationID() == id){
                return constellation.getLink();
            }
        }

        return null;
    }


}
