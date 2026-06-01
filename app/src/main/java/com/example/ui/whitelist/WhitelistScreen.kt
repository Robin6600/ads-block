package com.example.ui.whitelist

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AppItem(
    val name: String,
    val packageName: String,
    val isProtected: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhitelistScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val pm = context.packageManager
    var appList by remember { mutableStateOf<List<AppItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val prefs = context.getSharedPreferences("adblock_prefs", Context.MODE_PRIVATE)

    LaunchedEffect(Unit) {
        scope.launch {
            val apps = withContext(Dispatchers.IO) {
                try {
                    val bypassed = prefs.getStringSet("bypassed_apps", emptySet()) ?: emptySet()
                    pm.getInstalledApplications(0)
                        .filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 || (it.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0 }
                        .map { info ->
                            AppItem(
                                name = try { info.loadLabel(pm).toString() } catch (e: Exception) { info.packageName },
                                packageName = info.packageName,
                                isProtected = !bypassed.contains(info.packageName)
                            )
                        }
                        .sortedBy { it.name.lowercase() }
                } catch (e: Exception) {
                    emptyList<AppItem>()
                }
            }
            appList = apps
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Protected Apps") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Toggle off to allow apps to bypass the proxy and show ads.", style = MaterialTheme.typography.bodyMedium)
                }
            }
            
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(appList) { app ->
                        AppListItem(app = app) { newValue ->
                            val bypassed = prefs.getStringSet("bypassed_apps", emptySet())?.toMutableSet() ?: mutableSetOf()
                            if (newValue) {
                                bypassed.remove(app.packageName)
                            } else {
                                bypassed.add(app.packageName)
                            }
                            prefs.edit().putStringSet("bypassed_apps", bypassed).apply()
                            appList = appList.map {
                                if (it.packageName == app.packageName) it.copy(isProtected = newValue) else it
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppListItem(app: AppItem, onToggle: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(app.name, style = MaterialTheme.typography.bodyLarge)
                Text(app.packageName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
            }
            Switch(
                checked = app.isProtected,
                onCheckedChange = onToggle
            )
        }
    }
}
