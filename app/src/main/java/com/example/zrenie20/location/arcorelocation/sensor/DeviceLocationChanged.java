package com.example.zrenie20.location.arcorelocation.sensor;

import android.location.Location;

/**
 * Created by johnwedgbury on 01/06/2018.
 */


public interface DeviceLocationChanged {
    void onChange(Location location);
    void simpleChange(Location location);
}
