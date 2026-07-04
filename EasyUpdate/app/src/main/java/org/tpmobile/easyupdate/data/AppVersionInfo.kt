package org.tpmobile.easyupdate.data

import java.util.Locale

data class AppVersionInfo(
  val name: String,
  val packageName: String,
  val versionName: String,
  val versionCode: Int
) {
  fun toCompareString() = "$versionName.${String.format(Locale.US, "%3d", versionCode)}"
  fun isNewerThan(appVersionInfo: AppVersionInfo) =
    this.toCompareString() > appVersionInfo.toCompareString()
}
