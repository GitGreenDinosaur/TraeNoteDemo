package com.harry.navigation.traenotedemo.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.harry.navigation.traenotedemo.data.model.Note
import com.harry.navigation.traenotedemo.ui.components.ConfirmDialog
import com.harry.navigation.traenotedemo.ui.components.ReminderDialog
import com.harry.navigation.traenotedemo.ui.viewmodel.NoteListViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * 笔记列表屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(
    viewModel: NoteListViewModel,
    onNoteClick: (Long) -> Unit,
    onCreateNote: () -> Unit,
    onNavigateToPrivateNotes: () -> Unit,
    onNavigateToSettings: () -> Unit = {},  // 添加设置导航参数，默认为空函数
    modifier: Modifier = Modifier
) {
    val notes by viewModel.notes.collectAsState()
    val starredNotes by viewModel.starredNotes.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    // 添加菜单状态
    var showMenu by remember { mutableStateOf(false) }

    // 添加提醒对话框状态
    var showReminderDialog by remember { mutableStateOf(false) }
    var selectedNote by remember { mutableStateOf<Note?>(null) }
    
    // 添加共享状态变量来跟踪当前滑动的笔记项ID
    var currentSwipedNoteId by remember { mutableStateOf<Long?>(null) }

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("笔记") },
                actions = {
                    // 添加菜单按钮
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "更多选项")
                    }

                    // 下拉菜单
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        // 隐私笔记选项
                        DropdownMenuItem(
                            text = { Text("隐私笔记") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = "隐私笔记"
                                )
                            },
                            onClick = {
                                // 直接导航到密码验证界面
                                onNavigateToPrivateNotes()
                                showMenu = false
                            }
                        )

                        // 设置选项
                        DropdownMenuItem(
                            text = { Text("设置") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Settings,
                                    contentDescription = "设置"
                                )
                            },
                            onClick = {
                                // 导航到设置界面
                                onNavigateToSettings()
                                showMenu = false
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateNote,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "新建笔记")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 搜索栏
            SearchBar(
                query = searchQuery,
                onQueryChange = {
                    searchQuery = it
                    // 实现实时搜索功能，在输入时就触发搜索
                    viewModel.searchNotes(it)
                },
                onSearch = {
                    isSearchActive = false
                },
                active = isSearchActive,
                onActiveChange = { isSearchActive = it },
                placeholder = { Text("搜索笔记") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "搜索") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(MaterialTheme.shapes.large) // 添加圆角样式
            ) {
                // 搜索结果背景添加圆角
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.large) // 添加圆角样式
                ) {
                    // 根据布局模式选择显示方式
                    val isGridLayout by viewModel.isGridLayout.collectAsState()

                    if (isGridLayout) {
                        // 网格布局
                        val searchGridState = rememberLazyGridState()
                        LazyVerticalGrid(
                            state = searchGridState,
                            columns = GridCells.Fixed(2)
                        ) {
                            items(uiState.searchResults) { note ->
                                NoteItem(
                                    note = note,
                                    noteColors = noteColors,
                                    onNoteClick = { onNoteClick(note.id) },
                                    onStarClick = { viewModel.toggleStarStatus(note) },
                                    onDeleteClick = { viewModel.moveToRecycleBin(note) },
                                    onLockClick = { viewModel.togglePrivateStatus(note) },
                                    onTaskClick = { viewModel.toggleTaskStatus(note) },
                                    onLongPress = {
                                        if (note.isTask) {
                                            selectedNote = note
                                            showReminderDialog = true
                                        }
                                    },
                                    currentSwipedNoteId = currentSwipedNoteId,
                                    onSwipe = { noteId -> currentSwipedNoteId = noteId }
                                )
                            }
                        }
                    } else {
                        // 列表布局
                        val searchListState = rememberLazyListState()
                        LazyColumn(
                            state = searchListState
                        ) {
                            items(uiState.searchResults) { note ->
                                NoteItem(
                                    note = note,
                                    noteColors = noteColors,
                                    onNoteClick = { onNoteClick(note.id) },
                                    onStarClick = { viewModel.toggleStarStatus(note) },
                                    onDeleteClick = { viewModel.moveToRecycleBin(note) },
                                    onLockClick = { viewModel.togglePrivateStatus(note) },
                                    onTaskClick = { viewModel.toggleTaskStatus(note) },
                                    onLongPress = {
                                        if (note.isTask) {
                                            selectedNote = note
                                            showReminderDialog = true
                                        }
                                    },
                                    currentSwipedNoteId = currentSwipedNoteId,
                                    onSwipe = { noteId -> currentSwipedNoteId = noteId }
                                )
                            }
                        }
                    }
                }
            }

            // 标星笔记
            if (starredNotes.isNotEmpty()) {
                Text(
                    text = "标星笔记",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                // 根据布局模式选择显示方式
                val isGridLayout by viewModel.isGridLayout.collectAsState()

                if (isGridLayout) {
                    // 网格布局
                    val starredGridState = rememberLazyGridState()
                    LazyVerticalGrid(
                        state = starredGridState,
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.height(200.dp)
                    ) {
                        items(starredNotes) { note ->
                            NoteItem(
                                note = note,
                                noteColors = noteColors,
                                onNoteClick = { onNoteClick(note.id) },
                                onStarClick = { viewModel.toggleStarStatus(note) },
                                onDeleteClick = { viewModel.moveToRecycleBin(note) },
                                onLockClick = { viewModel.togglePrivateStatus(note) },
                                onTaskClick = { viewModel.toggleTaskStatus(note) },
                                onLongPress = {
                                    if (note.isTask) {
                                        selectedNote = note
                                        showReminderDialog = true
                                    }
                                }
                            )
                        }
                    }
                } else {
                    // 列表布局
                    val starredListState = rememberLazyListState()
                    LazyColumn(
                        state = starredListState,
                        modifier = Modifier.height(200.dp)
                    ) {
                        items(starredNotes) { note ->
                            NoteItem(
                                note = note,
                                noteColors = noteColors,
                                onNoteClick = { onNoteClick(note.id) },
                                onStarClick = { viewModel.toggleStarStatus(note) },
                                onDeleteClick = { viewModel.moveToRecycleBin(note) },
                                onLockClick = { viewModel.togglePrivateStatus(note) },
                                onTaskClick = { viewModel.toggleTaskStatus(note) },
                                onLongPress = {
                                    if (note.isTask) {
                                        selectedNote = note
                                        showReminderDialog = true
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // 所有笔记
            Text(
                text = "所有笔记",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (notes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("没有笔记，点击右下角按钮创建")
                }
            } else {
                // 根据布局模式选择显示方式
                val isGridLayout by viewModel.isGridLayout.collectAsState()

                if (isGridLayout) {
                    // 网格布局
                    val gridState = rememberLazyGridState()
                    LazyVerticalGrid(
                        state = gridState,
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(notes) { note ->
                            NoteItem(
                                note = note,
                                noteColors = noteColors,
                                onNoteClick = { onNoteClick(note.id) },
                                onStarClick = { viewModel.toggleStarStatus(note) },
                                onDeleteClick = { viewModel.moveToRecycleBin(note) },
                                onLockClick = { viewModel.togglePrivateStatus(note) },
                                onTaskClick = { viewModel.toggleTaskStatus(note) },
                                onLongPress = {
                                    if (note.isTask) {
                                        selectedNote = note
                                        showReminderDialog = true
                                    }
                                }
                            )
                        }
                        // 添加底部间距，防止最后一项被底部导航栏遮挡
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                } else {
                    // 列表布局
                    // 使用rememberLazyListState保存滚动位置
                    val listState = rememberLazyListState()
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.weight(1f)
                    ) {
                        items(notes) { note ->
                            NoteItem(
                                note = note,
                                noteColors = noteColors,
                                onNoteClick = { onNoteClick(note.id) },
                                onStarClick = { viewModel.toggleStarStatus(note) },
                                onDeleteClick = { viewModel.moveToRecycleBin(note) },
                                onLockClick = { viewModel.togglePrivateStatus(note) },
                                onTaskClick = { viewModel.toggleTaskStatus(note) },
                                onLongPress = {
                                    if (note.isTask) {
                                        selectedNote = note
                                        showReminderDialog = true
                                    }
                                }
                            )
                        }
                        // 添加底部间距，防止最后一项被底部导航栏遮挡
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
        }
    }

    // 显示提醒对话框
    selectedNote?.let { note ->
        if (showReminderDialog) {
            ReminderDialog(
                note = note,
                onDismiss = { showReminderDialog = false },
                onSetReminder = { reminderTime ->
                    viewModel.updateReminderTime(note, reminderTime)
                    showReminderDialog = false
                }
            )
        }
    }

    // 显示提醒对话框
    selectedNote?.let { note ->
        if (showReminderDialog) {
            ReminderDialog(
                note = note,
                onDismiss = { showReminderDialog = false },
                onSetReminder = { reminderTime ->
                    viewModel.updateReminderTime(note, reminderTime)
                    showReminderDialog = false
                }
            )
        }
    }
}

/**
 * 笔记项组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteItem(
    note: Note,
    noteColors: List<Color>,
    onNoteClick: () -> Unit,
    onStarClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onLockClick: (() -> Unit)? = null,
    onTaskClick: (() -> Unit)? = null,
    onLongPress: (() -> Unit)? = null,
    currentSwipedNoteId: Long? = null,
    onSwipe: ((Long?) -> Unit)? = null
) {
    var offsetX by remember { mutableStateOf(0f) }
    val offsetXAnimated by animateFloatAsState(targetValue = offsetX)
    
    // 添加状态变量来跟踪是否显示删除按钮和隐私按钮
    var showDeleteButton by remember { mutableStateOf(false) }
    var showLockButton by remember { mutableStateOf(false) }
    
    // 添加确认对话框状态
    var showTaskConfirmDialog by remember { mutableStateOf(false) }
    
    // 监听当前滑动的笔记项ID变化，如果当前笔记不是被滑动的笔记，则重置状态
    LaunchedEffect(currentSwipedNoteId) {
        if (currentSwipedNoteId != null && currentSwipedNoteId != note.id && (offsetX != 0f || showDeleteButton || showLockButton)) {
            // 使用动画重置状态
            offsetX = 0f
            showDeleteButton = false
            showLockButton = false
            // 确保重置当前笔记的状态
            onSwipe?.invoke(currentSwipedNoteId)
        }
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        // 隐私按钮背景 - 放在卡片左侧，向右滑动时显示
        AnimatedVisibility(
            visible = showLockButton && onLockClick != null,
            enter = fadeIn() + expandHorizontally(expandFrom = Alignment.Start),
            exit = fadeOut() + shrinkHorizontally(shrinkTowards = Alignment.Start),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 8.dp)
        ) {
            // 隐私按钮
            IconButton(
                onClick = {
                    onLockClick?.invoke()
                    // 重置状态
                    offsetX = 0f
                    showLockButton = false
                },
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
                    .size(40.dp)
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = "设为隐私",
                    tint = Color.White
                )
            }
        }
        
        // 删除按钮背景 - 放在卡片右侧，向左滑动时显示
        AnimatedVisibility(
            visible = showDeleteButton,
            enter = fadeIn() + expandHorizontally(),
            exit = fadeOut() + shrinkHorizontally(),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 8.dp)
        ) {
            // 删除按钮
            IconButton(
                onClick = {
                    onDeleteClick()
                    // 重置状态
                    offsetX = 0f
                    showDeleteButton = false
                },
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.error,
                        shape = CircleShape
                    )
                    .size(40.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = Color.White
                )
            }
        }
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .offset { IntOffset(offsetXAnimated.roundToInt(), 0) }
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        // 如果开始滑动，通知父组件当前滑动的笔记项ID
                        if (offsetX == 0f && delta != 0f) {
                            onSwipe?.invoke(note.id)
                        }
                        
                        // 允许向左和向右滑动，但限制最大滑动距离
                        // 向左滑动时delta为负值，向右滑动时delta为正值
                        offsetX = (offsetX + delta).coerceIn(-120f, 120f)
                        // 当向左滑动超过阈值时，显示删除按钮
                        showDeleteButton = offsetX < -50
                        // 当向右滑动超过阈值时，显示隐私按钮
                        showLockButton = offsetX > 50 && onLockClick != null
                    },
                    onDragStopped = {
                        when {
                            offsetX < -50 -> {
                                // 向左滑动超过阈值，显示删除按钮
                                showDeleteButton = true
                                showLockButton = false
                                // 保持滑动状态
                            }
                            offsetX > 50 && onLockClick != null -> {
                                // 向右滑动超过阈值，显示隐私按钮
                                showLockButton = true
                                showDeleteButton = false
                                // 保持滑动状态
                            }
                            else -> {
                                // 滑动距离很小，回到原位
                                offsetX = 0f
                                showDeleteButton = false
                                showLockButton = false
                                // 重置当前滑动的笔记项ID
                                onSwipe?.invoke(null)
                            }
                        }
                    }
                )
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { 
                            if (!showDeleteButton && !showLockButton) {
                                onNoteClick() 
                            } else {
                                // 如果显示操作按钮，点击笔记项重置状态
                                offsetX = 0f
                                showDeleteButton = false
                                showLockButton = false
                                // 重置当前滑动的笔记项ID
                                onSwipe?.invoke(null)
                            }
                        },
                        onLongPress = { 
                            // 如果提供了onTaskClick回调，显示待办确认对话框
                            if (onTaskClick != null) {
                                showTaskConfirmDialog = true
                            } else {
                                // 否则执行原有的长按回调
                                onLongPress?.invoke()
                            }
                        }
                    )
                }
        ) {

            // 原有的笔记内容
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(noteColors[note.color])
                    .padding(16.dp)
            ) {
                // 标题和操作按钮行
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 标题
                    Text(
                        text = note.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // 标星按钮
                    IconButton(
                        onClick = onStarClick
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "标星",
                            tint = if (note.isStarred) Color(0xFFFFC107) else Color.Gray
                        )
                    }

                    // 显示确认对话框
                    if (showTaskConfirmDialog && onTaskClick != null) {
                        ConfirmDialog(
                            title = if (note.isTask) "取消待办" else "设为待办",
                            message = if (note.isTask) "确定要取消此笔记的待办状态吗？" else "确定要将此笔记设为待办项吗？",
                            confirmButtonText = "确认",
                            dismissButtonText = "取消",
                            onConfirm = {
                                // 调用待办状态切换函数
                                onTaskClick()
                                showTaskConfirmDialog = false
                            },
                            onDismiss = { showTaskConfirmDialog = false }
                        )
                    }

                }

                Spacer(modifier = Modifier.height(4.dp))

                // 笔记内容
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 类别和时间信息
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (note.category.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.small)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = note.category,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    // 显示时间（创建或修改时间）
                    Column {
                        Text(
                            text = formatDate(if (note.updatedTime != note.createdTime) note.updatedTime else note.createdTime),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // 显示提醒时间（如果有）
                        if (note.isTask && note.reminderTime != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Alarm,
                                    contentDescription = "提醒时间",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = formatDate(note.reminderTime),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
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

    // 显示提醒对话框
    @Composable
    fun ShowReminderDialogIfNeeded(viewModel: NoteListViewModel) {
        var showReminderDialog by remember { mutableStateOf(false) }
        var selectedNote by remember { mutableStateOf<Note?>(null) }

        // 显示提醒对话框
        selectedNote?.let { note ->
            if (showReminderDialog) {
                ReminderDialog(
                    note = note,
                    onDismiss = { showReminderDialog = false },
                    onSetReminder = { reminderTime ->
                        viewModel.updateReminderTime(note, reminderTime)
                        showReminderDialog = false
                    }
                )
            }
        }
    }
}