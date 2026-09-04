package com.noobdevs.osint.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.noobdevs.osint.data.StatsResponse
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
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 14.dp, bottom = 32.dp)
    ) {
        item {
            HeaderCard(onRefresh = { viewModel.loadDashboardStats() })
        }

        // Time Range Filter Bar (Website replication)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("RANGE:", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(com.noobdevs.osint.data.TimeRange.values()) { tr ->
                            FilterChip(
                                selected = selectedTimeRange == tr,
                                onClick = { viewModel.setTimeRange(tr) },
                                label = { Text(tr.label, fontSize = 11.sp) },
                                modifier = Modifier.height(28.dp)
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
                            .height(240.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            is UiState.Error -> {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Failed to load intelligence metrics", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                            Text(state.message, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { viewModel.loadDashboardStats() }) {
                                Text("Retry")
                            }
                        }
                    }
                }
            }
            is UiState.Success -> {
                val stats = state.data

                item {
                    Text(
                        "KEY INTEL METRICS",
                        style = MaterialTheme.typography.labelMedium.copy(
                            letterSpacing = 1.2.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            MetricCard(
                                title = "Total Posts",
                                value = "%,d".format(stats.totalPosts),
                                icon = Icons.Default.Assessment,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            MetricCard(
                                title = "Est. Reach",
                                value = "%,d".format(stats.estimatedReach),
                                icon = Icons.Default.Visibility,
                                color = StatusGreen,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            MetricCard(
                                title = "Incidents",
                                value = "%,d".format(stats.totalShaheedIncidents),
                                icon = Icons.Default.Warning,
                                color = StatusRed,
                                modifier = Modifier.weight(1f)
                            )
                            MetricCard(
                                title = "High Profile",
                                value = "%,d".format(stats.totalHighProfile),
                                icon = Icons.Default.Shield,
                                color = StatusAmber,
                                modifier = Modifier.weight(1f)
                            )
                            MetricCard(
                                title = "Intercepts",
                                value = "%,d".format(stats.postsWithImages),
                                icon = Icons.Default.Image,
                                color = StatusPurple,
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
                        title = "Top Trending Hashtags",
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
                        title = "Top Tracked Accounts",
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(StatusGreen)
                    )
                    Text("LIVE INTEL HUB", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StatusGreen)
                }
                Text("Talkwalker OSINT Command", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Automated Threat Monitoring & Dissemination", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onRefresh) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
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
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
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
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.FlashOn, contentDescription = null, tint = StatusRed, modifier = Modifier.size(20.dp))
                Text("Attacks & Incident Categories", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Text("Categorized threat and security incident distribution", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

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
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(5.dp)).background(color))
                            Text(attack.attackType.replace("_", " "), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Text("%,d posts".format(attack.count), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
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
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(items) { item ->
                    AssistChip(
                        onClick = { onItemClick(item) },
                        label = { Text(item, fontSize = 12.sp) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        )
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
                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = StatusGreen, modifier = Modifier.size(20.dp))
                Text("15-Day Dissemination Volume", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            stats.activity.take(6).forEach { act ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(act.date, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("%,d items".format(act.count), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
