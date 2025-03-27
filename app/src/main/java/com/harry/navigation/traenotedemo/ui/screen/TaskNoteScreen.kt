package com.harry.navigation.traenotedemo.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.harry.navigation.traenotedemo.ui.viewmodel.NoteListViewModel

/**
 * 待办笔记屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskNoteScreen(
    viewModel: NoteListViewModel,
    onNoteClick: (Long) -> Unit,
    onNavigateBack: () -> Unit,
    onCreateNote: () -> Unit,
    modifier: Modifier = Modifier
) {
    val taskNotes by viewModel.taskNotes.collectAsState()
    
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
                title = { Text("待办笔记") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
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
        if (taskNotes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("没有待办笔记")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Text(
                    text = "待办笔记",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                
                val listState = rememberLazyListState()
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f)
                ) {
                    items(taskNotes) { note ->
                        NoteItem(
                            note = note,
                            noteColors = noteColors,
                            onNoteClick = { onNoteClick(note.id) },
                            onStarClick = { viewModel.toggleStarStatus(note) },
                            onDeleteClick = { viewModel.moveToRecycleBin(note) },
                            onTaskClick = { viewModel.toggleTaskStatus(note) }
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