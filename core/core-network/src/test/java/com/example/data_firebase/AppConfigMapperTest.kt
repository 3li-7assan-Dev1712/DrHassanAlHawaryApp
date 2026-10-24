package com.example.data_firebase

import com.example.domain.module.AppConfig
import java.util.Date
import org.junit.Assert.assertEquals
import org.junit.Test

class AppConfigMapperTest {

    @Test
    fun missingDocument_isAllDefaults() {
        assertEquals(AppConfig(), appConfigFrom(null))
        assertEquals(AppConfig(), appConfigFrom(emptyMap()))
    }

    @Test
    fun wellTypedDocument_readsEveryField() {
        // Firestore hands whole numbers over as Long.
        val config = appConfigFrom(
            mapOf(
                "minVersionCode" to 10L,
                "latestVersionCode" to 12L,
                "forceUpdate" to true,
                "updateUrl" to "https://play.google.com/store/apps/details?id=app.netlify.devalihassan",
                "maintenanceMode" to true,
                "maintenanceMessage" to "  نعود الساعة ٩ مساءً  ",
            )
        )
        assertEquals(
            AppConfig(
                minVersionCode = 10,
                latestVersionCode = 12,
                forceUpdate = true,
                updateUrl = "https://play.google.com/store/apps/details?id=app.netlify.devalihassan",
                maintenanceMode = true,
                maintenanceMessage = "نعود الساعة ٩ مساءً",
            ),
            config,
        )
    }

    @Test
    fun valuesTypedAsText_areStillRead() {
        val config = appConfigFrom(
            mapOf("maintenanceMode" to "TRUE", "minVersionCode" to " 11 ", "forceUpdate" to "false")
        )
        assertEquals(true, config.maintenanceMode)
        assertEquals(11, config.minVersionCode)
        assertEquals(false, config.forceUpdate)
    }

    @Test
    fun unreadableValues_keepTheirDefaults_andTheRestStillApply() {
        val config = appConfigFrom(
            mapOf(
                "maintenanceMode" to "yes",
                "minVersionCode" to "twelve",
                "latestVersionCode" to Date(),
                "maintenanceMessage" to 42L,
                "updateUrl" to listOf("x"),
                "forceUpdate" to 1L,
                "unknownField" to mapOf("a" to 1),
            )
        )
        assertEquals(AppConfig(), config)
    }

    @Test
    fun maintenanceStoredAsANumber_readsAsOff() {
        assertEquals(false, appConfigFrom(mapOf("maintenanceMode" to 1L)).maintenanceMode)
    }

    @Test
    fun fractionalAndHugeNumbers() {
        assertEquals(12, appConfigFrom(mapOf("minVersionCode" to 12.7)).minVersionCode)
        assertEquals(1, appConfigFrom(mapOf("minVersionCode" to 9_999_999_999L)).minVersionCode)
    }

    @Test
    fun nullValues_keepTheirDefaults() {
        val config = appConfigFrom(mapOf("maintenanceMode" to null, "maintenanceMessage" to null))
        assertEquals(AppConfig(), config)
    }
}
