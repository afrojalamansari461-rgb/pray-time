package com.example.utils

import kotlin.math.*

object QiblaCalculator {
    private const val KAABA_LAT = 21.422487
    private const val KAABA_LON = 39.826206

    /**
     * Calculates the Qibla bearing (in degrees relative to True North)
     * for a given latitude and longitude.
     */
    fun calculateBearing(latitude: Double, longitude: Double): Double {
        val latRad = Math.toRadians(latitude)
        val lonRad = Math.toRadians(longitude)
        val kaabaLatRad = Math.toRadians(KAABA_LAT)
        val kaabaLonRad = Math.toRadians(KAABA_LON)

        val dLon = kaabaLonRad - lonRad

        val y = sin(dLon)
        val x = cos(latRad) * tan(kaabaLatRad) - sin(latRad) * cos(dLon)

        val qiblaRad = atan2(y, x)
        return (Math.toDegrees(qiblaRad) + 360.0) % 360.0
    }
}
