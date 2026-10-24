package com.example.profile.presentation.about_app

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.pm.PackageInfoCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

data class AppInfo(
    val appName: String,
    val versionName: String,
    val versionCode: Long
)

interface AppInfoProvider {
    fun getAppInfo(): AppInfo
}

class AndroidAppInfoProvider @Inject constructor(
    @ApplicationContext private val context: Context
) : AppInfoProvider {

    override fun getAppInfo(): AppInfo {
        val pm = context.packageManager
        val pkg = context.packageName

        // PackageInfoFlags is Android 13+ and longVersionCode Android 9+: called directly,
        // they crashed the About screen on older devices (minSdk is 24).
        val pInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getPackageInfo(pkg, PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            pm.getPackageInfo(pkg, 0)
        }
        val versionName = pInfo.versionName ?: "1.0.0"
        val versionCode = PackageInfoCompat.getLongVersionCode(pInfo)

        val appName = context.applicationInfo.loadLabel(pm).toString()

        return AppInfo(
            appName = appName,
            versionName = versionName,
            versionCode = versionCode
        )
    }
}