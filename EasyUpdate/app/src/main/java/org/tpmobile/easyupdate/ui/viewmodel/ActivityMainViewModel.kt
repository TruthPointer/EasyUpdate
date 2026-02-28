package org.tpmobile.easyupdate.ui.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.tpmobile.easyupdate.MyApp
import org.tpmobile.easyupdate.data.UpdateInfo
import org.tpmobile.easyupdate.ui.parseJsonData
import org.tpmobile.easyupdate.util.Logger
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader

class ActivityMainViewModel : ViewModel() {

    private val emptyUpdateInfo = UpdateInfo(
        version = "",
        sources = mutableListOf(),
        appInfos = mutableListOf()
    )

    private val _disableIndex = MutableLiveData(-1)
    val disableIndex: LiveData<Int> = _disableIndex

    private val _apkFileForInstall = MutableLiveData("")
    val apkFileForInstall: MutableLiveData<String> = _apkFileForInstall

    private val _updateInfo = MutableLiveData<UpdateInfo>(emptyUpdateInfo)
    val updateInfo: LiveData<UpdateInfo> = _updateInfo

    fun setDiableIndex(index: Int) {
        //if (_disableIndex.value != index)
        _disableIndex.value = index
    }

    fun setApkFileForInstall(filePath: String) {
        _apkFileForInstall.value = filePath
    }

    fun setUpdateInfo(updateInfo: UpdateInfo) {
        refreshUpdateInfo(updateInfo)
        //_updateInfo.value = updateInfo
    }

    fun initUpdateInfo(context: Context) {
        val updateInfoFile = File(context.cacheDir, "update_info.json")
        if (updateInfoFile.exists()) {
            var cacheUpdateInfo: UpdateInfo? = null
            BufferedReader(InputStreamReader(FileInputStream(updateInfoFile))).use { bufferedReader ->
                val json = bufferedReader.readLines().joinToString("\n")
                cacheUpdateInfo = parseJsonData(json) ?: emptyUpdateInfo
            }
            var assetsUpdateInfo: UpdateInfo? = null
            BufferedReader(InputStreamReader(context.assets.open("update_info.json"))).use { bufferedReader ->
                val json = bufferedReader.readLines().joinToString("\n")
                assetsUpdateInfo = parseJsonData(json) ?: emptyUpdateInfo
            }
            if (assetsUpdateInfo?.isAssetsNewerThanCache(
                    assetsUpdateInfo.version,
                    cacheUpdateInfo?.version
                ) == true
            ) {
                Logger.i("initUpdateInfo: assets的比cache的新")
                updateInfoFile.delete()
                _updateInfo.value = assetsUpdateInfo
            } else {
                Logger.i("initUpdateInfo: assets的比cache的旧")
                _updateInfo.value = cacheUpdateInfo ?: emptyUpdateInfo
            }
        } else {
            BufferedReader(InputStreamReader(context.assets.open("update_info.json"))).use { bufferedReader ->
                val json = bufferedReader.readLines().joinToString("\n")
                _updateInfo.value = parseJsonData(json) ?: emptyUpdateInfo
            }
        }
    }

    fun refreshUpdateInfo(updateInfo: UpdateInfo) {
        var assetsUpdateInfo: UpdateInfo? = null
        BufferedReader(InputStreamReader(MyApp.appContext.assets.open("update_info.json"))).use { bufferedReader ->
            val json = bufferedReader.readLines().joinToString("\n")
            assetsUpdateInfo = parseJsonData(json) ?: emptyUpdateInfo
        }
        if (assetsUpdateInfo?.isAssetsNewerThanCache(
                assetsUpdateInfo.version,
                updateInfo.version
            ) == true
        ) {
            Logger.i("refreshUpdateInfo: assets的比更新到的新")
            File(MyApp.appContext.cacheDir, "update_info.json").delete()
            _updateInfo.value = assetsUpdateInfo
        } else {
            Logger.i("refreshUpdateInfo: assets的比更新到的旧")
            _updateInfo.value = updateInfo
        }
    }

    class Factory() : ViewModelProvider.NewInstanceFactory() {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ActivityMainViewModel() as T
        }
    }
}
