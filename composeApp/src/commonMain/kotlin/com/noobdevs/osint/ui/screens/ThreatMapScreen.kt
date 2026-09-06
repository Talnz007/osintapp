package com.noobdevs.osint.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noobdevs.osint.data.models.ActivityCategory
import com.noobdevs.osint.data.models.MapSortOrder
import com.noobdevs.osint.data.models.TheaterCategory
import com.noobdevs.osint.data.models.ThreatMapMarker
import com.noobdevs.osint.data.models.TimeRange
import com.noobdevs.osint.platform.PlatformActions
import com.noobdevs.osint.platform.PlatformThreatMapView
import com.noobdevs.osint.ui.MainViewModel
import com.noobdevs.osint.ui.components.InbuiltVideoPlayerModal
import com.noobdevs.osint.ui.components.WhatsAppGreen
import com.noobdevs.osint.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreatMapScreen(viewModel: MainViewModel) {
        val markers by viewModel.threatMarkers.collectAsState()
    val selectedTheater by viewModel.selectedTheater.collectAsState()
    val selectedActivity by viewModel.selectedActivity.collectAsState()
    val sortOrder by viewModel.mapSortOrder.collectAsState()
    val selectedTimeRange by viewModel.selectedTimeRange.collectAsState()

    var selectedMarker by remember { mutableStateOf<ThreatMapMarker?>(null) }
    var showVideoPlayer by remember { mutableStateOf(false) }
    var theaterDropdownExpanded by remember { mutableStateOf(false) }
    var activityDropdownExpanded by remember { mutableStateOf(false) }
    var sortDropdownExpanded by remember { mutableStateOf(false) }
    var centerTrigger by remember { mutableStateOf(0) }

    Box(modifier = Modifier.fillMaxSize()) {
        PlatformThreatMapView(
            markers = markers,
            onMarkerClicked = { markerId ->
                selectedMarker = markers.find { it.id == markerId }
            },
            modifier = Modifier.fillMaxSize(),
            centerTrigger = centerTrigger
        )

        // Top Minimalist Floating Control Bar
        Surface(
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            shape = RoundedCornerShape(12.dp),
            shadowElevation = 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .align(Alignment.TopCenter)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Theater Dropdown Menu
                    ExposedDropdownMenuBox(
                        expanded = theaterDropdownExpanded,
                        onExpandedChange = { theaterDropdownExpanded = !theaterDropdownExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 7.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    selectedTheater.shortCode,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(15.dp))
                            }
                        }

                        ExposedDropdownMenu(
                            expanded = theaterDropdownExpanded,
                            onDismissRequest = { theaterDropdownExpanded = false }
                        ) {
                            TheaterCategory.entries.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.label, fontSize = 12.5.sp) },
                                    onClick = {
                                        viewModel.setTheaterFilter(cat)
                                        theaterDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Activity Type Dropdown Menu
                    ExposedDropdownMenuBox(
                        expanded = activityDropdownExpanded,
                        onExpandedChange = { activityDropdownExpanded = !activityDropdownExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 7.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    when (selectedActivity) {
                                        ActivityCategory.ALL -> "All Types"
                                        ActivityCategory.ATTACKS -> "Attacks"
                                        ActivityCategory.ACTIVITIES -> "Media"
                                    },
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(15.dp))
                            }
                        }

                        ExposedDropdownMenu(
                            expanded = activityDropdownExpanded,
                            onDismissRequest = { activityDropdownExpanded = false }
                        ) {
                            ActivityCategory.entries.forEach { act ->
                                DropdownMenuItem(
                                    text = { Text(act.label, fontSize = 12.5.sp) },
                                    onClick = {
                                        viewModel.setActivityFilter(act)
                                        activityDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Sort By Dropdown Menu
                    ExposedDropdownMenuBox(
                        expanded = sortDropdownExpanded,
                        onExpandedChange = { sortDropdownExpanded = !sortDropdownExpanded },
                        modifier = Modifier.weight(1.1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 7.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    sortOrder.label,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(15.dp))
                            }
                        }

                        ExposedDropdownMenu(
                            expanded = sortDropdownExpanded,
                            onDismissRequest = { sortDropdownExpanded = false }
                        ) {
                            MapSortOrder.entries.forEach { order ->
                                DropdownMenuItem(
                                    text = { Text(order.label, fontSize = 12.5.sp) },
                                    onClick = {
                                        viewModel.setMapSortOrder(order)
                                        sortDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Center Map Action Button
                    IconButton(
                        onClick = {
                            centerTrigger++
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = "Center Map", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    }
                }

                // Row 2: Time Filter Pills & Mapped Incidents Counter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(
                            TimeRange.LIVE to "All",
                            TimeRange.HOURS_6 to "6h",
                            TimeRange.HOURS_24 to "24h",
                            TimeRange.MONTH to "Month"
                        ).forEach { (range, label) ->
                            val isSelected = selectedTimeRange == range
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.clickable { viewModel.setTimeRange(range) }
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    ) {
                        Text(
                            "${markers.size} Incidents",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }

        // Bottom Incident SITREP Card when a marker is tapped
        selectedMarker?.let { marker ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(14.dp),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            val badgeColor = when (marker.theater) {
                                TheaterCategory.FAK -> StatusRed
                                TheaterCategory.FAH -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.outline
                            }
                            Surface(color = badgeColor.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp)) {
                                Text(
                                    marker.theater.shortCode,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = badgeColor,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text(marker.district, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }

                        IconButton(onClick = { selectedMarker = null }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(18.dp))
                        }
                    }

                    Text(
                        marker.snippet,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 3
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Button(
                                onClick = { viewModel.sendPostToWhatsApp(marker.post) },
                                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("WhatsApp", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            if (!marker.post.url.isNullOrBlank()) {
                                OutlinedButton(
                                    onClick = { showVideoPlayer = true },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.PlayCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Watch", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { viewModel.downloadImage(marker.post) }) {
                                Icon(Icons.Default.Download, contentDescription = "Download Media", tint = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = { viewModel.toggleBookmark(marker.post) }) {
                                val isSaved = viewModel.isBookmarked(marker.post.id)
                                Icon(
                                    if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "Save",
                                    tint = if (isSaved) StatusAmber else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showVideoPlayer && selectedMarker?.post != null) {
        InbuiltVideoPlayerModal(
            post = selectedMarker!!.post,
            viewModel = viewModel,
            onDismiss = { showVideoPlayer = false }
        )
    }
}

