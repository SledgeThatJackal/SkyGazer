package com.echo.skygazer.io;

import androidx.annotation.NonNull;

public class HygDataRow
{
    int id = 0;
    /**
     * Col 7: Common name for a star. Examples: "Barnard's Star", "Sirius", "Betelgeuse").
     * Note that >99% of stars do not have a common name. In these cases the 'properName' will be null.
     */
    String properName = null;
    double rightAscension;  //Col 08: Referring to a coordinate, in angles (radius? degrees? IDK yet)
    double declination;     //Col 09: Referring to a coordinate, in angles (radius? degrees? IDK yet)
    //More useful fields: (relative magnitutde, luminosity, etc etc)

    /**
     * For sprint 2 we only need the proper name really
     * @param properName
     */
    public HygDataRow(int id, String properName) {
        this.id = id;
        this.properName = properName;
    }

    public int getId() { return id; }
    public String getProperName() { return properName; }

    @NonNull
    @Override
    public String toString() {
        return "Star{ properName="+properName+" }";
    }
}
