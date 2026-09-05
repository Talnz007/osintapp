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

@Composable
fun DashboardScreen(viewModel: MainViewModel) {
    val statsState by viewModel.statsState.collectAsState()
    val selectedTimeRange by viewModel.selectedTimeRange.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 36.dp)
    ) {
        item {
            HeaderCard(onRefresh = { viewModel.loadDashboardStats() })
        }

        // Clean Segmented Time Window
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TimeRange.entries.forEach { tr ->
                    FilterChip(
                        selected = selectedTimeRange == tr,
                        onClick = { viewModel.setTimeRange(tr) },
                        label = { Text(tr.label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier.weight(1f).height(36.dp)
                    )
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
                    AttackTypeBreakdownCard(stats = stats, onAttackClick = { type ->
                        viewModel.setAttackTypeFilter(type)
                        viewModel.setTab(AppTab.FEED)
                    })
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
}

@Composable
fun HeaderCard(onRefresh: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(StatusGreen)
                    )
                    Text("ACTIVE SURVEILLANCE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StatusGreen, letterSpacing = 1.sp)
                }
                Text("OSINT Command Center", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                Text("National Tactical Monitoring & Threat Dissemination", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(
                onClick = onRefresh,
                modifier = Modifier.size(42.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = MaterialTheme.colorScheme.primary)
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
fun AttackTypeBreakdownCard(stats: StatsResponse, onAttackClick: (String) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.FlashOn, contentDescription = null, tint = StatusRed, modifier = Modifier.size(22.dp))
                Text("Incident & Threat Breakdown", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            }
            Text("Categorized distribution of tracked security incidents. Tap to inspect filtered wire.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            stats.attackTypes.forEach { attack ->
                val color = when (attack.attackType) {
                    "AMBUSH_FIRE" -> StatusRed
                    "IED_EXPLOSIVE" -> Color(0xFFE11D48)
                    "CIVIL_UNREST" -> StatusAmber
                    "SABOTAGE" -> StatusPurple
                    else -> MaterialTheme.colorScheme.primary
                }
                Surface(
                    onClick = { onAttackClick(attack.attackType) },
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
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
                            Text(attack.attackType.replace("_", " "), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("${attack.count} items", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
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
