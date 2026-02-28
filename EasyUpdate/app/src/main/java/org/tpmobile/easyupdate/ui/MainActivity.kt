package org.tpmobile.easyupdate.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.tpmobile.easyupdate.R
import org.tpmobile.easyupdate.data.AppInfo
import org.tpmobile.easyupdate.data.UpdateInfo
import org.tpmobile.easyupdate.ui.theme.EasyUpdateTheme
import org.tpmobile.easyupdate.ui.viewmodel.ActivityMainViewModel
import org.tpmobile.easyupdate.util.CHECK_LIST
import org.tpmobile.easyupdate.util.DEFAULT_VALUE_TIME_INTERVAL_MAX
import org.tpmobile.easyupdate.util.DEFAULT_VALUE_TIME_INTERVAL_MIN
import org.tpmobile.easyupdate.util.DEFAULT_VALUE_TRY_TIMES
import org.tpmobile.easyupdate.util.EncryptUtils
import org.tpmobile.easyupdate.util.HttpUtil
import org.tpmobile.easyupdate.util.Logger
import org.tpmobile.easyupdate.util.PREF_TIME_INTERVAL_MAX
import org.tpmobile.easyupdate.util.PREF_TIME_INTERVAL_MIN
import org.tpmobile.easyupdate.util.PREF_TRY_TIMES
import org.tpmobile.easyupdate.util.ZipUtil
import org.tpmobile.easyupdate.util.ktx.getPref
import org.tpmobile.easyupdate.util.ktx.setPref
import org.tpmobile.easyupdate.util.ktx.toIntWithDefaultZero
import org.tpmobile.easyupdate.util.ktx.toast
import java.io.File
import kotlin.coroutines.cancellation.CancellationException
import kotlin.random.Random

class MainActivity : ComponentActivity() {

    private var exitTime = 0L
    var apkFileForInstall: String = ""
    val viewModel: ActivityMainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        dispatchOnBackEvent()

        viewModel.apkFileForInstall.observe(this) { filePath ->
            Logger.i("1.[apkFileForInstall.observe]...")
            if (filePath.isEmpty()) return@observe
            apkFileForInstall = filePath
            installApk(apkFileForInstall)
        }

        viewModel.updateInfo.observe(this) { updateInfo ->
            if (updateInfo?.appInfos?.isEmpty() == true) {
                toast("数据错误，请在菜单选择刷新以更新数据源。")
            }
        }

        viewModel.initUpdateInfo(this)

