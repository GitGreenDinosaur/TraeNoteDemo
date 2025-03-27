package com.harry.navigation.traenotedemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.harry.navigation.traenotedemo.ui.navigation.NoteNavigation
import com.harry.navigation.traenotedemo.ui.theme.TraeNoteDemoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val noteApplication = application as NoteApplication
        
        setContent {
            TraeNoteDemoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NoteNavigation(noteApplication = noteApplication)
                }
            }
        }
    }
}