package org.tpmobile.easyupdate.ui.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.tpmobile.easyupdate.data.UpdateInfo
import org.tpmobile.easyupdate.ui.parseJsonData
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader

class ActivityMainViewModel : ViewModel() {

    private val emptyUpdateInfo = UpdateInfo(
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
        _updateInfo.value = updateInfo
    }

    fun initUpdateInfo(context: Context) {
        var json = ""
        val updateInfoFile = File(context.cacheDir, "update_info.json")
        if (updateInfoFile.exists()) {
            BufferedReader(InputStreamReader(FileInputStream(updateInfoFile))).use { bufferedReader ->
                json = bufferedReader.readLines().joinToString("\n")
            }
        } else {
            BufferedReader(InputStreamReader(context.assets.open("update_info.json"))).use { bufferedReader ->
                json = bufferedReader.readLines().joinToString("\n")
            }
        }
        _updateInfo.value = parseJsonData(context, json) ?: emptyUpdateInfo
    }

    class Factory() : ViewModelProvider.NewInstanceFactory() {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ActivityMainViewModel() as T
        }
    }
}