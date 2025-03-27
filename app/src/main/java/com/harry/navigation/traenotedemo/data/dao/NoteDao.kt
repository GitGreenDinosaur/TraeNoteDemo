package com.harry.navigation.traenotedemo.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.harry.navigation.traenotedemo.data.model.Note
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * 笔记DAO接口
 */
@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note): Long
    
    @Update
    suspend fun updateNote(note: Note)
    
    // 软删除笔记（移动到回收站）
    @Update
    suspend fun moveToRecycleBin(note: Note)
    
    // 永久删除笔记
    @Delete
    suspend fun deleteNotePermanently(note: Note)
    
    @Query("SELECT * FROM notes WHERE isDeleted = 0 AND isTask = 0 ORDER BY updatedTime DESC")
    fun getAllNotes(): Flow<List<Note>>
    
    @Query("SELECT * FROM notes WHERE id = :noteId")
    fun getNoteById(noteId: Long): Flow<Note>
    
    // 获取回收站中的笔记
    @Query("SELECT * FROM notes WHERE isDeleted = 1 ORDER BY deletedTime DESC")
    fun getRecycleBinNotes(): Flow<List<Note>>
    
    // 获取需要永久删除的笔记（在回收站中超过7天的笔记）
    @Query("SELECT * FROM notes WHERE isDeleted = 1 AND deletedTime <= :date")
    fun getNotesToDelete(date: Date): Flow<List<Note>>
    
    @Query("SELECT * FROM notes WHERE isDeleted = 0 AND isTask = 0 AND (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%')")
    fun searchNotes(query: String): Flow<List<Note>>
    
    @Query("SELECT * FROM notes WHERE isDeleted = 0 AND category = :category ORDER BY updatedTime DESC")
    fun getNotesByCategory(category: String): Flow<List<Note>>
    
    @Query("SELECT * FROM notes WHERE isDeleted = 0 AND isStarred = 1 ORDER BY updatedTime DESC")
    fun getStarredNotes(): Flow<List<Note>>
    
    // 获取隐私笔记
    @Query("SELECT * FROM notes WHERE isDeleted = 0 AND isPrivate = 1 ORDER BY updatedTime DESC")
    fun getPrivateNotes(): Flow<List<Note>>
    
    // 获取非隐私笔记
    @Query("SELECT * FROM notes WHERE isDeleted = 0 AND isPrivate = 0 AND isTask = 0 ORDER BY updatedTime DESC")
    fun getNonPrivateNotes(): Flow<List<Note>>
    
    // 获取标星的非隐私笔记
    @Query("SELECT * FROM notes WHERE isDeleted = 0 AND isPrivate = 0 AND isStarred = 1 AND isTask = 0 ORDER BY updatedTime DESC")
    fun getStarredNonPrivateNotes(): Flow<List<Note>>
    
    // 获取待办笔记
    @Query("SELECT * FROM notes WHERE isDeleted = 0 AND isTask = 1 ORDER BY reminderTime ASC, updatedTime DESC")
    fun getTaskNotes(): Flow<List<Note>>
    
    // 获取非隐私的待办笔记
    @Query("SELECT * FROM notes WHERE isDeleted = 0 AND isPrivate = 0 AND isTask = 1 ORDER BY reminderTime ASC, updatedTime DESC")
    fun getNonPrivateTaskNotes(): Flow<List<Note>>
}