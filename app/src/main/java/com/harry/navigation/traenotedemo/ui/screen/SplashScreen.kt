package com.harry.navigation.traenotedemo.ui.screen

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.harry.navigation.traenotedemo.R
import com.harry.navigation.traenotedemo.util.ImageCacheManager
import kotlinx.coroutines.delay

/**
 * 启动页面
 */
@Composable
fun SplashScreen(
    onNavigateToMain: () -> Unit,
    showCountdown: Boolean = true,
    customBackgroundUri: String? = null
) {
    // 是否已经完成倒计时
    var isTimerFinished by remember { mutableStateOf(false) }
    // 倒计时数字
    var countdownNumber by remember { mutableStateOf(3) }
    
    // 启动倒计时，3秒后自动跳转
    LaunchedEffect(key1 = true) {
        if (showCountdown) {
            // 每秒更新倒计时数字
            while(countdownNumber > 0) {
                delay(1200) // 延迟3秒
                countdownNumber--
            }
            isTimerFinished = true
            onNavigateToMain() // 自动跳转到主界面
        } else {
            // 如果不显示倒计时，直接跳转
            onNavigateToMain()
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // 背景图片
        val context = LocalContext.current
        
        // 首先检查缓存目录中是否存在splash01.png
        val cachedImageUri = ImageCacheManager.getCachedSplashImageUri(context)
        
        if (cachedImageUri != null) {
            // 优先使用缓存的图片
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(context)
                        .data(Uri.parse(cachedImageUri))
                        .error(R.drawable.splash_pic001) // 加载失败时使用默认图片
                        .build()
                ),
                contentDescription = "启动页背景",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else if (customBackgroundUri != null) {
            // 如果缓存中没有图片，但有自定义背景URI，则使用自定义背景
            // 创建一个安全的URI解析函数
            val imageData = try {
                Uri.parse(customBackgroundUri)
            } catch (e: Exception) {
                // URI解析失败时返回null
                null
            }

            // 使用Coil的error参数处理加载失败情况
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(context)
                        .data(imageData)
                        .error(R.drawable.splash_pic001) // 加载失败时使用默认图片
                        .build()
                ),
                contentDescription = "启动页背景",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            // 使用默认背景图片
            Image(
                painter = painterResource(id = R.drawable.splash_pic001),
                contentDescription = "启动页背景",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        
        // 只有在显示倒计时时才显示倒计时文本和跳过按钮
        if (showCountdown) {
            // 左上角倒计时显示
            Text(
                text = countdownNumber.toString(),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 56.dp, start = 32.dp),
                style = androidx.compose.material3.MaterialTheme.typography.headlineLarge
            )
            
            // 右上角跳过按钮
            Button(
                onClick = {
                    if (!isTimerFinished) {
                        onNavigateToMain() // 手动跳转到主界面
                    }
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 56.dp, end = 32.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent
                )
            ) {
                Text("跳过", fontSize = 20.sp)
            }
        }
    }
}