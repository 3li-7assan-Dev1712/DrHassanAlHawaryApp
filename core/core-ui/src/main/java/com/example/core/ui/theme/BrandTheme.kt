package com.example.core.ui.theme

import androidx.compose.runtime.compositionLocalOf

/**
 * The app's single brand identity (brown/cream palette). Kept as an enum, rather
 * than collapsed away, so the persisted-preference plumbing and [LocalBrandTheme]
 * call sites don't need to change if a second brand is ever reintroduced.
 */
enum class BrandTheme {
    BROWN;

    companion object {
        fun fromStorageValue(value: String?): BrandTheme =
            entries.find { it.name == value } ?: BROWN
    }
}

val LocalBrandTheme = compositionLocalOf { BrandTheme.BROWN }
