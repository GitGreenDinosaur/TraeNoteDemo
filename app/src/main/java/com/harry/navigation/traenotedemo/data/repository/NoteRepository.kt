package com.harry.navigation.traenotedemo.data.repository

import com.harry.navigation.traenotedemo.data.dao.NoteDao
import com.harry.navigation.traenotedemo.data.model.Note
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import java.util.Date

/**
 * 笔记仓库
 */
class NoteRepository(private val noteDao: NoteDao) {
    
    // 获取所有笔记
    fun getAllNotes(): Flow<List<Note>> = noteDao.getAllNotes()
    
    // 根据ID获取笔记
    fun getNoteById(id: Long): Flow<Note> = noteDao.getNoteById(id)
    
    // 搜索笔记
    fun searchNotes(query: String): Flow<List<Note>> = noteDao.searchNotes(query)
    
    // 获取指定分类的笔记
    fun getNotesByCategory(category: String): Flow<List<Note>> = noteDao.getNotesByCategory(category)
    
    // 获取标星笔记
    fun getStarredNotes(): Flow<List<Note>> = noteDao.getStarredNotes()
    
    // 获取标星的非隐私笔记
    fun getStarredNonPrivateNotes(): Flow<List<Note>> = noteDao.getStarredNonPrivateNotes()
    
    // 获取隐私笔记
    fun getPrivateNotes(): Flow<List<Note>> = noteDao.getPrivateNotes()
    
    // 获取非隐私笔记
    fun getNonPrivateNotes(): Flow<List<Note>> = noteDao.getNonPrivateNotes()
    
    // 获取待办笔记
    fun getTaskNotes(): Flow<List<Note>> = noteDao.getTaskNotes()
    
    // 获取非隐私的待办笔记
    fun getNonPrivateTaskNotes(): Flow<List<Note>> = noteDao.getNonPrivateTaskNotes()
    
    // 插入笔记
    suspend fun insertNote(note: Note): Long = noteDao.insertNote(note)
    
    // 更新笔记
    suspend fun updateNote(note: Note) = noteDao.updateNote(note)
    
    // 切换笔记隐私状态
    suspend fun togglePrivateStatus(note: Note) {
        val updatedNote = note.copy(isPrivate = !note.isPrivate)
        noteDao.updateNote(updatedNote)
    }
    
    // 切换笔记待办状态
    suspend fun toggleTaskStatus(note: Note) {
        val updatedNote = note.copy(isTask = !note.isTask)
        noteDao.updateNote(updatedNote)
    }
    
    // 更新笔记提醒时间
    suspend fun updateReminderTime(note: Note, reminderTime: Date?) {
        val updatedNote = note.copy(reminderTime = reminderTime)
        noteDao.updateNote(updatedNote)
    }
    
    // 将笔记移动到回收站（软删除）
    suspend fun moveToRecycleBin(note: Note) {
        val recycleBinNote = note.copy(isDeleted = true, deletedTime = Date())
        noteDao.moveToRecycleBin(recycleBinNote)
    }
    
    // 从回收站恢复笔记
    suspend fun restoreFromRecycleBin(note: Note) {
        val restoredNote = note.copy(isDeleted = false, deletedTime = null)
        noteDao.updateNote(restoredNote)
    }
    
    // 永久删除笔记
    suspend fun deleteNotePermanently(note: Note) = noteDao.deleteNotePermanently(note)
    
    // 获取回收站中的笔记
    fun getRecycleBinNotes(): Flow<List<Note>> = noteDao.getRecycleBinNotes()
    
    // 清理回收站（删除超过7天的笔记）
    suspend fun cleanRecycleBin() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -7) // 7天前的日期
        val dateToDelete = calendar.time
        
        noteDao.getNotesToDelete(dateToDelete).collect { notes ->
            notes.forEach { note ->
                deleteNotePermanently(note)
            }
        }
    }
}