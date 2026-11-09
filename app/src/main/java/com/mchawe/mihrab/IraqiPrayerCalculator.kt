package com.mchawe.mihrab

import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.DateComponents
import com.batoulapps.adhan.Madhab
import com.batoulapps.adhan.PrayerTimes
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

/**
 * Offline astronomical estimates for Iraqi governorate centres.
 * These are NOT official Diwan timetables. Verify locally before relying on them.
 * Fajr 18°, Isha 17° (MWL parameters adjusted for the Iraqi Sunni reference method).
 */
object IraqiPrayerCalculator {
    data class City(val name: String, val latitude: Double, val longitude: Double)
    data class Times(
        val fajr: String, val sunrise: String, val dhuhr: String,
        val asr: String, val maghrib: String, val isha: String
    )

    val cities = listOf(
        City("بغداد", 33.3152, 44.3661),
        City("البصرة", 30.5085, 47.7804),
        City("نينوى - الموصل", 36.3350, 43.1189),
        City("أربيل", 36.1901, 44.0091),
        City("السليمانية", 35.5613, 45.4309),
        City("دهوك", 36.8671, 42.9507),
        City("كركوك", 35.4681, 44.3922),
        City("النجف", 32.0000, 44.3350),
        City("كربلاء", 32.6160, 44.0249),
        City("الأنبار - الرمادي", 33.4230, 43.3070),
        City("بابل - الحلة", 32.4890, 44.4300),
        City("واسط - الكوت", 32.5128, 45.8182),
        City("ذي قار - الناصرية", 31.0570, 46.2570),
        City("ميسان - العمارة", 31.8350, 47.1440),
        City("المثنى - السماوة", 31.3180, 45.2800),
        City("صلاح الدين - تكريت", 34.6158, 43.6786),
        City("القادسية - الديوانية", 31.9920, 44.9250),
        City("ديالى - بعقوبة", 33.7485, 44.6436),
        City("حلبجة", 35.1770, 45.9861)
    )

    private val formatter = DateTimeFormatter.ofPattern("HH:mm", Locale.ROOT)

    fun calculate(cityName: String, date: LocalDate = LocalDate.now()): Times? {
        val city = cities.firstOrNull { it.name == cityName } ?: return null
        val coordinates = Coordinates(city.latitude, city.longitude)
        val components = DateComponents(date.year, date.monthValue, date.dayOfMonth)
        val params = CalculationMethod.MUSLIM_WORLD_LEAGUE.parameters
        params.fajrAngle = 18.0
        params.ishaAngle = 17.0
        params.madhab = Madhab.HANAFI
        val result = PrayerTimes(coordinates, components, params)
        fun fmt(value: Date): String = value.toInstant()
            .atZone(ZoneId.of("Asia/Baghdad")).toLocalTime().format(formatter)
        return Times(
            fmt(result.fajr), fmt(result.sunrise), fmt(result.dhuhr),
            fmt(result.asr), fmt(result.maghrib), fmt(result.isha)
        )
    }
}