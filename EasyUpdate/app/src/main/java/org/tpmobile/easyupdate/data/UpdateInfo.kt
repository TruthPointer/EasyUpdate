package org.tpmobile.easyupdate.data


data class UpdateInfo(val sources: MutableList<String>?, val appInfos: MutableList<AppInfo>?) {
    fun isUpdateInfoValid(): Boolean = appInfos?.all { it.isAppInfoValid() } ?: false
    fun isSourcesValid(): Boolean = sources?.all { it.isNotEmpty() } ?: false
}

data class AppInfo(
    val name: String,
    val urls: MutableList<String>,
) {
    fun isAppInfoValid(): Boolean =
        name.isNotEmpty() && urls.all { it.isNotEmpty() && it.startsWith("http") }

}
