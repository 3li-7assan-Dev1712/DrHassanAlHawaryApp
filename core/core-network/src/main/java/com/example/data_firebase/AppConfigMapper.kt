package com.example.data_firebase

import com.example.domain.module.AppConfig

/**
 * Builds [AppConfig] from `config/app_config`'s fields one by one. The document is edited by
 * hand in the Firebase console, and `toObject` throws on a field of the wrong type (`"true"`
 * typed as text, a version code typed as text); inside the snapshot listener that crashed the
 * app on launch. Here a field that can't be read keeps its default and the others still apply,
 * so a bad maintenance switch reads as off and the app keeps working.
 *
 * [data] is null when the document doesn't exist: all defaults.
 */
internal fun appConfigFrom(data: Map<String, Any?>?): AppConfig {
    val defaults = AppConfig()
    if (data == null) return defaults
    return AppConfig(
        minVersionCode = data["minVersionCode"].asInt() ?: defaults.minVersionCode,
        latestVersionCode = data["latestVersionCode"].asInt() ?: defaults.latestVersionCode,
        forceUpdate = data["forceUpdate"].asBoolean() ?: defaults.forceUpdate,
        updateUrl = data["updateUrl"] as? String ?: defaults.updateUrl,
        maintenanceMode = data["maintenanceMode"].asBoolean() ?: defaults.maintenanceMode,
        maintenanceMessage = (data["maintenanceMessage"] as? String)?.trim() ?: defaults.maintenanceMessage,
    )
}

/** A whole number stored as a number or as its text ("12"); a fraction is cut, as toObject did. */
private fun Any?.asInt(): Int? = when (this) {
    is Number -> toLong().takeIf { it in Int.MIN_VALUE..Int.MAX_VALUE }?.toInt()
    is String -> trim().toIntOrNull()
    else -> null
}

/** A boolean, or the text "true" / "false" in any case. Anything else (1, "yes") isn't read. */
private fun Any?.asBoolean(): Boolean? = when (this) {
    is Boolean -> this
    is String -> trim().lowercase().toBooleanStrictOrNull()
    else -> null
}
