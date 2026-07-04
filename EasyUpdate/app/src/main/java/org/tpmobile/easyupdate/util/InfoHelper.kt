package org.tpmobile.easyupdate.util

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import org.tpmobile.easyupdate.data.AppVersionInfo

object InfoHelper {
  private val TAG = "InfoHelper"

  fun needUpdate(context: Context, jsonName: String, apkPath: String): Boolean {
    val verInfo = getApkVersionInfo(context, apkPath)
    val byEquivalent = CHECK_BY_JSON_NAME.find { verInfo.name.startsWith(it) } == null
    val searchName = if (byEquivalent) verInfo.name else jsonName
    val installedVerInfo = getInstallAppVersionInfoByAppName(context, searchName, byEquivalent)
    val isNewer = verInfo.isNewerThan(installedVerInfo)
    return isNewer
  }

  fun getApkVersionInfo(context: Context, apkPath: String): AppVersionInfo {
    val packageManager = context.packageManager
    val packageInfo = packageManager.getPackageArchiveInfo(
      apkPath, PackageManager.GET_ACTIVITIES
    )
    val appName = packageInfo?.applicationInfo?.loadLabel(packageManager).toString()
    val packageName = packageInfo?.packageName ?: ""
    val versionName = packageInfo?.versionName ?: ""
    val versionCode = if (Build.VERSION.SDK_INT >= 28)
      (packageInfo?.longVersionCode?.toInt() ?: 0)
    else
      (packageInfo?.versionCode ?: 0)
    Logger.i(TAG, "getApkVersionInfo = ${appName}\t\t$packageName\t\t$versionName\t\t$versionCode")
    return AppVersionInfo(appName, packageName, versionName, versionCode)
  }

  fun getInstallAppVersionInfoByAppName(
    context: Context,
    appName: String,
    byEquivalent: Boolean = true
  ): AppVersionInfo {
    val packageManager = context.packageManager
    val packageInfo =
      packageManager.getInstalledPackages(PackageManager.GET_ACTIVITIES).find { info ->
        val info2 = packageManager.getPackageInfo(info.packageName, 0)
        val name = info2.applicationInfo?.loadLabel(packageManager) ?: ""
        Logger.i(TAG, "${info.packageName} - $name")
        if (byEquivalent) appName == name else name.startsWith(appName)
      }
    val packageName = packageInfo?.packageName ?: ""
    val versionName = packageInfo?.versionName ?: ""
    val versionCode = if (Build.VERSION.SDK_INT >= 28)
      (packageInfo?.longVersionCode?.toInt() ?: 0)
    else
      (packageInfo?.versionCode ?: 0)
    Logger.i(
      TAG,
      "getInstallAppVersionInfoByAppName = $appName\t\t\t$packageName\t\t$versionName\t\t$versionCode"
    )
    return AppVersionInfo(appName, packageName, versionName, versionCode)
  }

}