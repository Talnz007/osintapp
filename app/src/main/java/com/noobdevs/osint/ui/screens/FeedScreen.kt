package com.noobdevs.osint.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.noobdevs.osint.data.PipelineFilter
import com.noobdevs.osint.data.PostItem
import com.noobdevs.osint.data.TimeRange
import com.noobdevs.osint.ui.MainViewModel
import com.noobdevs.osint.ui.components.InbuiltVideoPlayerModal
import com.noobdevs.osint.ui.components.WhatsAppGreen
import com.noobdevs.osint.ui.theme.*
import com.noobdevs.osint.util.MediaDownloadHelper
import com.noobdevs.osint.util.ShareHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val posts by viewModel.posts.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()
    val showOnlyBookmarks by viewModel.showOnlyBookmarks.collectAsState()
    val isLoading by viewModel.isPostsLoading.collectAsState()
    val error by viewModel.postsError.collectAsState()
    val selectedProvince by viewModel.selectedProvince.collectAsState()
    val selectedAttackType by viewModel.selectedAttackType.collectAsState()
    val selectedTimeRange by viewModel.selectedTimeRange.collectAsState()
    val selectedPipeline by viewModel.selectedPipeline.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var activeSearchText by remember { mutableStateOf(searchQuery) }
    var postForVideoDownloader by remember { mutableStateOf<PostItem?>(null) }
    var showFilterSheet by remember { mutableStateOf(false) }

    val provinces = listOf("All", "Balochistan", "KPK", "AJK / Kashmir", "Sindh", "Punjab", "International")
    val hasActiveFilters = selectedTimeRange != TimeRange.LIVE || selectedPipeline != PipelineFilter.ALL || selectedAttackType != "ALL"

    // Filter posts by evidence bag selection and province
    val basePosts = if (showOnlyBookmarks) bookmarks else posts
    val filteredPosts = remember(basePosts, selectedProvince) {
        if (selectedProvince == "All") basePosts
        else basePosts.filter { it.detectProvince().equals(selectedProvince, ignoreCase = true) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // EXECUTIVE CLEAN HEADER
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Title & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Intelligence Wire",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(StatusGreen))
                        Text(
                            "Live Operational Feed • %,d Intercepts".format(posts.size),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                if (showOnlyBookmarks && bookmarks.isNotEmpty()) {
                    FilledTonalButton(
                        onClick = { viewModel.exportEvidenceDossier() },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export Dossier", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Search Bar + Filter Dialog Trigger Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = activeSearchText,
                    onValueChange = {
                        activeSearchText = it
                        viewModel.setSearchQuery(it)
                    },
                    placeholder = { Text("Search wire, accounts, incidents...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
                    trailingIcon = {
                        if (activeSearchText.isNotEmpty()) {
                            IconButton(onClick = {
                                activeSearchText = ""
                                viewModel.setSearchQuery("")
                            }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )

                // Dedicated Filter Button with Active Badge
                FilledTonalButton(
                    onClick = { showFilterSheet = true },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp)
                ) {
                    BadgedBox(badge = {
                        if (hasActiveFilters) {
                            Badge { Text("!") }
                        }
                    }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filters", modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Filters", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Single Row Zone & Evidence Bag Filter Bar
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
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
                        label = { Text("Evidence Bag (${bookmarks.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StatusAmber.copy(alpha = 0.2f),
                            selectedLabelColor = StatusAmber
                        ),
                        modifier = Modifier.height(34.dp)
                    )
                }

                items(provinces) { province ->
                    FilterChip(
                        selected = !showOnlyBookmarks && selectedProvince == province,
                        onClick = {
                            if (showOnlyBookmarks) viewModel.toggleShowOnlyBookmarks()
                            viewModel.setProvinceFilter(province)
                        },
                        label = { Text(if (province == "All") "All Regions" else province, fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                        modifier = Modifier.height(34.dp)
                    )
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))

        // Posts Stream
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
                            Text("Failed to retrieve intelligence wire", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
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
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = 32.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                if (showOnlyBookmarks) Icons.Default.BookmarkBorder else Icons.Default.SearchOff,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                if (showOnlyBookmarks) "Evidence Bag is Empty" else "No matching intelligence records",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                if (showOnlyBookmarks) "Save important posts using the bookmark icon to review them later in your Evidence Bag."
                                else "Try resetting filters or searching with different keywords.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }

            items(filteredPosts, key = { it.id }) { post ->
                EnhancedPostCard(
                    post = post,
                    viewModel = viewModel,
                    onOpenVideoDownloader = { postForVideoDownloader = post },
                    onOpenBrowser = { url -> MediaDownloadHelper.openUrlInBrowser(context, url) }
                )
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(28.dp))
                    } else if (filteredPosts.isNotEmpty()) {
                        OutlinedButton(
                            onClick = { viewModel.loadNextPage() },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.ExpandMore, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Load More Intelligence (${filteredPosts.size} showing)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }

    // CLEAN FILTER DIALOG
    if (showFilterSheet) {
        Dialog(onDismissRequest = { showFilterSheet = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Filter Intelligence Stream", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showFilterSheet = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    // 1. Time Window
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("TIME WINDOW", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            TimeRange.values().forEach { tr ->
                                FilterChip(
                                    selected = selectedTimeRange == tr,
                                    onClick = { viewModel.setTimeRange(tr) },
                                    label = { Text(tr.label, fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // 2. Feed Source Pipeline
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("INTEL STREAM PIPELINE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            PipelineFilter.values().forEach { pipe ->
                                FilterChip(
                                    selected = selectedPipeline == pipe,
                                    onClick = { viewModel.setPipelineFilter(pipe) },
                                    label = { Text(pipe.label, fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // 3. Incident Type
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("INCIDENT & ATTACK CATEGORY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StatusRed)
                        val attackTypes = listOf("ALL", "AMBUSH_FIRE", "IED_EXPLOSIVE", "CIVIL_UNREST", "SABOTAGE")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(attackTypes) { attack ->
                                FilterChip(
                                    selected = selectedAttackType == attack,
                                    onClick = { viewModel.setAttackTypeFilter(attack) },
                                    label = { Text(attack.replace("_", " "), fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    // Dialog Actions
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = {
                                viewModel.setTimeRange(TimeRange.LIVE)
                                viewModel.setPipelineFilter(PipelineFilter.ALL)
                                viewModel.setAttackTypeFilter("ALL")
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Reset")
                        }

                        Button(
                            onClick = { showFilterSheet = false },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Apply")
                        }
                    }
                }
            }
        }
    }

    // INBUILT VIDEO PLAYER & MEDIA MODAL
    postForVideoDownloader?.let { post ->
        InbuiltVideoPlayerModal(
            post = post,
            viewModel = viewModel,
            onDismiss = { postForVideoDownloader = null }
        )
    }
}

@Composable
fun EnhancedPostCard(
    post: PostItem,
    viewModel: MainViewModel,
    onOpenVideoDownloader: () -> Unit,
    onOpenBrowser: (String) -> Unit
) {
    val context = LocalContext.current
    val province = post.detectProvince()
    val mediaUrl = viewModel.apiClient.getMediaUrl(post.sentimentImagePath)

    val provinceColor = when (province) {
        "Balochistan" -> StatusAmber
        "KPK" -> StatusPurple
        "AJK / Kashmir" -> MaterialTheme.colorScheme.primary
        "Sindh" -> StatusGreen
        "Punjab" -> Color(0xFF0284C7)
        "International" -> Color(0xFF2563EB)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val isAttack = post.attackType == "AMBUSH_FIRE" || post.attackType == "IED_EXPLOSIVE" || post.attackType == "SABOTAGE"

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isAttack) StatusRed.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                RoundedCornerShape(16.dp)
            )
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header: Account & Province
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            post.account?.take(1)?.uppercase() ?: "@",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontSize = 14.sp
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(post.account ?: "Unknown Source", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(
                            "${post.sourcePipeline ?: "Field Stream"} • ${post.scrapedDate?.take(16)?.replace("T", " ") ?: "Recent"}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = provinceColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        province,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = provinceColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            // High-Priority Incident Indicators (Clean & dignified)
            if (isAttack || post.isShaheedIncident == 1 || post.isHighProfile == 1) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!post.attackType.isNullOrBlank() && post.attackType != "GENERAL") {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isAttack) StatusRed.copy(alpha = 0.15f) else MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(if (isAttack) StatusRed else MaterialTheme.colorScheme.primary))
                                Text(
                                    post.attackType.replace("_", " "),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAttack) StatusRed else MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                    if (post.isShaheedIncident == 1) {
                        Surface(shape = RoundedCornerShape(6.dp), color = StatusRed.copy(alpha = 0.15f)) {
                            Text("CASUALTY REPORT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StatusRed, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                        }
                    }
                    if (post.isHighProfile == 1) {
                        Surface(shape = RoundedCornerShape(6.dp), color = StatusAmber.copy(alpha = 0.15f)) {
                            Text("HIGH PROFILE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StatusAmber, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                        }
                    }
                }
            }

            // Highly Legible Content Body
            Text(
                post.content.orEmpty(),
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Normal
            )

            // Intercepted Image Preview (if available)
            if (mediaUrl != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(mediaUrl)
                            .addHeader("Authorization", viewModel.authManager.getBasicAuthHeader())
                            .crossfade(true)
                            .build(),
                        contentDescription = "Intercepted Media",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Hashtags (if any)
            if (post.hashtags.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(post.hashtags) { tag ->
                        Text(tag, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            // Action Row with WhatsApp, Inbuilt Video Player, Save Pic, Bookmark, and Share
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    // DIRECT WHATSAPP BUTTON (CORPORATE DISPATCH)
                    Button(
                        onClick = { ShareHelper.sendPostToWhatsApp(context, post) },
                        colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "WhatsApp", tint = Color.White, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text("WhatsApp", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // Inbuilt Video Player Trigger
                    if (!post.url.isNullOrBlank()) {
                        OutlinedButton(
                            onClick = onOpenVideoDownloader,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.PlayCircle, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Watch", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (mediaUrl != null) {
                        FilledTonalButton(
                            onClick = { viewModel.downloadImage(post) },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Photo", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
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

                    IconButton(
                        onClick = { viewModel.sharePost(post) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Share SITREP",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    if (!post.url.isNullOrBlank()) {
                        IconButton(onClick = { onOpenBrowser(post.url) }, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "Open Source Link", modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}
