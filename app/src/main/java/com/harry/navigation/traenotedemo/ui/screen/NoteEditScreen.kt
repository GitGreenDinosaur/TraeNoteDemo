package com.harry.navigation.traenotedemo.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.harry.navigation.traenotedemo.ui.viewmodel.NoteEditViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 笔记编辑屏幕
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun NoteEditScreen(
    viewModel: NoteEditViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }

    // 键盘控制器
    val keyboardController = LocalSoftwareKeyboardController.current

    // 添加键盘状态监听
    var isKeyboardVisible by remember { mutableStateOf(false) }

    // 添加撤销和恢复状态
    var canUndo by remember { mutableStateOf(false) }
    var canRedo by remember { mutableStateOf(false) }

    // 文本历史记录 - 使用TextFieldValue来跟踪光标位置
    val titleFieldValue = remember { mutableStateOf(TextFieldValue("")) }
    val contentFieldValue = remember { mutableStateOf(TextFieldValue("")) }
    val titleHistory = remember { mutableStateListOf<TextFieldValue>() }
    val contentHistory = remember { mutableStateListOf<TextFieldValue>() }
    var titleHistoryIndex = remember { mutableStateOf(-1) }
    var contentHistoryIndex = remember { mutableStateOf(-1) }

    // 处理返回操作的函数
    fun handleBackNavigation() {
        // 隐藏键盘
        keyboardController?.hide()

        // 如果标题或内容不为空，则保存笔记
        if (uiState.title.isNotEmpty() || uiState.content.isNotEmpty()) {
            viewModel.saveNote()
            // 注意：不需要在这里调用onNavigateBack，因为在LaunchedEffect中已经监听了isNoteSaved状态
        } else {
            // 如果笔记为空，直接返回
            onNavigateBack()
        }
    }

    // 处理系统返回键
    BackHandler {
        handleBackNavigation()
    }

    // 更新撤销和恢复状态的函数
    fun updateUndoRedoState() {
        canUndo = titleHistoryIndex.value > 0 || contentHistoryIndex.value > 0
        canRedo =
            titleHistoryIndex.value < titleHistory.size - 1 || contentHistoryIndex.value < contentHistory.size - 1
    }

    // 初始化历史记录
    LaunchedEffect(uiState.title, uiState.content) {
        if (titleHistory.isEmpty() && uiState.title.isNotEmpty()) {
            val initialTitleValue = TextFieldValue(uiState.title)
            titleFieldValue.value = initialTitleValue
            titleHistory.add(initialTitleValue)
            titleHistoryIndex.value = 0
        }
        if (contentHistory.isEmpty() && uiState.content.isNotEmpty()) {
            val initialContentValue = TextFieldValue(uiState.content)
            contentFieldValue.value = initialContentValue
            contentHistory.add(initialContentValue)
            contentHistoryIndex.value = 0
        }
    }

    // 监听标题变化并更新历史记录
    LaunchedEffect(titleFieldValue.value.text) {
        if (titleHistory.isEmpty() || titleFieldValue.value.text != titleHistory.lastOrNull()?.text) {
            // 添加新的历史记录，不再清除后面的历史记录
            if (titleFieldValue.value.text.isNotEmpty()) {
                // 如果当前不是在最后一个索引，则添加新的历史记录
                if (titleHistoryIndex.value == titleHistory.lastIndex) {
                    titleHistory.add(titleFieldValue.value)
                    titleHistoryIndex.value = titleHistory.lastIndex
                }
            }

            // 更新撤销状态，不影响恢复状态
            canUndo = titleHistoryIndex.value > 0 || contentHistoryIndex.value > 0

            // 同步到ViewModel
            viewModel.updateTitle(titleFieldValue.value.text)
        }
    }

    // 监听内容变化并更新历史记录
    LaunchedEffect(contentFieldValue.value.text) {
        if (contentHistory.isEmpty() || contentFieldValue.value.text != contentHistory.lastOrNull()?.text) {
            // 添加新的历史记录，不再清除后面的历史记录
            if (contentFieldValue.value.text.isNotEmpty()) {
                // 如果当前不是在最后一个索引，则添加新的历史记录
                if (contentHistoryIndex.value == contentHistory.lastIndex) {
                    contentHistory.add(contentFieldValue.value)
                    contentHistoryIndex.value = contentHistory.lastIndex
                }
            }

            // 更新撤销状态，不影响恢复状态
            canUndo = titleHistoryIndex.value > 0 || contentHistoryIndex.value > 0

            // 同步到ViewModel
            viewModel.updateContent(contentFieldValue.value.text)
        }
    }



    // 撤销操作
    fun undo() {
        var didUndo = false

        // 优先撤销内容，如果内容没有可撤销的，再撤销标题
        if (contentHistoryIndex.value > 0) {
            contentHistoryIndex.value--
            // 直接使用历史记录中的TextFieldValue，保留光标位置
            contentFieldValue.value = contentHistory[contentHistoryIndex.value]
            viewModel.updateContent(contentFieldValue.value.text)
            didUndo = true
        } else if (titleHistoryIndex.value > 0) {
            titleHistoryIndex.value--
            // 直接使用历史记录中的TextFieldValue，保留光标位置
            titleFieldValue.value = titleHistory[titleHistoryIndex.value]
            viewModel.updateTitle(titleFieldValue.value.text)
            didUndo = true
        }

        // 更新状态
        if (didUndo) {
            // 更新撤销和恢复状态
            updateUndoRedoState()
        }
    }

    // 恢复操作
    fun redo() {
        var didRedo = false

        // 优先恢复内容，如果内容没有可恢复的，再恢复标题
        if (contentHistoryIndex.value < contentHistory.size - 1) {
            contentHistoryIndex.value++
            // 直接使用历史记录中的TextFieldValue，保留光标位置
            contentFieldValue.value = contentHistory[contentHistoryIndex.value]
            viewModel.updateContent(contentFieldValue.value.text)
            didRedo = true
        } else if (titleHistoryIndex.value < titleHistory.size - 1) {
            titleHistoryIndex.value++
            // 直接使用历史记录中的TextFieldValue，保留光标位置
            titleFieldValue.value = titleHistory[titleHistoryIndex.value]
            viewModel.updateTitle(titleFieldValue.value.text)
            didRedo = true
        }

        // 更新状态
        if (didRedo) {
            // 更新撤销和恢复状态
            updateUndoRedoState()
        }
    }

    // 预定义的笔记颜色
    val noteColors = listOf(
        Color(0xFFFFFFFF), // 白色
        Color(0xFFF28B82), // 红色
        Color(0xFFFBBC04), // 黄色
        Color(0xFFFFF475), // 浅黄
        Color(0xFFCBFF90), // 绿色
        Color(0xFFA7FFEB), // 青色
        Color(0xFFCBF0F8), // 浅蓝
        Color(0xFFAECBFA), // 蓝色
        Color(0xFFD7AEFB), // 紫色
        Color(0xFFFDCFE8)  // 粉色
    )

    // 预定义的笔记分类
    val categories = listOf("工作", "学习", "生活", "旅行", "购物", "其他")

    // 当笔记保存成功时，返回上一页
    LaunchedEffect(uiState.isNoteSaved) {
        if (uiState.isNoteSaved) {
            onNavigateBack()
        }
    }

    // 显示错误信息
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(message = "错误: $it")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { /* 移除标题显示 */ },
                navigationIcon = {
                    IconButton(onClick = { handleBackNavigation() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    // 撤销和恢复按钮
                    IconButton(
                        onClick = { undo() },
                        enabled = canUndo
                    ) {
                        Icon(
                            imageVector = Icons.Default.Undo,
                            contentDescription = "撤销",
                            tint = if (canUndo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(
                                alpha = 0.3f
                            )
                        )
                    }

                    IconButton(
                        onClick = { redo() },
                        enabled = canRedo
                    ) {
                        Icon(
                            imageVector = Icons.Default.Redo,
                            contentDescription = "恢复",
                            tint = if (canRedo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(
                                alpha = 0.3f
                            )
                        )
                    }

                    // 颜色选择按钮
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(noteColors[uiState.color])
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                CircleShape
                            )
                            .clickable { showColorPicker = !showColorPicker }
                    )

                    // 分类选择按钮
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clickable { showCategoryDialog = !showCategoryDialog }
                    ) {
                        Text(
                            text = uiState.category.ifEmpty { "分类" },
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // 标星按钮
                    IconButton(onClick = { viewModel.toggleStarred() }) {
                        Icon(
                            imageVector = if (uiState.isStarred) Icons.Default.Star else Icons.Outlined.StarOutline,
                            contentDescription = if (uiState.isStarred) "取消标星" else "标星",
                            tint = if (uiState.isStarred) Color(0xFFFFC107) else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // 待办按钮
                    IconButton(onClick = { viewModel.toggleTaskStatus() }) {
                        Icon(
                            imageVector = if (uiState.isTask) Icons.Default.CheckCircle else Icons.Outlined.CheckCircleOutline,
                            contentDescription = if (uiState.isTask) "取消待办" else "设为待办",
                            tint = if (uiState.isTask) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )

            // 颜色选择器下拉面板
            if (showColorPicker) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 56.dp)
                ) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        items(noteColors.indices.toList()) { index ->
                            Box(
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(noteColors[index])
                                    .clickable {
                                        viewModel.updateColor(index)
                                        showColorPicker = false
                                    }
                                    .then(
                                        if (index == uiState.color) {
                                            Modifier.border(
                                                width = 2.dp,
                                                color = MaterialTheme.colorScheme.primary,
                                                shape = CircleShape
                                            )
                                        } else Modifier
                                    )
                            )
                        }
                    }
                }
            }

            // 分类选择器下拉面板
            if (showCategoryDialog) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 56.dp)
                ) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        items(categories) { category ->
                            Box(
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .clip(MaterialTheme.shapes.small)
                                    .background(
                                        if (category == uiState.category)
                                            MaterialTheme.colorScheme.primaryContainer
                                        else
                                            MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable {
                                        viewModel.updateCategory(category)
                                        showCategoryDialog = false
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = category,
                                    color = if (category == uiState.category)
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.saveNote() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Check, contentDescription = "保存")
                }
            }
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(noteColors[uiState.color].copy(alpha = 0.1f))
                    .verticalScroll(scrollState)
            ) {
                // 标题输入框 - 无边框样式
                TextField(
                    value = titleFieldValue.value,
                    onValueChange = { newValue ->
                        // 记录历史记录
                        if (titleFieldValue.value.text != newValue.text) {
                            // 添加新的历史记录，不再清除后面的历史记录
                            if (titleHistoryIndex.value == titleHistory.lastIndex) {
                                titleFieldValue.value = newValue
                                titleHistory.add(newValue)
                                titleHistoryIndex.value = titleHistory.lastIndex
                            } else {
                                titleFieldValue.value = newValue
                            }

                            // 更新撤销状态，不影响恢复状态
                            canUndo = titleHistoryIndex.value > 0 || contentHistoryIndex.value > 0

                            // 同步到ViewModel
                            viewModel.updateTitle(newValue.text)
                        } else {
                            // 仅更新光标位置
                            titleFieldValue.value = newValue
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .onFocusChanged { focusState ->
                            isKeyboardVisible = focusState.isFocused
                        },
                    placeholder = { Text("请输入标题") },
                    singleLine = true,
                    colors = androidx.compose.material3.TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    textStyle = MaterialTheme.typography.titleLarge
                )
                
                // 这里不再需要功能栏，因为已经将功能按钮移回到TopAppBar中
                // 添加创建时间和字数统计显示
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 创建时间显示
                    uiState.note?.createdTime?.let { createdTime ->
                        Text(
                            text = "创建于: ${formatDate(createdTime)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // 字数统计显示
                    Text(
                        text = "字数: ${uiState.wordCount}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // 内容输入框 - 无边框样式，扩展到全屏
                TextField(
                    value = contentFieldValue.value,
                    onValueChange = { newValue ->
                        // 记录历史记录
                        if (contentFieldValue.value.text != newValue.text) {
                            // 添加新的历史记录，不再清除后面的历史记录
                            if (contentHistoryIndex.value == contentHistory.lastIndex) {
                                contentFieldValue.value = newValue
                                contentHistory.add(newValue)
                                contentHistoryIndex.value = contentHistory.lastIndex
                            } else {
                                contentFieldValue.value = newValue
                            }

                            // 更新撤销状态，不影响恢复状态
                            canUndo = titleHistoryIndex.value > 0 || contentHistoryIndex.value > 0

                            // 同步到ViewModel
                            viewModel.updateContent(newValue.text)
                        } else {
                            // 仅更新光标位置
                            contentFieldValue.value = newValue
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                        .onFocusChanged { focusState ->
                            isKeyboardVisible = focusState.isFocused
                        },
                    placeholder = { Text("请输入内容") },
                    colors = androidx.compose.material3.TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
                

            }
        }
    }
    
    /**
     * 格式化日期
     */
    fun formatDate(date: Date): String {
        val now = Date()
        val diff = now.time - date.time

        return when {
            diff < 60 * 60 * 1000 -> "刚刚"
            diff < 24 * 60 * 60 * 1000 -> "今天 ${
                SimpleDateFormat(
                    "HH:mm",
                    Locale.getDefault()
                ).format(date)
            }"

            diff < 48 * 60 * 60 * 1000 -> "昨天 ${
                SimpleDateFormat(
                    "HH:mm",
                    Locale.getDefault()
                ).format(date)
            }"

            diff < 7 * 24 * 60 * 60 * 1000 -> SimpleDateFormat(
                "MM-dd HH:mm",
                Locale.getDefault()
            ).format(date)

            else -> SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(date)
        }
    }
}