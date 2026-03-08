package com.example.domain.module

data class AppConfig(
    val minVersionCode: Int = 1,
    val latestVersionCode: Int = 1,
    val forceUpdate: Boolean = false,
    val updateUrl: String = "",
    val maintenanceMode: Boolean = false
)