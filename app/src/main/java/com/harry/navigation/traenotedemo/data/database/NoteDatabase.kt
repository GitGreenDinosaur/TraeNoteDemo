package com.harry.navigation.traenotedemo.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.harry.navigation.traenotedemo.data.dao.NoteDao
import com.harry.navigation.traenotedemo.data.model.Note
import com.harry.navigation.traenotedemo.util.DateConverter

/**
 * 笔记数据库
 */
@Database(entities = [Note::class], version = 4, exportSchema = false)
@TypeConverters(DateConverter::class)
abstract class NoteDatabase : RoomDatabase() {
    
    abstract fun noteDao(): NoteDao
    
    companion object {
        @Volatile
        private var INSTANCE: NoteDatabase? = null
        
        // 版本1到版本2的迁移：添加isDeleted和deletedTime列
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 添加isDeleted列，默认值为0（false）
                database.execSQL("ALTER TABLE notes ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
                // 添加deletedTime列，允许为null
                database.execSQL("ALTER TABLE notes ADD COLUMN deletedTime INTEGER")
            }
        }
        
        // 版本2到版本3的迁移：添加isPrivate列
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 添加isPrivate列，默认值为0（false）
                database.execSQL("ALTER TABLE notes ADD COLUMN isPrivate INTEGER NOT NULL DEFAULT 0")
            }
        }
        
        // 版本3到版本4的迁移：添加isTask和reminderTime列
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 添加isTask列，默认值为0（false）
                database.execSQL("ALTER TABLE notes ADD COLUMN isTask INTEGER NOT NULL DEFAULT 0")
                // 添加reminderTime列，允许为null
                database.execSQL("ALTER TABLE notes ADD COLUMN reminderTime INTEGER")
            }
        }
        
        fun getDatabase(context: Context): NoteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NoteDatabase::class.java,
                    "note_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
//                .addMigration(MIGRATION_1_2)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}