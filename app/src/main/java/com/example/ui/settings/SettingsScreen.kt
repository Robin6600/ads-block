package com.example.ui.settings

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("adblock_prefs", Context.MODE_PRIVATE) }
    
    var autoStart by remember { mutableStateOf(prefs.getBoolean("auto_start", true)) }
    var showNotifications by remember { mutableStateOf(prefs.getBoolean("show_notifications", true)) }
    var strictMode by remember { mutableStateOf(prefs.getBoolean("strict_mode", false)) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ListItem(
                headlineContent = { Text("Auto-start on Boot") },
                supportingContent = { Text("Start VPN automatically when device boots") },
                leadingContent = { Icon(Icons.Default.Settings, contentDescription = null) },
                trailingContent = {
                    Switch(
                        checked = autoStart,
                        onCheckedChange = { 
                            autoStart = it
                            prefs.edit().putBoolean("auto_start", it).apply()
                        }
                    )
                }
            )
            HorizontalDivider()
            
            ListItem(
                headlineContent = { Text("Show Notifications") },
                supportingContent = { Text("Show ad-blocking stats in notification") },
                leadingContent = { Icon(Icons.Default.Notifications, contentDescription = null) },
                trailingContent = {
                    Switch(
                        checked = showNotifications,
                        onCheckedChange = { 
                            showNotifications = it
                            prefs.edit().putBoolean("show_notifications", it).apply()
                        }
                    )
                }
            )
            HorizontalDivider()
            
            ListItem(
                headlineContent = { Text("Strict DNS Mode") },
                supportingContent = { Text("Block malware and adult content alongside ads") },
                leadingContent = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingContent = {
                    Switch(
                        checked = strictMode,
                        onCheckedChange = { 
                            strictMode = it
                            prefs.edit().putBoolean("strict_mode", it).apply()
                        }
                    )
                }
            )
            HorizontalDivider()
            
            ListItem(
                headlineContent = { Text("About Disturb Robin") },
                supportingContent = { Text("Version 1.0.0\nBuilt to protect your privacy.") },
                leadingContent = { Icon(Icons.Default.Info, contentDescription = null) }
            )
        }
    }
}
