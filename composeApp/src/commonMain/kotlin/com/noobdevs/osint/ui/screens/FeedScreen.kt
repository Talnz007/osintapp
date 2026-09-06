package com.noobdevs.osint.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.noobdevs.osint.data.models.ActivityCategory
import com.noobdevs.osint.data.models.PostItem
import com.noobdevs.osint.data.models.TheaterCategory
import com.noobdevs.osint.data.models.TimeRange
import com.noobdevs.osint.platform.PlatformActions
import com.noobdevs.osint.ui.MainViewModel
import com.noobdevs.osint.ui.components.InbuiltVideoPlayerModal
import com.noobdevs.osint.ui.components.WhatsAppGreen
import com.noobdevs.osint.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(viewModel: MainViewModel) {
        val posts by viewModel.posts.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()
    val showOnlyBookmarks by viewModel.showOnlyBookmarks.collectAsState()
    val isLoading by viewModel.isPostsLoading.collectAsState()
    val error by viewModel.postsError.collectAsState()
    val selectedTheater by viewModel.selectedTheater.collectAsState()
    val selectedActivity by viewModel.selectedActivity.collectAsState()
    val selectedTimeRange by viewModel.selectedTimeRange.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var activeSearchText by remember { mutableStateOf(searchQuery) }
    var postForVideoDownloader by remember { mutableStateOf<PostItem?>(null) }

    var theaterDropdownExpanded by remember { mutableStateOf(false) }
    var activityDropdownExpanded by remember { mutableStateOf(false) }
    var timeDropdownExpanded by remember { mutableStateOf(false) }

    val basePosts = if (showOnlyBookmarks) bookmarks else posts
    val filteredPosts = remember(basePosts, selectedTheater, selectedActivity) {
        basePosts.filter { post ->
            val matchTheater = selectedTheater == TheaterCategory.ALL || post.detectTheater() == selectedTheater
            val matchActivity = selectedActivity == ActivityCategory.ALL || post.detectActivityType() == selectedActivity
            matchTheater && matchActivity
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // MINIMALIST CORPORATE EXECUTIVE HEADER
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Top Status & Actions Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(StatusGreen))
                        Text(
                            "${filteredPosts.size} Strategic Intercepts",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        // Evidence Bag Toggle
                        FilterChip(
                            selected = showOnlyBookmarks,
                            onClick = { viewModel.toggleShowOnlyBookmarks() },
                            leadingIcon = {
                                Icon(
                                    if (showOnlyBookmarks) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = null,
                                    tint = if (showOnlyBookmarks) StatusAmber else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            label = { Text("Evidence (${bookmarks.size})", fontSize = 11.5.sp, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.height(34.dp)
                        )

                        IconButton(onClick = { viewModel.loadPosts(1, append = false) }, modifier = Modifier.size(34.dp)) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", modifier = Modifier.size(20.dp))
                        }
                    }
                }

                // Search Bar
                OutlinedTextField(
                    value = activeSearchText,
                    onValueChange = {
                        activeSearchText = it
                        viewModel.setSearchQuery(it)
                    },
                    placeholder = { Text("Search by keywords, accounts, districts...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        if (activeSearchText.isNotEmpty()) {
                            IconButton(onClick = {
                                activeSearchText = ""
                                viewModel.setSearchQuery("")
                            }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                )

                // CORPORATE DROPDOWN SELECTION ROW
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Theater Dropdown
                    ExposedDropdownMenuBox(
                        expanded = theaterDropdownExpanded,
                        onExpandedChange = { theaterDropdownExpanded = !theaterDropdownExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .menuAnchor()
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    selectedTheater.shortCode,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
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

                    // Activity Type Dropdown
                    ExposedDropdownMenuBox(
                        expanded = activityDropdownExpanded,
                        onExpandedChange = { activityDropdownExpanded = !activityDropdownExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .menuAnchor()
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    when (selectedActivity) {
                                        ActivityCategory.ALL -> "All Types"
                                        ActivityCategory.ATTACKS -> "Attacks"
                                        ActivityCategory.ACTIVITIES -> "Media"
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
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

                    // Time Window Dropdown
                    ExposedDropdownMenuBox(
                        expanded = timeDropdownExpanded,
                        onExpandedChange = { timeDropdownExpanded = !timeDropdownExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .menuAnchor()
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    selectedTimeRange.label.replace("Last ", ""),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }

                        ExposedDropdownMenu(
                            expanded = timeDropdownExpanded,
                            onDismissRequest = { timeDropdownExpanded = false }
                        ) {
                            TimeRange.entries.forEach { tr ->
                                DropdownMenuItem(
                                    text = { Text(tr.label, fontSize = 12.5.sp) },
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
        }

        // POSTS STREAM
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp)
        ) {
            if (error != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Failed to retrieve wire via live server", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                            Text(error ?: "", fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { viewModel.loadPosts(1, append = false) }) {
                                Text("Retry")
                            }
                        }
                    }
                }
            }

            if (filteredPosts.isEmpty() && !isLoading && error == null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                if (showOnlyBookmarks) Icons.Default.BookmarkBorder else Icons.Default.SearchOff,
                                contentDescription = null,
                                modifier = Modifier.size(44.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                if (showOnlyBookmarks) "Evidence Bag is Empty" else "No matching intercepts",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                if (showOnlyBookmarks) "Tap the bookmark icon on any card to save high-priority intelligence here."
                                else "Try selecting 'All Theaters' or clearing the search query.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }

            items(filteredPosts, key = { it.id }) { post ->
                MinimalistPostCard(
                    post = post,
                    viewModel = viewModel,
                    onOpenVideoDownloader = { postForVideoDownloader = post },
                    onOpenBrowser = { url ->
                        PlatformActions.openUrl(url)
                    }
                )
            }

            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(28.dp))
                    }
                }
            }
        }
    }

    postForVideoDownloader?.let { post ->
        InbuiltVideoPlayerModal(
            post = post,
            viewModel = viewModel,
            onDismiss = { postForVideoDownloader = null }
        )
    }
}

@Composable
fun MinimalistPostCard(
    post: PostItem,
    viewModel: MainViewModel,
    onOpenVideoDownloader: () -> Unit,
    onOpenBrowser: (String) -> Unit
) {
        val theater = post.detectTheater()
    val activity = post.detectActivityType()
    val district = post.detectLocationName()
    val mediaUrl = viewModel.apiClient.getMediaUrl(post.sentimentImagePath)

    val badgeColor = when (theater) {
        TheaterCategory.FAK -> StatusRed
        TheaterCategory.FAH -> MaterialTheme.colorScheme.primary
        TheaterCategory.INTL -> Color(0xFF475569)
        else -> MaterialTheme.colorScheme.outline
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header Row: Faction Tag, District / Province, Date, Browser Link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = badgeColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            theater.shortCode,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = badgeColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (activity == ActivityCategory.ATTACKS) {
                        Surface(
                            color = Color(0xFFB91C1C).copy(alpha = 0.12f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                "ATTACK",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB91C1C),
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(district, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        post.scrapedDate?.take(10) ?: "",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!post.url.isNullOrBlank()) {
                        IconButton(onClick = { onOpenBrowser(post.url) }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "Open Source", modifier = Modifier.size(15.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            // Body Content (Clean High-Contrast Typography for Senior Executives)
            Text(
                post.content ?: post.whatsappMessageSent.orEmpty(),
                fontSize = 14.5.sp,
                lineHeight = 21.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Optional Image Attachment Preview
            if (mediaUrl != null) {
                AsyncImage(
                    model = mediaUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            // Action Row: WhatsApp, Watch, Simple Download, Bookmark, Share
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    // Direct WhatsApp Dispatch
                    Button(
                        onClick = { viewModel.sendPostToWhatsApp(post) },
                        colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "WhatsApp", tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("WhatsApp", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // Inbuilt Video Player Modal
                    if (!post.url.isNullOrBlank()) {
                        OutlinedButton(
                            onClick = onOpenVideoDownloader,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.PlayCircle, contentDescription = null, modifier = Modifier.size(15.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Watch", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Simple Download Icon Button (Clean and direct)
                    IconButton(onClick = { viewModel.downloadImage(post) }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Download, contentDescription = "Download", modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                    }

                    // Save / Bookmark Evidence
                    val isSaved = viewModel.isBookmarked(post.id)
                    IconButton(
                        onClick = { viewModel.toggleBookmark(post) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Save Evidence",
                            tint = if (isSaved) StatusAmber else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Share SITREP
                    IconButton(
                        onClick = { viewModel.sharePost(post) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share SITREP", modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
