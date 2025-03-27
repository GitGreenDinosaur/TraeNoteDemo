package com.harry.navigation.traenotedemo.util

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.resume

/**
 * 应用更新管理器
 * 使用GitHub作为更新服务器，实现应用的远程更新功能
 */
class UpdateManager(private val context: Context) {
    
    companion object {
        private const val TAG = "UpdateManager"
        
        // GitHub仓库信息，用户需要修改为自己的仓库信息
        private const val DEFAULT_REPO_OWNER = "your-github-username" // 修改为您的GitHub用户名
        private const val DEFAULT_REPO_NAME = "TraeNoteDemo" // 修改为您的仓库名称
        private const val DEFAULT_UPDATE_FILE = "update.json" // 更新信息文件名
        
        // 更新信息的URL
        private fun getUpdateUrl(repoOwner: String = DEFAULT_REPO_OWNER, 
                               repoName: String = DEFAULT_REPO_NAME, 
                               updateFile: String = DEFAULT_UPDATE_FILE): String {
            return "https://raw.githubusercontent.com/$repoOwner/$repoName/main/$updateFile"
        }
    }
    
    /**
     * 更新信息数据类
     */
    data class UpdateInfo(
        val versionCode: Int,
        val versionName: String,
        val updateMessage: String,
        val apkUrl: String
    )
    
    /**
     * 检查更新
     * @return 如果有更新返回更新信息，否则返回null
     */
    suspend fun checkUpdate(): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            // 获取当前应用版本
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val currentVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode
            }
            
            // 获取更新信息
            val updateUrl = getUpdateUrl()
            val updateJson = fetchUpdateInfo(updateUrl)
            
            // 解析更新信息
            val versionCode = updateJson.getInt("versionCode")
            val versionName = updateJson.getString("versionName")
            val updateMessage = updateJson.getString("updateMessage")
            val apkUrl = updateJson.getString("apkUrl")
            
            // 检查是否有更新
            if (versionCode > currentVersionCode) {
                UpdateInfo(versionCode, versionName, updateMessage, apkUrl)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "检查更新失败: ${e.message}")
            null
        }
    }
    
    /**
     * 获取更新信息
     */
    private suspend fun fetchUpdateInfo(updateUrl: String): JSONObject = withContext(Dispatchers.IO) {
        val url = URL(updateUrl)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 15000
        connection.readTimeout = 15000
        
        try {
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()
                JSONObject(response.toString())
            } else {
                throw Exception("HTTP错误: $responseCode")
            }
        } finally {
            connection.disconnect()
        }
    }
    
    /**
     * 下载更新
     * @param apkUrl APK下载地址
     * @param progressCallback 下载进度回调
     * @return 下载完成后的APK文件URI
     */
    suspend fun downloadUpdate(apkUrl: String, progressCallback: (Int) -> Unit): Uri? {
        return try {
            // 创建下载请求
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val request = DownloadManager.Request(Uri.parse(apkUrl))
                .setTitle("应用更新")
                .setDescription("正在下载新版本")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, "app-update.apk")
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
            
            // 开始下载
            val downloadId = downloadManager.enqueue(request)
            
            // 监听下载进度
            monitorDownloadProgress(downloadManager, downloadId, progressCallback)
            
            // 等待下载完成
            val uri = waitForDownloadComplete(downloadManager, downloadId)
            
            // 返回下载完成的APK文件URI
            uri
        } catch (e: Exception) {
            Log.e(TAG, "下载更新失败: ${e.message}")
            null
        }
    }
    
    /**
     * 监听下载进度
     */
    private suspend fun monitorDownloadProgress(
        downloadManager: DownloadManager,
        downloadId: Long,
        progressCallback: (Int) -> Unit
    ) = withContext(Dispatchers.IO) {
        var isDownloading = true
        while (isDownloading) {
            val query = DownloadManager.Query().setFilterById(downloadId)
            val cursor = downloadManager.query(query)
            if (cursor.moveToFirst()) {
                val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                val bytesDownloadedIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                val bytesTotalIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                
                if (statusIndex != -1 && bytesDownloadedIndex != -1 && bytesTotalIndex != -1) {
                    val status = cursor.getInt(statusIndex)
                    val bytesDownloaded = cursor.getLong(bytesDownloadedIndex)
                    val bytesTotal = cursor.getLong(bytesTotalIndex)
                    
                    when (status) {
                        DownloadManager.STATUS_SUCCESSFUL, DownloadManager.STATUS_FAILED -> {
                            isDownloading = false
                        }
                        DownloadManager.STATUS_RUNNING -> {
                            if (bytesTotal > 0) {
                                val progress = ((bytesDownloaded * 100) / bytesTotal).toInt()
                                progressCallback(progress)
                            }
                        }
                    }
                }
            }
            cursor.close()
            if (isDownloading) {
                kotlinx.coroutines.delay(500) // 每500毫秒更新一次进度
            }
        }
    }
    
    /**
     * 等待下载完成
     */
    private suspend fun waitForDownloadComplete(
        downloadManager: DownloadManager,
        downloadId: Long
    ): Uri? = suspendCancellableCoroutine { continuation ->
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id == downloadId) {
                    val query = DownloadManager.Query().setFilterById(downloadId)
                    val cursor = downloadManager.query(query)
                    if (cursor.moveToFirst()) {
                        val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                        if (statusIndex != -1) {
                            val status = cursor.getInt(statusIndex)
                            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                                val uriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                                if (uriIndex != -1) {
                                    val uriString = cursor.getString(uriIndex)
                                    val uri = Uri.parse(uriString)
                                    continuation.resume(uri)
                                } else {
                                    // 如果无法从下载管理器获取URI，则尝试从下载目录获取文件
                                    val file = File(context?.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "app-update.apk")
                                    if (file.exists()) {
                                        val fileUri =
                                            FileProvider.getUriForFile(
                                                context!!,
                                                "${context.packageName}.provider",
                                                file
                                            )
                                        continuation.resume(fileUri)
                                    } else {
                                        continuation.resume(null)
                                    }
                                }
                            } else {
                                continuation.resume(null)
                            }
                        } else {
                            continuation.resume(null)
                        }
                    } else {
                        continuation.resume(null)
                    }
                    cursor.close()
                    context?.unregisterReceiver(this)
                }
            }
        }
        
        // 注册广播接收器
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                Context.RECEIVER_NOT_EXPORTED)
        }

        // 取消时注销广播接收器
        continuation.invokeOnCancellation {
            try {
                context.unregisterReceiver(receiver)
            } catch (e: Exception) {
                // 忽略异常
            }
        }
    }
    
    /**
     * 安装APK
     */
    fun installApk(apkUri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            flags = flags or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        context.startActivity(intent)
    }
}