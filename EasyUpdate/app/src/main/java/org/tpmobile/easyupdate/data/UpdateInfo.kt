package org.tpmobile.easyupdate.data

import org.tpmobile.easyupdate.util.ktx.toIntWithDefaultZero


data class UpdateInfo(val version: String?, val sources: MutableList<String>?, val appInfos: MutableList<AppInfo>?) {
    fun isUpdateInfoValid(): Boolean = appInfos?.all { it.isAppInfoValid() } ?: false
    fun isSourcesValid(): Boolean = sources?.all { it.isNotEmpty() } ?: false

    fun isAssetsNewerThanCache(assetsVersion: String?, cacheVersion: String? ): Boolean{
        if(cacheVersion.isNullOrEmpty()) return true
        if(assetsVersion.isNullOrEmpty()) return false
        val cacheVers = cacheVersion.split(".").map { it.toIntWithDefaultZero() }
        val assetsVers = assetsVersion.split(".").map { it.toIntWithDefaultZero() }
        assetsVers.forEachIndexed { index, i ->
            if(i == cacheVers[index]) return@forEachIndexed
            return i > assetsVers[index]
        }
        return false
    }
}

data class AppInfo(
    val name: String,
    val description: String?,
    val withTutorial: Boolean?,
    val urls: MutableList<String>,
) {
    fun isAppInfoValid(): Boolean =
        name.isNotEmpty() && urls.all { it.isNotEmpty() && it.startsWith("http") }

}
