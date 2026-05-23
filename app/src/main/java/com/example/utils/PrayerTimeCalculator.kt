package com.example.utils

import java.util.Calendar
import kotlin.math.*

object PrayerTimeCalculator {

    data class PrayerTimes(
        val fajr: String,
        val sunrise: String,
        val dhuhr: String,
        val asr: String,
        val maghrib: String,
        val isha: String
    )

    enum class AsrSchool(val shadowFactor: Double) {
        STANDARD(1.0), // Shafi'i, Maliki, Hanbali
        HANAFI(2.0)    // Hanafi
    }

    private fun degreesToRadians(deg: Double): Double = deg * PI / 180.0
    private fun radiansToDegrees(rad: Double): Double = rad * 180.0 / PI

    fun calculate(
        latitude: Double,
        longitude: Double,
        timezoneOffset: Double, // local timezone offset in hours, e.g. -5.0 for EST, 5.5 for IST
        calendar: Calendar,
        asrSchool: AsrSchool = AsrSchool.STANDARD,
        fajrAngle: Double = 18.0, // MWL is 18.0, ISNA is 15.0
        ishaAngle: Double = 17.0  // MWL is 17.0, ISNA is 15.0
    ): PrayerTimes {
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        
        // Approximate fractional year in radians (Gamma)
        val gamma = 2.0 * PI / 365.0 * (dayOfYear - 1)

        // Equation of Time (EqT) in minutes - solar transit deviation
        val eqTime = 229.18 * (0.000075 + 
                0.001868 * cos(gamma) - 
                0.032077 * sin(gamma) - 
                0.014615 * cos(2.0 * gamma) - 
                0.040849 * sin(2.0 * gamma))

        // Solar Declination in radians (decl) - tilt of Earth relative to rays
        val decl = 0.006918 - 
                0.399912 * cos(gamma) + 
                0.070257 * sin(gamma) - 
                0.006758 * cos(2.0 * gamma) + 
                0.0009 * sin(2.0 * gamma) - 
                0.002697 * cos(3.0 * gamma) + 
                0.00148 * sin(3.0 * gamma)

        // Latitude in radians
        val latRad = degreesToRadians(latitude)

        // Solar Noon/Transit (Dhuhr in minutes from UT midnight)
        val transitUTC = 720.0 - 4.0 * longitude - eqTime
        // Convert to local time in minutes
        val localTransitMinutes = (transitUTC + timezoneOffset * 60.0 + 1440.0) % 1440.0

        // Function to calculate Solar Hour Angle (H) for a given altitude/depression angle
        fun hourAngle(angleDegrees: Double): Double? {
            val angleRad = degreesToRadians(angleDegrees)
            val denom = cos(latRad) * cos(decl)
            if (denom == 0.0) return null
            val cosH = (sin(angleRad) - sin(latRad) * sin(decl)) / denom
            if (cosH < -1.0 || cosH > 1.0) return null
            return radiansToDegrees(acos(cosH))
        }

        // Sunrise and Sunset (constant depression = -0.833° for refraction/solar disk)
        val hSunrise = hourAngle(-0.833) ?: 90.0
        val sunriseMinutes = localTransitMinutes - hSunrise * 4.0
        val sunsetMinutes = localTransitMinutes + hSunrise * 4.0

        // Fajr (depression = -fajrAngle)
        val hFajr = hourAngle(-fajrAngle) ?: 60.0
        val fajrMinutes = localTransitMinutes - hFajr * 4.0

        // Isha (depression = -ishaAngle)
        val hIsha = hourAngle(-ishaAngle) ?: 60.0
        val ishaMinutes = localTransitMinutes + hIsha * 4.0

        // Asr Angle Calculation:
        // angle = arccot(ShadowFactor + tan(lat - decl))
        // Shafi'i shadowFactor = 1.0, Hanafi = 2.0
        val angleDiff = abs(latRad - decl)
        val tanDiff = tan(angleDiff)
        val asrAngleRad = atan(1.0 / (asrSchool.shadowFactor + tanDiff))
        val cosHAsr = (sin(asrAngleRad) - sin(latRad) * sin(decl)) / (cos(latRad) * cos(decl))
        val hAsr = if (cosHAsr in -1.0..1.0) radiansToDegrees(acos(cosHAsr)) else 50.0
        val asrMinutes = localTransitMinutes + hAsr * 4.0

        // Maghrib is identical to Sunset
        val maghribMinutes = sunsetMinutes

        return PrayerTimes(
            fajr = formatMinutes(fajrMinutes),
            sunrise = formatMinutes(sunriseMinutes),
            dhuhr = formatMinutes(localTransitMinutes),
            asr = formatMinutes(asrMinutes),
            maghrib = formatMinutes(maghribMinutes),
            isha = formatMinutes(ishaMinutes)
        )
    }

    private fun formatMinutes(minutesInput: Double): String {
        val totalMinutes = (minutesInput.roundToInt() + 1440) % 1440
        val hrs = totalMinutes / 60
        val mins = totalMinutes % 60
        return String.format("%02d:%02d", hrs, mins)
    }
}
