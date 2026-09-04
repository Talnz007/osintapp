package com.noobdevs.osint

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.noobdevs.osint.ui.AppTab
import com.noobdevs.osint.ui.MainViewModel
import com.noobdevs.osint.ui.screens.*
import com.noobdevs.osint.ui.theme.OSINTTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val mainViewModel: MainViewModel = viewModel()
            val isDarkMode by mainViewModel.isDarkMode.collectAsState()

            OSINTTheme(darkTheme = isDarkMode) {
                MainAppScaffold(viewModel = mainViewModel)
            }
        }
    }
}

@Composable
fun MainAppScaffold(viewModel: MainViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == AppTab.DASHBOARD,
                    onClick = { viewModel.setTab(AppTab.DASHBOARD) },
                    icon = { Icon(Icons.Default.Assessment, contentDescription = "Overview") },
                    label = { Text("Overview", fontSize = 11.sp, fontWeight = FontWeight.Medium) }
                )
                NavigationBarItem(
                    selected = currentTab == AppTab.FEED,
                    onClick = { viewModel.setTab(AppTab.FEED) },
                    icon = { Icon(Icons.Default.Feed, contentDescription = "Feed") },
                    label = { Text("Feed", fontSize = 11.sp, fontWeight = FontWeight.Medium) }
                )
                NavigationBarItem(
                    selected = currentTab == AppTab.OSINT_PICS,
                    onClick = { viewModel.setTab(AppTab.OSINT_PICS) },
                    icon = { Icon(Icons.Default.PhotoLibrary, contentDescription = "Pics") },
                    label = { Text("OSINT Pics", fontSize = 11.sp, fontWeight = FontWeight.Medium) }
                )
                NavigationBarItem(
                    selected = currentTab == AppTab.DOR,
                    onClick = { viewModel.setTab(AppTab.DOR) },
                    icon = { Icon(Icons.Default.Description, contentDescription = "DOR") },
                    label = { Text("DOR", fontSize = 11.sp, fontWeight = FontWeight.Medium) }
                )
                NavigationBarItem(
                    selected = currentTab == AppTab.SETTINGS,
                    onClick = { viewModel.setTab(AppTab.SETTINGS) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings", fontSize = 11.sp, fontWeight = FontWeight.Medium) }
                )
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            when (currentTab) {
                AppTab.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                AppTab.FEED -> FeedScreen(viewModel = viewModel)
                AppTab.OSINT_PICS -> OsintPicsScreen(viewModel = viewModel)
                AppTab.DOR -> DorScreen(viewModel = viewModel)
                AppTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
            }
        }
    }
}