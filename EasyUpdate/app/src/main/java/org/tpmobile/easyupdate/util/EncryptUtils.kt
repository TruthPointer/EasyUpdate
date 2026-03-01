package org.tpmobile.easyupdate.util

import android.content.pm.PackageManager
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tpmobile.easyupdate.MyApp
import org.tpmobile.easyupdate.util.ktx.toHex
import org.tpmobile.easyupdate.util.ktx.toHumanReadableSize
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object EncryptUtils {
    private const val TAG = "EncryptUtils"

    suspend fun computeFileSha512(
        file: File,
        onProgress: ((Int, String) -> Unit)? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val md = MessageDigest.getInstance("SHA-512")
            val fis = FileInputStream(file)
            val fileLength = file.length()
            var processCount = 0
            var lastProgress = 0
            val buffer = ByteArray(8192)
            var length: Int
            while (fis.read(buffer).also { length = it } != -1) {
                md.update(buffer, 0, length)
                processCount += length
                val progress = (processCount * 100 / fileLength).toInt()
                if (progress > lastProgress) {
                    val detail =
                        "${processCount.toHumanReadableSize()}/${fileLength.toHumanReadableSize()}"
                    onProgress?.invoke(progress, detail)
                }
                lastProgress = progress
            }
            fis.close()
            Result.success(md.digest().toHex())
        } catch (e: IOException) {
            println(e)
            Result.failure(e)
        } catch (e: NoSuchAlgorithmException) {
            println(e)
            Result.failure(e)
        }
    }

    private fun encryptAlgorithm(data: ByteArray, algorithm: String = "SHA-256"): String {
        return try {
            val md = MessageDigest.getInstance(algorithm)
            md.update(data)
            md.digest().toHex()
        } catch (e: NoSuchAlgorithmException) {
            Logger.e(TAG, e.message ?: "未知原因")
            ""
        }
    }

    fun getSha256OfApkSignature(apkPath: String): String {
        val bytes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            MyApp.appContext.packageManager.getPackageArchiveInfo(
                apkPath,
                PackageManager.PackageInfoFlags.of(PackageManager.GET_SIGNING_CERTIFICATES.toLong())
            )?.signatures?.get(0)?.toByteArray()
        } else {
            MyApp.appContext.packageManager.getPackageArchiveInfo(
                apkPath,
                PackageManager.GET_SIGNATURES
            )?.signatures?.get(0)?.toByteArray()
        }

        if (bytes == null || bytes.isEmpty()) {
            println("sha256=, SIG_SEC=$SIG_SEC")
            return ""
        }
        val sha256 = encryptAlgorithm(bytes)
        Logger.i(TAG, "sha256=$sha256, SIG_SEC=$SIG_SEC")
        return sha256
    }

    fun isMySignature(apkPath: String): Boolean = getSha256OfApkSignature(apkPath) == SIG_SEC

}
