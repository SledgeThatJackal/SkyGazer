package com.echo.skygazer.io;

import androidx.annotation.NonNull;

public class HygDataRow
{
    private int id = -1;
    /**
     * Col 7: Common name for a star. Examples: "Barnard's Star", "Sirius", "Betelgeuse").
     * Note that >99% of stars do not have a common name. In these cases the 'properName' will be null.
     */
    private String properName = null;

    /**
     * Col 8 ("ra"): Referring to right ascension, the horizontal "angular coordinate" within the celestial sphere, in radians.
     * For example: Polaris has ra=2.53 degrees, which is close to 0 since it is just a little to the east of the direction of the north pole.
     */
    //private double rightAscension = 0.0; //UNUSED

    /**
     * Col 9 ("dec"): Referring to declination, the vertical "angular coordinate" within the celestial sphere, in radians.
     * For example: Polaris has dec=89.26 degrees, which is close to 90 degrees. This means that if you were to stand at the north pole, Polaris would almost be straight above you.
     */
    //private double declination = 0.0; //UNUSED
    //More useful fields: (relative magnitutde, luminosity, etc etc)

    /**
     * Column 14: Apparent visual magnitude: How bright the star appears from Earth. In SkyGazer this determines the radius of the sky dot that makes up the star.
     */
    private double mag = 0.0;

    /**
     * Columns 18-20: X, Y, Z cartesian coordinates of the star relative to Earth.
     */
    private double x = 0.0;
    private double y = 0.0;
    private double z = 0.0;

    /**
     *
     */
    public HygDataRow(int id, String properName, double mag, double x, double y, double z) {
        this.id = id;
        this.properName = properName;
        //this.rightAscension = ra;
        //this.declination = dec;
        this.mag = mag;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getId() { return id; }
    public String getProperName() { return properName; }
    public double getMag() { return mag; }
    public double getX() { return x; };
    public double getY() { return y; };
    public double getZ() { return z; };

    @NonNull
    @Override
    public String toString() {
        return "Star{ "
            +"id="+id
            +", properName="+properName
            +", x="+x
            +", y="+y
            +", y="+z
            +" }";
    }
}