        setContent {
            EasyUpdateTheme {
                var showRefreshDialog by remember { mutableStateOf(false) }
                var showSettingsDialog by remember { mutableStateOf(false) }
                var showAboutDialog by remember { mutableStateOf(false) }

                val updateInfoState by viewModel.updateInfo.observeAsState()

                Scaffold(
                    modifier = Modifier.fillMaxSize(), topBar = {
                        TopAppBar(title = {}, navigationIcon = {
                            Image(ImageVector.vectorResource(R.drawable.app_logo), null)
                        }, actions = {
                            if (updateInfoState?.sources?.isNotEmpty() == true) {
                                IconButton(onClick = {
                                    showRefreshDialog = true
                                }) {
                                    Icon(Icons.Filled.Refresh, "更新源")
                                }
                            } else {
                                toast("没有可用于更新的源！")
                            }
                            IconButton(onClick = {
                                showSettingsDialog = true
                            }) {
                                Icon(Icons.Filled.Settings, "设置")
                            }
                            IconButton(onClick = {
                                showAboutDialog = true
                            }) {
                                Icon(Icons.Filled.Info, "程序信息")
                            }
                        })
                    }) { innerPadding ->

                    Greeting(
                        modifier = Modifier.padding(innerPadding),
                        innerPadding.calculateTopPadding(),
                        viewModel,
                        updateInfoState?.appInfos ?: mutableListOf()
                    )
                    if (showRefreshDialog) {
                        if (updateInfoState?.isSourcesValid() == false) {
                            toast("没有可更新的源！")
                            return@Scaffold
                        }
                        ShowRefreshDialog(
                            updateInfoState?.sources!!,
                            viewModel,
                            onDismissRequest = {
                                showRefreshDialog = false
                            },
                            onConfirmation = {})
                    }
                    if (showSettingsDialog) {
                        ShowSettingsDialog(
                            onConfirmation = { /*toast("确认了")*/ },
                            onDismissRequest = { showSettingsDialog = false })
                    }
                    if (showAboutDialog) {
                        //ShowTestDialog2({showAboutDialog = false}, onConfirmation = {})
                        ShowAboutDialog(
                            { showAboutDialog = false })
                    }
                }
            }
        }
    }

    //////////////////////////
    val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Logger.i("permissionLauncher: ${result.resultCode}")
            if (result.resultCode == RESULT_OK) {
                toast("成功获得安装程序的权限，开始安装...")
                if (apkFileForInstall.isEmpty()) return@registerForActivityResult
                installApk(apkFileForInstall)//???
            } else {
                toast("没能获得安装程序的权限，安装取消")
            }
        }
    val apkInstallationLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Logger.i("apkInstallationLauncher: REQUEST_CODE_APP_INSTALL ${result.resultCode}")
            if (result.resultCode == RESULT_OK) {
                installApk(apkFileForInstall)//???
            }
        }

    private fun installApk(apkFilePath: String) {
        Logger.i("...installApk: $apkFilePath")
        try {
            val apkFile = File(apkFilePath)
            if (!apkFile.exists()) {
                toast("安装包不存在")
                return
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val hasInstallPermission = packageManager.canRequestPackageInstalls()
                if (!hasInstallPermission) {
                    toast("请开启“安装未知来源应用”的权限", true)
                    val permissionIntent = Intent(
                        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, ("package:$packageName").toUri()
                    )
                    permissionLauncher.launch(permissionIntent)
                    return
                }
            }

            val installIntent = Intent(Intent.ACTION_VIEW)
            val authority = applicationContext.packageName + ".fileProvider"
            val apkUri = FileProvider.getUriForFile(this, authority, apkFile)
            installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            installIntent.setDataAndType(apkUri, "application/vnd.android.package-archive")
            installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)//???
            apkInstallationLauncher.launch(installIntent)
        } catch (e: Exception) {
            Logger.e("installApk: ${e.message}")
            toast("安装出错，详情：${e.message}")
        }
    }

    fun dispatchOnBackEvent() {
        onBackPressedDispatcher.addCallback(
            this,
            onBackPressedCallback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if ((System.currentTimeMillis() - exitTime) > 2000) {
                        toast(R.string.quit_the_app_after_pressing_back_key_once_again)
                        exitTime = System.currentTimeMillis()
                    } else {
                        finish()
                    }
                }
            })
    }
}

@Composable
fun Greeting(
    modifier: Modifier = Modifier,
    paddingTop: Dp,
    viewModel: ActivityMainViewModel,
    appInfos: MutableList<AppInfo>
) {
    AppList(
        appInfos, paddingTop = paddingTop, viewModel = viewModel
    )
}

@Composable
fun AppList(
    appInfos: MutableList<AppInfo>,
    modifier: Modifier = Modifier,
    paddingTop: Dp,
    viewModel: ActivityMainViewModel
) {
    LazyColumn(
        modifier = Modifier.padding(top = paddingTop),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        itemsIndexed(items = appInfos) { index, item ->
            AppInfoItem(index, item, viewModel)
        }
    }
}

