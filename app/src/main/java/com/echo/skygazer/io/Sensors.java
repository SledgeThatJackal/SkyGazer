package com.echo.skygazer.io;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.Toast;

import com.echo.skygazer.Main;
import com.echo.skygazer.MainActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Sensors {
    /**
     * @param coord - 0=x, 1=y, 2=z. Don't put in any other value!
     * @return Orientation of the phone in degrees (azimuth, pitch, roll).
     */
    public static double getOrientation(int coord) {
        switch (coord) {
            case 0: {
                double val = MainActivity.getLocAzimuth();
                if(val<0) {
                    val += 360;
                }
                return val;
            }
            case 1: {
                double val = MainActivity.getLocPitch();
                return (val+180d)/2;
            }
            case 2: {
                return MainActivity.getLocRoll();
            }
        }

        return -9999d;
    }

    /**
     * @param returnLat - If =0, return latitude. If anything else, return longitude.
     * @return Latitude or longitude of the current location.
     */
    public static double getLocation(int returnLat) {
        double lat = MainActivity.getLocLatitude();
        double lon = MainActivity.getLocLongitude();

        //Warning if location has not been defined yet
        if( MainActivity.isLocationInvalid() ) {
            Context ctxt = Main.getMainActivity().getApplicationContext();
            Toast.makeText(ctxt, "Warning: Restart required to calibrate location.", Toast.LENGTH_SHORT).show();
        }

        //Return location component
        if( returnLat==0 ) {
            return lat;
        } else {
            return lon;
        }
    }

    @SuppressLint("SimpleDateFormat")
    public static String getCurrentTimeUTC(String format) {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return simpleDateFormat.format(date);

        //String date = getCurrentTimeUTC("hh:mm:ss a");
    }

    /**
     * @param time Any time with the format "hh,mm,ss a".
     * @return Number from 0.0 to 1.0 indicating the fraction of the day that has passed (12:00am-11:59pm).
     */
    public static double getDayFractionPassed(String time)
    {
        // Find clock - array of 3 numerical values representing hours/minutes/seconds
        String[] timeStrArr = time.substring(0,8).split(",");
        double[] clock = {-99, -99, -99};
        for(int i = 0; i<timeStrArr.length; i++) {
            try {
                clock[i] = Integer.parseInt(timeStrArr[i]);
            } catch (Exception ignored) {}
        }

        // Find whether it is currently PM or not
        boolean pm = time.substring(9).equals("PM");

        //Modify clock
        if(!pm && clock[0]==12) {
            clock[0] = 0;
        }
        if(pm && clock[0]!=12) {
            clock[0] += 12;
        }

        //Find portion
        return clock[0]/24d + clock[1]/(24d*60d) + clock[2]/(24d*3600d);
    }

    /**
     * Get the fraction of the day that has passed based on the current time.
     */
    public static double getDayFractionPassed() {
        String now = getCurrentTimeUTC("hh,mm,ss a");
        return getDayFractionPassed(now);
    }
}
