package com.test.murphy.weatherapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by wsmurphy on 10/31/17.
 */

public class LocationUtils {

    private static Location getLastBestLocation(Context context, Activity activity) {
        final int REQUEST_LOCATION = 0;

        LocationManager locationManager = (LocationManager)
                context.getSystemService(Context.LOCATION_SERVICE);
        if ( ContextCompat.checkSelfPermission( context, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions( activity, new String[] {  android.Manifest.permission.ACCESS_COARSE_LOCATION  },
                    REQUEST_LOCATION );
        }

        if ( ContextCompat.checkSelfPermission( context, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions( activity, new String[] {  android.Manifest.permission.ACCESS_FINE_LOCATION  },
                    REQUEST_LOCATION );
        }

        Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location locationNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        long GPSLocationTime = 0;
        if (null != locationGPS) { GPSLocationTime = locationGPS.getTime(); }

        long NetLocationTime = 0;

        if (null != locationNet) {
            NetLocationTime = locationNet.getTime();
        }

        if ( 0 < GPSLocationTime - NetLocationTime ) {
            return locationGPS;
        }
        else {
            return locationNet;
        }
    }

    private static String getZipFromLocation(Location location, Context context) {
        Geocoder gcd = new Geocoder(context, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = gcd.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses.size() > 0) {
                return addresses.get(0).getPostalCode();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    public static String resolveLocation(Context context, Activity activity) {
        //Resolve current location
        Location lastBest = getLastBestLocation(context, activity);
        if (lastBest != null) {
            String lastZip = getZipFromLocation(lastBest, context);
            return lastZip;
        }

        return  "";
    }
}
