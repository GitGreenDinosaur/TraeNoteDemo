package com.harry.navigation.traenotedemo.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * 笔记实体类
 */
@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val category: String = "", // 笔记分类
    val isStarred: Boolean = false, // 是否标星
    val createdTime: Date = Date(), // 创建时间
    val updatedTime: Date = Date(), // 更新时间
    val color: Int = 0, // 笔记颜色
    val isDeleted: Boolean = false, // 是否已删除（在回收站中）
    val deletedTime: Date? = null, // 删除时间（用于计算在回收站中的保存时间）
    val isPrivate: Boolean = false, // 是否为隐私笔记
    val isTask: Boolean = false, // 是否为待办事项
    val reminderTime: Date? = null // 提醒时间
)