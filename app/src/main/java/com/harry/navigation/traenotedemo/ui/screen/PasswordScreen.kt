package com.harry.navigation.traenotedemo.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.harry.navigation.traenotedemo.ui.components.NumberKey
import com.harry.navigation.traenotedemo.ui.viewmodel.NoteListViewModel

/**
 * 密码验证屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordScreen(
    viewModel: NoteListViewModel,
    onPasswordVerified: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    val maxPasswordLength = 6 // 密码最大长度
    
    // 密码验证函数
    fun verifyPassword() {
        // 这里使用简单的密码验证，实际应用中应该使用更安全的方式
        if (password == "123456") { // 默认密码
            viewModel.updatePasswordVerification(true)
            passwordError = ""
            onPasswordVerified()
        } else {
            passwordError = "密码错误，请重试"
            password = "" // 清空密码，让用户重新输入
        }
    }
    
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 密码验证界面
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "请输入密码以访问隐私笔记",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                
                // 显示密码掩码
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    for (i in 0 until maxPasswordLength) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .padding(horizontal = 4.dp)
                                .background(
                                    color = Color.Transparent,
                                    shape = CircleShape
                                )
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                                .then(
                                    if (i < password.length) {
                                        Modifier.background(
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = CircleShape
                                        )
                                    } else {
                                        Modifier
                                    }
                                )
                        )
                    }
                }
                
                if (passwordError.isNotEmpty()) {
                    Text(
                        text = passwordError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
                
                // 数字键盘
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 第一行：1, 2, 3
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (number in 1..3) {
                            NumberKey(number = number.toString()) {
                                if (password.length < maxPasswordLength) {
                                    password += number.toString()
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 第二行：4, 5, 6
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (number in 4..6) {
                            NumberKey(number = number.toString()) {
                                if (password.length < maxPasswordLength) {
                                    password += number.toString()
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 第三行：7, 8, 9
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (number in 7..9) {
                            NumberKey(number = number.toString()) {
                                if (password.length < maxPasswordLength) {
                                    password += number.toString()
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 第四行：删除, 0, 确认
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // 删除按钮
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .clickable {
                                    if (password.isNotEmpty()) {
                                        password = password.dropLast(1)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Backspace,
                                contentDescription = "删除",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        // 数字0
                        NumberKey(number = "0") {
                            if (password.length < maxPasswordLength) {
                                password += "0"
                            }
                        }
                        
                        // 确认按钮
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .clickable {
                                    if (password.length == maxPasswordLength) {
                                        verifyPassword()
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "确认",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text("返回")
                }
            }
        }
    }
}