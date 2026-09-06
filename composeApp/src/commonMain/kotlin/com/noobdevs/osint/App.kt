package com.noobdevs.osint

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noobdevs.osint.ui.AppTab
import com.noobdevs.osint.ui.MainViewModel
import com.noobdevs.osint.ui.UiState
import com.noobdevs.osint.ui.components.LexiconDialog
import com.noobdevs.osint.ui.components.SitrepPreviewDialog
import com.noobdevs.osint.ui.screens.*
import com.noobdevs.osint.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun App(viewModel: MainViewModel = remember { MainViewModel() }) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.toastEvents.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    OSINTTheme(darkTheme = isDarkMode) {
        MainAppScaffold(viewModel = viewModel, snackbarHostState = snackbarHostState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(
    viewModel: MainViewModel,
    snackbarHostState: SnackbarHostState
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val posts by viewModel.posts.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()
    val statsState by viewModel.statsState.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    var showLexiconDialog by remember { mutableStateOf(false) }
    var showSitrepDialog by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = currentTab != AppTab.THREAT_MAP,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(310.dp),
                drawerContainerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        // Executive Drawer Header
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(StatusGreen))
                                    Text(
                                        "CLASSIFIED // TACTICAL",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Text(
                                    "OSINT Command Center",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    "Frontier Security & Counter-Terrorism Intelligence",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 15.sp
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                        // Navigation Items
                        Text("OPERATIONAL SUITE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)

                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.Assessment, contentDescription = null) },
                            label = { Text("Executive Overview", fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold) },
                            selected = currentTab == AppTab.DASHBOARD,
                            onClick = {
                                viewModel.setTab(AppTab.DASHBOARD)
                                coroutineScope.launch { drawerState.close() }
                            }
                        )

                        NavigationDrawerItem(
                            icon = { Icon(Icons.AutoMirrored.Filled.FormatListBulleted, contentDescription = null) },
                            label = { Text("Intelligence Wire", fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold) },
                            selected = currentTab == AppTab.FEED,
                            onClick = {
                                viewModel.setTab(AppTab.FEED)
                                coroutineScope.launch { drawerState.close() }
                            }
                        )

                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.Map, contentDescription = null) },
                            label = { Text("Situational Threat Map", fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold) },
                            selected = currentTab == AppTab.THREAT_MAP,
                            onClick = {
                                viewModel.setTab(AppTab.THREAT_MAP)
                                coroutineScope.launch { drawerState.close() }
                            }
                        )

                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.PhotoLibrary, contentDescription = null) },
                            label = { Text("OSINT Media & Footage", fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold) },
                            selected = currentTab == AppTab.OSINT_PICS,
                            onClick = {
                                viewModel.setTab(AppTab.OSINT_PICS)
                                coroutineScope.launch { drawerState.close() }
                            }
                        )

                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.Description, contentDescription = null) },
                            label = { Text("Daily Operational Reports (DOR)", fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold) },
                            selected = currentTab == AppTab.DOR,
                            onClick = {
                                viewModel.setTab(AppTab.DOR)
                                coroutineScope.launch { drawerState.close() }
                            }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                        // Tangible Intelligence Tools
                        Text("TACTICAL TOOLS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)

                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.FlashOn, contentDescription = null, tint = StatusRed) },
                            label = { Text("Generate 24h Flash SITREP", fontSize = 13.5.sp, fontWeight = FontWeight.Bold) },
                            selected = false,
                            onClick = {
                                coroutineScope.launch { drawerState.close() }
                                showSitrepDialog = true
                            }
                        )

                        NavigationDrawerItem(
                            icon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            label = { Text("Military & Threat Lexicon", fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold) },
                            selected = false,
                            onClick = {
                                coroutineScope.launch { drawerState.close() }
                                showLexiconDialog = true
                            }
                        )

                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.Bookmark, contentDescription = null, tint = StatusAmber) },
                            label = { Text("Evidence Bag (${bookmarks.size})", fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold) },
                            selected = false,
                            onClick = {
                                viewModel.setTab(AppTab.FEED)
                                if (!viewModel.showOnlyBookmarks.value) {
                                    viewModel.toggleShowOnlyBookmarks()
                                }
                                coroutineScope.launch { drawerState.close() }
                            }
                        )
                    }

                    // Drawer Footer
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                            label = { Text("Settings & Connection", fontSize = 13.sp) },
                            selected = currentTab == AppTab.SETTINGS,
                            onClick = {
                                viewModel.setTab(AppTab.SETTINGS)
                                coroutineScope.launch { drawerState.close() }
                            }
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(StatusGreen))
                            Text(
                                "Server Online • GitHub Mirror Ready",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            when (currentTab) {
                                AppTab.DASHBOARD -> "Situation Room"
                                AppTab.FEED -> "Intelligence Wire"
                                AppTab.THREAT_MAP -> "Strategic Threat Map"
                                AppTab.OSINT_PICS -> "Media & Visual Intel"
                                AppTab.DOR -> "Operational Bulletins"
                                AppTab.SETTINGS -> "System Settings"
                            },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Open Drawer")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showLexiconDialog = true }) {
                            Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = "Lexicon", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { showSitrepDialog = true }) {
                            Icon(Icons.Default.FlashOn, contentDescription = "Flash SITREP", tint = StatusRed)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    tonalElevation = 0.dp,
                    modifier = Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                    )
                ) {
                    val navItemColors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    NavigationBarItem(
                        selected = currentTab == AppTab.DASHBOARD,
                        onClick = { viewModel.setTab(AppTab.DASHBOARD) },
                        icon = { Icon(Icons.Default.Assessment, contentDescription = "Overview") },
                        label = { Text("Overview", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = navItemColors
                    )
                    NavigationBarItem(
                        selected = currentTab == AppTab.FEED,
                        onClick = { viewModel.setTab(AppTab.FEED) },
                        icon = { Icon(Icons.AutoMirrored.Filled.FormatListBulleted, contentDescription = "Wire") },
                        label = { Text("Wire", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = navItemColors
                    )
                    NavigationBarItem(
                        selected = currentTab == AppTab.THREAT_MAP,
                        onClick = { viewModel.setTab(AppTab.THREAT_MAP) },
                        icon = { Icon(Icons.Default.Map, contentDescription = "Threat Map") },
                        label = { Text("Map", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = navItemColors
                    )
                    NavigationBarItem(
                        selected = currentTab == AppTab.OSINT_PICS,
                        onClick = { viewModel.setTab(AppTab.OSINT_PICS) },
                        icon = { Icon(Icons.Default.PhotoLibrary, contentDescription = "Pics") },
                        label = { Text("Pics", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = navItemColors
                    )
                    NavigationBarItem(
                        selected = currentTab == AppTab.DOR,
                        onClick = { viewModel.setTab(AppTab.DOR) },
                        icon = { Icon(Icons.Default.Description, contentDescription = "DOR") },
                        label = { Text("DOR", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = navItemColors
                    )
                    NavigationBarItem(
                        selected = currentTab == AppTab.SETTINGS,
                        onClick = { viewModel.setTab(AppTab.SETTINGS) },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = navItemColors
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
                    AppTab.THREAT_MAP -> ThreatMapScreen(viewModel = viewModel)
                    AppTab.OSINT_PICS -> OsintPicsScreen(viewModel = viewModel)
                    AppTab.DOR -> DorScreen(viewModel = viewModel)
                    AppTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }

    if (showLexiconDialog) {
        LexiconDialog(onDismiss = { showLexiconDialog = false })
    }

    if (showSitrepDialog) {
        val stats = (statsState as? UiState.Success)?.data
        SitrepPreviewDialog(
            posts = posts,
            stats = stats,
            onDismiss = { showSitrepDialog = false },
            onShowMessage = { viewModel.postMessage(it) }
        )
    }
}
