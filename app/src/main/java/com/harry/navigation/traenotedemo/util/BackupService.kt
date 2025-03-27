package com.harry.navigation.traenotedemo.util

import android.content.Context
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.harry.navigation.traenotedemo.data.model.Note
import com.harry.navigation.traenotedemo.data.repository.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

/**
 * 备份服务类
 * 负责笔记数据的备份和恢复功能
 */
class BackupService(private val context: Context, private val repository: NoteRepository) {
    
    companion object {
        private const val TAG = "BackupService"
        private const val BACKUP_FILE_PREFIX = "note_backup_"
        private const val BACKUP_FILE_EXTENSION = ".json"
        private const val ENCRYPTION_ALGORITHM = "AES"
        private const val DEFAULT_PASSWORD = "123456" // 默认密码，与PasswordScreen中的一致
    }
    
    /**
     * 备份所有笔记数据
     * @param uri 备份文件的URI
     * @param password 用于加密隐私笔记的密码，默认使用应用的密码
     * @return 备份是否成功
     */
    suspend fun backupAllNotes(uri: Uri, password: String = DEFAULT_PASSWORD): Boolean {
        return try {
            // 获取所有笔记（包括普通笔记和隐私笔记）
            val allNotes = repository.getAllNotes().first()
            val privateNotes = repository.getPrivateNotes().first()
            val nonPrivateNotes = repository.getNonPrivateNotes().first()
            
            // 创建备份JSON对象
            val backupJson = JSONObject()
            backupJson.put("timestamp", System.currentTimeMillis())
            backupJson.put("version", 1) // 备份版本号，方便后续升级
            
            // 处理普通笔记
            val normalNotesArray = JSONArray()
            for (note in nonPrivateNotes) {
                normalNotesArray.put(noteToJson(note))
            }
            backupJson.put("normal_notes", normalNotesArray)
            
            // 处理隐私笔记（加密）
            val privateNotesArray = JSONArray()
            for (note in privateNotes) {
                // 将隐私笔记转换为JSON，然后加密
                val noteJson = noteToJson(note).toString()
                val encryptedNote = encrypt(noteJson, password)
                
                val privateNoteObj = JSONObject()
                privateNoteObj.put("encrypted_data", encryptedNote)
                privateNotesArray.put(privateNoteObj)
            }
            backupJson.put("private_notes", privateNotesArray)
            
            // 写入备份文件
            withContext(Dispatchers.IO) {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    BufferedWriter(OutputStreamWriter(outputStream)).use { writer ->
                        writer.write(backupJson.toString(4)) // 使用缩进格式化JSON
                    }
                }
            }
            
            Log.d(TAG, "备份成功：${allNotes.size}条笔记（${privateNotes.size}条隐私笔记）")
            true
        } catch (e: Exception) {
            Log.e(TAG, "备份失败", e)
            false
        }
    }
    
    /**
     * 从备份文件恢复笔记数据
     * @param uri 备份文件的URI
     * @param password 用于解密隐私笔记的密码
     * @param restorePrivateNotes 是否恢复隐私笔记
     * @return 恢复是否成功
     */
    suspend fun restoreFromBackup(
        uri: Uri, 
        password: String = DEFAULT_PASSWORD,
        restorePrivateNotes: Boolean = true
    ): Boolean {
        return try {
            // 读取备份文件
            val backupContent = withContext(Dispatchers.IO) {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        reader.readText()
                    }
                } ?: throw Exception("无法读取备份文件")
            }
            
            val backupJson = JSONObject(backupContent)
            val version = backupJson.optInt("version", 1)
            
            // 恢复普通笔记
            val normalNotesArray = backupJson.getJSONArray("normal_notes")
            for (i in 0 until normalNotesArray.length()) {
                val noteJson = normalNotesArray.getJSONObject(i)
                val note = jsonToNote(noteJson)
                repository.insertNote(note)
            }
            
            // 恢复隐私笔记（如果需要）
            if (restorePrivateNotes) {
                val privateNotesArray = backupJson.getJSONArray("private_notes")
                for (i in 0 until privateNotesArray.length()) {
                    try {
                        val privateNoteObj = privateNotesArray.getJSONObject(i)
                        val encryptedData = privateNoteObj.getString("encrypted_data")
                        
                        // 解密笔记数据
                        val decryptedJson = decrypt(encryptedData, password)
                        val noteJson = JSONObject(decryptedJson)
                        val note = jsonToNote(noteJson)
                        repository.insertNote(note)
                    } catch (e: Exception) {
                        Log.e(TAG, "解密隐私笔记失败", e)
                        // 继续处理下一条笔记
                    }
                }
            }
            
            Log.d(TAG, "恢复成功：${normalNotesArray.length()}条普通笔记")
            if (restorePrivateNotes) {
                Log.d(TAG, "恢复成功：${backupJson.getJSONArray("private_notes").length()}条隐私笔记")
            }
            
            true
        } catch (e: Exception) {
            Log.e(TAG, "恢复失败", e)
            false
        }
    }
    
    /**
     * 将笔记对象转换为JSON对象
     */
    private fun noteToJson(note: Note): JSONObject {
        val jsonObject = JSONObject()
        jsonObject.put("id", note.id)
        jsonObject.put("title", note.title)
        jsonObject.put("content", note.content)
        jsonObject.put("category", note.category)
        jsonObject.put("isStarred", note.isStarred)
        jsonObject.put("createdTime", note.createdTime.time)
        jsonObject.put("updatedTime", note.updatedTime.time)
        jsonObject.put("color", note.color)
        jsonObject.put("isDeleted", note.isDeleted)
        jsonObject.put("deletedTime", note.deletedTime?.time)
        jsonObject.put("isPrivate", note.isPrivate)
        return jsonObject
    }
    
    /**
     * 将JSON对象转换为笔记对象
     */
    private fun jsonToNote(jsonObject: JSONObject): Note {
        return Note(
            id = if (jsonObject.has("id")) jsonObject.getLong("id") else 0,
            title = jsonObject.getString("title"),
            content = jsonObject.getString("content"),
            category = jsonObject.optString("category", ""),
            isStarred = jsonObject.optBoolean("isStarred", false),
            createdTime = Date(jsonObject.optLong("createdTime", System.currentTimeMillis())),
            updatedTime = Date(jsonObject.optLong("updatedTime", System.currentTimeMillis())),
            color = jsonObject.optInt("color", 0),
            isDeleted = jsonObject.optBoolean("isDeleted", false),
            deletedTime = if (jsonObject.has("deletedTime") && !jsonObject.isNull("deletedTime")) 
                            Date(jsonObject.getLong("deletedTime")) else null,
            isPrivate = jsonObject.optBoolean("isPrivate", false)
        )
    }
    
    /**
     * 生成当前时间的备份文件名
     */
    fun generateBackupFileName(): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val timestamp = dateFormat.format(Date())
        return "${BACKUP_FILE_PREFIX}${timestamp}${BACKUP_FILE_EXTENSION}"
    }
    
    /**
     * 加密字符串
     * @param data 要加密的数据
     * @param password 加密密码
     * @return 加密后的Base64字符串
     */
    private fun encrypt(data: String, password: String): String {
        val key = generateKey(password)
        val cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val encryptedBytes = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
    }
    
    /**
     * 解密字符串
     * @param encryptedData 加密后的Base64字符串
     * @param password 解密密码
     * @return 解密后的原始字符串
     */
    private fun decrypt(encryptedData: String, password: String): String {
        val key = generateKey(password)
        val cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, key)
        val encryptedBytes = Base64.decode(encryptedData, Base64.DEFAULT)
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes, Charsets.UTF_8)
    }
    
    /**
     * 从密码生成加密密钥
     */
    private fun generateKey(password: String): SecretKeySpec {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = password.toByteArray(Charsets.UTF_8)
        digest.update(bytes, 0, bytes.size)
        val key = digest.digest()
        return SecretKeySpec(key, ENCRYPTION_ALGORITHM)
    }
}