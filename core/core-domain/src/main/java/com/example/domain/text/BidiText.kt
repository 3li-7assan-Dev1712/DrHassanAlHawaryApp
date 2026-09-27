package com.example.domain.text

/**
 * Left-to-right fragments (handles, emails, URLs) inside Arabic UI text. Without an
 * isolate the bidi algorithm moves neutral characters to the wrong side, so a handle
 * shows as "ali_7assan@". Wrapped in LRI…PDI (U+2066…U+2069), it stays "@ali_7assan".
 * Never converts digits: this is user data.
 */
object BidiText {
    const val LRI = '⁦'
    const val PDI = '⁩'

    /** [text] inside an LTR isolate; "" stays "". */
    fun ltr(text: String): String = if (text.isEmpty()) text else "$LRI$text$PDI"

    /** "@handle" in an LTR isolate, whether or not [raw] already starts with "@"; "" if blank. */
    fun handle(raw: String): String {
        val name = raw.trim().removePrefix("@").trim()
        return if (name.isEmpty()) "" else ltr("@$name")
    }
}
