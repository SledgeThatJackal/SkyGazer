package com.echo.skygazer.io;

import java.util.Arrays;
import java.util.List;

public class SpecificConstellation {
    private final int CONSTELLATION_ID;
    private final String CONSTELLATION_NAME;
    private final int[] LINKS;

    public SpecificConstellation(int CONSTELLATION_ID, String CONSTELLATION_NAME, int[] LINKS) {
        this.CONSTELLATION_ID = CONSTELLATION_ID;
        this.CONSTELLATION_NAME = CONSTELLATION_NAME;
        this.LINKS = LINKS;
    }

    public int getConstellationID() {
        return CONSTELLATION_ID;
    }

    public String getConstellationName() {
        return CONSTELLATION_NAME;
    }

    public int[] getLinks() {
        return LINKS;
    }
}