@Composable
fun AppInfoItem(
    itemIndex: Int, data: AppInfo, viewModel: ActivityMainViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var taskRunningDetails by remember { mutableStateOf("准备中") }
    var enabledState by remember { mutableStateOf(true) }
    var enabledUpdateButtonState by remember { mutableStateOf(true) }
    var hideProgressBar by remember { mutableStateOf(false) }
    var loadingState by remember { mutableStateOf(false) }
    var currentTryTimes by remember { mutableIntStateOf(1) }
    var currentProgress by remember { mutableIntStateOf(0) }
    var cancelJoinJob by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var job: Job? = null

    val disableIndex by viewModel.disableIndex.observeAsState()

    val tryTimes = context.getPref(PREF_TRY_TIMES, DEFAULT_VALUE_TRY_TIMES)
    val timeIntervalMin =
        context.getPref(PREF_TIME_INTERVAL_MIN, DEFAULT_VALUE_TIME_INTERVAL_MIN)
    val timeIntervalMax =
        context.getPref(PREF_TIME_INTERVAL_MAX, DEFAULT_VALUE_TIME_INTERVAL_MAX)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(8.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = data.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight(800),
                    textAlign = TextAlign.Center,
                    //modifier = Modifier.fillMaxWidth(0.2f)
                )
                Row(
                    //Modifier.fillMaxWidth(0.7f),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = {
                            Logger.i("$[${data.name}] 正在更新...")
                            enabledState = false
                            loadingState = true
                            taskRunningDetails = "正在下载..."
                            viewModel.setDiableIndex(itemIndex)
                            job = scope.launch {
                                enabledState = false
                                loadingState = true

                                //1.下载
                                //1.1
                                //taskRunningDetails = "正在下载..."
                                //viewModel.setDiableIndex(itemIndex)
                                var filePath = ""
                                var urlIndex = 0
                                for (index in 1..tryTimes) {
                                    Logger.e("index = $index => cancelJoinJob: $cancelJoinJob, isActive = $isActive")
                                    if (!isActive) {
                                        Logger.i("取消退出！")
                                        taskRunningDetails = "取消退出！"
                                        context.toast("取消退出！")
                                        viewModel.setDiableIndex(-1)
                                        enabledState = true
                                        loadingState = false
                                        hideProgressBar = false
                                        delay(1000)
                                        return@launch
                                    }

                                    //taskRunningDetails = ""
                                    currentProgress = 0
                                    currentTryTimes = index
                                    taskRunningDetails = "已下载0%"
                                    hideProgressBar = false
                                    urlIndex = when {
                                        tryTimes >= data.urls.size -> {
                                            currentTryTimes % data.urls.size
                                        }

                                        else -> {
                                            Random.nextInt(data.urls.size)
                                        }
                                    }
                                    //1.2
                                    HttpUtil.downloadFile(
                                        context,
                                        data.urls[urlIndex],
                                        onProgress = { progress, info ->
                                            when (progress) {
                                                -2 -> {
                                                    Logger.i("下载状态：$info")
                                                    hideProgressBar = true
                                                    taskRunningDetails = info
                                                }

                                                else -> {
                                                    currentProgress = progress
                                                    taskRunningDetails = "已下载${currentProgress}%"
                                                    Logger.i("下载状态：$info")
                                                }
                                            }
                                        }).fold(
                                        onSuccess = { path ->
                                            Logger.i("成功下载")
                                            taskRunningDetails = "成功下载"
                                            context.toast("下载成功")
                                            filePath = path
                                            break
                                        },
                                        onFailure = { e ->
                                            taskRunningDetails =
                                                "下载失败，详情：${e.message ?: "原因不详"}"
                                            if (index == tryTimes) {
                                                Logger.e("所有下载尝试都失败了！")
                                                taskRunningDetails = "所有下载尝试都失败了！"
                                                context.toast("所有下载尝试都失败了！")
                                                viewModel.setDiableIndex(-1)
                                                enabledState = true
                                                loadingState = false
                                                delay(1000)
                                                return@launch
                                            } else {
                                                delay(1000)
                                                val delayTimeSecond =
                                                    Random.nextInt(timeIntervalMin, timeIntervalMax)
                                                taskRunningDetails =
                                                    "下载出错了，等待${delayTimeSecond}秒再尝试..."
                                                Logger.e("下载出错，等待${delayTimeSecond}秒再尝试...")
                                                delay(delayTimeSecond * 1000L)
                                            }
                                            hideProgressBar = false
                                            continue
                                        }
                                    )
                                }
                                hideProgressBar = false
                                taskRunningDetails = "下载完成"
                                Logger.i("下载文件：$filePath")

                                //2.判断及解压
                                var apkFile = filePath
                                if (!filePath.endsWith(".apk", ignoreCase = true)) {
                                    if (!isActive) {
                                        viewModel.setDiableIndex(-1)
                                        enabledState = true
                                        loadingState = false
                                        taskRunningDetails = ""
                                        return@launch
                                    }
                                    taskRunningDetails = "正在解压..."
                                    val zipFile = File(filePath)
                                    val unzipPath =
                                        context.cacheDir.absolutePath + File.separator + zipFile.nameWithoutExtension + "-unzip"
                                    ZipUtil.unZipFileWithProgress(
                                        zipFile, unzipPath, onProgress = { progress, info ->
                                            currentProgress = progress
                                            taskRunningDetails = "已解压${currentProgress}%"
                                            Logger.i("解压状态：$info")
                                        }).fold(
                                        onSuccess = {
                                            Logger.i("解压完成1")
                                        },
                                        onFailure = { e ->
                                            Logger.e("解压出错了，详情：${e.message}")
                                            context.toast("解压出错了，详情：${e.message}")
                                            taskRunningDetails = "解压出错了，详情：${e.message}"

                                            Logger.e("解压出错了")
                                            viewModel.setDiableIndex(-1)
                                            enabledState = true
                                            loadingState = false
                                            taskRunningDetails = ""
                                            return@launch
                                        }
                                    )
                                    Logger.i("解压完成2")
                                    taskRunningDetails = "解压完成"

                                    //3.查找apk文件，等待安装
                                    if (!isActive) {
                                        viewModel.setDiableIndex(-1)
                                        enabledState = true
                                        loadingState = false
                                        taskRunningDetails = ""
                                        return@launch
                                    }
                                    taskRunningDetails = "正在检查安装文件..."
                                    delay(500)
                                    taskRunningDetails = ""
                                    apkFile = ZipUtil.searchFile(File(unzipPath))
                                }

                                //4.安装
                                if (!isActive || apkFile.isEmpty()) {
                                    viewModel.setDiableIndex(-1)
                                    enabledState = true
                                    loadingState = false
                                    taskRunningDetails =
                                        if (apkFile.isEmpty()) "解压文件内无有效可安装文件！" else ""
                                    if (apkFile.isEmpty())
                                        context.toast("解压文件内无有效可安装文件！")
                                    return@launch
                                }
                                //4.1
                                val apkName = File(apkFile).nameWithoutExtension
                                if (CHECK_LIST.find {
                                        it.equals(
                                            apkName,
                                            ignoreCase = true
                                        )
                                    } != null) {
                                    taskRunningDetails = "验证安装文件..."
                                    Logger.i("找到 $apkName， 需要验证...")
                                    if (!EncryptUtils.isMySignature(apkFile)) {
                                        Logger.e("【验证失败】遇到无效的安装文件，无法继续安装！")
                                        viewModel.setDiableIndex(-1)
                                        enabledState = true
                                        loadingState = false
                                        taskRunningDetails = "验证安装文件失败！"
                                        context.toast("验证安装文件失败，无法继续安装！")
                                        return@launch
                                    }
                                    Logger.i("【验证成功】")
                                    taskRunningDetails = "验证安装文件成功！"
                                    delay(1000)
                                }
                                //4.2
                                taskRunningDetails = "等待安装..."
                                viewModel.setApkFileForInstall(apkFile)

                                //6.恢复初始状态
                                viewModel.setDiableIndex(-1)
                                enabledState = true
                                loadingState = false
                                taskRunningDetails = ""
                            }
                            Logger.i("job = ${job.key}")
                        },
                        enabled = enabledState && (disableIndex!! == -1 || disableIndex == itemIndex) && enabledUpdateButtonState
                    ) {
                        Icon(Icons.Filled.PlayArrow, "更新")
                    }
                    IconButton(
                        onClick = {
                            enabledUpdateButtonState = false
                            loadingState = false
                            cancelJoinJob = true
                            scope.launch {
                                delay(2000)
                                Logger.e("cancelAndJoin...")

                                job?.cancel(CancellationException("自定义"))
                                job?.join()
                                Logger.e("cancelAndJoin---")
                                enabledUpdateButtonState = true
                                cancelJoinJob = false
                                enabledState = true
                                loadingState = false
                                viewModel.setDiableIndex(-1)
                            }
                        },
                        enabled = !enabledState && (disableIndex!! == -1 || disableIndex == itemIndex)
                    ) {
                        Icon(ImageVector.vectorResource(R.drawable.ic_action_stop), "取消")
                    }
                    if (data.withTutorial == true) {
                        IconButton(onClick = { expanded = !expanded }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "更多")
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("分享程序") },
                                onClick = {
                                    expanded = false
                                    try {
                                        scope.launch {
                                            //1、查找已下载的文件
                                            var unzipPath: File?
                                            var fileName: String
                                            var apkFilePath = ""
                                            data.urls.forEach { url ->
                                                fileName = File(url).nameWithoutExtension
                                                if (url.endsWith(".apk", ignoreCase = true)) {
                                                    apkFilePath =
                                                        context.cacheDir.listFiles { it.isFile }
                                                            ?.find { it.nameWithoutExtension == fileName }?.absolutePath.toString()
                                                } else {//其它，包括 .zip 文件
                                                    unzipPath = context.cacheDir
                                                        .listFiles { it.isDirectory }
                                                        ?.find { it.name == "${fileName}-unzip" }
                                                    if (unzipPath == null) return@forEach
                                                    apkFilePath = ZipUtil.searchFile(unzipPath)
                                                    if (apkFilePath.isEmpty()) return@forEach
                                                }
                                            }
                                            if (apkFilePath.isEmpty() || !File(apkFilePath).exists()) {
                                                context.toast("没有找到可分享的文件")
                                                return@launch
                                            }
                                            Logger.i("share: $apkFilePath")
                                            //2、分享
                                            val authority =
                                                context.packageName + ".fileProvider"
                                            val apkUri = FileProvider.getUriForFile(
                                                context,
                                                authority,
                                                File(apkFilePath)
                                            )
                                            ShareCompat.IntentBuilder(context)
                                                .setStream(apkUri)
                                                .setType("application/*")//使用 application/vnd.android.package-archive 会导致不显示蓝牙
                                                .startChooser()
                                        }
                                    } catch (e: Exception) {
                                        Logger.e(e.message ?: "未知异常")
                                        context.toast("分享文件错误！详情：${e.message ?: "未知异常"}")
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("查看教程") },
                                onClick = {
                                    expanded = false
                                    try {
                                        scope.launch {
                                            //1、查找已下载的文件
                                            var unzipPath: File?
                                            var fileName: String
                                            var pdfFilePath = ""
                                            data.urls.forEach { url ->
                                                fileName = File(url).nameWithoutExtension
                                                if (url.endsWith(".apk", ignoreCase = true)) {
                                                    pdfFilePath =
                                                        context.cacheDir.listFiles { it.isFile }
                                                            ?.find { it.nameWithoutExtension == fileName }?.absolutePath.toString()
                                                } else {//其它，包括 .zip 文件
                                                    unzipPath = context.cacheDir
                                                        .listFiles { it.isDirectory }
                                                        ?.find { it.name == "${fileName}-unzip" }
                                                    if (unzipPath == null) return@forEach
                                                    pdfFilePath =
                                                        ZipUtil.searchFile(unzipPath, "pdf")
                                                    if (pdfFilePath.isEmpty()) return@forEach
                                                }
                                            }
                                            if (pdfFilePath.isEmpty() || !File(pdfFilePath).exists()) {
                                                context.toast("没有找到教程")
                                                return@launch
                                            }
                                            Logger.i("share: $pdfFilePath")
                                            //2、打开
                                            val authority =
                                                context.packageName + ".fileProvider"
                                            val pdfUri = FileProvider.getUriForFile(
                                                context,
                                                authority,
                                                File(pdfFilePath)
                                            )
                                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                setDataAndType(pdfUri, "application/pdf")
                                            }
                                            context.startActivity(intent)
                                        }
                                    } catch (e: Exception) {
                                        Logger.e(e.message ?: "未知异常")
                                        context.toast("打开教程错误！详情：${e.message ?: "未知异常"}")
                                    }
                                }
                            )
                        }
                    } else {
                        IconButton(
                            {
                                try {
                                    scope.launch {
                                        //1、查找已下载的文件
                                        var unzipPath: File?
                                        var fileName: String
                                        var apkFilePath = ""
                                        data.urls.forEach { url ->
                                            fileName = File(url).nameWithoutExtension
                                            if (url.endsWith(".apk", ignoreCase = true)) {
                                                apkFilePath =
                                                    context.cacheDir.listFiles { it.isFile }
                                                        ?.find { it.nameWithoutExtension == fileName }?.absolutePath.toString()
                                            } else {//其它，包括 .zip 文件
                                                unzipPath = context.cacheDir
                                                    .listFiles { it.isDirectory }
                                                    ?.find { it.name == "${fileName}-unzip" }
                                                if (unzipPath == null) return@forEach
                                                apkFilePath = ZipUtil.searchFile(unzipPath)
                                                if (apkFilePath.isEmpty()) return@forEach
                                            }
                                        }
                                        if (apkFilePath.isEmpty() || !File(apkFilePath).exists()) {
                                            context.toast("没有找到可分享的文件")
                                            return@launch
                                        }
                                        Logger.i("share: $apkFilePath")
                                        //2、分享
                                        val authority = context.packageName + ".fileProvider"
                                        val apkUri = FileProvider.getUriForFile(
                                            context,
                                            authority,
                                            File(apkFilePath)
                                        )
                                        ShareCompat.IntentBuilder(context)
                                            .setStream(apkUri)
                                            .setType("application/*")//使用 application/vnd.android.package-archive 会导致不显示蓝牙
                                            .startChooser()
                                    }
                                } catch (e: Exception) {
                                    Logger.e(e.message ?: "未知异常")
                                    context.toast("分享文件错误！详情：${e.message ?: "未知异常"}")
                                }
                            },
                            enabled = enabledState && (disableIndex!! == -1 || disableIndex == itemIndex) && enabledUpdateButtonState
                        ) {
                            Icon(Icons.Filled.Share, "分享")
                        }
                    }
                }
            }
            if (!data.description.isNullOrEmpty()) {
                Spacer(Modifier.height(3.dp))
                Text(
                    text = data.description,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            data.urls.forEachIndexed { index, url ->
                Text(
                    text = "${index + 1}.${url}",
                    fontSize = 14.sp,
                    lineHeight = 16.sp,
                    textDecoration = TextDecoration.Underline
                )
            }

            if (loadingState) {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Column(
                    Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "第 $currentTryTimes 次尝试下载...",
                        modifier = Modifier.padding(start = 6.dp)
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = taskRunningDetails,
                        modifier = Modifier.padding(start = 6.dp),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(3.dp))
                    if (!hideProgressBar) {
                        LinearProgressIndicator(
                            progress = { currentProgress / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp),
                        )
                    }
                }
            }
            if (cancelJoinJob) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(
                            width = 50.dp,
                            height = 50.dp
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("正在取消任务，请稍后！")
                    /*LaunchedEffect(null)  {
                        delay(2000)
                        cancelJoinJob = false
                    }*/

                }
            }
            Spacer(Modifier.height(6.dp))
        }
    }
}

