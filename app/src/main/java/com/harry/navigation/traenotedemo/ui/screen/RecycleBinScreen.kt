package com.harry.navigation.traenotedemo.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.harry.navigation.traenotedemo.data.model.Note
import com.harry.navigation.traenotedemo.ui.viewmodel.NoteListViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * 回收站屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecycleBinScreen(
    viewModel: NoteListViewModel,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {}
) {
    val recycleBinNotes by viewModel.recycleBinNotes.collectAsState()
    
    // 添加确认删除对话框状态
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var noteToDelete by remember { mutableStateOf<Note?>(null) }
    
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
                title = { Text("回收站") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (recycleBinNotes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("回收站为空")
                }
            } else {
                Text(
                    text = "笔记将在回收站保留7天，之后将被自动删除",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
                
                LazyColumn {
                    items(recycleBinNotes) { note ->
                        RecycleBinNoteItem(
                            note = note,
                            noteColors = noteColors,
                            onRestoreClick = { viewModel.restoreFromRecycleBin(note) },
                            onDeleteClick = { 
                                noteToDelete = note
                                showDeleteConfirmDialog = true
                            }
                        )
                    }
                    // 添加底部间距，防止最后一项被底部导航栏遮挡
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
            
            // 确认删除对话框
            if (showDeleteConfirmDialog && noteToDelete != null) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmDialog = false },
                    title = { Text("确认删除") },
                    text = { Text("确定要永久删除这条笔记吗？此操作不可撤销。") },
                    confirmButton = {
                        Button(
                            onClick = {
                                noteToDelete?.let { viewModel.deleteNotePermanently(it) }
                                showDeleteConfirmDialog = false
                                noteToDelete = null
                            }
                        ) {
                            Text("确认删除")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showDeleteConfirmDialog = false
                                noteToDelete = null
                            }
                        ) {
                            Text("取消")
                        }
                    }
                )
            }
        }
    }
}

/**
 * 回收站笔记项组件
 */
@Composable
fun RecycleBinNoteItem(
    note: Note,
    noteColors: List<Color>,
    onRestoreClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(noteColors[note.color])
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 笔记内容
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // 显示删除时间
                note.deletedTime?.let { deletedTime ->
                    Text(
                        text = "删除于: ${formatDeletedDate(deletedTime)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // 计算剩余天数
                    val daysLeft = calculateDaysLeft(deletedTime)
                    Text(
                        text = "将在${daysLeft}天后永久删除",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (daysLeft <= 2) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // 恢复按钮
            IconButton(onClick = onRestoreClick) {
                Icon(
                    imageVector = Icons.Default.Restore,
                    contentDescription = "恢复"
                )
            }
            
            // 永久删除按钮
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "永久删除",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/**
 * 格式化删除日期
 */
private fun formatDeletedDate(date: Date): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(date)
}

/**
 * 计算剩余天数
 */
private fun calculateDaysLeft(deletedDate: Date): Int {
    val now = Date()
    val diff = 7 - TimeUnit.MILLISECONDS.toDays(now.time - deletedDate.time)
    return if (diff < 0) 0 else diff.toInt()
}