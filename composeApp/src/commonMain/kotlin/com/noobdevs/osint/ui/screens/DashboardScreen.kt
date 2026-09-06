package com.noobdevs.osint.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noobdevs.osint.data.models.StatsResponse
import com.noobdevs.osint.data.models.TimeRange
import com.noobdevs.osint.ui.AppTab
import com.noobdevs.osint.ui.MainViewModel
import com.noobdevs.osint.ui.UiState
import com.noobdevs.osint.ui.theme.*

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.noobdevs.osint.data.models.ActivityCategory
import com.noobdevs.osint.data.models.PostItem
import com.noobdevs.osint.data.models.TheaterCategory
import com.noobdevs.osint.ui.components.SitrepPreviewDialog

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: MainViewModel) {
    val statsState by viewModel.statsState.collectAsState()
    val posts by viewModel.posts.collectAsState()
    val selectedTimeRange by viewModel.selectedTimeRange.collectAsState()
    var timeDropdownExpanded by remember { mutableStateOf(false) }
    var showSitrepDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 36.dp)
    ) {
        // Strategic Threat Level Gauge & Executive Action
        item {
            val stats = (statsState as? UiState.Success)?.data
            ThreatAdvisoryCard(
                posts = posts,
                stats = stats,
                onRefresh = { viewModel.loadDashboardStats() },
                onGenerateSitrep = { showSitrepDialog = true }
            )
        }

        // Minimalist Corporate Dropdown Time Window Selector
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "REPORTING WINDOW",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.1.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                ExposedDropdownMenuBox(
                    expanded = timeDropdownExpanded,
                    onExpandedChange = { timeDropdownExpanded = !timeDropdownExpanded },
                    modifier = Modifier.width(180.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                selectedTimeRange.label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }

                    ExposedDropdownMenu(
                        expanded = timeDropdownExpanded,
                        onDismissRequest = { timeDropdownExpanded = false }
                    ) {
                        TimeRange.entries.forEach { tr ->
                            DropdownMenuItem(
                                text = { Text(tr.label, fontSize = 12.sp) },
                                onClick = {
                                    viewModel.setTimeRange(tr)
                                    timeDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        when (val state = statsState) {
            is UiState.Loading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                    }
                }
            }
            is UiState.Error -> {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Failed to load intelligence metrics", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(state.message, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                            Button(onClick = { viewModel.loadDashboardStats() }, shape = RoundedCornerShape(10.dp)) {
                                Text("Retry Connection")
                            }
                        }
                    }
                }
            }
            is UiState.Success -> {
                val stats = state.data

                item {
                    Text(
                        "OPERATIONAL KPI SUMMARY",
                        style = MaterialTheme.typography.labelMedium.copy(
                            letterSpacing = 1.2.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Balanced, highly readable 2x2 Metric Grid
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            MetricCard(
                                title = "Total Wire Posts",
                                value = stats.totalPosts.toString(),
                                icon = Icons.Default.Assessment,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            MetricCard(
                                title = "Estimated Reach",
                                value = stats.estimatedReach.toString(),
                                icon = Icons.Default.Visibility,
                                color = StatusGreen,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            MetricCard(
                                title = "Kinetic Incidents",
                                value = stats.totalShaheedIncidents.toString(),
                                icon = Icons.Default.Warning,
                                color = StatusRed,
                                modifier = Modifier.weight(1f)
                            )
                            MetricCard(
                                title = "High-Profile Alerts",
                                value = stats.totalHighProfile.toString(),
                                icon = Icons.Default.Shield,
                                color = StatusAmber,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                item {
                    OperationalThreatMatrixCard(
                        stats = stats,
                        onTheaterClick = { theater ->
                            viewModel.setTheaterFilter(theater)
                            viewModel.setTab(AppTab.FEED)
                        },
                        onActivityClick = { activity ->
                            viewModel.setActivityFilter(activity)
                            viewModel.setTab(AppTab.FEED)
                        }
                    )
                }

                item {
                    TrendingSectionCard(
                        title = "Key Tracked Hashtags",
                        icon = Icons.Default.Tag,
                        items = stats.topHashtags.map { "${it.hashtag} (${it.count})" },
                        onItemClick = { tag ->
                            val cleanTag = tag.substringBefore(" (")
                            viewModel.setSearchQuery(cleanTag)
                            viewModel.setTab(AppTab.FEED)
                        }
                    )
                }

                item {
                    TrendingSectionCard(
                        title = "Key Monitored Accounts",
                        icon = Icons.Default.AlternateEmail,
                        items = stats.topMentions.map { "${it.mention} (${it.count})" },
                        onItemClick = { mention ->
                            val clean = mention.substringBefore(" (")
                            viewModel.setSearchQuery(clean)
                            viewModel.setTab(AppTab.FEED)
                        }
                    )
                }

                item {
                    ActivityTrendCard(stats = stats)
                }
            }
            UiState.Idle -> {}
        }
    }

    if (showSitrepDialog) {
        val stats = (statsState as? UiState.Success)?.data
        SitrepPreviewDialog(
            posts = posts,
            stats = stats,
            onDismiss = { showSitrepDialog = false }
        )
    }
}

@Composable
fun ThreatAdvisoryCard(
    posts: List<PostItem>,
    stats: StatsResponse?,
    onRefresh: () -> Unit,
    onGenerateSitrep: () -> Unit
) {
    val kineticCount = posts.count { it.detectActivityType() == ActivityCategory.ATTACKS }
    val highProfileCount = posts.count { it.isHighProfile == 1 }

    val (level, levelColor, advisoryTitle, note) = when {
        kineticCount >= 12 || highProfileCount >= 4 -> Quadruple(
            "LEVEL 4",
            StatusRed,
            "CRITICAL OPERATIONAL RISK",
            "Heightened kinetic friction reported across frontier outposts. Immediate operational sanitization and vigilance advised."
        )
        kineticCount >= 5 || highProfileCount >= 1 -> Quadruple(
            "LEVEL 3",
            StatusAmber,
            "ELEVATED THREAT POSTURE",
            "Increased kinetic probes along KP/Afghan corridor alongside separatist narrative activity in Southern Balochistan."
        )
        else -> Quadruple(
            "LEVEL 2",
            StatusGreen,
            "GUARDED OPERATIONAL POSTURE",
            "Routine operational vigilance. Monitoring peripheral transit corridors and digital disinformation channels."
        )
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, levelColor.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = levelColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            level,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Black,
                            color = levelColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Text(advisoryTitle, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }

                IconButton(onClick = onRefresh, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Text(
                note,
                fontSize = 13.sp,
                lineHeight = 18.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("• $kineticCount Kinetic", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = StatusRed)
                    Text("• $highProfileCount High-Profile", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = StatusAmber)
                }

                Button(
                    onClick = onGenerateSitrep,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Flash SITREP", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                Box(
                    modifier = Modifier.size(32.dp).clip(CircleShape).background(color.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                }
            }
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun OperationalThreatMatrixCard(
    stats: StatsResponse,
    onTheaterClick: (TheaterCategory) -> Unit,
    onActivityClick: (ActivityCategory) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                Text("Operational Threat Matrix", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            }
            Text("Actionable distribution by strategic theater and tactical doctrine. Tap to inspect filtered wire.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            val items = listOf(
                Triple("FAK (Khawarij / KP & Afghan Frontier)", StatusRed, { onTheaterClick(TheaterCategory.FAK) }),
                Triple("FAH (Baloch Separatists / Southern Axis)", MaterialTheme.colorScheme.primary, { onTheaterClick(TheaterCategory.FAH) }),
                Triple("Kinetic Standoffs & Ambush Operations", Color(0xFFB91C1C), { onActivityClick(ActivityCategory.ATTACKS) }),
                Triple("Media Releases, Statements & Propaganda", Color(0xFF475569), { onActivityClick(ActivityCategory.ACTIVITIES) })
            )

            items.forEach { (label, color, onClick) ->
                Surface(
                    onClick = onClick,
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
                            Text(label, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                        }
                        Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, modifier = Modifier.size(16.dp), tint = color)
                    }
                }
            }
        }
    }
}

@Composable
fun TrendingSectionCard(
    title: String,
    icon: ImageVector,
    items: List<String>,
    onItemClick: (String) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(items) { item ->
                    AssistChip(
                        onClick = { onItemClick(item) },
                        label = { Text(item, fontSize = 13.sp, fontWeight = FontWeight.Medium) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier.height(34.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ActivityTrendCard(stats: StatsResponse) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = StatusGreen, modifier = Modifier.size(20.dp))
                Text("15-Day Dissemination Volume", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            stats.activity.take(6).forEach { act ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(act.date, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${act.count} items", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
