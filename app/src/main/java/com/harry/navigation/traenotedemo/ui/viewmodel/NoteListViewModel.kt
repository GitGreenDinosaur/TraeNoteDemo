package com.harry.navigation.traenotedemo.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.harry.navigation.traenotedemo.data.model.Note
import com.harry.navigation.traenotedemo.data.repository.NoteRepository
import com.harry.navigation.traenotedemo.util.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date

/**
 * 笔记列表ViewModel
 */
class NoteListViewModel(private val repository: NoteRepository, private val context: Context) : ViewModel() {
    
    // 笔记列表状态
    private val _uiState = MutableStateFlow(NoteListUiState())
    val uiState: StateFlow<NoteListUiState> = _uiState
    
    // 偏好设置管理器
    private val preferencesManager = PreferencesManager(context)
    
    // 布局模式（网格布局或列表布局）
    val isGridLayout = preferencesManager.isGridLayout
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )
        
    // 是否显示启动页
    val showSplashScreen = preferencesManager.showSplashScreen
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )
        
    // 启动页背景图片URI
    val splashBackgroundUri = preferencesManager.splashBackgroundUri
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    
    // 所有非隐私笔记
    val notes = repository.getNonPrivateNotes()
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        
    // 隐私笔记
    val privateNotes = repository.getPrivateNotes()
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        
    // 回收站笔记
    val recycleBinNotes = repository.getRecycleBinNotes()
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // 标星笔记（非隐私的标星笔记）
    val starredNotes = repository.getStarredNonPrivateNotes()
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        
    // 待办笔记（非隐私的待办笔记）
    val taskNotes = repository.getNonPrivateTaskNotes()
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // 搜索笔记
    fun searchNotes(query: String) {
        viewModelScope.launch {
            repository.searchNotes(query)
                .catch { _uiState.value = _uiState.value.copy(isLoading = false, error = it.message) }
                .collect { notes ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        searchResults = notes
                    )
                }
        }
    }
    
    // 将笔记移动到回收站
    // 优化实现，避免整个列表刷新
    fun moveToRecycleBin(note: Note) {
        viewModelScope.launch {
            try {
                // 使用repository移动笔记到回收站
                // 由于我们使用Flow从数据库获取数据，数据库更新会自动触发Flow更新
                // 但Flow会发出整个列表，这会导致整个列表重新渲染
                // 我们依赖Compose的key机制和LazyListState来保持滚动位置
                repository.moveToRecycleBin(note)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    // 从回收站恢复笔记
    fun restoreFromRecycleBin(note: Note) {
        viewModelScope.launch {
            try {
                repository.restoreFromRecycleBin(note)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    // 永久删除笔记
    fun deleteNotePermanently(note: Note) {
        viewModelScope.launch {
            try {
                repository.deleteNotePermanently(note)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    // 切换笔记标星状态
    fun toggleStarStatus(note: Note) {
        viewModelScope.launch {
            try {
                val updatedNote = note.copy(isStarred = !note.isStarred)
                repository.updateNote(updatedNote)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    // 切换笔记隐私状态
    fun togglePrivateStatus(note: Note) {
        viewModelScope.launch {
            try {
                repository.togglePrivateStatus(note)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    // 切换笔记待办状态
    fun toggleTaskStatus(note: Note) {
        viewModelScope.launch {
            try {
                repository.toggleTaskStatus(note)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    // 更新笔记提醒时间
    fun updateReminderTime(note: Note, reminderTime: Date?) {
        viewModelScope.launch {
            try {
                repository.updateReminderTime(note, reminderTime)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    // 更新密码验证状态
    fun updatePasswordVerification(isVerified: Boolean) {
        _uiState.value = _uiState.value.copy(isPasswordVerified = isVerified)
    }
    
    // 按分类获取笔记
    fun getNotesByCategory(category: String) {
        viewModelScope.launch {
            repository.getNotesByCategory(category)
                .catch { _uiState.value = _uiState.value.copy(isLoading = false, error = it.message) }
                .collect { notes ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        categoryNotes = notes
                    )
                }
        }
    }
    
    // 切换布局模式
    fun toggleGridLayout() {
        viewModelScope.launch {
            preferencesManager.setGridLayout(!isGridLayout.value)
        }
    }
    
    // 切换启动页显示状态
    fun toggleSplashScreen() {
        viewModelScope.launch {
            preferencesManager.setShowSplashScreen(!showSplashScreen.value)
        }
    }
    
    // 更新启动页背景图片URI
    fun updateSplashBackgroundUri(uri: String?) {
        viewModelScope.launch {
            try {
                // 验证URI的有效性
                if (uri != null) {
                    // 尝试解析URI，如果无效会抛出异常
                    Uri.parse(uri)
                }
                // 保存URI到偏好设置
                preferencesManager.setSplashBackgroundUri(uri)
            } catch (e: Exception) {
                // URI无效，记录错误并保存为null
                println("无效的URI: ${e.message}")
                preferencesManager.setSplashBackgroundUri(null)
            }
        }
    }
}

/**
 * 笔记列表UI状态
 */
data class NoteListUiState(
    val isLoading: Boolean = false,
    val searchResults: List<Note> = emptyList(),
    val categoryNotes: List<Note> = emptyList(),
    val error: String? = null,
    val isPasswordVerified: Boolean = false,
    val passwordAttempts: Int = 0
)

/**
 * ViewModel工厂
 */
class NoteListViewModelFactory(private val repository: NoteRepository, private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NoteListViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}