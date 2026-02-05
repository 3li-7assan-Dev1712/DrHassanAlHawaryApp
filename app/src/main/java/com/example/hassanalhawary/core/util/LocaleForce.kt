package com.example.hassanalhawary.core.util


import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

object LocaleForce {

    private val ARABIC = Locale("ar")

    fun wrap(context: Context): Context {
        Locale.setDefault(ARABIC)

        val config = Configuration(context.resources.configuration)

        config.setLocale(ARABIC)
        config.setLayoutDirection(ARABIC)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            context
        }
    }
}
