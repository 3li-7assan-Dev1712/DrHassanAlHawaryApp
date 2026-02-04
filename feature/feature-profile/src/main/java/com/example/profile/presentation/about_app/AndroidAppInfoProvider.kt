package com.example.profile.presentation.about_app

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
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

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun getAppInfo(): AppInfo {
        val pm = context.packageManager
        val pkg = context.packageName

        val pInfo = pm.getPackageInfo(pkg, PackageManager.PackageInfoFlags.of(0))
        val versionName = pInfo.versionName ?: "1.0.0"
        val versionCode = pInfo.longVersionCode

        val appName = context.applicationInfo.loadLabel(pm).toString()

        return AppInfo(
            appName = appName,
            versionName = versionName,
            versionCode = versionCode
        )
    }
}