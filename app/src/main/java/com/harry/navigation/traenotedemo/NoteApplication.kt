package com.harry.navigation.traenotedemo

import android.app.Application
import com.harry.navigation.traenotedemo.data.database.NoteDatabase
import com.harry.navigation.traenotedemo.data.repository.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 应用程序类
 * 用于初始化数据库和提供全局访问点
 */
class NoteApplication : Application() {
    // 懒加载数据库实例
    private val database by lazy { NoteDatabase.getDatabase(this) }
    
    // 懒加载笔记仓库实例
    val repository by lazy { NoteRepository(database.noteDao()) }
    
    override fun onCreate() {
        super.onCreate()
        // 在应用启动时清理回收站中超过7天的笔记
        cleanRecycleBin()
    }
    
    // 清理回收站中超过7天的笔记
    private fun cleanRecycleBin() {
        kotlinx.coroutines.GlobalScope.launch(Dispatchers.IO) {
            repository.cleanRecycleBin()
        }
    }
}