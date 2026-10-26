package com.example.domain.module

/**
 * Firestore `config/app_config`: the update and maintenance switches.
 *
 * [maintenanceMode] shows every user the maintenance screen until it is switched off;
 * [maintenanceMessage] replaces that screen's default text when it isn't blank.
 */
data class AppConfig(
    val minVersionCode: Int = 1,
    val latestVersionCode: Int = 1,
    val forceUpdate: Boolean = false,
    val updateUrl: String = "",
    val maintenanceMode: Boolean = false,
    val maintenanceMessage: String = ""
)
