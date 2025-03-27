package com.harry.navigation.traenotedemo.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * 图片缓存管理器
 * 用于处理图片的缓存和读取操作
 */
class ImageCacheManager(private val context: Context) {
    
    companion object {
        private const val TAG = "ImageCacheManager"
        private const val SPLASH_IMAGE_FILENAME = "splash01.png"
        
        /**
         * 获取缓存的启动页图片文件
         * @param context 上下文
         * @return 缓存的图片文件，如果不存在则返回null
         */
        fun getCachedSplashImageFile(context: Context): File? {
            val cacheDir = context.cacheDir
            val imageFile = File(cacheDir, SPLASH_IMAGE_FILENAME)
            return if (imageFile.exists()) imageFile else null
        }
        
        /**
         * 获取缓存的启动页图片URI
         * @param context 上下文
         * @return 缓存的图片URI字符串，如果不存在则返回null
         */
        fun getCachedSplashImageUri(context: Context): String? {
            val imageFile = getCachedSplashImageFile(context)
            return imageFile?.let { "file://${it.absolutePath}" }
        }
    }
    
    /**
     * 将选择的图片复制到缓存目录
     * @param imageUri 图片URI
     * @return 是否复制成功
     */
    fun copyImageToCache(imageUri: Uri): Boolean {
        try {
            // 打开输入流读取图片
            val inputStream = context.contentResolver.openInputStream(imageUri)
            if (inputStream == null) {
                Log.e(TAG, "无法打开图片输入流")
                return false
            }
            
            // 解码图片
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            
            if (bitmap == null) {
                Log.e(TAG, "无法解码图片")
                return false
            }
            
            // 获取缓存目录
            val cacheDir = context.cacheDir
            val imageFile = File(cacheDir, SPLASH_IMAGE_FILENAME)
            
            // 如果文件已存在，先删除
            if (imageFile.exists()) {
                imageFile.delete()
            }
            
            // 创建输出流并保存图片
            val outputStream = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            
            // 释放Bitmap资源
            bitmap.recycle()
            
            Log.d(TAG, "图片已成功复制到缓存: ${imageFile.absolutePath}")
            return true
        } catch (e: IOException) {
            Log.e(TAG, "复制图片到缓存失败: ${e.message}")
            return false
        } catch (e: Exception) {
            Log.e(TAG, "处理图片时发生错误: ${e.message}")
            return false
        }
    }
    
    /**
     * 清除缓存的启动页图片
     * @return 是否清除成功
     */
    fun clearCachedSplashImage(): Boolean {
        val cacheDir = context.cacheDir
        val imageFile = File(cacheDir, SPLASH_IMAGE_FILENAME)
        return if (imageFile.exists()) {
            imageFile.delete()
        } else {
            true // 文件不存在，视为清除成功
        }
    }
}