fun parseJsonData(/*context: Context, */json: String): UpdateInfo? {
    try {
        if (json.isEmpty()) return null

        val updateInfo: UpdateInfo =
            Gson().fromJson(json, object : TypeToken<UpdateInfo>() {}.type)
        if (updateInfo.sources?.isEmpty() == false) {
            for (index in updateInfo.sources.size - 1 downTo 0) {
                if (updateInfo.sources[index].isEmpty())
                    updateInfo.sources.removeAt(index)
            }
        }
        if (updateInfo.appInfos?.isEmpty() == false) {
            for (index in updateInfo.appInfos.size - 1 downTo 0) {
                if (updateInfo.appInfos[index].name.isEmpty()) {
                    updateInfo.appInfos.removeAt(index)
                    continue
                }
                val nus =
                    updateInfo.appInfos[index].urls.filter { it.isNotEmpty() && it.startsWith("http") }
                if (nus.isEmpty()) {
                    updateInfo.appInfos.removeAt(index)
                    continue
                } /*else if (nus.size != updateInfo.appInfos[index].urls.size) {}*/
                updateInfo.appInfos[index].urls.clear()
                updateInfo.appInfos[index].urls.addAll(nus)
            }
        }
        return updateInfo
    } catch (e: Exception) {
        Logger.i(e.message ?: "未知原因")
        //context.toast("Gson解析数据出错！！！详情：${e.message}")
        return null
    }
}

