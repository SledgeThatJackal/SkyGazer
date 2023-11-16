package com.echo.skygazer.io;

import java.util.Arrays;
import java.util.List;

public class SpecificConstellation {
    private final int CONSTELLATION_ID;
    private final String CONSTELLATION_NAME;
    private final int[] LINK;

    public SpecificConstellation(int CONSTELLATION_ID, String CONSTELLATION_NAME, int[] LINK) {
        this.CONSTELLATION_ID = CONSTELLATION_ID;
        this.CONSTELLATION_NAME = CONSTELLATION_NAME;
        this.LINK = LINK;
    }

    public int getConstellationID() {
        return CONSTELLATION_ID;
    }

    public String getConstellationName() {
        return CONSTELLATION_NAME;
    }

    public int[] getLink() {
        return LINK;
    }
}
