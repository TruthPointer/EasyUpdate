package org.tpmobile.easyupdate.util


import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import org.tpmobile.easyupdate.MyApp
import org.tpmobile.easyupdate.util.ktx.toHumanReadableSize
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

object HttpUtil {
    private val TAG = "HttpUtil"

    /////////////////////////////////////
    suspend fun downloadFile(
        context: Context,
        urlString: String,
        onProgress: ((Int, String) -> Unit)? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        // 打开连接
        try {
            onProgress?.invoke(0, "开始下载...")

            val connection = URL(urlString).openConnection() as HttpURLConnection
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.requestMethod = "GET"
            connection.connect()

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                // 转到 catch{} 部分处理了，onProgress?.invoke(-1, "下载失败，详情：服务器响应异常")
                throw kotlin.Exception(Exception("服务器响应错误: ${connection.responseCode} ${connection.responseMessage}"))
            }

            // 准备文件路径
            val fileLength = connection.contentLength
            val fileName = File(urlString).name//"download_${System.currentTimeMillis()}.dat"
            val file = File(context.cacheDir, fileName)
            Logger.i(TAG, "fileName = $fileName, fileLength = $fileLength")

            // 流操作
            connection.inputStream.use { input ->
                FileOutputStream(file).use { output ->
                    val data = ByteArray(8192) // 8KB buffer
                    var total: Long = 0
                    var count: Int

                    while (input.read(data).also { count = it } != -1 && isActive) {
                        total += count
                        output.write(data, 0, count)

                        // 计算进度并回调
                        if (fileLength > 0) {
                            val progress = (total * 100 / fileLength).toInt()
                            val detail = "${total.toHumanReadableSize()}/${fileLength.toHumanReadableSize()}"
                            onProgress?.invoke(progress, detail)
                        } else {
                            // 如果服务器没返回长度，只显示已下载大小
                            onProgress?.invoke(-2, "已下载：${total.toHumanReadableSize()}")
                        }
                    }
                }
            }
            onProgress?.invoke(100, "下载成功")
            return@withContext Result.success(file.absolutePath)
        } catch (e: Exception) {
            Logger.e(TAG, e.message ?: "错误原因不详")
            return@withContext Result.failure(Exception(e))
        }
    }

    suspend fun fakeDownloadProgress(hasError: Boolean = false, digitalProgress: Boolean = true, updateProgress: (Int, String) -> Unit): Result<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                for (i in 1..100) {
                    if(digitalProgress) {
                        updateProgress(i, "")
                    }else{
                        updateProgress(-2, "已下载：${i} KB")
                    }
                    delay(100)
                    if(hasError){
                        if(i ==50){
                            throw (Exception("self defined exception"))
                        }
                    }
                }
                return@withContext Result.success(true)
            }catch (e: Exception) {
                return@withContext Result.failure(e)
            }
        }


}