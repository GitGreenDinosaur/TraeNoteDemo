package com.harry.navigation.traenotedemo.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// 为应用创建一个DataStore实例
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * 偏好设置管理器
 * 用于管理应用的各种偏好设置，如笔记列表布局模式等
 */
class PreferencesManager(private val context: Context) {
    
    companion object {
        // 偏好设置键
        private val GRID_LAYOUT_KEY = booleanPreferencesKey("grid_layout")
        private val SHOW_SPLASH_SCREEN_KEY = booleanPreferencesKey("show_splash_screen")
        private val SPLASH_BACKGROUND_URI_KEY = stringPreferencesKey("splash_background_uri")
        private val DARK_THEME_KEY = booleanPreferencesKey("dark_theme")
        private val FOLLOW_SYSTEM_THEME_KEY = booleanPreferencesKey("follow_system_theme")
        private val LAST_UPDATE_CHECK_KEY = stringPreferencesKey("last_update_check")
    }
    
    // 获取当前布局模式（网格布局或列表布局）
    val isGridLayout: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            // 默认为列表布局（false）
            preferences[GRID_LAYOUT_KEY] ?: false
        }
    
    // 获取是否显示启动页
    val showSplashScreen: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            // 默认显示启动页（true）
            preferences[SHOW_SPLASH_SCREEN_KEY] ?: true
        }
    
    // 获取启动页背景图片URI
    val splashBackgroundUri: Flow<String?> = context.dataStore.data
        .map { preferences ->
            // 默认为null，表示使用默认背景
            preferences[SPLASH_BACKGROUND_URI_KEY]
        }
    
    // 更新布局模式
    suspend fun setGridLayout(isGrid: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[GRID_LAYOUT_KEY] = isGrid
        }
    }
    
    // 更新启动页显示设置
    suspend fun setShowSplashScreen(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SHOW_SPLASH_SCREEN_KEY] = show
        }
    }
    
    // 更新启动页背景图片URI
    suspend fun setSplashBackgroundUri(uri: String?) {
        context.dataStore.edit { preferences ->
            if (uri != null) {
                preferences[SPLASH_BACKGROUND_URI_KEY] = uri
            } else {
                preferences.remove(SPLASH_BACKGROUND_URI_KEY)
            }
        }
    }
    
    // 获取上次更新检查时间
    val lastUpdateCheck: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[LAST_UPDATE_CHECK_KEY]
        }
    
    // 设置上次更新检查时间
    suspend fun setLastUpdateCheck(timestamp: String) {
        context.dataStore.edit { preferences ->
            preferences[LAST_UPDATE_CHECK_KEY] = timestamp
        }
    }
}