@Composable
fun ShowRefreshDialog(
    sources: List<String>,
    viewModel: ActivityMainViewModel,
    onConfirmation: () -> Unit, onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var taskRunningDetails by remember { mutableStateOf("第1次尝试...") }
    var currentProgress by remember { mutableIntStateOf(0) }
    var hideProgressBar by remember { mutableStateOf(false) }
    var loadingState by remember { mutableStateOf(false) }
    var closeDialog by remember { mutableStateOf(false) }
    var cancelJoinJob by remember { mutableStateOf(false) }
    var job: Job? = null

    var currentTryTimes by remember { mutableIntStateOf(0) }
    val tryTimes = context.getPref(PREF_TRY_TIMES, DEFAULT_VALUE_TRY_TIMES)
    val timeIntervalMin =
        context.getPref(PREF_TIME_INTERVAL_MIN, DEFAULT_VALUE_TIME_INTERVAL_MIN)
    val timeIntervalMax =
        context.getPref(PREF_TIME_INTERVAL_MAX, DEFAULT_VALUE_TIME_INTERVAL_MAX)

    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            if (cancelJoinJob) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(
                            width = 60.dp,
                            height = 60.dp
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("正在取消任务，请稍后！")
                }
            } else {
                Column(Modifier.padding(top = 16.dp, bottom = 32.dp, start = 16.dp, end = 16.dp)) {
                    Text("更新数据源", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "请点击“更新数据”按钮，以确认执行更新。",
                        fontWeight = FontWeight.Bold
                    )
                    if (currentTryTimes > 0) {
                        Spacer(Modifier.height(8.dp))
                        Text(taskRunningDetails, maxLines = 3, overflow = TextOverflow.Ellipsis)
                        Spacer(Modifier.height(8.dp))
                        if (loadingState && !hideProgressBar) {
                            LinearProgressIndicator(
                                progress = { currentProgress / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp),
                            )
                        }
                    }
                    //按钮
                    Spacer(Modifier.height(24.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = {
                            loadingState = true
                            job = scope.launch {
                                val url = sources[Random.nextInt(sources.size)]//随机访问
                                var filePath = ""
                                for (index in 1..tryTimes) {
                                    if (!isActive) {
                                        Logger.i("取消退出！")
                                        taskRunningDetails = "取消退出！"
                                        context.toast("取消退出！")
                                        loadingState = false
                                        delay(1000)
                                        return@launch
                                    }
                                    //1.下载
                                    taskRunningDetails = "正在下载..."
                                    currentTryTimes = index

                                    HttpUtil.downloadFile(
                                        context,
                                        url,
                                        onProgress = { progress, info ->
                                            when (progress) {
                                                -2 -> {
                                                    Logger.i("下载状态：$info")
                                                    hideProgressBar = true
                                                    taskRunningDetails = info
                                                }

                                                else -> {
                                                    currentProgress = progress
                                                    taskRunningDetails =
                                                        "已下载${currentProgress}%"
                                                    Logger.i("下载状态：$info")
                                                }
                                            }
                                        }).fold(
                                        onSuccess = { path ->
                                            Logger.i("成功下载")
                                            taskRunningDetails = "成功下载"
                                            context.toast("下载成功")
                                            filePath = path
                                        },
                                        onFailure = { e ->
                                            Logger.e("下载出错了，详情：${e.message}")
                                            taskRunningDetails = "下载失败，详情：${e.message}"
                                            if (index == tryTimes) {
                                                delay(1000)
                                                Logger.e("所有下载尝试都失败了！")
                                                taskRunningDetails = "所有下载尝试都失败了！"
                                                context.toast("所有下载尝试都失败了！")
                                            } else {
                                                delay(1000)
                                                val delayTimeSecond =
                                                    Random.nextInt(timeIntervalMin, timeIntervalMax)
                                                taskRunningDetails =
                                                    "下载出错了，等待${delayTimeSecond}秒再尝试..."
                                                Logger.e("下载出错，等待${delayTimeSecond}秒再尝试...")
                                                delay(delayTimeSecond * 1000L)
                                            }
                                            hideProgressBar = false
                                            continue
                                        }
                                    )

                                    //2.解析
                                    taskRunningDetails = "正在解析..."
                                    val newUpdateInfo =
                                        parseJsonData(File(filePath).readText())
                                    if (newUpdateInfo?.isUpdateInfoValid() == true) {
                                        viewModel.setUpdateInfo(newUpdateInfo)
                                        closeDialog = true
                                        break
                                    } else {
                                        Logger.e("解析数据出错，详情：---")
                                        delay(1000)
                                        val delayTimeSecond =
                                            Random.nextInt(timeIntervalMin, timeIntervalMax)
                                        taskRunningDetails =
                                            "解析出错了，等待${delayTimeSecond}秒再尝试..."
                                        delay(delayTimeSecond * 1000L)
                                        continue
                                    }
                                }
                                closeDialog = true
                                hideProgressBar = false
                                loadingState = false
                            }
                            //onConfirmation()
                            //onDismissRequest()
                        }, enabled = !closeDialog) {
                            Text("更新数据")
                        }
                        TextButton(onClick = {
                            scope.launch {
                                cancelJoinJob = true
                                job?.cancelAndJoin()
                                Logger.i("Leaving...")
                                cancelJoinJob = false
                                onDismissRequest()
                            }
                        }) {
                            Text("取消任务")
                        }
                    }
                }
            }
        }
        if (closeDialog) {
            onDismissRequest()
        }
    }
}

