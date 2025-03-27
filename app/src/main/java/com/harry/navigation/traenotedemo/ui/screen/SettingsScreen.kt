package com.harry.navigation.traenotedemo.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.harry.navigation.traenotedemo.ui.viewmodel.NoteListViewModel
import com.harry.navigation.traenotedemo.util.BackupService
import com.harry.navigation.traenotedemo.util.ImageCacheManager
import com.harry.navigation.traenotedemo.util.UpdateManager
import kotlinx.coroutines.launch

/**
 * 设置界面
 * 提供备份和恢复功能，以及布局设置
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    backupService: BackupService,
    viewModel: NoteListViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToRecycleBin: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // 状态
    var isLoading by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var selectedBackupUri by remember { mutableStateOf<Uri?>(null) }
    var restorePrivateNotes by remember { mutableStateOf(true) }
    
    // 更新相关状态
    var showUpdateDialog by remember { mutableStateOf(false) }
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0) }
    var updateInfo by remember { mutableStateOf<UpdateManager.UpdateInfo?>(null) }

    // 创建备份文件选择器
    val createBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            isLoading = true
            coroutineScope.launch {
                val success = backupService.backupAllNotes(uri)
                isLoading = false
                snackbarHostState.showSnackbar(
                    if (success) "备份成功" else "备份失败"
                )
            }
        }
    }

    // 选择备份文件恢复
    val selectBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            selectedBackupUri = uri
            showRestoreDialog = true
        }
    }

    // 获取Context，在Composable函数上下文中调用
    val context = LocalContext.current
    
    // 创建图片缓存管理器
    val imageCacheManager = remember { ImageCacheManager(context) }
    
    // 选择启动页背景图片
    val selectBackgroundImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            // 显示加载中提示
            coroutineScope.launch {
                snackbarHostState.showSnackbar("正在处理图片...")
            }
            
            // 复制图片到缓存目录
            try {
                // 在后台线程中复制图片
                coroutineScope.launch {
                    val copySuccess = imageCacheManager.copyImageToCache(uri)
                    
                    if (copySuccess) {
                        // 获取缓存图片的URI
                        val cachedImageUri = ImageCacheManager.getCachedSplashImageUri(context)
                        if (cachedImageUri != null) {
                            // 更新启动页背景图片URI为缓存图片的URI
                            viewModel.updateSplashBackgroundUri(cachedImageUri)
                            snackbarHostState.showSnackbar("背景图片已更新并保存到缓存")
                        } else {
                            // 如果获取缓存URI失败，使用原始URI
                            viewModel.updateSplashBackgroundUri(uri.toString())
                            snackbarHostState.showSnackbar("背景图片已更新，但缓存失败")
                        }
                    } else {
                        // 复制失败，使用原始URI
                        viewModel.updateSplashBackgroundUri(uri.toString())
                        snackbarHostState.showSnackbar("图片缓存失败，使用原始图片")
                    }
                }
            } catch (e: Exception) {
                // 处理异常
                println("图片处理错误: ${e.javaClass.simpleName} - ${e.message}")
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("处理图片时出错: ${e.message}")
                }
            }
        }
    }
    
    // 权限请求启动器
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // 权限已授予，启动图片选择器
            selectBackgroundImageLauncher.launch("image/*")
        } else {
            // 权限被拒绝
            coroutineScope.launch {
                snackbarHostState.showSnackbar("需要存储权限才能选择背景图片")
            }
        }
    }
    
    // 检查并请求存储权限
    fun checkAndRequestStoragePermission() {
        // 根据Android版本选择合适的权限
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        
        // 检查权限状态
        when {
            // 已有权限，直接启动图片选择器
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED -> {
                selectBackgroundImageLauncher.launch("image/*")
            }
            // 请求权限
            else -> {
                requestPermissionLauncher.launch(permission)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // 备份功能卡片
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "数据备份",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "将笔记数据备份到外部存储，包括普通笔记和加密的隐私笔记。",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                // 生成默认文件名
                                val fileName = backupService.generateBackupFileName()
                                createBackupLauncher.launch(fileName)
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Icon(Icons.Default.Backup, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("创建备份")
                        }
                    }
                }
            }

            // 恢复功能卡片
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "数据恢复",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "从备份文件恢复笔记数据，可以选择是否恢复隐私笔记。",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                selectBackupLauncher.launch(arrayOf("application/json"))
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Icon(Icons.Default.Restore, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("恢复备份")
                        }
                    }
                }
            }

            // 布局设置卡片
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "布局设置",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "选择笔记列表的显示方式，可以切换列表布局或宫格布局。",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 布局切换开关
                        val isGridLayout by viewModel.isGridLayout.collectAsState()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isGridLayout) Icons.Default.GridView else Icons.AutoMirrored.Filled.ViewList,
                                contentDescription = "布局模式",
                                tint = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = if (isGridLayout) "宫格布局" else "列表布局",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )

                            Switch(
                                checked = isGridLayout,
                                onCheckedChange = { viewModel.toggleGridLayout() }
                            )
                        }
                    }
                }
            }

            // 回收站卡片
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .clickable { onNavigateToRecycleBin() }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "回收站",
                                tint = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "回收站",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = "查看和管理已删除的笔记",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }

            // 应用更新卡片
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "应用更新",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "检查应用是否有新版本可用，获取最新功能和修复。",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                // 显示检查中提示
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("正在检查更新...")
                                    
                                    // 创建更新管理器
                                    val updateManager = UpdateManager(context)
                                    
                                    // 检查更新
                                    val newUpdateInfo = updateManager.checkUpdate()
                                    
                                    if (newUpdateInfo != null) {
                                        // 有新版本
                                        updateInfo = newUpdateInfo
                                        showUpdateDialog = true
                                    } else {
                                        // 没有新版本
                                        snackbarHostState.showSnackbar("当前已是最新版本")
                                    }
                                }
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("检查更新")
                        }
                    }
                }
            }
            
            // 启动页设置卡片
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "启动页设置",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "控制应用启动时是否显示启动页面。关闭后将直接进入笔记列表。",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 启动页开关
                        val showSplashScreen by viewModel.showSplashScreen.collectAsState()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Note,
                                contentDescription = "启动页",
                                tint = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = if (showSplashScreen) "显示启动页" else "跳过启动页",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )

                            Switch(
                                checked = showSplashScreen,
                                onCheckedChange = { viewModel.toggleSplashScreen() }
                            )
                        }

                        // 只有在启用启动页时才显示背景图片设置
                        if (showSplashScreen) {
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "自定义启动页背景图片(点击右侧空白选择图片)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // 获取当前背景图片URI
                            val backgroundUri by viewModel.splashBackgroundUri.collectAsState()
                            val context = LocalContext.current

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 显示当前背景图片预览
                                if (backgroundUri != null) {
                                    Image(
                                        painter = rememberAsyncImagePainter(
                                            ImageRequest.Builder(context)
                                                .data(Uri.parse(backgroundUri))
                                                .build()
                                        ),
                                        contentDescription = "背景图片预览",
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(MaterialTheme.shapes.small)
                                            .border(
                                                width = 1.dp,
                                                color = MaterialTheme.colorScheme.outline,
                                                shape = MaterialTheme.shapes.small
                                            ),
                                        contentScale = ContentScale.Crop
                                    )

                                    Spacer(modifier = Modifier.width(16.dp))
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Image,
                                        contentDescription = "选择背景图片",
                                        modifier = Modifier.size(40.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )

                                    Spacer(modifier = Modifier.width(16.dp))
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (backgroundUri != null) "已设置自定义背景" else "未设置自定义背景",
                                        style = MaterialTheme.typography.bodyMedium
                                    )

                                    Text(
                                        text = "选择本地图片作为启动页背景",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Button(
                                    onClick = {
                                        checkAndRequestStoragePermission()
                                    }
                                ) {
                                    Text("选择图片")
                                }
                            }

                            // 添加重置按钮
                            if (backgroundUri != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(
                                    onClick = {
                                        // 清除缓存的图片
                                        coroutineScope.launch {
                                            imageCacheManager.clearCachedSplashImage()
                                            viewModel.updateSplashBackgroundUri(null)
                                            snackbarHostState.showSnackbar("已恢复默认背景并清除缓存")
                                        }
                                    },
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text("恢复默认背景")
                                }
                            }
                        }
                    }
                }

                // 加载指示器
                item {
                    if (isLoading) {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("处理中...")
                            }
                        }
                    }
                }
            }
        }
    }

    // 恢复确认对话框
    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text("恢复备份") },
            text = {
                Column {
                    Text("确定要从选择的备份文件恢复笔记数据吗？")
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = restorePrivateNotes,
                            onCheckedChange = { restorePrivateNotes = it }
                        )
                        Text("包含隐私笔记")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRestoreDialog = false
                        selectedBackupUri?.let { uri ->
                            isLoading = true
                            coroutineScope.launch {
                                val success = backupService.restoreFromBackup(uri, restorePrivateNotes)
                                isLoading = false
                                snackbarHostState.showSnackbar(
                                    if (success) "恢复成功" else "恢复失败"
                                )
                            }
                        }
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    // 密码输入对话框
    if (showPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            title = { Text("输入密码") },
            text = {
                Column {
                    Text("请输入密码以恢复隐私笔记。如果密码不正确，隐私笔记将无法恢复。")
                    Spacer(modifier = Modifier.height(16.dp))

                    // 这里可以添加密码输入框，但为简化实现，直接使用默认密码

                    Button(
                        onClick = {
                            showPasswordDialog = false
                            isLoading = true

                            selectedBackupUri?.let { uri ->
                                coroutineScope.launch {
                                    // 使用默认密码恢复
                                    val success = backupService.restoreFromBackup(
                                        uri = uri,
                                        restorePrivateNotes = restorePrivateNotes
                                    )
                                    isLoading = false
                                    snackbarHostState.showSnackbar(
                                        if (success) "恢复成功" else "恢复失败"
                                    )
                                }
                            }
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("确认恢复")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPasswordDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
    
    // 更新对话框
    if (showUpdateDialog) {
        AlertDialog(
            onDismissRequest = { showUpdateDialog = false },
            title = { Text("发现新版本") },
            text = {
                Column {
                    updateInfo?.let { info ->
                        Text("发现新版本: ${info.versionName}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("更新内容:")
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(info.updateMessage)
                        
                        if (isDownloading) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("正在下载: $downloadProgress%")
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { downloadProgress / 100f },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        updateInfo?.let { info ->
                            if (!isDownloading) {
                                // 开始下载
                                isDownloading = true
                                coroutineScope.launch {
                                    val context = LocalContext.current
                                    val updateManager = UpdateManager(context)
                                    
                                    // 下载APK
                                    val apkUri = updateManager.downloadUpdate(info.apkUrl) { progress ->
                                        downloadProgress = progress
                                    }
                                    
                                    isDownloading = false
                                    showUpdateDialog = false
                                    
                                    if (apkUri != null) {
                                        // 安装APK
                                        updateManager.installApk(apkUri)
                                    } else {
                                        snackbarHostState.showSnackbar("下载失败，请稍后重试")
                                    }
                                }
                            }
                        }
                    },
                    enabled = !isDownloading
                ) {
                    Text(if (isDownloading) "下载中..." else "立即更新")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showUpdateDialog = false },
                    enabled = !isDownloading
                ) {
                    Text("稍后再说")
                }
            }
        )
    }

}