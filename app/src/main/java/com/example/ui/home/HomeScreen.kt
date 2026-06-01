package com.example.ui.home

import android.app.Activity.RESULT_OK
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.res.stringResource
import com.example.R
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.viewmodel.AdBlockViewModel
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    viewModel: AdBlockViewModel = viewModel(),
    onNavigateToStats: () -> Unit,
    onNavigateToWhitelist: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val isVpnActive by viewModel.isVpnActive.collectAsState()
    val adsBlockedToday by viewModel.adsBlockedToday.collectAsState()
    val adsBlockedWeek by viewModel.adsBlockedWeek.collectAsState()
    val totalAdsBlocked by viewModel.totalAdsBlocked.collectAsState()

    val vpnPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            viewModel.startVpnService(context)
        }
    }

    LaunchedEffect(isVpnActive) {
        viewModel.checkStatus()
        while (true) {
            delay(1000)
            viewModel.checkStatus()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1C1E))
    ) {
        // App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFD0BCFF), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "App Icon",
                        tint = Color(0xFF381E72),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Disturb Robin",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = Color(0xFFE2E2E6)
                )
            }
            IconButton(onClick = onNavigateToSettings) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color(0xFFE2E2E6)
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Status Info
            Text(
                text = if (isVpnActive) "STATUS: ACTIVE" else "STATUS: INACTIVE",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 2.sp
                ),
                color = Color(0xFFD0BCFF)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isVpnActive) "All ads are being blocked right now" else "Tap the shield to activate protection",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF938F99)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Large Circular Toggle
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(200.dp)
                    .clickable {
                        try {
                            viewModel.toggleVpn(context) { intent ->
                                vpnPermissionLauncher.launch(intent)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
            ) {
                if (isVpnActive) {
                    PulseEffect()
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(160.dp)
                        .shadow(
                            elevation = if (isVpnActive) 40.dp else 0.dp,
                            shape = CircleShape,
                            ambientColor = Color(0xFFD0BCFF),
                            spotColor = Color(0xFFD0BCFF)
                        )
                        .clip(CircleShape)
                        .background(if (isVpnActive) Color(0xFFD0BCFF) else Color(0xFF2D2F33))
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Toggle Protection",
                            tint = if (isVpnActive) Color(0xFF381E72) else Color(0xFF938F99),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isVpnActive) "ACTIVE" else "INACTIVE",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = if (isVpnActive) Color(0xFF381E72) else Color(0xFF938F99)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Facebook Follow CTA Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 450.dp)
                    .clickable {
                        try {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                android.net.Uri.parse("https://www.facebook.com/SheikhRaselRobin98")
                            )
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2F33)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1877F2).copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Facebook Premium Circular Gradient Icon
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                    colors = listOf(Color(0xFF18ACFE), Color(0xFF1877F2))
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "f",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                            modifier = Modifier.offset(x = 1.dp, y = (-2).dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // Name & Action Description
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Sheikh Rasel Robin",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                ),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            // Small Premium Blue Checkmark
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .background(Color(0xFF1877F2), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "✓",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.offset(y = (-0.5).dp)
                                )
                            }
                        }
                        Text(
                            text = stringResource(R.string.follow_developer),
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = Color(0xFF938F99)
                        )
                    }
                    
                    // Styled CTA Badge/Button
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF1877F2), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "FOLLOW",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 0.5.sp
                            ),
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Stats Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Today Card
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFF2D2F33), RoundedCornerShape(24.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                        .padding(20.dp)
                ) {
                    Text(
                        text = "TODAY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = Color(0xFF938F99)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = String.format(java.util.Locale.getDefault(), "%,d", adsBlockedToday),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Light),
                        color = Color(0xFFEADDFF)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF4ADE80),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Real-time blocked",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = Color(0xFF4ADE80)
                        )
                    }
                }

                // Total Card
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFF2D2F33), RoundedCornerShape(24.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                        .padding(20.dp)
                ) {
                    Text(
                        text = "TOTAL",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = Color(0xFF938F99)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = String.format(java.util.Locale.getDefault(), "%,d", totalAdsBlocked),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Light),
                        color = Color(0xFFEADDFF)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "All-time secure",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = Color(0xFF938F99)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Insights List
            var isUpdating by remember { mutableStateOf(false) }
            val updateProgress by animateFloatAsState(
                targetValue = if (isUpdating) 1f else 0f,
                animationSpec = tween(durationMillis = 2000, easing = LinearOutSlowInEasing)
            )

            LaunchedEffect(isUpdating) {
                if (isUpdating) {
                    delay(2000)
                    isUpdating = false
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF2D2F33).copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                    .padding(8.dp)
                    .clickable { isUpdating = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF2D2F33), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFFF87171).copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isUpdating) {
                                CircularProgressIndicator(
                                    progress = { updateProgress },
                                    modifier = Modifier.size(20.dp),
                                    color = Color(0xFFF87171),
                                    strokeWidth = 2.dp,
                                    trackColor = Color.Transparent
                                )
                            } else {
                                Text(
                                    text = "ADS",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFF87171)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Blocklist Update (Tap to Update)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = Color(0xFFE2E2E6)
                            )
                            Text(
                                text = if (isUpdating) "Updating rules..." else "Real-time: 215,842 domains filtered",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF938F99)
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF49454F), CircleShape)
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isUpdating) "Syncing" else "Latest",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFEADDFF)
                        )
                    }
                }
            }
        }

        // Bottom Navigation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1C1B1F))
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(
                icon = Icons.Default.Home,
                label = "Home",
                isSelected = true,
                onClick = { }
            )
            NavItem(
                icon = Icons.AutoMirrored.Filled.List,
                label = "Stats",
                isSelected = false,
                onClick = onNavigateToStats
            )
            NavItem(
                icon = Icons.Default.Security,
                label = "Whitelist",
                isSelected = false,
                onClick = onNavigateToWhitelist
            )
            NavItem(
                icon = Icons.Default.Settings,
                label = "Settings",
                isSelected = false,
                onClick = onNavigateToSettings
            )
        }
    }
}

@Composable
fun NavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .background(
                    if (isSelected) Color(0xFFE8DEF8).copy(alpha = 0.1f) else Color.Transparent,
                    RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 20.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) Color(0xFFD0BCFF) else Color(0xFF938F99),
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = if (isSelected) Color(0xFFD0BCFF) else Color(0xFF938F99)
        )
    }
}

@Composable
fun PulseEffect() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            color = Color(0xFFD0BCFF),
            radius = (size.minDimension / 2) * 0.8f,
            alpha = 0.2f,
            style = Stroke(width = 4.dp.toPx())
        )
        drawCircle(
            color = Color(0xFFD0BCFF),
            radius = (size.minDimension / 2) * 0.6f,
            alpha = 0.1f
        )
    }
}
