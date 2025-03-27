package com.harry.navigation.traenotedemo.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.harry.navigation.traenotedemo.NoteApplication
import com.harry.navigation.traenotedemo.ui.screen.MainScreen
import com.harry.navigation.traenotedemo.ui.screen.NoteEditScreen
import com.harry.navigation.traenotedemo.ui.screen.PasswordScreen
import com.harry.navigation.traenotedemo.ui.screen.PrivateNoteScreen
import com.harry.navigation.traenotedemo.ui.screen.RecycleBinScreen
import com.harry.navigation.traenotedemo.ui.screen.SettingsScreen
import com.harry.navigation.traenotedemo.ui.screen.SplashScreen
import com.harry.navigation.traenotedemo.ui.viewmodel.NoteEditViewModel
import com.harry.navigation.traenotedemo.ui.viewmodel.NoteEditViewModelFactory
import com.harry.navigation.traenotedemo.ui.viewmodel.NoteListViewModel
import com.harry.navigation.traenotedemo.ui.viewmodel.NoteListViewModelFactory
import com.harry.navigation.traenotedemo.util.BackupService

/**
 * 笔记应用导航组件
 */
@Composable
fun NoteNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String? = null,
    noteApplication: NoteApplication
) {
    // 创建ViewModel以获取用户偏好设置
    val viewModel: NoteListViewModel = viewModel(
        factory = NoteListViewModelFactory(noteApplication.repository, navController.context)
    )
    
    // 获取启动页显示偏好
    val showSplashScreen by viewModel.showSplashScreen.collectAsState()
    
    // 根据用户偏好决定起始目的地
    val actualStartDestination = startDestination ?: if (showSplashScreen) "splash_screen" else "note_list"
    NavHost(navController = navController, startDestination = actualStartDestination) {
        // 启动页面
        composable("splash_screen") {
            // 获取自定义背景图片URI
            val splashBackgroundUri by viewModel.splashBackgroundUri.collectAsState()
            
            SplashScreen(
                onNavigateToMain = {
                    navController.navigate("note_list") {
                        popUpTo("splash_screen") { inclusive = true }
                    }
                },
                showCountdown = showSplashScreen,
                customBackgroundUri = splashBackgroundUri
            )
        }
        // 笔记列表页面
        composable("note_list") {
            val viewModel: NoteListViewModel = viewModel(
                factory = NoteListViewModelFactory(noteApplication.repository, navController.context)
            )

            MainScreen(
                viewModel = viewModel,
                onNoteClick = { noteId ->
                    navController.navigate("note_edit/$noteId")
                },
                onCreateNote = {
                    navController.navigate("note_edit/-1")
                },
                onNavigateToPrivateNotes = {
                    navController.navigate("password_screen")
                },
                onNavigateToSettings = {
                    navController.navigate("settings")
                }
            )
        }

        // 笔记编辑页面
        composable(
            route = "note_edit/{noteId}",
            arguments = listOf(navArgument("noteId") { type = NavType.LongType })
        ) { backStackEntry ->
            // 从路由参数中获取noteId并设置到savedStateHandle中
            val noteId = backStackEntry.arguments?.getLong("noteId") ?: -1L
            val savedStateHandle = androidx.lifecycle.SavedStateHandle().apply {
                set("noteId", noteId)
            }

            val viewModel: NoteEditViewModel = viewModel(
                factory = NoteEditViewModelFactory(
                    repository = noteApplication.repository,
                    savedStateHandle = savedStateHandle
                )
            )

            NoteEditScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // 密码验证页面
        composable("password_screen") {
            val viewModel: NoteListViewModel = viewModel(
                factory = NoteListViewModelFactory(noteApplication.repository, navController.context)
            )

            PasswordScreen(
                viewModel = viewModel,
                onPasswordVerified = {
                    navController.navigate("private_notes")
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // 隐私笔记页面
        composable("private_notes") {
            val viewModel: NoteListViewModel = viewModel(
                factory = NoteListViewModelFactory(noteApplication.repository, navController.context)
            )

            PrivateNoteScreen(
                viewModel = viewModel,
                onNoteClick = { noteId ->
                    navController.navigate("note_edit/$noteId")
                }
            )
        }

        // 设置页面
        composable("settings") {
            val backupService = BackupService(navController.context, noteApplication.repository)
            val viewModel: NoteListViewModel = viewModel(
                factory = NoteListViewModelFactory(noteApplication.repository, navController.context)
            )

            SettingsScreen(
                backupService = backupService,
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToRecycleBin = {
                    navController.navigate("recycle_bin")
                }
            )
        }
        
        // 回收站页面
        composable("recycle_bin") {
            val viewModel: NoteListViewModel = viewModel(
                factory = NoteListViewModelFactory(noteApplication.repository, navController.context)
            )
            
            RecycleBinScreen(
                viewModel = viewModel,
                modifier = Modifier,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}