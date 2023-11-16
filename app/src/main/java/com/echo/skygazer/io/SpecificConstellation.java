package com.echo.skygazer.io;

import java.util.Arrays;
import java.util.List;

public class SpecificConstellation {
    private int constellationID;
    private String constellationName;
    private int[] link;

    public SpecificConstellation(int constellationID, String constellationName, int[] link) {
        this.constellationID = constellationID;
        this.constellationName = constellationName;
        this.link = link;
    }

    public int getConstellationID() {
        return constellationID;
    }

    public String getConstellationName() {
        return constellationName;
    }

    public int[] getLink() {
        return link;
    }

    @Override
    public String toString() {
        return "SpecificConstellation{" +
                "constellationID=" + constellationID +
                ", constellationName='" + constellationName + '\'' +
                ", link=" + Arrays.toString(link) +
                '}';
    }
}
