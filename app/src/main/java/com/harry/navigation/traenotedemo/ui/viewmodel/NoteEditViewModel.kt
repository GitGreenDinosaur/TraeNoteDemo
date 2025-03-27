package com.harry.navigation.traenotedemo.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.harry.navigation.traenotedemo.data.model.Note
import com.harry.navigation.traenotedemo.data.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date

/**
 * 笔记编辑ViewModel
 */
class NoteEditViewModel(
    private val repository: NoteRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val noteId: Long = savedStateHandle["noteId"] ?: -1L
    
    // UI状态
    private val _uiState = MutableStateFlow(NoteEditUiState())
    val uiState: StateFlow<NoteEditUiState> = _uiState.asStateFlow()
    
    init {
        if (noteId != -1L) {
            loadNote(noteId)
        }
    }
    
    // 加载笔记
    private fun loadNote(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getNoteById(id)
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message
                        )
                    }
                }
                .collect { note ->
                    // 计算初始字数
                    val initialWordCount = calculateWordCount(note.content)
                    
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            note = note,
                            title = note.title,
                            content = note.content,
                            category = note.category,
                            isStarred = note.isStarred,
                            color = note.color,
                            isTask = note.isTask,
                            reminderTime = note.reminderTime,
                            wordCount = initialWordCount
                        )
                    }
                }
        }
    }
    
    // 更新标题
    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title) }
    }
    
    // 计算字数（简单实现，按空格分割计算单词数，中文则按字符数计算）
    private fun calculateWordCount(content: String): Int {
        return if (content.isEmpty()) {
            0
        } else {
            // 对于中文内容，直接计算字符数
            // 对于英文内容和数字，按空格分割计算单词数
            if (content.any { it.code > 127 }) { // 包含非ASCII字符，可能是中文
                content.length
            } else {
                // 修正正则表达式，使用Regex对象而不是字符串
                // 这样可以正确处理数字和英文混合的情况
                content.split(Regex("\\s+")).filter { it.isNotEmpty() }.size
            }
        }
    }
    
    // 更新内容
    fun updateContent(content: String) {
        val wordCount = calculateWordCount(content)
        _uiState.update { it.copy(content = content, wordCount = wordCount) }
    }
    
    // 更新分类
    fun updateCategory(category: String) {
        _uiState.update { it.copy(category = category) }
    }
    
    // 切换标星状态
    fun toggleStarred() {
        _uiState.update { it.copy(isStarred = !it.isStarred) }
    }
    
    // 更新颜色
    fun updateColor(color: Int) {
        _uiState.update { it.copy(color = color) }
    }
    
    // 切换待办状态
    fun toggleTaskStatus() {
        _uiState.update { it.copy(isTask = !it.isTask) }
    }
    
    // 更新提醒时间
    fun updateReminderTime(reminderTime: Date?) {
        _uiState.update { it.copy(reminderTime = reminderTime) }
    }
    
    // 保存笔记
    fun saveNote() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isSaving = true) }
                
                val currentTime = Date()
                val note = if (noteId != -1L) {
                    // 更新现有笔记
                    _uiState.value.note?.copy(
                        title = _uiState.value.title,
                        content = _uiState.value.content,
                        category = _uiState.value.category,
                        isStarred = _uiState.value.isStarred,
                        updatedTime = currentTime,
                        color = _uiState.value.color,
                        isTask = _uiState.value.isTask,
                        reminderTime = _uiState.value.reminderTime
                    )
                } else {
                    // 创建新笔记
                    Note(
                        title = _uiState.value.title,
                        content = _uiState.value.content,
                        category = _uiState.value.category,
                        isStarred = _uiState.value.isStarred,
                        createdTime = currentTime,
                        updatedTime = currentTime,
                        color = _uiState.value.color,
                        isTask = _uiState.value.isTask,
                        reminderTime = _uiState.value.reminderTime
                    )
                }
                
                note?.let {
                    if (noteId != -1L) {
                        repository.updateNote(it)
                    } else {
                        repository.insertNote(it)
                    }
                    _uiState.update { state ->
                        state.copy(
                            isSaving = false,
                            isNoteSaved = true
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = e.message
                    )
                }
            }
        }
    }
}

/**
 * 笔记编辑UI状态
 */
data class NoteEditUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isNoteSaved: Boolean = false,
    val note: Note? = null,
    val title: String = "",
    val content: String = "",
    val category: String = "",
    val isStarred: Boolean = false,
    val color: Int = 0,
    val isTask: Boolean = false,
    val reminderTime: Date? = null,
    val error: String? = null,
    val wordCount: Int = 0 // 添加字数统计字段
)

/**
 * ViewModel工厂
 */
class NoteEditViewModelFactory(
    private val repository: NoteRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteEditViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NoteEditViewModel(repository, savedStateHandle) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}