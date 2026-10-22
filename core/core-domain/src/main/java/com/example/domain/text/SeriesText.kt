package com.example.domain.text

import com.example.domain.module.Series
import com.example.domain.module.SeriesCollection

/**
 * A series' line on its card and header: "٥٩ درسًا · ٢٨ ساعة". Counts follow
 * [ArabicDates.count]; the line stands alone, so duals are nominative ("درسان").
 */
object SeriesText {

    private val LESSON = ArabicDates.Unit("درس واحد", "درسان", "دروس", many = "درسًا", other = "درس")
    private val ARTICLE = ArabicDates.Unit("مقال واحد", "مقالان", "مقالات", many = "مقالًا", other = "مقال")
    private val DESIGN = ArabicDates.Unit("تصميم واحد", "تصميمان", "تصاميم", many = "تصميمًا", other = "تصميم")
    private val HOUR = ArabicDates.Unit("ساعة واحدة", "ساعتان", "ساعات", many = "ساعة", other = "ساعة")
    private val MINUTE = ArabicDates.Unit("دقيقة واحدة", "دقيقتان", "دقائق", many = "دقيقة", other = "دقيقة")

    private const val MINUTE_MS = 60_000L

    /** "٥٩ درسًا · ٢٨ ساعة"; articles and designs have no duration: "١٢ مقالًا". */
    fun meta(series: Series): String =
        listOfNotNull(itemCount(series.itemCount, series.collection), duration(series.totalDurationMs))
            .joinToString(ArabicNumerals.DATE_SEPARATOR)

    /** "درس واحد", "درسان", "٣ دروس", "٥٩ درسًا"; videos count as lessons too. */
    fun itemCount(n: Long, collection: SeriesCollection): String = ArabicDates.count(
        n,
        when (collection) {
            SeriesCollection.AUDIOS, SeriesCollection.VIDEOS -> LESSON
            SeriesCollection.ARTICLES -> ARTICLE
            SeriesCollection.IMAGE_GROUPS -> DESIGN
        },
    )

    /**
     * Rounded to the minute under an hour ("٤٥ دقيقة"), else to the hour ("٢٨ ساعة"); null
     * for no duration.
     */
    fun duration(millis: Long): String? {
        if (millis <= 0L) return null
        val minutes = ((millis + MINUTE_MS / 2) / MINUTE_MS).coerceAtLeast(1L)
        return if (minutes < 60L) {
            ArabicDates.count(minutes, MINUTE)
        } else {
            ArabicDates.count((minutes + 30L) / 60L, HOUR)
        }
    }

    /** An episode's number before its row: "١", "٢٨". */
    fun episodeNumber(n: Long): String = ArabicNumerals.digits(n.toString())
}
