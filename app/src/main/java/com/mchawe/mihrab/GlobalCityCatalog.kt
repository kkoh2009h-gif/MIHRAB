package com.mchawe.mihrab

import android.content.Context
import java.util.Locale

/**
 * Offline city catalogue generated from GeoNames cities15000.txt.
 * GeoNames data is licensed under CC BY 4.0; see GEONAMES-LICENSE.txt.
 */
object GlobalCityCatalog {
    data class City(
        val name: String,
        val countryCode: String,
        val latitude: Double,
        val longitude: Double,
        val timeZone: String
    ) {
        val country: String
            get() = Locale("", countryCode).getDisplayCountry(Locale("ar"))
                .ifBlank { countryCode }
        val label: String get() = "$name، $country"
    }

    @Volatile private var cache: List<City>? = null

    fun load(context: Context): List<City> {
        cache?.let { return it }
        val parsed = context.assets.open("cities15000.txt").bufferedReader(Charsets.UTF_8).useLines { lines ->
            lines.mapNotNull { line ->
                val fields = line.split('\t')
                if (fields.size < 18) return@mapNotNull null
                val latitude = fields[4].toDoubleOrNull() ?: return@mapNotNull null
                val longitude = fields[5].toDoubleOrNull() ?: return@mapNotNull null
                val code = fields[8].takeIf { it.isNotBlank() } ?: return@mapNotNull null
                val zone = fields[17].takeIf { it.isNotBlank() } ?: return@mapNotNull null
                City(fields[1], code, latitude, longitude, zone)
            }.toList()
        }
        cache = parsed
        return parsed
    }

    fun search(context: Context, query: String, limit: Int = 100): List<City> {
        val term = query.trim()
        if (term.isEmpty()) return emptyList()
        return load(context).asSequence()
            .filter { it.name.contains(term, ignoreCase = true) ||
                it.country.contains(term, ignoreCase = true) ||
                it.countryCode.equals(term, ignoreCase = true) }
            .sortedByDescending { it.name.equals(term, ignoreCase = true) }
            .take(limit)
            .toList()
    }
}
