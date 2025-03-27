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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.harry.navigation.traenotedemo.data.model.Note
import com.harry.navigation.traenotedemo.ui.viewmodel.NoteListViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 隐私笔记屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivateNoteScreen(
    viewModel: NoteListViewModel,
    onNoteClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val privateNotes by viewModel.privateNotes.collectAsState()
    
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
                title = { Text("隐私笔记") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 显示隐私笔记列表
            if (privateNotes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("没有隐私笔记")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(privateNotes) { note ->
                        PrivateNoteItem(
                            note = note,
                            noteColors = noteColors,
                            onNoteClick = { onNoteClick(note.id) },
                            onStarClick = { viewModel.toggleStarStatus(note) },
                            onDeleteClick = { viewModel.moveToRecycleBin(note) },
                            onLockClick = { viewModel.togglePrivateStatus(note) }
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

/**
 * 隐私笔记项组件
 */
@Composable
fun PrivateNoteItem(
    note: Note,
    noteColors: List<Color>,
    onNoteClick: () -> Unit,
    onStarClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onLockClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onNoteClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(noteColors[note.color])
                .padding(16.dp)
        ) {
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
                
                // 隐私按钮
                IconButton(
                    onClick = onLockClick
                ) {
                    Icon(
                        imageVector = Icons.Default.LockOpen,
                        contentDescription = "取消隐私",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                // 删除按钮
                IconButton(
                    onClick = onDeleteClick
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
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
                Text(
                    text = formatDate(if (note.updatedTime != note.createdTime) note.updatedTime else note.createdTime),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
        diff < 24 * 60 * 60 * 1000 -> "今天 ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)}"
        diff < 48 * 60 * 60 * 1000 -> "昨天 ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)}"
        diff < 7 * 24 * 60 * 60 * 1000 -> SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(date)
        else -> SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(date)
    }
}