@Composable
fun ShowSettingsDialog(
    onConfirmation: () -> Unit, onDismissRequest: () -> Unit
) {
    val context = LocalContext.current

    val initTryTimes = context.getPref(PREF_TRY_TIMES, DEFAULT_VALUE_TRY_TIMES)
    val initTimeIntervalMin =
        context.getPref(PREF_TIME_INTERVAL_MIN, DEFAULT_VALUE_TIME_INTERVAL_MIN)
    val initTimeIntervalMax =
        context.getPref(PREF_TIME_INTERVAL_MAX, DEFAULT_VALUE_TIME_INTERVAL_MAX)
    var tryTimes by remember { mutableStateOf(initTryTimes.toString()) }
    var timeIntervalMin by remember { mutableStateOf(initTimeIntervalMin.toString()) }
    var timeIntervalMax by remember { mutableStateOf(initTimeIntervalMax.toString()) }

    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(Modifier.padding(top = 16.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)) {
                Text("网络参数设置", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("尝试次数")
                    TextField(
                        modifier = Modifier.onFocusChanged({ focusState ->
                            val times = tryTimes.toIntWithDefaultZero()
                            if (!focusState.isFocused && times == 0) {
                                tryTimes = DEFAULT_VALUE_TRY_TIMES.toString()
                            }
                        }),
                        value = tryTimes,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        onValueChange = {
                            val regex = "^[0-9]*$".toRegex()
                            if (it.matches(regex) && it.length <= 3) {
                                tryTimes = it
                            }
                        })
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("访问间隔(秒)")
                    TextField(
                        modifier = Modifier
                            .width(65.dp)
                            .onFocusChanged({ focusState ->
                                val min = timeIntervalMin.toIntWithDefaultZero()
                                if (!focusState.isFocused && min == 0) {
                                    timeIntervalMin = DEFAULT_VALUE_TIME_INTERVAL_MIN.toString()
                                }
                            }),
                        value = timeIntervalMin,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        onValueChange = {
                            val regex = "^[0-9]*$".toRegex()
                            if (!it.matches(regex) || it.length > 3) {
                                return@TextField
                            }
                            timeIntervalMin = it
                        })
                    Text("-", Modifier.width(10.dp))
                    TextField(
                        modifier = Modifier
                            //.fillMaxWidth(1f)
                            .onFocusChanged({ focusState ->
                                val max = timeIntervalMax.toIntWithDefaultZero()
                                if (!focusState.isFocused && max == 0) {
                                    timeIntervalMax = DEFAULT_VALUE_TIME_INTERVAL_MAX.toString()
                                }
                            }),
                        value = timeIntervalMax,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        onValueChange = {
                            val regex = "^[0-9]*$".toRegex()
                            if (!it.matches(regex) || it.length > 3) {
                                return@TextField
                            }
                            timeIntervalMax = it
                        })
                }
                Spacer(Modifier.height(24.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = {
                        val times = tryTimes.toIntWithDefaultZero()
                        val min = timeIntervalMin.toIntWithDefaultZero()
                        val max = timeIntervalMax.toIntWithDefaultZero()
                        if (times == 0 || min == 0 || max == 0) {
                            context.toast("设置错误，值不能为空或为0！")
                            return@TextButton
                        }
                        if (min > max) {
                            context.toast("访问间隔最小值应小于等于最大值！")
                            return@TextButton
                        }
                        context.setPref(PREF_TRY_TIMES, tryTimes.toInt())
                        context.setPref(PREF_TIME_INTERVAL_MIN, timeIntervalMin.toInt())
                        context.setPref(PREF_TIME_INTERVAL_MAX, timeIntervalMax.toInt())

                        onConfirmation()
                        onDismissRequest()
                    }) {
                        Text("确定")
                    }
                    TextButton(onClick = {
                        onDismissRequest()
                    }) {
                        Text("取消")
                    }
                }
            }
        }
    }
}

@Composable
fun ShowAboutDialog(onDismissRequest: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onDismissRequest() },
        title = { Text(text = stringResource(R.string.dialog_about)) },
        text = { Text(text = stringResource(R.string.app_name_version_cn)) },
        confirmButton = {
            TextButton(onClick = { onDismissRequest() }) {
                Text(stringResource(R.string.dialog_button_ok))
            }
        })
}

@Composable
fun ShowAlertDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    dialogText: String,
    icon: ImageVector,
) {
    AlertDialog(
        icon = { Icon(icon, contentDescription = "") },
        title = { Text(text = dialogTitle, style = MaterialTheme.typography.titleLarge) },
        text = { Text(text = dialogText) },
        onDismissRequest = { onDismissRequest() },
        confirmButton = {
            TextButton(onClick = { onConfirmation() }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismissRequest() }) {
                Text("Dismiss")
            }
        })
}

