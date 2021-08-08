package com.example.zrenie20.location.arcorelocation.utils

import android.util.Log

/**
 * Created by John on 02/03/2018.
 */
object LocationUtils {
    /**
     * Bearing in degrees between two coordinates.
     * [0-360] Clockwise
     *
     * @param lat1
     * @param lon1
     * @param lat2
     * @param lon2
     * @return
     */
    fun bearing(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val latitude1 = Math.toRadians(lat1)
        val latitude2 = Math.toRadians(lat2)
        val longDiff = Math.toRadians(lon2 - lon1)
        val y = Math.sin(longDiff) * Math.cos(latitude2)
        val x =
            Math.cos(latitude1) * Math.sin(latitude2) - Math.sin(latitude1) * Math.cos(latitude2) * Math.cos(
                longDiff
            )
        return (Math.toDegrees(Math.atan2(y, x)) + 360) % 360
    }

    /**
     * Distance in metres between two coordinates
     *
     * @param lat1
     * @param lat2
     * @param lon1
     * @param lon2
     * @param el1  - Elevation 1
     * @param el2  - Elevation 2
     * @return
     */
    @JvmStatic
    fun distance(
        lat1: Double, lat2: Double, lon1: Double,
        lon2: Double, el1: Double, el2: Double
    ): Double {
        val R = 6371 // Radius of the earth
        val latDistance = Math.toRadians(lat2 - lat1)
        val lonDistance = Math.toRadians(lon2 - lon1)
        val a = (Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + (Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2)))
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        var distance = R * c * 1000 // convert to meters
        val height = el1 - el2
        distance = Math.pow(distance, 2.0) + Math.pow(height, 2.0)
        Log.e("LocationUtils", "distance : lat1 : ${lat1}, lat2 : ${lat2}, lon1 : ${lon1}, lon2 : ${lon2}")
        Log.e("LocationUtils", "distance : ${Math.sqrt((lat1 - lat2) * (lat1 - lat2) + (lon1 - lon2) * (lon1 - lon2))}")

        //return  Math.sqrt((lat1 - lat2) * (lat1 - lat2) + (lon1 - lon2) * (lon1 - lon2))
        return Math.sqrt(distance);
    }
}