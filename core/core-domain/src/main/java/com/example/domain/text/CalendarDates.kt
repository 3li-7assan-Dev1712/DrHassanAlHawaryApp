package com.example.domain.text

/** A Hijri date as announced in the source title - never computed, so it always
 * matches what the institute actually published (moon-sighting can differ from
 * any calendar algorithm by a day). [monthName] is the canonical Arabic name. */
data class HijriDate(
    val day: Int,
    val monthName: String,
    val year: Int,
)

/** A plain Gregorian calendar date. Not java.time.LocalDate: minSdk 24 without
 * core-library desugaring, and nothing here needs date arithmetic. */
data class GregorianDate(
    val year: Int,
    val month: Int,
    val day: Int,
)
