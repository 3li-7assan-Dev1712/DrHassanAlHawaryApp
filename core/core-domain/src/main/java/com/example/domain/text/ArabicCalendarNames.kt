package com.example.domain.text

/** Canonical Arabic names used everywhere a date is shown, so every screen spells them the same. */
object ArabicCalendarNames {

    /** Index 0 = محرم ... 11 = ذو الحجة. Same spellings [ShareTitleParser] normalises titles to. */
    val hijriMonths = listOf(
        "محرم", "صفر", "ربيع الأول", "ربيع الآخر", "جمادى الأولى", "جمادى الآخرة",
        "رجب", "شعبان", "رمضان", "شوال", "ذو القعدة", "ذو الحجة",
    )

    /** Index 0 = Sunday (java.util.Calendar.SUNDAY - 1) ... 6 = Saturday. */
    val weekdays = listOf("الأحد", "الاثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت")
}
