package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.home.HomeScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DisturbRobinAppNavHost()
                }
            }
        }
    }
}

@Composable
fun DisturbRobinAppNavHost() {
    val navController = rememberNavController()
    
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onNavigateToStats = { navController.navigate("stats") },
                onNavigateToWhitelist = { navController.navigate("whitelist") },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }
        composable("stats") {
            com.example.ui.stats.StatsScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable("whitelist") {
            com.example.ui.whitelist.WhitelistScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable("settings") {
            com.example.ui.settings.SettingsScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
