package com.harry.navigation.traenotedemo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * 日期时间选择器组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePicker(
    initialDateTime: Date? = null,
    onDateTimeSelected: (Date) -> Unit,
    onDismiss: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(true) }
    
    // 创建日历实例用于处理日期和时间
    val calendar = remember { Calendar.getInstance() }
    initialDateTime?.let { calendar.time = it }
    
    // 日期选择器状态
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = calendar.timeInMillis,
        initialDisplayMode = DisplayMode.Picker
    )
    
    // 时间选择器状态
    val timePickerState = rememberTimePickerState(
        initialHour = calendar.get(Calendar.HOUR_OF_DAY),
        initialMinute = calendar.get(Calendar.MINUTE)
    )
    
    // 检查选择的日期是否有效
    val confirmEnabled by remember {
        derivedStateOf { datePickerState.selectedDateMillis != null }
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // 标题栏
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (showDatePicker) "选择日期" else "选择时间",
                        style = MaterialTheme.typography.titleLarge
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "关闭")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 日期或时间选择器
                if (showDatePicker) {
                    DatePicker(state = datePickerState)
                } else {
                    TimePicker(state = timePickerState)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 底部按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    if (showDatePicker) {
                        Button(
                            onClick = { showDatePicker = false },
                            enabled = confirmEnabled
                        ) {
                            Text("下一步")
                        }
                    } else {
                        Button(
                            onClick = {
                                // 获取选择的日期和时间
                                val selectedDateMillis = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                                val selectedCalendar = Calendar.getInstance().apply {
                                    timeInMillis = selectedDateMillis
                                    set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                                    set(Calendar.MINUTE, timePickerState.minute)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }
                                
                                // 回调选择的日期和时间
                                onDateTimeSelected(selectedCalendar.time)
                                onDismiss()
                            }
                        ) {
                            Text("确定")
                        }
                    }
                }
            }
        }
    }
}

/**
 * 日期时间选择器触发器
 */
@Composable
fun DateTimePickerTrigger(
    dateTime: Date?,
    onDateTimeSelected: (Date?) -> Unit,
    onRemoveDateTime: () -> Unit
) {
    var showDateTimePicker by remember { mutableStateOf(false) }
    
    // 日期时间格式化
    val dateTimeFormatter = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .clickable { showDateTimePicker = true }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = dateTime?.let { dateTimeFormatter.format(it) } ?: "设置提醒时间",
                style = MaterialTheme.typography.bodyMedium
            )
            
            if (dateTime != null) {
                IconButton(onClick = onRemoveDateTime) {
                    Icon(Icons.Default.Close, contentDescription = "移除提醒时间")
                }
            }
        }
    }
    
    if (showDateTimePicker) {
        DateTimePicker(
            initialDateTime = dateTime ?: Date(),
            onDateTimeSelected = { onDateTimeSelected(it) },
            onDismiss = { showDateTimePicker = false }
        )
    }
}