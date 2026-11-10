package com.mchawe.mihrab

import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.HighLatitudeRule
import com.batoulapps.adhan.Madhab
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.data.DateComponents
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

/**
 * Worldwide offline prayer-time calculation.
 * Results are astronomical calculations, not official local timetables.
 */
object GlobalPrayerCalculator {
    enum class Method(val title: String, val calculationMethod: CalculationMethod) {
        MUSLIM_WORLD_LEAGUE("رابطة العالم الإسلامي", CalculationMethod.MUSLIM_WORLD_LEAGUE),
        EGYPTIAN("الهيئة المصرية العامة للمساحة", CalculationMethod.EGYPTIAN),
        KARACHI("جامعة العلوم الإسلامية - كراتشي", CalculationMethod.KARACHI),
        UMM_AL_QURA("أم القرى - مكة", CalculationMethod.UMM_AL_QURA),
        DUBAI("الإمارات - دبي", CalculationMethod.DUBAI),
        QATAR("قطر", CalculationMethod.QATAR),
        KUWAIT("الكويت", CalculationMethod.KUWAIT),
        SINGAPORE("سنغافورة", CalculationMethod.SINGAPORE),
        NORTH_AMERICA("أمريكا الشمالية (ISNA)", CalculationMethod.NORTH_AMERICA),
        TEHRAN("معهد الجيوفيزياء - طهران", CalculationMethod.TEHRAN),
        MOON_SIGHTING("لجنة رؤية الهلال", CalculationMethod.MOON_SIGHTING_COMMITTEE)
    }

    enum class AsrMethod(val title: String, val madhab: Madhab) {
        SHAFII("شافعي", Madhab.SHAFI),
        HANAFI("حنفي", Madhab.HANAFI)
    }

    data class Times(
        val fajr: String,
        val sunrise: String,
        val dhuhr: String,
        val asr: String,
        val maghrib: String,
        val isha: String
    )

    fun calculate(
        latitude: Double,
        longitude: Double,
        date: LocalDate,
        zoneId: String,
        method: Method = Method.MUSLIM_WORLD_LEAGUE,
        asrMethod: AsrMethod = AsrMethod.SHAFII
    ): Times? {
        if (!latitude.isFinite() || !longitude.isFinite() ||
            latitude !in -90.0..90.0 || longitude !in -180.0..180.0
        ) return null

        return try {
            val coordinates = Coordinates(latitude, longitude)
            val components = DateComponents(date.year, date.monthValue, date.dayOfMonth)
            val params = method.calculationMethod.parameters
            params.madhab = asrMethod.madhab
            params.highLatitudeRule = HighLatitudeRule.TWILIGHT_ANGLE
            val result = PrayerTimes(coordinates, components, params)
            val zone = ZoneId.of(zoneId)
            fun fmt(value: Date): String = value.toInstant().atZone(zone)
                .toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm", Locale.ROOT))
            Times(
                fmt(result.fajr), fmt(result.sunrise), fmt(result.dhuhr),
                fmt(result.asr), fmt(result.maghrib), fmt(result.isha)
            )
        } catch (_: Exception) {
            null
        }
    }
}
