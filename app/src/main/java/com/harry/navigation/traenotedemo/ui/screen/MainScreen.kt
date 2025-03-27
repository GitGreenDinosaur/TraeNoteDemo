package com.harry.navigation.traenotedemo.ui.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.harry.navigation.traenotedemo.ui.viewmodel.NoteListViewModel

/**
 * 主屏幕，包含底部导航栏
 */
@Composable
fun MainScreen(
    viewModel: NoteListViewModel,
    onNoteClick: (Long) -> Unit,
    onCreateNote: () -> Unit,
    onNavigateToPrivateNotes: () -> Unit,
    onNavigateToSettings: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Note, contentDescription = "笔记") },
                    label = { Text("笔记") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.CheckCircle, contentDescription = "待办") },
                    label = { Text("待办") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
            }
        },
        floatingActionButton = {
            // 只在笔记列表标签或待办标签被选中时显示新建笔记按钮
            if (selectedTab == 0 || selectedTab == 1) {
                FloatingActionButton(
                    onClick = onCreateNote,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "新建笔记")
                }
            }
        }
    ) { innerPadding ->
        when (selectedTab) {
            0 -> NoteListScreen(
                viewModel = viewModel,
                onNoteClick = onNoteClick,
                onCreateNote = onCreateNote,
                onNavigateToPrivateNotes = onNavigateToPrivateNotes,
                onNavigateToSettings = onNavigateToSettings,
                modifier = Modifier.padding(innerPadding)
            )
            1 -> TaskNoteScreen(
                viewModel = viewModel,
                onNoteClick = onNoteClick,
                onNavigateBack = { selectedTab = 0 },
                onCreateNote = onCreateNote,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}