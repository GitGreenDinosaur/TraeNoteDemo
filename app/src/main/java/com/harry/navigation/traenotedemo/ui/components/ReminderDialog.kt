package com.harry.navigation.traenotedemo.ui.components

import android.content.Intent
import android.provider.AlarmClock
import android.provider.CalendarContract
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.harry.navigation.traenotedemo.data.model.Note
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * 提醒对话框组件
 * 用于设置待办笔记的提醒时间和提醒方式
 */
@Composable
fun ReminderDialog(
    note: Note,
    onDismiss: () -> Unit,
    onSetReminder: (Date) -> Unit
) {
    val context = LocalContext.current
    var showDateTimePicker by remember { mutableStateOf(true) }
    var selectedDate by remember { mutableStateOf(note.reminderTime ?: Date()) }
    var reminderType by remember { mutableStateOf(ReminderType.ALARM) }
    
    if (showDateTimePicker) {
        // 显示日期时间选择器
        DateTimePicker(
            initialDateTime = selectedDate,
            onDateTimeSelected = { 
                selectedDate = it
                showDateTimePicker = false
            },
            onDismiss = onDismiss
        )
    } else {
        // 显示提醒方式选择对话框
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("设置提醒方式") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 显示选择的日期和时间
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    Text(
                        text = "已选择时间: ${dateFormat.format(selectedDate)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 提醒方式选择
                    Text(
                        text = "选择提醒方式:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Column(Modifier.selectableGroup()) {
                        ReminderTypeOption(
                            text = "设置为闹钟提醒",
                            selected = reminderType == ReminderType.ALARM,
                            onClick = { reminderType = ReminderType.ALARM }
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        ReminderTypeOption(
                            text = "添加到系统日历",
                            selected = reminderType == ReminderType.CALENDAR,
                            onClick = { reminderType = ReminderType.CALENDAR }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // 根据选择的提醒方式创建系统提醒
                        when (reminderType) {
                            ReminderType.ALARM -> {
                                // 创建闹钟提醒
                                val calendar = Calendar.getInstance().apply {
                                    time = selectedDate
                                }
                                
                                val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                                    putExtra(AlarmClock.EXTRA_MESSAGE, note.title)
                                    putExtra(AlarmClock.EXTRA_HOUR, calendar.get(Calendar.HOUR_OF_DAY))
                                    putExtra(AlarmClock.EXTRA_MINUTES, calendar.get(Calendar.MINUTE))
                                    putExtra(AlarmClock.EXTRA_SKIP_UI, false) // 显示闹钟设置界面
                                }
                                
                                context.startActivity(intent)
                            }
                            ReminderType.CALENDAR -> {
                                // 创建日历事件
                                val calendar = Calendar.getInstance().apply {
                                    time = selectedDate
                                }
                                
                                val startMillis = calendar.timeInMillis
                                // 默认事件持续1小时
                                calendar.add(Calendar.HOUR, 1)
                                val endMillis = calendar.timeInMillis
                                
                                val intent = Intent(Intent.ACTION_INSERT).apply {
                                    data = CalendarContract.Events.CONTENT_URI
                                    putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
                                    putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
                                    putExtra(CalendarContract.Events.TITLE, note.title)
                                    putExtra(CalendarContract.Events.DESCRIPTION, note.content)
                                }
                                
                                context.startActivity(intent)
                            }
                        }
                        
                        // 更新笔记的提醒时间
                        onSetReminder(selectedDate)
                        onDismiss()
                    }
                ) {
                    Text("确认")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
            },
            properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
        )
    }
}

/**
 * 提醒方式选项组件
 */
@Composable
private fun ReminderTypeOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null // null because we're handling the click on the row
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text)
    }
}

/**
 * 提醒类型枚举
 */
enum class ReminderType {
    ALARM,      // 闹钟提醒
    CALENDAR    // 日历提醒
}